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
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class SubatomicAntimatterSyntheticator extends WorkableElectricMultiblockMachine {

    public SubatomicAntimatterSyntheticator(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public static final MachineDefinition SASYNTH = REGISTRATE
            .multiblock("sasynth", SubatomicAntimatterSyntheticator::new)
            .rotationState(RotationState.ALL)
            .recipeType(GregERecipeTypes.SASCRAFTING)
            .recipeModifier(GTRecipeModifiers.OC_NON_PERFECT)
            .appearanceBlock(GCYMBlocks.CASING_SECURE_MACERATION)
            .pattern(definition -> FactoryBlockPattern.start()
                    .aisle("aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa")
                    .aisle("abdddbbbaaaaaaabbbdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaabaaabaaabdddba", "aagggdeeedgggdeeedgggaa", "aagggdfffdgggdfffdgggaa", "aagggdeeedgggdeeedgggaa", "abdddbaaabaaabaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbbbaaaaaaabbbdddba")
                    .aisle("abdddbccbbaaabbccbdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaaaaaaaaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaabaaabaaabdddba", "aaghadfffdgggdfffdahgaa", "aagihdaaaihihiaaadhigaa", "aaghgdfffdgggdfffdghgaa", "abdddbaaabaaabaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaaaaaaaaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbccbbaaabbccbdddba")
                    .aisle("abdddbbbccbbbccbbbdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaabaaabaaabdddba", "aagggdeeedgggdeeedgggaa", "aagggdfffdgggdfffdgggaa", "aagggdeeedgggdeeedgggaa", "abdddbaaabaaabaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbbbccbbbccbbbdddba")
                    .aisle("aabbbaaabbcbcbbaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaabbcbcbbaaabbbaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aabbbaaaaacbcaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaacbcaaaaabbbaa")
                    .aisle("abdddbaaacbbbcaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "ajjjjjaaaaaaaaaaajjjjja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajjjjjaaaaaaaaaaajjjjja", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaacbbbcaaabdddba")
                    .aisle("bdddddbccbbbbbccbdddddb", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "bdddddbaaaaaaaaabdddddb", "ajdddjaaaaaaaaaaajdddja", "agadagaaaaaaaaaaagadaga", "agaaagaaaaaaaaaaagaaaga", "agaaagaaaaaaaaaaagaaaga", "agaaagaaaaaaaaaaagaaaga", "agadagaaaaaaaaaaagadaga", "ajdddjaaaaaaaaaaajdddja", "bdddddbaaaaaaaaabdddddb", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "bdddddbccbbbbbccbdddddb")
                    .aisle("bdddddbbbbbbbbbbbdddddb", "afaaafaaaaalaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "bdddddbaaaaaaaaabdddddb", "ajdddjaaaaaaaaaaajdddja", "agdddgaaaaaaaaaaagdddga", "agakagaaaaaaaaaaagakaga", "agaaagaaaaaaaaaaagaaaga", "agakagaaaaaaaaaaagakaga", "agdddgaaaaaaaaaaagdddga", "ajdddjaaaaaaaaaaajdddja", "bdddddbaaaaaaaaabdddddb", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "afaaafaaaaaaaaaaafaaafa", "bdddddbbbbbbbbbbbdddddb")
                    .aisle("bdddddbccbbbbbccbdddddb", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "bdddddbaaaaaaaaabdddddb", "ajdddjaaaaaaaaaaajdddja", "agadagaaaaaaaaaaagadaga", "agaaagaaaaaaaaaaagaaaga", "agaaagaaaaaaaaaaagaaaga", "agaaagaaaaaaaaaaagaaaga", "agadagaaaaaaaaaaagadaga", "ajdddjaaaaaaaaaaajdddja", "bdddddbaaaaaaaaabdddddb", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "aeaaaeaaaaaaaaaaaeaaaea", "bdddddbccbbbbbccbdddddb")
                    .aisle("abdddbaaacbbbcaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "ajjjjjaaaaaaaaaaajjjjja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajgggjaaaaaaaaaaajgggja", "ajjjjjaaaaaaaaaaajjjjja", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaacbbbcaaabdddba")
                    .aisle("aabbbaaaaacbcaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaacbcaaaaabbbaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aacbcaaaaabcbaaaaacbcaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aacbcaaaaabcbaaaaacbcaa")
                    .aisle("aabbbaaabbcbcbbaaabbbca", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaabbcbcbbaaabbbaa")
                    .aisle("abdddbbbccbbbccbbbdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaabaaabaaabdddba", "aagggdeeedgggdeeedgggaa", "aagggdfffdgggdfffdgggaa", "aagggdeeedgggdeeedgggaa", "abdddbaaabaaabaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbbbccbbbccbbbdddba")
                    .aisle("abdddbccbbaaabbccbdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaaaaaaaaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaabaaabaaabdddba", "aaghadfffdgggdfffdahgaa", "aagihdaaaihihiaaadhigaa", "aaghgdfffdgggdfffdghgaa", "abdddbaaabaaabaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbaaaaaaaaaaabdddba", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "aafafaaaaaaaaaaaaafafaa", "abdddbccbbaaabbccbdddba")
                    .aisle("abdddbbbaaaaaaabbbdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaabaaabaaabdddba", "aagggdeeedgggdeeedgggaa", "aagggdfffdgggdfffdgggaa", "aagggdeeedgggdeeedgggaa", "abdddbaaabaaabaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbaaaaaaaaaaabdddba", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "aaefeaaaaaaaaaaaaaefeaa", "abdddbbbaaaaaaabbbdddba")
                    .aisle("aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aaaaabaaabaaabaaabaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaa", "aabbbaaaaaaaaaaaaabbbaa")
                    .where("a", Predicates.any())
                    .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:secure_maceration_casing")))
                    .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                    .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2)))
                    .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:reaction_safe_mixing_casing"))))
                    .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                    .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:awakened_draconium_coil"))))
                    .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:industrial_steam_casing"))))
                    .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                    .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing"))))
                    .where("i", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:steel_gearbox"))))
                    .where("j", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:nonconducting_casing"))))
                    .where("k", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:iron_bars"))))
                    .where('l', Predicates.controller(Predicates.blocks(definition.get())))
                    .build()
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.antimatter1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.antimatter2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.antimatter3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.antimatter4").withStyle(style -> style.withColor(0x90EE90)))
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/secure_maceration_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createSASRender))
            )
            .register();

    public static void init() {}
}
