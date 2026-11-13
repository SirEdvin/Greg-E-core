package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc;

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

public class PressurePartMachine extends FluidHatchPartMachine {

    public static final PartAbility PRESSURE_INPUT = new PartAbility("pressure_input");

    public PressurePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH, 2500, 1);
    }

    public static PartAbility getPartAbility() {
        return PRESSURE_INPUT;
    }

    public static final MachineDefinition PRESSURE_INPUT_MACHINE = REGISTRATE.machine("pressure_input_machine", (holder) ->
                    new PressurePartMachine(holder, GTValues.LV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(PressurePartMachine.PRESSURE_INPUT)
            .tier(GTValues.LV)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/coolant_input/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.pressure1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();

    public static void init() {
    }
}
