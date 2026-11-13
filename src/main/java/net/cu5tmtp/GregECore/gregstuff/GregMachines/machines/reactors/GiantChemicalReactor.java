package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.reactors;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.BacteriaInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;
import static net.cu5tmtp.GregECore.item.ModItems.*;

public class GiantChemicalReactor extends WorkableElectricMultiblockMachine {
    public GiantChemicalReactor(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private final List<IItemHandler> cachedBoosterHandlers = new ArrayList<>();
    public double energyBoost = 1.0;
    public int parallelBoost = 1;
    public double speedBoost = 1.0;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedBoosterHandlers.clear();

        //This part of the code is adaptation of HPCA coolant consuming from base GTCEu - thanks for teaching me how to do that!
        for (IMultiPart part : getParts()) {

            if(!(part instanceof BacteriaInputPartMachine)){
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.cachedBoosterHandlers::add);
            }
        }
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        resetBoosts();

        for (IItemHandler handler : this.cachedBoosterHandlers) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                if (stack.is(ENERGY_BACTERIA.get())) {
                    this.energyBoost = 0.1;
                    this.parallelBoost = 1;
                    this.speedBoost = 1.0;
                    return super.beforeWorking(recipe);
                } else if (stack.is(PARALLEL_BACTERIA.get())) {
                    this.parallelBoost = 64;
                    this.energyBoost = 1.0;
                    this.speedBoost = 1.0;
                    return super.beforeWorking(recipe);
                } else if (stack.is(SPEED_BACTERIA.get())) {
                    this.speedBoost = 0.1;
                    this.energyBoost = 1.0;
                    this.parallelBoost = 1;
                    return super.beforeWorking(recipe);
                } else if (stack.is(ULTIMATE_BACTERIA.get())) {
                    this.speedBoost = 0.1;
                    this.energyBoost = 0.1;
                    this.parallelBoost = 64;
                    return super.beforeWorking(recipe);
                }
            }
        }

        return super.beforeWorking(recipe);
    }

    private void resetBoosts(){
        this.energyBoost = 1.0;
        this.parallelBoost = 1;
        this.speedBoost = 1.0;
    }

    public static MachineDefinition GIANTCHR = REGISTRATE
            .multiblock("giantchr", GiantChemicalReactor::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GTRecipeTypes.LARGE_CHEMICAL_RECIPES, GTRecipeTypes.CHEMICAL_RECIPES)
            .recipeModifiers(GregEModifiers::giantChemicalReactor, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(CASING_STEEL_SOLID)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle(" GGG ", " EEE ", " EFE ", " EEE ", " HHH ", " EEE ", " EFE ", " EEE ", " GGG ")
                        .aisle("GEEEG", "E   E", "E F E", "E   E", "H F H", "E   E", "E F E", "E   E", "GEEEG")
                        .aisle("GEFEG", "E F E", "FFFFF", "E F E", "HFFFH", "E F E", "FFFFF", "E F E", "GEFEG")
                        .aisle("GEEEG", "E   E", "E F E", "E   E", "H F H", "E   E", "E F E", "E   E", "GEEEG")
                        .aisle(" GGG ", " EEE ", " EFE ", " EEE ", " HIH ", " EEE ", " EFE ", " EEE ", " GGG ")
                        .aisle("     ", "     ", "     ", "     ", "  I  ", "     ", "     ", "     ", "     ")
                        .aisle(" CCC ", " BIB ", " BIB ", "  I  ", "  I  ", "     ", "     ", "     ", "     ")
                        .aisle(" CCC ", " BAB ", " BJB ", "     ", "     ", "     ", "     ", "     ", "     ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(CASING_STEEL_SOLID.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2)))
                        .where('C', Predicates.blocks(FIREBOX_STEEL.get()))
                        .where('E', Predicates.blocks(CASING_PTFE_INERT.get()))
                        .where('F', Predicates.blocks(GCYMBlocks.MOLYBDENUM_DISILICIDE_COIL_BLOCK.get()))
                        .where('G', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:ptfe_engine_intake"))))
                        .where('H', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:ptfe_firebox_casing"))))
                        .where('I', Predicates.blocks(CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('J', Predicates.abilities(BacteriaInputPartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1))
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gcr1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gcr2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gcr3").withStyle(s -> s.withColor(0x90EE90))
                    .append(Component.translatable("info.gregecore.gcr4").withStyle(s -> s.withColor(0xFF0000)))
                    .append(Component.translatable("info.gregecore.gcr5").withStyle(s -> s.withColor(0x90EE90))))
            .tooltips(Component.translatable("info.gregecore.gcr6").withStyle(s -> s.withColor(0x90EE90))
                    .append(Component.translatable("info.gregecore.gcr7").withStyle(s -> s.withColor(0xFF0000)))
                    .append(Component.translatable("info.gregecore.gcr8").withStyle(s -> s.withColor(0x90EE90))))
            .tooltips(Component.translatable("info.gregecore.gcr9").withStyle(s -> s.withColor(0x90EE90))
                    .append(Component.translatable("info.gregecore.gcr10").withStyle(s -> s.withColor(0xFF0000)))
                    .append(Component.translatable("info.gregecore.gcr11").withStyle(s -> s.withColor(0x90EE90))))
            .tooltips(Component.translatable("info.gregecore.gcr12").withStyle(s -> s.withColor(0x90EE90))
                    .append(Component.translatable("info.gregecore.gcr13").withStyle(s -> s.withColor(0xFF0000))))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gcr14").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.gcr15").append((int) (100 - (speedBoost * 100)) + "%").withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("info.gregecore.gcr16").append((int) (100 - (energyBoost * 100)) + "%").withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("info.gregecore.gcr17").append(parallelBoost + "").withStyle(ChatFormatting.AQUA));
        }
        super.addDisplayText(textList);
    }

    public static void init() {}
}
