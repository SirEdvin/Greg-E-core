package net.cu5tmtp.GregECore.mixin.gregtech;

import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.recipe.condition.DimensionCondition;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.dimension.CheckForDim;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = DimensionCondition.class, remap = false)
public abstract class DimensionConditionMixin {

    @Shadow
    private ResourceKey<Level> dimension;

    @Inject(
            method = "testCondition",
            at = @At("RETURN"),
            cancellable = true
    )
    private void grege$checkSimulatedDimension(GTRecipe recipe, RecipeLogic recipeLogic, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        if (CheckForDim.canSimulate(
                recipeLogic.getMachine(),
                dimension.location()
        )) {
            cir.setReturnValue(true);
        }
    }
}
