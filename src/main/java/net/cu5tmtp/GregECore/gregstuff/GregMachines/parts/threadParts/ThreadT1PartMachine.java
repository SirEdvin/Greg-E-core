package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts;

import com.gregtechceu.gtceu.GTCEu;
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

public class ThreadT1PartMachine extends TieredPartMachine {

    public static final PartAbility THREADING_1 = new PartAbility("threading_tier_one");

    public ThreadT1PartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    public static PartAbility getPartAbility() {
        return THREADING_1;
    }

    public static final MachineDefinition THREADING_1_MACHINE = REGISTRATE.machine("threading_one_machine", (holder) ->
                    new ThreadT1PartMachine(holder, GTValues.HV))
            .tier(GTValues.HV)
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(ThreadT1PartMachine.THREADING_1)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/threading/threading_1/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.textthreading1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .editableUI(new EditableMachineUI("threading_tier_one", GregECore.id("threading_tier_one"), WidgetGroup::new, (group, machine) -> {
                group.addWidget(new LabelWidget(-40, 1, "This machine can now use"));
                group.addWidget(new LabelWidget(0, 29, "§5Threading!"));
            }))
            .register();

    public static void init() {
    }
}
