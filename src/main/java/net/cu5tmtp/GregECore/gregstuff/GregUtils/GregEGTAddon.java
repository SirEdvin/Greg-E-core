package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.addon.events.KJSRecipeKeyEvent;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.world.item.Items;
import net.minecraft.data.recipes.FinishedRecipe;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
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
        GTRecipeTypes.ASSEMBLY_LINE_RECIPES.recipeBuilder(GregECore.id("safety_intention_hatch"))
                .inputItems(GTMachines.ELECTRIC_FURNACE[GTValues.LuV], 8)
                .inputItems(GTMachines.MACERATOR[GTValues.LuV], 8)
                .inputItems(GTMachines.ALLOY_SMELTER[GTValues.LuV], 8)
                .inputItems(GTMachines.ARC_FURNACE[GTValues.LuV], 8)
                .inputItems(GTMachines.ASSEMBLER[GTValues.LuV], 8)
                .inputItems(GTMachines.AUTOCLAVE[GTValues.LuV], 8)
                .inputItems(GTMachines.BENDER[GTValues.LuV], 8)
                .inputItems(GTMachines.BREWERY[GTValues.LuV], 8)
                .inputItems(GTMachines.CENTRIFUGE[GTValues.LuV], 8)
                .inputItems(GTMachines.CHEMICAL_BATH[GTValues.LuV], 8)
                .inputItems(GTMachines.CHEMICAL_REACTOR[GTValues.LuV], 8)
                .inputItems(GTMachines.CIRCUIT_ASSEMBLER[GTValues.LuV], 8)
                .inputItems(GTMachines.CUTTER[GTValues.LuV], 8)
                .inputItems(GTMachines.ELECTROLYZER[GTValues.LuV], 8)
                .inputItems(GTMachines.ELECTROMAGNETIC_SEPARATOR[GTValues.LuV], 8)
                .inputItems(GTMachines.EXTRACTOR[GTValues.LuV], 8)
                .outputItems(IntentionHatchPartMachine.SAFETY)
                .duration(1200).EUt(GTValues.VA[GTValues.LuV]).save(provider);
        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("bravery_intention_hatch"))
                .inputItems(BraveryBookIngredient.INSTANCE)
                .outputItems(IntentionHatchPartMachine.BRAVERY)
                .duration(1200)
                .addData("required_state", "ACTIVE")
                .addData("required_casing_tier", 2)
                .addData("operation", WorldEaterTamerRecipeLogic.BRAVERY_OPERATION)
                .save(provider);

        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("information_collection"))
                .inputItems(GTItems.SENSOR_IV, 1)
                .outputItems(WorldEaterReport.create(Map.of(), true, 0, 0))
                .duration(WorldEaterScanDomain.SCAN_TICKS)
                .addData("required_state", "DORMANT")
                .addData("operation", WorldEaterTamerRecipeLogic.OPERATION)
                .save(provider);
        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("purple_casing"))
                .inputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING.asItem(), 1)
                .inputItems(Items.PURPLE_TERRACOTTA, 16)
                .outputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.asItem(), 1)
                .duration(100)
                .addData("required_state", "ACTIVE")
                .addData("operation", WorldEaterTamerRecipeLogic.CASING_OPERATION)
                .save(provider);
        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("green_casing"))
                .inputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING.asItem(), 1)
                .inputItems(Items.GREEN_TERRACOTTA, 16)
                .outputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.asItem(), 1)
                .duration(100)
                .addData("required_state", "ACTIVE")
                .addData("operation", WorldEaterTamerRecipeLogic.CASING_OPERATION)
                .save(provider);
        GregERecipeTypes.WORLD_EATER_TAMER.recipeBuilder(GregECore.id("orange_casing"))
                .inputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING.asItem(), 1)
                .inputItems(Items.ORANGE_TERRACOTTA, 16)
                .outputItems(GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.asItem(), 1)
                .duration(100)
                .addData("required_state", "ACTIVE")
                .addData("operation", WorldEaterTamerRecipeLogic.CASING_OPERATION)
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
