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
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GCYMRecipeTypes;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT2PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class GiantAlloyBlastSmelter extends WorkableElectricMultiblockMachine {

    public GiantAlloyBlastSmelter(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public boolean canBeThreaded = false;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        canBeThreaded = false;

        for (IMultiPart part : getParts()) {
            if (part instanceof ThreadT2PartMachine) {
                canBeThreaded = true;
                if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
                    logic.setMultiThreaded(canBeThreaded);
                }
            }
        }
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 4);
    }

    @Override
    public void onStructureInvalid() {
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }
        super.onStructureInvalid();
    }

    public static final MachineDefinition GIANT_ABS = REGISTRATE
            .multiblock("giantabs", GiantAlloyBlastSmelter::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GCYMRecipeTypes.ALLOY_BLAST_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT, GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("bbiiiiibb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbaaaaabb")
                    .aisle("bicccccib", "bdaaaaadb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdaaaaadb", "bacccccab")
                    .aisle("iccccccci", "babbbbbab", "bbeefeebb", "bbeefeebb", "bbeefeebb", "bbeefeebb", "bbeefeebb", "babbbbbab", "accccccca")
                    .aisle("iccccccci", "babbbbbab", "bbebbbebb", "bbebbbebb", "bbebbbebb", "bbebbbebb", "bbebbbebb", "babbbbbab", "accccccca")
                    .aisle("iccccccci", "babbbbbab", "bbfbbbfbb", "bbfbbbfbb", "bbfbbbfbb", "bbfbbbfbb", "bbfbbbfbb", "babbbbbab", "acccgccca")
                    .aisle("iccccccci", "babbbbbab", "bbebbbebb", "bbebbbebb", "bbebbbebb", "bbebbbebb", "bbebbbebb", "babbbbbab", "accccccca")
                    .aisle("iccccccci", "babbbbbab", "bbeefeebb", "bbeefeebb", "bbeefeebb", "bbeefeebb", "bbeefeebb", "babbbbbab", "accccccca")
                    .aisle("bicccccib", "bdaaaaadb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdbbbbbdb", "bdaaaaadb", "bacccccab")
                    .aisle("bbiijiibb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbaaaaabb")
                    .where('a', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:high_temperature_smelting_casing"))))
                    .where('b', Predicates.any())
                    .where('c', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                    .where('d', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:black_steel_frame"))))
                    .where('e', Predicates.heatingCoils())
                    .where('f', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:steel_gearbox"))))
                    .where('g', Predicates.abilities(PartAbility.MUFFLER).setExactLimit(1))
                    .where('i', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:high_temperature_smelting_casing")))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(ThreadT2PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                    .where('j', Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            )
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gabs1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.gabs2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.threading2"))
            .register();

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic && logic.isMultiThreaded()) {
            List<MultiThreadedRecipeLogic.RecipeThread> threads = logic.getActiveThreads();
            for (int i = 0; i < threads.size(); i++) {
                var thread = threads.get(i);
                int percent = thread.duration > 0 ? (int) (((float) thread.progress / thread.duration) * 100) : 0;
                textList.add(Component.literal("  Thread " + (i + 1) + ": " + percent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
    }

    public static void init() {}
}