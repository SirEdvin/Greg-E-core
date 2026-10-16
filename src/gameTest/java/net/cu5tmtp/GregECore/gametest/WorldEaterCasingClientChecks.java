package net.cu5tmtp.GregECore.gametest;

import com.google.gson.Gson;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Explicit opt-in disposable-world gameplay and rendered asset acceptance. */
@Mod.EventBusSubscriber(modid = GregECore.MOD_ID, value = Dist.CLIENT)
public final class WorldEaterCasingClientChecks {
    private static final BlockPos ORIGIN = new BlockPos(12288, 80, 12288);
    private static final BlockPos CONTROLLER = ORIGIN.south(6);
    private static final Path OUTPUT = Path.of(System.getenv("TMPDIR"), "world-eater-casings-client");
    private static final List<String> failures = new CopyOnWriteArrayList<>();
    private static int wait, frame;
    private static boolean initialized, modelsChecked, reloadRequested, gameplayRequested, postReloadChecked;
    private static volatile boolean ready, gameplayDone, reloaded;
    private static boolean finished, perimeterPreviewRequested;
    private static int screenshots;

    static void tick() throws Exception {
        Minecraft client = Minecraft.getInstance();
        if (finished) return;
        if (client.level == null || client.player == null) {
            if (++wait % 100 == 0 && client.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen) {
                for (var child : client.screen.children()) {
                    if (child instanceof net.minecraft.client.gui.components.Button button
                            && button.getMessage().getString().equals("Proceed")) { button.onPress(); break; }
                }
            }
            if (wait > 2400) { failures.add("Timed out opening isolated client world"); finish(client); }
            return;
        }
        if (!initialized) {
            initialized = true;
            Files.createDirectories(OUTPUT);
            client.setScreen(null);
            client.getSingleplayerServer().execute(() -> {
                try { setup(client); ready = true; }
                catch (Throwable error) { failures.add("Setup: " + error); gameplayDone = ready = true; }
            });
            return;
        }
        if (!ready) return;
        frame++;
        if (frame == 60 && !gameplayRequested) {
            gameplayRequested = true;
            client.getSingleplayerServer().execute(() -> {
                try { gameplay(client); }
                catch (Throwable error) { failures.add("Gameplay: " + error); error.printStackTrace(); }
                finally { gameplayDone = true; }
            });
        }
        if (frame >= 100 && gameplayDone && !modelsChecked) {
            client.setScreen(new ItemSheet());
            inspectModels(client);
            modelsChecked = true;
        }
        if (frame == 340) client.getSingleplayerServer().execute(() -> lights(client, false));
        if (frame == 600 && !reloadRequested) {
            reloadRequested = true;
            client.reloadResourcePacks().whenComplete((ignored, error) -> {
                if (error != null) failures.add("Resource reload: " + error);
                reloaded = true;
            });
        }
        if (frame >= 680 && reloaded && gameplayDone && !postReloadChecked) {
            postReloadChecked = true;
            inspectModels(client);
            client.getSingleplayerServer().execute(() -> {
                var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                for (int i = 0; i < 4; i++) {
                    if (level.getBrightness(LightLayer.BLOCK, ORIGIN.east(-3 + 2 * i).above(2).north()) != 0)
                        failures.add("Casing introduced world light in sealed dark room");
                }
            });
        }
        if (frame >= 780 && postReloadChecked && !perimeterPreviewRequested) {
            perimeterPreviewRequested = true;
            client.getSingleplayerServer().execute(() -> {
                var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                var p = WorldEaterCasingConnectionChecks.panel(2);
                for (int x = 0; x < 4; x++) for (int y = 0; y < 3; y++)
                    level.setBlockAndUpdate(p.offset(x, y, 0), y < 2 ? WorldEaterCasingTests.casings()[2].defaultBlockState() : Blocks.AIR.defaultBlockState());
                lights(client, true);
                for (int x : new int[]{1, 3, 5, 7}) level.setBlockAndUpdate(ORIGIN.offset(x, 4, -1), Blocks.SEA_LANTERN.defaultBlockState());
                var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                player.teleportTo(ORIGIN.getX() + 4, ORIGIN.getY() + .6, ORIGIN.getZ() - 3.5);
                player.setYRot(0); player.setXRot(4);
            });
        }
        if (frame == 920 && !WorldEaterCasingJeiChecks.showPurpleRecipe()) failures.add("JEI runtime unavailable");
        if (frame >= 980 && postReloadChecked) finish(client);
        if (frame > 1800) { failures.add("Timed out completing client acceptance"); finish(client); }
    }

