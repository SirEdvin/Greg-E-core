package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class GenesisCrucibleCases extends WorkableElectricMultiblockMachine {

    public GenesisCrucibleCases(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static MachineDefinition GENESISCRUCIBLECASE1 = REGISTRATE
            .multiblock("genesiscruciblecaseone", GenesisCrucibleCases::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.DUMMYRECIPE)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbbbb", "aaaaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")
                        .aisle("baaab", "adbda", "cdbdc", "cdbdc", "cbbbc", "cbbbc", "cbdbc", "cbbbc", "cbbbc", "cbdbc", "cbbbc", "cdbdc", "cdbdc", "adbda", "baaab")
                        .aisle("baeab", "abfba", "cbfbc", "cbfbc", "cbdbc", "cbdbc", "cdddc", "cbdbc", "cbdbc", "cdddc", "cbdbc", "cbfbc", "cbfbc", "abfba", "baeab")
                        .aisle("baaab", "adbda", "cdbdc", "cdbdc", "cbbbc", "cbbbc", "cbdbc", "cbbbc", "cbbbc", "cbdbc", "cbbbc", "cdbdc", "cdbdc", "adbda", "baaab")
                        .aisle("bbbbb", "aazaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")
                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_dark_gray"))))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:reinforced_delirium_block"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_red"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:iron_bars"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GregECore.id("block/machine_casing_tiled_dark_gray"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower")
            )
            .register();

    public static MachineDefinition GENESISCRUCIBLECASE2 = REGISTRATE
            .multiblock("genesiscruciblecasetwo", GenesisCrucibleCases::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.DUMMYRECIPE)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbbbb", "ccccc", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "ccccc", "bbbbb")
                        .aisle("bcccb", "cbbbc", "abbba", "abbba", "adbda", "adbda", "adbda", "abbba", "adbda", "adbda", "adbda", "abbba", "abbba", "cbbbc", "bcccb")
                        .aisle("bcecb", "cbdbc", "abdba", "abdba", "abbba", "abbba", "abbba", "abdba", "abbba", "abbba", "abbba", "abdba", "abdba", "cbdbc", "bcecb")
                        .aisle("bcccb", "cbbbc", "abbba", "abbba", "adbda", "adbda", "adbda", "abbba", "adbda", "adbda", "adbda", "abbba", "abbba", "cbbbc", "bcccb")
                        .aisle("bbbbb", "cczcc", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "caaac", "ccccc", "bbbbb")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_dark_gray"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:reinforced_kamenium_block"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_orange"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GregECore.id("block/machine_casing_tiled_dark_gray"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower")
            )
            .register();

    public static MachineDefinition GENESISCRUCIBLECASE3 = REGISTRATE
            .multiblock("genesiscruciblecasethree", GenesisCrucibleCases::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.DUMMYRECIPE)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbbbb", "aaaaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")
                        .aisle("baaab", "abbba", "cbbbc", "cbdbc", "cbdbc", "cbdbc", "cbbbc", "cbbbc", "cbbbc", "cbdbc", "cbdbc", "cbdbc", "cbbbc", "abbba", "baaab")
                        .aisle("baeab", "abdba", "cbdbc", "cdbdc", "cdbdc", "cdbdc", "cbdbc", "cbdbc", "cbdbc", "cdbdc", "cdbdc", "cdbdc", "cbdbc", "abdba", "baeab")
                        .aisle("baaab", "abbba", "cbbbc", "cbdbc", "cbdbc", "cbdbc", "cbbbc", "cbbbc", "cbbbc", "cbdbc", "cbdbc", "cbdbc", "cbbbc", "abbba", "baaab")
                        .aisle("bbbbb", "aazaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_dark_gray"))))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:supergrympl_block"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_lime"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GregECore.id("block/machine_casing_tiled_dark_gray"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower")
            )
            .register();

    public static MachineDefinition GENESISCRUCIBLECASE4 = REGISTRATE
            .multiblock("genesiscruciblecasefour", GenesisCrucibleCases::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.DUMMYRECIPE)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("bbbbb", "aaaaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")
                        .aisle("baaab", "abbba", "cbbbc", "cbbbc", "cbbbc", "cbbdc", "cbdbc", "cdbbc", "cbbbc", "cbbdc", "cbdbc", "cbbbc", "cbbbc", "abbba", "baaab")
                        .aisle("baeab", "abdba", "cbdbc", "cbdbc", "cdbdc", "cbbbc", "cbbbc", "cbbbc", "cdbdc", "cbbbc", "cbbbc", "cbdbc", "cbdbc", "abdba", "baeab")
                        .aisle("baaab", "abbba", "cbbbc", "cbbbc", "cbbbc", "cdbbc", "cbdbc", "cbbdc", "cbbbc", "cdbbc", "cbdbc", "cbbbc", "cbbbc", "abbba", "baaab")
                        .aisle("bbbbb", "aazaa", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "accca", "aaaaa", "bbbbb")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_tiled_dark_gray"))))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:xynredar_block"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_purple"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .workableCasingModel(
                    GregECore.id("block/machine_casing_tiled_dark_gray"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower")
            )
            .register();

    public static void init() {
    }
}