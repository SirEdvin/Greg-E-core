package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterDemandMaterialTests {
    @GameTest(template = "empty", batch = "generatedTokensAndReward")
    public static void generatedTokensAndReward(GameTestHelper helper) {
        String[] ids = {"desarium", "implementium", "tamed_world_eater_essense"};
        String[] names = {"Desarium", "Implementium", "Tamed world eater essense"};
        MaterialIconSet[] icons = {MaterialIconSet.DIAMOND, MaterialIconSet.EMERALD, MaterialIconSet.SHINY};
        int[] colors = {0x9B59B6, 0x38A65A, 0x3B1656};
        var tab = ModCreativeModTabs.GREGE_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true,
                helper.getLevel().registryAccess()));
        for (int i = 0; i < ids.length; i++) {
            var material = GTCEuAPI.materialManager.getMaterial(GregECore.id(ids[i]).toString());
            helper.assertTrue(material != null, "Registered addon material: " + ids[i]);
            helper.assertTrue(material.getResourceLocation().equals(GregECore.id(ids[i])), "Addon-owned material ID");
            helper.assertTrue(material.getMaterialIconSet() == icons[i] && material.getMaterialRGB() == colors[i], "Material appearance");
            helper.assertTrue(material.getLocalizedName().getString().equals(names[i]), "Material name: " + material.getLocalizedName().getString());
            var stack = ChemicalHelper.get(i == 2 ? TagPrefix.ingot : TagPrefix.gem, material);
            helper.assertTrue(!stack.isEmpty() && stack.getCount() == 1, "Native generated primary form");
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(s -> s.is(stack.getItem())), "Creative primary form");
        }
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "requestedNativeMaterialForms")
    public static void requestedNativeMaterialForms(GameTestHelper helper) {
        var processed = new java.util.HashSet<net.minecraft.world.item.Item>();
        for (var recipe : helper.getLevel().getRecipeManager().getRecipes()) {
            if (recipe instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe gt) {
                for (var content : gt.getOutputContents(com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability.CAP)) {
                    for (var stack : ((net.minecraft.world.item.crafting.Ingredient) content.content).getItems()) processed.add(stack.getItem());
                }
            } else processed.add(recipe.getResultItem(helper.getLevel().registryAccess()).getItem());
        }
        for (Material material : new Material[]{GTMaterials.Steel, GTMaterials.Aluminium, GTMaterials.StainlessSteel,
                GTMaterials.Titanium, GTMaterials.TungstenSteel, GTMaterials.RhodiumPlatedPalladium,
                GTMaterials.NaquadahAlloy, GTMaterials.Darmstadtium, GTMaterials.Neutronium}) {
            for (TagPrefix prefix : new TagPrefix[]{TagPrefix.gear, TagPrefix.rodLong, TagPrefix.rotor}) {
                var stack = ChemicalHelper.get(prefix, material);
                helper.assertTrue(!stack.isEmpty(), "Required native form: " + material.getName() + "/" + prefix.name);
                helper.assertTrue(processed.contains(stack.getItem()), "Native processing route: " + material.getName() + "/" + prefix.name);
            }
        }
        helper.succeed();
    }
}
