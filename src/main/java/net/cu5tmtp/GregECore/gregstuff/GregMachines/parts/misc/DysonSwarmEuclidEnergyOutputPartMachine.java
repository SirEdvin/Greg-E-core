package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.EnergyHatchPartMachine;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class DysonSwarmEuclidEnergyOutputPartMachine extends EnergyHatchPartMachine {

    public static final PartAbility DYSON_SWARM_EN_EUCLID_OUTPUT = new PartAbility("dyswarmoutputeuclid");

    public DysonSwarmEuclidEnergyOutputPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.OUT, 1024);
    }

    public static PartAbility getPartAbility() {
        return DYSON_SWARM_EN_EUCLID_OUTPUT;
    }

    public static final MachineDefinition DYSON_SWARM_EN_OUTPUT_EUCLID_MACHINE = REGISTRATE.machine("dyswarmoutputeuclid_machine", (holder) ->
                    new DysonSwarmEuclidEnergyOutputPartMachine(holder, GTValues.UIV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(DysonSwarmEuclidEnergyOutputPartMachine.DYSON_SWARM_EN_EUCLID_OUTPUT)
            .tier(GTValues.UIV)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/machine/overlay_energy_16a_in"))
            .tooltips(Component.translatable("info.gregecore.dysonoutput1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.dysonoutput2").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.dysonoutput5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.dysonoutput4").withStyle(ChatFormatting.LIGHT_PURPLE)))
            .register();


    public static void init() {
    }
}
