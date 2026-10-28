package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import io.netty.buffer.Unpooled;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.BraveryBookIngredient;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class IntentionRecipeTests {
    static final String[] SAFETY_INPUTS = {"electric_furnace", "macerator", "alloy_smelter", "arc_furnace", "assembler",
            "autoclave", "bender", "brewery", "centrifuge", "chemical_bath", "chemical_reactor", "circuit_assembler",
            "cutter", "electrolyzer", "electromagnetic_separator", "extractor"};

    static ItemStack book(int author, String page) {
        var book = BraveryBookIngredient.exampleBook();
        var tag = book.getOrCreateTag();
        tag.putString("title", "Independent title " + author);
        tag.putString("author", "Independent author " + author);
        tag.putInt("generation", author % 3);
        var pages = new ListTag();
        pages.add(StringTag.valueOf(page));
        pages.add(StringTag.valueOf("not even valid JSON on a later page"));
        tag.put("pages", pages);
        return book;
    }

    static ItemStack book(int author) {
        return book(author, Component.Serializer.toJson(Component.literal(BraveryBookIngredient.ACKNOWLEDGEMENT)));
    }

    static GTRecipe recipe(net.minecraft.world.level.Level level, boolean safety) {
        return (GTRecipe) level.getRecipeManager().byKey(GregECore.id((safety ? "assembly_line/" : "world_eater_tamer/")
                + (safety ? "safety" : "bravery") + "_intention_hatch")).orElseThrow();
    }

    @GameTest(template = "empty")
    public static void exactBookMatchingAndSerialization(GameTestHelper helper) {
        var original = BraveryBookIngredient.INSTANCE;
        var network = new FriendlyByteBuf(Unpooled.buffer());
        try {
            original.toNetwork(network);
            Ingredient[] predicates = {original, Ingredient.fromJson(original.toJson()), Ingredient.fromNetwork(network)};
            for (var predicate : predicates) {
                helper.assertTrue(!predicate.isSimple(), "Custom semantics survive JSON and network serialization");
                helper.assertTrue(predicate.getItems().length == 1 && predicate.test(predicate.getItems()[0]), "Valid candidate for lookup and JEI");
                for (String encoding : new String[]{"\"I know what I am doing\"",
                        "{\"text\":\"I know what I am doing\",\"bold\":true,\"color\":\"green\"}",
                        "[\"I know \", {\"text\":\"what I am doing\"}]",
                        "{\"text\":\"I know \",\"extra\":[{\"text\":\"what I am doing\"}]}",
                        "{\"text\":\"I know what I am do\\u0069ng\"}"}) {
                    for (int author = 0; author < 8; author++) {
                        var valid = book(author, encoding);
                        var before = valid.copy();
                        helper.assertTrue(predicate.test(valid), "Decoded exact text with different metadata: " + encoding);
                        helper.assertTrue(ItemStack.matches(before, valid), "Matching must not mutate books");
                    }
                }
                for (String text : new String[]{"", "I Know what I am doing", "I know what I am doing!",
                        " I know what I am doing", "I know what I am doing ", "I know what I am doing\n", "wrong first page"}) {
                    helper.assertTrue(!predicate.test(book(1, Component.Serializer.toJson(Component.literal(text)))), "Reject wording: " + text);
                }
                for (String page : new String[]{"{", "null", "{}", "{\"text\":[]}", "x".repeat(40000)})
                    helper.assertTrue(!predicate.test(book(1, page)), "Reject malformed or overlong page");
                helper.assertTrue(!predicate.test(null) && !predicate.test(ItemStack.EMPTY), "Null/empty reject");
                for (var item : new net.minecraft.world.item.Item[]{Items.BOOK, Items.WRITABLE_BOOK, Items.WRITTEN_BOOK})
                    helper.assertTrue(!predicate.test(new ItemStack(item)), "Wrong item or missing data rejects");
                var malformed = book(1);
                malformed.getOrCreateTag().putString("pages", BraveryBookIngredient.ACKNOWLEDGEMENT);
                helper.assertTrue(!predicate.test(malformed), "Wrong page-list type rejects");
                var pages = new ListTag(); pages.add(new CompoundTag());
                malformed.getOrCreateTag().put("pages", pages);
                helper.assertTrue(!predicate.test(malformed), "Wrong page element type rejects");
                malformed = book(1, "\"wrong\"");
                malformed.getOrCreateTag().putString("title", BraveryBookIngredient.ACKNOWLEDGEMENT);
                helper.assertTrue(!predicate.test(malformed), "Title is not first-page acknowledgement");
                var later = malformed.getOrCreateTag().getList("pages", net.minecraft.nbt.Tag.TAG_STRING);
                later.add(StringTag.valueOf("\"" + BraveryBookIngredient.ACKNOWLEDGEMENT + "\""));
                helper.assertTrue(!predicate.test(malformed), "Later page cannot qualify");
            }
        } finally { network.release(); }
        helper.succeed();
    }

    static void assertRecipe(GameTestHelper helper, GTRecipe recipe, boolean safety) {
        helper.assertTrue(recipe.recipeType == (safety ? GTRecipeTypes.ASSEMBLY_LINE_RECIPES : GregERecipeTypes.WORLD_EATER_TAMER), "Correct acquisition machine");
        helper.assertTrue(recipe.duration == 1200 && RecipeHelper.getRealEUt(recipe).getTotalEU() == (safety ? GTValues.VA[GTValues.LuV] : 0), "Base duration and machine-specific power");
        helper.assertTrue(recipe.conditions.isEmpty(), "No research or other hidden condition");
        helper.assertTrue(!recipe.inputs.containsKey(FluidRecipeCapability.CAP)
                && !recipe.tickInputs.containsKey(FluidRecipeCapability.CAP), "No fluid payment");
        helper.assertTrue(recipe.inputs.size() == 1 && recipe.tickOutputs.isEmpty()
                && (safety ? recipe.tickInputs.size() == 1 && recipe.tickInputs.containsKey(EURecipeCapability.CAP)
                : !recipe.hasTick()), "Only specified items and Safety energy");
        if (!safety) helper.assertTrue(recipe.data.getString("required_state").equals("ACTIVE")
                && recipe.data.getInt("required_casing_tier") == 2
                && recipe.data.getString("operation").equals("bravery_crafting"), "ACTIVE tier-2 ordinary craft");
        var inputs = recipe.getInputContents(ItemRecipeCapability.CAP);
        helper.assertTrue(inputs.size() == (safety ? 16 : 1), "Ordered input entry count");
        for (int i = 0; i < inputs.size(); i++) {
            Ingredient input = ItemRecipeCapability.CAP.of(inputs.get(i).content);
            var candidates = input.getItems();
            helper.assertTrue(candidates.length == 1 && candidates[0].getCount() == (safety ? 8 : 1), "Exact per-entry payment");
            if (safety) helper.assertTrue(ForgeRegistries.ITEMS.getKey(candidates[0].getItem()).toString()
                    .equals("gtceu:luv_" + SAFETY_INPUTS[i]), "Exact ordered Safety identity " + i);
            else helper.assertTrue(input.test(book(i)) && !input.test(book(i, "\"wrong\"")), "Custom predicate survives loaded recipe");
        }
        var output = recipe.getOutputContents(ItemRecipeCapability.CAP);
        var stack = ItemRecipeCapability.CAP.of(output.get(0).content).getItems()[0];
        helper.assertTrue(recipe.outputs.size() == 1 && output.size() == 1 && stack.getCount() == 1
                && stack.is((safety ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).getItem()), "Single correct hatch output");
    }

    @GameTest(template = "empty")
    public static void loadedRecipeContracts(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(GregECore.id("assembly_line/bravery_intention_hatch")).isEmpty(), "No obsolete Assembly Line Bravery recipe");
        for (var loaded : helper.getLevel().getRecipeManager().getAllRecipesFor(GregERecipeTypes.WORLD_EATER_TAMER)) {
            var captions = loaded.recipeType.getDataInfos().stream().map(info -> info.apply(loaded.data)).toList();
            helper.assertTrue(captions.stream().noneMatch(java.util.Objects::isNull), "All tamer recipes retain renderable JEI captions: " + loaded.id);
            if (loaded.id.equals(recipe(helper.getLevel(), false).id)) helper.assertTrue(
                    captions.stream().anyMatch(line -> line.contains("ACTIVE"))
                            && captions.stream().anyMatch(line -> line.contains("Casing tier 2") && line.contains("no power")),
                    "Bravery JEI captions explain both gates and power");
        }
        for (boolean safety : new boolean[]{true, false}) {
            var recipe = recipe(helper.getLevel(), safety);
            assertRecipe(helper, recipe, safety);
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                var serializer = (net.minecraft.world.item.crafting.RecipeSerializer<GTRecipe>) recipe.getSerializer();
                serializer.toNetwork(buffer, recipe);
                assertRecipe(helper, serializer.fromNetwork(recipe.id, buffer), safety);
            } finally { buffer.release(); }
            long found = helper.getLevel().getRecipeManager().getRecipes().stream().filter(r -> r instanceof GTRecipe gt
                    && gt.getOutputContents(ItemRecipeCapability.CAP).stream().anyMatch(c -> ItemRecipeCapability.CAP.of(c.content)
                    .test(new ItemStack((safety ? IntentionHatchPartMachine.SAFETY : IntentionHatchPartMachine.BRAVERY).getItem())))).count();
            helper.assertTrue(found == 1, "Exactly one acquisition recipe");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void hatchPlacementCreativeAndRecovery(GameTestHelper helper) {
        var tab = net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs.GREGE_TAB.get();
        tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        var player = net.minecraftforge.common.util.FakePlayerFactory.getMinecraft(helper.getLevel());
        player.getAbilities().instabuild = false;
        int index = 0;
        for (var definition : new com.gregtechceu.gtceu.api.machine.MachineDefinition[]{IntentionHatchPartMachine.SAFETY, IntentionHatchPartMachine.BRAVERY}) {
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(s -> s.is(definition.getItem())), "Creative hatch entry");
            var stack = new ItemStack(definition.getItem());
            helper.assertTrue(stack.getHoverName().getString().equals(index == 0 ? "Safety Hatch" : "Bravery Hatch"), "Distinct localized name");
            var below = helper.absolutePos(new net.minecraft.core.BlockPos(1 + index++ * 2, 2, 1));
            helper.getLevel().setBlockAndUpdate(below, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
            helper.getLevel().setBlockAndUpdate(below.above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            var context = new net.minecraft.world.item.context.BlockPlaceContext(helper.getLevel(), player,
                    net.minecraft.world.InteractionHand.MAIN_HAND, stack, new net.minecraft.world.phys.BlockHitResult(
                    net.minecraft.world.phys.Vec3.atCenterOf(below).add(0, .5, 0), net.minecraft.core.Direction.UP, below, false));
            helper.assertTrue(((net.minecraft.world.item.BlockItem) definition.getItem()).place(context).consumesAction() && stack.isEmpty(), "Native place/pay");
            var part = (IntentionHatchPartMachine) com.gregtechceu.gtceu.api.machine.MetaMachine.getMachine(helper.getLevel(), below.above());
            helper.assertTrue(part.getIntention() == (index == 1 ? IntentionHatchPartMachine.Intention.SAFETY : IntentionHatchPartMachine.Intention.BRAVERY), "Fixed placed variant");
            var holder = (net.minecraft.world.level.block.entity.BlockEntity) part.getHolder();
            helper.assertTrue(!holder.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER).isPresent(), "No hatch inventory");
            var drops = net.minecraft.world.level.block.Block.getDrops(helper.getLevel().getBlockState(below.above()), helper.getLevel(), below.above(), holder, player, new ItemStack(Items.IRON_PICKAXE));
            helper.assertTrue(drops.size() == 1 && drops.get(0).is(definition.getItem()) && drops.get(0).getCount() == 1, "Recover same variant");
        }
        helper.succeed();
    }
}
