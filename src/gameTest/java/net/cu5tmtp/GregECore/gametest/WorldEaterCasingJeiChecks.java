package net.cu5tmtp.GregECore.gametest;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/** Test-only public JEI API bridge for native recipe-screen acceptance. */
@JeiPlugin
public final class WorldEaterCasingJeiChecks implements IModPlugin {
    private static IJeiRuntime runtime;
    static com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget structurePreview;
    @Override public ResourceLocation getPluginUid() { return GregECore.id("casing_verification"); }
    @Override public void onRuntimeAvailable(IJeiRuntime value) { runtime = value; }
    @Override public void onRuntimeUnavailable() { runtime = null; }

    static boolean showPurpleRecipe() {
        if (runtime == null) return false;
        var type = runtime.getRecipeManager().getRecipeType(
                GregERecipeTypes.WORLD_EATER_TAMER.getCategory().registryKey, GTRecipe.class).orElseThrow();
        var category = runtime.getRecipeManager().getRecipeCategory(type);
        var recipe = (GTRecipe) Minecraft.getInstance().level.getRecipeManager()
                .byKey(GregECore.id("world_eater_tamer/purple_casing")).orElseThrow();
        runtime.getRecipesGui().showRecipes(category, List.of(recipe), List.of());
        return true;
    }

    static boolean showTamerStructure() {
        if (runtime == null) return false;
        var type = com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory.RECIPE_TYPE;
        var category = runtime.getRecipeManager().getRecipeCategory(type);
        var wrapper = new com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoWrapper(
                (com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition) net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER);
        structurePreview = wrapper.getWidget();
        runtime.getRecipesGui().showRecipes(category, List.of(wrapper), List.of());
        return true;
    }
}
