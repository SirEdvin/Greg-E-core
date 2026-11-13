package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.fluid.FluidHandlerList;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.block.ModBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant.CoolantOutputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

@SuppressWarnings("removal")
public class FissionReactor extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            FissionReactor.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private TickableSubscription heatSubscription;

    @Persisted
    public int heatLevel = 0;

    @Persisted
    private int recipeTemp = 0;

    @Persisted
    private int hullDmg = 100;

    private IFluidHandler coolantHandlerInput;
    private IFluidHandler coolantHandlerOutput;

    private static final int MAX_HEAT = 3000;

    public FissionReactor(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        List<IFluidHandler> coolantContainers = new ArrayList<>();

        for (IMultiPart part : getParts()) {

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
        this.coolantHandlerInput = new FluidHandlerList(coolantContainers);

        coolantContainers.clear();

        for (IMultiPart part : getParts()) {

            if(!(part instanceof CoolantOutputPartMachine)){
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
        this.coolantHandlerOutput = new FluidHandlerList(coolantContainers);

        if (heatSubscription != null) {
            heatSubscription.unsubscribe();
            heatSubscription = null;
        }

        heatSubscription = this.subscribeServerTick(this::manageHeat);
    }

    private void manageHeat() {

        if (this.getLevel() != null && this.getLevel().isClientSide) return;

        if (getOffsetTimer() % 20 == 0) {

            if (this.getRecipeLogic().isWorking()) {
                this.heatLevel += recipeTemp;
            }
        }

        if (getOffsetTimer() % 5 == 0) {

            if (this.heatLevel > 0 && this.coolantHandlerInput != null && this.coolantHandlerOutput != null) {
                int maxDrainAmount = 10;
                var simulatedDrain = this.coolantHandlerInput.drain(maxDrainAmount, IFluidHandler.FluidAction.SIMULATE);

                if (!simulatedDrain.isEmpty()) {
                    int coolingPower = getFluidCoolingPower(simulatedDrain);
                    FluidStack hotFluidOut = getHeatedCoolant(simulatedDrain);

                    if (coolingPower > 0 && hotFluidOut != null) {
                        hotFluidOut.setAmount(maxDrainAmount);

                        int acceptedFill = this.coolantHandlerOutput.fill(hotFluidOut, IFluidHandler.FluidAction.SIMULATE);

                        if (acceptedFill > 0) {
                            var actualDrained = this.coolantHandlerInput.drain(acceptedFill, IFluidHandler.FluidAction.EXECUTE);
                            hotFluidOut.setAmount(actualDrained.getAmount());
                            this.coolantHandlerOutput.fill(hotFluidOut, IFluidHandler.FluidAction.EXECUTE);

                            this.heatLevel -= coolingPower;
                            if (this.heatLevel < 0) this.heatLevel = 0;
                        }
                    }
                }
            }

            if (this.heatLevel > 0 && !this.getRecipeLogic().isWorking()) {
                this.heatLevel -= 5;
                if (this.heatLevel < 0) this.heatLevel = 0;
            }

            if (this.heatLevel > MAX_HEAT) {
                this.hullDmg -= 1;

                if (this.hullDmg <= 0) {
                    explodeMachine();
                }
            } else {
                if (this.heatLevel < (MAX_HEAT / 2) && this.hullDmg < 100) {
                    if (getOffsetTimer() % 20 == 0) {
                        this.hullDmg++;
                    }
                }
            }
        }
    }

    private void explodeMachine() {
        net.minecraft.world.level.Level level = getLevel();
        if (level != null && !level.isClientSide) {
            var pos = getPos();
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15.0F, Level.ExplosionInteraction.BLOCK);
        }
    }

    private int getFluidCoolingPower(FluidStack fluidStack) {
        String fluidKey = ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString();

        switch (fluidKey) {
            case "gtceu:sodium_coolant": return 50;
            case "gtceu:bose_einstein_condensate": return 250;
            default: return 0;
        }
    }

    private FluidStack getHeatedCoolant(FluidStack inputCoolant) {
        String fluidKey = ForgeRegistries.FLUIDS.getKey(inputCoolant.getFluid()).toString();
        Fluid outputFluid = null;

        if (fluidKey.equals("gtceu:sodium_coolant")) {
            outputFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtceu:superheated_sodium"));
        }

        if (fluidKey.equals("gtceu:bose_einstein_condensate")) {
            outputFluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation("gtceu:superheated_bose_einstein_condensate"));
        }

        if (outputFluid != null && outputFluid != Fluids.EMPTY) {
            return new FluidStack(outputFluid, 1);
        }
        return null;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        assert recipe != null;
        this.recipeTemp = recipe.data.getInt("heatgen");
        return super.beforeWorking(recipe);
    }

    public static MachineDefinition FISSIONREACTOR = REGISTRATE
            .multiblock("fissionreactor", FissionReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GregERecipeTypes.FISSION_REACTION)
            .appearanceBlock(CASING_TUNGSTENSTEEL_TURBINE)
            .recipeModifier(GTRecipeModifiers.PARALLEL_HATCH)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("ggggg", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "hhhhh")
                        .aisle("gaaag", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "heeeh")
                        .aisle("gaaag", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "heeeh")
                        .aisle("gaaag", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "cdddc", "heeeh")
                        .aisle("ggfgg", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "hhhhh")
                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:tungstensteel_turbine_casing"))))
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:ptfe_pipe_casing"))))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                        .where("d", Predicates.any())
                        .where("e", Predicates.blocks(ModBlocks.NOZZLE.get()))
                        .where('f', Predicates.controller(blocks(definition.getBlock())))
                        .where('g', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:tungstensteel_turbine_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(CoolantInputPartMachine.COOLANT_INPUT).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('h', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:tungstensteel_turbine_casing")))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(CoolantOutputPartMachine.COOLANT_OUTPUT).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/mechanic/machine_casing_turbine_tungstensteel"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createFissionRodRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fission1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fission2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fission3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fission4").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.fission5").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.fission6").withStyle(style -> style.withColor(0xBF40BF)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.fission7").withStyle(ChatFormatting.AQUA).append(this.hullDmg + "% / 100%").withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("info.gregecore.fission8").withStyle(ChatFormatting.AQUA).append(this.heatLevel + "K / 3000K").withStyle(ChatFormatting.AQUA));
        }
        super.addDisplayText(textList);
    }

    public static void init() {}
}