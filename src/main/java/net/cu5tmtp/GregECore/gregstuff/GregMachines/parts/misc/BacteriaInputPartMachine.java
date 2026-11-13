package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class BacteriaInputPartMachine extends ItemBusPartMachine {

    public static final PartAbility BACTERIA_INPUT = new PartAbility("bacteria_input");

    public BacteriaInputPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return BACTERIA_INPUT;
    }

    public static final MachineDefinition BACTERIAL_INPUT_MACHINE = REGISTRATE.machine("bacteria_input_machine", (holder) ->
                    new BacteriaInputPartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(BacteriaInputPartMachine.BACTERIA_INPUT)
            .tier(GTValues.ZPM)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/feeder/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.bacteriainput1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();

    public static void init() {
    }
}
