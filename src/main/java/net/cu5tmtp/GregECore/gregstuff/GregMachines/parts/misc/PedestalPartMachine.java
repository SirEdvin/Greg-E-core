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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class PedestalPartMachine extends ItemBusPartMachine {

    public static final PartAbility PEDESTAL_INF = new PartAbility("pedestal_inf");

    public PedestalPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN);
    }

    public static PartAbility getPartAbility() {
        return PEDESTAL_INF;
    }

    public static final MachineDefinition PEDESTAL_INF_MACHINE = REGISTRATE.machine("pedestal_inf_machine", (holder) ->
                    new PedestalPartMachine(holder, GTValues.ULV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(PedestalPartMachine.PEDESTAL_INF)
            .blockProp(BlockBehaviour.Properties::noOcclusion)
            .model((ctx, prov, builder) -> {
                ModelFile customModel = prov.models().getExistingFile(GregECore.id("block/pedestal"));

                builder.addModels(
                        builder.partialState(),
                        new ConfiguredModel(customModel)
                );
            })
            .tooltips(Component.translatable("info.gregecore.pedestal1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();


    public static void init() {
    }
}
