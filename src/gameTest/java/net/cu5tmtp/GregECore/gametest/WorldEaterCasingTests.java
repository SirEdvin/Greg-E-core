package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.machines.GCYMMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ParallelHatchPartMachine;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterCasingTests {
    static final Item[] TERRACOTTA = {Items.PURPLE_TERRACOTTA, Items.GREEN_TERRACOTTA, Items.ORANGE_TERRACOTTA};
    static final String[] COLORS = {"purple", "green", "orange"};

    static Block[] casings() {
        return new Block[]{GreggyBlocks.WORLD_EATER_TAMER_CASING.get(), GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.get(),
                GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.get(), GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.get()};
    }

    static GTRecipe recipe(GameTestHelper helper, int color) {
        return (GTRecipe) helper.getLevel().getRecipeManager().byKey(GregECore.id("world_eater_tamer/" + COLORS[color] + "_casing")).orElseThrow();
    }

    static void active(WorldEaterTamerRecipeLogic logic) {
        CompoundTag tag = new CompoundTag(), custom = new CompoundTag();
        custom.putInt("Version", 1); custom.putString("State", "ACTIVE");
        tag.put("WorldEaterTamer", custom);
        logic.loadCustomPersistedData(tag);
    }

    static ParallelHatchPartMachine hatch(GameTestHelper helper, WorldEaterRunTests.Fixture fixture, int index) {
        var pos = WorldEaterStructureTests.shellPositions(fixture.controller()).get(index);
        helper.getLevel().setBlockAndUpdate(pos, GCYMMachines.PARALLEL_HATCH[GTValues.IV].defaultBlockState());
        return (ParallelHatchPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
    }

    static void feed(WorldEaterRunTests.Fixture f, int color, int black, int terracotta) {
        for (int slot = 0; slot < f.input().getInventory().getSlots(); slot++)
            f.input().getInventory().setStackInSlot(slot, ItemStack.EMPTY);
        f.input().getInventory().setStackInSlot(0, new ItemStack(GreggyBlocks.WORLD_EATER_TAMER_CASING.asItem(), black));
        f.input().getInventory().setStackInSlot(1, new ItemStack(TERRACOTTA[color], terracotta));
    }

    static int outputCount(WorldEaterRunTests.Fixture f, Item item) {
        int count = 0;
        for (int slot = 0; slot < f.output().getInventory().getSlots(); slot++) {
            var stack = f.output().getInventory().getStackInSlot(slot);
            if (stack.is(item)) count += stack.getCount();
        }
        return count;
    }

    static void ticks(WorldEaterRunTests.Fixture f, int count) {
        for (int i = 0; i < count; i++) f.logic().serverTick();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void casingRegistrationAndLoot(GameTestHelper helper) {
        var tab = ModCreativeModTabs.GREGE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        var player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.getAbilities().instabuild = false;
        int index = 0;
        for (var block : casings()) {
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(block.asItem())), "Creative casing: " + block);
            helper.assertTrue(!block.getName().getString().startsWith("block."), "Localized casing name");
            BlockPos below = helper.absolutePos(new BlockPos(1 + index++ * 2, 2, 1));
            helper.getLevel().setBlockAndUpdate(below, Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(below.above(), Blocks.AIR.defaultBlockState());
            ItemStack stack = new ItemStack(block.asItem());
            var context = new BlockPlaceContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, stack,
                    new BlockHitResult(Vec3.atCenterOf(below).add(0, .5, 0), Direction.UP, below, false));
            helper.assertTrue(((BlockItem) block.asItem()).place(context).consumesAction(), "Native BlockItem placement");
            helper.assertTrue(stack.isEmpty() && helper.getLevel().getBlockState(below.above()).is(block), "Placed and paid exactly one casing");
            var drops = Block.getDrops(block.defaultBlockState(), helper.getLevel(), below.above(), null, player, new ItemStack(Items.IRON_PICKAXE));
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(block.asItem()) && drops.get(0).getCount() == 1, "Variant self drop");
        }
        for (var r : helper.getLevel().getRecipeManager().getRecipes()) {
            if (r instanceof GTRecipe gt) {
                for (var content : gt.getOutputContents(ItemRecipeCapability.CAP)) {
                    helper.assertTrue(!((net.minecraft.world.item.crafting.Ingredient) content.content).test(new ItemStack(casings()[0])), "No GT recipe produces black casing");
                }
            } else helper.assertTrue(!r.getResultItem(helper.getLevel().registryAccess()).is(casings()[0].asItem()), "No survival black casing recipe");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void allColorsAndStateGates(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 100000);
        helper.runAfterDelay(6, () -> {
            WorldEaterRunTests.form(helper, f);
            f.logic().setWorkingEnabled(true);
            for (int color = 0; color < 3; color++) {
                feed(f, color, 1, 16);
                f.logic().setupRecipe(recipe(helper, color));
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "DORMANT conversion rejected");
            }
            active(f.logic());
            f.logic().setWorkingEnabled(true);
            for (int color = 0; color < 3; color++) {
                GTRecipe original = recipe(helper, color);
                helper.assertTrue(original.duration == 100 && WorldEaterTamerRecipeLogic.isCasingRecipe(original), "Loaded recipe metadata");
                helper.assertTrue(original.inputs.get(ItemRecipeCapability.CAP).size() == 2 && original.outputs.get(ItemRecipeCapability.CAP).size() == 1, "Two inputs and one output");
                helper.assertTrue(!original.inputs.containsKey(EURecipeCapability.CAP) && !original.inputs.containsKey(FluidRecipeCapability.CAP)
                        && !original.hasTick(), "No EU or fluids");
                for (String invalid : new String[]{"", "UNKNOWN", "DORMANT"}) {
                    feed(f, color, 1, 16);
                    var rejected = original.copy(); rejected.data = original.data.copy();
                    if (invalid.isEmpty()) rejected.data.remove("required_state");
                    else rejected.data.putString("required_state", invalid);
                    f.logic().setupRecipe(rejected);
                    helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Invalid ACTIVE metadata rejected");
                }
                feed(f, color, 0, 16); f.logic().setupRecipe(original);
                helper.assertTrue(f.input().getInventory().getStackInSlot(1).getCount() == 16, "Missing black input rejected");
                feed(f, color, 1, 15); f.logic().setupRecipe(original);
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Insufficient terracotta rejected");
                feed(f, (color + 1) % 3, 1, 16); f.logic().setupRecipe(original);
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Wrong color rejected");
                feed(f, color, 2, 32);
                f.logic().findAndHandleRecipe();
                helper.assertTrue(f.logic().isWorking() && f.logic().getDuration() == 100 && f.logic().getLastRecipe().parallels == 1, "Native single recipe search");
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1
                        && f.input().getInventory().getStackInSlot(1).getCount() == 16, "Exact single input payment");
                // Remove surplus to isolate completion from native immediate repeat.
                f.input().getInventory().setStackInSlot(0, ItemStack.EMPTY);
                ticks(f, 99);
                helper.assertTrue(outputCount(f, casings()[color + 1].asItem()) == 0, "Not complete before 100 ticks");
                ticks(f, 1);
                helper.assertTrue(outputCount(f, casings()[color + 1].asItem()) == 1, "One output at exactly 100 ticks");
                helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE
                        && f.logic().getCurrentChunk() == null && f.logic().getCounts().isEmpty()
                        && !f.logic().hasPendingReport() && !f.logic().getCustomProgressLine().getString().contains("Scan:"), "No scan side effects");
                f.logic().resetRecipeLogic();
            }
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void activeResumeAndBlockedOutput(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 105000);
        helper.runAfterDelay(6, () -> {
            WorldEaterRunTests.form(helper, f); active(f.logic());
            f.logic().setWorkingEnabled(true);
            feed(f, 0, 1, 16);
            WorldEaterRunTests.blockOutput(f);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Blocked startup does not pay");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            f.logic().findAndHandleRecipe();
            ticks(f, 35);
            f.logic().setWorkingEnabled(false);
            ticks(f, 20);
            helper.assertTrue(f.logic().getProgress() == 35, "Suspended craft does not advance");
            var holder = (BlockEntity) f.controller().getHolder();
            holder.load(holder.saveWithFullMetadata().copy());
            helper.assertTrue(f.logic().getProgress() == 35 && f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE, "Native saved craft survives");
            f.logic().setWorkingEnabled(true);
            WorldEaterRunTests.blockOutput(f);
            ticks(f, 65);
            helper.assertTrue(f.logic().getProgress() == 100 && f.logic().isWaiting(), "Paid output waits at completion");
            holder.load(holder.saveWithFullMetadata().copy());
            ticks(f, 10);
            helper.assertTrue(f.logic().getProgress() == 100 && f.input().getInventory().getStackInSlot(0).isEmpty(), "Blocked output retained across reload without recharge");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            ticks(f, 1);
            helper.assertTrue(outputCount(f, casings()[1].asItem()) == 1, "Pending output delivered once");
            ticks(f, 10);
            helper.assertTrue(outputCount(f, casings()[1].asItem()) == 1 && !f.logic().hasPendingReport(), "No duplicate or survey book");
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void hatchLimitsAndParallelCrafts(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 110000);
        var hatch = hatch(helper, f, 2);
        helper.runAfterDelay(6, () -> {
            WorldEaterRunTests.form(helper, f);
            helper.assertTrue(f.controller().getParallelHatch().isPresent(), "Standard hatch attached while DORMANT");
            active(f.logic());
            f.logic().setWorkingEnabled(true);
            hatch.setCurrentParallel(2);
            feed(f, 1, 4, 64);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.logic().getLastRecipe() != null && f.logic().getLastRecipe().parallels == 2
                    && f.logic().getDuration() == 100, "Current hatch setting controls parallels without speedup");
            helper.assertTrue(!f.logic().getLastRecipe().hasTick(), "Standard hatch adds no synthetic zero-EU tick capability");
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 2
                    && f.input().getInventory().getStackInSlot(1).getCount() == 32, "Two complete payments");
            f.input().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            ticks(f, 45);
            var holder = (BlockEntity) f.controller().getHolder();
            holder.load(holder.saveWithFullMetadata().copy());
            ticks(f, 55);
            helper.assertTrue(outputCount(f, casings()[2].asItem()) == 2, "Parallel output survives reload");
            f.logic().resetRecipeLogic();
            hatch.setCurrentParallel(4);
            feed(f, 1, 1, 31);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.logic().getLastRecipe() != null && f.logic().getLastRecipe().parallels == 1
                    && f.input().getInventory().getStackInSlot(1).getCount() == 15, "Input limit never pays partial parallel");
            ticks(f, 100);
            helper.assertTrue(outputCount(f, casings()[2].asItem()) == 3, "Input limited output");
            f.logic().resetRecipeLogic();
            WorldEaterRunTests.blockOutput(f);
            f.output().getInventory().setStackInSlot(0, new ItemStack(casings()[2].asItem(), 63));
            feed(f, 1, 4, 64);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.logic().getLastRecipe() != null && f.logic().getLastRecipe().parallels == 1, "Output capacity limits parallel count");
            f.input().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            ticks(f, 100);
            helper.assertTrue(outputCount(f, casings()[2].asItem()) == 64, "No output overflow");
            f.logic().setWorkingEnabled(false);
            f.controller().onStructureInvalid();
            hatch(helper, f, 3);
            helper.assertTrue(!f.controller().checkPattern(), "Second hatch rejected while ACTIVE");
            helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.shellPositions(f.controller()).get(3), casings()[0].defaultBlockState());
            helper.assertTrue(f.controller().checkPattern(), "One hatch remains valid while ACTIVE");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void parallelHatchNeverMultipliesSurvey(GameTestHelper helper) {
        var f = WorldEaterRunTests.fixture(helper, 115000);
        hatch(helper, f, 2).setCurrentParallel(4);
        helper.runAfterDelay(6, () -> {
            WorldEaterRunTests.form(helper, f);
            f.input().getInventory().setStackInSlot(0, GTItems.SENSOR_IV.asStack(4));
            f.logic().setWorkingEnabled(true);
            for (int i = 0; i < 3; i++) {
                var invalid = WorldEaterRunTests.recipe();
                if (i == 0) invalid.parallels = 2;
                if (i == 1) invalid.batchParallels = 2;
                if (i == 2) invalid.subtickParallels = 2;
                f.logic().setupRecipe(invalid);
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 4, "Reject pre-multiplied survey");
            }
            int cx = f.controller().getPos().getX() >> 4, cz = f.controller().getPos().getZ() >> 4;
            for (int x = cx - 4; x <= cx + 4; x++) for (int z = cz - 4; z <= cz + 4; z++) helper.getLevel().getChunk(x, z);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.logic().getLastRecipe().parallels == 1 && f.logic().getDuration() == 2400
                    && f.input().getInventory().getStackInSlot(0).getCount() == 3, "Hatch permits a single survey with exact payment");
            WorldEaterRunTests.blockOutput(f);
            ticks(f, 2399);
            helper.assertTrue(!f.logic().hasPendingReport(), "Survey cannot finish early");
            ticks(f, 1);
            helper.assertTrue(f.logic().hasPendingReport() && f.logic().getProgress() == 2400 && f.logic().getElapsedDelays() == 0, "Unchanged full survey duration");
            // Completed report survives invalidation, old-frame rejection, and rebuild.
            var holder = (BlockEntity) f.controller().getHolder();
            holder.load(holder.saveWithFullMetadata().copy());
            f.controller().onStructureInvalid();
            var pos = WorldEaterStructureTests.shellPositions(f.controller()).get(3);
            helper.getLevel().setBlockAndUpdate(pos, com.gregtechceu.gtceu.common.data.GTBlocks.MACHINE_CASING_UHV.getDefaultState());
            helper.assertTrue(!f.controller().checkPattern(), "UHV cannot reform saved controller");
            helper.getLevel().setBlockAndUpdate(pos, casings()[0].defaultBlockState());
            helper.assertTrue(f.controller().checkPatternWithLock(), "Black rebuild forms");
            f.controller().onStructureFormed();
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            ticks(f, 1);
            helper.assertTrue(outputCount(f, Items.WRITTEN_BOOK) == 1 && f.input().getInventory().getStackInSlot(0).getCount() == 3, "One retained report, surplus sensors unchanged");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Below-threshold survey stays dormant");
            f.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
