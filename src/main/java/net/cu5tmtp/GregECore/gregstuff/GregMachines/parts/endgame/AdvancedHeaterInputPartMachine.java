package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

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

public class AdvancedHeaterInputPartMachine extends FluidHatchPartMachine {

    public static final PartAbility ADVANCED_HEATER_INPUT = new PartAbility("advanced_heater_input");

    public AdvancedHeaterInputPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH, 20000, 1);
    }

    public static PartAbility getPartAbility() {
        return ADVANCED_HEATER_INPUT;
    }

    public static final MachineDefinition ADVANCED_HEATER_INPUT_MACHINE = REGISTRATE.machine("advanced_heater_input_machine", (holder) ->
                    new AdvancedHeaterInputPartMachine(holder, GTValues.UHV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(AdvancedHeaterInputPartMachine.ADVANCED_HEATER_INPUT)
            .workableCasingModel(GregECore.id("block/draconium_fusion"), GregECore.id("block/overlay/feeder"))
            .tooltips(Component.translatable("info.gregecore.advancedheater1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();

    public static void init() {
    }
}
