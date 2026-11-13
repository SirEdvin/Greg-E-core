package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.essentiaParts;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;

import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class AerInputPartMachine extends EssentiaInputPartMachine {

    public static final PartAbility AER_INPUT = new PartAbility("aer_input");

    public AerInputPartMachine(IMachineBlockEntity holder, int tier) {
        super(holder, tier, IO.IN, 100000, 1);
    }

    public static PartAbility getPartAbility() {
        return AER_INPUT;
    }

    @Override
    public Predicate<FluidStack> getFluidCapFilter(@Nullable Direction side, IO io) {
        return super.getFluidCapFilter(side, io);
    }

    public static final MachineDefinition AER_INPUT_MACHINE = REGISTRATE.machine(
                    "aer_input_machine",
                    holder -> new AerInputPartMachine(holder, GTValues.LV))
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(AER_INPUT)
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GregECore.id("block/overlay/aer_input"))
            .tooltips(Component.translatable("info.gregecore.essentia1").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();

    public static void init() {
    }

    @Override
    protected Fluid getAcceptedFluid() {
        return GreggyItems.AER.getFluid();
    }
}
