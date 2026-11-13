package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.parallel;

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

public class ParallelBoosterPartMachine extends TieredPartMachine {

    public static final PartAbility PARALLEL_BOOSTER = new PartAbility("parallel_booster");

    public ParallelBoosterPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier);
    }

    public static PartAbility getPartAbility() {
        return PARALLEL_BOOSTER;
    }

    public static final MachineDefinition PARALLEL_BOOSTER_MACHINE = REGISTRATE.machine("parallel_booster_machine", (holder) ->
                    new ParallelBoosterPartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(ParallelBoosterPartMachine.PARALLEL_BOOSTER)
            .tier(GTValues.ZPM)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/parallel_booster/overlay_front"))
            .tooltips(Component.translatable("info.gregecore.parallelbooster1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .editableUI(new EditableMachineUI("parallel_booster", GregECore.id("parallel_booster"), WidgetGroup::new, (group, machine) -> {
                group.addWidget(new LabelWidget(-24, 1, "Parallel multiplier: 2"));
            }))
            .register();

    public static void init() {
    }
}
