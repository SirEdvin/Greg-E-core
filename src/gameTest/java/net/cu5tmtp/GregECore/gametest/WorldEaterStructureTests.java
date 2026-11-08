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
    // Lazy: client checks load this class before block registration completes.
    static net.minecraft.world.level.block.Block rear() { return GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get(); }

    /** Builds the complete structure with its controller at {@code relativeCenter}. */
    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, BlockPos relativeCenter) {
        return build(helper.getLevel(), helper.absolutePos(relativeCenter), facing);
    }

    /** Also used by the opt-in client checks, which have no GameTest helper. */
    static IncompleteWorldEaterTamer build(net.minecraft.server.level.ServerLevel level, BlockPos center, Direction facing) {
        var shape = IncompleteWorldEaterTamer.SHAPE;
        for (int aisle = 0; aisle < shape.length; aisle++) for (int row = 0; row < shape[aisle].length; row++)
            for (int column = 0; column < shape[aisle][row].length(); column++) {
                char symbol = shape[aisle][row].charAt(column);
                // Unrestricted positions keep whatever is there; flag 2 avoids thousands of neighbor updates.
                if (symbol != 'S' && symbol != 'b') level.setBlock(at(center, facing, aisle, row, column), block(symbol).defaultBlockState(), 2);
            }
        // Never adopt a stale controller (and its saved progression) left at this position.
        level.removeBlock(center, false);
        level.setBlockAndUpdate(center, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
        var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, center);
        controller.setFrontFacing(facing);
        controller.getRecipeLogic().setWorkingEnabled(false);
        return controller;
    }

    /** A far-away shell whose surrounding chunks stay loaded. */
    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, int location) {
        var center = helper.absolutePos(new BlockPos(location, 3, location));
        for (int x = (center.getX() >> 4) - 1; x <= (center.getX() >> 4) + 1; x++)
            for (int z = (center.getZ() >> 4) - 1; z <= (center.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
        return shell(helper, facing, new BlockPos(location, 3, location));
    }

    static BlockPos at(BlockPos controller, Direction facing, int aisle, int row, int column) {
        return controller.relative(facing.getOpposite(), aisle).above(row - 7).relative(facing.getClockWise(), column - 7);
    }

    static net.minecraft.world.level.block.Block block(char symbol) {
        return switch (symbol) {
            case 'a' -> GreggyBlocks.WORLD_EATER_TAMER_CASING.get();
            case 'b' -> Blocks.AIR;
            case 'c' -> registered("gtceu:inert_machine_casing");
            case 'd' -> registered("gtceu:large_scale_assembler_casing");
            case 'e' -> registered("gtceu:high_temperature_smelting_casing");
            case 'f', 'I' -> rear();
            case 'g' -> GreggyBlocks.PTFE_ENGINE_INTAKE.get();
            case 'h' -> registered("gtceu:laminated_glass");
            case 'i' -> registered("gtceu:superconducting_coil");
            case 'j' -> registered("gtceu:shock_proof_cutting_casing");
            default -> throw new IllegalArgumentException("Unknown symbol " + symbol);
        };
    }

    private static net.minecraft.world.level.block.Block registered(String id) {
        return net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(net.minecraft.resources.ResourceLocation.parse(id));
    }

    /** Positions of the given pattern symbols, front aisle first. */
    static List<BlockPos> positions(IncompleteWorldEaterTamer controller, String symbols) {
        List<BlockPos> positions = new ArrayList<>();
        var shape = IncompleteWorldEaterTamer.SHAPE;
        for (int aisle = 0; aisle < shape.length; aisle++) for (int row = 0; row < shape[aisle].length; row++)
            for (int column = 0; column < shape[aisle][row].length(); column++)
                if (symbols.indexOf(shape[aisle][row].charAt(column)) >= 0)
                    positions.add(at(controller.getPos(), controller.getFrontFacing(), aisle, row, column));
        return positions;
    }

    /** World Eater casing positions, where ordinary parts may substitute. */
    static List<BlockPos> shellPositions(IncompleteWorldEaterTamer controller) {
        return positions(controller, "a");
    }

    static ItemBusPartMachine bus(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.getLevel().setBlockAndUpdate(pos, definition.defaultBlockState());
        return (ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
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
        var machine = shell(helper, Direction.NORTH, 120000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPatternWithLock(), "Complete structure forms with an empty interior");
            var positions = shellPositions(machine);
            helper.assertTrue(positions.size() == 180, "Exactly 180 World Eater casing positions");
            for (var pos : positions(machine, "b")) helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            helper.assertTrue(machine.checkPatternWithLock(), "Arbitrary blocks in unrestricted positions form");
            for (var pos : positions(machine, "b")) helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            helper.assertTrue(machine.checkPatternWithLock(), "Empty unrestricted positions form");
            for (var pos : positions(machine, "acdefghijI")) {
                var state = helper.getLevel().getBlockState(pos);
                helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Every structural block is required: " + pos);
                helper.getLevel().setBlockAndUpdate(pos, state);
            }
            MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
            for (int type = 0; type < types.length; type++) for (int count = 0; count < 5; count++)
                helper.getLevel().setBlockAndUpdate(positions.get(type * 5 + count), types[type].defaultBlockState());
            helper.assertTrue(machine.checkPatternWithLock(), "Five of EACH part type form");
            for (var type : types) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), type.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Reject sixth " + type.getId());
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            for (var pos : positions(machine, "cdefghijI")) {
                var state = helper.getLevel().getBlockState(pos);
                helper.getLevel().setBlockAndUpdate(pos, types[0].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(0), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Parts only replace World Eater casings: " + pos);
                helper.getLevel().setBlockAndUpdate(pos, state);
                helper.getLevel().setBlockAndUpdate(positions.get(0), types[0].defaultBlockState());
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].defaultBlockState());
            helper.assertTrue(!machine.checkPatternWithLock(), "Reject energy hatch");
            var rejected = new ArrayList<>(List.of(GTBlocks.MACHINE_CASING_UHV.get(), Blocks.STONE));
            rejected.addAll(List.of(WorldEaterCasingTests.casings()).subList(1, 4));
            for (var block : rejected) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), block.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Reject unsupported frame casing: " + block);
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            helper.assertTrue(machine.checkPatternWithLock(), "Restored structure forms");
            machine.onStructureFormed();
            helper.assertTrue(machine.getCasingTier() == 1, "Black frame is tier 1");
            var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
            helper.assertTrue(!logic.isCountableInterface(machine), "Exclude self");
            for (var part : machine.getParts()) helper.assertTrue(!logic.isCountableInterface(part.self()), "Exclude attached part");
            machine.onStructureInvalid();
            helper.assertTrue(machine.getCasingTier() == 0, "Invalidation clears tier");
            helper.succeed();
        });
    }

    static BlockPos intentionPosition(IncompleteWorldEaterTamer machine) {
        return machine.getPos().relative(machine.getFrontFacing().getOpposite(), 14);
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
            machines.add(shell(helper, facing, location));
            location += 100;
        }
        helper.runAfterDelay(6, () -> {
            for (var machine : machines) {
                var rear = intentionPosition(machine);
                var positions = shellPositions(machine);
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getIntention() == Intention.NONE, "No implicit intention");
                for (var variant : new MachineDefinition[]{IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY}) {
                    helper.getLevel().setBlockAndUpdate(rear, variant.defaultBlockState());
                    assertTier(helper, machine, 1);
                    var hatch = (IntentionHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), rear);
                    helper.assertTrue(machine.getIntention() == hatch.getIntention() && machine.getParts().contains(hatch), "Correct attached variant: " + machine.getFrontFacing());
                    helper.assertTrue(hatch.replacePartModelWhenFormed(), "Formed hatch inherits controller casing");
                    machine.onStructureInvalid();
                    helper.assertTrue(!hatch.replacePartModelWhenFormed(), "Detached hatch restores standalone hull");
                    assertTier(helper, machine, 1);
                    for (int i = 0; i < positions.size(); i += 13) {
                        var wrong = positions.get(i);
                        helper.getLevel().setBlockAndUpdate(wrong, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                        helper.assertTrue(!machine.checkPatternWithLock() && machine.getIntention() == Intention.NONE, "Second/misplaced frame hatch rejected: " + wrong);
                        helper.getLevel().setBlockAndUpdate(wrong, WorldEaterCasingTests.casings()[0].defaultBlockState());
                    }
                    assertTier(helper, machine, 1);
                }
                helper.getLevel().setBlockAndUpdate(rear, rear().defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(0), IntentionHatchPartMachine.BRAVERY.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Lone hatch at wrong frame coordinate rejected");
                helper.getLevel().setBlockAndUpdate(positions.get(0), WorldEaterCasingTests.casings()[0].defaultBlockState());
                var center = BlockPos.containing(machine.getStructureCenter());
                for (var pos : new BlockPos[]{center, center.above()})
                    helper.getLevel().setBlockAndUpdate(pos, IntentionHatchPartMachine.BRAVERY.defaultBlockState());
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getIntention() == Intention.NONE && machine.getParts().isEmpty(), "Unrestricted hatches do not attach");
                MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                        GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
                var parallel = com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV];
                for (int t = 0; t < types.length; t++) for (int count = 0; count < 5; count++)
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5 + count), types[t].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(20), parallel.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(rear, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getParts().size() == 22 && machine.getIntention() == Intention.SAFETY, "Intention has its own allowance");
                helper.getLevel().setBlockAndUpdate(positions.get(20), WorldEaterCasingTests.casings()[0].defaultBlockState());
                for (int t = 0; t < types.length; t++) {
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5), WorldEaterCasingTests.casings()[0].defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(rear, types[t].defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Rear slot rejects ordinary parts");
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5), types[t].defaultBlockState());
                }
                for (var rejected : List.of(parallel, GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV])) {
                    helper.getLevel().setBlockAndUpdate(rear, rejected.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock() && machine.getIntention() == Intention.NONE, "Rear parallel, energy and stale selection rejected");
                }
                helper.getLevel().setBlockAndUpdate(rear, rear().defaultBlockState());
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
                default -> rear().defaultBlockState();
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
        var machine = shell(helper, Direction.EAST, 121000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPatternWithLock(), "Rotated shell forms");
            helper.assertTrue(machine.getStructureCenter().equals(net.minecraft.world.phys.Vec3.atCenterOf(machine.getPos().west(7))), "Rotated geometric center is seven blocks behind the controller");
            var tab = ModCreativeModTabs.GREGE_TAB.get();
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(machine.getDefinition().getItem())), "Creative registration");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void meDemandOverlappingOwnership(GameTestHelper helper) {
        if (!com.gregtechceu.gtceu.GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        var origin = new BlockPos(311000, 3, 311000);
        var absolute = helper.absolutePos(origin);
        for (int x = (absolute.getX() >> 4) - 1; x <= (absolute.getX() >> 4) + 2; x++)
            for (int z = (absolute.getZ() >> 4) - 1; z <= (absolute.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
        var a = shell(helper, Direction.NORTH, origin);
        // Mirror-symmetric side faces coincide when the second structure is 14 blocks east.
        var b = shell(helper, Direction.NORTH, origin.east(14));
        var pos = shellPositions(a).stream().filter(p -> p.getX() == a.getPos().getX() + 7).findFirst().orElseThrow();
        helper.getLevel().setBlockAndUpdate(pos, net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(GregECore.id("me_demand_hatch")).defaultBlockState());
        helper.runAfterDelay(8, () -> {
            a.onStructureInvalid(); b.onStructureInvalid();
            assertTier(helper, a, 1);
            var hatch = (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart) MetaMachine.getMachine(helper.getLevel(), pos);
            helper.assertTrue(hatch.getControllers().size() == 1 && hatch.getControllers().contains(a), "First native controller owns shared-position hatch");
            helper.assertTrue(!b.checkPatternWithLock(), "Overlapping second controller cannot attach owned hatch");
            a.onStructureInvalid();
            assertTier(helper, b, 1);
            helper.assertTrue(hatch.getControllers().size() == 1 && hatch.getControllers().contains(b), "Native detachment permits one new owner");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600)
    public static void meDemandPlacementAndIndependentLimits(GameTestHelper helper) {
        if (!com.gregtechceu.gtceu.GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        var hatchBlock = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(GregECore.id("me_demand_hatch"));
        var machines = new ArrayList<IncompleteWorldEaterTamer>();
        int location = 310000;
        for (var facing : Direction.Plane.HORIZONTAL) {
            machines.add(shell(helper, facing, location));
            location += 100;
        }
        helper.runAfterDelay(8, () -> {
            for (var machine : machines) {
                var positions = shellPositions(machine);
                // A stride samples every face and aisle without hundreds of full-structure checks.
                for (int i = 0; i < positions.size(); i += 13) {
                    var pos = positions.get(i);
                    helper.getLevel().setBlockAndUpdate(pos, hatchBlock.defaultBlockState());
                    assertTier(helper, machine, 1);
                    var hatch = (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart) MetaMachine.getMachine(helper.getLevel(), pos);
                    helper.assertTrue(machine.getParts().contains(hatch) && !hatch.canShared(), "Single-owner demand hatch at " + pos);
                    helper.assertTrue(machine.getIntention() == Intention.NONE && hatch.replacePartModelWhenFormed(), "No intention, native formed appearance");
                    var second = positions.get(pos.equals(positions.get(0)) ? 1 : 0);
                    helper.getLevel().setBlockAndUpdate(second, hatchBlock.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Second demand hatch cannot use ordinary bus allowance");
                    machine.onStructureInvalid();
                    helper.getLevel().setBlockAndUpdate(second, WorldEaterCasingTests.casings()[0].defaultBlockState());
                    helper.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[0].defaultBlockState());
                }
                var rear = intentionPosition(machine);
                MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                        GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
                for (int t = 0; t < types.length; t++) for (int n = 0; n < 5; n++)
                    helper.getLevel().setBlockAndUpdate(positions.get(t * 5 + n), types[t].defaultBlockState());
                var parallel = com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV];
                helper.getLevel().setBlockAndUpdate(positions.get(20), parallel.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(21), hatchBlock.defaultBlockState());
                helper.getLevel().setBlockAndUpdate(rear, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                var center = BlockPos.containing(machine.getStructureCenter());
                for (var pos : List.of(center, center.above()))
                    helper.getLevel().setBlockAndUpdate(pos, hatchBlock.defaultBlockState());
                assertTier(helper, machine, 1);
                helper.assertTrue(machine.getParts().size() == 23 && machine.getIntention() == Intention.SAFETY, "Independent allowances; decorative hatches unattached");
                for (var type : List.of(types[0], types[1], types[2], types[3], parallel, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV])) {
                    helper.getLevel().setBlockAndUpdate(positions.get(22), type.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Existing limits preserved with demand hatch: " + type.getId());
                }
                machine.onStructureInvalid();
            }
            helper.succeed();
        });
    }
}
