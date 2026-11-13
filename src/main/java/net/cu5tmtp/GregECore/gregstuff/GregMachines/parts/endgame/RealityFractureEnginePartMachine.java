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

public class RealityFractureEnginePartMachine extends ItemBusPartMachine {

    public static final PartAbility REALITY_FRACTURE_PART = new PartAbility("realityfracturepart");

    public RealityFractureEnginePartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.BOTH);
    }

    public static PartAbility getPartAbility() {
        return REALITY_FRACTURE_PART;
    }

    public static final MachineDefinition REALITY_FRACTURE_PART_MACHINE = REGISTRATE.machine("realityfracturepart", (holder) ->
                    new RealityFractureEnginePartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(RealityFractureEnginePartMachine.REALITY_FRACTURE_PART)
            .workableCasingModel(GTCEu.id("block/casings/gcym/industrial_steam_casing"), GregECore.id("block/overlay/feeder"))
            .tooltips(Component.translatable("info.gregecore.realityholder1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();


    public static void init() {
    }
}
