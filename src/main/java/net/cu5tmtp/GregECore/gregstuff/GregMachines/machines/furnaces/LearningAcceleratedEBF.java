package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT3PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_LASER_SAFE_ENGRAVING;
import static com.gregtechceu.gtceu.common.data.GCYMBlocks.CASING_SHOCK_PROOF;
import static com.gregtechceu.gtceu.common.data.GTBlocks.*;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class LearningAcceleratedEBF extends WorkableElectricMultiblockMachine {
    public LearningAcceleratedEBF(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            LearningAcceleratedEBF.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER
    );

    @Persisted
    protected int tier1 = 0;
    @Persisted
    protected int tier2 = 0;
    @Persisted
    protected int tier3 = 0;
    @Persisted
    protected int tier4 = 0;
    @Persisted
    protected int tier5 = 0;

    int recipeTemp = 0;
    int currentBoost = 0;
    int coilTemp = 12500;

    public boolean canBeThreaded = false;

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {

        super.onStructureFormed();

        for (IMultiPart part : getParts()) {

            if (part instanceof ThreadT3PartMachine) {
                canBeThreaded = true;
                if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
                    logic.setMultiThreaded(canBeThreaded);
                }
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }
        super.onStructureInvalid();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 8);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        assert recipe != null;
        recipeTemp = recipe.data.contains("ebf_temp") ? recipe.data.getInt("ebf_temp") : 0;
        return super.beforeWorking(recipe);
    }

    @Override
    public void afterWorking() {
        if (recipeTemp >= 9000) {
            if (tier5 < 1000) {
                tier5 = Math.min(1000, tier5 + currentBoost);
            }
        } else if (recipeTemp >= 7000) {
            if (tier4 < 2000) {
                tier4 = Math.min(2000, tier4 + currentBoost);
            }
        } else if (recipeTemp >= 5000) {
            if (tier3 < 4000) {
                tier3 = Math.min(4000, tier3 + currentBoost);
            }
        } else if (recipeTemp >= 3000) {
            if (tier2 < 10000) {
                tier2 = Math.min(10000, tier2 + currentBoost);
            }
        } else if (recipeTemp >= 1) {
            if (tier1 < 15000) {
                tier1 = Math.min(15000, tier1 + currentBoost);
            }
        }
        super.afterWorking();
    }


    public double getEnergyBoost() {
        double mod = 0;
        if (this.tier1 >= 15000) mod += 0.5;
        if (this.tier2 >= 10000) mod += 0.3;
        if (this.tier3 >= 4000)  mod += 0.15;
        return 1.0 - mod;
    }

    public double getSpeedBoost() {
        double mod = 0;
        if (this.tier2 >= 10000) mod += 0.5;
        if (this.tier3 >= 4000)  mod += 0.3;
        if (this.tier4 >= 2000)  mod += 0.15;
        return 1.0 - mod;
    }

    public int getParallelBoost() {
        int mod = 0;
        if (this.tier3 >= 4000) mod += 16;
        if (this.tier4 >= 2000) mod += 80;
        if (this.tier5 >= 1000) mod += 160;
        return (mod == 0) ? 1 : mod;
    }

    public static MachineDefinition LEARNING_ACC_EBF = REGISTRATE
            .multiblock("learning_acc_ebf", LearningAcceleratedEBF::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GregEModifiers::learningEBF, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(CASING_SHOCK_PROOF)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("   BBBBB   ", "     H     ", "     H     ", "     H     ", "     H     ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("  BHGGGHB  ", "           ", "           ", "           ", "           ", "     H     ", "     H     ", "     H     ", "   HHDHH   ", "     H     ", "     H     ", "     H     ", "     H     ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle(" BGHGGGHGB ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  HGGGGGH  ", "           ", "           ", "           ", "           ", "     H     ", "     H     ", "    HDH    ", "           ", "           ", "           ", "    JJJ    ")
                        .aisle("BHHHHHHHHHB", "   FCCCF   ", "   FCCCF   ", "   FCCCF   ", "   EEEEE   ", "   FCCCF   ", "   FCCCF   ", "   FCCCF   ", " HGGHHHGGH ", "   I   I   ", "   I   I   ", "   I   I   ", "   IEEEI   ", "   I   I   ", "   I   I   ", "   HGGGH   ", "   I   I   ", "   I   I   ", "   I   I   ", "   JHHHJ   ")
                        .aisle("BGGHHHHHGGB", "   C   C   ", "   C   C   ", "   C   C   ", "   E   E   ", "   C   C   ", "   C   C   ", "   C   C   ", " HGH   HGH ", "    FCF    ", "    FCF    ", "    FCF    ", "   E   E   ", "    FCF    ", "    FCF    ", "  HGHHHGH  ", "    FCF    ", "    FEF    ", "    FCF    ", "  JHHHHHJ  ")
                        .aisle("BGGHHHHHGGB", "H  C   C  H", "H  C   C  H", "H  C   C  H", "H  E   E  H", " H C   C H ", " H C   C H ", " H C   C H ", " DGH   HGD ", " H  C C  H ", " H  C C  H ", " H  C C  H ", " H E   E H ", "  H C C H  ", "  H C C H  ", "  DGH HGD  ", "    C C    ", "    E E    ", "    C C    ", "  JHHKHHJ  ")
                        .aisle("BGGHHHHHGGB", "   C   C   ", "   C   C   ", "   C   C   ", "   E   E   ", "   C   C   ", "   C   C   ", "   C   C   ", " HGH   HGH ", "    FCF    ", "    FCF    ", "    FCF    ", "   E   E   ", "    FCF    ", "    FCF    ", "  HGHHHGH  ", "    FCF    ", "    FEF    ", "    FCF    ", "  JHHHHHJ  ")
                        .aisle("BHHHHHHHHHB", "   FCCCF   ", "   FCCCF   ", "   FCCCF   ", "   EEEEE   ", "   FCCCF   ", "   FCCCF   ", "   FCCCF   ", " HGGHHHGGH ", "   I   I   ", "   I   I   ", "   I   I   ", "   IEEEI   ", "   I   I   ", "   I   I   ", "   HGGGH   ", "   I   I   ", "   I   I   ", "   I   I   ", "   JHHHJ   ")
                        .aisle(" BGHGGGHGB ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  I     I  ", "  HGGGGGH  ", "           ", "           ", "           ", "           ", "     H     ", "     H     ", "    HDH    ", "           ", "           ", "           ", "    JJJ    ")
                        .aisle("  BHGGGHB  ", "           ", "           ", "           ", "           ", "     H     ", "     H     ", "     H     ", "   HHDHH   ", "     H     ", "     H     ", "     H     ", "     H     ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .aisle("   BBABB   ", "     H     ", "     H     ", "     H     ", "     H     ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ", "           ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(CASING_SHOCK_PROOF.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(ThreadT3PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('C', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:awakened_draconium_coil"))))
                        .where('D', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:shockproof_engine"))))
                        .where('E', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:ptfe_engine_intake"))))
                        .where('F', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:ptfe_firebox_casing"))))
                        .where('G', Predicates.blocks(CASING_HSSE_STURDY.get()))
                        .where('H', Predicates.blocks(CASING_SHOCK_PROOF.get()))
                        .where('I', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:hsss_frame"))))
                        .where('J', Predicates.blocks(CASING_LASER_SAFE_ENGRAVING.get()))
                        .where('K', Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1).setPreviewCount(1))
                        .where(' ', Predicates.any())
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/shock_proof_cutting_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.laebf1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.laebf2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.laebf3").withStyle(style -> style.withColor(0xFF8C00)))
            .tooltips(Component.translatable("info.gregecore.laebf4").withStyle(style -> style.withColor(0xFF8C00)))
            .tooltips(Component.translatable("info.gregecore.laebf5").withStyle(style -> style.withColor(0xFFEE00)))
            .tooltips(Component.translatable("info.gregecore.laebf6").withStyle(style -> style.withColor(0xFFEE00)))
            .tooltips(Component.translatable("info.gregecore.laebf7").withStyle(style -> style.withColor(0x4DE94C)))
            .tooltips(Component.translatable("info.gregecore.laebf8").withStyle(style -> style.withColor(0x4DE94C)))
            .tooltips(Component.translatable("info.gregecore.laebf9").withStyle(style -> style.withColor(0x3783FF)))
            .tooltips(Component.translatable("info.gregecore.laebf10").withStyle(style -> style.withColor(0x3783FF)))
            .tooltips(Component.translatable("info.gregecore.laebf11").withStyle(style -> style.withColor(0x2266dd)))
            .tooltips(Component.translatable("info.gregecore.laebf12").withStyle(style -> style.withColor(0x2266dd)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.laebf13").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.threading3"))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.laebf14").withStyle(ChatFormatting.AQUA)
                    .append(tier1 >= 15000 ? Component.literal("DONE!").withStyle(ChatFormatting.GREEN) : Component.literal(tier1 + "/" + 15000).withStyle(ChatFormatting.AQUA)));

            textList.add(Component.translatable("info.gregecore.laebf15").withStyle(ChatFormatting.AQUA)
                    .append(tier2 >= 10000 ? Component.literal("DONE!").withStyle(ChatFormatting.GREEN) : Component.literal(tier2 + "/" + 10000).withStyle(ChatFormatting.AQUA)));

            textList.add(Component.translatable("info.gregecore.laebf16").withStyle(ChatFormatting.AQUA)
                    .append(tier3 >= 4000 ? Component.literal("DONE!").withStyle(ChatFormatting.GREEN) : Component.literal(tier3 + "/" + 4000).withStyle(ChatFormatting.AQUA)));

            textList.add(Component.translatable("info.gregecore.laebf17").withStyle(ChatFormatting.AQUA)
                    .append(tier4 >= 2000 ? Component.literal("DONE!").withStyle(ChatFormatting.GREEN) : Component.literal(tier4 + "/" + 2000).withStyle(ChatFormatting.AQUA)));

            textList.add(Component.translatable("info.gregecore.laebf18").withStyle(ChatFormatting.AQUA)
                    .append(tier5 >= 1000 ? Component.literal("DONE!").withStyle(ChatFormatting.GREEN) : Component.literal(tier5 + "/" + 1000).withStyle(ChatFormatting.AQUA)));

            if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic && logic.isMultiThreaded()) {
                List<MultiThreadedRecipeLogic.RecipeThread> threads = logic.getActiveThreads();
                for (int i = 0; i < threads.size(); i++) {
                    var thread = threads.get(i);
                    int percent = thread.duration > 0 ? (int) (((float) thread.progress / thread.duration) * 100) : 0;
                    textList.add(Component.literal("  Thread " + (i + 1) + ": " + percent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }

        }
    }

    public int getMaxTemp() {
        return this.coilTemp;
    }

    public void setCurrentBoost(int currentBoostEBF) {
        this.currentBoost = currentBoostEBF;
    }

    public static void init() {}
}