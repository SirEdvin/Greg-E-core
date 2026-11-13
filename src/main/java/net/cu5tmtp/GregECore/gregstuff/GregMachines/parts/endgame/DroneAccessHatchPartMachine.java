package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

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

public class DroneAccessHatchPartMachine extends ItemBusPartMachine {

    public static final PartAbility DRONE_ACCESS = new PartAbility("drone_access");

    public DroneAccessHatchPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return DRONE_ACCESS;
    }

    public static final MachineDefinition DRONE_ACCESS_MACHINE = REGISTRATE.machine("drone_access_machine", (holder) ->
                    new DroneAccessHatchPartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(DroneAccessHatchPartMachine.DRONE_ACCESS)
            .tier(GTValues.UHV)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/feeder/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.droneaccess1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();


    public static void init() {
    }
}
