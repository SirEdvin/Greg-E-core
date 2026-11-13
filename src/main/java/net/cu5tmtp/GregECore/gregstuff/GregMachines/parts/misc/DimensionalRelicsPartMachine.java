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

public class DimensionalRelicsPartMachine extends ItemBusPartMachine {

    public DimensionalRelicsPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return PartAbility.PASSTHROUGH_HATCH;
    }

    public static final MachineDefinition DIMENSIONAL_RELICS_MACHINE = REGISTRATE.machine("dimensional_relics_machine", (holder) ->
                    new DimensionalRelicsPartMachine(holder, GTValues.HV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(PartAbility.PASSTHROUGH_HATCH)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/feeder/overlay_front"))
            .tier(GTValues.HV)
            .tooltips(Component.translatable("info.gregecore.dimensionalrecils1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.spectrometer4").withStyle(ChatFormatting.AQUA))
            .tooltips(Component.translatable("info.gregecore.spectrometer6").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer7").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer8").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer9").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer10").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer11").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer12").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer13").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer14").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer15").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer16").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer17").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer18").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer19").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer20").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer21").withStyle(ChatFormatting.BLUE)))
            .tooltips(Component.translatable("info.gregecore.spectrometer22").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(Component.translatable("info.gregecore.spectrometer5").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.spectrometer23").withStyle(ChatFormatting.BLUE)))
            .register();

    public static void init() {
    }
}
