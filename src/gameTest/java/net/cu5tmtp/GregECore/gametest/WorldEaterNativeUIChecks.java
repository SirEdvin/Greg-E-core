package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.cu5tmtp.GregECore.block.GreggyBlocks;

import com.gregtechceu.gtceu.common.data.GTMachines;

import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Native integrated-server interaction and client synchronization fixture. */
public final class WorldEaterNativeUIChecks {
    private static final BlockPos CENTER = new BlockPos(8192, 80, 8192);
    static final List<String> failures = new ArrayList<>();
    private static int frame;
    private static int stage;
    static boolean done;

    static void begin() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(null);
        client.getSingleplayerServer().execute(() -> {
            var server = client.getSingleplayerServer();
            var level = server.getLevel(Level.OVERWORLD);
            server.getPlayerList().setViewDistance(2);
            var player = server.getPlayerList().getPlayer(client.player.getUUID());
            player.setGameMode(GameType.CREATIVE);
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(CENTER.getX() + .5, CENTER.getY(), CENTER.getZ() - 5.5);
            player.setYRot(0); player.setXRot(10);
            for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = -2; y < 5; y++) {
                var pos = CENTER.south(back).east(x).above(y);
                boolean edge = y >= 0 && (back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0) >= 2;
                level.setBlockAndUpdate(pos, edge ? GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState() : Blocks.AIR.defaultBlockState());
            }
            level.setBlockAndUpdate(CENTER, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
            level.setBlockAndUpdate(CENTER.west(2), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].defaultBlockState());
            level.setBlockAndUpdate(CENTER.east(2), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].defaultBlockState());
            var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CENTER);
            machine.setFrontFacing(Direction.NORTH);
            machine.getRecipeLogic().setWorkingEnabled(false);
        });
    }

    static void tick() {
        if (done) return;
        Minecraft client = Minecraft.getInstance();
        frame++;
        if (stage == 3) {
            if (!WorldEaterTierClientChecks.tick()) return;
            failures.addAll(WorldEaterTierClientChecks.failures);
            stage = 4;
            WorldEaterParticleChecks.begin(CENTER);
        }
        if (stage == 4) {
            WorldEaterParticleChecks.tick();
            done = WorldEaterParticleChecks.done;
            return;
        }
        if (frame == 40) client.getSingleplayerServer().execute(() -> {
            var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
            var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CENTER);
            if (!machine.checkPatternWithLock()) { failures.add("Native client fixture did not form"); return; }
            machine.onStructureFormed();
            var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
            level.getBlockState(CENTER).use(level, player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(CENTER), Direction.NORTH, CENTER, false));
        });
        if (frame < 60 || frame % 40 != 20) return;
        if (stage < 3) {
            if (!(client.screen instanceof com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer)) {
                if (frame > 300) { failures.add("Native modular UI did not open: " + (client.screen == null ? "none" : client.screen.getClass().getSimpleName())); done = true; }
                return;
            }
            var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(client.level, CENTER);
            var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
            var expected = stage == 1 ? WorldEaterTamerRecipeLogic.State.ACTIVE : WorldEaterTamerRecipeLogic.State.DORMANT;
            if (logic.getProgressionState() != expected || (stage == 2 && (logic.getRetryAttempts() == 0 || logic.getCurrentChunk() == null))) {
                if (frame > 1200) { failures.add("Native state/retry synchronization failed at stage " + stage
                        + ": " + logic.getCustomProgressLine().getString()); done = true; }
                return;
            }
            if (stage == 2 && !logic.getCurrentChunkLine().getString().contains("X " + logic.getCurrentChunk().x))
                failures.add("Current chunk translation did not resolve on native client");
            screenshot(client, "ui-" + stage + ".png");
            stage++;
            if (stage < 3) client.getSingleplayerServer().execute(() -> {
                var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CENTER);
                var serverLogic = (WorldEaterTamerRecipeLogic) controller.getRecipeLogic();
                CompoundTag tag = new CompoundTag(), custom = new CompoundTag();
                custom.putInt("Version", 1); custom.putString("State", stage == 1 ? "ACTIVE" : "DORMANT");
                tag.put("WorldEaterTamer", custom); serverLogic.loadCustomPersistedData(tag);
                if (stage == 2) {
                    try {
                        // Replay real loader-produced retry persistence, not invented UI state.
                        var snapshot = net.minecraft.nbt.NbtIo.readCompressed(
                                Path.of(System.getenv("TMPDIR"), "world-eater-retry-snapshot.nbt").toFile());
                        ((net.minecraft.world.level.block.entity.BlockEntity) controller.getHolder()).load(snapshot);
                        controller.getRecipeLogic().setWorkingEnabled(false);
                    } catch (java.io.IOException exception) {
                        throw new RuntimeException(exception);
                    }
                }
            });
            else WorldEaterTierClientChecks.begin();
        } else {
            screenshot(client, "controller.png");
            done = true;
        }
    }

    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), name,
                client.getMainRenderTarget(), message -> System.out.println("WORLD_EATER_SCREENSHOT " + message.getString()));
    }
}
