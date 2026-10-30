package net.cu5tmtp.GregECore.gametest;

import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntentionTamerTests {
    static WorldEaterRunTests.Fixture fixture(GameTestHelper helper, int location) {
        var f = WorldEaterRunTests.fixture(helper, location);
        WorldEaterStructureTests.colorFrame(f.controller(), -1);
        return f;
    }

    static void form(GameTestHelper helper, WorldEaterRunTests.Fixture f) {
        WorldEaterRunTests.form(helper, f);
        WorldEaterCasingTests.active(f.logic());
        f.logic().setWorkingEnabled(true);
        feed(f, ItemStack.EMPTY);
    }

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

    static void start(GameTestHelper helper, WorldEaterRunTests.Fixture f) {
        f.logic().findAndHandleRecipe();
        helper.assertTrue(f.logic().isWorking() && f.logic().getLastRecipe().id.equals(IntentionRecipeTests.recipe(helper.getLevel(), false).id),
                "Native Bravery discovery: tier=" + f.controller().getCasingTier() + " state=" + f.logic().getProgressionState()
                        + " status=" + f.logic().getStatus() + " data=" + IntentionRecipeTests.recipe(helper.getLevel(), false).data);
        helper.assertTrue(f.logic().getDuration() == 1200 && f.logic().getLastRecipe().parallels == 1
                && !f.logic().getLastRecipe().hasTick(), "Single 1200-tick power-free craft");
    }

    private static void rejectionReason(GameTestHelper helper, WorldEaterRunTests.Fixture f, String expected) {
        f.logic().serverTick();
        var reasons = f.logic().getFancyTooltip().stream().map(net.minecraft.network.chat.Component::getString).toList();
        helper.assertTrue(f.logic().showFancyTooltip() && reasons.contains(expected)
                && reasons.stream().noneMatch(String::isBlank), "Readable native rejection tooltip: " + reasons);
    }

    static void finish(GameTestHelper helper, WorldEaterRunTests.Fixture f) {
        WorldEaterCasingTests.ticks(f, 1199);
        helper.assertTrue(WorldEaterCasingTests.outputCount(f, IntentionHatchPartMachine.BRAVERY.getItem()) == 0, "No early hatch");
        WorldEaterCasingTests.ticks(f, 1);
        WorldEaterCasingTests.ticks(f, 10);
        helper.assertTrue(WorldEaterCasingTests.outputCount(f, IntentionHatchPartMachine.BRAVERY.getItem()) == 1, "Exactly one hatch after 1200 ticks");
        helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE
                && f.logic().getCurrentChunk() == null && f.logic().getCounts().isEmpty()
                && !f.logic().hasPendingReport(), "No survey or progression side effects");
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void braveryStateTierAndMetadataGates(GameTestHelper helper) {
        var f = fixture(helper, 267000);
        helper.runAfterDelay(8, () -> {
            for (int tier : new int[]{1, 2}) {
                f.controller().onStructureInvalid();
                WorldEaterStructureTests.colorFrame(f.controller(), tier == 1 ? 0 : -1);
                WorldEaterRunTests.form(helper, f);
                f.logic().setWorkingEnabled(true);
                feed(f, IntentionRecipeTests.book(1));
                rejectedWithoutPayment(helper, f, "DORMANT tier " + tier);
                rejectionReason(helper, f, "Requires ACTIVE state (current: DORMANT).");
            }
            WorldEaterCasingTests.active(f.logic());
            f.controller().onStructureInvalid();
            WorldEaterStructureTests.colorFrame(f.controller(), 0);
            WorldEaterRunTests.form(helper, f);
            f.logic().setWorkingEnabled(true);
            feed(f, IntentionRecipeTests.book(1));
            rejectedWithoutPayment(helper, f, "ACTIVE tier 1");
            rejectionReason(helper, f, "Requires a formed tier-2 frame (all casings colored).");
            f.controller().onStructureInvalid();
            rejectedWithoutPayment(helper, f, "Unvalidated structure");
            WorldEaterStructureTests.colorFrame(f.controller(), -1);
            WorldEaterRunTests.form(helper, f);
            feed(f, IntentionRecipeTests.book(1));
            var original = IntentionRecipeTests.recipe(helper.getLevel(), false);
            for (int invalid = 0; invalid < 9; invalid++) {
                var recipe = original.copy(); recipe.data = original.data.copy();
                switch (invalid) {
                    case 0 -> recipe.data.remove("required_state");
                    case 1 -> recipe.data.putString("required_state", "DORMANT");
                    case 2 -> recipe.data.putString("required_state", "UNKNOWN");
                    case 3 -> recipe.data.remove("required_casing_tier");
                    case 4 -> recipe.data.putString("required_casing_tier", "2");
                    case 5 -> recipe.data.putInt("required_casing_tier", 1);
                    case 6 -> recipe.data.putString("operation", "unsupported");
                    case 7 -> recipe.parallels = 2;
                    default -> recipe.data.putDouble("required_casing_tier", 2.5);
                }
                f.logic().setupRecipe(recipe);
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Malformed contract rejected before payment: " + invalid);
                helper.assertTrue(!f.logic().checkMatchedRecipeAvailable(recipe)
                        && !f.logic().getFailureReasonMap().get(recipe).getString().isBlank(), "Malformed recipe rejection has a reason: " + invalid);
            }
            start(helper, f);
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).isEmpty(), "One book paid after tier-2 validation");
            finish(helper, f);
            f.controller().onStructureInvalid();
            WorldEaterStructureTests.colorFrame(f.controller(), 0);
            WorldEaterRunTests.form(helper, f);
            feed(f, IntentionRecipeTests.book(1));
            rejectedWithoutPayment(helper, f, "Downgrade revokes recipe without revoking ACTIVE");
            rejectionReason(helper, f, "Requires a formed tier-2 frame (all casings colored).");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE, "Retain earned ACTIVE");
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void braveryBooksCopiesSurplusAndIntention(GameTestHelper helper) {
        var fixtures = new WorldEaterRunTests.Fixture[3];
        for (int i = 0; i < fixtures.length; i++) {
            fixtures[i] = fixture(helper, 268000 + i * 1000);
            if (i > 0) helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(fixtures[i].controller()),
                    (i == 1 ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).defaultBlockState());
        }
        WorldEaterCasingTests.hatch(helper, fixtures[2], 2).setCurrentParallel(4);
        helper.runAfterDelay(8, () -> {
            for (int i = 0; i < fixtures.length; i++) {
                var f = fixtures[i]; form(helper, f);
                for (var invalid : new ItemStack[]{ItemStack.EMPTY, new ItemStack(Items.BOOK), new ItemStack(Items.WRITABLE_BOOK),
                        new ItemStack(Items.WRITTEN_BOOK), IntentionRecipeTests.book(i, "{"), IntentionRecipeTests.book(i, "\"wrong\"")}) {
                    feed(f, invalid.copy());
                    rejectedWithoutPayment(helper, f, "Missing or invalid acknowledgement");
                }
                var wrong = IntentionRecipeTests.book(i, "\"wrong\"");
                feed(f, wrong.copy());
                f.input().getInventory().setStackInSlot(1, IntentionRecipeTests.book(i));
                var surplus = IntentionRecipeTests.book(i + 3); surplus.setCount(2);
                f.input().getInventory().setStackInSlot(2, surplus.copy());
                start(helper, f);
                helper.assertTrue(ItemStack.matches(wrong, f.input().getInventory().getStackInSlot(0))
                        && f.input().getInventory().getStackInSlot(1).isEmpty()
                        && ItemStack.matches(surplus, f.input().getInventory().getStackInSlot(2)), "Only one qualifying book paid, even with parallel hatch");
                f.input().getInventory().setStackInSlot(2, ItemStack.EMPTY);
                finish(helper, f);
                f.logic().setWorkingEnabled(false);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void braveryBlockedOutputAndSavedWork(GameTestHelper helper) {
        var f = fixture(helper, 271000);
        helper.runAfterDelay(8, () -> {
            form(helper, f); feed(f, IntentionRecipeTests.book(12));
            WorldEaterRunTests.blockOutput(f);
            rejectedWithoutPayment(helper, f, "No output space at startup");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            start(helper, f);
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).isEmpty(), "One initial payment");
            WorldEaterCasingTests.ticks(f, 35);
            f.logic().setWorkingEnabled(false);
            WorldEaterCasingTests.ticks(f, 20);
            var holder = (BlockEntity) f.controller().getHolder();
            holder.load(holder.saveWithFullMetadata().copy());
            helper.assertTrue(f.logic().getProgress() == 35, "Paused saved work retained");
            f.logic().setWorkingEnabled(true);
            WorldEaterRunTests.blockOutput(f);
            WorldEaterCasingTests.ticks(f, 1165);
            helper.assertTrue(f.logic().getProgress() == 1200 && f.logic().isWaiting(), "Paid hatch waits for output");
            holder.load(holder.saveWithFullMetadata().copy());
            WorldEaterCasingTests.ticks(f, 10);
            helper.assertTrue(f.logic().getProgress() == 1200 && f.input().getInventory().getStackInSlot(0).isEmpty(), "No loss or repayment on reload");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            WorldEaterCasingTests.ticks(f, 11);
            helper.assertTrue(WorldEaterCasingTests.outputCount(f, IntentionHatchPartMachine.BRAVERY.getItem()) == 1
                    && !f.logic().hasPendingReport(), "Exactly one retained hatch, not a survey report");
            feed(f, IntentionRecipeTests.book(13)); start(helper, f);
            WorldEaterCasingTests.ticks(f, 10);
            f.controller().onStructureInvalid();
            helper.assertTrue(f.logic().getLastRecipe() == null && f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE,
                    "Structural interruption retains progression, not unfinished work");
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
