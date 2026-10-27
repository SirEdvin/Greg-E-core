package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.BraveryBookIngredient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Runs only after the existing prefix-guarded disposable-world UI checks. */
final class IntentionClientChecks {
    private static final BlockPos CONTROLLER = new BlockPos(8192, 80, 8192);
    private static final BlockPos REAR = CONTROLLER.south(4);
    static final List<String> failures = new CopyOnWriteArrayList<>();
    static volatile boolean recipeReloaded, resourcesReloaded;
    static boolean reconnected;
    private static int frame, stage, postReloadFrame, ticks;

    static boolean tick() {
        var client = Minecraft.getInstance();
        frame++;
        try {
            if (++ticks > 2400) throw new AssertionError("Timed out verifying intention reload/reconnect");
            if ((stage == 1 && frame == 100 || stage == 0 && (frame == 40 || frame == 150 || frame == 190))
                    && !formedOnClient(client, stage == 0 && frame < 190)) {
                if (ticks % 100 == 0) {
                    var part = MetaMachine.getMachine(client.level, REAR);
                    System.out.println("INTENTION_WAIT_FOR_FORMED_SYNC frame=" + frame + " part=" + (part == null ? "missing"
                            : part.getDefinition().getId() + " render=" + part.getRenderState()
                                    + " attached=" + ((IntentionHatchPartMachine) part).hasController(CONTROLLER)));
                    client.getSingleplayerServer().execute(() -> {
                        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CONTROLLER);
                        var serverPart = MetaMachine.getMachine(level, REAR);
                        System.out.println("INTENTION_SERVER_FORMATION formed=" + machine.isFormed() + " tier=" + machine.getCasingTier()
                                + " error=" + machine.getMultiblockState().error + " part=" + serverPart.getRenderState()
                                + " attached=" + ((IntentionHatchPartMachine) serverPart).hasController(CONTROLLER)
                                + " syncing=" + serverPart.getHolder().isAsyncSyncing()
                                + " dirty=" + serverPart.getHolder().getRootStorage().hasDirtySyncFields()
                                + " syncPeriod=" + com.lowdragmc.lowdraglib.async.AsyncThreadData.getOrCreate(level).getPeriodID());
                    });
                }
                frame--;
                return false;
            }
            if (stage == 1) {
                if (frame < 120) return false;
                recipes(client);
                models(client, false);
                screenshot(client, "intention-after-reconnect.png");
                reconnected = true;
                return true;
            }
            if (frame == 1) {
                client.setScreen(null);
                client.getSingleplayerServer().execute(() -> {
                    try {
                        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                        for (int i = 0; i < 2; i++) {
                            var pos = REAR.east(4 + i * 2);
                            level.setBlockAndUpdate(pos, (i == 0 ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).defaultBlockState());
                            MetaMachine.getMachine(level, pos).setFrontFacing(Direction.SOUTH);
                        }
                        level.setBlockAndUpdate(REAR.above(6), Blocks.SEA_LANTERN.defaultBlockState());
                        install(client, true);
                    } catch (Throwable error) { failures.add("Intention setup: " + error); }
                });
            }
            if (frame == 40) { recipes(client); models(client, true); }
            if (frame == 45 && !WorldEaterCasingJeiChecks.showIntentionRecipe(true)) throw new AssertionError("Safety JEI recipe unavailable");
            if (frame == 65) screenshot(client, "intention-safety-recipe.png");
            if (frame == 66 && !WorldEaterCasingJeiChecks.showIntentionRecipe(false)) throw new AssertionError("Bravery JEI recipe unavailable");
            if (frame == 90) screenshot(client, "intention-bravery-recipe.png");
            if (frame == 91) {
                var book = BraveryBookIngredient.exampleBook();
                var tooltip = book.getTooltipLines(client.player, TooltipFlag.Default.NORMAL).stream().map(Component::getString).toList();
                require(tooltip.stream().anyMatch(t -> t.contains(BraveryBookIngredient.ACKNOWLEDGEMENT))
                        && tooltip.stream().anyMatch(t -> t.contains("Any title/author")), "Book tooltip explains content, not fixed authorship");
                client.setScreen(new Screen(Component.literal("Bravery acknowledgement tooltip")) {
                    @Override public boolean isPauseScreen() { return false; }
                    @Override public void render(GuiGraphics graphics, int x, int y, float partialTick) {
                        renderBackground(graphics);
                        graphics.renderTooltip(font, book, 20, 50);
                        graphics.renderItem(new ItemStack(IntentionHatchPartMachine.SAFETY.getItem()), 30, 150);
                        graphics.renderItem(new ItemStack(IntentionHatchPartMachine.BRAVERY.getItem()), 60, 150);
                    }
                });
            }
            if (frame == 110) screenshot(client, "intention-acknowledgement-tooltip.png");
            if (frame == 111) {
                client.setScreen(null);
                client.getSingleplayerServer().execute(() -> {
                    var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                    player.connection.teleport(REAR.getX() + 1.5, REAR.getY() + 1, REAR.getZ() + 5.5, 180, 12);
                });
            }
            if (frame == 150) { models(client, true); screenshot(client, "intention-safety-formed.png"); }
            if (frame == 151) client.getSingleplayerServer().execute(() -> {
                try { install(client, false); } catch (Throwable error) { failures.add("Swap hatch: " + error); }
            });
            if (frame == 190) models(client, false);
            // Chunk meshes are rebuilt asynchronously after the model state arrives.
            if (frame == 210) { models(client, false); screenshot(client, "intention-bravery-formed.png"); }
            if (frame == 211) {
                client.reloadResourcePacks().whenComplete((ignored, error) -> {
                    if (error != null) failures.add("Resource reload: " + error);
                    resourcesReloaded = true;
                });
                var server = client.getSingleplayerServer();
                server.execute(() -> server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, error) -> {
                    if (error != null) failures.add("Server recipe reload: " + error);
                    recipeReloaded = true;
                }));
            }
            if (frame > 250 && recipeReloaded && resourcesReloaded) {
                if (postReloadFrame == 0) postReloadFrame = frame;
                if (frame - postReloadFrame < 40) return false;
                recipes(client); models(client, false);
                var world = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
                require(world.startsWith("albina-world-eater-verification-"), "Reconnect only to disposable verification world");
                stage = 1; frame = 0;
                client.level.disconnect();
                client.clearLevel(new TitleScreen());
                client.createWorldOpenFlows().loadLevel(new TitleScreen(), world);
            }
        } catch (Exception | AssertionError error) {
            failures.add("Intention client: " + error);
            return true;
        }
        return false;
    }

    private static void install(Minecraft client, boolean safety) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CONTROLLER);
        machine.onStructureInvalid();
        level.setBlockAndUpdate(REAR, (safety ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).defaultBlockState());
        MetaMachine.getMachine(level, REAR).setFrontFacing(Direction.SOUTH);
        // Let native reformation run after the new part's onLoad and sync its attachments.
        machine.onPartUnload();
    }

    private static void recipes(Minecraft client) {
        for (boolean safety : new boolean[]{true, false}) {
            var recipe = IntentionRecipeTests.recipe(client.level, safety);
            var inputs = recipe.getInputContents(ItemRecipeCapability.CAP);
            require(inputs.size() == (safety ? 16 : 1) && recipe.duration == 1200
                    && RecipeHelper.getRealEUt(recipe).getTotalEU() == (safety ? 30720 : 0), "Synchronized input count/duration/power");
            if (!safety) require(!recipe.hasTick() && recipe.data.getString("required_state").equals("ACTIVE")
                    && recipe.data.getInt("required_casing_tier") == 2, "Synchronized power-free ACTIVE tier-2 gate");
            for (int i = 0; i < inputs.size(); i++) {
                var ingredient = ItemRecipeCapability.CAP.of(inputs.get(i).content);
                require(ingredient.getItems()[0].getCount() == (safety ? 8 : 1), "Synchronized counted inputs");
                if (!safety) require(ingredient.test(IntentionRecipeTests.book(i + 15))
                        && !ingredient.test(IntentionRecipeTests.book(i, "\"wrong\"")), "Synchronized custom acknowledgement predicate");
                else require(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(ingredient.getItems()[0].getItem()).toString()
                        .equals("gtceu:luv_" + IntentionRecipeTests.SAFETY_INPUTS[i]), "Synchronized exact machine order");
            }
            var output = recipe.getOutputContents(ItemRecipeCapability.CAP);
            var stack = ItemRecipeCapability.CAP.of(output.get(0).content).getItems()[0];
            require(output.size() == 1 && stack.getCount() == 1 && stack.is((safety ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).getItem()), "Synchronized output");
        }
    }

    private static void models(Minecraft client, boolean safety) {
        for (int i = 0; i < 3; i++) {
            var pos = i == 0 ? REAR : REAR.east(2 + i * 2);
            String variant = i == 0 ? (safety ? "safety" : "bravery") : (i == 1 ? "safety" : "bravery");
            var sprites = new ArrayList<String>();
            for (Direction face : Direction.values()) WorldEaterCasingConnectionChecks.quads(client, pos, face)
                    .forEach(q -> sprites.add(q.getSprite().contents().name().toString()));
            WorldEaterCasingConnectionChecks.quads(client, pos, null).forEach(q -> sprites.add(q.getSprite().contents().name().toString()));
            require(sprites.stream().anyMatch(s -> s.endsWith("overlay/intention/" + variant)), "Distinct " + variant + " overlay at " + pos);
            require(sprites.stream().noneMatch(s -> s.contains("missingno")), "No missing hatch texture");
            if (i == 0) require(sprites.stream().anyMatch(s -> s.contains("world_eater/black")), "Formed hatch uses black casing: " + sprites);
            else require(sprites.stream().anyMatch(s -> s.contains("casings/voltage/luv")), "Standalone native hull");
        }
        for (var definition : new com.gregtechceu.gtceu.api.machine.MachineDefinition[]{IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY})
            require(client.getItemRenderer().getModel(new ItemStack(definition.getItem()), client.level, client.player, 0)
                    != client.getModelManager().getMissingModel(), "Hatch item model resolves");
    }

    private static boolean formedOnClient(Minecraft client, boolean safety) {
        return MetaMachine.getMachine(client.level, REAR) instanceof IntentionHatchPartMachine hatch
                && hatch.getIntention() == (safety ? IntentionHatchPartMachine.Intention.SAFETY : IntentionHatchPartMachine.Intention.BRAVERY)
                && hatch.hasController(CONTROLLER) && hatch.replacePartModelWhenFormed();
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }
    private static void screenshot(Minecraft client, String name) {
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), name, client.getMainRenderTarget(), ignored -> {});
    }
}
