package net.cu5tmtp.GregECore.block;

import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.block.IFilterType;
import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.common.data.models.GTModels;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.cleanroom.DimensionalFilterType;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraftforge.client.model.generators.ModelFile;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

@SuppressWarnings("removal")
public class GreggyBlocks {

    private static BlockEntry<Block> worldEaterCasing(String suffix, String displayName) {
        String name = "world_eater_tamer_casing" + suffix;
        return REGISTRATE.block(name, Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(5.0f, 6.0f).sound(SoundType.METAL))
                .lang(displayName)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.get(),
                        new ModelFile.UncheckedModelFile(GregECore.id("block/" + name))))
                .loot((prov, block) -> prov.dropSelf(block))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, CustomTags.TOOL_TIERS[1])
                .item(BlockItem::new)
                .model((ctx, prov) -> prov.withExistingParent(ctx.getName(), GregECore.id("block/" + name)))
                .build()
                .register();
    }

    public static final BlockEntry<Block> WORLD_EATER_TAMER_CASING = worldEaterCasing("", "World Eater Tamer Casing");
    public static final BlockEntry<Block> WORLD_EATER_TAMER_CASING_PURPLE = worldEaterCasing("_purple", "Dark Purple World Eater Casing");
    public static final BlockEntry<Block> WORLD_EATER_TAMER_CASING_GREEN = worldEaterCasing("_green", "Dark Green World Eater Casing");
    public static final BlockEntry<Block> WORLD_EATER_TAMER_CASING_ORANGE = worldEaterCasing("_orange", "Very Dark Orange World Eater Casing");

    //Thanks to StarT core for teaching me how to do this
    protected static BlockEntry<ActiveBlock> createActiveBlock(String name, ResourceLocation baseTexture) {
        return REGISTRATE.block(name, ActiveBlock::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .addLayer(() -> RenderType::cutoutMipped)
                .blockstate((ctx, prov) -> {
                    ModelFile inactive = prov.models().cubeAll(ctx.getName(), baseTexture);
                    ResourceLocation activeTexLoc = new ResourceLocation(baseTexture.getNamespace(), baseTexture.getPath() + "_active");
                    ModelFile active = prov.models().withExistingParent(ctx.getName() + "_active", "minecraft:block/cube_all")
                            .texture("all", activeTexLoc);

                    prov.getVariantBuilder(ctx.get())
                            .partialState().with(GTBlockStateProperties.ACTIVE, false)
                            .modelForState().modelFile(inactive).addModel()
                            .partialState().with(GTBlockStateProperties.ACTIVE, true)
                            .modelForState().modelFile(active).addModel();
                })
                .item(BlockItem::new)
                .build()
                .register();
    }

    private static BlockEntry<Block> createCleanroomFilters(IFilterType filterType) {
        var filterBlock = REGISTRATE.block(filterType.getSerializedName(), Block::new)
                .initialProperties(() -> Blocks.IRON_BLOCK)
                .properties(properties -> properties.strength(2.0f, 8.0f).sound(SoundType.METAL)
                        .isValidSpawn((blockState, blockGetter, blockPos, entityType) -> false))
                .blockstate(GTModels.createCleanroomFilterModel(filterType))
                .tag(CustomTags.MINEABLE_WITH_CONFIG_VALID_PICKAXE_WRENCH, CustomTags.TOOL_TIERS[1])
                .item(BlockItem::new)
                .build()
                .register();
        GTCEuAPI.CLEANROOM_FILTERS.put(filterType, filterBlock);
        return filterBlock;
    }

    public static final BlockEntry<Block> BLAZING_CLEANROOM_FILTER_CASING = createCleanroomFilters(
                    DimensionalFilterType.FILTER_CASING_DIMENSIONAL);

    public static final BlockEntry<ActiveBlock> MANASTEEL_COIL = createActiveBlock("manasteel_coil",
            GregECore.id("block/coils/machine_coil_manasteel"));

    public static final BlockEntry<ActiveBlock> TWILIGHT_COIL = createActiveBlock("twilight_coil",
            GregECore.id("block/coils/machine_coil_twilight"));

    public static final BlockEntry<ActiveBlock> DESH_COIL = createActiveBlock("desh_coil",
            GregECore.id("block/coils/machine_coil_desh"));

    public static final BlockEntry<ActiveBlock> MALACHITE_COIL = createActiveBlock("malachite_coil",
            GregECore.id("block/coils/machine_coil_malachite"));

    public static final BlockEntry<ActiveBlock> FORGOTTEN_COIL = createActiveBlock("forgotten_coil",
            GregECore.id("block/coils/machine_coil_forgotten"));

    public static final BlockEntry<ActiveBlock> SUPERELEMENT_COIL = createActiveBlock("superelement_coil",
            GregECore.id("block/coils/machine_coil_superelement"));

    public static final BlockEntry<ActiveBlock> PTFE_ENGINE_INTAKE = createActiveBlock("ptfe_engine_intake",
            GregECore.id("block/engines/ptfe_engine_intake"));

    public static final BlockEntry<ActiveBlock> VIBRATION_ENGINE_INTAKE = createActiveBlock("vibration_engine_intake",
            GregECore.id("block/engines/vibration_engine_intake"));

    public static final BlockEntry<ActiveBlock> PTFE_FIREBOX = createActiveBlock("ptfe_firebox_casing",
            GregECore.id("block/firebox/ptfe_firebox"));

    public static final BlockEntry<ActiveBlock> SHOCKPROOF_ENGINE = createActiveBlock("shockproof_engine",
            GregECore.id("block/engines/shockproof_engine_intake"));

    public static final BlockEntry<ActiveBlock> AW_DRACONIUM_COIL = createActiveBlock("awakened_draconium_coil",
            GregECore.id("block/coils/machine_coil_aw_draconium"));

    public static final BlockEntry<ActiveBlock> ATOMIC_ENGINE_INTAKE = createActiveBlock("atomic_engine_intake",
            GregECore.id("block/engines/atomic_engine_intake"));

    public static final BlockEntry<ActiveBlock> SOLID_ENGINE_INTAKE = createActiveBlock("solid_engine_intake",
            GregECore.id("block/engines/solid_engine_intake"));

    public static final BlockEntry<ActiveBlock> ASSEMBLY_ENGINE_INTAKE = createActiveBlock("assembly_engine_intake",
            GregECore.id("block/engines/assembly_engine_intake"));

    public static final void init(){}
}


