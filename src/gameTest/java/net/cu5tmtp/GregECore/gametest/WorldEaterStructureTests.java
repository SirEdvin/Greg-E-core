package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine.Intention;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterStructureTests {
    static final BlockPos CONTROLLER = new BlockPos(3, 3, 1);

    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing) {
        return shell(helper, facing, CONTROLLER);
    }

    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        Direction right = facing.getClockWise();
        for (int back = 0; back < 5; back++) {
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos = center.relative(facing.getOpposite(), back).relative(right, x).above(y);
                    int boundaries = (back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0);
                    boolean edge = boundaries >= 2;
                    helper.getLevel().setBlockAndUpdate(pos, edge ? GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState() : Blocks.STONE.defaultBlockState());
                }
            }
        }
        helper.getLevel().setBlockAndUpdate(center, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
        var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), center);
        controller.setFrontFacing(facing);
        controller.getRecipeLogic().setWorkingEnabled(false);
        return controller;
    }

    static List<BlockPos> shellPositions(IncompleteWorldEaterTamer controller) {
        List<BlockPos> positions = new ArrayList<>();
        Direction facing = controller.getFrontFacing(), right = facing.getClockWise();
        for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) {
            if (back == 0 && x == 0 && y == 0) continue;
            if ((back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0) >= 2)
                positions.add(controller.getPos().relative(facing.getOpposite(), back).relative(right, x).above(y));
        }
        return positions;
    }

    static ItemBusPartMachine bus(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.getLevel().setBlockAndUpdate(pos, definition.defaultBlockState());
        return (ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
    }

    static void colorFrame(IncompleteWorldEaterTamer machine, int color) {
        int index = 0;
        for (var pos : shellPositions(machine)) {
            var state = machine.getLevel().getBlockState(pos);
            for (var casing : WorldEaterCasingTests.casings()) if (state.is(casing)) {
                machine.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[color == -1 ? 1 + index++ % 3 : color].defaultBlockState());
                break;
            }
        }
    }

    static void assertTier(GameTestHelper helper, IncompleteWorldEaterTamer machine, int tier) {
        helper.assertTrue(machine.checkPatternWithLock(), "Tier fixture matches: facing=" + machine.getFrontFacing()
                + " error=" + (machine.getMultiblockState().error == null ? "none" : machine.getMultiblockState().error.getErrorInfo().getString())
                + " at=" + machine.getMultiblockState().getPos());
        machine.onStructureFormed();
        helper.assertTrue(machine.getCasingTier() == tier, "Expected casing tier " + tier + ", got " + machine.getCasingTier());
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void structureLimits(GameTestHelper helper) {
        var machine = shell(helper, Direction.NORTH);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPattern(), "Edge-only frame forms with arbitrary face panels and interior");
            var positions = shellPositions(machine);
            helper.assertTrue(positions.size() == 43, "Exactly 44 frame blocks including the edge-centered controller");
            for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) {
                var pos = machine.getPos().south(back).east(x).above(y);
                if (!pos.equals(machine.getPos()) && !positions.contains(pos))
                    helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            helper.assertTrue(machine.checkPattern(), "Open faces and empty interior form");
            for (var pos : positions) {
                helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Every frame edge and corner is required: " + pos);
                helper.getLevel().setBlockAndUpdate(pos, GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            }
            MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
            for (int type = 0; type < types.length; type++) for (int count = 0; count < 5; count++)
                helper.getLevel().setBlockAndUpdate(positions.get(type * 5 + count), types[type].defaultBlockState());
            helper.assertTrue(machine.checkPattern(), "Five of EACH part type form");
            for (var type : types) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), type.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Reject sixth " + type.getId());
                helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].defaultBlockState());
            helper.assertTrue(!machine.checkPattern(), "Reject energy hatch");
            for (var rejected : new net.minecraft.world.level.block.Block[]{GTBlocks.MACHINE_CASING_UHV.get(), Blocks.STONE}) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), rejected.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Reject unsupported frame casing: " + rejected);
            }
            for (var accepted : WorldEaterCasingTests.casings()) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), accepted.defaultBlockState());
                helper.assertTrue(machine.checkPattern(), "Accept mixed colored frame casing: " + accepted);
            }
            for (var accepted : WorldEaterCasingTests.casings()) {
                for (var pos : positions) helper.getLevel().setBlockAndUpdate(pos, accepted.defaultBlockState());
                helper.assertTrue(machine.checkPattern(), "Accept uniform frame: " + accepted);
            }
            for (int i = 0; i < positions.size(); i++)
                helper.getLevel().setBlockAndUpdate(positions.get(i), WorldEaterCasingTests.casings()[i % 4].defaultBlockState());
            helper.assertTrue(machine.checkPattern(), "Accept all four casing variants together");
            for (int type = 0; type < types.length; type++) for (int count = 0; count < 5; count++)
                helper.getLevel().setBlockAndUpdate(positions.get(type * 5 + count), types[type].defaultBlockState());
            helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            helper.assertTrue(machine.checkPattern(), "Restored shell forms");
            machine.onStructureFormed();
            var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
            helper.assertTrue(!logic.isCountableInterface(machine), "Exclude self");
            for (var part : machine.getParts()) helper.assertTrue(!logic.isCountableInterface(part.self()), "Exclude attached part");
            machine.onStructureInvalid();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void casingTiersAndExclusions(GameTestHelper helper) {
        var machines = new ArrayList<IncompleteWorldEaterTamer>();
        int location = 130000;
        for (var facing : Direction.Plane.HORIZONTAL) {
            var center = helper.absolutePos(new BlockPos(location, 3, location));
            for (int x = (center.getX() >> 4) - 1; x <= (center.getX() >> 4) + 1; x++)
                for (int z = (center.getZ() >> 4) - 1; z <= (center.getZ() >> 4) + 1; z++)
                    helper.getLevel().setChunkForced(x, z, true);
            machines.add(shell(helper, facing, new BlockPos(location, 3, location)));
            location += 100;
        }
        helper.runAfterDelay(6, () -> {
            for (var machine : machines) {
                System.out.println("WORLD_EATER_TIER_CHECK facing=" + machine.getFrontFacing() + " stage=uniform-and-traversal");
                assertTier(helper, machine, 1);
                for (int color = 1; color <= 3; color++) {
                    colorFrame(machine, color);
                    assertTier(helper, machine, 2);
                }
                colorFrame(machine, -1);
                var positions = shellPositions(machine);
                for (var pos : positions) {
                    helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[0].defaultBlockState());
                    assertTier(helper, machine, 1);
                    helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[1].defaultBlockState());
                    assertTier(helper, machine, 2);
                }
                helper.getLevel().setBlockAndUpdate(machine.getPos().above(), WorldEaterCasingTests.casings()[0].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(machine.getPos().relative(machine.getFrontFacing().getOpposite(), 2).above(2), WorldEaterCasingTests.casings()[0].defaultBlockState());
                MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                        GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
                for (int type = 0; type < types.length; type++) for (int count = 0; count < 5; count++)
                    helper.getLevel().setBlockAndUpdate(positions.get(type * 5 + count), types[type].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(20), com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV].defaultBlockState());
                System.out.println("WORLD_EATER_TIER_CHECK facing=" + machine.getFrontFacing() + " stage=parts");
                assertTier(helper, machine, 2);
                helper.assertTrue(machine.getParts().size() == 21, "All permitted parts attached; black appearances do not count");
                for (var type : types) {
                    helper.getLevel().setBlockAndUpdate(positions.get(21), type.defaultBlockState());
                    helper.assertTrue(!machine.checkPattern() && machine.getCasingTier() == 0, "Colored frame rejects sixth " + type.getId());
                }
                helper.getLevel().setBlockAndUpdate(positions.get(21), com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV].defaultBlockState());
                helper.assertTrue(!machine.checkPattern() && machine.getCasingTier() == 0, "Colored frame rejects second parallel hatch");
                helper.getLevel().setBlockAndUpdate(positions.get(21), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].defaultBlockState());
                helper.assertTrue(!machine.checkPattern() && machine.getCasingTier() == 0, "Colored frame rejects energy");
                helper.getLevel().setBlockAndUpdate(positions.get(21), WorldEaterCasingTests.casings()[0].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(42), Blocks.AIR.defaultBlockState());
                helper.assertTrue(!machine.checkPattern() && machine.getCasingTier() == 0, "Failed partial black match unavailable");
                helper.getLevel().setBlockAndUpdate(positions.get(21), WorldEaterCasingTests.casings()[2].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(42), WorldEaterCasingTests.casings()[3].defaultBlockState());
                System.out.println("WORLD_EATER_TIER_CHECK facing=" + machine.getFrontFacing() + " stage=recovery");
                assertTier(helper, machine, 2);
                assertTier(helper, machine, 2);
                machine.getMultiblockState().getMatchContext().reset();
                helper.assertTrue(machine.getCasingTier() == 0, "Missing match data never defaults to tier 2");
                assertTier(helper, machine, 2);
                machine.onStructureInvalid();
                helper.assertTrue(machine.getCasingTier() == 0, "Invalidation clears tier");
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void nativeCasingTierLifecycle(GameTestHelper helper) {
        var active = WorldEaterRunTests.fixture(helper, 140000);
        var dormant = WorldEaterRunTests.fixture(helper, 141000);
        WorldEaterCasingTests.active(active.logic());
        lifecycle(helper, new WorldEaterRunTests.Fixture[]{active, dormant}, 0, 0);
    }

    @GameTest(template = "empty", timeoutTicks = 1800)
    public static void dominantCasingAppearance(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 280000);
        helper.getLevel().setBlockAndUpdate(intentionPosition(f.controller()), IntentionHatchPartMachine.SAFETY.defaultBlockState());
        var frame = shellPositions(f.controller());
        frame.remove(f.input().getPos());
        frame.remove(f.output().getPos());
        frame.remove(intentionPosition(f.controller()));
        helper.getLevel().setBlockAndUpdate(frame.remove(0), GTMachines.FLUID_IMPORT_HATCH[GTValues.LV].defaultBlockState());
        helper.assertTrue(frame.size() == 39, "39 actual casing voters; controller and four parts excluded");
        // Extra colored face/interior blocks must not affect the frame vote.
        helper.getLevel().setBlockAndUpdate(f.controller().getPos().above(), WorldEaterCasingTests.casings()[3].defaultBlockState());
        helper.getLevel().setBlockAndUpdate(BlockPos.containing(f.controller().getStructureCenter()), WorldEaterCasingTests.casings()[3].defaultBlockState());
        appearanceLifecycle(helper, f, frame, 0, -1);
    }

    private static void appearanceLifecycle(GameTestHelper helper, WorldEaterRunTests.Fixture f, List<BlockPos> frame, int stage, int waited) {
        int[][] votes = {{39, 0, 0}, {0, 39, 0}, {0, 0, 39}, {13, 13, 13}, {7, 16, 16}, {15, 15, 9}, {0, 0, 39}, {0, 0, 39}, {0, 0, 39}};
        int[] winners = {1, 2, 3, 1, 2, 1, 0, -1, 3};
        var machine = f.controller();
        if (waited == -1) {
            for (int i = 0; i < frame.size(); i++) {
                int color = i < votes[stage][0] ? 1 : i < votes[stage][0] + votes[stage][1] ? 2 : 3;
                helper.getLevel().setBlockAndUpdate(frame.get(i), WorldEaterCasingTests.casings()[color].defaultBlockState());
            }
            if (stage == 6) helper.getLevel().setBlockAndUpdate(frame.get(0), WorldEaterCasingTests.casings()[0].defaultBlockState());
            if (stage == 7) helper.getLevel().setBlockAndUpdate(frame.get(0), Blocks.AIR.defaultBlockState());
            helper.runAfterDelay(2, () -> appearanceLifecycle(helper, f, frame, stage, 0));
            return;
        }
        int winner = winners[stage];
        var appearance = machine.getPartAppearance(f.input(), Direction.NORTH, null, null);
        var expected = winner < 0 ? null : WorldEaterCasingTests.casings()[winner].defaultBlockState();
        if (machine.isFormed() != (winner >= 0) || appearance != expected) {
            helper.assertTrue(waited < 160, "Native appearance converges at stage " + stage + ": " + appearance);
            awaitPatternWorker();
            helper.runAfterDelay(1, () -> appearanceLifecycle(helper, f, frame, stage, waited + 1));
            return;
        }
        for (var part : machine.getParts()) for (var side : Direction.values())
            helper.assertTrue(part.getFormedAppearance(null, null, side) == expected, "Every formed part uses dominant frame color");
        if (stage == 8) {
            var holder = (net.minecraft.world.level.block.entity.BlockEntity) machine.getHolder();
            var saved = holder.saveWithFullMetadata();
            helper.getLevel().removeBlock(machine.getPos(), false);
            helper.getLevel().setBlockAndUpdate(machine.getPos(), machine.getDefinition().defaultBlockState());
            var fresh = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), machine.getPos());
            ((net.minecraft.world.level.block.entity.BlockEntity) fresh.getHolder()).load(saved.copy());
            helper.assertTrue(fresh.getDefinition().getPartAppearance().apply(fresh, f.input(), Direction.NORTH)
                    == WorldEaterCasingTests.casings()[0].defaultBlockState(), "Appearance is derived again, not persisted");
            assertTier(helper, fresh, 2);
            helper.assertTrue(fresh.getPartAppearance(f.input(), Direction.NORTH, null, null) == expected, "Reload recomputes orange winner");
            helper.succeed();
            return;
        }
        appearanceLifecycle(helper, f, frame, stage + 1, -1);
    }

    private static void lifecycle(GameTestHelper helper, WorldEaterRunTests.Fixture[] fixtures, int stage, int waited) {
        int expected = switch (stage) { case 0, 1, 4, 6 -> 1; case 5 -> 0; default -> 2; };
        boolean ready = true;
        for (int i = 0; i < fixtures.length; i++) {
            var f = fixtures[i];
            helper.assertTrue(f.logic().getProgressionState().name().equals(i == 0 ? "ACTIVE" : "DORMANT"), "Progression independent at lifecycle stage " + stage);
            ready &= f.controller().getCasingTier() == expected && f.controller().isFormed() == (expected != 0);
        }
        if (!ready) {
            helper.assertTrue(waited < 160, "Native callbacks reach tier " + expected + " at stage " + stage);
            awaitPatternWorker();
            helper.runAfterDelay(1, () -> lifecycle(helper, fixtures, stage, waited + 1));
            return;
        }
        for (var f : fixtures) {
            var text = new ArrayList<Component>();
            f.controller().addDisplayText(text);
            helper.assertTrue(text.stream().anyMatch(line -> line.getString().equals("Casing tier: 2")) == (expected == 2),
                    "Tier UI shown only at tier 2, lifecycle stage " + stage);
        }
        if (stage == 7) { helper.succeed(); return; }
        for (var f : fixtures) {
            var machine = f.controller();
            var pos = shellPositions(machine).get(3);
            switch (stage) {
                case 0 -> helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[1].defaultBlockState());
                case 1 -> colorFrame(machine, -1);
                case 2 -> helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[3].defaultBlockState());
                case 3 -> helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[0].defaultBlockState());
                case 4 -> helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                case 5 -> helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[0].defaultBlockState());
                default -> colorFrame(machine, -1);
            }
        }
        helper.runAfterDelay(2, () -> lifecycle(helper, fixtures, stage + 1, 0));
    }

    static BlockPos intentionPosition(IncompleteWorldEaterTamer machine) {
        return machine.getPos().relative(machine.getFrontFacing().getOpposite(), 4);
    }

    private static void awaitPatternWorker() {
        // GameTest ticks are unpaced, but native pattern checks run on a wall-clock worker.
        try { Thread.sleep(50); }
        catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted awaiting native pattern checks", interrupted);
        }
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void intentionPlacementAndSharedLimits(GameTestHelper helper) {
        var machines = new ArrayList<IncompleteWorldEaterTamer>();
        int location = 230000;
        for (var facing : Direction.Plane.HORIZONTAL) {
            var center = helper.absolutePos(new BlockPos(location, 3, location));
            for (int x = (center.getX() >> 4) - 1; x <= (center.getX() >> 4) + 1; x++)
                for (int z = (center.getZ() >> 4) - 1; z <= (center.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
            machines.add(shell(helper, facing, new BlockPos(location, 3, location)));
            location += 100;
        }
        helper.runAfterDelay(6, () -> {
            for (var machine : machines) {
                var rear = intentionPosition(machine);
                var positions = shellPositions(machine); positions.remove(rear);
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getIntention() == Intention.NONE, "No implicit intention");
                colorFrame(machine, -1);
                for (var variant : new MachineDefinition[]{IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY}) {
                    helper.getLevel().setBlockAndUpdate(rear, variant.defaultBlockState());
                    assertTier(helper, machine, 2);
                    var hatch = (IntentionHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), rear);
                    helper.assertTrue(machine.getIntention() == hatch.getIntention() && machine.getParts().contains(hatch), "Correct attached variant: " + machine.getFrontFacing());
                    helper.assertTrue(hatch.replacePartModelWhenFormed(), "Formed hatch inherits controller casing");
                    machine.onStructureInvalid();
                    helper.assertTrue(!hatch.replacePartModelWhenFormed(), "Detached hatch restores standalone hull");
                    assertTier(helper, machine, 2);
                    for (var wrong : positions) {
                        helper.getLevel().setBlockAndUpdate(wrong, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                        helper.assertTrue(!machine.checkPattern() && machine.getIntention() == Intention.NONE, "Second/misplaced frame hatch rejected: " + wrong);
                        helper.getLevel().setBlockAndUpdate(wrong, WorldEaterCasingTests.casings()[1].defaultBlockState());
                    }
                    assertTier(helper, machine, 2);
                }
                helper.getLevel().setBlockAndUpdate(rear, WorldEaterCasingTests.casings()[1].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(0), IntentionHatchPartMachine.BRAVERY.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Lone hatch at wrong frame coordinate rejected");
                helper.getLevel().setBlockAndUpdate(positions.get(0), WorldEaterCasingTests.casings()[1].defaultBlockState());
                for (var pos : new BlockPos[]{machine.getPos().above(), machine.getPos().relative(machine.getFrontFacing().getOpposite(), 2).above(2)})
                    helper.getLevel().setBlockAndUpdate(pos, IntentionHatchPartMachine.BRAVERY.defaultBlockState());
                assertTier(helper, machine, 2);
                helper.assertTrue(machine.getIntention() == Intention.NONE && machine.getParts().isEmpty(), "Unrestricted hatches do not attach");
                MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                        GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
                var parallel = com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV];
                for (int t = 0; t < types.length; t++) for (int count = 0; count < 5; count++)
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5 + count), types[t].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(20), parallel.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(rear, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                assertTier(helper, machine, 2);
                helper.assertTrue(machine.getParts().size() == 22 && machine.getIntention() == Intention.SAFETY, "Intention has its own allowance");
                for (int t = 0; t < types.length; t++) {
                    helper.getLevel().setBlockAndUpdate(rear, types[t].defaultBlockState());
                    helper.assertTrue(!machine.checkPattern(), "Rear slot shares the five-part limit");
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5), WorldEaterCasingTests.casings()[1].defaultBlockState());
                    assertTier(helper, machine, 2);
                    helper.assertTrue(machine.getIntention() == Intention.NONE, "Rear ordinary part is not intention");
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5), types[t].defaultBlockState());
                }
                helper.getLevel().setBlockAndUpdate(rear, parallel.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Rear slot shares the parallel limit");
                helper.getLevel().setBlockAndUpdate(positions.get(20), WorldEaterCasingTests.casings()[1].defaultBlockState());
                assertTier(helper, machine, 2);
                helper.getLevel().setBlockAndUpdate(rear, GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV].defaultBlockState());
                helper.assertTrue(!machine.checkPattern() && machine.getIntention() == Intention.NONE, "Rear energy and stale selection rejected");
                helper.getLevel().setBlockAndUpdate(rear, WorldEaterCasingTests.casings()[0].defaultBlockState());
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getIntention() == Intention.NONE, "Recovery without intention");
                machine.onStructureInvalid();
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1400)
    public static void nativeIntentionLifecycle(GameTestHelper helper) {
        var fixtures = new WorldEaterRunTests.Fixture[]{WorldEaterRunTests.fixture(helper, 240000), WorldEaterRunTests.fixture(helper, 241000)};
        WorldEaterCasingTests.active(fixtures[0].logic());
        intentionLifecycle(helper, fixtures, 0, 0);
    }

    private static void intentionLifecycle(GameTestHelper helper, WorldEaterRunTests.Fixture[] fixtures, int stage, int waited) {
        Intention expected = switch (stage) { case 1 -> Intention.SAFETY; case 2, 6 -> Intention.BRAVERY; default -> Intention.NONE; };
        boolean ready = true;
        for (int i = 0; i < fixtures.length; i++) {
            var machine = fixtures[i].controller();
            helper.assertTrue(fixtures[i].logic().getProgressionState().name().equals(i == 0 ? "ACTIVE" : "DORMANT"), "Hatch changes retain progression");
            ready &= machine.getIntention() == expected && machine.isFormed() == (stage != 4);
        }
        if (!ready) {
            helper.assertTrue(waited < 160, "Native intention callbacks converge at stage " + stage);
            awaitPatternWorker();
            helper.runAfterDelay(1, () -> intentionLifecycle(helper, fixtures, stage, waited + 1));
            return;
        }
        if (stage == 7) { helper.succeed(); return; }
        for (int i = 0; i < fixtures.length; i++) {
            var f = fixtures[i];
            var machine = f.controller();
            var state = switch (stage) {
                case 0 -> IntentionHatchPartMachine.SAFETY.defaultBlockState();
                case 1, 5 -> IntentionHatchPartMachine.BRAVERY.defaultBlockState();
                case 3 -> Blocks.AIR.defaultBlockState();
                default -> WorldEaterCasingTests.casings()[0].defaultBlockState();
            };
            helper.getLevel().setBlockAndUpdate(intentionPosition(machine), state);
            if (stage == 6) {
                var holder = (net.minecraft.world.level.block.entity.BlockEntity) machine.getHolder();
                var saved = holder.saveWithFullMetadata();
                helper.getLevel().removeBlock(machine.getPos(), false);
                helper.getLevel().setBlockAndUpdate(machine.getPos(), machine.getDefinition().defaultBlockState());
                var fresh = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), machine.getPos());
                ((net.minecraft.world.level.block.entity.BlockEntity) fresh.getHolder()).load(saved.copy());
                helper.assertTrue(fresh.getIntention() == Intention.NONE, "Fresh load cannot expose unvalidated intention");
                fixtures[i] = new WorldEaterRunTests.Fixture(fresh, f.input(), f.output());
            }
        }
        helper.runAfterDelay(2, () -> intentionLifecycle(helper, fixtures, stage + 1, 0));
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void rotatedStructureAndCreativeTab(GameTestHelper helper) {
        var machine = shell(helper, Direction.EAST);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPattern(), "Rotated shell forms");
            helper.assertTrue(machine.getStructureCenter().equals(net.minecraft.world.phys.Vec3.atCenterOf(machine.getPos().west(2).above(2))), "Rotated geometric center is two blocks behind and above the controller");
            var tab = ModCreativeModTabs.GREGE_TAB.get();
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(machine.getDefinition().getItem())), "Creative registration");
            helper.succeed();
        });
    }
}
