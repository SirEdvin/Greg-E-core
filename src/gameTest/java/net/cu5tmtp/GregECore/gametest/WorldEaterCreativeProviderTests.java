package net.cu5tmtp.GregECore.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEBlocks;
import appeng.me.helpers.BaseActionSource;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.CreativeDemandPatternProviderMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterDemands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;

/** AE2-only fixtures for the creative demand pattern provider; invoked by guarded entry points. */
final class WorldEaterCreativeProviderTests {
    private static final int DELAY = CreativeDemandPatternProviderMachine.DELAY;

    record Fixture(WorldEaterMETests.Fixture me, CreativeDemandPatternProviderMachine provider) {
        BlockPos cell() { return me.hatch().getPos().north(); }
        appeng.api.storage.MEStorage storage() { return me.hatch().getMainNode().getGrid().getStorageService().getInventory(); }
    }

    /** Replaces the native pattern provider of the ME fixture with the creative one, facing the drive. */
    static Fixture fixture(GameTestHelper helper, int location) {
        var me = WorldEaterMETests.fixture(helper, location);
        var pos = me.hatch().getPos().north(2).east();
        helper.getLevel().setBlockAndUpdate(pos, CreativeDemandPatternProviderMachine.PROVIDER.defaultBlockState());
        var provider = (CreativeDemandPatternProviderMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        provider.setFrontFacing(Direction.WEST);
        return new Fixture(me, provider);
    }

    static void ready(GameTestHelper helper, Fixture f, Runnable action) {
        WorldEaterMETests.awaitNetwork(helper, f.me(), 0, () -> WorldEaterDemandTests.await(helper,
                () -> f.provider().getMainNode().isOnline() && f.provider().getMainNode().getGrid() == f.me().hatch().getMainNode().getGrid()
                        && craftable(f, WorldEaterMETests.entry().stack(1).getItem()), 0, action));
    }

    static boolean craftable(Fixture f, net.minecraft.world.item.Item item) {
        return f.me().hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(item));
    }

    static long amount(Fixture f, AEKey key) { return f.storage().extract(key, Long.MAX_VALUE, Actionable.SIMULATE, new BaseActionSource()); }

    static final AEItemKey IRON = AEItemKey.of(Items.IRON_INGOT);

    static String dump(appeng.api.stacks.KeyCounter counter) {
        var out = new StringBuilder();
        for (var e : counter) out.append(e.getKey()).append('=').append(e.getLongValue()).append(' ');
        return out.toString();
    }

    static long time(GameTestHelper helper) { return helper.getLevel().getGameTime(); }

    /** Polls once per tick (no wall-clock sleep) so timing assertions see every game tick. */
    static void ticks(GameTestHelper helper, BooleanSupplier condition, int waited, Runnable action) {
        if (condition.getAsBoolean()) { action.run(); return; }
        helper.assertTrue(waited < 400, "Condition reached within bound");
        helper.runAfterDelay(1, () -> ticks(helper, condition, waited + 1, action));
    }

