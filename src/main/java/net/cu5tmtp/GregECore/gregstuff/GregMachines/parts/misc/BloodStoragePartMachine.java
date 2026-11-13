package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc;

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

public class BloodStoragePartMachine extends ItemBusPartMachine {

    public static final PartAbility BLOODSTORAGE_INPUT = new PartAbility("bloodstorage_input");
    public BloodStoragePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return BLOODSTORAGE_INPUT;
    }

    public static final MachineDefinition BLOOD_STORAGE_MACHINE = REGISTRATE.machine("blood_storage_machine", (holder) ->
                    new BloodStoragePartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(BloodStoragePartMachine.BLOODSTORAGE_INPUT)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/feeder/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.bloodstorage1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tier(GTValues.ZPM)
            .register();

    public static void init() {
    }
}
