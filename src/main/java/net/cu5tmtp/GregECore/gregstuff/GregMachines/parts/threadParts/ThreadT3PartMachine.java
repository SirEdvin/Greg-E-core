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

public class ThreadT3PartMachine extends TieredPartMachine {

    public static final PartAbility THREADING_3 = new PartAbility("threading_tier_three");

    public ThreadT3PartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    public static PartAbility getPartAbility() {
        return THREADING_3;
    }

    public static final MachineDefinition THREADING_3_MACHINE = REGISTRATE.machine("threading_three_machine", (holder) ->
                    new ThreadT3PartMachine(holder, GTValues.UHV))
            .tier(GTValues.UHV)
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(ThreadT3PartMachine.THREADING_3)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/threading/threading_3/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.textthreading1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .editableUI(new EditableMachineUI("threading_tier_three", GregECore.id("threading_tier_three"), WidgetGroup::new, (group, machine) -> {
                group.addWidget(new LabelWidget(-40, 1, "This machine can now use"));
                group.addWidget(new LabelWidget(0, 29, "§5Threading!"));
            }))
            .register();

    public static void init() {
    }
}
