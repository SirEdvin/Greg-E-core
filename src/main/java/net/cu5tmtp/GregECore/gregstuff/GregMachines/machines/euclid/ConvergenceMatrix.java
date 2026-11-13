package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.recipe.GregEModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.recipe.GregERecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;


public class ConvergenceMatrix extends WorkableElectricMultiblockMachine {

    public ConvergenceMatrix(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            ConvergenceMatrix.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {

        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(true);
        }

        super.onStructureFormed();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 2);
    }

    public static MachineDefinition CONVERGENCEMATRIX = REGISTRATE
            .multiblock("convergencematrix", ConvergenceMatrix::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GCYMBlocks.CASING_NONCONDUCTING)
            .recipeTypes(GregERecipeTypes.CONVERGINGMATRIXXX)
            .recipeModifier(GregEModifiers::convergingMatrixBoost)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabccdccbaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabccdccbaaa", "aabaaaeaaabaa", "aaabccdccbaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa")
                        .aisle("aaaabcdcbaaaa", "aaabaaeaabaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aafaaaaaaafaa", "ababccdccbaba", "aafaaaaaaafaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaabaaeaabaaa", "aaaabcdcbaaaa")
                        .aisle("aaabaghgabaaa", "aababcdcbabaa", "aaabaaaaabaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "abaaaaaaaaaba", "babaahahaabab", "abaaaaaaaaaba", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabaaaaabaaa", "aababcdcbabaa", "aaabaghgabaaa")
                        .aisle("aabaaagaaabaa", "ababaagaababa", "aabaaaaaaabaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "acaaaaaaaaaca", "cacaahahaacac", "acaaaaaaaaaca", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aabaaaaaaabaa", "ababaagaababa", "aabaaagaaabaa")
                        .aisle("aacgaagaagcaa", "acacaagaacaca", "aacaaagaaacaa", "aaaaaagaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "acaaaaaaaaaca", "cachhaaahhcac", "acaaaaaaaaaca", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaagaaaaaa", "aacaaagaaacaa", "acacaagaacaca", "aacgaagaagcaa")
                        .aisle("aadhggfgghdaa", "adedgggggdeda", "aadaagggaadaa", "aaaaagggaaaaa", "aaaaaagaaaaaa", "aaaaaagaaaaaa", "adaaaaaaaaada", "dedaaaaaaaded", "adaaaaaaaaada", "aaaaaagaaaaaa", "aaaaaagaaaaaa", "aaaaagggaaaaa", "aadaagggaadaa", "adedgggggdeda", "aadhggfgghdaa")
                        .aisle("aacgaagaagcaa", "acacaagaacaca", "aacaaagaaacaa", "aaaaaagaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "acaaaaaaaaaca", "cachhaaahhcac", "acaaaaaaaaaca", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaagaaaaaa", "aacaaagaaacaa", "acacaagaacaca", "aacgaagaagcaa")
                        .aisle("aabaaagaaabaa", "ababaagaababa", "aabaaaaaaabaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "acaaaaaaaaaca", "cacaahahaacac", "acaaaaaaaaaca", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aabaaaaaaabaa", "ababaagaababa", "aabaaagaaabaa")
                        .aisle("aaabaghgabaaa", "aababcdcbabaa", "aaabaaaaabaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "abaaaaaaaaaba", "babaahahaabab", "abaaaaaaaaaba", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabaaaaabaaa", "aababcdcbabaa", "aaabaghgabaaa")
                        .aisle("aaaabcdcbaaaa", "aaabaaeaabaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aafaaaaaaafaa", "ababccdccbaba", "aafaaaaaaafaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaabaaeaabaaa", "aaaabcdcbaaaa")
                        .aisle("aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabccdccbaaa", "aabaaaeaaabaa", "aaabccdccbaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaabcdcbaaaa", "aaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaabcczccbaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa", "aaaaaaaaaaaaa")

                        .where("a", Predicates.any())
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:secure_maceration_casing"))))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_glass"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:atomic_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1)))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_coil"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:ptfe_engine_intake"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_purple"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("kubejs:machine_casing_block_magenta"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/atomic_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createConvergenceMatrixRender))
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.convergencematrix1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.convergencematrix2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.convergencematrix3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    public static void init() {
    }
}