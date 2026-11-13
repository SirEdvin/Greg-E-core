package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.cu5tmtp.GregECore.block.ModBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class NetherDrillRig extends WorkableElectricMultiblockMachine {

    public NetherDrillRig(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static MachineDefinition NETHERDRILLRIG = REGISTRATE
            .multiblock("netherdrillrig", NetherDrillRig::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.NETHERDRILLRIGRECIPE)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT, GTRecipeModifiers.OC_PERFECT_SUBTICK)
            .appearanceBlock(GTBlocks.CASING_INVAR_HEATPROOF)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aabaa", "accca", "accca", "accca", "cabac", "ccdcc", "ccdcc", "ccdcc", "ccccc", "ccccc")
                        .aisle("acbca", "ccccc", "ccccc", "ccccc", "acdca", "ccccc", "ccccc", "ccccc", "ccdcc", "ccccc")
                        .aisle("bbebb", "ccfcc", "ccfcc", "ccdcc", "bdddb", "dcdcd", "dcfcd", "dcfcd", "cdbdc", "ccbcc")
                        .aisle("acbca", "ccccc", "ccccc", "ccccc", "acdca", "ccccc", "ccccc", "ccccc", "ccdcc", "ccccc")
                        .aisle("aaHaa", "accca", "accca", "accca", "cabac", "ccdcc", "ccdcc", "ccdcc", "ccccc", "ccccc")

                        .where("a", Predicates.blocks(GTBlocks.CASING_STAINLESS_CLEAN.get()))
                        .where("b", Predicates.blocks(GTBlocks.CASING_INVAR_HEATPROOF.get())
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.PASSTHROUGH_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2)))
                        .where("c", Predicates.any())
                        .where("d", Predicates.blocks(GTBlocks.CASING_STEEL_SOLID.get()))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:steel_pipe_casing"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:iron_bars"))))
                        .where("H", Predicates.controller(Predicates.blocks(definition.get())))
                        .where("#", Predicates.any())
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("gtceu:block/casings/solid/machine_casing_heatproof"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower")
            )
            .register();

    public static void init() {
    }
}