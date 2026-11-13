package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.recipe.modifier.ParallelLogic;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.chillers.BigFreezer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.chillers.EnhancedBlastChiller;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.dyson.DysonSwarmEnergyCollector;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid.QuantumSynergizer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.AcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.GiantAcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces.LearningAcceleratedEBF;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.DeepSpaceExplorer;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc.PressureChamber;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.FissionReactor;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors.GiantChemicalReactor;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.managers.DysonSwarmManager;
import net.minecraft.server.level.ServerLevel;

public class GregEModifiers {

    public static ModifierFunction acceleratedEBFModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof AcceleratedEBF ebf)) {
            return ModifierFunction.NULL;
        }

        int blastFurnaceTemperature = ebf.getMaxTemp();
        int recipeTemp = recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;

        if (recipeTemp > blastFurnaceTemperature) {
            return ModifierFunction.NULL;
        }

        if (RecipeHelper.getRecipeEUtTier(recipe) > ebf.getTier()) {
            return ModifierFunction.NULL;
        }

        double speedMultiplier = switch (blastFurnaceTemperature) {
            case 1800 -> 0.85;
            case 3600 -> 0.70;
            case 5400 -> 0.60;
            default -> 0.85;
        };

        return ModifierFunction.builder()
                .eutMultiplier(0.8)
                .durationMultiplier(speedMultiplier)
                .build();
    }

    public static ModifierFunction giantAcceleratedEBFModifier(MetaMachine machine, GTRecipe recipe) {
        if (!(machine instanceof GiantAcceleratedEBF ebf)) {
            return ModifierFunction.NULL;
        }

        int blastFurnaceTemperature = ebf.getMaxTemp();
        int recipeTemp = recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;

        if (recipeTemp > blastFurnaceTemperature) {
            return ModifierFunction.NULL;
        }

        if (RecipeHelper.getRecipeEUtTier(recipe) > ebf.getTier()) {
            return ModifierFunction.NULL;
        }


        double speedMultiplier = switch (blastFurnaceTemperature) {
            case 7400 -> 0.75;
            case 9300 -> 0.6;
            case 11000 -> 0.4;
            default -> 0.8;
        };

        int parallelsMax = switch (blastFurnaceTemperature) {
            case 7400 -> 2;
            case 9300 -> 4;
            case 11000 -> 8;
            default -> 2;
        };

        int parallelBoost = switch (ebf.parallelBooster){
            case 1 -> 2;
            case 2 -> 4;
            default -> 1;
        };

        parallelsMax = parallelBoost * parallelsMax;

        //This part of the code is a inspiration from StarT core - Throughput Boosting, I modified it to work with my custom coils.
        int parallelsAvailable = Math.max(0, ParallelLogic.getParallelAmountFast(machine, recipe, parallelsMax));

        if (parallelsAvailable > 0) {
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallelsAvailable))
                    .eutMultiplier(speedMultiplier)
                    .durationMultiplier(speedMultiplier)
                    .parallels(parallelsAvailable)
                    .build();
        }
        return ModifierFunction.IDENTITY;
    }

    public static ModifierFunction giantChemicalReactor(MetaMachine machine, GTRecipe recipe){

        if (!(machine instanceof GiantChemicalReactor gcr)) {
            return ModifierFunction.NULL;
        }

        int parallelsAvailableGCR = Math.max(0, ParallelLogic.getParallelAmountFast(machine, recipe, gcr.parallelBoost));

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallelsAvailableGCR))
                .eutMultiplier(gcr.energyBoost)
                .durationMultiplier(gcr.speedBoost)
                .parallels(parallelsAvailableGCR)
                .build();

    }

    public static ModifierFunction dysonSwarmGenBoost(MetaMachine machine, GTRecipe recipe){
        if (!(machine instanceof DysonSwarmEnergyCollector ds)) {
            return ModifierFunction.NULL;
        }

        double boost = 0;
        if (machine.getLevel() instanceof ServerLevel serverLevel) {
            boost = DysonSwarmManager.get(serverLevel).getBoost();
        }

        return ModifierFunction.builder()
                .eutMultiplier(boost)
                .build();
    }

    public static ModifierFunction presureChamberBoost(MetaMachine machine, GTRecipe recipe){
        if (!(machine instanceof PressureChamber ps)) {
            return ModifierFunction.NULL;
        }

        double durationMult;

        if(!ps.isSafeMode()){
            durationMult = 0.5;
        } else {
            durationMult = 1;
        }

        return ModifierFunction.builder()
                .durationMultiplier(durationMult)
                .build();
    }
    public static ModifierFunction learningEBF(MetaMachine machine, GTRecipe recipe){
        if(!(machine instanceof LearningAcceleratedEBF laebf)){
            return ModifierFunction.NULL;
        }

        int blastFurnaceTemperature = laebf.getMaxTemp();
        int recipeTemp = recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;

        if (recipeTemp > blastFurnaceTemperature) {
            return ModifierFunction.NULL;
        }

        int parallelsAvailableLEBF = Math.max(0, ParallelLogic.getParallelAmountFast(machine, recipe, laebf.getParallelBoost()));
        laebf.setCurrentBoost(parallelsAvailableLEBF);

        return ModifierFunction.builder()
                .modifyAllContents(ContentModifier.multiplier(parallelsAvailableLEBF))
                .eutMultiplier(laebf.getEnergyBoost())
                .durationMultiplier(laebf.getSpeedBoost())
                .parallels(parallelsAvailableLEBF)
                .build();
    }

    public static ModifierFunction enhancedBlastChillerCoreBoost(MetaMachine machine, GTRecipe recipe){

        if (!(machine instanceof EnhancedBlastChiller ebc)) {
            return ModifierFunction.NULL;
        }

        double speedModifier = 1 - ((ebc.getNumberOfCores() * 6.5) / 100.0);

        int parallelsAvailableEBC = Math.max(0, ParallelLogic.getParallelAmountFast(machine, recipe, ebc.getNumberOfCores() * 16));

        return ModifierFunction.builder()
                .parallels(parallelsAvailableEBC)
                .modifyAllContents(ContentModifier.multiplier(parallelsAvailableEBC))
                .durationMultiplier(speedModifier)
                .build();
    }

    public static ModifierFunction quantumSynergizerBoost(MetaMachine machine, GTRecipe recipe){

        if (!(machine instanceof QuantumSynergizer qs)) {
            return ModifierFunction.NULL;
        }

        int combo = qs.getComboCount();
        double speed = 1.0d;

        if(combo == 3){
            speed = 0.000001d;
        }

        return ModifierFunction.builder()
                .durationMultiplier(speed)
                .build();
    }

    public static ModifierFunction deepSpaceExplorerRocketBoost(MetaMachine machine, GTRecipe recipe){

        if (!(machine instanceof DeepSpaceExplorer dse)) {
            return ModifierFunction.NULL;
        }

        double speedMultiplier;
        int droneIn = dse.getDroneIn();

        switch(droneIn){
            case 2 -> speedMultiplier = 0.8;
            case 3 -> speedMultiplier = 0.6;
            default -> speedMultiplier = 1;
        }

        return ModifierFunction.builder()
                .durationMultiplier(speedMultiplier)
                .build();
    }

    public static ModifierFunction bigFreezerBoost(MetaMachine machine, GTRecipe recipe){

        if (!(machine instanceof BigFreezer bf)) {
            return ModifierFunction.NULL;
        }

        int parallelBoost = bf.getParallelBooster();
        double speedBoost;

        switch (parallelBoost){
            case 1 -> speedBoost = 0.2;
            case 2 -> speedBoost = 0.3;
            default -> speedBoost = 0.1;
        }

        int parallelsAvailableEBC = Math.max(0, ParallelLogic.getParallelAmountFast(machine, recipe, parallelBoost * 4));

        return ModifierFunction.builder()
                .parallels(parallelsAvailableEBC)
                .modifyAllContents(ContentModifier.multiplier(parallelsAvailableEBC))
                .durationMultiplier(speedBoost)
                .build();
    }
}

