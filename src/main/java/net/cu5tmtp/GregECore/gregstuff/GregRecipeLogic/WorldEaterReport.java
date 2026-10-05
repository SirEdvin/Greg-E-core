package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class WorldEaterReport {
    private WorldEaterReport() {}

    public static ItemStack create(Map<ResourceLocation, Long> counts, boolean success, int failedX, int failedZ) {
        List<String> lines = new ArrayList<>();
        lines.add(success ? "Scan complete" : "Scan FAILED — incomplete");
        if (!success) {
            lines.add("Unavailable area: X " + ((long) failedX * 16) + ".." + ((long) failedX * 16 + 15)
                    + ", Z " + ((long) failedZ * 16) + ".." + ((long) failedZ * 16 + 15));
        }
        long total = 0;
        for (var entry : new TreeMap<>(counts).entrySet()) {
            var definition = GTRegistries.MACHINES.get(entry.getKey());
            String name = definition == null ? entry.getKey().toString() : definition.getItem().getDescription().getString();
            lines.add(name + " - " + entry.getValue());
            total = Math.addExact(total, entry.getValue());
        }
        lines.add((success ? "Total: " : "Partial count: ") + total);
        ListTag pages = new ListTag();
        StringBuilder page = new StringBuilder();
        // Conservative readable pages; split long names without losing characters.
        for (String line : lines) {
            String remaining = line;
            do {
                int end = Math.min(remaining.length(), 200);
                if (end < remaining.length() && end > 0 && Character.isHighSurrogate(remaining.charAt(end - 1))) end--;
                String part = remaining.substring(0, end);
                if (page.length() + part.length() + 1 > 240) {
                    pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page.toString()))));
                    page.setLength(0);
                }
                if (!page.isEmpty()) page.append('\n');
                page.append(part);
                remaining = remaining.substring(end);
            } while (!remaining.isEmpty());
        }
        if (!page.isEmpty()) pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(page.toString()))));
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = book.getOrCreateTag();
        tag.putString("title", success ? "World Eater Survey" : "World Eater Scan Failure");
        tag.putString("author", "World Eater tamer");
        tag.putBoolean("resolved", true);
        tag.putInt("generation", 0);
        tag.put("pages", pages);
        return book;
    }
}
