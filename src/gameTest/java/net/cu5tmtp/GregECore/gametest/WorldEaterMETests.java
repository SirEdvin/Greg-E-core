package net.cu5tmtp.GregECore.gametest;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.blockentity.storage.DriveBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.me.helpers.BaseActionSource;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.MEDemandHatchPartMachine;
import net.minecraft.gametest.framework.GameTestHelper;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterDemands;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.api.networking.crafting.ICraftingPlan;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

/** AE2-only fixtures: invoked by a guarded common GameTest entry point. */
final class WorldEaterMETests {
    record Fixture(WorldEaterRunTests.Fixture tamer, MEDemandHatchPartMachine hatch) {}

    static Fixture fixture(GameTestHelper helper, int location) {
        var f = WorldEaterDemandTests.fixture(helper, location, true);
        var pos = WorldEaterStructureTests.shellPositions(f.controller()).get(2);
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, MEDemandHatchPartMachine.HATCH.defaultBlockState());
        var hatch = (MEDemandHatchPartMachine) MetaMachine.getMachine(level, pos);
        level.setBlockAndUpdate(pos.north(), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        level.setBlockAndUpdate(pos.north(2), AEBlocks.DRIVE.block().defaultBlockState());
        ((DriveBlockEntity) level.getBlockEntity(pos.north(2))).getInternalInventory().addItems(AEItems.ITEM_CELL_64K.stack());
        level.setBlockAndUpdate(pos.north(2).west(), AEBlocks.CRAFTING_STORAGE_64K.block().defaultBlockState());
        level.setBlockAndUpdate(pos.north(2).east(), AEBlocks.PATTERN_PROVIDER.block().defaultBlockState());
        level.setBlockAndUpdate(pos.north(2).east(2), net.minecraft.world.level.block.Blocks.CHEST.defaultBlockState());
        return new Fixture(f, hatch);
    }

    static void awaitNetwork(GameTestHelper helper, Fixture f, int waited, Runnable action) {
        if (!f.hatch().getMainNode().isOnline() || !f.hatch().getMainNode().isPowered()
                || f.hatch().getMainNode().getGrid().getCraftingService().getCpus().isEmpty()) {
            helper.assertTrue(waited < 200, "Native powered ME grid and crafting CPU become available");
            helper.runAfterDelay(1, () -> awaitNetwork(helper, f, waited + 1, action));
            return;
        }
        action.run();
    }

