package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces;

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
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT2PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.AdvancedParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel.ParallelBoosterPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEPredicates;
import net.cu5tmtp.GregECore.item.GreggyItems;
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

public class GiantAcceleratedEBF extends WorkableElectricMultiblockMachine {

    public GiantAcceleratedEBF(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private int coilTemp = 0;
    private IFluidHandler coolantHandler;
    public int parallelBooster = 0;
    private boolean canBeThreaded = false;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        this.coilTemp = GregEPredicates.getMagicalCoilTemperature(getMultiblockState().getMatchContext());

        List<IFluidHandler> coolantContainers = new ArrayList<>();

        //This part of the code is the same as HPCA coolant consuming from base GTCEu - thanks for teaching me how to do that!
        for (IMultiPart part : getParts()) {

            if(part instanceof ParallelBoosterPartMachine){
                parallelBooster = 1;
            }

            if(part instanceof AdvancedParallelBoosterPartMachine){
                parallelBooster = 2;
            }

            if(part instanceof ThreadT2PartMachine){
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
        int amountToDrain = 10;
        Fluid coolant = GreggyItems.DEIONIZED_WATER.getFluid();
        FluidStack resource = new FluidStack(coolant, amountToDrain);

        if (getOffsetTimer() % 20 == 0) {

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

        //Check if input hatches have enough coolant
        int amountToDrain = 10;
        Fluid coolant = GreggyItems.DEIONIZED_WATER.getFluid();
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
        parallelBooster = 0;
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }

        this.coilTemp = 0;

        super.onStructureInvalid();
    }

    public int getMaxTemp() {
        return this.coilTemp;
    }

    public static MachineDefinition GIANTACCELERATEDEBF = REGISTRATE
            .multiblock("giantacceleratedebf", GiantAcceleratedEBF::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GregEModifiers::giantAcceleratedEBFModifier, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(FIREBOX_TUNGSTENSTEEL)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("BFFFB", "G   G", "G   G", "G   G", "G   G", "G   G", "BFFFB")
                        .aisle("BBBBB", " DDD ", " DDD ", " EEE ", " DDD ", " DDD ", "BBBBB")
                        .aisle("BBBBB", " D D ", " D D ", " E E ", " D D ", " D D ", "BBCBB")
                        .aisle("BBBBB", " DDD ", " DDD ", " EEE ", " DDD ", " DDD ", "BBBBB")
                        .aisle("BHAIB", "G   G", "G   G", "G   G", "G   G", "G   G", "BFFFB")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(CASING_TUNGSTENSTEEL_ROBUST.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(CoolantInputPartMachine.getPartAbility()).setExactLimit(1)))
                        .where('C', Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
                        .where('D', GregEPredicates.tierTwoMagicalCoils())
                        .where('E', Predicates.blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                        .where('F', Predicates.blocks(FIREBOX_TUNGSTENSTEEL.get()))
                        .where('G', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:tungsten_carbide_frame"))))
                        .where('H', Predicates.blocks(FIREBOX_TUNGSTENSTEEL.get())
                                .or(Predicates.abilities(ParallelBoosterPartMachine.getPartAbility()).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(AdvancedParallelBoosterPartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('I', Predicates.blocks(FIREBOX_TUNGSTENSTEEL.get())
                                        .or(Predicates.abilities(ThreadT2PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/firebox/machine_casing_firebox_tungstensteel"),
                                 GTCEu.id("block/multiblock/distillation_tower"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gaebf1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gaebf2").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gaebf3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gaebf4").withStyle(style -> style.withColor(0x90EE90)).append(Component.translatable("info.gregecore.gaebf5").withStyle(style -> style.withColor(0xFF0000))))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gaebf6").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.gaebf7").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.threading2"))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {

            if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic && logic.isMultiThreaded()) {
                List<MultiThreadedRecipeLogic.RecipeThread> threads = logic.getActiveThreads();
                for (int i = 0; i < threads.size(); i++) {
                    var thread = threads.get(i);
                    int percent = thread.duration > 0 ? (int) (((float) thread.progress / thread.duration) * 100) : 0;
                    textList.add(Component.literal("  Thread " + (i + 1) + ": " + percent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }

            textList.add(Component.translatable("info.gregecore.gaebf8").append(coilTemp + "K").withStyle(ChatFormatting.AQUA));
            switch (coilTemp){
                case 7400 -> {
                    textList.add(Component.translatable("info.gregecore.gaebf9").withStyle(ChatFormatting.GREEN));
                    textList.add(Component.translatable("info.gregecore.gaebf10" ).withStyle(ChatFormatting.GREEN));
                }
                case 9300 -> {
                    textList.add(Component.translatable("info.gregecore.gaebf11").withStyle(ChatFormatting.GREEN));
                    textList.add(Component.translatable("info.gregecore.gaebf12").withStyle(ChatFormatting.GREEN));
                }
                case 11000 -> {
                    textList.add(Component.translatable("info.gregecore.gaebf13").withStyle(ChatFormatting.GREEN));
                    textList.add(Component.translatable("info.gregecore.gaebf14").withStyle(ChatFormatting.GREEN));
                }
                default -> textList.add(Component.translatable("info.gregecore.gaebf15").withStyle(ChatFormatting.RED));
            }
            switch (parallelBooster){
                case 1 -> textList.add(Component.translatable("info.gregecore.gaebf16").withStyle(ChatFormatting.LIGHT_PURPLE));
                case 2 -> textList.add(Component.translatable("info.gregecore.gaebf17").withStyle(ChatFormatting.LIGHT_PURPLE));
                default -> textList.add(Component.translatable("info.gregecore.gaebf18").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    public static void init() {}
}