    @SubscribeEvent
    public static void render(TickEvent.RenderTickEvent event) {
        if (!Boolean.getBoolean("gregecore.worldEaterCasingVerification") || event.phase != TickEvent.Phase.END
                || !modelsChecked || finished || frame < 110 || frame % (frame < 600 ? 2 : 10) != 0) return;
        // Once per client tick, after the framebuffer is actually rendered.
        String name = String.format("%s-%04d.png", frame >= 920 ? "recipe" : frame >= 800 ? "perimeter" : frame < 340 ? "lit" : frame < 600 ? "dark" : "reloaded", frame);
        if (Files.exists(OUTPUT.resolve("screenshots").resolve(name))) return;
        Screenshot.grab(OUTPUT.toFile(), name, Minecraft.getInstance().getMainRenderTarget(), message -> {});
        screenshots++;
    }

    private static void setup(Minecraft client) {
        var server = client.getSingleplayerServer();
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        server.getPlayerList().setViewDistance(3);
        for (int x = -12; x <= 12; x++) for (int z = -12; z <= 14; z++) for (int y = -1; y <= 8; y++) {
            boolean wall = x == -12 || x == 12 || z == -12 || z == 14 || y == -1 || y == 8;
            level.setBlockAndUpdate(ORIGIN.offset(x, y, z), wall ? Blocks.GRAY_CONCRETE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        var player = server.getPlayerList().getPlayer(client.player.getUUID());
        player.setGameMode(GameType.CREATIVE);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(ORIGIN.getX() + .5, ORIGIN.getY() + 2, ORIGIN.getZ() - 9.5);
        player.setYRot(0); player.setXRot(9);
        WorldEaterCasingConnectionChecks.setup(level);
        for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) {
            int borders = (back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0);
            if (borders >= 2) level.setBlockAndUpdate(CONTROLLER.south(back).east(x).above(y), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
        }
        level.setBlockAndUpdate(CONTROLLER, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
        level.setBlockAndUpdate(CONTROLLER.west(2), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].defaultBlockState());
        level.setBlockAndUpdate(CONTROLLER.east(2), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].defaultBlockState());
        level.setBlockAndUpdate(CONTROLLER.west(1), GCYMMachines.PARALLEL_HATCH[GTValues.IV].defaultBlockState());
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CONTROLLER);
        machine.setFrontFacing(Direction.NORTH);
        machine.getRecipeLogic().setWorkingEnabled(false);
        lights(client, true);
    }