    static void realRequestAndExactStoragePayment(GameTestHelper helper) {
        var f = fixture(helper, 320000);
        awaitNetwork(helper, f, 0, () -> {
            var t = f.tamer();
            WorldEaterDemandTests.prepare(helper, t);
            helper.assertTrue(f.hatch().getSlotList().getConfigurableSlot(0).getConfig() == null, "Preparation never requests from ME");
            WorldEaterDemandTests.start(helper, t, false);
            var demand = t.logic().getDemand();
            var key = AEItemKey.of(demand);
            helper.assertTrue(new GenericStack(key, demand.getCount()).equals(f.hatch().getSlotList().getConfigurableSlot(0).getConfig()),
                    "Paid real demand configures the exact ghost request immediately");
            t.logic().serverTick();
            helper.assertTrue(!t.logic().getDemand().isEmpty(), "Ghost configuration is not inventory");
            var storage = f.hatch().getMainNode().getGrid().getStorageService().getInventory();
            storage.insert(key, demand.getCount() + 2, Actionable.MODULATE, new BaseActionSource());
            t.logic().serverTick();
            helper.assertTrue(t.logic().getDemand().isEmpty() && WorldEaterCasingTests.outputCount(t, WorldEaterDemandTests.reward().getItem()) == 1,
                    "Fresh ME stock settles once without waiting for the display cache");
            helper.assertTrue(storage.extract(key, 100, Actionable.SIMULATE, new BaseActionSource()) == 2, "Only the exact demand leaves actual ME storage");
            helper.assertTrue(f.hatch().getSlotList().getConfigurableSlot(0).getConfig() == null, "Paid request clears immediately");
            t.logic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    static WorldEaterDemands.Entry entry() { return WorldEaterDemands.pool().stream().filter(e -> e.tier() == 3).findFirst().orElseThrow(); }

    static void startExact(GameTestHelper helper, Fixture f) {
        startExact(helper, f, ItemStack.EMPTY);
    }

    private static void startExact(GameTestHelper helper, Fixture f, ItemStack localSupply) {
        var pool = WorldEaterDemands.pool();
        try {
            WorldEaterDemandTests.field(null, WorldEaterDemands.class, "pool", java.util.List.of(entry()));
            for (long seed = 0;; seed++) {
                var random = net.minecraft.util.RandomSource.create(seed);
                random.nextInt(1);
                if (random.nextInt(5) == 2) { helper.getLevel().random.setSeed(seed); break; }
            }
            // The common feed helper clears all local slots; install startup supply after it.
            IntentionTamerTests.feed(f.tamer(), WorldEaterDemandTests.token(false));
            f.tamer().input().getInventory().setStackInSlot(1, localSupply.copy());
            f.tamer().logic().findAndHandleRecipe();
            helper.assertTrue(f.tamer().input().getInventory().getStackInSlot(0).isEmpty(), "Exactly one real-run token consumed");
            helper.assertTrue(f.tamer().logic().getDemand().getCount() == 3, "Real startup selects deterministic three-item demand");
        } finally { WorldEaterDemandTests.field(null, WorldEaterDemands.class, "pool", pool); }
    }

    static long stock(Fixture f) {
        return f.hatch().getMainNode().getGrid().getStorageService().getInventory().extract(
                AEItemKey.of(entry().stack(1)), 1000, Actionable.SIMULATE, new BaseActionSource());
    }

    static void supply(Fixture f, long amount) {
        f.hatch().getMainNode().getGrid().getStorageService().getInventory().insert(
                AEItemKey.of(entry().stack(1)), amount, Actionable.MODULATE, new BaseActionSource());
    }

    static void paymentAliasingDeadlineAndCleanup(GameTestHelper helper) {
        var f = fixture(helper, 321000);
        var t = f.tamer();
        // Directly below the demand hatch, so both front faces join one grid.
        var busPos = WorldEaterStructureTests.shellPositions(t.controller()).get(4);
        helper.getLevel().setBlockAndUpdate(busPos, GTAEMachines.STOCKING_IMPORT_BUS_ME.defaultBlockState());
        var bus = (MEStockingBusPartMachine) MetaMachine.getMachine(helper.getLevel(), busPos);
        // The neighbouring front faces touch the same energy-cell/drive grid.
        helper.getLevel().setBlockAndUpdate(busPos.north(), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, t);
            await(helper, () -> bus.getMainNode().isOnline() && bus.getMainNode().getGrid() == f.hatch().getMainNode().getGrid(), 0, () -> {
                for (boolean distinct : new boolean[]{false, true}) {
                    bus.setDistinct(distinct);
                    bus.getSlotList().getConfigurableSlot(0).setConfig(new GenericStack(AEItemKey.of(entry().stack(1)), 1));
                    startExact(helper, f);
                    supply(f, 1);
                    t.input().getInventory().setStackInSlot(1, entry().stack(1));
                    t.logic().serverTick();
                    helper.assertTrue(stock(f) == 1 && t.input().getInventory().getStackInSlot(1).getCount() == 1
                            && t.logic().getDemand().getCount() == 3, "Aliased insufficient ME stock cannot partially pay: distinct=" + distinct);
                    supply(f, 3);
                    WorldEaterDemandTests.field(t.logic(), RecipeLogic.class, "progress", t.logic().getDuration() - 1);
                    t.logic().serverTick();
                    helper.assertTrue(t.logic().getDemand().isEmpty() && stock(f) + t.input().getInventory().getStackInSlot(1).getCount() == 2,
                            "Fresh final-tick supply pays exactly once across native input views");
                    helper.assertTrue(f.hatch().getRequest().isEmpty() && f.hatch().getSlotList().getConfigurableSlot(0).getConfig() == null,
                            "Payment clears both display and native request");
                    f.hatch().getMainNode().getGrid().getStorageService().getInventory().extract(AEItemKey.of(entry().stack(1)), 100, Actionable.MODULATE, new BaseActionSource());
                    t.input().getInventory().setStackInSlot(1, ItemStack.EMPTY);
                    t.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
                }
                startExact(helper, f);
                WorldEaterRunTests.blockOutput(t);
                supply(f, 4); t.logic().serverTick();
                helper.assertTrue(t.logic().hasPendingReward() && stock(f) == 1 && f.hatch().getRequest().isEmpty(), "Blocked earned reward has no request");
                WorldEaterDemandTests.reload(t); f.hatch().autoIO(); t.logic().serverTick();
                helper.assertTrue(t.logic().hasPendingReward() && stock(f) == 1 && f.hatch().getRequest().isEmpty(), "Reload cannot charge earned reward again");
                t.logic().setWorkingEnabled(false); helper.succeed();
            });
        });
    }

    static void await(GameTestHelper helper, BooleanSupplier condition, int waited, Runnable action) {
        WorldEaterDemandTests.await(helper, condition, waited, action);
    }

    static Object pending(Fixture f) {
        try { var field = MEDemandHatchPartMachine.class.getDeclaredField("pendingCraft"); field.setAccessible(true); return field.get(f.hatch()); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    @SuppressWarnings("unchecked")
    static Future<ICraftingPlan> calculation(Object pending) {
        try { var method = pending.getClass().getDeclaredMethod("calculation"); method.setAccessible(true); return (Future<ICraftingPlan>) method.invoke(pending); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }

    static void pattern(Fixture f) {
        var provider = (PatternProviderBlockEntity) f.hatch().getLevel().getBlockEntity(f.hatch().getPos().north(2).east());
        provider.getLogic().getPatternInv().addItems(PatternDetailsHelper.encodeProcessingPattern(
                new GenericStack[]{new GenericStack(AEItemKey.of(Items.COBBLESTONE), 1)},
                new GenericStack[]{new GenericStack(AEItemKey.of(entry().stack(1)), 1)}));
        f.hatch().getMainNode().getGrid().getStorageService().getInventory().insert(AEItemKey.of(Items.COBBLESTONE), 64,
                Actionable.MODULATE, new BaseActionSource());
    }

    static long jobs(Fixture f) { return f.hatch().getMainNode().getGrid().getCraftingService().getCpus().stream().filter(cpu -> cpu.getJobStatus() != null).count(); }

    private record Gate(Future<ICraftingPlan> real, CompletableFuture<ICraftingPlan> held) {}

    private static Gate hold(Fixture f) {
        var original = pending(f);
        if (original == null) throw new AssertionError("Startup calculation must exist before gating its completion");
        var held = new CompletableFuture<ICraftingPlan>();
        try {
            var components = original.getClass().getRecordComponents();
            var values = new Object[components.length];
            for (int i = 0; i < components.length; i++) {
                var accessor = components[i].getAccessor(); accessor.setAccessible(true);
                values[i] = i == components.length - 1 ? held : accessor.invoke(original);
            }
            var constructor = original.getClass().getDeclaredConstructors()[0]; constructor.setAccessible(true);
            WorldEaterDemandTests.field(f.hatch(), MEDemandHatchPartMachine.class, "pendingCraft", constructor.newInstance(values));
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
        return new Gate(calculation(original), held);
    }

    private static ICraftingPlan ready(Future<ICraftingPlan> future) {
        if (!future.isDone()) throw new AssertionError("Never wait for a running calculation on the server thread");
        try { return future.get(); } catch (Exception e) { throw new AssertionError(e); }
    }

    static void asynchronousRacesAndNoRetry(GameTestHelper helper) {
        var f = fixture(helper, 324000);
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, f.tamer()); pattern(f);
            await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0,
                    () -> race(helper, f, 0));
        });
    }

    private static void race(GameTestHelper helper, Fixture f, int scenario) {
        if (scenario == 9) { f.tamer().logic().setWorkingEnabled(false); helper.succeed(); return; }
        var t = f.tamer(); startExact(helper, f); var gate = hold(f);
        int before = t.logic().getProgress();
        t.logic().serverTick();
        helper.assertTrue(t.logic().getProgress() == before + 1 && !gate.held().isDone(), "Unfinished future does not block or extend processing");
        await(helper, gate.real()::isDone, 0, () -> {
            var plan = ready(gate.real());
            helper.assertTrue(!plan.simulation() && plan.finalOutput().amount() == 3, "Race uses a real craftable native startup plan");
            var grid = f.hatch().getMainNode().getGrid();
            switch (scenario) {
                case 0 -> supply(f, 3);
                case 1 -> supply(f, 1);
                case 2 -> WorldEaterDemandTests.pay(t);
                case 3 -> t.logic().interruptRecipe();
                case 4 -> WorldEaterDemandTests.reload(t);
                case 5 -> {
                    WorldEaterDemandTests.pay(t); startExact(helper, f);
                    var next = hold(f); next.held().cancel(false);
                }
                case 6 -> grid.getCraftingService().submitJob(plan, null, null, false, f.hatch().getActionSource());
                case 7 -> WorldEaterStructureTests.invalidated(t.controller());
                case 8 -> {
                    WorldEaterDemandTests.field(t.logic(), RecipeLogic.class, "progress", t.logic().getDuration() - 1);
                    t.logic().serverTick();
                }
                default -> throw new AssertionError(scenario);
            }
            gate.held().complete(plan);
            t.logic().serverTick();
            helper.assertTrue(jobs(f) == (scenario == 1 || scenario == 6 ? 1 : 0), "Guarded single submission for race " + scenario);
            helper.assertTrue(pending(f) == null, "No retained or replacement calculation for race " + scenario);
            if (scenario == 1) helper.assertTrue(grid.getCraftingService().getCpus().iterator().next().getJobStatus().crafting().amount() == 3,
                    "Partial arrival does not recalculate original shortage");
            for (var cpu : grid.getCraftingService().getCpus()) ((appeng.me.cluster.implementations.CraftingCPUCluster) cpu).cancel();
            for (int tick = 0; tick < 4; tick++) t.logic().serverTick();
            helper.assertTrue(jobs(f) == 0 && pending(f) == null, "External cancellation never retries run " + scenario);
            t.logic().interruptRecipe();
            grid.getStorageService().getInventory().extract(AEItemKey.of(entry().stack(1)), 100, Actionable.MODULATE, new BaseActionSource());
            t.input().getInventory().setStackInSlot(1, ItemStack.EMPTY);
            t.output().getInventory().setStackInSlot(0, ItemStack.EMPTY);
            if (scenario == 7) { helper.assertTrue(t.controller().checkPatternWithLock(), "Reform after interruption"); WorldEaterStructureTests.formed(t.controller()); }
            helper.runAfterDelay(4, () -> race(helper, f, scenario + 1));
        });
    }

    static void startupSkipsAndMatchingJobs(GameTestHelper helper) {
        var f = fixture(helper, 325000);
        awaitNetwork(helper, f, 0, () -> {
            var t = f.tamer(); WorldEaterDemandTests.prepare(helper, t);
            supply(f, 3); startExact(helper, f);
            helper.assertTrue(pending(f) == null, "Sufficient startup stock skips calculation");
            assertSavedOpportunity(helper, f);
            f.hatch().getMainNode().getGrid().getStorageService().getInventory().extract(AEItemKey.of(entry().stack(1)), 100, Actionable.MODULATE, new BaseActionSource());
            for (int i = 0; i < 4; i++) t.logic().serverTick();
            helper.assertTrue(pending(f) == null, "Stock disappearing cannot reopen startup");
            t.logic().interruptRecipe(); startExact(helper, f);
            var absentPattern = pending(f);
            await(helper, () -> pending(f) == null, 0, () -> {
                helper.assertTrue(absentPattern != null && jobs(f) == 0, "Missing pattern settles initial attempt without job");
                pattern(f);
                await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0, () -> {
                    helper.assertTrue(pending(f) == null && jobs(f) == 0, "Adding a pattern cannot retry old run");
                    t.logic().interruptRecipe();
                    var grid = f.hatch().getMainNode().getGrid();
                    var external = grid.getCraftingService().beginCraftingCalculation(helper.getLevel(), () -> f.hatch().getActionSource(),
                            AEItemKey.of(entry().stack(1)), 1, appeng.api.networking.crafting.CalculationStrategy.REPORT_MISSING_ITEMS);
                    await(helper, external::isDone, 0, () -> {
                        grid.getCraftingService().submitJob(ready(external), null, null, false, f.hatch().getActionSource());
                        helper.assertTrue(jobs(f) == 1, "Independent one-item job actually scheduled");
                        startExact(helper, f);
                        helper.assertTrue(pending(f) == null, "Existing final-output job suppresses startup before final dispatch, despite insufficient size");
                        for (var cpu : grid.getCraftingService().getCpus()) ((appeng.me.cluster.implementations.CraftingCPUCluster) cpu).cancel();
                        for (int i = 0; i < 5; i++) t.logic().serverTick();
                        helper.assertTrue(pending(f) == null && jobs(f) == 0, "Cancelled matching work never reopens skipped opportunity");
                        t.logic().setWorkingEnabled(false); helper.succeed();
                    });
                });
            });
        });
    }

