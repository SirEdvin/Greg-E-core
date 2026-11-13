package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.SimpleTieredMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.common.data.GTMachines;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntentionAssemblerTests {
    record Fixture(SimpleTieredMachine machine) {
        NotifiableItemStackHandler input() { return machine.importItems; }
        ItemStack output() { return machine.exportItems.getStackInSlot(0); }
    }

    static Fixture fixture(GameTestHelper helper, int location) {
        var pos = WorldEaterStructureTests.far(helper, new BlockPos(location, 5, location));
        helper.getLevel().setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
        helper.getLevel().setBlockAndUpdate(pos, GTMachines.ASSEMBLER[GTValues.LuV].defaultBlockState());
        var machine = (SimpleTieredMachine) MetaMachine.getMachine(helper.getLevel(), pos);
        machine.getRecipeLogic().setWorkingEnabled(false);
        return new Fixture(machine);
    }

    static void form(GameTestHelper helper, Fixture f) {
        f.machine.getRecipeLogic().setWorkingEnabled(true);
    }

    static void feed(Fixture f) {
        f.machine.getRecipeLogic().resetRecipeLogic();
        f.machine.exportItems.setStackInSlot(0, ItemStack.EMPTY);
        var contents = IntentionRecipeTests.recipe(f.machine.getLevel(), true).getInputContents(ItemRecipeCapability.CAP);
        for (int i = 0; i < f.input().getSlots(); i++) {
            var stack = i < contents.size() ? ItemRecipeCapability.CAP.of(contents.get(i).content).getItems()[0].copy() : ItemStack.EMPTY;
            if (!stack.isEmpty()) stack.grow(1);
            f.input().setStackInSlot(i, stack);
        }
    }

    static void rejectedWithoutPayment(GameTestHelper helper, Fixture f, String message) {
        var before = java.util.stream.IntStream.range(0, f.input().getSlots()).mapToObj(i -> f.input().getStackInSlot(i).copy()).toList();
        f.machine.getRecipeLogic().findAndHandleRecipe();
        helper.assertTrue(!f.machine.getRecipeLogic().isWorking(), message);
        for (int i = 0; i < before.size(); i++)
            helper.assertTrue(ItemStack.matches(before.get(i), f.input().getStackInSlot(i)), "No partial payment: " + message);
    }

    static void craft(GameTestHelper helper, Fixture f) {
        var logic = f.machine.getRecipeLogic();
        var energy = f.machine.energyContainer;
        energy.changeEnergy(energy.getEnergyCapacity());
        logic.findAndHandleRecipe();
        helper.assertTrue(logic.isWorking() && logic.getLastRecipe().id.equals(IntentionRecipeTests.recipe(f.machine.getLevel(), true).id),
                "Normal Safety lookup: " + logic.getStatus() + ", " + logic.getLastRecipe());
        helper.assertTrue(logic.getDuration() == 1200, "No overclock at LuV recipe voltage");
        for (int i = 0; i < 8; i++) helper.assertTrue(f.input().getStackInSlot(i).getCount() == 1, "Exact payment with surplus retained");
        for (int tick = 1; tick <= 1200; tick++) {
            energy.changeEnergy(energy.getEnergyCapacity());
            long before = energy.getEnergyStored();
            logic.serverTick();
            helper.assertTrue(before - energy.getEnergyStored() == 30720, "Normal per-tick energy payment");
            if (tick < 1200) helper.assertTrue(f.output().isEmpty(), "No early output");
        }
        var output = f.output();
        helper.assertTrue(output.is(IntentionHatchPartMachine.SAFETY.getItem())
                && output.getCount() == 1, "Exactly one hatch at 1200 ticks");
        for (int i = 0; i < 10; i++) logic.serverTick();
        helper.assertTrue(f.output().getCount() == 1, "No duplicate delivery or second partial payment");
    }

    @GameTest(template = "empty", timeoutTicks = 1600, batch = "actualReloadPreservesLookupAndPayment")
    public static void actualReloadPreservesLookupAndPayment(GameTestHelper helper) {
        var safety = fixture(helper, 265000);
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

    @GameTest(template = "empty", timeoutTicks = 300, batch = "safetyPaymentAndRejections")
    public static void safetyPaymentAndRejections(GameTestHelper helper) {
        var f = fixture(helper, 250000);
        helper.runAfterDelay(8, () -> {
            form(helper, f);
            f.machine.energyContainer.changeEnergy(f.machine.energyContainer.getEnergyCapacity());
            feed(f);
            f.input().getStackInSlot(7).setCount(7);
            rejectedWithoutPayment(helper, f, "Short Safety payment");
            feed(f);
            f.input().setStackInSlot(0, new ItemStack(GTMachines.ELECTRIC_FURNACE[GTValues.IV].getItem(), 8));
            rejectedWithoutPayment(helper, f, "Wrong voltage tier");
            feed(f);
            f.input().setStackInSlot(0, ItemStack.EMPTY);
            rejectedWithoutPayment(helper, f, "Missing machine");
            feed(f);
            craft(helper, f);
            f.machine.getRecipeLogic().setWorkingEnabled(false);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 300, batch = "safetyRequiresPower")
    public static void safetyRequiresPower(GameTestHelper helper) {
        var f = fixture(helper, 251000);
        helper.runAfterDelay(8, () -> {
            form(helper, f);
            feed(f);
            f.machine.energyContainer.changeEnergy(-f.machine.energyContainer.getEnergyStored());
            f.machine.getRecipeLogic().findAndHandleRecipe();
            for (int i = 0; i < 10; i++) f.machine.getRecipeLogic().serverTick();
            helper.assertTrue(f.machine.getRecipeLogic().getProgress() == 0 && f.output().isEmpty(), "No power-free processing");
            feed(f);
            craft(helper, f);
            f.machine.getRecipeLogic().setWorkingEnabled(false);
            helper.succeed();
        });
    }
}
