package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.part.TieredPartMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;

import java.util.Set;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IntentionHatchPartMachine extends TieredPartMachine {
    public enum Intention { NONE, SAFETY, BRAVERY }

    public static final PartAbility INTENTION = new PartAbility("gregecore_intention");
    public static final MachineDefinition SAFETY = register("safety", "Safety Hatch", Intention.SAFETY,
            "Compressed factory that can potentially work as a lightning rod", "Use this intention hatch if you like safety");
    public static final MachineDefinition BRAVERY = register("bravery", "Bravery Hatch", Intention.BRAVERY,
            "Fate favors those who are brave", "Use this intention hatch if you are brave enough");
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

    private static MachineDefinition register(String name, String label, Intention intention, String flavor, String usage) {
        String key = "gregecore.tooltip." + name + "_intention_hatch.";
        return REGISTRATE.machine(name + "_intention_hatch", holder -> new IntentionHatchPartMachine(holder, intention))
                .langValue(label)
                .tier(GTValues.LuV)
                .rotationState(RotationState.NON_Y_AXIS)
                .abilities(INTENTION)
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .colorOverlayTieredHullModel(GregECore.id("block/overlay/intention/" + name))
                .tooltips(REGISTRATE.addRawLang(key + 0, flavor).withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC),
                        REGISTRATE.addRawLang(key + 1, usage))
                .register();
    }

    public static void init() {}
}