    static void nativeCraftDeliveryAndFireAndForget(GameTestHelper helper) {
        var f = fixture(helper, 322000);
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, f.tamer()); pattern(f);
            await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0, () -> {
                startExact(helper, f);
                helper.assertTrue(pending(f) != null, "Paid startup begins one actual AE2 calculation");
                await(helper, () -> jobs(f) == 1, 0, () -> {
                    helper.assertTrue(pending(f) == null, "Submitted result is discarded, not retained as a job link");
                    assertSavedOpportunity(helper, f);
                    var cpu = f.hatch().getMainNode().getGrid().getCraftingService().getCpus().iterator().next();
                    helper.assertTrue(cpu.getJobStatus().crafting().amount() == 3, "Actual CPU receives exact startup deficit");
                    await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().getRequestedAmount(AEItemKey.of(entry().stack(1))) == 3, 0, () -> {
                        // Processing machine returns the output into the actual ME crafting/storage service.
                        supply(f, 5);
                        await(helper, () -> f.tamer().logic().getDemand().isEmpty(), 0, () -> {
                            helper.assertTrue(stock(f) == 2 && WorldEaterCasingTests.outputCount(f.tamer(), WorldEaterDemandTests.reward().getItem()) == 1,
                                    "Standalone CPU output reaches ME storage, pays demand once and leaves surplus");
                            f.hatch().getMainNode().getGrid().getStorageService().getInventory().extract(AEItemKey.of(entry().stack(1)), 100, Actionable.MODULATE, new BaseActionSource());
                            startExact(helper, f);
                            await(helper, () -> jobs(f) == 1, 0, () -> {
                                WorldEaterDemandTests.pay(f.tamer());
                                helper.assertTrue(jobs(f) == 1 && f.hatch().getRequest().isEmpty(), "Manual fulfillment leaves fired CPU job running");
                                var grid = f.hatch().getMainNode().getGrid();
                                helper.getLevel().destroyBlock(f.hatch().getPos(), true);
                                helper.assertTrue(grid.getCraftingService().getCpus().stream().anyMatch(c -> c.getJobStatus() != null), "Hatch removal never cancels fired jobs");
                                f.tamer().logic().setWorkingEnabled(false);
                                await(helper, () -> grid.getCraftingService().getRequestedAmount(AEItemKey.of(entry().stack(1))) == 3, 0, () -> {
                                    grid.getStorageService().getInventory().insert(AEItemKey.of(entry().stack(1)), 5, Actionable.MODULATE, new BaseActionSource());
                                    await(helper, () -> grid.getStorageService().getInventory().extract(AEItemKey.of(entry().stack(1)), 100, Actionable.SIMULATE, new BaseActionSource()) == 5, 0, () -> {
                                        helper.assertTrue(WorldEaterCasingTests.outputCount(f.tamer(), WorldEaterDemandTests.reward().getItem()) == 2,
                                                "Late job output and surplus remain in storage without rewarding the ended run");
                                        helper.succeed();
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private static void assertSavedOpportunity(GameTestHelper helper, Fixture f) {
        var demand = f.tamer().logic().getDemand();
        int progress = f.tamer().logic().getProgress(), deadline = f.tamer().logic().getDuration();
        var snapshot = ((net.minecraft.world.level.block.entity.BlockEntity) f.tamer().controller().getHolder()).saveWithFullMetadata().copy();
        ((net.minecraft.world.level.block.entity.BlockEntity) f.tamer().controller().getHolder()).load(snapshot);
        MEDemandHatchPartMachine.refresh(f.tamer().controller());
        helper.assertTrue(ItemStack.matches(demand, f.tamer().logic().getDemand()) && progress == f.tamer().logic().getProgress()
                && deadline == f.tamer().logic().getDuration() && pending(f) == null, "Fresh NBT replay preserves paid demand and elapsed deadline without replaying opportunity");
        var data = new CompoundTag(); f.tamer().logic().saveCustomPersistedData(data, false);
        var saved = data.getCompound("WorldEaterTamer").getCompound("Demand");
        helper.assertTrue(saved.getAllKeys().stream().noneMatch(k -> k.toLowerCase(java.util.Locale.ROOT).contains("job")
                || k.toLowerCase(java.util.Locale.ROOT).contains("link")), "Job ownership never persists");
    }

    static void legacyPersistenceAndReadOnlyControls(GameTestHelper helper) {
        var f = fixture(helper, 323000);
        awaitNetwork(helper, f, 0, () -> {
            var t = f.tamer(); WorldEaterDemandTests.prepare(helper, t); startExact(helper, f);
            var snapshot = ((net.minecraft.world.level.block.entity.BlockEntity) t.controller().getHolder()).saveWithFullMetadata();
            ((net.minecraft.world.level.block.entity.BlockEntity) t.controller().getHolder()).load(snapshot.copy());
            MEDemandHatchPartMachine.refresh(t.controller());
            helper.assertTrue(t.logic().getDemand().getCount() == 3 && t.logic().getProgressionState() == WorldEaterTamerRecipeLogic.State.PREPARED
                    && pending(f) == null, "Valid saved demand survives reload without retry");
            var stick = new ItemStack(Items.STICK); var tag = new CompoundTag(); var copied = new CompoundTag();
            copied.putBoolean("AutoPull", true); copied.putByte("GhostCircuit", (byte) 5); copied.putBoolean("DistinctBuses", false);
            tag.put("MEInputBus", copied); stick.setTag(tag);
            f.hatch().onDataStickUse(net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel()), stick);
            f.hatch().setAutoPull(true); f.hatch().autoIO();
            helper.assertTrue(!f.hatch().isAutoPull() && ItemStack.matches(f.hatch().getRequest(), t.logic().getDemand()), "Data stick cannot create unrelated stocking");
            t.logic().interruptRecipe(); f.hatch().autoIO();
            helper.assertTrue(f.hatch().getRequest().isEmpty() && pending(f) == null, "Cancellation removes display, virtual stock and unfinished calculation");
            t.logic().setWorkingEnabled(false); helper.succeed();
        });
    }

    static void startupShortageAndAbsentHatch(GameTestHelper helper) {
        var f = fixture(helper, 333000);
        var pos = WorldEaterStructureTests.shellPositions(f.tamer().controller()).get(4);
        helper.getLevel().setBlockAndUpdate(pos, GTAEMachines.STOCKING_IMPORT_BUS_ME.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.north(), AEBlocks.CREATIVE_ENERGY_CELL.block().defaultBlockState());
        var bus = (MEStockingBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, f.tamer()); pattern(f);
            await(helper, () -> bus.getMainNode().getGrid() == f.hatch().getMainNode().getGrid() && bus.getMainNode().isOnline()
                    && f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))),
                    0, () -> shortage(helper, f, bus, 0));
        });
    }

    private static void shortage(GameTestHelper helper, Fixture f, MEStockingBusPartMachine bus, int mode) {
        var t = f.tamer();
        if (mode == 3) {
            helper.getLevel().setBlockAndUpdate(f.hatch().getPos(), WorldEaterCasingTests.casings()[0].defaultBlockState());
            helper.assertTrue(t.controller().checkPatternWithLock(), "Frame without optional hatch reforms"); WorldEaterStructureTests.formed(t.controller());
            startExact(helper, f);
            WorldEaterDemandTests.pay(t);
            helper.assertTrue(t.logic().getDemand().isEmpty(), "Hatchless run still accepts ordinary local payment");
            t.logic().setWorkingEnabled(false); helper.succeed(); return;
        }
        bus.setDistinct(mode != 0); bus.setMinStackSize(mode == 2 ? 8 : 1);
        bus.getSlotList().getConfigurableSlot(0).setConfig(new GenericStack(AEItemKey.of(entry().stack(1)), 1));
        supply(f, 1);
        startExact(helper, f, entry().stack(1)); var gate = hold(f);
        await(helper, gate.real()::isDone, 0, () -> {
            helper.assertTrue(ready(gate.real()).finalOutput().amount() == 1, "Startup deficit counts one ME item plus one local item exactly once: " + mode);
            t.logic().interruptRecipe();
            var storage = f.hatch().getMainNode().getGrid().getStorageService().getInventory();
            helper.assertTrue(stock(f) == 1 && t.input().getInventory().getStackInSlot(1).getCount() == 1, "Planning never consumes partial supply");
            storage.extract(AEItemKey.of(entry().stack(1)), 100, Actionable.MODULATE, new BaseActionSource());
            t.input().getInventory().setStackInSlot(1, ItemStack.EMPTY);
            shortage(helper, f, bus, mode + 1);
        });
    }

    static void queuedRunsAndOfflineConsequences(GameTestHelper helper) {
        var f = fixture(helper, 330000);
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, f.tamer());
            f.tamer().input().getInventory().setStackInSlot(0, WorldEaterDemandTests.token(false).copyWithCount(3));
            queued(helper, f, 0);
        });
    }

    private static void queued(GameTestHelper helper, Fixture f, int completed) {
        if (completed == 3) {
            f.tamer().logic().setWorkingEnabled(false);
            var a = fixture(helper, 331000); var b = fixture(helper, 332000);
            helper.getLevel().setBlockAndUpdate(WorldEaterStructureTests.intentionPosition(b.tamer().controller()),
                    net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine.BRAVERY.defaultBlockState());
            var victim = b.tamer().controller().getPos().east(8);
            helper.getLevel().setBlockAndUpdate(victim, com.gregtechceu.gtceu.common.data.GTMachines.MACERATOR[com.gregtechceu.gtceu.api.GTValues.LV].defaultBlockState());
            awaitNetwork(helper, a, 0, () -> awaitNetwork(helper, b, 0, () -> {
                for (var current : java.util.List.of(a, b)) {
                    WorldEaterDemandTests.prepare(helper, current.tamer()); supply(current, 2);
                    helper.getLevel().destroyBlock(current.hatch().getPos().north(), false);
                }
                await(helper, () -> !a.hatch().getMainNode().isPowered() && !b.hatch().getMainNode().isPowered(), 0, () -> {
                    for (var current : java.util.List.of(a, b)) {
                        var t = current.tamer(); startExact(helper, current); t.input().getInventory().setStackInSlot(1, entry().stack(1));
                        t.logic().serverTick();
                        helper.assertTrue(pending(current) == null && t.input().getInventory().getStackInSlot(1).getCount() == 1
                                && t.logic().getDemand().getCount() == 3, "Physically disconnected ME stock never enables partial payment");
                        helper.assertTrue(t.logic().getDuration() == entry().deadline(), "Automation cannot extend tier deadline");
                        WorldEaterDemandTests.field(t.logic(), RecipeLogic.class, "progress", t.logic().getDuration() - 1);
                        t.logic().serverTick(); t.logic().onRecipeFinish();
                        helper.assertTrue(current.hatch().getRequest().isEmpty() && WorldEaterCasingTests.outputCount(t, WorldEaterDemandTests.reward().getItem()) == 0
                                && t.input().getInventory().getStackInSlot(0).isEmpty() && t.input().getInventory().getStackInSlot(1).getCount() == 1,
                                "Offline timeout clears request without reward, extra token payment, or partial demand loss");
                        t.logic().setWorkingEnabled(false);
                    }
                    helper.assertTrue(helper.getLevel().isEmptyBlock(WorldEaterStructureTests.intentionPosition(a.tamer().controller())), "Offline Safety timeout retains ordinary sacrifice");
                    helper.assertTrue(helper.getLevel().isEmptyBlock(victim) && !helper.getLevel().isEmptyBlock(WorldEaterStructureTests.intentionPosition(b.tamer().controller())),
                            "Offline Bravery timeout retains ordinary one-victim consequence");
                    helper.succeed();
                });
            }));
            return;
        }
        await(helper, () -> !f.tamer().logic().getDemand().isEmpty(), 0, () -> {
            helper.assertTrue(ItemStack.matches(f.hatch().getRequest(), f.tamer().logic().getDemand())
                    && f.tamer().input().getInventory().getStackInSlot(0).getCount() == 2 - completed, "Native queued run pays one token and replaces request");
            var demand = f.tamer().logic().getDemand();
            f.hatch().getMainNode().getGrid().getStorageService().getInventory().insert(AEItemKey.of(demand), demand.getCount(), Actionable.MODULATE, new BaseActionSource());
            await(helper, () -> WorldEaterCasingTests.outputCount(f.tamer(), WorldEaterDemandTests.reward().getItem()) == completed + 1,
                    0, () -> queued(helper, f, completed + 1));
        });
    }

    static void sharedGridAndIndependentJobs(GameTestHelper helper) {
        var a = fixture(helper, 327000); var b = fixture(helper, 327100);
        awaitNetwork(helper, a, 0, () -> awaitNetwork(helper, b, 0, () -> {
            WorldEaterDemandTests.prepare(helper, a.tamer()); WorldEaterDemandTests.prepare(helper, b.tamer()); pattern(a); pattern(b);
            await(helper, () -> a.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1)))
                    && b.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0, () -> {
                startExact(helper, a);
                await(helper, () -> jobs(a) == 1, 0, () -> {
                    startExact(helper, b); var gate = hold(b);
                    helper.assertTrue(a.hatch().getMainNode().getGrid() != b.hatch().getMainNode().getGrid(), "Another grid's matching job cannot suppress calculation");
                    appeng.api.networking.GridHelper.createConnection(a.hatch().getMainNode().getNode(), b.hatch().getMainNode().getNode());
                    await(helper, () -> a.hatch().getMainNode().getGrid() == b.hatch().getMainNode().getGrid(), 0, () -> {
                        await(helper, gate.real()::isDone, 0, () -> {
                            gate.held().complete(ready(gate.real())); b.tamer().logic().serverTick();
                            helper.assertTrue(pending(b) == null && jobs(a) == 1, "Grid change/matching job invalidates pending submission");
                            b.tamer().logic().interruptRecipe(); startExact(helper, b);
                            helper.assertTrue(pending(b) == null && jobs(b) == 1, "Second tamer skips matching output on shared grid");
                            a.tamer().logic().setWorkingEnabled(false); b.tamer().logic().setWorkingEnabled(false);
                            var before = a.tamer().logic().getProgress();
                            for (int i = 0; i < 5; i++) a.tamer().logic().serverTick();
                            helper.assertTrue(a.tamer().logic().getProgress() == before && jobs(a) == 1, "Suspension pauses deadline without cancelling independent job");
                            await(helper, () -> a.hatch().getMainNode().getGrid().getCraftingService().getRequestedAmount(AEItemKey.of(entry().stack(1))) == 3, 0, () -> {
                                supply(a, 8);
                                await(helper, () -> stock(a) == 8, 0, () -> {
                                    a.tamer().logic().setWorkingEnabled(true); b.tamer().logic().setWorkingEnabled(true);
                                    a.tamer().logic().serverTick(); b.tamer().logic().serverTick();
                                    helper.assertTrue(stock(a) == 2 && a.hatch().getRequest().isEmpty() && b.hatch().getRequest().isEmpty(), "Two demands pay separately and preserve batch surplus");
                                    helper.assertTrue(WorldEaterCasingTests.outputCount(a.tamer(), WorldEaterDemandTests.reward().getItem()) == 1
                                            && WorldEaterCasingTests.outputCount(b.tamer(), WorldEaterDemandTests.reward().getItem()) == 1, "One reward per paid tamer, not per grid");
                                    a.tamer().logic().setWorkingEnabled(false); b.tamer().logic().setWorkingEnabled(false); helper.succeed();
                                });
                            });
                        });
                    });
                });
            });
        }));
    }

