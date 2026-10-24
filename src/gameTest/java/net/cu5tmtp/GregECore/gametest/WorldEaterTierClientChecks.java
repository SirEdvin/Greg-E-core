package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Opt-in checks of the actual JEI preview selection and native item tooltip. */
final class WorldEaterTierClientChecks {
    static final List<String> failures = new ArrayList<>();
    private static int frame;

    static void begin() {
        if (!WorldEaterCasingJeiChecks.showTamerStructure()) failures.add("JEI structure preview unavailable");
    }

    @SuppressWarnings("unchecked")
    static boolean tick() {
        var client = Minecraft.getInstance();
        frame++;
        try {
            if (frame == 20) {
                var preview = WorldEaterCasingJeiChecks.structurePreview;
                if (preview == null || preview.patterns.length == 0) throw new AssertionError("No native structure preview");
                var field = PatternPreviewWidget.MBPattern.class.getDeclaredField("predicateMap");
                field.setAccessible(true);
                var predicates = (Map<BlockPos, TraceabilityPredicate>) field.get(preview.patterns[0]);
                var entry = predicates.entrySet().stream().filter(e -> e.getValue().common.stream().anyMatch(p ->
                        p.getCandidates().stream().anyMatch(s -> s.is(WorldEaterCasingTests.casings()[0].asItem())))).findFirst().orElseThrow();
                var casing = entry.getValue().common.stream().filter(p -> p.getCandidates().stream()
                        .anyMatch(s -> s.is(WorldEaterCasingTests.casings()[0].asItem()))).findFirst().orElseThrow().getCandidates();
                if (casing.size() != 4 || !casing.get(0).is(WorldEaterCasingTests.casings()[0].asItem()))
                    throw new AssertionError("Four casing candidates, black first");
                for (var block : WorldEaterCasingTests.casings()) if (casing.stream().noneMatch(s -> s.is(block.asItem())))
                    throw new AssertionError("Missing casing candidate " + block);
                var sceneField = PatternPreviewWidget.class.getDeclaredField("sceneWidget");
                sceneField.setAccessible(true);
                var scene = (SceneWidget) sceneField.get(preview);
                scene.getOnSelected().accept(entry.getKey(), Direction.NORTH);
                var candidatesField = PatternPreviewWidget.class.getDeclaredField("candidates");
                candidatesField.setAccessible(true);
                if (((Object[]) candidatesField.get(preview)).length != 6)
                    throw new AssertionError("Casing selector and all five part-family selectors must remain usable");
                if (!client.screen.getClass().getName().contains("RecipesGui")) throw new AssertionError("JEI preview screen not displayed");
                System.out.println("WORLD_EATER_TIER_CLIENT_VERIFIED casingCandidates=4 partSelectors=5 selected=" + entry.getKey());
            }
            if (frame == 40) screenshot("tier-structure-preview.png");
            if (frame == 41) {
                var stack = new ItemStack(IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.getItem());
                var text = stack.getTooltipLines(client.player, TooltipFlag.Default.NORMAL).stream().map(Component::getString).toList();
                if (text.stream().noneMatch(s -> s.contains("any mix of black, purple, green, or orange"))
                        || text.stream().noneMatch(s -> s.contains("tier 1") && s.contains("tier 2"))
                        || text.stream().anyMatch(s -> s.contains("Requires black"))) throw new AssertionError("Revised native tooltip missing or obsolete");
                client.setScreen(new Screen(Component.literal("Casing tier tooltip verification")) {
                    @Override public void render(GuiGraphics graphics, int x, int y, float partialTick) {
                        renderBackground(graphics);
                        graphics.renderTooltip(font, stack, 24, 60);
                    }
                });
            }
            if (frame == 60) screenshot("tier-controller-tooltip.png");
        } catch (Exception | AssertionError error) {
            failures.add("Tier client check: " + error);
            return true;
        }
        return frame >= 65;
    }

    private static void screenshot(String name) {
        var client = Minecraft.getInstance();
        Screenshot.grab(Path.of(System.getenv("TMPDIR"), "world-eater-client").toFile(), name,
                client.getMainRenderTarget(), message -> {});
    }
}
