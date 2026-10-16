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
}
