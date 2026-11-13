package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.editor.EditableMachineUI;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class ThreadT1OverclockPartMachine extends TieredPartMachine {

    public static final PartAbility THREADING_1_O = new PartAbility("threading_tier_one_overlcock");

    public ThreadT1OverclockPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    public static PartAbility getPartAbility() {
        return THREADING_1_O;
    }

    public static final MachineDefinition THREADING_1_O_MACHINE = REGISTRATE.machine("threading_one_overlcock_machine", (holder) ->
                    new ThreadT1OverclockPartMachine(holder, GTValues.HV))
            .tier(GTValues.HV)
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(ThreadT1OverclockPartMachine.THREADING_1_O)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/threading/threading_1_o/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.textthreading1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .editableUI(new EditableMachineUI("threading_tier_one", GregECore.id("threading_tier_one"), WidgetGroup::new, (group, machine) -> {
                group.addWidget(new LabelWidget(-40, 1, "This machine can now use"));
                group.addWidget(new LabelWidget(0, 29, "§5Threading!"));
            }))
            .register();

    public static void init() {
    }
}
