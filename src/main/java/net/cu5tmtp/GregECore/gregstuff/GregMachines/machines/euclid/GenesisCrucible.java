package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class GenesisCrucible extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            GenesisCrucible.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private TickableSubscription logicSubscription;

    public final Map<String, Boolean> caseStates = new HashMap<>();
    @DescSynced
    public int activeCasesMask = 0;

    private record PosOffset(int forward, int above, int right) {}
    private record CaseGroup(String expectedName, PosOffset[] offsets) {}

    private final CaseGroup[] caseGroups = new CaseGroup[]{
            // Front left
            new CaseGroup("genesiscruciblecaseone", new PosOffset[]{
                    new PosOffset(6, 13, 10),
                    new PosOffset(4, 13, 8),
                    new PosOffset(4, 13, 12),
                    new PosOffset(2, 13, 10)
            }),
            // Front right
            new CaseGroup("genesiscruciblecasetwo", new PosOffset[]{
                    new PosOffset(6, 13, -10),
                    new PosOffset(4, 13, -12),
                    new PosOffset(4, 13, -8),
                    new PosOffset(2, 13, -10)
            }),
            new CaseGroup("genesiscruciblecasethree", new PosOffset[]{
                    new PosOffset(-18, 13, -10),
                    new PosOffset(-16, 13, -12),
                    new PosOffset(-16, 13, -8),
                    new PosOffset(-14, 13, -10)
            }),
            new CaseGroup("genesiscruciblecasefour", new PosOffset[]{
                    new PosOffset(-18, 13, 10),
                    new PosOffset(-16, 13, 8),
                    new PosOffset(-16, 13, 12),
                    new PosOffset(-14, 13, 10)
            })
    };

    public GenesisCrucible(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        if (this.logicSubscription == null) {
            this.logicSubscription = this.subscribeServerTick(this::manageLogic);
        }
    }

    @Override
    public void onStructureInvalid() {
        this.caseStates.clear();
        this.activeCasesMask = 0;

        if (this.logicSubscription != null) {
            this.logicSubscription.unsubscribe();
            this.logicSubscription = null;
        }
        super.onStructureInvalid();
    }

    private void manageLogic() {
        if (isFormed && getOffsetTimer() % 40 == 0) {
            checkLinkedCases();
        }
    }

    private void checkLinkedCases() {
        if (getLevel() == null) return;

        BlockPos center = getPos();
        Direction forward = getFrontFacing();
        Direction right = forward.getClockWise();

        int newMask = 0;

        for (int i = 0; i < caseGroups.length; i++) {
            CaseGroup group = caseGroups[i];
            boolean anyPartFormed = false;

            for (PosOffset offset : group.offsets()) {
                BlockPos targetPos = center.relative(forward, offset.forward())
                        .above(offset.above())
                        .relative(right, offset.right());

                boolean partFormed = false;

                if (getLevel().getBlockEntity(targetPos) instanceof IMachineBlockEntity mbe) {
                    if (mbe.getMetaMachine() instanceof GenesisCrucibleCases crucibleCase) {
                        String actualName = crucibleCase.getDefinition().getName();
                        if (actualName.equals(group.expectedName())) {
                            partFormed = crucibleCase.isFormed();
                        }
                    }
                }

                if (partFormed) {
                    anyPartFormed = true;
                    break;
                }
            }

            this.caseStates.put(group.expectedName(), anyPartFormed);

            if (anyPartFormed) {
                newMask |= (1 << i);
            }
        }

        this.activeCasesMask = newMask;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        assert recipe != null;
        String requiredCartridges = recipe.data.getString("insertedc");

        if (!requiredCartridges.isEmpty()) {
            String[] parts = requiredCartridges.split(",");
            for (String part : parts) {
                String expectedCase = part.trim();
                if (!expectedCase.isEmpty()) {
                    boolean isCaseFormed = this.caseStates.getOrDefault(expectedCase, false);
                    if (!isCaseFormed) {
                        return false;
                    }
                }
            }
        }

        return super.beforeWorking(recipe);
    }

    public static MachineDefinition GENESISCRUCIBLE = REGISTRATE
            .multiblock("genesiscrucible", GenesisCrucible::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.GCCRAFTING)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbcccccbbbbbbbbbbbbbbbcccccbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbcccccbbbbbbbbbbbbbbbcccccbb")
                        .aisle("bcdddddcbbbbbbbbbbbbbcdddddcb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bcdddddcbbbbbbbbbbbbbcdddddcb")
                        .aisle("cddeeeddcbbbbbbbbbbbcddeeeddc", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfebefbb", "bbfegefbbbbbbbbbbbbbbbfebefbb", "bbfegefbbbbbbbbbbbbbbbfebefbb", "bbfegefbbbbbbbbbbbbbbbfebefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "cdfeeefdcbbbbbbbbbbbcdfeeefdc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbgibigbbbbbbbbbbbbbbbbibigbb", "bbgibigbbbbbbbbbbbbbbbbibigbb", "bbgibigbbbbbbbbbbbbbbbbibigbb", "bbgibigbbbbbbbbbbbbbbbbibigbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbfaiafbbbbbbbbbbbbbbbfaqafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfaiafbbbbbbbbbbbbbbbfaqafbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cddeeeddcbbbbbbbbbbbcddeeeddc", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "cdfeeefdcbbbbbbbbbbbcdfeeefdc")
                        .aisle("bcdddddmccbbbbbbbbbccmdddddcb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bcdddddmccbbbbbbbbbccmdddddcb")
                        .aisle("bbccccccmcccccccccccmccccccbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbccccccmcccccccccccmccccccbb")
                        .aisle("bbbbbbbccmccdddddccmccbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbccmccdddddccmccbbbbbbb")
                        .aisle("bbbbbbbbccmdnnnnndmccbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbccmdnnnnndmccbbbbbbbb")
                        .aisle("bbbbbbbbccdnnnnnnndccbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbonnnnnobbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbonnnnnobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbccdnnnnnnndccbbbbbbbb")
                        .aisle("bbbbbbbbcdnnnnnnnnndcbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbonnbbbnnobbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbboonnnoobbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbbooobbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbooobbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbboonnnoobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbcdnnnnnnnnndcbbbbbbbb")
                        .aisle("bbbbbbbbcdnnnnnnnnndcbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbcdnnnnnnnnndcbbbbbbbb")
                        .aisle("bbbbbbbbcdnnnnnnnnndcbbbbbbbb", "bbbbbbbbbbmbbbbbbbmbbbbbbbbbb", "bbbbbbbbbbmbbbbbbbmbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbbmnmbbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbbmbbbmbbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbbmbbbbbmbbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbmbbbbbbbmbbbbbbbbbb", "bbbbbbbbbbmbbbbbbbmbbbbbbbbbb", "bbbbbbbbcdnnnnnnnnndcbbbbbbbb")
                        .aisle("bbbbbbbbcdnnnnnnnnndcbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbmbbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbbnmnbbbbbbbbbbbbb", "bbbbbbbbbbbbonnnobbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbbnbbbnbbbbbbbbbbbb", "bbbbbbbbbbbonbbbnobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbcdnnnnnnnnndcbbbbbbbb")
                        .aisle("bbbbbbbbcdnnnnnnnnndcbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbonnbbbnnobbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbbnnbbbnnbbbbbbbbbbb", "bbbbbbbbbbboonnnoobbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbbooobbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbooobbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbbbpnmnpbbbbbbbbbbbb", "bbbbbbbbbbboonnnoobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbonbbbbbnobbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbbbnbbbbbbbnbbbbbbbbbb", "bbbbbbbbcdnnnnnnnnndcbbbbbbbb")
                        .aisle("bbbbbbbbccdnnnnnnndccbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbonnnnnobbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbpnnmnnpbbbbbbbbbbb", "bbbbbbbbbbbonnnnnobbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbbbbnbbbbbnbbbbbbbbbbb", "bbbbbbbbccdnnnnnnndccbbbbbbbb")
                        .aisle("bbbbbbbbccmdnnnnndmccbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbooooobbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbbbbbnnmnnbbbbbbbbbbbb", "bbbbbbbbccmdnnnnndmccbbbbbbbb")
                        .aisle("bbbbbbbccmccdddddccmccbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbccmccdddddccmccbbbbbbb")
                        .aisle("bbccccccmccccczcccccmccccccbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbccccccmcccccccccccmccccccbb")
                        .aisle("bcdddddmccbbbbbbbbbccmdddddcb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bcdddddmccbbbbbbbbbccmdddddcb")
                        .aisle("cddeeeddcbbbbbbbbbbbcddeeeddc", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "cdfeeefdcbbbbbbbbbbbcdfeeefdc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbfakafbbbbbbbbbbbbbbbfalafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfakafbbbbbbbbbbbbbbbfalafbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbbjbjbbbbbbbbbbbbbbbbbjbjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbgibigbbbbbbbbbbbbbbbgibigbb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cdeeeeedcbbbbbbbbbbbcdeeeeedc", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbabbbabbbbbbbbbbbbbbbabbbabb", "bbfaaafbbbbbbbbbbbbbbbfaaafbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbbjjjbbbbbbbbbbbbbbbbbjjjbbb", "bbfbbbfbbbbbbbbbbbbbbbfbbbfbb", "bbebbbebbbbbbbbbbbbbbbebbbebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbebibebbbbbbbbbbbbbbbebibebb", "bbeeeeebbbbbbbbbbbbbbbeeeeebb", "cdeeeeedcbbbbbbbbbbbcdeeeeedc")
                        .aisle("cddeeeddcbbbbbbbbbbbcddeeeddc", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbaaaaabbbbbbbbbbbbbbbaaaaabb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhbbbhbbbbbbbbbbbbbbbhbbbhbb", "bbhfffhbbbbbbbbbbbbbbbhfffhbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfegefbbbbbbbbbbbbbbbfegefbb", "bbfeeefbbbbbbbbbbbbbbbfeeefbb", "cdfeeefdcbbbbbbbbbbbcdfeeefdc")
                        .aisle("bcdddddcbbbbbbbbbbbbbcdddddcb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "babbbbbabbbbbbbbbbbbbabbbbbab", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bcdddddcbbbbbbbbbbbbbcdddddcb")
                        .aisle("bbcccccbbbbbbbbbbbbbbbcccccbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "abbbbbbbabbbbbbbbbbbabbbbbbba", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbbbbbbbbbbbbbbbb", "bbcccccbbbbbbbbbbbbbbbcccccbb")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:atomic_casing"))))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_gray")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2)))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:high_temperature_smelting_casing"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:large_scale_assembler_casing"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:sturdy_machine_casing"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:nebulon_alpha_frame"))))
                        .where("i", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_purple"))))
                        .where("j", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:awakened_draconium_coil"))))
                        .where("k", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_red"))))
                        .where("l", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_orange"))))
                        .where("m", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:superconducting_coil"))))
                        .where("n", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                        .where("o", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:industrial_steam_casing"))))
                        .where("p", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:neutronium_frame"))))
                        .where("q", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_lime"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GregECore.id("block/machine_casing_tiled_gray"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createGenesisCrucibleRender))
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible4").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible5").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible6").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible7").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.genesiscrucible8").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.genesiscrucible9").withStyle(ChatFormatting.LIGHT_PURPLE));

            boolean case1 = caseStates.getOrDefault("genesiscruciblecaseone", false);
            textList.add(Component.translatable("info.gregecore.genesiscrucible10").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(case1 ? "info.gregecore.genesiscrucible14" : "info.gregecore.genesiscrucible15").withStyle(case1 ? ChatFormatting.GREEN : ChatFormatting.RED)));

            boolean case2 = caseStates.getOrDefault("genesiscruciblecasetwo", false);
            textList.add(Component.translatable("info.gregecore.genesiscrucible11").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(case2 ? "info.gregecore.genesiscrucible14" : "info.gregecore.genesiscrucible15").withStyle(case2 ? ChatFormatting.GREEN : ChatFormatting.RED)));

            boolean case3 = caseStates.getOrDefault("genesiscruciblecasethree", false);
            textList.add(Component.translatable("info.gregecore.genesiscrucible12").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(case3 ? "info.gregecore.genesiscrucible14" : "info.gregecore.genesiscrucible15").withStyle(case3 ? ChatFormatting.GREEN : ChatFormatting.RED)));

            boolean case4 = caseStates.getOrDefault("genesiscruciblecasefour", false);
            textList.add(Component.translatable("info.gregecore.genesiscrucible13").withStyle(ChatFormatting.GRAY)
                    .append(Component.translatable(case4 ? "info.gregecore.genesiscrucible14" : "info.gregecore.genesiscrucible15").withStyle(case4 ? ChatFormatting.GREEN : ChatFormatting.RED)));
        }
    }

    public static void init() {
    }
}