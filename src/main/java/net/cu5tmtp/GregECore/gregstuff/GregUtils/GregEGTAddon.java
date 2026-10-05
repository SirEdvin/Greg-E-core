package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.data.recipes.FinishedRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterReport;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import java.util.Map;
import java.util.function.Consumer;

@SuppressWarnings("unused")
@GTAddon
public class GregEGTAddon implements IGTAddon {

    @Override
    public GTRegistrate getRegistrate() {
        return GregECore.REGISTRATE;
    }

    @Override
    public void initializeAddon() {
        GreggyBlocks.init();
    }

    @Override
    public String addonModId() {
        return GregECore.MOD_ID;
    }

    @Override
    public void registerTagPrefixes() {}

    @Override
    public void addRecipes(Consumer<FinishedRecipe> provider) {
        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("information_collection"))
                .inputItems(GTItems.SENSOR_IV, 1)
                .outputItems(WorldEaterReport.create(Map.of(), true, 0, 0))
                .duration(WorldEaterScanDomain.SCAN_TICKS)
                .addData("required_state", "DORMANT")
                .addData("operation", WorldEaterTamerRecipeLogic.OPERATION)
                .save(provider);
    }

    @Override
    public void registerElements() {
        IGTAddon.super.registerElements();
    }

    @Override
    public void registerRecipeCapabilities() {
    }

    @Override
    public void registerRecipeKeys(KJSRecipeKeyEvent event) {
    }
}