    private static void lights(Minecraft client, boolean on) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        for (int x : new int[]{-5, 5}) for (int z : new int[]{-3, 5, 11})
            level.setBlockAndUpdate(ORIGIN.offset(x, 6, z), (on ? Blocks.SEA_LANTERN : Blocks.AIR).defaultBlockState());
    }

    private static void gameplay(Minecraft client) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CONTROLLER);
        var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
        require(machine.checkPatternWithLock(), "Native black frame forms with hatch");
        machine.onStructureFormed();
        var input = (ItemBusPartMachine) MetaMachine.getMachine(level, CONTROLLER.west(2));
        var output = (ItemBusPartMachine) MetaMachine.getMachine(level, CONTROLLER.east(2));
        var hatch = (ParallelHatchPartMachine) MetaMachine.getMachine(level, CONTROLLER.west(1));
        hatch.setCurrentParallel(4);
        int cx = CONTROLLER.getX() >> 4, cz = CONTROLLER.getZ() >> 4;
        for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) level.getChunk(x, z);
        var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
        for (int i = 0; i < 500; i++) level.setBlockAndUpdate(new BlockPos(domain.x(i), domain.y(i), domain.z(i)), GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
        input.getInventory().setStackInSlot(0, GTItems.SENSOR_IV.asStack(4));
        logic.setWorkingEnabled(true);
        logic.findAndHandleRecipe();
        require(logic.getLastRecipe() != null && logic.getLastRecipe().parallels == 1 && logic.getDuration() == 2400, "Client hatch leaves survey single");
        for (int i = 0; i < 2400; i++) logic.serverTick();
        require(logic.getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE, "Real survey activates at 500 machines");
        require(input.getInventory().getStackInSlot(0).getCount() == 3 && output.getInventory().getStackInSlot(0).is(Items.WRITTEN_BOOK), "One sensor and one survey book");
        input.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        for (int color = 0; color < 3; color++) {
            logic.resetRecipeLogic();
            hatch.setCurrentParallel(color == 2 ? 2 : 1);
            int parallels = color == 2 ? 2 : 1;
            input.getInventory().setStackInSlot(0, new ItemStack(GreggyBlocks.WORLD_EATER_TAMER_CASING.asItem(), parallels));
            input.getInventory().setStackInSlot(1, new ItemStack(WorldEaterCasingTests.TERRACOTTA[color], 16 * parallels));
            logic.findAndHandleRecipe();
            require(logic.getLastRecipe() != null && logic.getLastRecipe().parallels == parallels && logic.getDuration() == 100, "Client conversion starts: " + color);
            for (int i = 0; i < 100; i++) logic.serverTick();
            var result = output.getInventory().getStackInSlot(0);
            require(result.is(WorldEaterCasingTests.casings()[color + 1].asItem()) && result.getCount() == parallels, "Client paid output: " + color);
            require(input.getInventory().getStackInSlot(0).isEmpty() && input.getInventory().getStackInSlot(1).isEmpty(), "Client exact payment");
            output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        }
        logic.setWorkingEnabled(false);
        System.out.println("WORLD_EATER_CASING_CLIENT_GAMEPLAY_PASSED");
    }

    private static void inspectModels(Minecraft client) {
        for (var block : WorldEaterCasingTests.casings()) {
            var state = block.defaultBlockState();
            var model = client.getBlockRenderer().getBlockModel(state);
            var quads = model.getQuads(state, Direction.NORTH, RandomSource.create());
            if (quads.size() < 2 || quads.stream().anyMatch(q -> q.getSprite().contents().name().getPath().contains("missingno")))
                failures.add("Missing base/emissive casing model: " + block);
            if (quads.stream().noneMatch(q -> !q.isShade() && q.getVertices()[6] == 0x00f000f0))
                failures.add("No fullbright emissive casing detail: " + block);
            var stack = new ItemStack(block.asItem());
            var item = client.getItemRenderer().getModel(stack, client.level, client.player, 0);
            if (item == client.getModelManager().getMissingModel()) failures.add("Missing item model: " + block);
        }
        var state = client.level.getBlockState(CONTROLLER);
        var model = client.getBlockRenderer().getBlockModel(state);
        var quads = model.getQuads(state, Direction.NORTH, RandomSource.create());
        if (quads.stream().noneMatch(q -> q.getSprite().contents().name().getPath().endsWith("black_emissive")))
            failures.add("Controller lacks emissive black casing layer");
        for (var pos : new BlockPos[]{CONTROLLER.west(2), CONTROLLER.east(2), CONTROLLER.west(1)}) {
            var partState = client.level.getBlockState(pos);
            var partModel = client.getBlockRenderer().getBlockModel(partState);
            var data = partModel.getModelData(client.level, pos, partState, net.minecraftforge.client.model.data.ModelData.EMPTY);
            var partQuads = partModel.getQuads(partState, Direction.NORTH, RandomSource.create(), data, null);
            if (partQuads.stream().noneMatch(q -> q.getSprite().contents().name().getPath().endsWith("world_eater/black")
                    || q.getSprite().contents().name().getPath().endsWith("world_eater/black_ctm")))
                failures.add("Formed part lacks black casing appearance: " + pos);
        }
        failures.addAll(WorldEaterCasingConnectionChecks.verify(client));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }

    private static void finish(Minecraft client) throws Exception {
        if (finished) return;
        finished = true;
        Files.createDirectories(OUTPUT);
        Files.writeString(OUTPUT.resolve("result.json"), new Gson().toJson(Map.of("passed", failures.isEmpty(),
                "failures", failures, "gameplayDone", gameplayDone, "resourceReload", reloaded, "screenshots", screenshots)));
        System.out.println("WORLD_EATER_CASINGS_CLIENT_VERIFIED " + failures);
        client.stop();
    }

    private static final class ItemSheet extends Screen {
        ItemSheet() { super(Component.literal("World Eater casing preview")); }
        @Override public boolean isPauseScreen() { return false; }
        @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.fill(4, 4, 264, 55, 0xd0202028);
            graphics.drawString(font, "Native casing items / world preview", 10, 8, 0xffffff);
            graphics.pose().pushPose();
            graphics.pose().scale(2, 2, 1);
            for (int i = 0; i < 4; i++) graphics.renderItem(new ItemStack(WorldEaterCasingTests.casings()[i]), 12 + 30 * i, 12);
            graphics.pose().popPose();
        }
    }
}
