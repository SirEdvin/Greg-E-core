package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.chillers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.AdvancedParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.ParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT2PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

@SuppressWarnings("removal")
public class BigFreezer extends WorkableElectricMultiblockMachine {

    public BigFreezer(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private IFluidHandler coolantHandler;

    public int parallelBooster = 0;

    public boolean canBeThreaded = false;

    @Override
    public void onStructureFormed() {

        super.onStructureFormed();
        List<IFluidHandler> coolantContainers = new ArrayList<>();

        for (IMultiPart part : getParts()) {

            if(part instanceof ParallelBoosterPartMachine){
                parallelBooster = 1;
            }

            if(part instanceof AdvancedParallelBoosterPartMachine){
                parallelBooster = 2;
            }

            if (part instanceof ThreadT2PartMachine) {
                canBeThreaded = true;
                if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
                    logic.setMultiThreaded(canBeThreaded);
                }
            }

            if(!(part instanceof CoolantInputPartMachine)){
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(FluidRecipeCapability.CAP).stream()
                        .filter(IFluidHandler.class::isInstance)
                        .map(IFluidHandler.class::cast)
                        .forEach(coolantContainers::add);
            }
        }

        this.coolantHandler = new FluidHandlerList(coolantContainers);


    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 4);
    }

    @Override
    public boolean onWorking() {

        int amountToDrain = 100;
        Fluid coolant = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtceu:ice"));
        FluidStack resource = new FluidStack(coolant, amountToDrain);

        if (getOffsetTimer() % 10 == 0) {

            FluidStack simulation = GTTransferUtils.drainFluidAccountNotifiableList(
                    coolantHandler,
                    resource,
                    IFluidHandler.FluidAction.SIMULATE
            );

            if(simulation.isEmpty()) {
                getRecipeLogic().interruptRecipe();
                return false;
            }

            GTTransferUtils.drainFluidAccountNotifiableList(
                    coolantHandler,
                    resource,
                    IFluidHandler.FluidAction.EXECUTE
            );
        }
        return super.onWorking();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        int amountToDrain = 100;
        Fluid coolant = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtceu:ice"));
        FluidStack resource = new FluidStack(coolant, amountToDrain);

        FluidStack simulation = GTTransferUtils.drainFluidAccountNotifiableList(
                coolantHandler,
                resource,
                IFluidHandler.FluidAction.SIMULATE
        );

        if(simulation.isEmpty()) {
            return false;
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureInvalid() {
        this.parallelBooster = 0;
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }
        super.onStructureInvalid();
    }

    public static MachineDefinition BIGFREEZER = REGISTRATE
            .multiblock("bigfreezer", BigFreezer::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.VACUUM_RECIPES)
            .recipeModifiers(GregEModifiers::bigFreezerBoost, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(CASING_ALUMINIUM_FROSTPROOF)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("BBBBB", "GCCCG", "GCCCG", "GCCCG", "BBBBB")
                        .aisle("BBBBB", "C   C", "C   C", "C   C", "BBBBB")
                        .aisle("BBBBB", "C D C", "C D C", "C D C", "BBBBB")
                        .aisle("BBBBB", "C   C", "C   C", "C   C", "BBBBB")
                        .aisle("BEAFB", "GCCCG", "GCCCG", "GCCCG", "BBBBB")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(CASING_ALUMINIUM_FROSTPROOF.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(ThreadT2PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('C', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:tempered_glass"))))
                        .where('D', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:extreme_engine_intake_casing"))))
                        .where('E', Predicates.blocks(CASING_ALUMINIUM_FROSTPROOF.get())
                                .or(Predicates.abilities(ParallelBoosterPartMachine.getPartAbility()).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(AdvancedParallelBoosterPartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('F', Predicates.abilities(CoolantInputPartMachine.getPartAbility()).setMaxGlobalLimited(1))
                        .where('G', Predicates.blocks(CASING_ALUMINIUM_FROSTPROOF.get()))
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_frost_proof"),
                                 GTCEu.id("block/multiblock/distillation_tower"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.bigfreezer1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.bigfreezer2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.bigfreezer3").withStyle(style -> style.withColor(0x90EE90))
                    .append(Component.translatable("info.gregecore.bigfreezer4").withStyle(style -> style.withColor(0xFF0000))))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.bigfreezer5").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.bigfreezer6").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.bigfreezer7").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.bigfreezer8").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.threading2"))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic && logic.isMultiThreaded()) {
            List<MultiThreadedRecipeLogic.RecipeThread> threads = logic.getActiveThreads();
            for (int i = 0; i < threads.size(); i++) {
                var thread = threads.get(i);
                int percent = thread.duration > 0 ? (int) (((float) thread.progress / thread.duration) * 100) : 0;
                textList.add(Component.literal("  Thread " + (i + 1) + ": " + percent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }

        switch (parallelBooster)
        {
            case 2 -> {
                textList.add(Component.translatable("info.gregecore.bigfreezer9").withStyle(ChatFormatting.AQUA));
                textList.add(Component.translatable("info.gregecore.bigfreezer10").withStyle(ChatFormatting.AQUA));
            }
            case 1 -> {
                textList.add(Component.translatable("info.gregecore.bigfreezer11").withStyle(ChatFormatting.AQUA));
                textList.add(Component.translatable("info.gregecore.bigfreezer12").withStyle(ChatFormatting.AQUA));
            }
            default -> {
                textList.add(Component.translatable("info.gregecore.bigfreezer13").withStyle(ChatFormatting.AQUA));
                textList.add(Component.translatable("info.gregecore.bigfreezer14").withStyle(ChatFormatting.AQUA));
            }
        }
    }

    public int getParallelBooster() {
        return parallelBooster == 0 ? 1 : parallelBooster * 2;
    }

    public static void init() {}
}
