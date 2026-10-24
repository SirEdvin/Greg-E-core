package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
            helper.runAfterDelay(1, () -> lifecycle(helper, fixtures, stage, waited + 1));
            return;
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
