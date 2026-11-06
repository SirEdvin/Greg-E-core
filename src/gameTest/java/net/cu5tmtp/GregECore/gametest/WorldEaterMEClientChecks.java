package net.cu5tmtp.GregECore.gametest;

import appeng.core.definitions.AEBlocks;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.MEDemandHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Actual native ME hatch screens/models; only invoked in the isolated client fixture. */
final class WorldEaterMEClientChecks {
    static final List<String> failures = new CopyOnWriteArrayList<>();
    private static final Direction[] FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private static int phase, frame, ticks;
    private static boolean done, reconnected;
    private static volatile boolean reloaded;
    private static BlockPos hatchPos;
    private static ItemStack expected = ItemStack.EMPTY;

    static boolean tick() {
        if (done) return true;
        var client = Minecraft.getInstance();
        try {
            if (++ticks > 2600) throw new AssertionError("ME client timeout at phase " + phase);
            frame++;
            if (phase < 4) {
                if (frame == 1) {
                    client.setScreen(null);
                    int orientation = phase;
                    server(client, () -> {
                        WorldEaterDemandClientChecks.build(client, FACINGS[orientation]);
                        var f = WorldEaterDemandClientChecks.fixture(client);
                        hatchPos = f.controller().getPos().relative(FACINGS[orientation].getClockWise());
                        var level = f.controller().getLevel();
                        level.setBlockAndUpdate(hatchPos, MEDemandHatchPartMachine.HATCH.defaultBlockState());
                        var hatch = (MEDemandHatchPartMachine) MetaMachine.getMachine(level, hatchPos);
                        hatch.setFrontFacing(FACINGS[orientation]);
                        if (orientation != 0) level.setBlockAndUpdate(hatchPos.relative(FACINGS[orientation]), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
                    });
                }
                if (frame == 40) server(client, () -> {
                    var f = WorldEaterDemandClientChecks.fixture(client);
                    WorldEaterDemandTests.state(f.logic(), "PREPARED");
                    WorldEaterDemandClientChecks.start(client, false);
                    if (phase == 2) f.logic().interruptRecipe();
                    MEDemandHatchPartMachine.refresh(f.controller());
                    ((MEDemandHatchPartMachine) MetaMachine.getMachine(f.controller().getLevel(), hatchPos)).getHolder().getRootStorage().markAllDirty();
                });
                if (frame == 90) {
                    var hatch = hatch(client);
                    require(hatch.isFormed() && hatch.getRenderState().getValue(GTMachineModelProperties.IS_FORMED), "Formed native hatch model: " + FACINGS[phase]);
                    require(hatch.isOnline() == (phase != 0), "Actual offline/online connection display");
                    require(hatch.getRequest().isEmpty() == (phase == 2), "Read-only idle/pending request");
                    require(client.getItemRenderer().getModel(new ItemStack(MEDemandHatchPartMachine.HATCH.getItem()), client.level, client.player, 0)
                            != client.getModelManager().getMissingModel(), "Native hatch item model exists");
                    capture(client, "me-formed-" + FACINGS[phase].getName() + ".png");
                    server(client, () -> open(client));
                }
                if (frame < 125) return false;
                require(client.screen instanceof ModularUIGuiContainer, "Actual read-only hatch UI opens");
                capture(client, "me-ui-" + FACINGS[phase].getName() + ".png");
                expected = hatch(client).getRequest(); next(); return false;
            }
            if (phase == 4) {
                if (frame == 1) require(WorldEaterCasingJeiChecks.showMEDemandRecipe(), "Exact shapeless ME hatch JEI recipe");
                if (frame == 30) {
                    var recipe = client.level.getRecipeManager().byKey(GregECore.id("me_demand_hatch")).orElseThrow();
                    require(recipe.getIngredients().size() == 5, "Synchronized client recipe has one bus and four ingots");
                    capture(client, "me-four-ingot-recipe.png");
                    require(WorldEaterCasingJeiChecks.showTamerStructure(), "Native structure preview opens");
                }
                if (frame < 60) return false;
                if (frame == 60) verifyPreview();
                if (frame < 80) return false;
                capture(client, "me-structure-preview.png"); client.setScreen(null); next(); return false;
            }
            if (phase == 5) {
                if (frame == 1) client.reloadResourcePacks().whenComplete((unused, error) -> {
                    if (error != null) failures.add("ME resource reload: " + error);
                    reloaded = true;
                });
                if (!reloaded || client.getOverlay() != null || frame < 100) return false;
                require(ItemStack.matches(hatch(client).getRequest(), expected), "Resource reload keeps authoritative request");
                capture(client, "me-resource-reload.png");
                var world = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
                require(world.startsWith("albina-world-eater-verification-"), "Disposable verification world only");
                next(); client.level.disconnect(); client.clearLevel(new TitleScreen());
                client.createWorldOpenFlows().loadLevel(new TitleScreen(), world); return false;
            }
            if (phase == 6) {
                if (frame < 120) return false;
                require(ItemStack.matches(hatch(client).getRequest(), expected) && hatch(client).isFormed(), "Reconnect reconstructs request and appearance");
                capture(client, "me-reconnect.png"); reconnected = true;
                server(client, () -> {
                    var f = WorldEaterDemandClientChecks.fixture(client);
                    f.logic().setWorkingEnabled(true); WorldEaterDemandTests.pay(f); f.logic().setWorkingEnabled(false);
                    open(client);
                });
                next(); return false;
            }
            if (phase == 7) {
                if (frame < 90) return false;
                require(hatch(client).getRequest().isEmpty() && client.screen instanceof ModularUIGuiContainer, "Payment clears actual synchronized hatch screen");
                capture(client, "me-cleared-ui.png"); client.setScreen(null); reloaded = false;
                client.reloadResourcePacks().whenComplete((unused, error) -> {
                    if (error != null) failures.add("Cleared ME resource reload: " + error);
                    reloaded = true;
                });
                next(); return false;
            }
            if (phase == 8) {
                if (!reloaded || client.getOverlay() != null || frame < 100) return false;
                require(hatch(client).getRequest().isEmpty(), "Resource reload cannot revive settled request");
                capture(client, "me-cleared-resource-reload.png");
                var world = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
                require(world.startsWith("albina-world-eater-verification-"), "Disposable verification world only");
                next(); client.level.disconnect(); client.clearLevel(new TitleScreen());
                client.createWorldOpenFlows().loadLevel(new TitleScreen(), world); return false;
            }
            if (frame < 120) return false;
            require(hatch(client).getRequest().isEmpty(), "Reconnect cannot resurrect settled demand from saved hatch configuration");
            capture(client, "me-cleared-reconnect.png");
            finish();
        } catch (Exception | AssertionError error) {
            failures.add("ME client phase " + phase + ": " + error);
            finish();
        }
        return done;
    }

    private static void next() { phase++; frame = 0; }

    @SuppressWarnings("unchecked")
    private static void verifyPreview() throws ReflectiveOperationException {
        var preview = WorldEaterCasingJeiChecks.structurePreview;
        var field = com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget.MBPattern.class.getDeclaredField("predicateMap");
        field.setAccessible(true);
        var predicates = (Map<BlockPos, com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate>) field.get(preview.patterns[0]);
        var positions = predicates.entrySet().stream().filter(e -> java.util.stream.Stream.concat(e.getValue().common.stream(), e.getValue().limited.stream())
                .anyMatch(p -> p.getCandidates().stream().anyMatch(s -> s.is(MEDemandHatchPartMachine.HATCH.getItem())))).toList();
        require(positions.size() == 43, "ME hatch preview is available at every ordinary frame position, not controller/interior");
        var sceneField = com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget.class.getDeclaredField("sceneWidget");
        sceneField.setAccessible(true);
        ((com.lowdragmc.lowdraglib.gui.widget.SceneWidget) sceneField.get(preview)).getOnSelected().accept(positions.get(0).getKey(), Direction.NORTH);
    }

    private static MEDemandHatchPartMachine hatch(Minecraft client) { return (MEDemandHatchPartMachine) MetaMachine.getMachine(client.level, hatchPos); }
    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void server(Minecraft client, Runnable action) {
        client.getSingleplayerServer().execute(() -> {
            try { action.run(); } catch (Exception | AssertionError error) { failures.add("ME server fixture: " + error); }
        });
    }
    private static void open(Minecraft client) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
        level.getBlockState(hatchPos).use(level, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(hatchPos), Direction.UP, hatchPos, false));
    }
    private static void capture(Minecraft client, String name) {
        require(client.getOverlay() == null, "Capture has no loading overlay");
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), name, client.getMainRenderTarget(), ignored -> {});
    }
    private static void finish() {
        done = true;
        try {
            Files.createDirectories(Path.of(System.getenv("TMPDIR"), "world-eater-client"));
            Files.writeString(Path.of(System.getenv("TMPDIR"), "world-eater-client", "me-result.json"), new com.google.gson.Gson().toJson(
                    Map.of("passed", failures.isEmpty(), "failures", failures, "phase", phase, "resourceReload", reloaded, "reconnect", reconnected, "clearedReconnect", phase == 9)));
        } catch (java.io.IOException e) { throw new IllegalStateException(e); }
    }
}
