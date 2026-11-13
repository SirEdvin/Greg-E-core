package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.coolant;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class CoolantOutputPartMachine extends FluidHatchPartMachine {

    public static final PartAbility COOLANT_OUTPUT = new PartAbility("coolant_output");

    public CoolantOutputPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH, 20000, 1);
    }

    public static PartAbility getPartAbility() {
        return COOLANT_OUTPUT;
    }

    public static final MachineDefinition COOLANT_OUTPUT_MACHINE = REGISTRATE.machine("coolant_output_machine", (holder) ->
                    new CoolantOutputPartMachine(holder, GTValues.EV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(CoolantOutputPartMachine.COOLANT_OUTPUT)
            .tier(GTValues.EV)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/coolant_output/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.coolantoutput1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();


    public static void init() {
    }
}
