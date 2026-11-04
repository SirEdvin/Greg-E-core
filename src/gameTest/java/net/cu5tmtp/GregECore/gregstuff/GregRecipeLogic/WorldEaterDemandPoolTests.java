package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterDemandPoolTests {
    @GameTest(template = "empty")
    public static void optionalComponentsAndFixedMaterialPool(GameTestHelper helper) {
        var all = WorldEaterDemands.pool();
        var low = WorldEaterDemands.build(id -> id.getPath().startsWith("uhv_") || id.getPath().startsWith("uev_")
                ? null : ForgeRegistries.ITEMS.getValue(id));
        helper.assertTrue(low.size() == 8 * 8 + 9 * 3, "All LV-UV families and all nine material tiers");
        helper.assertTrue(low.stream().map(WorldEaterDemands.Entry::item).distinct().count() == low.size(), "No duplicate weighting");
        var optionalId = new ResourceLocation("gtceu", "uev_sensor");
        var optional = ForgeRegistries.ITEMS.getValue(optionalId);
        var partial = WorldEaterDemands.build(id -> {
            if (id.equals(optionalId)) return optional;
            return id.getPath().startsWith("uhv_") || id.getPath().startsWith("uev_") ? Items.AIR : ForgeRegistries.ITEMS.getValue(id);
        });
        helper.assertTrue(partial.size() == low.size() + (optional == null || optional == Items.AIR ? 0 : 1), "Optional entries resolved individually");
        for (var entry : all) {
            helper.assertTrue(entry.item() != Items.AIR && entry.tier() >= GTValues.LV && entry.tier() <= GTValues.UEV, "Nonempty supported tier");
            helper.assertTrue(entry.deadline() == 600 * entry.tier(), "Thirty seconds per voltage index");
            helper.assertTrue(entry.stack(1).getCount() == 1 && entry.stack(5).getCount() == 5, "Exact demand quantity bounds");
        }
        for (var prefix : new TagPrefix[]{TagPrefix.gear, TagPrefix.rodLong, TagPrefix.rotor}) {
            var neutronium = ChemicalHelper.get(prefix, GTMaterials.Neutronium);
            helper.assertTrue(low.stream().anyMatch(e -> e.item() == neutronium.getItem() && e.tier() == GTValues.UHV),
                    "UHV material parts are independent of optional components");
        }
        helper.assertTrue(all.stream().filter(e -> e.tier() == GTValues.UEV).allMatch(e ->
                ForgeRegistries.ITEMS.getKey(e.item()).getPath().startsWith("uev_")), "UEV has components, not guessed materials");
        helper.succeed();
    }
}
