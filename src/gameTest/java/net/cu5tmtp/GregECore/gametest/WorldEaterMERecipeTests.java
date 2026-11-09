package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterMERecipeTests {
    @GameTest(template = "empty", timeoutTicks = 600, batch = "craftingConsumptionLayoutsReloadAndNetwork")
    public static void craftingConsumptionLayoutsReloadAndNetwork(GameTestHelper helper) {
        if (!GTCEu.Mods.isAE2Loaded()) { helper.succeed(); return; }
        verify(helper);
        var server = helper.getLevel().getServer();
        server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, error) -> server.execute(() -> {
            helper.assertTrue(error == null, "Native recipe reload succeeds: " + error);
            verify(helper);
            helper.succeed();
        }));
    }

    private static void verify(GameTestHelper helper) {
        var level = helper.getLevel();
        var id = GregECore.id("me_demand_hatch");
        var recipe = (ShapelessRecipe) level.getRecipeManager().byKey(id).orElseThrow();
        var bus = ForgeRegistries.ITEMS.getValue(GTCEu.id("me_stocking_input_bus"));
        var essence = ChemicalHelper.get(TagPrefix.ingot, GreggyItems.TAMED_WORLD_EATER_ESSENSE).getItem();
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(level);
        var menu = new CraftingMenu(1, player.getInventory(), ContainerLevelAccess.create(level, helper.absolutePos(net.minecraft.core.BlockPos.ZERO)));
        var buffer = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            RecipeSerializer.SHAPELESS_RECIPE.toNetwork(buffer, recipe);
            var decoded = RecipeSerializer.SHAPELESS_RECIPE.fromNetwork(id, buffer);
            helper.assertTrue(decoded.getIngredients().size() == 5 && decoded.getResultItem(level.registryAccess()).getCount() == 1,
                    "Network recipe retains all five counted ingredients and one output");
            for (int layout = 0; layout < 9; layout++) {
                for (int slot = 1; slot <= 9; slot++) menu.getSlot(slot).set(ItemStack.EMPTY);
                menu.getSlot(1 + layout).set(new ItemStack(bus, 2));
                for (int ingredient = 1; ingredient <= 4; ingredient++) menu.getSlot(1 + (layout + ingredient) % 9).set(new ItemStack(essence, 3));
                var output = menu.getSlot(0).getItem();
                helper.assertTrue(output.is(ForgeRegistries.ITEMS.getValue(id)) && output.getCount() == 1, "Shapeless layout " + layout);
                var taken = menu.getSlot(0).remove(1);
                menu.getSlot(0).onTake(player, taken);
                helper.assertTrue(menu.getSlot(1 + layout).getItem().getCount() == 1, "One actual bus consumed");
                for (int ingredient = 1; ingredient <= 4; ingredient++) helper.assertTrue(
                        menu.getSlot(1 + (layout + ingredient) % 9).getItem().getCount() == 2, "Four actual ingots consumed; surplus preserved");
            }
            for (int invalid = 0; invalid < 6; invalid++) {
                for (int slot = 1; slot <= 9; slot++) menu.getSlot(slot).set(ItemStack.EMPTY);
                menu.getSlot(1).set(new ItemStack(bus));
                for (int slot = 2; slot <= 5; slot++) menu.getSlot(slot).set(new ItemStack(essence));
                switch (invalid) {
                    case 0 -> menu.getSlot(5).set(ItemStack.EMPTY);
                    case 1 -> menu.getSlot(1).set(new ItemStack(Items.HOPPER));
                    case 2 -> menu.getSlot(5).set(new ItemStack(Items.IRON_INGOT));
                    case 3 -> menu.getSlot(5).set(ChemicalHelper.get(TagPrefix.dust, GreggyItems.TAMED_WORLD_EATER_ESSENSE));
                    case 4 -> menu.getSlot(1).set(new ItemStack(ForgeRegistries.ITEMS.getValue(GTCEu.id("me_input_bus"))));
                    default -> menu.getSlot(6).set(new ItemStack(Items.STONE));
                }
                helper.assertTrue(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(2).getItem().getCount() == 1,
                        "Wrong or insufficient ingredients never offer or consume a result: " + invalid);
            }
        } finally { buffer.release(); }
    }
}
