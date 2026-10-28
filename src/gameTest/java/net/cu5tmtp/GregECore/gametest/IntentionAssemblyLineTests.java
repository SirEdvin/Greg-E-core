package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.AssemblyLineMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import com.gregtechceu.gtceu.config.ConfigHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntentionAssemblyLineTests {
    record Fixture(AssemblyLineMachine machine, List<ItemBusPartMachine> inputs, ItemBusPartMachine output) {}

    static Fixture fixture(GameTestHelper helper, int location, int inputCount) {
        var origin = helper.absolutePos(new BlockPos(location, 5, location));
        for (int x = (origin.getX() >> 4) - 1; x <= (origin.getX() >> 4) + 2; x++)
            for (int z = (origin.getZ() >> 4) - 1; z <= (origin.getZ() >> 4) + 1; z++) helper.getLevel().setChunkForced(x, z, true);
        var inputs = new ArrayList<ItemBusPartMachine>();
        ItemBusPartMachine output = null;
        for (int slice = 0; slice <= inputCount; slice++) for (int y = 0; y < 4; y++) for (int depth = 0; depth < 3; depth++) {
            if (y == 3 && depth != 1) continue;
            var pos = origin.east(slice).south(depth).above(y - 2);
            var state = switch (y) {
                case 0 -> depth != 1 ? GTBlocks.CASING_STEEL_SOLID.getDefaultState()
                        : (slice == inputCount ? GTMachines.ITEM_EXPORT_BUS[GTValues.LV] : GTMachines.ITEM_IMPORT_BUS[0]).defaultBlockState();
                case 1 -> depth == 1 ? GTBlocks.CASING_ASSEMBLY_LINE.getDefaultState() : GTBlocks.CASING_LAMINATED_GLASS.getDefaultState();
                case 2 -> depth == 1 ? GTBlocks.CASING_ASSEMBLY_CONTROL.getDefaultState()
                        : (slice == 0 && depth == 0 ? GTMultiMachines.ASSEMBLY_LINE.defaultBlockState()
                        : slice == 1 && depth == 0 ? PartAbility.DATA_ACCESS.getAllBlocks().iterator().next().defaultBlockState()
                        : GTBlocks.CASING_GRATE.getDefaultState());
                default -> slice == 0 ? GTMachines.ENERGY_INPUT_HATCH[GTValues.LuV].defaultBlockState() : GTBlocks.CASING_STEEL_SOLID.getDefaultState();
            };
            helper.getLevel().setBlockAndUpdate(pos, state);
            if (y == 0 && depth == 1) {
                var bus = (ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
                if (slice == inputCount) output = bus; else inputs.add(bus);
            }
        }
        var machine = (AssemblyLineMachine) MetaMachine.getMachine(helper.getLevel(), origin);
        machine.setFrontFacing(Direction.NORTH);
        machine.getRecipeLogic().setWorkingEnabled(false);
        return new Fixture(machine, inputs, output);
    }

    static void form(GameTestHelper helper, Fixture f) {
        boolean matches = f.machine.checkPatternWithLock();
        var state = f.machine.getMultiblockState();
        helper.assertTrue(matches, "Native Assembly Line pattern: " + (state.error == null ? "none" : state.error.getErrorInfo().getString())
                + " at " + state.getPos() + " actual=" + helper.getLevel().getBlockState(state.getPos())
                + " origin=" + f.machine.getPos() + " up=" + f.machine.getUpwardsFacing());
        f.machine.onStructureFormed();
        helper.assertTrue(ConfigHolder.INSTANCE.machines.enableResearch && ConfigHolder.INSTANCE.machines.orderedAssemblyLineItems,
                "Run fixture with ordinary research and input ordering enabled");
        f.machine.getRecipeLogic().setWorkingEnabled(true);
    }

    static void feed(Fixture f) {
        f.machine.getRecipeLogic().resetRecipeLogic();
        f.output.getInventory().setStackInSlot(0, ItemStack.EMPTY);
        var recipe = IntentionRecipeTests.recipe(f.machine.getLevel(), true);
        var contents = recipe.getInputContents(ItemRecipeCapability.CAP);
        for (int i = 0; i < f.inputs.size(); i++) {
            var stack = ItemRecipeCapability.CAP.of(contents.get(i).content).getItems()[0].copy();
            stack.grow(1);
            f.inputs.get(i).getInventory().setStackInSlot(0, stack);
        }
    }

    static void rejectedWithoutPayment(GameTestHelper helper, Fixture f, String message) {
        var before = f.inputs.stream().map(b -> b.getInventory().getStackInSlot(0).copy()).toList();
        f.machine.getRecipeLogic().findAndHandleRecipe();
        helper.assertTrue(!f.machine.getRecipeLogic().isWorking(), message);
        for (int i = 0; i < before.size(); i++)
            helper.assertTrue(ItemStack.matches(before.get(i), f.inputs.get(i).getInventory().getStackInSlot(0)), "No partial payment: " + message);
    }

    static void craft(GameTestHelper helper, Fixture f) {
        var logic = f.machine.getRecipeLogic();
        var energy = f.machine.getEnergyContainer();
        energy.changeEnergy(energy.getEnergyCapacity());
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isWorking() && logic.getLastRecipe().id.equals(IntentionRecipeTests.recipe(f.machine.getLevel(), true).id),
                "Normal Safety lookup: " + logic.getStatus() + ", " + logic.getLastRecipe());
        helper.assertTrue(logic.getDuration() == 1200, "No overclock at LuV recipe voltage");
        for (var bus : f.inputs) helper.assertTrue(bus.getInventory().getStackInSlot(0).getCount() == 1, "Exact payment with surplus retained");
        for (int tick = 1; tick <= 1200; tick++) {
            energy.changeEnergy(energy.getEnergyCapacity());
            long before = energy.getEnergyStored();
            logic.serverTick();
            helper.assertTrue(before - energy.getEnergyStored() == 30720, "Normal per-tick energy payment");
            if (tick < 1200) helper.assertTrue(f.output.getInventory().getStackInSlot(0).isEmpty(), "No early output");
        }
        var output = f.output.getInventory().getStackInSlot(0);
        helper.assertTrue(output.is(IntentionHatchPartMachine.SAFETY.getItem())
                && output.getCount() == 1, "Exactly one hatch at 1200 ticks");
        for (int i = 0; i < 10; i++) logic.serverTick();
        helper.assertTrue(f.output.getInventory().getStackInSlot(0).getCount() == 1, "No duplicate delivery or second partial payment");
    }

    @GameTest(template = "empty", batch = "intention_reload", timeoutTicks = 1600)
    public static void actualReloadPreservesLookupAndPayment(GameTestHelper helper) {
        var safety = fixture(helper, 265000, 16);
        var bravery = IntentionTamerTests.fixture(helper, 266000);
        helper.runAfterDelay(8, () -> {
            form(helper, safety); IntentionTamerTests.form(helper, bravery);
            var oldRecipe = IntentionRecipeTests.recipe(helper.getLevel(), false);
            var server = helper.getLevel().getServer();
            server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, error) -> server.execute(() ->
                    helper.runAfterDelay(2, () -> {
                        helper.assertTrue(error == null, "Actual server recipe reload: " + error);
                        helper.assertTrue(oldRecipe != IntentionRecipeTests.recipe(helper.getLevel(), false), "Recipe manager replaced definitions");
                        feed(safety); craft(helper, safety);
                        IntentionTamerTests.feed(bravery, IntentionRecipeTests.book(87, "\"invalid after reload\""));
                        IntentionTamerTests.rejectedWithoutPayment(helper, bravery, "Reload preserves content predicate");
                        IntentionTamerTests.feed(bravery, IntentionRecipeTests.book(88));
                        IntentionTamerTests.start(helper, bravery);
                        helper.assertTrue(bravery.input().getInventory().getStackInSlot(0).isEmpty(), "Reloaded craft charges one book");
                        IntentionTamerTests.finish(helper, bravery);
                        safety.machine.getRecipeLogic().setWorkingEnabled(false);
                        bravery.logic().setWorkingEnabled(false);
                        helper.succeed();
                    })));
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void safetyPaymentAndRejections(GameTestHelper helper) {
        var f = fixture(helper, 250000, 16);
        helper.runAfterDelay(8, () -> {
            form(helper, f);
            f.machine.getEnergyContainer().changeEnergy(f.machine.getEnergyContainer().getEnergyCapacity());
            feed(f);
            f.inputs.get(15).getInventory().getStackInSlot(0).setCount(7);
            rejectedWithoutPayment(helper, f, "Short Safety payment");
            feed(f);
            f.inputs.get(0).getInventory().setStackInSlot(0, new ItemStack(GTMachines.ELECTRIC_FURNACE[GTValues.IV].getItem(), 8));
            rejectedWithoutPayment(helper, f, "Wrong voltage tier");
            feed(f);
            f.inputs.get(0).getInventory().setStackInSlot(0, ItemStack.EMPTY);
            rejectedWithoutPayment(helper, f, "Missing machine");
            feed(f);
            craft(helper, f);
            f.machine.getRecipeLogic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300)
    public static void safetyRequiresPower(GameTestHelper helper) {
        var f = fixture(helper, 251000, 16);
        helper.runAfterDelay(8, () -> {
            form(helper, f);
            feed(f);
            f.machine.getEnergyContainer().changeEnergy(-f.machine.getEnergyContainer().getEnergyStored());
            f.machine.getRecipeLogic().findAndHandleRecipe();
            for (int i = 0; i < 10; i++) f.machine.getRecipeLogic().serverTick();
            helper.assertTrue(f.machine.getRecipeLogic().getProgress() == 0 && f.output.getInventory().getStackInSlot(0).isEmpty(), "No power-free processing");
            feed(f);
            craft(helper, f);
            f.machine.getRecipeLogic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
