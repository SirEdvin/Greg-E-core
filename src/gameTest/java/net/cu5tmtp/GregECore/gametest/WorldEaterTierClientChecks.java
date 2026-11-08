package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.gui.widget.SceneWidget;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
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
                var entry = predicates.entrySet().stream().filter(e -> !hasCandidate(e.getValue(), IntentionHatchPartMachine.SAFETY.getItem())
                        && hasCandidate(e.getValue(), WorldEaterCasingTests.casings()[0].asItem())).findFirst().orElseThrow();
                var casing = entry.getValue().common.stream().filter(p -> p.getCandidates().stream()
                        .anyMatch(s -> s.is(WorldEaterCasingTests.casings()[0].asItem()))).findFirst().orElseThrow().getCandidates();
                if (casing.size() != 1 || !casing.get(0).is(WorldEaterCasingTests.casings()[0].asItem()))
                    throw new AssertionError("Only the black casing is a frame candidate");
                var sceneField = PatternPreviewWidget.class.getDeclaredField("sceneWidget");
                sceneField.setAccessible(true);
                var scene = (SceneWidget) sceneField.get(preview);
                scene.getOnSelected().accept(entry.getKey(), Direction.NORTH);
                var candidatesField = PatternPreviewWidget.class.getDeclaredField("candidates");
                candidatesField.setAccessible(true);
                int ordinarySelectors = ((Object[]) candidatesField.get(preview)).length;
                if (ordinarySelectors != entry.getValue().common.size() + entry.getValue().limited.size())
                    throw new AssertionError("Every ordinary predicate remains selectable");
                var rear = predicates.entrySet().stream().filter(e -> hasCandidate(e.getValue(), IntentionHatchPartMachine.SAFETY.getItem())).toList();
                if (rear.size() != 1 || !hasCandidate(rear.get(0).getValue(), IntentionHatchPartMachine.BRAVERY.getItem()))
                    throw new AssertionError("Exactly one slot offers both intention variants");
                var controller = predicates.entrySet().stream().filter(e -> hasCandidate(e.getValue(), IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.getItem())).findFirst().orElseThrow().getKey();
                if (rear.get(0).getKey().getY() != controller.getY() || rear.get(0).getKey().distManhattan(controller) != 14)
                    throw new AssertionError("Rear selector is opposite the controller on the same Y");
                if (!hasCandidate(rear.get(0).getValue(), WorldEaterStructureTests.rear().asItem())
                        || hasCandidate(rear.get(0).getValue(), WorldEaterCasingTests.casings()[0].asItem()))
                    throw new AssertionError("Rear selector offers the intake, not frame casing or parts");
                scene.getOnSelected().accept(rear.get(0).getKey(), Direction.SOUTH);
                if (((Object[]) candidatesField.get(preview)).length != rear.get(0).getValue().common.size() + rear.get(0).getValue().limited.size())
                    throw new AssertionError("Rear selector offers intake and intention variants");
                if (!client.screen.getClass().getName().contains("RecipesGui")) throw new AssertionError("JEI preview screen not displayed");
                System.out.println("WORLD_EATER_TIER_CLIENT_VERIFIED casingCandidates=1 selected=" + entry.getKey());
            }
            if (frame == 40) screenshot("tier-structure-preview.png");
            if (frame == 41) {
                var stack = new ItemStack(IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.getItem());
                var text = stack.getTooltipLines(client.player, TooltipFlag.Default.NORMAL).stream().map(Component::getString).toList();
                if (text.stream().noneMatch(s -> s.contains("Frame: World Eater Tamer Casing."))
                        || text.stream().noneMatch(s -> s.contains("rear face center"))
                        || text.stream().anyMatch(s -> s.contains("any mix of black"))) throw new AssertionError("Revised native tooltip missing or obsolete");
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

    private static boolean hasCandidate(TraceabilityPredicate predicate, net.minecraft.world.item.Item item) {
        return java.util.stream.Stream.concat(predicate.common.stream(), predicate.limited.stream())
                .anyMatch(p -> p.getCandidates().stream().anyMatch(s -> s.is(item)));
    }
}