    static void awaitedIntermediateAndBusyCpu(GameTestHelper helper) {
        var f = fixture(helper, 329000);
        awaitNetwork(helper, f, 0, () -> {
            var t = f.tamer(); WorldEaterDemandTests.prepare(helper, t); pattern(f);
            var provider = (PatternProviderBlockEntity) helper.getLevel().getBlockEntity(f.hatch().getPos().north(2).east());
            provider.getLogic().getPatternInv().addItems(PatternDetailsHelper.encodeProcessingPattern(
                    new GenericStack[]{new GenericStack(AEItemKey.of(entry().stack(1)), 1)},
                    new GenericStack[]{new GenericStack(AEItemKey.of(Items.STONE), 1)}));
            var grid = f.hatch().getMainNode().getGrid();
            await(helper, () -> grid.getCraftingService().isCraftable(AEItemKey.of(Items.STONE)), 0, () -> {
                var future = grid.getCraftingService().beginCraftingCalculation(helper.getLevel(), () -> f.hatch().getActionSource(),
                        AEItemKey.of(Items.STONE), 1, appeng.api.networking.crafting.CalculationStrategy.REPORT_MISSING_ITEMS);
                await(helper, future::isDone, 0, () -> {
                    class OtherRequester implements appeng.api.networking.crafting.ICraftingRequester {
                        appeng.api.networking.crafting.ICraftingLink link;
                        public appeng.api.networking.IGridNode getActionableNode() { return f.hatch().getMainNode().getNode(); }
                        public com.google.common.collect.ImmutableSet<appeng.api.networking.crafting.ICraftingLink> getRequestedJobs() {
                            return link == null ? com.google.common.collect.ImmutableSet.of() : com.google.common.collect.ImmutableSet.of(link);
                        }
                        public long insertCraftedItems(appeng.api.networking.crafting.ICraftingLink link, appeng.api.stacks.AEKey key, long amount, Actionable action) { return amount; }
                        public void jobStateChange(appeng.api.networking.crafting.ICraftingLink link) {}
                    }
                    var requester = new OtherRequester();
                    var submitted = grid.getCraftingService().submitJob(ready(future), requester, null, false, f.hatch().getActionSource());
                    requester.link = submitted.link();
                    helper.assertTrue(submitted.successful() && requester.link != null && jobs(f) == 1, "Real other-requester job for unrelated final output");
                    startExact(helper, f); var gate = hold(f);
                    helper.assertTrue(pending(f) != null, "Unrelated final output does not suppress before intermediate is awaiting");
                    await(helper, () -> grid.getCraftingService().isRequesting(AEItemKey.of(entry().stack(1))), 0, () -> {
                        await(helper, gate.real()::isDone, 0, () -> {
                            gate.held().complete(ready(gate.real())); t.logic().serverTick();
                            helper.assertTrue(pending(f) == null && jobs(f) == 1, "Awaited intermediate suppresses submission without touching someone else's job");
                            t.logic().interruptRecipe(); startExact(helper, f);
                            helper.assertTrue(pending(f) == null, "Awaited intermediate also suppresses startup");
                            // Complete that intermediate; CPU now awaits STONE rather than our demand.
                            supply(f, 1);
                            await(helper, () -> !grid.getCraftingService().isRequesting(AEItemKey.of(entry().stack(1)))
                                    && grid.getCraftingService().isRequesting(AEItemKey.of(Items.STONE)), 0, () -> {
                                t.logic().interruptRecipe(); startExact(helper, f);
                                helper.assertTrue(pending(f) != null, "Unrelated busy CPU does not suppress calculation");
                                await(helper, () -> pending(f) == null, 0, () -> {
                                    helper.assertTrue(jobs(f) == 1, "Busy CPU rejects new job without replacing external job");
                                    for (var cpu : grid.getCraftingService().getCpus()) ((appeng.me.cluster.implementations.CraftingCPUCluster) cpu).cancel();
                                    for (int i = 0; i < 5; i++) t.logic().serverTick();
                                    helper.assertTrue(pending(f) == null && jobs(f) == 0, "Freeing busy CPU does not retry");
                                    t.logic().setWorkingEnabled(false); helper.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    static void unavailableStartupAndFailures(GameTestHelper helper) {
        var f = fixture(helper, 328000);
        awaitNetwork(helper, f, 0, () -> {
            var t = f.tamer(); WorldEaterDemandTests.prepare(helper, t); pattern(f);
            await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0, () -> {
                f.hatch().setWorkingEnabled(false); startExact(helper, f);
                helper.assertTrue(pending(f) == null && !f.hatch().getRequest().isEmpty(), "Offline/disabled startup keeps passive request but consumes opportunity");
                f.hatch().setWorkingEnabled(true);
                for (int i = 0; i < 5; i++) t.logic().serverTick();
                helper.assertTrue(pending(f) == null && jobs(f) == 0, "Re-enabling never starts first craft late");
                supply(f, 3); t.logic().serverTick(); helper.assertTrue(f.hatch().getRequest().isEmpty(), "Passive payment resumes after unavailable startup");
                var storage = f.hatch().getMainNode().getGrid().getStorageService().getInventory();
                storage.extract(AEItemKey.of(Items.COBBLESTONE), 1000, Actionable.MODULATE, new BaseActionSource());
                startExact(helper, f); var missing = pending(f);
                await(helper, () -> pending(f) == null, 0, () -> {
                    helper.assertTrue(missing != null && jobs(f) == 0, "Missing ingredients use one failed calculation");
                    storage.insert(AEItemKey.of(Items.COBBLESTONE), 64, Actionable.MODULATE, new BaseActionSource());
                    for (int i = 0; i < 5; i++) t.logic().serverTick();
                    helper.assertTrue(pending(f) == null && jobs(f) == 0, "Later ingredients cannot retry");
                    t.logic().interruptRecipe(); startExact(helper, f); var gate = hold(f);
                    gate.held().completeExceptionally(new IllegalStateException("Deliberate fixture calculation failure"));
                    t.logic().serverTick();
                    helper.assertTrue(pending(f) == null && jobs(f) == 0, "Calculation failure consumes opportunity");
                    WorldEaterDemandTests.reload(t); t.logic().serverTick();
                    helper.assertTrue(pending(f) == null && jobs(f) == 0, "Failed-attempt save cannot reopen startup");
                    t.logic().interruptRecipe(); helper.getLevel().destroyBlock(f.hatch().getPos().north(2).west(), false);
                    await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().getCpus().isEmpty(), 0, () -> {
                        startExact(helper, f); var noCpu = pending(f);
                        await(helper, () -> pending(f) == null, 0, () -> {
                            helper.assertTrue(noCpu != null && jobs(f) == 0, "No suitable CPU rejects single submission");
                            helper.getLevel().setBlockAndUpdate(f.hatch().getPos().north(2).west(), AEBlocks.CRAFTING_STORAGE_64K.block().defaultBlockState());
                            awaitNetwork(helper, f, 0, () -> {
                                helper.assertTrue(pending(f) == null && jobs(f) == 0, "New CPU cannot retry old run");
                                t.logic().setWorkingEnabled(false); helper.succeed();
                            });
                        });
                    });
                });
            });
        });
    }

    static void nativeHatchEvictionAndReload(GameTestHelper helper) {
        eviction(helper, 326000, false);
    }

    static void nativeSubmittedJobEvictionAndReload(GameTestHelper helper) {
        eviction(helper, 326500, true);
    }

    private static void eviction(GameTestHelper helper, int location, boolean submitted) {
        var f = fixture(helper, location);
        awaitNetwork(helper, f, 0, () -> {
            WorldEaterDemandTests.prepare(helper, f.tamer()); pattern(f);
            await(helper, () -> f.hatch().getMainNode().getGrid().getCraftingService().isCraftable(AEItemKey.of(entry().stack(1))), 0, () -> {
                startExact(helper, f);
                if (submitted) await(helper, () -> jobs(f) == 1, 0, () -> releaseForEviction(helper, f, null));
                else releaseForEviction(helper, f, hold(f));
            });
        });
    }

    private static void releaseForEviction(GameTestHelper helper, Fixture f, Gate gate) {
        f.tamer().logic().setWorkingEnabled(false);
        int cx = f.tamer().controller().getPos().getX() >> 4, cz = f.tamer().controller().getPos().getZ() >> 4;
        for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) helper.getLevel().setChunkForced(x, z, false);
        awaitEviction(helper, f, gate, 0);
    }

    private static void awaitEviction(GameTestHelper helper, Fixture old, Gate gate, int waited) {
        var level = helper.getLevel();
        var pos = old.tamer().controller().getPos(); var hatchPos = old.hatch().getPos();
        if (!WorldEaterUnloadTests.wasUnloaded(pos) || !WorldEaterUnloadTests.wasUnloaded(hatchPos)
                || level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4) != null
                || level.getChunkSource().getChunkNow(hatchPos.getX() >> 4, hatchPos.getZ() >> 4) != null) {
            helper.assertTrue(waited < 1400, "Controller and hatch really evict, including unload event and non-loading cache absence");
            helper.runAfterDelay(1, () -> awaitEviction(helper, old, gate, waited + 1)); return;
        }
        helper.assertTrue(pending(old) == null && (gate == null || gate.held().isCancelled()) && old.hatch().getRequest().isEmpty(),
                "Real unload discards only unfinished calculation and derived request");
        int cx = pos.getX() >> 4, cz = pos.getZ() >> 4;
        for (int x = cx - 1; x <= cx + 1; x++) for (int z = cz - 1; z <= cz + 1; z++) level.setChunkForced(x, z, true);
        level.getChunk(cx, cz);
        helper.runAfterDelay(8, () -> {
            var controller = (net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer) MetaMachine.getMachine(level, pos);
            var hatch = (MEDemandHatchPartMachine) MetaMachine.getMachine(level, hatchPos);
            helper.assertTrue(controller != old.tamer().controller() && hatch != old.hatch(), "Fresh controller and ME part after eviction");
            helper.assertTrue(controller.checkPatternWithLock(), "Reloaded ME frame forms"); WorldEaterStructureTests.formed(controller);
            var f = new Fixture(new WorldEaterRunTests.Fixture(controller,
                    (com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine) MetaMachine.getMachine(level, old.tamer().input().getPos()),
                    (com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine) MetaMachine.getMachine(level, old.tamer().output().getPos())), hatch);
            helper.assertTrue(ItemStack.matches(f.tamer().logic().getDemand(), old.tamer().logic().getDemand())
                    && f.tamer().logic().getProgress() == old.tamer().logic().getProgress() && pending(f) == null,
                    "Reload retains original paid demand/deadline without restarting calculation");
            awaitNetwork(helper, f, 0, () -> {
                helper.assertTrue(ItemStack.matches(hatch.getRequest(), f.tamer().logic().getDemand()) && jobs(f) == (gate == null ? 1 : 0),
                        "Reload reconstructs request, never restarts calculation or cancels independently persisted CPU job");
                supply(f, 3); f.tamer().logic().setWorkingEnabled(true);
                await(helper, () -> f.tamer().logic().getDemand().isEmpty(), 0, () -> {
                    helper.assertTrue(hatch.getRequest().isEmpty() && WorldEaterCasingTests.outputCount(f.tamer(), WorldEaterDemandTests.reward().getItem()) == 1,
                            "Reloaded request still pays once through ordinary ME stock");
                    f.tamer().logic().setWorkingEnabled(false); helper.succeed();
                });
            });
        });
    }
}
