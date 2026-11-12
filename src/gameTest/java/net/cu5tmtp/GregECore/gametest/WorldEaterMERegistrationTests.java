package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.GTCEu;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterMERegistrationTests {
    @GameTest(template = "empty", batch = "optionalDemandHatchRegistration")
    public static void optionalDemandHatchRegistration(GameTestHelper helper) {
        var id = GregECore.id("me_demand_hatch");
        boolean enabled = GTCEu.Mods.isAE2Loaded();
        helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id) == enabled, "ME Demand Hatch item registration follows AE2 availability");
        helper.assertTrue(ForgeRegistries.BLOCKS.containsKey(id) == enabled, "ME Demand Hatch block registration follows AE2 availability");
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(id).isPresent() == enabled, "No dangling acquisition recipe without AE2");
        var tab = ModCreativeModTabs.GREGE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) == enabled,
                "Creative inventory contains the optional hatch exactly when available");
        if (enabled) {
            var block = ForgeRegistries.BLOCKS.getValue(id);
            var pos = helper.absolutePos(new net.minecraft.core.BlockPos(1, 2, 1));
            helper.getLevel().setBlockAndUpdate(pos, block.defaultBlockState());
            var drops = net.minecraft.world.level.block.Block.getDrops(block.defaultBlockState(), helper.getLevel(), pos,
                    helper.getLevel().getBlockEntity(pos), net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel()),
                    new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.IRON_PICKAXE));
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(ForgeRegistries.ITEMS.getValue(id)) && drops.get(0).getCount() == 1,
                    "Normal native recovery keeps exact hatch identity");
            helper.assertTrue(drops.get(0).getHoverName().getString().equals("ME Demand Hatch"), "Readable localized hatch name");
            var help = drops.get(0).getTooltipLines(null, net.minecraft.world.item.TooltipFlag.Default.NORMAL).stream()
                    .map(net.minecraft.network.chat.Component::getString).collect(java.util.stream.Collectors.joining("\n"));
            for (var phrase : java.util.List.of("Optional", "4 Tamed world eater essense", "one startup", "already scheduled", "No retries", "Fire-and-forget", "never tracked or cancelled", "deadlines"))
                helper.assertTrue(help.contains(phrase), "Native hatch help explains: " + phrase);
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "creativeDemandProviderRegistration")
    public static void creativeDemandProviderRegistration(GameTestHelper helper) {
        var id = GregECore.id("creative_demand_pattern_provider");
        boolean enabled = GTCEu.Mods.isAE2Loaded();
        helper.assertTrue(ForgeRegistries.ITEMS.containsKey(id) == enabled && ForgeRegistries.BLOCKS.containsKey(id) == enabled,
                "Creative provider registration follows AE2 availability");
        var tab = ModCreativeModTabs.GREGE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> id.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))) == enabled,
                "Creative inventory lists the provider exactly when available");
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipes().stream()
                .noneMatch(r -> id.equals(ForgeRegistries.ITEMS.getKey(r.getResultItem(helper.getLevel().registryAccess()).getItem()))),
                "No recipe produces the creative provider");
        if (enabled) {
            var stack = new net.minecraft.world.item.ItemStack(ForgeRegistries.ITEMS.getValue(id));
            helper.assertTrue(stack.getHoverName().getString().equals("Creative Demand Pattern Provider"), "Readable localized name");
            var help = stack.getTooltipLines(null, net.minecraft.world.item.TooltipFlag.Default.NORMAL).stream()
                    .map(net.minecraft.network.chat.Component::getString).collect(java.util.stream.Collectors.joining("\n"));
            for (var phrase : java.util.List.of("Creative-only", "no recipe", "1 Iron Ingot", "2 seconds"))
                helper.assertTrue(help.contains(phrase), "Provider help explains: " + phrase);
        }
        helper.succeed();
    }
}
