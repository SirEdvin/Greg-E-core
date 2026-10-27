package net.cu5tmtp.GregECore.gregstuff.GregUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import java.util.stream.Stream;

public final class BraveryBookIngredient extends AbstractIngredient {
    public static final String ACKNOWLEDGEMENT = "I know what I am doing";
    public static final ResourceLocation ID = GregECore.id("bravery_book");
    public static final BraveryBookIngredient INSTANCE = new BraveryBookIngredient();
    public static final IIngredientSerializer<BraveryBookIngredient> SERIALIZER = new IIngredientSerializer<>() {
        @Override public BraveryBookIngredient parse(JsonObject json) { return INSTANCE; }
        @Override public BraveryBookIngredient parse(FriendlyByteBuf buffer) { return INSTANCE; }
        @Override public void write(FriendlyByteBuf buffer, BraveryBookIngredient ingredient) {}
    };

    private BraveryBookIngredient() {
        super(Stream.of(new Ingredient.ItemValue(exampleBook())));
    }

    @Override
    public boolean test(ItemStack stack) {
        if (stack == null || !stack.is(Items.WRITTEN_BOOK) || stack.getTag() == null) return false;
        var pages = stack.getTag().getList(WrittenBookItem.TAG_PAGES, Tag.TAG_STRING);
        if (pages.isEmpty() || pages.size() > WrittenBookItem.MAX_PAGES) return false;
        String page = pages.getString(0);
        if (page.length() > WrittenBookItem.PAGE_LENGTH) return false;
        try {
            var text = Component.Serializer.fromJson(page);
            return text != null && ACKNOWLEDGEMENT.equals(text.getString());
        } catch (RuntimeException | StackOverflowError malformed) {
            // Item data can contain malformed or pathologically nested components.
            return false;
        }
    }

    @Override public boolean isSimple() { return false; }
    @Override public IIngredientSerializer<? extends Ingredient> getSerializer() { return SERIALIZER; }

    @Override
    public JsonElement toJson() {
        var json = new JsonObject();
        json.addProperty("type", ID.toString());
        return json;
    }

    public static ItemStack exampleBook() {
        var book = new ItemStack(Items.WRITTEN_BOOK);
        var tag = book.getOrCreateTag();
        tag.putString(WrittenBookItem.TAG_TITLE, "Any title");
        tag.putString(WrittenBookItem.TAG_AUTHOR, "Any author");
        tag.putBoolean(WrittenBookItem.TAG_RESOLVED, true);
        var pages = new ListTag();
        pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(ACKNOWLEDGEMENT))));
        tag.put(WrittenBookItem.TAG_PAGES, pages);
        var lore = new ListTag();
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("First page exactly: " + ACKNOWLEDGEMENT))));
        lore.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal("Any title/author; copies accepted."))));
        book.getOrCreateTagElement("display").put("Lore", lore);
        return book;
    }
}
