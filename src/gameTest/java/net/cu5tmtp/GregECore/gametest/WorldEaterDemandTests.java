package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterDemandTests {
    static GTRecipe recipe(net.minecraft.world.level.Level level, boolean test) {
        return (GTRecipe) level.getRecipeManager().byKey(GregECore.id("world_eater_tamer/" + (test ? "preparation_test" : "essence_production"))).orElseThrow();
    }

    @GameTest(template = "empty")
    @SuppressWarnings("unchecked")
    public static void demandRecipeSerializationAndHelp(GameTestHelper helper) {
        for (boolean test : new boolean[]{true, false}) {
            var r = recipe(helper.getLevel(), test);
            helper.assertTrue(WorldEaterTamerRecipeLogic.isDemandRecipe(r), "Loaded exact power-free single recipe");
            var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {
                var serializer = (net.minecraft.world.item.crafting.RecipeSerializer<GTRecipe>) r.getSerializer();
                serializer.toNetwork(buffer, r);
                helper.assertTrue(WorldEaterTamerRecipeLogic.isDemandRecipe(serializer.fromNetwork(r.id, buffer)), "Network round trip");
            } finally { buffer.release(); }
            var captions = r.recipeType.getDataInfos().stream().map(info -> info.apply(r.data)).toList();
            helper.assertTrue(captions.stream().noneMatch(s -> s == null || s.isBlank())
                    && captions.stream().anyMatch(s -> s.contains("PREPARED"))
                    && captions.stream().anyMatch(s -> s.contains("immediately"))
                    && captions.stream().anyMatch(s -> s.contains("Deadline")), "Readable outcome and deadline captions");
        }
        for (var hatch : java.util.List.of(IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY)) {
            var text = new ItemStack(hatch.getItem()).getTooltipLines(null, net.minecraft.world.item.TooltipFlag.Default.NORMAL)
                    .stream().map(net.minecraft.network.chat.Component::getString).reduce("", (a, b) -> a + " " + b);
            helper.assertTrue(text.contains("timeout") && text.contains("No collateral") && text.contains("cancellations")
                    && text.contains("rear face center") && !text.contains("future recipes") && !text.contains("five"), "Implemented intention help: " + text);
        }
        helper.succeed();
    }
}
