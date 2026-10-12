package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterScanTests {
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void currentChunkTracksScanAndRetry(GameTestHelper helper) {
        var fixture = WorldEaterRunTests.fixture(helper, -65000);
        helper.runAfterDelay(4, () -> {
            WorldEaterRunTests.form(helper, fixture);
            var logic = fixture.logic();
            var level = helper.getLevel();
            int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
            var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
            helper.assertTrue(logic.getCurrentChunk() == null, "Idle has no scanned chunk");
            logic.setWorkingEnabled(true);
            logic.setupRecipe(WorldEaterRunTests.recipe());
            WorldEaterRunTests.blockOutput(fixture);
            var first = new net.minecraft.world.level.ChunkPos(cx - 4, cz - 4);
            helper.assertTrue(first.equals(logic.getCurrentChunk()), "Setup shows first chunk including negative coordinates");
            logic.serverTick();
            helper.assertTrue(first.equals(logic.getCurrentChunk()) && logic.getRetryAttempts() == 1, "Unavailable chunk remains visible during retry");
            var holder = (net.minecraft.world.level.block.entity.BlockEntity) fixture.controller().getHolder();
            holder.load(holder.saveWithFullMetadata());
            helper.assertTrue(first.equals(logic.getCurrentChunk()), "Retry chunk reconstructed after reload");
            logic.setWorkingEnabled(false);
            for (int i = 0; i < 30; i++) logic.serverTick();
            helper.assertTrue(first.equals(logic.getCurrentChunk()) && logic.getProgress() == 1, "Suspension retains pending chunk");
            for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) level.getChunk(x, z);
            logic.setWorkingEnabled(true);
            for (int i = 0; i < 19; i++) logic.serverTick();
            for (int tick = 0; tick < 2400; tick++) {
                logic.serverTick();
                if (tick == 2399) {
                    helper.assertTrue(logic.getCurrentChunk() == null && logic.hasPendingReport(), "Completed report clears chunk display");
                } else {
                    long next = domain.boundary(tick + 1);
                    var expected = new net.minecraft.world.level.ChunkPos(domain.chunkX(next), domain.chunkZ(next));
                    helper.assertTrue(expected.equals(logic.getCurrentChunk()), "Display follows deterministic cursor at tick " + tick);
                    if (tick == 80) {
                        holder.load(holder.saveWithFullMetadata());
                        helper.assertTrue(expected.equals(logic.getCurrentChunk()), "Active cursor reconstructed after reload");
                    }
                }
            }
            logic.setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void recoveryOnEveryAllowedAttempt(GameTestHelper helper) {
        var fixtures = new WorldEaterRunTests.Fixture[5];
        for (int attempt = 1; attempt <= 5; attempt++) fixtures[attempt - 1] = WorldEaterRunTests.fixture(helper, 40000 + attempt * 1000);
        helper.runAfterDelay(4, () -> {
            for (int attempt = 1; attempt <= 5; attempt++) {
                var fixture = fixtures[attempt - 1];
                WorldEaterRunTests.form(helper, fixture);
                fixture.logic().setWorkingEnabled(true);
                fixture.logic().setupRecipe(WorldEaterRunTests.recipe());
                WorldEaterRunTests.blockOutput(fixture);
                for (int tick = 0; tick < attempt * 20; tick++) fixture.logic().serverTick();
                helper.assertTrue(fixture.logic().getElapsedDelays() == attempt * 20, "Exactly 20 skipped ticks per retry");
                helper.assertTrue(fixture.logic().getRetryAttempts() == attempt && fixture.logic().getCounts().isEmpty(), "Allowance and atomic pending section");
                int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
                var level = helper.getLevel();
                for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) level.getChunk(x, z);
                var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
                BlockPos first = new BlockPos(domain.x(0), domain.y(0), domain.z(0));
                level.setBlockAndUpdate(first, GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
                for (int tick = 0; tick < 2400; tick++) {
                    fixture.logic().serverTick();
                    if (tick == 0) {
                        helper.assertTrue(fixture.logic().getRetryAttempts() == 0 && fixture.logic().getProgress() == attempt * 20 + 1, "Retry resets and same section resumes");
                        helper.assertTrue(fixture.logic().getCounts().values().stream().mapToLong(Long::longValue).sum() == 1, "First position retained");
                    }
                    if (tick < 2399) helper.assertTrue(!fixture.logic().hasPendingReport(), "No early completion after recovery");
                }
                helper.assertTrue(fixture.logic().hasPendingReport() && fixture.logic().getProgress() == 2400 + attempt * 20, "Completion timing after recovery " + attempt);
                fixture.logic().setWorkingEnabled(false);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void eligibilityAndBoundaryCoverage(GameTestHelper helper) {
        var fixture = WorldEaterRunTests.fixture(helper, 51000);
        helper.runAfterDelay(4, () -> {
            WorldEaterRunTests.form(helper, fixture);
            var level = helper.getLevel();
            int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
            for (int x = cx - 5; x <= cx + 5; x++) for (int z = cz - 5; z <= cz + 5; z++) level.getChunk(x, z);
            var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
            for (long index : new long[]{0, domain.volume() - 1})
                level.setBlockAndUpdate(new BlockPos(domain.x(index), domain.y(index), domain.z(index)), GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
            BlockPos controllerPos = fixture.controller().getPos().above(10);
            level.setBlockAndUpdate(controllerPos, fixture.controller().getDefinition().defaultBlockState());
            BlockPos partPos = controllerPos.east();
            level.setBlockAndUpdate(partPos, GTMachines.ITEM_IMPORT_BUS[GTValues.LV].defaultBlockState());
            level.setBlockAndUpdate(controllerPos.west(), Blocks.CHEST.defaultBlockState());
            level.setBlockAndUpdate(new BlockPos((cx + 5) * 16, domain.minY(), cz * 16), GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
            BlockPos outside = new BlockPos((cx + 5) * 16, fixture.controller().getPos().getY() + 10, cz * 16);
            var external = WorldEaterStructureTests.shell(helper, Direction.EAST, helper.relativePos(outside));
            var externalPart = WorldEaterStructureTests.bus(helper, outside.west(4), GTMachines.ITEM_IMPORT_BUS[GTValues.LV]);
            helper.assertTrue(external.checkPatternWithLock(), "Outside controller has inside part");
            external.onStructureFormed();
            helper.assertTrue(!fixture.logic().isCountableInterface(externalPart), "Never follow outside controller through an inside part");
            helper.assertTrue(fixture.logic().isCountableInterface(MetaMachine.getMachine(level, controllerPos)), "Other controller eligible");
            helper.assertTrue(!fixture.logic().isCountableInterface(MetaMachine.getMachine(level, partPos)), "Unattached part excluded");
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().setupRecipe(WorldEaterRunTests.recipe());
            WorldEaterRunTests.blockOutput(fixture);
            for (int tick = 0; tick < 2400; tick++) {
                fixture.logic().serverTick();
                if (tick == 2398) helper.assertTrue(fixture.logic().getCounts().getOrDefault(GTMachines.MACERATOR[GTValues.LV].getId(), 0L) == 1, "Final position not visited early");
            }
            helper.assertTrue(fixture.logic().hasPendingReport(), "Bounded run finished");
            helper.assertTrue(fixture.logic().getCounts().getOrDefault(GTMachines.MACERATOR[GTValues.LV].getId(), 0L) == 2, "Low and high boundary grouped; outside excluded");
            helper.assertTrue(fixture.logic().getCounts().values().stream().mapToLong(Long::longValue).sum() == 3, "Only two standalone machines and one other controller");
            fixture.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void failureAfterHighPartialCount(GameTestHelper helper) {
        var fixture = WorldEaterRunTests.fixture(helper, 53000);
        helper.runAfterDelay(4, () -> {
            WorldEaterRunTests.form(helper, fixture);
            var level = helper.getLevel();
            int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
            var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
            level.getChunk(cx - 4, cz - 4);
            for (int i = 0; i < 501; i++)
                level.setBlockAndUpdate(new BlockPos(domain.x(i), domain.y(i), domain.z(i)), GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().setupRecipe(WorldEaterRunTests.recipe());
            WorldEaterRunTests.blockOutput(fixture);
            for (int tick = 0; tick < 200 && !fixture.logic().hasPendingReport(); tick++) fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().hasPendingReport(), "Early exhaustion before base remainder");
            helper.assertTrue(fixture.logic().getCounts().values().stream().mapToLong(Long::longValue).sum() == 501, "Partial high total retained");
            fixture.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "High partial failure cannot activate");
            fixture.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
