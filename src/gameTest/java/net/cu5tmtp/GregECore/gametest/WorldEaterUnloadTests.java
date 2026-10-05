package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = GregECore.MOD_ID)
public class WorldEaterUnloadTests {
    private static final java.util.Set<Long> unloaded = java.util.concurrent.ConcurrentHashMap.newKeySet();
    static {
        if (Boolean.getBoolean("forge.gameTestServer")) {
            try {
                var report = java.nio.file.Path.of(System.getenv("TMPDIR"), "world-eater-gametest.xml");
                java.nio.file.Files.deleteIfExists(report);
                net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(
                        new net.minecraft.gametest.framework.JUnitLikeTestReporter(report.toFile()));
            } catch (java.io.IOException | javax.xml.parsers.ParserConfigurationException exception) {
                throw new ExceptionInInitializerError(exception);
            }
        }
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onChunkUnload(net.minecraftforge.event.level.ChunkEvent.Unload event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD))
            unloaded.add(event.getChunk().getPos().toLong());
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void nativeControllerEvictionAndReload(GameTestHelper helper) {
        var fixtures = new WorldEaterRunTests.Fixture[4];
        for (int i = 0; i < fixtures.length; i++) fixtures[i] = WorldEaterRunTests.fixture(helper, 56000 + i * 1000);
        helper.runAfterDelay(4, () -> {
            for (int i = 0; i < fixtures.length; i++) {
                var fixture = fixtures[i];
                WorldEaterRunTests.form(helper, fixture);
                fixture.logic().setWorkingEnabled(true);
                fixture.logic().setupRecipe(WorldEaterRunTests.recipe());
                WorldEaterRunTests.blockOutput(fixture);
                if (i == 0) {
                    int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
                    helper.getLevel().getChunk(cx - 4, cz - 4);
                    helper.getLevel().setBlockAndUpdate(new net.minecraft.core.BlockPos((cx - 4) * 16,
                            helper.getLevel().getMinBuildHeight(), (cz - 4) * 16),
                            com.gregtechceu.gtceu.common.data.GTMachines.MACERATOR[com.gregtechceu.gtceu.api.GTValues.LV].defaultBlockState());
                }
                int ticks = switch (i) { case 0 -> 5; case 1 -> 7; case 2 -> 20; default -> 101; };
                for (int tick = 0; tick < ticks; tick++) fixture.logic().serverTick();
                fixture.logic().setWorkingEnabled(false);
                int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
                for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) helper.getLevel().setChunkForced(x, z, false);
            }
            waitForEviction(helper, fixtures, 0);
        });
    }

    private static void waitForEviction(GameTestHelper helper, WorldEaterRunTests.Fixture[] fixtures, int waited) {
        boolean allGone = true;
        for (var fixture : fixtures) {
            var pos = fixture.controller().getPos();
            allGone &= unloaded.contains(net.minecraft.world.level.ChunkPos.asLong(pos.getX() >> 4, pos.getZ() >> 4))
                    && helper.getLevel().getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) == null;
        }
        if (!allGone) {
            helper.assertTrue(waited < 1000, "Controller chunks must really evict");
            helper.runAfterDelay(1, () -> waitForEviction(helper, fixtures, waited + 1));
            return;
        }
        for (var fixture : fixtures) {
            var pos = fixture.controller().getPos();
            int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
            for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) helper.getLevel().setChunkForced(x, z, true);
            helper.getLevel().getChunk(cx, cz);
        }
        helper.runAfterDelay(5, () -> {
            for (var fixture : fixtures) {
                var reloaded = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), fixture.controller().getPos());
                helper.assertTrue(reloaded != null && reloaded != fixture.controller(), "Fresh controller instance after real eviction");
                var logic = (WorldEaterTamerRecipeLogic) reloaded.getRecipeLogic();
                helper.assertTrue(logic.getProgress() == fixture.logic().getProgress() && logic.getElapsedDelays() == fixture.logic().getElapsedDelays(), "No repeated/skipped section after eviction");
                helper.assertTrue(logic.getRetryAttempts() == fixture.logic().getRetryAttempts() && logic.getDelayRemaining() == fixture.logic().getDelayRemaining(), "Retry allowance and delay retained");
                helper.assertTrue(logic.getCounts().equals(fixture.logic().getCounts()), "Counts retained");
                helper.assertTrue(logic.hasPendingReport() == fixture.logic().hasPendingReport()
                        && ItemStack.matches(logic.getPendingBook(), fixture.logic().getPendingBook()), "Exact pending output retained");
                helper.assertTrue(reloaded.checkPatternWithLock(), "Reloaded shell forms");
                reloaded.onStructureFormed();
                var inputPos = fixture.input().getPos();
                var input = (com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), inputPos);
                helper.assertTrue(input.getInventory().getStackInSlot(0).getCount() == 1, "No second sensor on reload");
                int progressBefore = logic.getProgress();
                if (!logic.hasPendingReport() && logic.getElapsedDelays() == 0) {
                    int cx = reloaded.getPos().getX() >> 4, cz = reloaded.getPos().getZ() >> 4;
                    helper.getLevel().getChunk(cx - 4, cz - 4);
                }
                boolean completed = logic.hasPendingReport();
                logic.setWorkingEnabled(true);
                logic.serverTick();
                helper.assertTrue(logic.getProgress() == progressBefore + (completed ? 0 : 1), "Resumes next scheduled tick after real reload");
                helper.assertTrue(input.getInventory().getStackInSlot(0).getCount() == 1, "Resumption consumes no additional input");
                if (!completed && logic.getElapsedDelays() == 0)
                    helper.assertTrue(logic.getCounts().values().stream().mapToLong(Long::longValue).sum() == 1, "Accumulated count neither lost nor repeated across real reload");
                logic.setWorkingEnabled(false);
            }
            System.out.println("WORLD_EATER_VERIFIED nativeEvictionStages=4 evictionWaitTicks=" + waited);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 1200)
    public static void enabledScanDoesNotRetainTargetChunk(GameTestHelper helper) {
        var fixture = WorldEaterRunTests.fixture(helper, 62000);
        helper.runAfterDelay(4, () -> {
            WorldEaterRunTests.form(helper, fixture);
            int cx = fixture.controller().getPos().getX() >> 4, cz = fixture.controller().getPos().getZ() >> 4;
            helper.getLevel().setChunkForced(cx - 4, cz - 4, true);
            helper.getLevel().getChunk(cx - 4, cz - 4);
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().setupRecipe(WorldEaterRunTests.recipe());
            WorldEaterRunTests.blockOutput(fixture);
            helper.getLevel().setChunkForced(cx - 4, cz - 4, false);
            // Forced controller tickets retain a generation halo beyond the scan square.
            // Release the fixture tickets too, then exercise non-loading reads until native eviction.
            for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++)
                helper.getLevel().setChunkForced(x, z, false);
            waitForTarget(helper, fixture, cx - 4, cz - 4, 0);
        });
    }

    private static void waitForTarget(GameTestHelper helper, WorldEaterRunTests.Fixture fixture, int x, int z, int waited) {
        fixture.logic().serverTick();
        if (!unloaded.contains(net.minecraft.world.level.ChunkPos.asLong(x, z))
                || helper.getLevel().getChunkSource().getChunkNow(x, z) != null) {
            helper.assertTrue(waited < 1000, "Enabled scanning must not prevent target eviction");
            helper.runAfterDelay(1, () -> waitForTarget(helper, fixture, x, z, waited + 1));
            return;
        }
        for (int tick = 0; tick < 101 && !fixture.logic().hasPendingReport(); tick++) fixture.logic().serverTick();
        helper.assertTrue(fixture.logic().hasPendingReport(), "Unavailable target produces early failure");
        helper.assertTrue(helper.getLevel().getChunkSource().getChunkNow(x, z) == null, "Retries never reload the evicted target");
        helper.assertTrue(!helper.getLevel().getForcedChunks().contains(net.minecraft.world.level.ChunkPos.asLong(x, z)), "No forced scan ticket");
        fixture.logic().setWorkingEnabled(false);
        System.out.println("WORLD_EATER_VERIFIED targetEvictionWaitTicks=" + waited + " targetRemainedUnloaded=true");
        helper.succeed();
    }
}