    static Future<ICraftingPlan> calculate(Fixture f, AEKey key, long amount) {
        return f.me().hatch().getMainNode().getGrid().getCraftingService().beginCraftingCalculation(f.provider().getLevel(),
                () -> f.provider().getActionSource(), key, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
    }

    static void submit(GameTestHelper helper, Fixture f, AEKey key, long amount, Runnable action) {
        var calculation = calculate(f, key, amount);
        WorldEaterDemandTests.await(helper, calculation::isDone, 0, () -> {
            try {
                var plan = calculation.get();
                helper.assertTrue(!plan.simulation(), "Plan with enough iron is complete");
                var result = f.me().hatch().getMainNode().getGrid().getCraftingService().submitJob(plan, null, null, false, f.provider().getActionSource());
                helper.assertTrue(result.successful(), "Job submitted to the fixture CPU");
            } catch (Exception e) { throw new AssertionError(e); }
            action.run();
        });
    }

    static void patternsDeliveryAndIronCost(GameTestHelper helper) {
        var f = fixture(helper, 334000);
        ready(helper, f, () -> {
            var crafting = f.me().hatch().getMainNode().getGrid().getCraftingService();
            for (var entry : WorldEaterDemands.pool())
                helper.assertTrue(crafting.isCraftable(AEItemKey.of(entry.stack(1))), "Every demand pool item is craftable: " + entry.item());
            var patterns = CreativeDemandPatternProviderMachine.patterns(helper.getLevel());
            helper.assertTrue(patterns.size() == WorldEaterDemands.pool().size(), "Exactly one pattern per demand pool item");
            for (var pattern : patterns) {
                helper.assertTrue(pattern.getInputs().length == 1 && pattern.getInputs()[0].getMultiplier() == 1
                        && pattern.getInputs()[0].getPossibleInputs().length == 1
                        && pattern.getInputs()[0].getPossibleInputs()[0].what().equals(IRON)
                        && pattern.getInputs()[0].getPossibleInputs()[0].amount() == 1, "Only input is one Iron Ingot");
                helper.assertTrue(pattern.getOutputs().length == 1 && pattern.getOutputs()[0].amount() == 1, "Only output is one demand item");
            }
            helper.assertTrue(!craftable(f, Items.DIAMOND), "Items outside the pool are not offered");

            var key = AEItemKey.of(WorldEaterMETests.entry().stack(1));
            var missing = calculate(f, key, 2);
            WorldEaterDemandTests.await(helper, missing::isDone, 0, () -> {
                try {
                    var plan = missing.get();
                    helper.assertTrue(plan.simulation() && plan.missingItems().get(IRON) == 2, "Without iron AE2 reports the iron shortage: simulation=" + plan.simulation() + " missing=" + dump(plan.missingItems()) + " used=" + dump(plan.usedItems()) + " iron=" + amount(f, IRON));
                } catch (Exception e) { throw new AssertionError(e); }
                f.storage().insert(IRON, 5, Actionable.MODULATE, new BaseActionSource());
                submit(helper, f, key, 3, () -> ticks(helper, () -> f.provider().pendingDeliveries() > 0, 0, () -> {
                    long firstPush = time(helper);
                    int[] maxPending = {0};
                    // Push happened this tick or the previous one, so nothing may arrive before firstPush + DELAY - 1.
                    ticks(helper, () -> {
                        maxPending[0] = Math.max(maxPending[0], f.provider().pendingDeliveries());
                        if (time(helper) < firstPush + DELAY - 1)
                            helper.assertTrue(amount(f, key) == 0, "No output before the 2 second delay");
                        return amount(f, key) == 3 && f.provider().pendingDeliveries() == 0 && WorldEaterMETests.jobs(f.me()) == 0;
                    }, 0, () -> {
                        helper.assertTrue(time(helper) <= firstPush + DELAY + 10, "Outputs arrive shortly after the delay");
                        helper.assertTrue(maxPending[0] >= 2, "Overlapping pushes are all accepted");
                        helper.assertTrue(amount(f, IRON) == 2, "Exactly one iron per produced item");
                        offlineDelivery(helper, f, key);
                    });
                }));
            });
        });
    }

    private static void offlineDelivery(GameTestHelper helper, Fixture f, AEItemKey key) {
        f.storage().extract(key, 100, Actionable.MODULATE, new BaseActionSource());
        submit(helper, f, key, 1, () -> ticks(helper, () -> f.provider().pendingDeliveries() == 1, 0, () -> {
            long push = time(helper);
            helper.getLevel().removeBlock(f.cell(), false);
            ticks(helper, () -> time(helper) > push + DELAY + 5, 0, () -> {
                helper.assertTrue(!f.provider().getMainNode().isPowered(), "Network lost power");
                helper.assertTrue(f.provider().pendingDeliveries() == 1, "Due delivery waits while the network is unavailable");
                helper.assertTrue(!craftable(f, WorldEaterMETests.entry().stack(1).getItem()) && f.provider().getAvailablePatterns().isEmpty(), "Unpowered provider offers no patterns");
                helper.getLevel().setBlockAndUpdate(f.cell(), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
                ticks(helper, () -> f.provider().pendingDeliveries() == 0 && amount(f, key) == 1 && WorldEaterMETests.jobs(f.me()) == 0, 0, () -> {
                    helper.assertTrue(craftable(f, WorldEaterMETests.entry().stack(1).getItem()), "Patterns return with power");
                    helper.succeed();
                });
            });
        }));
    }

    static void reloadAndRemoval(GameTestHelper helper) {
        var f = fixture(helper, 335000);
        ready(helper, f, () -> {
            var key = AEItemKey.of(WorldEaterMETests.entry().stack(1));
            f.storage().insert(IRON, 10, Actionable.MODULATE, new BaseActionSource());
            submit(helper, f, key, 1, () -> ticks(helper, () -> f.provider().pendingDeliveries() == 1, 0, () -> {
                var holder = (BlockEntity) f.provider().getHolder();
                holder.load(holder.saveWithFullMetadata().copy());
                helper.assertTrue(f.provider().pendingDeliveries() == 1, "Pending delivery survives save/load");
                ticks(helper, () -> f.provider().pendingDeliveries() == 0 && WorldEaterMETests.jobs(f.me()) == 0, 0, () -> {
                    helper.assertTrue(amount(f, key) == 1, "Reloaded delivery arrives exactly once and completes the job");
                    f.storage().extract(key, 100, Actionable.MODULATE, new BaseActionSource());
                    submit(helper, f, key, 1, () -> ticks(helper, () -> f.provider().pendingDeliveries() == 1, 0, () -> {
                        var pos = f.provider().getPos();
                        long push = time(helper);
                        helper.getLevel().destroyBlock(pos, true);
                        ticks(helper, () -> time(helper) > push + DELAY + 5, 0, () -> {
                            helper.assertTrue(amount(f, key) == 0, "Removed provider never delivers");
                            helper.assertTrue(WorldEaterDemandTests.drops(helper, pos, WorldEaterMETests.entry().item()) == 0, "Pending outputs never drop");
                            helper.assertTrue(WorldEaterDemandTests.drops(helper, pos, CreativeDemandPatternProviderMachine.PROVIDER.getItem()) == 1, "Provider drops as itself");
                            helper.succeed();
                        });
                    }));
                });
            }));
        });
    }

    static void demandHatchPaidThroughProvider(GameTestHelper helper) {
        var f = fixture(helper, 336000);
        ready(helper, f, () -> {
            var t = f.me().tamer();
            WorldEaterDemandTests.prepare(helper, t);
            f.storage().insert(IRON, 10, Actionable.MODULATE, new BaseActionSource());
            WorldEaterMETests.startExact(helper, f.me());
            ticks(helper, () -> WorldEaterMETests.jobs(f.me()) == 1, 0, () -> {
                long submitted = time(helper);
                ticks(helper, () -> t.logic().getDemand().isEmpty(), 0, () -> {
                    helper.assertTrue(time(helper) >= submitted + DELAY - 1, "Demand paid no earlier than the provider delay");
                    helper.assertTrue(amount(f, IRON) == 7, "Iron reduced by exactly the demand amount");
                    helper.assertTrue(WorldEaterCasingTests.outputCount(t, WorldEaterDemandTests.reward().getItem()) == 1, "Run rewarded once");
                    t.logic().setWorkingEnabled(false);
                    helper.succeed();
                });
            });
        });
    }
}
