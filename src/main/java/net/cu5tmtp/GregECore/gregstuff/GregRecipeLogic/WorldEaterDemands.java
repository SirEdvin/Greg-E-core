package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public final class WorldEaterDemands {
    private static List<Entry> pool;

    private WorldEaterDemands() {}

    public record Entry(Item item, int tier) {
        public int deadline() { return 600 * tier; }
        public ItemStack stack(int count) { return new ItemStack(item, count); }
    }

    public static List<Entry> pool() {
        // Material forms and optional components must already be registered.
        if (pool == null) pool = build(ForgeRegistries.ITEMS::getValue);
        return pool;
    }

    static List<Entry> build(Function<ResourceLocation, Item> lookup) {
        var entries = new ArrayList<Entry>();
        for (int tier = GTValues.LV; tier <= GTValues.UEV; tier++) {
            for (String family : List.of("electric_motor", "electric_pump", "conveyor_module", "electric_piston",
                    "robot_arm", "sensor", "emitter", "field_generator")) {
                var id = new ResourceLocation("gtceu", GTValues.VN[tier].toLowerCase(Locale.ROOT) + "_" + family);
                var item = lookup.apply(id);
                if (item != null && item != Items.AIR) entries.add(new Entry(item, tier));
            }
        }
        Material[] materials = {GTMaterials.Steel, GTMaterials.Aluminium, GTMaterials.StainlessSteel,
                GTMaterials.Titanium, GTMaterials.TungstenSteel, GTMaterials.RhodiumPlatedPalladium,
                GTMaterials.NaquadahAlloy, GTMaterials.Darmstadtium, GTMaterials.Neutronium};
        for (int i = 0; i < materials.length; i++) {
            for (TagPrefix prefix : List.of(TagPrefix.gear, TagPrefix.rodLong, TagPrefix.rotor)) {
                var stack = ChemicalHelper.get(prefix, materials[i]);
                if (!stack.isEmpty()) entries.add(new Entry(stack.getItem(), GTValues.LV + i));
            }
        }
        return List.copyOf(entries);
    }
}
