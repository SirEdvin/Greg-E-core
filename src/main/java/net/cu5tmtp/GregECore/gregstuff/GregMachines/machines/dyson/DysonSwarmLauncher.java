package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.managers.DysonSwarmManager;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class DysonSwarmLauncher extends WorkableElectricMultiblockMachine {
    public DysonSwarmLauncher(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            DysonSwarmLauncher.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER
    );
    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        assert recipe != null;
        int num = recipe.data.getInt("sailMultiplier");

        if (getLevel() instanceof ServerLevel serverLevel) {

            if (num == 1) {
                DysonSwarmManager.get(serverLevel).addSail50();
                return super.beforeWorking(recipe);
            }

            if (num == 2) {
                DysonSwarmManager.get(serverLevel).addSail150();
                return super.beforeWorking(recipe);
            }

            DysonSwarmManager.get(serverLevel).addSail();
        }
        return super.beforeWorking(recipe);
    }

    public static MachineDefinition DYSON_SWARM_LAUNCHER = REGISTRATE
            .multiblock("dysonswarmlauncher", DysonSwarmLauncher::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.LAUNCH_SAILS)
            .appearanceBlock(COMPUTER_CASING)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("GGGGGGG", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", " H   H ", " H   H ", " H   H ", " H   H ", " H   H ", " H   H ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", "  III  ", "  IJI  ", "  III  ", "   I   ", "       ", "       ", "  H H  ", "  H H  ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", "  III  ", "  JKJ  ", "  IKI  ", "  IKI  ", "   K   ", "   K   ", "   K   ", "   K   ", "   K   ", "   K   ", "   L   ", "   K   ", "   K   ", "   L   ", "   K   ", "   K   ", "   L   ", "   K   ", "   K   ", "   K   ", "   M   ")
                        .aisle("GGGGGGG", "  III  ", "  IJI  ", "  III  ", "   I   ", "       ", "       ", "  H H  ", "  H H  ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", " H   H ", " H   H ", " H   H ", " H   H ", " H   H ", " H   H ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", " BFFFB ", " BFFFB ", " BFFFB ", " BBBBB ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", " BEEEB ", " BADDB ", " BDDDB ", " BBBBB ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .aisle("GGGGGGG", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ", "       ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(COMPUTER_CASING.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(1)))
                        .where('D', Predicates.blocks(ADVANCED_COMPUTER_CASING.get()))
                        .where('E', Predicates.blocks(HIGH_POWER_CASING.get()))
                        .where('F', Predicates.blocks(COMPUTER_HEAT_VENT.get()))
                        .where('G', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                        .where('H', Predicates.blocks(CASING_STEEL_TURBINE.get()))
                        .where('I', Predicates.blocks(GCYMBlocks.CASING_STRESS_PROOF.get()))
                        .where('J', Predicates.blocks(CASING_EXTREME_ENGINE_INTAKE.get()))
                        .where('K', Predicates.blocks(FUSION_CASING.get()))
                        .where('L', Predicates.blocks(FUSION_COIL.get()))
                        .where('M', Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                        .where(' ', Predicates.any())
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/hpca/computer_casing/front"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createDysonSwarmLauncherRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysonlauncher1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysonlauncher2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysonlauncher3").withStyle(style -> style.withColor(0x90EE90)))
            .register();




    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {

            double currentSails = 0;

            if (getLevel() instanceof ServerLevel serverLevel) {
                DysonSwarmManager manager = DysonSwarmManager.get(serverLevel);
                currentSails = manager.getTotalSails();
            }

            textList.add(Component.translatable("info.gregecore.dysonlauncher4").withStyle(ChatFormatting.GREEN));
            textList.add(Component.literal("" + (int) currentSails).withStyle(ChatFormatting.AQUA));
        }
    }

    public static void init() {}
}
