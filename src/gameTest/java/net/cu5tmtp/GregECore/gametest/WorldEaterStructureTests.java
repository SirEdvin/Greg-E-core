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
    // Lazy: client checks load this class before block registration completes.
    static net.minecraft.world.level.block.Block rear() { return GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get(); }

    // Tests run one per batch, so they reuse a few areas: fresh chunks cost far more than resetting used ones. Consecutive
    // tests use different slots, because GTCEu may still run a queued async re-form for the previous test's controller.
    private static final int SHARED = 10000, SLOTS = 4, SLOT_SPACING = 4096;
    private static int slot;
    private static GameTestHelper current;
    private static BlockPos base;
    private static final List<BlockPos> used = new ArrayList<>();

    /** Maps a test's far relative position onto the shared area (keeping offsets within the test), resetting the previous test first. */
    static BlockPos far(GameTestHelper helper, BlockPos relative) {
        if (helper != current) {
            reset(helper.getLevel());
            current = helper;
            base = relative;
            slot = (slot + 1) % SLOTS;
        }
        var pos = new BlockPos(SHARED + slot * SLOT_SPACING + relative.getX() - base.getX(), helper.absolutePos(BlockPos.ZERO).getY() + relative.getY(),
                SHARED + relative.getZ() - base.getZ());
        used.add(pos);
        return pos;
    }

    private static boolean shared(int x, int z) {
        return x > SHARED - 8192 && x < SHARED + SLOTS * SLOT_SPACING + 8192 && Math.abs(z - SHARED) < 8192;
    }

    private static void reset(net.minecraft.server.level.ServerLevel level) {
        // GTCEu keeps pos -> formed-structure mappings even for removed controllers; stale ones swallow block-change callbacks.
        var structures = com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData.getOrCreate(level);
        structures.mapping.entrySet().stream().filter(e -> shared(e.getKey().getX(), e.getKey().getZ())).map(java.util.Map.Entry::getValue)
                .distinct().toList().forEach(structures::removeMapping);
        // Scan tests fill whole 9x9-chunk scan columns, so reset every loaded chunk around each center to the flat.json layers.
        // shortcut: chunks that unloaded mid-test keep their leftovers; track unloaded chunks if a later test trips on them.
        var chunks = new java.util.HashSet<net.minecraft.world.level.chunk.LevelChunk>();
        for (var center : used) for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
            var chunk = level.getChunkSource().getChunkNow((center.getX() >> 4) + dx, (center.getZ() >> 4) + dz);
            if (chunk != null) chunks.add(chunk);
        }
        int bottom = level.getMinBuildHeight();
        for (var chunk : chunks) {
            var chunkPos = chunk.getPos();
            var sections = chunk.getSections();
            for (int i = 0; i < sections.length; i++) {
                if (sections[i].hasOnlyAir()) continue;
                int minY = chunk.getSectionYFromSectionIndex(i) << 4;
                for (var pos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), minY, chunkPos.getMinBlockZ(),
                        chunkPos.getMaxBlockX(), minY + 15, chunkPos.getMaxBlockZ())) {
                    int layer = pos.getY() - bottom;
                    var flat = (layer == 0 ? Blocks.BEDROCK : layer <= 2 ? Blocks.DIRT : layer == 3 ? Blocks.GRASS_BLOCK : Blocks.AIR).defaultBlockState();
                    if (chunk.getBlockState(pos) != flat) level.setBlock(pos, flat, 2);
                }
            }
            level.getEntities((net.minecraft.world.entity.Entity) null, new net.minecraft.world.phys.AABB(chunkPos.getMinBlockX(), bottom,
                    chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX() + 1, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 1),
                    entity -> !(entity instanceof net.minecraft.world.entity.player.Player)).forEach(net.minecraft.world.entity.Entity::discard);
        }
        used.clear();
        for (long chunk : level.getForcedChunks().toLongArray()) {
            var pos = new net.minecraft.world.level.ChunkPos(chunk);
            if (shared(pos.getMinBlockX(), pos.getMinBlockZ())) level.setChunkForced(pos.x, pos.z, false);
        }
        WorldEaterUnloadTests.unloaded.clear();
    }

    /**
     * Completes formation the way GTCEu's async former does: under the pattern lock (otherwise its async re-check can swap
     * the match context mid-formation and leave a formed controller without parts), registering the block-change mapping
     * real breaks rely on, and leaving the async re-check queue.
     */
    static void formed(com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller) {
        var machine = controller.self();
        machine.getPatternLock().lock();
        try {
            controller.onStructureFormed();
            if (machine.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
                var structures = com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData.getOrCreate(level);
                structures.addMapping(machine.getMultiblockState());
                structures.removeAsyncLogic(controller);
            }
        } finally {
            machine.getPatternLock().unlock();
        }
    }

    /**
     * Invalidates like GTCEu's block-change path, but unmapped first: invalidation updates part block states, and with the
     * mapping still present that update re-checks the (intact) pattern and re-forms inline, after which the outer call
     * clears capabilities, leaving a formed controller with no inputs. Tests re-form explicitly, so no async re-check.
     */
    static void invalidated(com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController controller) {
        var machine = controller.self();
        if (machine.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
            com.gregtechceu.gtceu.api.pattern.MultiblockWorldSavedData.getOrCreate(level).removeMapping(machine.getMultiblockState());
        controller.onStructureInvalid();
    }

    /** Builds the complete structure with its controller at {@code relativeCenter}. */
    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, BlockPos relativeCenter) {
        return build(helper.getLevel(), far(helper, relativeCenter), facing);
    }

    /** Also used by the opt-in client checks, which have no GameTest helper. */
    static IncompleteWorldEaterTamer build(net.minecraft.server.level.ServerLevel level, BlockPos center, Direction facing) {
        var shape = IncompleteWorldEaterTamer.SHAPE;
        for (int aisle = 0; aisle < shape.length; aisle++) for (int row = 0; row < shape[aisle].length; row++)
            for (int column = 0; column < shape[aisle][row].length(); column++) {
                char symbol = shape[aisle][row].charAt(column);
                // Unrestricted positions keep whatever is there; flag 2 avoids thousands of neighbor updates.
                if (symbol != 'K' && symbol != ' ') level.setBlock(at(center, facing, aisle, row, column), block(symbol).defaultBlockState(), 2);
            }
        // Never adopt a stale controller (and its saved progression) left at this position.
        level.removeBlock(center, false);
        level.setBlockAndUpdate(center, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
        var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, center);
        // setFrontFacing re-checks the pattern bare; hold the lock so GTCEu's async checker cannot race it.
        controller.getPatternLock().lock();
        try { controller.setFrontFacing(facing); } finally { controller.getPatternLock().unlock(); }
        controller.getRecipeLogic().setWorkingEnabled(false);
        return controller;
    }

    /** A far-away shell whose surrounding chunks stay loaded. */
    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, int location) {
        var center = far(helper, new BlockPos(location, 3, location));
        for (int x = (center.getX() >> 4) - 1; x <= (center.getX() >> 4) + 1; x++)
            for (int z = (center.getZ() >> 4) - 1; z <= (center.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
        return shell(helper, facing, new BlockPos(location, 3, location));
    }

    static BlockPos at(BlockPos controller, Direction facing, int aisle, int row, int column) {
        return controller.relative(facing.getOpposite(), aisle).above(row - 7).relative(facing.getClockWise(), column - 7);
    }

    static net.minecraft.world.level.block.Block block(char symbol) {
        return switch (symbol) {
            case 'C', 'T' -> GreggyBlocks.WORLD_EATER_TAMER_CASING.get();
            case ' ' -> Blocks.AIR;
            case 'N' -> registered("gtceu:inert_machine_casing");
            case 'A' -> registered("gtceu:large_scale_assembler_casing");
            case 'H' -> registered("gtceu:high_temperature_smelting_casing");
            case 'E', 'I' -> rear();
            case 'P' -> GreggyBlocks.PTFE_ENGINE_INTAKE.get();
            case 'G' -> registered("gtceu:laminated_glass");
            case 'S' -> registered("gtceu:superconducting_coil");
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
        return positions(controller, "C");
    }

    /** Inner-ring casing positions, whose colors decide the casing tier. */
    static List<BlockPos> tierPositions(IncompleteWorldEaterTamer controller) {
        return positions(controller, "T");
    }

    /** Recolors the tier ring: 0 black, 1-3 one color, -1 a mix of all three colors. */
    static void colorFrame(IncompleteWorldEaterTamer machine, int color) {
        int index = 0;
        for (var pos : tierPositions(machine))
            machine.getLevel().setBlockAndUpdate(pos, WorldEaterCasingTests.casings()[color == -1 ? 1 + index++ % 3 : color].defaultBlockState());
    }

    static ItemBusPartMachine bus(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.getLevel().setBlockAndUpdate(pos, definition.defaultBlockState());
        return (ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
    }

    static void assertTier(GameTestHelper helper, IncompleteWorldEaterTamer machine, int tier) {
        helper.assertTrue(machine.checkPatternWithLock(), "Tier fixture matches: facing=" + machine.getFrontFacing()
                + " error=" + (machine.getMultiblockState().error == null ? "none" : machine.getMultiblockState().error.getErrorInfo().getString())
                + " at=" + machine.getMultiblockState().getPos());
        WorldEaterStructureTests.formed(machine);
        helper.assertTrue(machine.getCasingTier() == tier, "Expected casing tier " + tier + ", got " + machine.getCasingTier());
    }

    @GameTest(template = "empty", timeoutTicks = 200, batch = "structureLimits")
    public static void structureLimits(GameTestHelper helper) {
        var machine = shell(helper, Direction.NORTH, 120000);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPatternWithLock(), "Complete structure forms with an empty interior");
            var positions = shellPositions(machine);
            helper.assertTrue(positions.size() == 180, "Exactly 180 World Eater casing positions");
            for (var pos : positions(machine, " ")) helper.getLevel().setBlock(pos, Blocks.STONE.defaultBlockState(), 2);
            helper.assertTrue(machine.checkPatternWithLock(), "Arbitrary blocks in unrestricted positions form");
            for (var pos : positions(machine, " ")) helper.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            helper.assertTrue(machine.checkPatternWithLock(), "Empty unrestricted positions form");
            for (var pos : positions(machine, "CNAHEPGSTI")) {
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
            for (var pos : positions(machine, "NAHEPGSTI")) {
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
            WorldEaterStructureTests.formed(machine);
            helper.assertTrue(machine.getCasingTier() == 1, "Black frame is tier 1");
            var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
            helper.assertTrue(!logic.isCountableInterface(machine), "Exclude self");
            for (var part : machine.getParts()) helper.assertTrue(!logic.isCountableInterface(part.self()), "Exclude attached part");
            WorldEaterStructureTests.invalidated(machine);
            helper.assertTrue(machine.getCasingTier() == 0, "Invalidation clears tier");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300, batch = "casingTiersAndExclusions")
    public static void casingTiersAndExclusions(GameTestHelper helper) {
        var machines = new ArrayList<IncompleteWorldEaterTamer>();
        int location = 130000;
        for (var facing : Direction.Plane.HORIZONTAL) {
            machines.add(shell(helper, facing, location));
            location += 100;
        }
        helper.runAfterDelay(6, () -> {
            for (var machine : machines) {
                System.out.println("WORLD_EATER_TIER_CHECK facing=" + machine.getFrontFacing() + " stage=uniform-and-traversal");
                assertTier(helper, machine, 1);
                assertBlackAppearance(helper, machine);
                for (int color = 1; color <= 3; color++) {
                    colorFrame(machine, color);
                    assertTier(helper, machine, 2);
                    assertBlackAppearance(helper, machine);
                }
                colorFrame(machine, -1);
                var tiers = tierPositions(machine);
                helper.assertTrue(tiers.size() == 44, "Exactly 44 tier-ring casings");
                // A stride samples every ring without hundreds of full-structure checks.
                for (int i = 0; i < tiers.size(); i += 5) {
                    helper.getLevel().setBlockAndUpdate(tiers.get(i), WorldEaterCasingTests.casings()[0].defaultBlockState());
                    assertTier(helper, machine, 1);
                    helper.getLevel().setBlockAndUpdate(tiers.get(i), WorldEaterCasingTests.casings()[1].defaultBlockState());
                    assertTier(helper, machine, 2);
                }
                for (var rejected : new net.minecraft.world.level.block.Block[]{GTBlocks.MACHINE_CASING_UHV.get(), Blocks.STONE,
                        GTMachines.ITEM_IMPORT_BUS[GTValues.LV].getBlock()}) {
                    helper.getLevel().setBlockAndUpdate(tiers.get(0), rejected.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Tier ring rejects " + rejected);
                }
                helper.getLevel().setBlockAndUpdate(tiers.get(0), WorldEaterCasingTests.casings()[1].defaultBlockState());
                // Black frame casing and unrestricted black casing never lower the tier.
                var center = BlockPos.containing(machine.getStructureCenter());
                helper.getLevel().setBlockAndUpdate(center, WorldEaterCasingTests.casings()[0].defaultBlockState());
                assertTier(helper, machine, 2);
                var positions = shellPositions(machine);
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
                    helper.assertTrue(!machine.checkPatternWithLock() && machine.getCasingTier() == 0, "Tier-2 structure rejects sixth " + type.getId());
                }
                helper.getLevel().setBlockAndUpdate(positions.get(21), com.gregtechceu.gtceu.common.data.machines.GCYMMachines.PARALLEL_HATCH[GTValues.IV].defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock() && machine.getCasingTier() == 0, "Tier-2 structure rejects second parallel hatch");
                helper.getLevel().setBlockAndUpdate(positions.get(21), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock() && machine.getCasingTier() == 0, "Tier-2 structure rejects energy");
                helper.getLevel().setBlockAndUpdate(positions.get(21), WorldEaterCasingTests.casings()[0].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(tiers.get(1), WorldEaterCasingTests.casings()[0].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(42), Blocks.AIR.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock() && machine.getCasingTier() == 0, "Failed partial black match unavailable");
                helper.getLevel().setBlockAndUpdate(tiers.get(1), WorldEaterCasingTests.casings()[2].defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(42), WorldEaterCasingTests.casings()[0].defaultBlockState());
                System.out.println("WORLD_EATER_TIER_CHECK facing=" + machine.getFrontFacing() + " stage=recovery");
                assertTier(helper, machine, 2);
                assertTier(helper, machine, 2);
                machine.getMultiblockState().getMatchContext().reset();
                helper.assertTrue(machine.getCasingTier() == 0, "Missing match data never defaults to tier 2");
                assertTier(helper, machine, 2);
                WorldEaterStructureTests.invalidated(machine);
                helper.assertTrue(machine.getCasingTier() == 0, "Invalidation clears tier");
                assertBlackAppearance(helper, machine);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1200, batch = "nativeCasingTierLifecycle")
    public static void nativeCasingTierLifecycle(GameTestHelper helper) {
        var active = WorldEaterRunTests.fixture(helper, 140000);
        var dormant = WorldEaterRunTests.fixture(helper, 141000);
        WorldEaterCasingTests.active(active.logic());
        lifecycle(helper, new WorldEaterRunTests.Fixture[]{active, dormant}, 0, 0);
    }

    /** Controller and formed parts keep the black casing whatever the tier-ring colors are. */
    private static void assertBlackAppearance(GameTestHelper helper, IncompleteWorldEaterTamer machine) {
        var casing = WorldEaterCasingTests.casings()[0].defaultBlockState();
        for (var side : Direction.values()) {
            helper.assertTrue(machine.getBlockAppearance(machine.getBlockState(), machine.getLevel(), machine.getPos(), side, casing,
                    machine.getPos().relative(side)) == casing, "Controller keeps black CTM appearance");
            for (var part : machine.getParts())
                helper.assertTrue(part.getFormedAppearance(null, null, side) == casing, "Formed part keeps black appearance");
        }
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
            var pos = tierPositions(machine).get(3);
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

    @GameTest(template = "empty", timeoutTicks = 300, batch = "intentionPlacementAndSharedLimits")
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
                colorFrame(machine, -1);
                for (var variant : new MachineDefinition[]{IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY}) {
                    helper.getLevel().setBlockAndUpdate(rear, variant.defaultBlockState());
                    assertTier(helper, machine, 2);
                    var hatch = (IntentionHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), rear);
                    helper.assertTrue(machine.getIntention() == hatch.getIntention() && machine.getParts().contains(hatch), "Correct attached variant: " + machine.getFrontFacing());
                    helper.assertTrue(hatch.replacePartModelWhenFormed(), "Formed hatch inherits controller casing");
                    WorldEaterStructureTests.invalidated(machine);
                    helper.assertTrue(!hatch.replacePartModelWhenFormed(), "Detached hatch restores standalone hull");
                    assertTier(helper, machine, 2);
                    for (int i = 0; i < positions.size(); i += 13) {
                        var wrong = positions.get(i);
                        helper.getLevel().setBlockAndUpdate(wrong, IntentionHatchPartMachine.SAFETY.defaultBlockState());
                        helper.assertTrue(!machine.checkPatternWithLock() && machine.getIntention() == Intention.NONE, "Second/misplaced frame hatch rejected: " + wrong);
                        helper.getLevel().setBlockAndUpdate(wrong, WorldEaterCasingTests.casings()[0].defaultBlockState());
                    }
                    assertTier(helper, machine, 2);
                }
                helper.getLevel().setBlockAndUpdate(rear, rear().defaultBlockState());
                helper.getLevel().setBlockAndUpdate(positions.get(0), IntentionHatchPartMachine.BRAVERY.defaultBlockState());
                helper.assertTrue(!machine.checkPatternWithLock(), "Lone hatch at wrong frame coordinate rejected");
                helper.getLevel().setBlockAndUpdate(positions.get(0), WorldEaterCasingTests.casings()[0].defaultBlockState());
                var center = BlockPos.containing(machine.getStructureCenter());
                for (var pos : new BlockPos[]{center, center.above()})
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
                assertTier(helper, machine, 2);
                helper.assertTrue(machine.getIntention() == Intention.NONE, "Recovery without intention");
                WorldEaterStructureTests.invalidated(machine);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1400, batch = "nativeIntentionLifecycle")
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

    @GameTest(template = "empty", timeoutTicks = 200, batch = "rotatedStructureAndCreativeTab")
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

    @GameTest(template = "empty", timeoutTicks = 400, batch = "meDemandOverlappingOwnership")
    public static void meDemandOverlappingOwnership(GameTestHelper helper) {
        if (!com.gregtechceu.gtceu.GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        var origin = new BlockPos(311000, 3, 311000);
        var absolute = far(helper, origin);
        for (int x = (absolute.getX() >> 4) - 1; x <= (absolute.getX() >> 4) + 2; x++)
            for (int z = (absolute.getZ() >> 4) - 1; z <= (absolute.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
        var a = shell(helper, Direction.NORTH, origin);
        // Mirror-symmetric side faces coincide when the second structure is 14 blocks east.
        var b = shell(helper, Direction.NORTH, origin.east(14));
        colorFrame(a, 1); colorFrame(b, 1);
        var pos = shellPositions(a).stream().filter(p -> p.getX() == a.getPos().getX() + 7).findFirst().orElseThrow();
        helper.getLevel().setBlockAndUpdate(pos, net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(GregECore.id("me_demand_hatch")).defaultBlockState());
        helper.runAfterDelay(8, () -> {
            WorldEaterStructureTests.invalidated(a); WorldEaterStructureTests.invalidated(b);
            assertTier(helper, a, 2);
            var hatch = (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart) MetaMachine.getMachine(helper.getLevel(), pos);
            helper.assertTrue(hatch.getControllers().size() == 1 && hatch.getControllers().contains(a), "First native controller owns shared-position hatch");
            helper.assertTrue(!b.checkPatternWithLock(), "Overlapping second controller cannot attach owned hatch");
            WorldEaterStructureTests.invalidated(a);
            assertTier(helper, b, 2);
            helper.assertTrue(hatch.getControllers().size() == 1 && hatch.getControllers().contains(b), "Native detachment permits one new owner");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 600, batch = "meDemandPlacementAndIndependentLimits")
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
                colorFrame(machine, 1);
                var positions = shellPositions(machine);
                // A stride samples every face and aisle without hundreds of full-structure checks.
                for (int i = 0; i < positions.size(); i += 13) {
                    var pos = positions.get(i);
                    helper.getLevel().setBlockAndUpdate(pos, hatchBlock.defaultBlockState());
                    assertTier(helper, machine, 2);
                    var hatch = (com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart) MetaMachine.getMachine(helper.getLevel(), pos);
                    helper.assertTrue(machine.getParts().contains(hatch) && !hatch.canShared(), "Single-owner demand hatch at " + pos);
                    helper.assertTrue(machine.getIntention() == Intention.NONE && hatch.replacePartModelWhenFormed(), "No intention, native formed appearance");
                    var second = positions.get(pos.equals(positions.get(0)) ? 1 : 0);
                    helper.getLevel().setBlockAndUpdate(second, hatchBlock.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Second demand hatch cannot use ordinary bus allowance");
                    WorldEaterStructureTests.invalidated(machine);
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
                assertTier(helper, machine, 2);
                helper.assertTrue(machine.getParts().size() == 23 && machine.getIntention() == Intention.SAFETY, "Independent allowances; decorative hatches unattached");
                for (var type : List.of(types[0], types[1], types[2], types[3], parallel, GTMachines.ENERGY_INPUT_HATCH[GTValues.LV])) {
                    helper.getLevel().setBlockAndUpdate(positions.get(22), type.defaultBlockState());
                    helper.assertTrue(!machine.checkPatternWithLock(), "Existing limits preserved with demand hatch: " + type.getId());
                }
                WorldEaterStructureTests.invalidated(machine);
            }
            helper.succeed();
        });
    }
}
