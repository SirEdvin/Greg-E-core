package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.managers.DysonSwarmManager;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.DysonSwarmEnergyOutputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class DysonSwarmEnergyCollector extends WorkableElectricMultiblockMachine {
    public DysonSwarmEnergyCollector(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

        public static MachineDefinition DYSON_SWARM_LAUNCHER = REGISTRATE
            .multiblock("dysonswarmenergycollector", DysonSwarmEnergyCollector::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.GET_SOLAR_SAIL_ENERGY)
            .recipeModifiers(GregEModifiers::dysonSwarmGenBoost)
            .appearanceBlock(COMPUTER_CASING)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("FFFFFFFFFFF", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "           ", "           ", "           ", "           ", "           ", "           ", "   IIIII   ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "           ", "           ", "           ", "           ", "           ", "   IIIII   ", "  IJ   JI  ", "   J   J   ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "    HHH    ", "     H     ", "     H     ", "    HHH    ", "    III    ", "   I   I   ", "  I     I  ", "           ", "    J J    ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "    HGGGG  ", "    HGH    ", "    HGH    ", "    HGH    ", "    IGI    ", "   I J I   ", "  I  J  I  ", "     J     ", "     J     ", "     J     ", "     J     ", "     J     ", "     K     ")
                        .aisle("FFFFFFFFFFF", "    HHH G  ", "     H     ", "     H     ", "    HHH    ", "    III    ", "   I   I   ", "  I     I  ", "           ", "    J J    ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "        G  ", "           ", "           ", "           ", "           ", "   IIIII   ", "  IJ   JI  ", "   J   J   ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", " BEEEB LGL ", " BEEEB LLL ", " BEEEB     ", " BBBBB     ", "           ", "           ", "   IIIII   ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", " BDDDB LLL ", " BACCB LLL ", " BCCCB     ", " BBBBB     ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("FFFFFFFFFFF", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(COMPUTER_CASING.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2)))
                        .where('C', Predicates.blocks(ADVANCED_COMPUTER_CASING.get()))
                        .where('D', Predicates.blocks(HIGH_POWER_CASING.get()))
                        .where('E', Predicates.blocks(COMPUTER_HEAT_VENT.get()))
                        .where('F', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                        .where('G', Predicates.blocks(CASING_STEEL_GEARBOX.get()))
                        .where('H', Predicates.blocks(GCYMBlocks.CASING_STRESS_PROOF.get()))
                        .where('I', Predicates.blocks(GCYMBlocks.CASING_SECURE_MACERATION.get()))
                        .where('J', Predicates.blocks(CASING_PTFE_INERT.get()))
                        .where('K', Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                        .where('L', Predicates.blocks(COMPUTER_CASING.get())
                                .or(Predicates.abilities(PartAbility.OUTPUT_ENERGY).setMaxGlobalLimited(11).setPreviewCount(1))
                                .or(Predicates.abilities(DysonSwarmEnergyOutputPartMachine.getPartAbility()).setMaxGlobalLimited(11, 1)))
                        .where(' ', Predicates.any())
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/hpca/computer_casing/front"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createDysonSwarmEnergyCollectorRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysoncollector1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysoncollector2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.dysoncollector3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        if (isFormed()) {

            double currentBoost = 0;

            if (getLevel() instanceof ServerLevel serverLevel) {
                DysonSwarmManager manager = DysonSwarmManager.get(serverLevel);
                currentBoost = manager.getBoost();
            }

            textList.add(Component.translatable("info.gregecore.dysoncollector4").withStyle(ChatFormatting.GREEN));
            textList.add(Component.literal((int) currentBoost + "").withStyle(ChatFormatting.AQUA));
        }
    }

    public static void init() {}
}
