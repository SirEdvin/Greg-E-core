package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterRunTests {
    record Fixture(IncompleteWorldEaterTamer controller, ItemBusPartMachine input, ItemBusPartMachine output) {
        WorldEaterTamerRecipeLogic logic() { return (WorldEaterTamerRecipeLogic) controller.getRecipeLogic(); }
    }

    static Fixture fixture(GameTestHelper helper, int location) {
        BlockPos center = helper.absolutePos(new BlockPos(location, 3, location));
        for (int x = (center.getX() >> 4) - 1; x <= (center.getX() >> 4) + 1; x++)
            for (int z = (center.getZ() >> 4) - 1; z <= (center.getZ() >> 4) + 1; z++)
                helper.getLevel().setChunkForced(x, z, true);
        var machine = WorldEaterStructureTests.shell(helper, Direction.NORTH, new BlockPos(location, 3, location));
        var positions = WorldEaterStructureTests.shellPositions(machine);
        var input = WorldEaterStructureTests.bus(helper, positions.get(0), GTMachines.ITEM_IMPORT_BUS[GTValues.LV]);
        var output = WorldEaterStructureTests.bus(helper, positions.get(1), GTMachines.ITEM_EXPORT_BUS[GTValues.LV]);
        return new Fixture(machine, input, output);
    }

    static com.gregtechceu.gtceu.api.recipe.GTRecipe recipe() {
        return GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("fixture_survey"))
                .duration(2400).inputItems(GTItems.SENSOR_IV, 1).outputItems(Items.WRITTEN_BOOK)
                .addData("required_state", "DORMANT").addData("operation", "information_collection").buildRawRecipe();
    }

    static void blockOutput(Fixture fixture) {
        for (int slot = 0; slot < fixture.output.getInventory().getSlots(); slot++)
            fixture.output.getInventory().setStackInSlot(slot, new ItemStack(Items.STONE, 64));
    }

    static void form(GameTestHelper helper, Fixture fixture) {
        boolean matches = fixture.controller.checkPatternWithLock();
        helper.assertTrue(matches, "Fixture shell valid: " + (fixture.controller.getMultiblockState().error == null ? "none" : fixture.controller.getMultiblockState().error.getErrorInfo().getString())
                + " at " + fixture.controller.getMultiblockState().getPos() + " controller " + fixture.controller.getPos());
        fixture.controller.onStructureFormed();
        fixture.input.getInventory().setStackInSlot(0, GTItems.SENSOR_IV.asStack(2));
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void loadedThresholdsAndBlockedDelivery(GameTestHelper helper) {
        loadedThresholdsAndBlockedDelivery(helper, 1);
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void tierTwoThresholdsAndBlockedDelivery(GameTestHelper helper) {
        loadedThresholdsAndBlockedDelivery(helper, 2);
    }

    private static void loadedThresholdsAndBlockedDelivery(GameTestHelper helper, int tier) {
        Fixture[] fixtures = {fixture(helper, 5000 + (tier - 1) * 500), fixture(helper, 10000 + (tier - 1) * 500),
                fixture(helper, 15000 + (tier - 1) * 500), fixture(helper, 20000 + (tier - 1) * 500)};
        for (int i = 0; i < fixtures.length; i++) {
            var f = fixtures[i];
            WorldEaterStructureTests.colorFrame(f.controller(), tier == 1 ? 0 : -1);
            if (i == 1 || i == 2) helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(f.controller()),
                    (i == 1 ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).defaultBlockState());
        }
        helper.runAfterDelay(4, () -> {
            int[] totals = {0, 499, 500, 501};
            for (int test = 0; test < fixtures.length; test++) {
                Fixture fixture = fixtures[test]; form(helper, fixture);
                var controller = fixture.controller;
                var level = helper.getLevel();
                int cx = controller.getPos().getX() >> 4, cz = controller.getPos().getZ() >> 4;
                for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) level.getChunk(x, z);
                var domain = new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight());
                for (int i = 0; i < totals[test]; i++) {
                    // Pack real machines into the first batch to exercise dense block-entity access.
                    BlockPos pos = new BlockPos(domain.x(i), domain.y(i), domain.z(i));
                    level.setBlockAndUpdate(pos, GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
                }
                fixture.logic().setWorkingEnabled(true);
                var sharedRecipe = recipe();
                var originalOutput = com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.contentToNbt(
                        sharedRecipe.outputs.get(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP).get(0).content).copy();
                fixture.logic().setupRecipe(sharedRecipe);
                helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 1, "Consumes exactly one sensor");
                // Occupy output AFTER setup, to exercise dynamic completion rather than recipe matching.
                blockOutput(fixture);
                long worst = 0;
                for (int tick = 0; tick < 2400; tick++) {
                    long start = System.nanoTime();
                    try {
                        fixture.logic().serverTick();
                    } catch (RuntimeException exception) {
                        exception.printStackTrace();
                        throw exception;
                    }
                    worst = Math.max(worst, System.nanoTime() - start);
                    if (tick < 2399) helper.assertTrue(!fixture.logic().hasPendingReport(), "Not finished early");
                }
                helper.assertTrue(fixture.logic().hasPendingReport(), "Final batch finishes exactly on scanning tick 2400");
                var nextSurvey = recipe();
                helper.assertTrue(!fixture.logic().checkMatchedRecipeAvailable(nextSurvey)
                        && fixture.logic().getFailureReasonMap().get(nextSurvey).getString().equals(
                                "Survey report must be delivered before starting another recipe."), "Pending report rejection has a reason");
                helper.assertTrue(fixture.logic().getCounts().values().stream().mapToLong(Long::longValue).sum() == totals[test], "Exact count " + totals[test]);
                helper.assertTrue(fixture.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Blocked report cannot activate");
                var pending = fixture.logic().getPendingBook().copy();
                if (test == 1 || test == 2) {
                    var rear = WorldEaterStructureTests.intentionPosition(controller);
                    helper.getLevel().setBlockAndUpdate(rear, (test == 1 ? IntentionHatchPartMachine.BRAVERY : IntentionHatchPartMachine.SAFETY).defaultBlockState());
                    WorldEaterStructureTests.assertTier(helper, controller, tier);
                    helper.assertTrue(ItemStack.matches(pending, fixture.logic().getPendingBook()), "Swapping intention retains completed report");
                    helper.getLevel().setBlockAndUpdate(rear, WorldEaterCasingTests.casings()[tier == 1 ? 0 : 1].defaultBlockState());
                    WorldEaterStructureTests.assertTier(helper, controller, tier);
                    helper.assertTrue(controller.getIntention() == IntentionHatchPartMachine.Intention.NONE
                            && ItemStack.matches(pending, fixture.logic().getPendingBook()), "Removing intention retains completed report");
                }
                var framePos = WorldEaterStructureTests.shellPositions(controller).get(3);
                helper.getLevel().setBlockAndUpdate(framePos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
                controller.onStructureInvalid();
                helper.getLevel().setBlockAndUpdate(framePos, WorldEaterCasingTests.casings()[1].defaultBlockState());
                WorldEaterStructureTests.colorFrame(controller, -1);
                WorldEaterStructureTests.assertTier(helper, controller, 2);
                helper.assertTrue(ItemStack.matches(pending, fixture.logic().getPendingBook()), "Same completed successful report retained through colored rebuild");
                CompoundTag saved = ((net.minecraft.world.level.block.entity.BlockEntity) controller.getHolder()).saveWithFullMetadata();
                ((net.minecraft.world.level.block.entity.BlockEntity) controller.getHolder()).load(saved);
                helper.assertTrue(fixture.logic().hasPendingReport(), "Blocked report persists through native NBT");
                for (int i = 0; i < 3; i++) fixture.logic().serverTick();
                helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 1, "No restart while blocked");
                fixture.output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                fixture.logic().serverTick();
                helper.assertTrue(fixture.output.getInventory().getStackInSlot(0).is(Items.WRITTEN_BOOK), "Delivered real book");
                helper.assertTrue(fixture.output.getInventory().getStackInSlot(0).getCount() == 1, "Exactly one book");
                helper.assertTrue(sharedRecipe.duration == 2400 && originalOutput.equals(
                        com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP.contentToNbt(
                                sharedRecipe.outputs.get(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP).get(0).content)), "Shared recipe output never mutated");
                helper.assertTrue(fixture.logic().getProgressionState().name().equals(totals[test] >= 500 ? "ACTIVE" : "DORMANT"), "Threshold transition " + totals[test]);
                var holder = (net.minecraft.world.level.block.entity.BlockEntity) controller.getHolder();
                holder.load(holder.saveWithFullMetadata());
                helper.assertTrue(fixture.logic().getProgressionState().name().equals(totals[test] >= 500 ? "ACTIVE" : "DORMANT"), "Native state round trip");
                controller.onStructureInvalid();
                helper.assertTrue(controller.checkPatternWithLock(), "Shell still valid after cancellation");
                controller.onStructureFormed();
                if (totals[test] >= 500) {
                    fixture.logic().setupRecipe(recipe());
                    fixture.logic().findAndHandleRecipe();
                    helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 1, "ACTIVE rejects new and cached searches");
                    helper.assertTrue(fixture.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE, "ACTIVE survives reformation");
                } else {
                    fixture.output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
                    fixture.logic().setupRecipe(recipe());
                    helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).isEmpty(), "Next run consumes its own sensor");
                    helper.assertTrue(fixture.logic().getCounts().isEmpty() && fixture.logic().getProgress() == 0, "Next run starts clean");
                    for (int tick = 0; tick < 2400; tick++) fixture.logic().serverTick();
                    helper.assertTrue(fixture.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Consecutive below-threshold scans do not accumulate");
                    helper.assertTrue(fixture.output.getInventory().getStackInSlot(0).getCount() == 1, "Fresh consecutive report");
                }
                fixture.logic().setWorkingEnabled(false);
                System.out.println("WORLD_EATER_VERIFIED threshold=" + totals[test] + " budget="
                        + new WorldEaterScanDomain(cx, cz, level.getMinBuildHeight(), level.getMaxBuildHeight()).batchBudget()
                        + " worstBatchNanos=" + worst);
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void metadataGatesAndCancellation(GameTestHelper helper) {
        metadataGatesAndCancellation(helper, 1);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void tierTwoMetadataGatesAndCancellation(GameTestHelper helper) {
        metadataGatesAndCancellation(helper, 2);
    }

    private static void metadataGatesAndCancellation(GameTestHelper helper, int tier) {
        Fixture fixture = fixture(helper, 25000 + (tier - 1) * 1000);
        WorldEaterStructureTests.colorFrame(fixture.controller(), tier == 1 ? 0 : -1);
        helper.runAfterDelay(4, () -> {
            form(helper, fixture);
            fixture.logic().setWorkingEnabled(true);
            for (String state : new String[]{"", "UNKNOWN", "ACTIVE"}) {
                var invalid = recipe(); invalid.data.putString("required_state", state);
                fixture.logic().setupRecipe(invalid);
                helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 2, "No input for invalid state " + state);
            }
            var invalid = recipe(); invalid.data.remove("required_state"); fixture.logic().setupRecipe(invalid);
            invalid = recipe(); invalid.data.putString("operation", "unsupported"); fixture.logic().setupRecipe(invalid);
            helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 2, "Missing state and unsupported operation fail closed");
            fixture.logic().findAndHandleRecipe();
            helper.assertTrue(fixture.logic().getLastRecipe() != null
                    && fixture.logic().getLastRecipe().id.equals(GregECore.id("world_eater_tamer/information_collection")), "Native recipe search starts the addon-owned recipe without energy");
            helper.assertTrue(fixture.input.getInventory().getStackInSlot(0).getCount() == 1
                    && fixture.logic().getDuration() == 2400, "Native startup consumes exactly one sensor without overclocking");
            fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().getProgress() == 1 && fixture.logic().getElapsedDelays() == 1, "Detection counts as first delay tick");
            fixture.logic().setWorkingEnabled(false);
            for (int i = 0; i < 30; i++) fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().getProgress() == 1, "Suspension preserves delay");
            fixture.controller.onPartUnload();
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().getProgress() == 1, "Part unavailability preserves delay");
            fixture.controller.onStructureInvalid();
            helper.assertTrue(fixture.logic().getProgress() == 0 && fixture.logic().getCounts().isEmpty(), "True cancellation clears run");
            fixture.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void resumedMetadataFailsClosed(GameTestHelper helper) {
        var fixture = fixture(helper, 73000);
        helper.runAfterDelay(4, () -> {
            form(helper, fixture);
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().setupRecipe(recipe());
            fixture.logic().serverTick();
            var holder = (net.minecraft.world.level.block.entity.BlockEntity) fixture.controller().getHolder();
            var snapshot = holder.saveWithFullMetadata();
            for (int invalid = 0; invalid < 4; invalid++) {
                holder.load(snapshot.copy());
                var resumed = fixture.logic().getLastRecipe();
                switch (invalid) {
                    case 0 -> resumed.data.remove("required_state");
                    case 1 -> resumed.data.putString("required_state", "UNKNOWN");
                    case 2 -> resumed.data.putString("required_state", "ACTIVE");
                    default -> resumed.data.putString("operation", "unsupported");
                }
                fixture.logic().serverTick();
                helper.assertTrue(fixture.logic().getProgress() == 0 && fixture.logic().getCounts().isEmpty(), "Invalid resumed recipe cancelled");
                helper.assertTrue(fixture.input().getInventory().getStackInSlot(0).getCount() == 1, "Invalid resume consumes no additional input");
            }
            fixture.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void retryExhaustionAndNativePersistence(GameTestHelper helper) {
        retryExhaustionAndNativePersistence(helper, null, 30000);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void safetySurveyFailure(GameTestHelper helper) {
        retryExhaustionAndNativePersistence(helper, IntentionHatchPartMachine.SAFETY, 262000);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void braverySurveyFailure(GameTestHelper helper) {
        retryExhaustionAndNativePersistence(helper, IntentionHatchPartMachine.BRAVERY, 263000);
    }

    private static void retryExhaustionAndNativePersistence(GameTestHelper helper, com.gregtechceu.gtceu.api.machine.MachineDefinition intention, int location) {
        Fixture fixture = fixture(helper, location);
        if (intention != null) helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(fixture.controller()), intention.defaultBlockState());
        helper.runAfterDelay(4, () -> {
            form(helper, fixture);
            fixture.logic().setWorkingEnabled(true);
            fixture.logic().setupRecipe(recipe());
            blockOutput(fixture);
            for (int tick = 0; tick < 100; tick++) {
                fixture.logic().serverTick();
                helper.assertTrue(!fixture.logic().hasPendingReport(), "No premature exhaustion");
                helper.assertTrue(fixture.logic().getProgress() == tick + 1 && fixture.logic().getElapsedDelays() == tick + 1, "Delay accounting");
                if (tick == 7 || tick == 19 || tick == 60) {
                    var holder = (net.minecraft.world.level.block.entity.BlockEntity) fixture.controller.getHolder();
                    CompoundTag saved = holder.saveWithFullMetadata(); holder.load(saved);
                    if (tick == 7 && intention == null) {
                        try {
                            net.minecraft.nbt.NbtIo.writeCompressed(saved,
                                    java.nio.file.Path.of(System.getenv("TMPDIR"), "world-eater-retry-snapshot.nbt").toFile());
                        } catch (java.io.IOException exception) {
                            throw new RuntimeException(exception);
                        }
                    }
                    helper.assertTrue(fixture.logic().getProgress() == tick + 1, "Native persistence retains retry progress");
                }
            }
            fixture.logic().serverTick();
            helper.assertTrue(fixture.logic().hasPendingReport(), "Fifth retry fails immediately on tick 101: "
                    + fixture.logic().getCustomProgressLine().getString() + " progress=" + fixture.logic().getProgress()
                    + " duration=" + fixture.logic().getDuration() + " status=" + fixture.logic().getStatus());
            helper.assertTrue(fixture.logic().getDuration() == 2500, "Exactly five extensions");
            helper.assertTrue(fixture.logic().getProgress() == 100, "No sixth delay or waiting base remainder");
            var book = fixture.logic().getPendingBook();
            fixture.controller.onStructureInvalid();
            var holder = (net.minecraft.world.level.block.entity.BlockEntity) fixture.controller.getHolder();
            CompoundTag saved = holder.saveWithFullMetadata(); holder.load(saved);
            helper.assertTrue(fixture.logic().hasPendingReport() && ItemStack.matches(book, fixture.logic().getPendingBook()), "Completed report survives cancellation and NBT");
            fixture.controller.checkPatternWithLock(); fixture.controller.onStructureFormed();
            fixture.output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
            fixture.logic().serverTick();
            helper.assertTrue(fixture.output.getInventory().getStackInSlot(0).is(Items.WRITTEN_BOOK), "Failure delivered");
            helper.assertTrue(fixture.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Failure stays dormant");
            fixture.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
