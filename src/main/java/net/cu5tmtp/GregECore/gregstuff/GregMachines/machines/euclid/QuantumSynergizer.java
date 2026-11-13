package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.euclid;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;


public class QuantumSynergizer extends WorkableElectricMultiblockMachine {

    public QuantumSynergizer(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            QuantumSynergizer.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @DescSynced
    @Persisted
    public int comboCount = 0;

    @Persisted
    private ArrayList<String> recipeHistory = new ArrayList<>();

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        assert recipe != null;
        String currentRecipeId = recipe.id != null ? recipe.id.toString() : "";

        if (this.comboCount >= 3) {
            this.comboCount = 0;
            this.recipeHistory.clear();
        } else {
            if (!this.recipeHistory.contains(currentRecipeId)) {
                this.comboCount++;
                this.recipeHistory.add(currentRecipeId);
            } else {
                this.comboCount = 1;
                this.recipeHistory.clear();
                this.recipeHistory.add(currentRecipeId);
            }
        }

        return super.beforeWorking(recipe);
    }

    public static MachineDefinition QUANTUMSYNERGIZER = REGISTRATE
            .multiblock("quantumsynergizer", QuantumSynergizer::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GCYMBlocks.CASING_NONCONDUCTING)
            .recipeTypes(GregERecipeTypes.ROBOTICFABRICATION)
            .recipeModifier(GregEModifiers::quantumSynergizerBoost)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("abbbbbbbbbbbbbbbbbbbbbbbbba", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aadeefeedadeefeedadeefeedaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadeefeedadeefeedadeefeedaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaadddddaaadddddaaadddddaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa", "aaaaaaaaaaaaegeaaaaaaaaaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaagahagaaagahagaaagahagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagahagaaagahagaaagahagaaa", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaadeeedaaadeeedaaadeeedaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaaeeeaaaaaeeeaaaaaeeeaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aafeeeeefafeeeeefafeeeeefaa", "aaaghhhgaaaghhhgaaaghhhgaaa", "aaagahagaaagahagaaagahagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagahagaaagahagaaagahagaaa", "aaaghhhgaaaghhhgaaaghhhgaaa", "aafeeaeefafeeaeefafeeaeefaa", "aaadeaedaaadeaedaaadeaedaaa", "aaaagagaaaaagagaaaaagagaaaa", "aaaagagaaaaagagaaaaagagaaaa", "aaaagagaaaaagagaaaaagagaaaa", "aaaaeaeaaaaaeaeaaaaaeaeaaaa", "aaaagagegggeaaaegggegagaaaa", "aaaagaaaaaaaaiaaaaaaaagaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaaaaaaaaaaegeaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaagahagaaagahagaaagahagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagaaagaaagaaagaaagaaagaaa", "aaagahagaaagahagaaagahagaaa", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaadeeedaaadeeedaaadeeedaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaagggaaaaagggaaaaagggaaaa", "aaaaeeeaaaaaeeeaaaaaeeeaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaagggegggeaaaegggegggaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaaegggeaaaegggeaaaegggeaaa", "aaeeeeeeeaeeeeeeeaeeeeeeeaa", "aaadddddaaadddddaaadddddaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa", "aaaaaaaaaaaaegeaaaaaaaaaaaa", "aaaaaaaaaaaaeeeaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .aisle("bcccccccccccccccccccccccccb", "aadeefeedadeefeedadeefeedaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadaaaaadadaaaaadadaaaaadaa", "aadeefeedadeefeedadeefeedaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .aisle("abbbbbbbbbbbbzbbbbbbbbbbbba", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaa")
                        .where("a", Predicates.any())
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:nonconducting_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1)))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:stress_proof_casing"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:blue_steel_frame"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:large_scale_assembler_casing"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gregecore:assembly_engine_intake"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:laminated_glass"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:superconducting_coil"))))
                        .where("i", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:fusion_coil"))))
                        .where("z", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/nonconducting_casing"),
                    GTCEu.id("block/multiblock/distillation_tower"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createRoboticFabricatorRender))
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.quantumsynergizer1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.quantumsynergizer2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.quantumsynergizer3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        textList.add(Component.translatable("info.gregecore.quantumsynergizer4" + comboCount).withStyle(ChatFormatting.AQUA));
    }

    public int getComboCount() {
        return comboCount;
    }

    public static void init() {
    }
}