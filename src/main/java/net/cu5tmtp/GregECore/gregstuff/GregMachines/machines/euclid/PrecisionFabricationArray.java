package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;


public class PrecisionFabricationArray extends WorkableElectricMultiblockMachine {

    public PrecisionFabricationArray(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    // This code is similar to StarTDraconicInfusionMachine - I learned it there.
    private static final List<Integer> RECIPE_INPUT_MAP = List.of(
            0,
            1,
            2,
            3,
            4,
            5
    );

    public static Comparator<IMultiPart> partSorter(MultiblockControllerMachine mc) {
        Comparator<IMultiPart> backSort = Comparator.comparing(p -> p.self().getPos(),
                RelativeDirection.BACK.getSorter(mc.getFrontFacing(), mc.getUpwardsFacing(), mc.isFlipped()));

        Comparator<IMultiPart> leftSort = Comparator.comparing(p -> p.self().getPos(),
                RelativeDirection.LEFT.getSorter(mc.getFrontFacing(), mc.getUpwardsFacing(), mc.isFlipped()));

        return backSort.thenComparing(leftSort);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (recipe == null) return false;

        var itemInputs = recipe.inputs.getOrDefault(ItemRecipeCapability.CAP, Collections.emptyList());
        if (itemInputs.isEmpty()) return true;

        int inputsSize = itemInputs.size();
        var itemHandlers = getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
        if (itemHandlers.size() < inputsSize) return false;

        var itemInventory = itemHandlers.stream()
                .filter(IRecipeHandler::shouldSearchContent)
                .map(container -> container.getContents().stream()
                        .filter(ItemStack.class::isInstance)
                        .map(ItemStack.class::cast)
                        .filter(s -> !s.isEmpty())
                        .findFirst())
                .limit(inputsSize)
                .map(o -> o.orElse(ItemStack.EMPTY))
                .toList();

        if (itemInventory.size() < inputsSize) return false;

        for (int i = 0; i < inputsSize; i++) {
            var itemStack = itemInventory.get(RECIPE_INPUT_MAP.get(i));
            Ingredient recipeStack = ItemRecipeCapability.CAP.of(itemInputs.get(i).content);
            if (!recipeStack.test(itemStack)) {
                return false;
            }
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureFormed() {
        getDefinition().setPartSorter(PrecisionFabricationArray::partSorter);
        super.onStructureFormed();
    }
    public static MachineDefinition PFARRAY = REGISTRATE
            .multiblock("pfarray", PrecisionFabricationArray::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .recipeTypes(GregERecipeTypes.PFARRAYCRAFT)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbaaaaabb", "bacccccab", "acdddddca", "acdddddca", "acdddddca", "acdddddca", "acdddddca", "bacccccab", "bbaaaaabb")
                        .aisle("bacccccab", "acbbbbbca", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "acbbbbbca", "bacccccab")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceexeeca", "cbbbfbbbc", "dbbgfgbbd", "dbbgfgbbd", "dbbgkgbbd", "dbbikibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("acyeeeeca", "cbfbbbbbc", "dbfgggbbd", "dbfffgbbd", "dbbhlhbbd", "dbbjmibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeyca", "cbbbbbfbc", "dbbgggfbd", "dbbgfffbd", "dbbhlhbbd", "dbbimjbbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("acyeeeeca", "cbfbbbbbc", "dbfgggbbd", "dbfffgbbd", "dbbhlhbbd", "dbbjmibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeyca", "cbbbbbfbc", "dbbgggfbd", "dbbgfffbd", "dbbhlhbbd", "dbbimjbbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("acyeeeeca", "cbfbbbbbc", "dbfgggbbd", "dbfffgbbd", "dbbhlhbbd", "dbbjmibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbgggbbd", "dbbgfgbbd", "dbbhlhbbd", "dbbimibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeyeeca", "cbbbfbbbc", "dbbgfgbbd", "dbbgfgbbd", "dbbgkgbbd", "dbbikibbd", "dbbbkbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("aceeeeeca", "cbbbbbbbc", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "dbbbbbbbd", "cbbbbbbbc", "acdddddca")
                        .aisle("bacccccab", "acbbbbbca", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "cbbbbbbbc", "acbbbbbca", "bacccccab")
                        .aisle("bbaazaabb", "bacccccab", "acdddddca", "acdddddca", "acdddddca", "acdddddca", "acdddddca", "bacccccab", "bbaaaaabb")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing")))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2)))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_very_dark_gray"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:secure_maceration_casing"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:ptfe_pipe_casing"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:large_scale_assembler_casing"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("i", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:computer_casing"))))
                        .where("j", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:grege_heat_sink_component"))))
                        .where("k", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_casing"))))
                        .where("l", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_grating"))))
                        .where("m", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:advanced_computer_casing"))))
                        .where("y", Predicates.abilities(PartAbility.IMPORT_ITEMS).setMinGlobalLimited(6, 6))
                        .where("x", Predicates.abilities(PartAbility.EXPORT_ITEMS).setMinGlobalLimited(1, 1))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/distillation_tower")
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fabricationarray1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fabricationarray2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fabricationarray3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    public static void init() {
    }
}