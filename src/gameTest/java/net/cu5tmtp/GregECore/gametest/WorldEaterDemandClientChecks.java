package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender.WorldEaterPartRender;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterDemands;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Server-selected, native client rendering and reconnect checks in the disposable fixture world. */
final class WorldEaterDemandClientChecks {
    static final BlockPos CONTROLLER = new BlockPos(8300, 80, 8192);
    private static final Direction[] FACINGS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    static final List<String> failures = new CopyOnWriteArrayList<>();
    static volatile boolean resourceReloaded;
    static boolean reconnected, done;
    private static int phase, frame, ticks, reloadSettledFrame;
    private static ItemStack expected = ItemStack.EMPTY;

    static boolean tick() {
        if (done) return true;
        var client = Minecraft.getInstance();
        try {
            if (++ticks > 3600) throw new AssertionError("Demand client verification timed out at phase " + phase);
            frame++;
            if (phase < 4) {
                if (frame == 1) { client.setScreen(null); server(client, () -> build(client, FACINGS[phase])); }
                if (frame == 40) server(client, () -> start(client, true));
                if (frame < 90) return false;
                requireDemand(client);
                capture(client, "demand-" + FACINGS[phase].getName() + ".png");
                expected = logic(client).getDemand();
                next(); return false;
            }
            if (phase == 4) {
                if (frame == 1) {
                    require(WorldEaterCasingJeiChecks.showDemandRecipe(true), "Exact preparation JEI recipe");
                    materials(client);
                    for (var recipe : client.level.getRecipeManager().getAllRecipesFor(WorldEaterDemandTests.recipe(client.level, true).recipeType)) {
                        var captions = recipe.recipeType.getDataInfos();
                        for (int i = 0; i < captions.size(); i++) {
                            String caption = captions.get(i).apply(recipe.data);
                            int limit = recipe.recipeType.getRecipeUI().getJEISize().width - (i == captions.size() - 1 ? 26 : 6);
                            require(client.font.width(caption) <= limit, "Caption width " + client.font.width(caption) + "/" + limit + ": " + caption);
                        }
                    }
                }
                if (frame == 20) capture(client, "demand-test-recipe.png");
                if (frame == 21) require(WorldEaterCasingJeiChecks.showDemandRecipe(false), "Exact real JEI recipe");
                if (frame == 40) capture(client, "demand-real-recipe.png");
                if (frame == 41) client.setScreen(new Screen(Component.literal("Native demand materials")) {
                    @Override public boolean isPauseScreen() { return false; }
                    @Override public void render(GuiGraphics graphics, int x, int y, float partial) {
                        renderBackground(graphics);
                        var forms = List.of(WorldEaterDemandTests.token(true), WorldEaterDemandTests.token(false), WorldEaterDemandTests.reward());
                        for (int i = 0; i < forms.size(); i++) {
                            graphics.renderItem(forms.get(i), 24, 35 + i * 35);
                            graphics.drawString(font, forms.get(i).getHoverName(), 50, 39 + i * 35, 0xFFFFFFFF);
                        }
                    }
                });
                if (frame == 60) { capture(client, "demand-materials.png"); client.setScreen(null); next(); }
                return false;
            }
            if (phase == 5) {
                if (frame == 1) client.reloadResourcePacks().whenComplete((unused, error) -> {
                    if (error != null) failures.add("Demand resource reload: " + error);
                    resourceReloaded = true;
                });
                if (!resourceReloaded || client.getOverlay() != null || frame < 80) return false;
                if (reloadSettledFrame == 0) reloadSettledFrame = frame;
                if (frame - reloadSettledFrame < 20) return false;
                requireDemand(client); materials(client); capture(client, "demand-resource-reload.png");
                var world = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).normalize().getFileName().toString();
                require(world.startsWith("albina-world-eater-verification-"), "Only reconnect to isolated world");
                next(); client.level.disconnect(); client.clearLevel(new TitleScreen());
                client.createWorldOpenFlows().loadLevel(new TitleScreen(), world);
                return false;
            }
            if (phase == 6) {
                if (frame < 100) return false;
                requireDemand(client);
                require(ItemStack.matches(expected, logic(client).getDemand()) && logic(client).isSuspend(), "Same paused demand after reconnect");
                reconnected = true; capture(client, "demand-reconnect.png");
                server(client, () -> {
                    var f = fixture(client); f.logic().setWorkingEnabled(true); WorldEaterDemandTests.pay(f);
                    f.logic().setWorkingEnabled(false);
                    var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                    var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                    level.getBlockState(CONTROLLER).use(level, player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(CONTROLLER), f.controller().getFrontFacing(), CONTROLLER, false));
                });
                next(); return false;
            }
            if (phase == 7) {
                if (frame < 80) return false;
                require(logic(client).getDemand().isEmpty() && logic(client).getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Success clears visible demand and synchronizes PREPARED");
                require(client.screen instanceof com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer, "Native PREPARED UI open");
                capture(client, "demand-prepared-ui.png"); client.setScreen(null);
                server(client, () -> start(client, false)); next(); return false;
            }
            if (phase == 8) {
                if (frame < 80) return false;
                if (frame == 80) {
                    requireDemand(client); capture(client, "demand-real-pending.png");
                    server(client, () -> {
                        var f = fixture(client);
                        var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                        level.getBlockState(CONTROLLER).use(level, player, InteractionHand.MAIN_HAND,
                                new BlockHitResult(Vec3.atCenterOf(CONTROLLER), f.controller().getFrontFacing(), CONTROLLER, false));
                    });
                }
                if (frame < 120) return false;
                require(client.screen instanceof com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer, "Native pending demand UI open");
                capture(client, "demand-active-ui.png");
                server(client, () -> {
                    var f = fixture(client); WorldEaterRunTests.blockOutput(f);
                    f.logic().setWorkingEnabled(true); WorldEaterDemandTests.pay(f); f.logic().setWorkingEnabled(false);
                    var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
                    var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
                    level.getBlockState(CONTROLLER).use(level, player, InteractionHand.MAIN_HAND,
                            new BlockHitResult(Vec3.atCenterOf(CONTROLLER), f.controller().getFrontFacing(), CONTROLLER, false));
                });
                next(); return false;
            }
            if (phase == 9) {
                if (frame < 80) return false;
                require(logic(client).getDemand().isEmpty() && logic(client).hasPendingReward(), "Earned output hides demand, reports pending reward");
                capture(client, "demand-earned-ui.png"); client.setScreen(null);
                server(client, () -> {
                    var f = fixture(client); f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
                    f.logic().setWorkingEnabled(true); f.logic().serverTick();
                    require(WorldEaterCasingTests.outputCount(f, WorldEaterDemandTests.reward().getItem()) == 1, "Native client reward delivered once");
                    f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
                    start(client, false); f.logic().interruptRecipe(); f.logic().setWorkingEnabled(false);
                });
                next(); return false;
            }
            if (phase == 10) {
                if (frame < 80) return false;
                require(logic(client).getDemand().isEmpty(), "Cancellation clears world display"); capture(client, "demand-cancelled.png");
                server(client, () -> {
                    start(client, false); var f = fixture(client);
                    WorldEaterDemandTests.field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1);
                    f.logic().setWorkingEnabled(true); f.logic().serverTick(); f.logic().setWorkingEnabled(false);
                });
                next(); return false;
            }
            if (frame < 80) return false;
            require(logic(client).getDemand().isEmpty() && logic(client).getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Timeout clears demand without losing preparation");
            capture(client, "demand-timeout.png"); done = true;
        } catch (Exception | AssertionError error) {
            failures.add("Demand client phase " + phase + ": " + error); done = true;
        }
        return done;
    }

    private static void next() { phase++; frame = 0; }
    private static void server(Minecraft client, Runnable action) {
        client.getSingleplayerServer().execute(() -> {
            try { action.run(); } catch (Exception | AssertionError error) { failures.add("Demand server fixture: " + error); }
        });
    }
    private static WorldEaterTamerRecipeLogic logic(Minecraft client) {
        return (WorldEaterTamerRecipeLogic) ((IncompleteWorldEaterTamer) MetaMachine.getMachine(client.level, CONTROLLER)).getRecipeLogic();
    }
    static WorldEaterRunTests.Fixture fixture(Minecraft client) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(level, CONTROLLER);
        return new WorldEaterRunTests.Fixture(machine,
                (ItemBusPartMachine) MetaMachine.getMachine(level, WorldEaterStructureTests.at(CONTROLLER, machine.getFrontFacing(), 0, 2, 6)),
                (ItemBusPartMachine) MetaMachine.getMachine(level, WorldEaterStructureTests.at(CONTROLLER, machine.getFrontFacing(), 0, 2, 8)));
    }
    static void build(Minecraft client, Direction front) {
        var level = client.getSingleplayerServer().getLevel(Level.OVERWORLD);
        // Clear every orientation's footprint so earlier phases leave no stray parts.
        for (var pos : BlockPos.betweenClosed(CONTROLLER.offset(-15, -8, -15), CONTROLLER.offset(15, 7, 15)))
            level.setBlock(pos, pos.getY() == CONTROLLER.getY() - 8 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
        var machine = WorldEaterStructureTests.build(level, CONTROLLER, front);
        WorldEaterStructureTests.colorFrame(machine, 1);
        level.setBlockAndUpdate(WorldEaterStructureTests.at(CONTROLLER, front, 0, 2, 6), GTMachines.ITEM_IMPORT_BUS[GTValues.LV].defaultBlockState());
        level.setBlockAndUpdate(WorldEaterStructureTests.at(CONTROLLER, front, 0, 2, 8), GTMachines.ITEM_EXPORT_BUS[GTValues.LV].defaultBlockState());
        level.setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(machine), IntentionHatchPartMachine.SAFETY.defaultBlockState());
        machine.getRecipeLogic().setWorkingEnabled(false);
        level.setDayTime(6000);
        var player = client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());
        // Inside the open core, two blocks in front of the floating demand display.
        var camera = CONTROLLER.relative(front.getOpposite(), 5).below();
        player.connection.teleport(camera.getX() + .5, camera.getY(), camera.getZ() + .5, front.toYRot() + 180, 10);
    }
    static void start(Minecraft client, boolean test) {
        var f = fixture(client);
        require(f.controller().checkPatternWithLock(), "Demand structure valid"); WorldEaterStructureTests.formed(f.controller());
        if (test) WorldEaterDemandTests.state(f.logic(), "ACTIVE");
        IntentionTamerTests.feed(f, WorldEaterDemandTests.token(test)); f.logic().setWorkingEnabled(true); f.logic().findAndHandleRecipe();
        require(!f.logic().getDemand().isEmpty(), "Real native token starts demand");
        var longest = WorldEaterDemands.pool().stream().max(java.util.Comparator.comparingInt(e -> e.stack(1).getHoverName().getString().length())).orElseThrow();
        WorldEaterDemandTests.demand(f, longest, 5);
        f.logic().setWorkingEnabled(false); f.controller().getHolder().getRootStorage().markAllDirty();
    }
    private static void requireDemand(Minecraft client) {
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(client.level, CONTROLLER);
        var demand = logic(client).getDemand();
        require(machine.isFormed() && !demand.isEmpty() && demand.getCount() == 5, "Native demand synchronization");
        require(demand.getHoverName().getString().length() >= 25, "Long native item name exercised");
        var renderer = new WorldEaterPartRender();
        require(renderer.shouldRender(machine, client.player.position()) && renderer.getRenderBoundingBox(machine).contains(machine.getStructureCenter()), "Dynamic rendering and interior bounds");
        require(client.font.split(Component.literal("5 x ").append(demand.getHoverName()), 112).stream().allMatch(line -> client.font.width(line) <= 112), "Long label wraps within interior width");
        require(logic(client).getCustomProgressLine().getString().contains(demand.getHoverName().getString()), "Native UI includes item name");
    }
    private static void materials(Minecraft client) {
        for (var stack : List.of(WorldEaterDemandTests.token(true), WorldEaterDemandTests.token(false), WorldEaterDemandTests.reward())) {
            var model = client.getItemRenderer().getModel(stack, client.level, client.player, 0);
            require(model != client.getModelManager().getMissingModel(), "Native material model exists: " + stack);
            require(!model.getParticleIcon().contents().name().getPath().contains("missingno"), "Material sprite exists");
            require(!stack.getHoverName().getString().contains("material."), "Material name localized");
        }
    }
    private static void require(boolean value, String message) { if (!value) throw new AssertionError(message); }
    private static void capture(Minecraft client, String name) {
        require(client.getOverlay() == null, "Capture is not covered by a loading overlay: " + name);
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), name, client.getMainRenderTarget(), ignored -> {});
    }
}
