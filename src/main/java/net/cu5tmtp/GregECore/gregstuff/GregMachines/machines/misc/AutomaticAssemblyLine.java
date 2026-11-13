package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc;

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
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT2PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class AutomaticAssemblyLine extends WorkableElectricMultiblockMachine {

    public AutomaticAssemblyLine(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public boolean canBeThreaded = false;

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 4);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

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
    public void onStructureInvalid() {
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }
        super.onStructureInvalid();
    }

    public static MachineDefinition AUTASSEMBLYLINE = REGISTRATE
            .multiblock("autassemblyline", AutomaticAssemblyLine::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.ASSEMBLY_LINE_RECIPES)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(GCYMBlocks.CASING_LARGE_SCALE_ASSEMBLING)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aaabbbaaa", "cccbbbccc", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cicbbbcic", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cccbbbccc", "aaabbbaaa")
                        .aisle("acaaaaaca", "cecbbbcec", "dfdbbbdfd", "dfdbbbdfd", "dfdbbbdfd", "ieibbbiei", "dfdbbbdfd", "dfdbbbdfd", "dfdbbbdfd", "cecbbbcec", "acaaaaaca")
                        .aisle("aaagagaaa", "cccbbbccc", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cicbbbcic", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cccbbbccc", "aaagagaaa")
                        .aisle("baggaggab", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "baggaggab")
                        .aisle("baaaaaaab", "bbbbhbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "baaaaaaab")
                        .aisle("baggaggab", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "bbbbbbbbb", "baggaggab")
                        .aisle("aaagagaaa", "cccbbbccc", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cicbbbcic", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cccbbbccc", "aaagagaaa")
                        .aisle("acaaaaaca", "cecbbbcec", "dfdbbbdfd", "dfdbbbdfd", "dfdbbbdfd", "ieibbbiei", "dfdbbbdfd", "dfdbbbdfd", "dfdbbbdfd", "cecbbbcec", "acaaaaaca")
                        .aisle("aaabbbaaa", "cccbbbccc", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cicbbbcic", "dddbbbddd", "dddbbbddd", "dddbbbddd", "cccbbbccc", "aaabbbaaa")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:large_scale_assembler_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(4).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(4).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(PartAbility.OPTICAL_DATA_RECEPTION).setMinGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(ThreadT2PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_unit"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:assembly_line_grating"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                        .where('h', Predicates.controller(blocks(definition.getBlock())))
                        .where("i", Predicates.blocks(GreggyBlocks.SOLID_ENGINE_INTAKE.get()))
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/assembly_line"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.autassembly1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.autassembly2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.autassembly3").withStyle(style -> style.withColor(0x90EE90)))
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

    public static void init() {
    }
}
