package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterReport;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;
import java.util.TreeMap;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterTamerTests {
    @GameTest(template = "empty")
    public static void registration(GameTestHelper helper) {
        var type = GregERecipeTypes.WORLD_EATER_TAMER;
        helper.assertTrue(type.registryName.equals(ResourceLocation.parse("gtceu:world_eater_tamer")), "Existing registration namespace");
        helper.assertTrue(GTRegistries.RECIPE_TYPES.get(type.registryName) == type, "Type lookup");
        helper.assertTrue(type.getMaxInputs(ItemRecipeCapability.CAP) == 1 && type.getMaxOutputs(ItemRecipeCapability.CAP) == 1, "Item IO");
        helper.assertTrue(type.getMaxInputs(FluidRecipeCapability.CAP) == 0 && type.getMaxOutputs(FluidRecipeCapability.CAP) == 0, "Fluid IO");
        helper.assertTrue(type.getMaxInputs(EURecipeCapability.CAP) == 0 && type.getMaxOutputs(EURecipeCapability.CAP) == 0, "No EU");
        var emitted = helper.getLevel().getRecipeManager().byKey(GregECore.id("world_eater_tamer/information_collection")).orElseThrow();
        helper.assertTrue(emitted instanceof com.gregtechceu.gtceu.api.recipe.GTRecipe, "Native addon recipe");
        var recipe = (com.gregtechceu.gtceu.api.recipe.GTRecipe) emitted;
        helper.assertTrue(recipe.recipeType == type && recipe.duration == 2400, "Recipe identity and base duration");
        helper.assertTrue(recipe.data.getString("required_state").equals("DORMANT")
                && recipe.data.getString("operation").equals("information_collection"), "Emitted metadata");
        helper.assertTrue(recipe.tickInputs.isEmpty() && recipe.tickOutputs.isEmpty()
                && !recipe.inputs.containsKey(EURecipeCapability.CAP), "Power-free emitted recipe");
        var definition = IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER;
        helper.assertTrue(GTRegistries.MACHINES.get(GregECore.id("incomplete_world_eater_tamer")) == definition, "Controller lookup");
        helper.assertTrue(definition.getItem().getDescription().getString().equals("Incomplete World Eater tamer"), "Controller name");
        helper.setBlock(new BlockPos(1, 1, 1), definition.defaultBlockState());
        var machine = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), helper.absolutePos(new BlockPos(1, 1, 1)));
        var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
        helper.assertTrue(logic.getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Default state");
        for (String state : new String[]{"DORMANT", "ACTIVE"}) {
            CompoundTag data = new CompoundTag();
            CompoundTag custom = new CompoundTag(); custom.putInt("Version", 1); custom.putString("State", state);
            data.put("WorldEaterTamer", custom);
            logic.loadCustomPersistedData(data);
            CompoundTag saved = new CompoundTag(); logic.saveCustomPersistedData(saved, false);
            logic.loadCustomPersistedData(saved);
            machine.onStructureInvalid();
            helper.assertTrue(logic.getProgressionState().name().equals(state), "State round trip and invalidation " + state);
        }
        logic.loadCustomPersistedData(new CompoundTag());
        helper.assertTrue(logic.getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT, "Missing state defaults");
        for (int corruption = 0; corruption < 6; corruption++) {
            CompoundTag data = new CompoundTag(), custom = new CompoundTag();
            custom.putInt("Version", 1);
            custom.putString("State", "ACTIVE");
            switch (corruption) {
                case 0 -> custom.putString("State", "UNKNOWN");
                case 1 -> custom.putInt("Version", 99);
                case 2 -> custom.putBoolean("Running", true);
                case 3 -> custom.putBoolean("ReportPending", true);
                case 4 -> custom.putInt("Attempts", 6);
                default -> {
                    CompoundTag counts = new CompoundTag(); counts.putLong("gtceu:lv_macerator", 500);
                    custom.put("Counts", counts);
                }
            }
            data.put("WorldEaterTamer", custom);
            logic.loadCustomPersistedData(data);
            helper.assertTrue(logic.getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT
                    && logic.getCounts().isEmpty() && !logic.hasPendingReport(), "Malformed data fails closed: " + corruption);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void deterministicCoverage(GameTestHelper helper) {
        for (int[] height : new int[][]{{-64, 320}, {0, 256}, {-2032, 2032}, {-16, 16}}) {
            var domain = new WorldEaterScanDomain(-7, -9, height[0], height[1]);
            long visited = 0;
            for (int tick = 0; tick < 2400; tick++) {
                long start = domain.boundary(tick), end = domain.boundary(tick + 1);
                helper.assertTrue(start == visited, "No overlaps/gaps at " + tick);
                helper.assertTrue(end - start <= domain.batchBudget(), "Bounded batch");
                visited = end;
            }
            helper.assertTrue(visited == domain.volume(), "Full coverage");
            helper.assertTrue(domain.x(0) == -176 && domain.z(0) == -208 && domain.y(0) == height[0], "First position");
            long last = domain.volume() - 1;
            helper.assertTrue(domain.x(last) == -33 && domain.z(last) == -65 && domain.y(last) == height[1] - 1, "Last position");
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void reportSerialization(GameTestHelper helper) {
        var empty = WorldEaterReport.create(Map.of(), true, 0, 0);
        helper.assertTrue(WrittenBookItem.makeSureTagIsValid(empty.getTag()), "Valid empty book");
        helper.assertTrue(text(empty.getTag()).contains("Total: 0"), "Readable zero count");
        Map<ResourceLocation, Long> counts = new TreeMap<>();
        for (int i = 0; i < 50; i++) counts.put(GregECore.id("escaped_" + i + "_" + "long_name_".repeat(30)), (long) i + 1);
        var book = WorldEaterReport.create(counts, true, 0, 0);
        helper.assertTrue(WrittenBookItem.makeSureTagIsValid(book.getTag()), "Valid long-name book");
        helper.assertTrue(book.getTag().getList("pages", Tag.TAG_STRING).size() > 1, "Multiple pages");
        helper.assertTrue(text(book.getTag()).contains("Total: 1275"), "All counts retained");
        String completeText = text(book.getTag()).replace("\n", "");
        for (var entry : counts.entrySet()) helper.assertTrue(completeText.contains(entry.getKey() + " - " + entry.getValue()), "Exact long-name entry retained");
        var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {
            buffer.writeItem(book);
            helper.assertTrue(buffer.readableBytes() < 2097152, "Native packet NBT payload bound");
            helper.assertTrue(net.minecraft.world.item.ItemStack.matches(buffer.readItem(), book), "Native item packet round trip");
        } finally {
            buffer.release();
        }
        var failed = WorldEaterReport.create(Map.of(), false, -2, 3);
        helper.assertTrue(text(failed.getTag()).contains("X -32..-17, Z 48..63"), "Block coordinates");
        helper.assertTrue(text(failed.getTag()).contains("incomplete"), "Explicit failure");
        helper.succeed();
    }

    private static String text(CompoundTag book) {
        StringBuilder text = new StringBuilder();
        var pages = book.getList("pages", Tag.TAG_STRING);
        for (int i = 0; i < pages.size(); i++) {
            text.append(Component.Serializer.fromJson(pages.getString(i)).getString());
        }
        return text.toString();
    }
}
