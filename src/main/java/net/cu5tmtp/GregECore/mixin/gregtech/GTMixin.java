package net.cu5tmtp.GregECore.mixin.gregtech;

import com.gregtechceu.gtceu.api.GTValues;
import net.minecraft.ChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GTValues.class, remap = false)
public class GTMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void changeUIVName(CallbackInfo ci) {
        GTValues.VNF[10] = ChatFormatting.GREEN + "AVA";
        GTValues.VNF[11] = ChatFormatting.YELLOW + "Euc";
    }
}