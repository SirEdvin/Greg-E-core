package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.BlockPos;

import java.util.Set;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IntentionHatchPartMachine extends TieredPartMachine {
    public enum Intention { NONE, SAFETY, BRAVERY }

    public static final PartAbility INTENTION = new PartAbility("gregecore_intention");
    public static final MachineDefinition SAFETY = register("safety", "Safety Hatch", Intention.SAFETY);
    public static final MachineDefinition BRAVERY = register("bravery", "Bravery Hatch", Intention.BRAVERY);
    private final Intention intention;

    public IntentionHatchPartMachine(IMachineBlockEntity holder, Intention intention) {
        super(holder, GTValues.LuV);
        this.intention = intention;
    }

    public Intention getIntention() { return intention; }

    @Override
    public void onLoad() {
        super.onLoad();
        // A newly placed part can attach before managed storage initializes its sync baseline.
        if (!isRemote()) getHolder().getRootStorage().markAllDirty();
    }

    @Override
    public void onControllersUpdated(Set<BlockPos> newPositions, Set<BlockPos> old) {
        super.onControllersUpdated(newPositions, old);
        // Derive the client model from the synchronized attachment, including reconnects.
        if (isRemote()) setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_FORMED, !newPositions.isEmpty()));
    }

    private static MachineDefinition register(String name, String label, Intention intention) {
        return REGISTRATE.machine(name + "_intention_hatch", holder -> new IntentionHatchPartMachine(holder, intention))
                .langValue(label)
                .tier(GTValues.LuV)
                .rotationState(RotationState.NON_Y_AXIS)
                .abilities(INTENTION)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .colorOverlayTieredHullModel(GregECore.id("block/overlay/intention/" + name))
                .tooltips(GregECore.tooltips(name + "_intention_hatch", 3))
                .register();
    }

    public static void init() {}
}
