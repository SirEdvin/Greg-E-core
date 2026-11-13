package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame;

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
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT3PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class SpecializedAssemblyLine extends WorkableElectricMultiblockMachine {

    public SpecializedAssemblyLine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public boolean canBeThreaded = false;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        canBeThreaded = false;

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
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 8);
    }

    @Override
    public void onStructureInvalid() {
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }
        super.onStructureInvalid();
    }

    public static final MachineDefinition SPECIALIZED_ASSEMBLY_LINE = REGISTRATE
            .multiblock("specializedassemblyline", SpecializedAssemblyLine::new)
            .rotationState(RotationState.ALL)
            .recipeType(GregERecipeTypes.SPECIALIZEDASSEMBLYLINE)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT, GTRecipeModifiers.PARALLEL_HATCH)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("aabbaIabbaa", "abcccdcccba", "bccccdccccb", "bccccdccccb", "bccccdccccb", "abcccdcccba", "aabbaIabbaa")
                    .aisle("abaaaIaaaba", "beeeeeeeeeb", "ceeeeeeeeec", "ceeeedeeeec", "ceeeeeeeeec", "beeeeeeeeeb", "abaaaIaaaba")
                    .aisle("baaaaIaaaab", "ceeeeeeeeec", "ceeeeeeeeec", "dfffffffffd", "ceeeeeeeeec", "ceeeeeeeeec", "baaaaIaaaab")
                    .aisle("IIIIIIIIIII", "deeeedeeeed", "dfffffffffd", "ggggggggggg", "dfffffffffd", "deeeedeeeed", "IIIIIIIIIII")
                    .aisle("baaaaIaaaab", "ceeeeeeeeec", "ceeeeeeeeec", "dfffffffffd", "ceeeeeeeeec", "ceeeeeeeeec", "baaaaIaaaab")
                    .aisle("abaaaIaaaba", "beeeeeeeeeb", "ceeeeeeeeec", "ceeeedeeeec", "ceeeeeeeeec", "beeeeeeeeeb", "abaaaIaaaba")
                    .aisle("aabbaJabbaa", "abcccdcccba", "bccccdccccb", "bccccdccccb", "bccccdccccb", "abcccdcccba", "aabbaIabbaa")
                    .where('a', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                    .where('b', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing"))))
                    .where('c', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                    .where('d', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:high_temperature_smelting_casing"))))
                    .where('e', Predicates.any())
                    .where('f', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_grating"))))
                    .where('g', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_unit"))))
                    .where('J', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('I', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:high_temperature_smelting_casing")))
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                            .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                            .or(Predicates.abilities(ThreadT3PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1))
                    )
                    .build()
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.specassembly1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.specassembly2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.threading3"))
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/distillation_tower")
            )
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
