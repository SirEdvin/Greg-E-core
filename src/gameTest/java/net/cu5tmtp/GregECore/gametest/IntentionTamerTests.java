package net.cu5tmtp.GregECore.gametest;

import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntentionTamerTests {
    static void feed(WorldEaterRunTests.Fixture f, ItemStack book) {
        f.logic().resetRecipeLogic();
        for (int i = 0; i < f.input().getInventory().getSlots(); i++)
            f.input().getInventory().setStackInSlot(i, ItemStack.EMPTY);
        f.input().getInventory().setStackInSlot(0, book);
    }

    static void rejectedWithoutPayment(GameTestHelper helper, WorldEaterRunTests.Fixture f, String message) {
        var before = new ItemStack[f.input().getInventory().getSlots()];
        for (int i = 0; i < before.length; i++) before[i] = f.input().getInventory().getStackInSlot(i).copy();
        f.logic().findAndHandleRecipe();
        helper.assertTrue(!f.logic().isWorking(), message);
        for (int i = 0; i < before.length; i++) helper.assertTrue(
                ItemStack.matches(before[i], f.input().getInventory().getStackInSlot(i)), "No payment: " + message);
    }

    private static void rejectionReason(GameTestHelper helper, WorldEaterRunTests.Fixture f, String expected) {
        f.logic().serverTick();
        var reasons = f.logic().getFancyTooltip().stream().map(net.minecraft.network.chat.Component::getString).toList();
        helper.assertTrue(f.logic().showFancyTooltip() && reasons.contains(expected)
                && reasons.stream().noneMatch(String::isBlank), "Readable native rejection tooltip: " + reasons);
    }

    // Tier 2 is unreachable with the black-only frame, so only its rejections remain testable.
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void braveryStateTierAndMetadataGates(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 267000);
        helper.runAfterDelay(8, () -> {
            WorldEaterRunTests.form(helper, f);
            f.logic().setWorkingEnabled(true);
            feed(f, IntentionRecipeTests.book(1));
            rejectedWithoutPayment(helper, f, "DORMANT tier 1");
            rejectionReason(helper, f, "Requires ACTIVE state (current: DORMANT).");
            WorldEaterCasingTests.active(f.logic());
            f.controller().onStructureInvalid();
            WorldEaterRunTests.form(helper, f);
            f.logic().setWorkingEnabled(true);
            feed(f, IntentionRecipeTests.book(1));
            rejectedWithoutPayment(helper, f, "ACTIVE tier 1");
            rejectionReason(helper, f, "Requires a formed tier-2 frame (all casings colored).");
            f.controller().onStructureInvalid();
            rejectedWithoutPayment(helper, f, "Unvalidated structure");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE, "Retain earned ACTIVE");
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
