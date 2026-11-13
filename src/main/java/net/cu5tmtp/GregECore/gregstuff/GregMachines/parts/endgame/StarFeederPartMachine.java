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

public class StarFeederPartMachine extends ItemBusPartMachine {

    public static final PartAbility STAR_FEEDER = new PartAbility("star_feeder");

    public StarFeederPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return STAR_FEEDER;
    }

    public static final MachineDefinition STAR_FEEDER_MACHINE = REGISTRATE.machine("star_feeder", (holder) ->
                    new StarFeederPartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(StarFeederPartMachine.STAR_FEEDER)
            .tier(GTValues.UHV)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/feeder/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.feeder1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();


    public static void init() {
    }
}
