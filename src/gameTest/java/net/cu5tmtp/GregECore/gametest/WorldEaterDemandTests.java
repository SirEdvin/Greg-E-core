package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterDemands;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterDemandTests {
    static WorldEaterRunTests.Fixture fixture(GameTestHelper helper, int location, boolean safety) {
        var f = IntentionTamerTests.fixture(helper, location);
        helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(f.controller()),
                (safety ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).defaultBlockState());
        return f;
    }

    static ItemStack token(boolean test) { return ChemicalHelper.get(TagPrefix.gem, test ? GreggyItems.DESARIUM : GreggyItems.IMPLEMENTIUM); }
    static ItemStack reward() { return ChemicalHelper.get(TagPrefix.ingot, GreggyItems.TAMED_WORLD_EATER_ESSENSE); }
    static GTRecipe recipe(net.minecraft.world.level.Level level, boolean test) {
        return (GTRecipe) level.getRecipeManager().byKey(GregECore.id("world_eater_tamer/" + (test ? "preparation_test" : "essence_production"))).orElseThrow();
    }
    static void state(WorldEaterTamerRecipeLogic logic, String state) {
        logic.resetRecipeLogic();
        var tag = new CompoundTag(); var data = new CompoundTag();
        data.putInt("Version", 1); data.putString("State", state); tag.put("WorldEaterTamer", data);
        logic.loadCustomPersistedData(tag);
        logic.setWorkingEnabled(true);
    }
    static void start(GameTestHelper helper, WorldEaterRunTests.Fixture f, boolean test) {
        IntentionTamerTests.feed(f, token(test));
        f.logic().findAndHandleRecipe();
        helper.assertTrue(!f.logic().getDemand().isEmpty() && f.logic().getLastRecipe().id.equals(recipe(helper.getLevel(), test).id),
                "Native paid demand startup: " + f.logic().getFailureReasonMap());
        helper.assertTrue(f.input().getInventory().getStackInSlot(0).isEmpty(), "One token consumed");
        helper.assertTrue(f.logic().getDemand().getCount() >= 1 && f.logic().getDemand().getCount() <= 5, "Random startup quantity is bounded");
        helper.assertTrue(f.logic().getCounts().isEmpty() && f.logic().getCurrentChunk() == null && !f.logic().hasPendingReport(), "Not a survey");
    }
    static void field(Object target, Class<?> owner, String name, Object value) {
        try { var field = owner.getDeclaredField(name); field.setAccessible(true); field.set(target, value); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    static void demand(WorldEaterRunTests.Fixture f, WorldEaterDemands.Entry entry, int count) {
        field(f.logic(), WorldEaterTamerRecipeLogic.class, "demand", entry.stack(count));
        field(f.logic(), WorldEaterTamerRecipeLogic.class, "demandTier", entry.tier());
        field(f.logic(), RecipeLogic.class, "duration", entry.deadline());
    }
    static void reload(WorldEaterRunTests.Fixture f) {
        var holder = (BlockEntity) f.controller().getHolder();
        holder.load(holder.saveWithFullMetadata().copy());
    }
    static boolean candidate(WorldEaterTamerRecipeLogic logic, GTRecipe recipe) {
        try {
            var method = WorldEaterTamerRecipeLogic.class.getDeclaredMethod("checkRecipe", GTRecipe.class);
            method.setAccessible(true);
            return ((com.gregtechceu.gtceu.api.recipe.ActionResult) method.invoke(logic, recipe)).isSuccess();
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    static void pay(WorldEaterRunTests.Fixture f) {
        f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
        f.logic().serverTick();
    }
    static void prepare(GameTestHelper helper, WorldEaterRunTests.Fixture f) {
        IntentionTamerTests.form(helper, f); start(helper, f, true); pay(f);
        helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Test permanently prepares");
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void stateHardwareMetadataAndEmptyPoolGates(GameTestHelper helper) {
        var f = fixture(helper, 280000, true);
        helper.runAfterDelay(8, () -> {
            WorldEaterRunTests.form(helper, f);
            for (String state : new String[]{"DORMANT", "ACTIVE", "PREPARED"}) {
                state(f.logic(), state);
                for (boolean test : new boolean[]{true, false}) {
                    IntentionTamerTests.feed(f, token(test));
                    var original = recipe(helper.getLevel(), test);
                    boolean allowed = state.equals(test ? "ACTIVE" : "PREPARED");
                    helper.assertTrue(candidate(f.logic(), original) == allowed, "Exact progression gate " + state + "/" + test);
                    helper.assertTrue(f.logic().getDemand().isEmpty() && f.input().getInventory().getStackInSlot(0).getCount() == 1, "Candidate checks do not pay/select");
                    for (int invalid = 0; invalid < 14; invalid++) {
                        var r = original.copy(); r.data = original.data.copy();
                        switch (invalid) {
                            case 0 -> r.data.remove("required_state");
                            case 1 -> r.data.putString("required_state", "UNKNOWN");
                            case 2 -> r.data.remove("required_casing_tier");
                            case 3 -> r.data.putString("required_casing_tier", "2");
                            case 4 -> r.data.putDouble("required_casing_tier", 2.5);
                            case 5 -> r.parallels = 2;
                            case 6 -> r.batchParallels = 2;
                            case 7 -> r.subtickParallels = 2;
                            case 8 -> r.ocLevel = 1;
                            case 9 -> r.duration++;
                            case 10 -> r.data.putString("operation", "unsupported");
                            case 11 -> r.inputs.clear();
                            case 12 -> r.tickInputs.put(ItemRecipeCapability.CAP, new java.util.ArrayList<>(original.getInputContents(ItemRecipeCapability.CAP)));
                            default -> r.tickOutputs.put(ItemRecipeCapability.CAP, new java.util.ArrayList<>(original.getInputContents(ItemRecipeCapability.CAP)));
                        }
                        f.logic().setupRecipe(r);
                        helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1 && f.logic().getDemand().isEmpty(), "Invalid startup does not pay: " + invalid);
                    }
                }
            }
            state(f.logic(), "ACTIVE"); IntentionTamerTests.feed(f, token(true));
            var pool = WorldEaterDemands.pool();
            try {
                field(null, WorldEaterDemands.class, "pool", java.util.List.of());
                f.logic().setupRecipe(recipe(helper.getLevel(), true));
                helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Empty pool rejects before payment");
            } finally { field(null, WorldEaterDemands.class, "pool", pool); }
            f.controller().onStructureInvalid();
            f.logic().setupRecipe(recipe(helper.getLevel(), true));
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1, "Unformed rejects");
            WorldEaterStructureTests.colorFrame(f.controller(), 0); WorldEaterRunTests.form(helper, f);
            IntentionTamerTests.feed(f, token(true)); f.logic().setupRecipe(recipe(helper.getLevel(), true));
            helper.assertTrue(f.logic().getDemand().isEmpty(), "Tier one rejects");
            f.controller().onStructureInvalid(); WorldEaterStructureTests.colorFrame(f.controller(), -1);
            helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(f.controller()), GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.getDefaultState());
            WorldEaterRunTests.form(helper, f); IntentionTamerTests.feed(f, token(true));
            f.logic().setupRecipe(recipe(helper.getLevel(), true));
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1 && f.logic().getDemand().isEmpty(), "Intention required");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void immediatePaymentAcrossBusesAndEveryDeadline(GameTestHelper helper) {
        var f = fixture(helper, 281000, false);
        var second = WorldEaterStructureTests.bus(helper, WorldEaterStructureTests.shellPositions(f.controller()).get(3), GTMachines.ITEM_IMPORT_BUS[GTValues.LV]);
        WorldEaterCasingTests.hatch(helper, f, 2).setCurrentParallel(4);
        helper.runAfterDelay(8, () -> {
            IntentionTamerTests.form(helper, f);
            var tokens = token(true).copyWithCount(4); IntentionTamerTests.feed(f, tokens);
            f.input().setDistinct(true);
            second.setDistinct(true);
            f.logic().findAndHandleRecipe();
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 3 && f.logic().getLastRecipe().parallels == 1, "Parallel hatch still pays one token");
            var entry = WorldEaterDemands.pool().stream().filter(e -> e.tier() == 3).findFirst().orElseThrow();
            demand(f, entry, 3);
            f.input().getInventory().setStackInSlot(1, entry.stack(1));
            second.getInventory().setStackInSlot(0, entry.stack(1));
            second.getInventory().setStackInSlot(1, new ItemStack(Items.STONE, 5));
            f.logic().serverTick();
            helper.assertTrue(f.input().getInventory().getStackInSlot(1).getCount() == 1 && second.getInventory().getStackInSlot(0).getCount() == 1, "No partial demand payment");
            for (var wrong : java.util.List.of(WorldEaterDemands.pool().stream().filter(e -> e.tier() == 1).findFirst().orElseThrow().stack(5),
                    ChemicalHelper.get(TagPrefix.gear, com.gregtechceu.gtceu.common.data.GTMaterials.Steel, 5),
                    ChemicalHelper.get(TagPrefix.rotor, com.gregtechceu.gtceu.common.data.GTMaterials.Steel, 5))) {
                second.getInventory().setStackInSlot(2, wrong.copy()); f.logic().serverTick();
                helper.assertTrue(ItemStack.matches(wrong, second.getInventory().getStackInSlot(2)) && !f.logic().getDemand().isEmpty(), "Wrong tier/form cannot pay");
            }
            second.getInventory().setStackInSlot(0, entry.stack(4)); f.logic().serverTick();
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED && f.logic().getDemand().isEmpty(), "Immediate preparation");
            helper.assertTrue(f.input().getInventory().getStackInSlot(1).getCount() + second.getInventory().getStackInSlot(0).getCount() == 2
                    && second.getInventory().getStackInSlot(1).getCount() == 5, "Exact split payment, extras untouched");
            for (int slot = 0; slot < second.getInventory().getSlots(); slot++) second.getInventory().setStackInSlot(slot, ItemStack.EMPTY);
            for (int tier = 1; tier <= 10; tier++) {
                final int t = tier;
                var selected = WorldEaterDemands.pool().stream().filter(e -> e.tier() == t).findFirst();
                if (selected.isEmpty()) continue;
                for (int quantity = 1; quantity <= 5; quantity++) {
                    start(helper, f, false); demand(f, selected.get(), quantity);
                    field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1);
                    pay(f);
                    helper.assertTrue(f.logic().getDemand().isEmpty() && !f.logic().hasPendingReward(), "Final allowed tick pays before timeout");
                    helper.assertTrue(WorldEaterCasingTests.outputCount(f, reward().getItem()) == 1, "Exactly one essence");
                    f.logic().onRecipeFinish(); f.logic().onRecipeFinish();
                    helper.assertTrue(WorldEaterCasingTests.outputCount(f, reward().getItem()) == 1, "No repeated completion output");
                    f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
                }
            }
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void queuedRealTokensRestartOnNativeTicks(GameTestHelper helper) {
        var f = fixture(helper, 298000, true);
        helper.runAfterDelay(8, () -> {
            prepare(helper, f);
            f.input().getInventory().setStackInSlot(0, token(false).copyWithCount(3));
        });
        for (int run = 0; run < 3; run++) {
            final int completed = run;
            helper.runAfterDelay(48 + run * 40, () -> {
                helper.assertTrue(!f.logic().getDemand().isEmpty()
                        && f.input().getInventory().getStackInSlot(0).getCount() == 2 - completed,
                        "Queued real run starts without manual recipe search: " + completed);
                helper.assertTrue(WorldEaterCasingTests.outputCount(f, reward().getItem()) == completed,
                        "Exactly one output per previously paid run");
                // Inventory delivery only: native subscribed ticks must finish and restart.
                f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
            });
        }
        helper.runAfterDelay(168, () -> {
            helper.assertTrue(f.logic().getDemand().isEmpty() && f.logic().isIdle()
                    && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 3,
                    "Queue drains to three rewards without phantom work");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void queuedPreparationRewardAndSuspensionRespectNativeTicks(GameTestHelper helper) {
        var f = fixture(helper, 299000, true);
        helper.runAfterDelay(8, () -> {
            IntentionTamerTests.form(helper, f);
            f.input().getInventory().setStackInSlot(0, token(true));
            f.input().getInventory().setStackInSlot(2, token(false).copyWithCount(3));
        });
        helper.runAfterDelay(48, () -> {
            helper.assertTrue(!f.logic().getDemand().isEmpty() && f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE,
                    "Native preparation starts before queued real runs");
            f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
        });
        helper.runAfterDelay(88, () -> {
            helper.assertTrue(!f.logic().getDemand().isEmpty() && f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED
                    && f.input().getInventory().getStackInSlot(2).getCount() == 2, "Preparation automatically discovers queued real token");
            WorldEaterRunTests.blockOutput(f);
            f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
        });
        helper.runAfterDelay(128, () -> {
            helper.assertTrue(f.logic().hasPendingReward() && f.logic().getDemand().isEmpty()
                    && f.input().getInventory().getStackInSlot(2).getCount() == 2, "Blocked earned reward never consumes next token");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
        });
        helper.runAfterDelay(168, () -> {
            helper.assertTrue(!f.logic().getDemand().isEmpty() && f.input().getInventory().getStackInSlot(2).getCount() == 1
                    && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 1, "Reward delivery resumes the queue exactly once");
            f.logic().setSuspendAfterFinish(true);
            f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
        });
        helper.runAfterDelay(208, () -> {
            helper.assertTrue(f.logic().isSuspend() && f.logic().getDemand().isEmpty()
                    && f.input().getInventory().getStackInSlot(2).getCount() == 1
                    && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 2, "Suspend-after-finish preserves the queued token");
            f.logic().setWorkingEnabled(true);
        });
        helper.runAfterDelay(248, () -> {
            helper.assertTrue(!f.logic().getDemand().isEmpty() && f.input().getInventory().getStackInSlot(2).isEmpty(),
                    "Explicit resume starts the retained queued token");
            f.input().getInventory().setStackInSlot(1, f.logic().getDemand());
        });
        helper.runAfterDelay(288, () -> {
            helper.assertTrue(f.logic().getDemand().isEmpty() && !f.logic().hasPendingReward()
                    && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 3, "All three queued real runs settle exactly once");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void savedDemandAndEarnedOutputSurvivePauseAndReset(GameTestHelper helper) {
        var f = fixture(helper, 282000, true);
        helper.runAfterDelay(8, () -> {
            prepare(helper, f);
            IntentionTamerTests.feed(f, token(false)); WorldEaterRunTests.blockOutput(f);
            f.logic().setupRecipe(recipe(helper.getLevel(), false));
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).getCount() == 1 && f.logic().getDemand().isEmpty(), "Blocked startup does not pay or choose");
            f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            start(helper, f, false);
            var expected = f.logic().getDemand(); int duration = f.logic().getDuration();
            WorldEaterCasingTests.ticks(f, 7); f.logic().setWorkingEnabled(false); reload(f);
            WorldEaterCasingTests.ticks(f, 20);
            helper.assertTrue(ItemStack.matches(expected, f.logic().getDemand()) && f.logic().getDuration() == duration && f.logic().getProgress() == 7, "Same paused paid demand after native reload");
            f.logic().setWorkingEnabled(true); WorldEaterRunTests.blockOutput(f); pay(f);
            helper.assertTrue(f.logic().hasPendingReward() && f.logic().getDemand().isEmpty(), "Success clears demand before blocked delivery");
            reload(f); f.controller().onStructureInvalid();
            helper.assertTrue(f.logic().hasPendingReward(), "Earned result survives structure reset");
            WorldEaterRunTests.form(helper, f); IntentionTamerTests.feed(f, ItemStack.EMPTY);
            f.logic().setWorkingEnabled(false); f.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            f.logic().serverTick(); helper.assertTrue(f.logic().hasPendingReward(), "Suspended reward does not deliver");
            f.logic().setWorkingEnabled(true); f.logic().serverTick(); f.logic().onRecipeFinish();
            helper.assertTrue(WorldEaterCasingTests.outputCount(f, reward().getItem()) == 1 && !f.logic().hasPendingReward(), "Retained reward delivered once without repayment");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Permanent preparation");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void corruptDemandCancelsWithoutRevokingPreparation(GameTestHelper helper) {
        var f = fixture(helper, 283000, true);
        helper.runAfterDelay(8, () -> {
            prepare(helper, f);
            for (int invalid = 0; invalid < 14; invalid++) {
                start(helper, f, false);
                var tag = new CompoundTag(); f.logic().saveCustomPersistedData(tag, false);
                var data = tag.getCompound("WorldEaterTamer"); var run = data.getCompound("Demand");
                switch (invalid) {
                    case 0 -> run.getCompound("Item").putByte("Count", (byte) 0);
                    case 1 -> run.getCompound("Item").putByte("Count", (byte) 6);
                    case 2 -> run.putInt("Tier", 11);
                    case 3 -> run.putString("Operation", "unsupported");
                    case 4 -> run.getCompound("Item").putString("id", "gtceu:absent_optional_component");
                    case 5 -> data.remove("Demand");
                    case 6 -> run.putBoolean("RewardPending", true);
                    case 7 -> field(f.logic(), RecipeLogic.class, "duration", f.logic().getDuration() + 1);
                    case 8 -> run.putDouble("Tier", 1.5);
                    case 9 -> run.remove("RewardPending");
                    case 10 -> run.remove("Item");
                    case 11 -> run.getCompound("Item").putDouble("Count", 2.5);
                    case 12 -> {
                        run.putBoolean("RewardPending", true);
                        run.getCompound("Item").putString("id", "gtceu:absent_optional_component");
                    }
                    default -> { run.putBoolean("RewardPending", true); run.getCompound("Item").remove("Count"); }
                }
                f.logic().loadCustomPersistedData(tag.copy()); f.logic().onRecipeFinish();
                helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED && f.logic().getDemand().isEmpty()
                        && !f.logic().hasPendingReward() && f.logic().getLastRecipe() == null, "Corrupt work cancels independently: " + invalid);
                helper.assertTrue(WorldEaterCasingTests.outputCount(f, reward().getItem()) == 0 && f.controller().getIntention() != IntentionHatchPartMachine.Intention.NONE, "No fabricated reward or punishment");
            }
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void timeoutOnlySafetyAndHarmlessInterruption(GameTestHelper helper) {
        var f = fixture(helper, 284000, true);
        var hatch = WorldEaterStructureTests.intentionPosition(f.controller());
        helper.runAfterDelay(8, () -> {
            IntentionTamerTests.form(helper, f); start(helper, f, true);
            field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1); f.logic().serverTick();
            helper.assertTrue(f.logic().getDemand().isEmpty() && f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE
                    && !helper.getLevel().isEmptyBlock(hatch), "Test timeout harmless");
            start(helper, f, true); pay(f);
            for (int interrupt = 0; interrupt < 3; interrupt++) {
                start(helper, f, false);
                if (interrupt == 0) f.logic().interruptRecipe();
                else if (interrupt == 1) f.controller().onStructureInvalid();
                else {
                    f.logic().getLastRecipe().data.putString("required_state", "ACTIVE"); f.logic().serverTick();
                }
                f.logic().onRecipeFinish(); helper.assertTrue(!helper.getLevel().isEmptyBlock(hatch), "Interruption cannot punish");
                if (!f.controller().isFormed()) WorldEaterRunTests.form(helper, f);
                helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Interruption retains unlock");
            }
            start(helper, f, false);
            var neighbor = hatch.south(); helper.getLevel().setBlockAndUpdate(neighbor, Blocks.STONE.defaultBlockState());
            var entity = new net.minecraft.world.entity.decoration.ArmorStand(helper.getLevel(), hatch.getX() + .5, hatch.getY() + 1, hatch.getZ() + .5);
            helper.getLevel().addFreshEntity(entity); float health = entity.getHealth();
            var before = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
            for (var pos : WorldEaterStructureTests.shellPositions(f.controller())) before.put(pos, helper.getLevel().getBlockState(pos));
            field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1); f.logic().serverTick(); f.logic().onRecipeFinish();
            helper.assertTrue(helper.getLevel().isEmptyBlock(hatch) && helper.getLevel().getBlockState(neighbor).is(Blocks.STONE)
                    && entity.isAlive() && entity.getHealth() == health, "Only Safety block removed; no collateral or entity damage");
            for (var e : before.entrySet()) if (!e.getKey().equals(hatch)) helper.assertTrue(helper.getLevel().getBlockState(e.getKey()).equals(e.getValue()), "Other frame blocks unchanged");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 0, "Timeout retains PREPARED without reward");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void braveryNearestOriginAndOneVictim(GameTestHelper helper) {
        var f = fixture(helper, 285000, false);
        var a = f.controller().getPos().east(8); var b = a.east(4);
        helper.getLevel().setBlockAndUpdate(a, GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
        helper.getLevel().setBlockAndUpdate(b, GTMachines.ELECTRIC_FURNACE[GTValues.LV].defaultBlockState());
        helper.runAfterDelay(8, () -> {
            prepare(helper, f);
            helper.assertTrue(f.logic().findBraveryVictim(a).getPos().equals(a) && f.logic().findBraveryVictim(b).getPos().equals(b), "Random origin controls nearest search");
            helper.assertTrue(f.logic().findBraveryVictim(a.east(2)).getPos().equals(a), "Stable coordinate tie");
            start(helper, f, false); field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1);
            f.logic().serverTick(); f.logic().onRecipeFinish();
            helper.assertTrue(helper.getLevel().isEmptyBlock(a) != helper.getLevel().isEmptyBlock(b), "Exactly one surrounding victim");
            helper.assertTrue(!helper.getLevel().isEmptyBlock(WorldEaterStructureTests.intentionPosition(f.controller())) && f.controller().isFormed(), "Bravery and frame survive");
            f.logic().onRecipeFinish(); helper.assertTrue(helper.getLevel().isEmptyBlock(a) != helper.getLevel().isEmptyBlock(b), "No second victim on stale finish");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void preparedRetainsCasingParallelAndBraveryCrafts(GameTestHelper helper) {
        var f = fixture(helper, 286000, false);
        WorldEaterCasingTests.hatch(helper, f, 2).setCurrentParallel(4);
        helper.runAfterDelay(8, () -> {
            prepare(helper, f);
            WorldEaterCasingTests.feed(f, 0, 2, 32); f.logic().findAndHandleRecipe();
            helper.assertTrue(f.logic().getLastRecipe().parallels == 2 && f.logic().getDuration() == 100, "PREPARED casing parallels");
            WorldEaterCasingTests.ticks(f, 100);
            helper.assertTrue(WorldEaterCasingTests.outputCount(f, GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.asItem()) == 2, "PREPARED casing output");
            IntentionTamerTests.feed(f, IntentionRecipeTests.book(25)); IntentionTamerTests.start(helper, f);
            WorldEaterCasingTests.ticks(f, 1200);
            helper.assertTrue(WorldEaterCasingTests.outputCount(f, IntentionHatchPartMachine.BRAVERY.getItem()) == 1 && f.logic().getDemand().isEmpty(), "PREPARED ordinary Bravery craft");
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED, "Older crafts preserve unlock");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty")
    @SuppressWarnings("unchecked")
    public static void demandRecipeSerializationAndHelp(GameTestHelper helper) {
        for (boolean test : new boolean[]{true, false}) {
            var r = recipe(helper.getLevel(), test);
            helper.assertTrue(WorldEaterTamerRecipeLogic.isDemandRecipe(r), "Loaded exact power-free single recipe");
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                var serializer = (net.minecraft.world.item.crafting.RecipeSerializer<GTRecipe>) r.getSerializer();
                serializer.toNetwork(buffer, r);
                helper.assertTrue(WorldEaterTamerRecipeLogic.isDemandRecipe(serializer.fromNetwork(r.id, buffer)), "Network round trip");
            } finally { buffer.release(); }
            var captions = r.recipeType.getDataInfos().stream().map(info -> info.apply(r.data)).toList();
            helper.assertTrue(captions.stream().noneMatch(s -> s == null || s.isBlank())
                    && captions.stream().anyMatch(s -> s.contains("PREPARED"))
                    && captions.stream().anyMatch(s -> s.contains("immediately"))
                    && captions.stream().anyMatch(s -> s.contains("Deadline")), "Readable outcome and deadline captions");
        }
        for (var hatch : java.util.List.of(IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY)) {
            var text = new ItemStack(hatch.getItem()).getTooltipLines(null, net.minecraft.world.item.TooltipFlag.Default.NORMAL)
                    .stream().map(net.minecraft.network.chat.Component::getString).reduce("", (a, b) -> a + " " + b);
            helper.assertTrue(text.contains("timeout") && text.contains("No collateral") && text.contains("cancellations")
                    && text.contains("lower rear edge") && !text.contains("future recipes") && !text.contains("five"), "Implemented intention help: " + text);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void braveryBoundsEmptyRegionAndControllerWithoutCollateral(GameTestHelper helper) {
        var f = fixture(helper, 287000, false);
        var other = WorldEaterRunTests.fixture(helper, 287010);
        var level = helper.getLevel();
        int cx = f.controller().getPos().getX() >> 4, cz = f.controller().getPos().getZ() >> 4;
        var low = new BlockPos((cx - 4) * 16, level.getMinBuildHeight(), (cz - 4) * 16);
        var high = new BlockPos((cx + 4) * 16 + 15, level.getMaxBuildHeight() - 1, (cz + 4) * 16 + 15);
        var outside = new BlockPos((cx + 5) * 16, low.getY(), low.getZ());
        for (var pos : java.util.List.of(low, high, outside)) {
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
            level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            level.setBlockAndUpdate(pos, GTMachines.MACERATOR[GTValues.LV].defaultBlockState());
        }
        helper.runAfterDelay(8, () -> {
            prepare(helper, f); WorldEaterRunTests.form(helper, other); other.logic().setWorkingEnabled(false);
            for (var pos : java.util.List.of(low, high)) {
                var found = f.logic().findBraveryVictim(pos);
                helper.assertTrue(found != null && found.getPos().equals(pos), "Inclusive boundary " + pos + "; found " + (found == null ? null : found.getPos()));
            }
            helper.assertTrue(!f.logic().findBraveryVictim(outside).getPos().equals(outside), "Outside loaded machine excluded");
            var lostVictim = MetaMachine.getMachine(level, low);
            level.removeBlock(low, false); level.removeBlock(high, false);
            try {
                var destroy = WorldEaterTamerRecipeLogic.class.getDeclaredMethod("destroyVictim", MetaMachine.class);
                destroy.setAccessible(true); destroy.invoke(f.logic(), lostVictim);
            } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
            helper.assertTrue(f.logic().findBraveryVictim(other.controller().getPos()) == other.controller(), "Other controller eligible, parts excluded");
            var before = new java.util.HashMap<BlockPos, net.minecraft.world.level.block.state.BlockState>();
            for (var pos : WorldEaterStructureTests.shellPositions(other.controller())) before.put(pos, level.getBlockState(pos));
            var entity = new net.minecraft.world.entity.decoration.ArmorStand(level, other.controller().getPos().getX() + .5,
                    other.controller().getPos().getY() + 1, other.controller().getPos().getZ() + .5);
            level.addFreshEntity(entity); float health = entity.getHealth();
            start(helper, f, false); field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1); f.logic().serverTick();
            helper.assertTrue(level.isEmptyBlock(other.controller().getPos()) && entity.isAlive() && entity.getHealth() == health, "Only selected controller, no entity blast");
            for (var e : before.entrySet()) if (!e.getKey().equals(other.controller().getPos()))
                helper.assertTrue(level.getBlockState(e.getKey()).equals(e.getValue()), "Victim frame and parts remain intact; no fire");
            helper.assertTrue(f.logic().findBraveryVictim(f.controller().getPos()) == null, "Self and orphan parts excluded; empty region ends search");
            start(helper, f, false); field(f.logic(), RecipeLogic.class, "progress", f.logic().getDuration() - 1); f.logic().serverTick();
            helper.assertTrue(f.logic().getDemand().isEmpty() && f.controller().isFormed() && !level.isEmptyBlock(outside), "No victim means no deferred or out-of-bounds attack");
            f.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void actualFrameAndIntentionRemovalCancelHarmlessly(GameTestHelper helper) {
        var fixtures = new WorldEaterRunTests.Fixture[]{fixture(helper, 293000, true), fixture(helper, 294000, false)};
        helper.runAfterDelay(8, () -> {
            for (int i = 0; i < fixtures.length; i++) {
                var f = fixtures[i]; prepare(helper, f); start(helper, f, false);
                var entry = WorldEaterDemands.pool().get(0); demand(f, entry, 3);
                f.input().getInventory().setStackInSlot(1, entry.stack(1));
                var broken = i == 0 ? WorldEaterStructureTests.shellPositions(f.controller()).get(3)
                        : WorldEaterStructureTests.intentionPosition(f.controller());
                helper.getLevel().removeBlock(broken, false);
            }
            awaitHarmlessInvalidation(helper, fixtures, 0);
        });
    }

    private static void awaitHarmlessInvalidation(GameTestHelper helper, WorldEaterRunTests.Fixture[] fixtures, int waited) {
        if (java.util.Arrays.stream(fixtures).anyMatch(f -> f.controller().isFormed() || !f.logic().getDemand().isEmpty())) {
            helper.assertTrue(waited < 200, "Real block callbacks cancel unfinished demands");
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new AssertionError(e); }
            helper.runAfterDelay(1, () -> awaitHarmlessInvalidation(helper, fixtures, waited + 1));
            return;
        }
        for (var f : fixtures) {
            f.logic().onRecipeFinish();
            helper.assertTrue(f.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED
                    && !f.logic().hasPendingReward() && WorldEaterCasingTests.outputCount(f, reward().getItem()) == 0, "No reward, punishment, or progression loss");
            helper.assertTrue(f.input().getInventory().getStackInSlot(0).isEmpty()
                    && f.input().getInventory().getStackInSlot(1).getCount() == 1, "No token refund or partial demand loss");
            f.logic().setWorkingEnabled(false);
        }
        helper.assertTrue(!helper.getLevel().isEmptyBlock(WorldEaterStructureTests.intentionPosition(fixtures[0].controller())), "Safety survives deliberate frame break");
        helper.succeed();
    }
}
