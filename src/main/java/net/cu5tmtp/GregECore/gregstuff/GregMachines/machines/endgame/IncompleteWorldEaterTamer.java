package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.models.GTMachineModels;
import com.gregtechceu.gtceu.client.model.machine.overlays.WorkableOverlays;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.MEDemandHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine.Intention;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender.WorldEaterPartRender;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.recipe.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.WorldEaterLang;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IncompleteWorldEaterTamer extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    private static final String CASING_TIER = "gregecore:world_eater_casing_tier";
    private int casingTier;

    // Aisles run front to back, strings bottom to top. K controller, C frame casing (parts allowed),
    // T tier casing (any World Eater color), I intention slot, E/P engine intakes, space = any block.
    public static final String[][] SHAPE = {
            {"               ", "               ", "      CNC      ", "      CNC      ", "      CAC      ", "      HAH      ", "  CCCHHAHHCCC  ", "  NNAAAKAAANN  ", "  CCCHHAHHCCC  ", "      HAH      ", "      CAC      ", "      CNC      ", "      CNC      ", "               ", "               "},
            {"               ", "      CPC      ", "               ", "               ", "               ", "               ", " C    GAG    C ", " P    ASA    P ", " C    GAG    C ", "               ", "               ", "               ", "               ", "      CPC      ", "               "},
            {"      CNC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "N     GSG     N", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CNC      "},
            {"      CNC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "N     GSG     N", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CNC      "},
            {"      CAC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "A     GSG     A", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CAC      "},
            {"      CAC      ", "               ", "               ", "               ", "               ", "     TTTTT     ", "H    T   T    H", "A    T   T    A", "H    T   T    H", "     TTTTT     ", "               ", "               ", "               ", "               ", "      HAH      "},
            {"  CCCCCACCCCC  ", " C    GAG    C ", "C     GGG     C", "C     GGG     C", "C     GGG     C", "H    T   T    H", "HGGGG     GGGGH", "AAGGG     GGGAA", "HGGGG     GGGGH", "H    T   T    H", "C     GGG     C", "C     GGG     C", "C     GGG     C", " C    GAG    C ", "  CCCHHAHHCCC  "},
            {"  NNAAAEAAANN  ", " P    ASA    P ", "N     GSG     N", "N     GSG     N", "A     GSG     A", "A    T   T    A", "AAGGG     GGGAA", "ESSSS     SSSSE", "AAGGG     GGGAA", "A    T   T    A", "A     GSG     A", "N     GSG     N", "N     GSG     N", " P    ASA    P ", "  NNAAAEAAANN  "},
            {"  CCCCCACCCCC  ", " C    GAG    C ", "C     GGG     C", "C     GGG     C", "C     GGG     C", "H    T   T    H", "HGGGG     GGGGH", "AAGGG     GGGAA", "HGGGG     GGGGH", "H    T   T    H", "C     GGG     C", "C     GGG     C", "C     GGG     C", " C    GAG    C ", "  CCCHHAHHCCC  "},
            {"      CAC      ", "               ", "               ", "               ", "               ", "     TTTTT     ", "H    T   T    H", "A    T   T    A", "H    T   T    H", "     TTTTT     ", "               ", "               ", "               ", "               ", "      HAH      "},
            {"      CAC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "A     GSG     A", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CAC      "},
            {"      CNC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "N     GSG     N", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CNC      "},
            {"      CNC      ", "               ", "               ", "               ", "               ", "               ", "C     GGG     C", "N     GSG     N", "C     GGG     C", "               ", "               ", "               ", "               ", "               ", "      CNC      "},
            {"               ", "      CPC      ", "               ", "               ", "               ", "               ", " C    GAG    C ", " P    ASA    P ", " C    GAG    C ", "               ", "               ", "               ", "               ", "      CPC      ", "               "},
            {"               ", "               ", "      CNC      ", "      CNC      ", "      CAC      ", "      HAH      ", "  CCCHHAHHCCC  ", "  NNAAAIAAANN  ", "  CCCHHAHHCCC  ", "      HAH      ", "      CAC      ", "      CNC      ", "      CNC      ", "               ", "               "}
    };

    public static final MachineDefinition INCOMPLETE_WORLD_EATER_TAMER = REGISTRATE
            .multiblock("incomplete_world_eater_tamer", IncompleteWorldEaterTamer::new)
            .langValue("Incomplete World Eater tamer")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GreggyBlocks.WORLD_EATER_TAMER_CASING)
            .recipeTypes(GregERecipeTypes.WORLD_EATER_TAMER)
            .recipeModifiers((machine, recipe) -> {
                if (machine instanceof IncompleteWorldEaterTamer tamer
                            && ((WorldEaterTamerRecipeLogic) tamer.getRecipeLogic()).getProgressionState() != WorldEaterTamerRecipeLogic.State.DORMANT
                            && recipe.recipeType == GregERecipeTypes.WORLD_EATER_TAMER
                            && WorldEaterTamerRecipeLogic.isCasingRecipe(recipe)) {
                    var parallel = GTRecipeModifiers.hatchParallel(machine, recipe);
                    return original -> {
                        var modified = parallel.apply(original);
                        if (modified != null) {
                            // The standard modifier synthesizes a zero-EU tick entry even
                            // for recipes without energy. Preserve genuinely power-free IO.
                            modified.tickInputs.remove(EURecipeCapability.CAP);
                            modified.tickOutputs.remove(EURecipeCapability.CAP);
                        }
                        return modified;
                    };
                }
                return ModifierFunction.IDENTITY;
            })
            .pattern(definition -> {
                var frame = Predicates.blocks(GreggyBlocks.WORLD_EATER_TAMER_CASING.get())
                        .or(Predicates.abilities(PartAbility.PARALLEL_HATCH).setMaxGlobalLimited(1).setPreviewCount(1))
                        .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(5))
                        .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(5))
                        .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(5))
                        .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(5));
                if (GTCEu.Mods.isAE2Loaded()) frame = frame.or(Predicates.abilities(MEDemandHatchPartMachine.DEMAND)
                        .setMaxGlobalLimited(1).setPreviewCount(1));
                var pattern = FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK);
                for (var aisle : SHAPE) pattern.aisle(aisle);
                return pattern
                    .where('K', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('C', frame)
                    .where('T', tierCasingPredicate())
                    .where(' ', Predicates.any())
                    .where('N', block("gtceu:inert_machine_casing"))
                    .where('A', block("gtceu:large_scale_assembler_casing"))
                    .where('H', block("gtceu:high_temperature_smelting_casing"))
                    .where('E', Predicates.blocks(GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get()))
                    .where('P', Predicates.blocks(GreggyBlocks.PTFE_ENGINE_INTAKE.get()))
                    .where('G', block("gtceu:laminated_glass"))
                    .where('S', block("gtceu:superconducting_coil"))
                    .where('I', Predicates.blocks(GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get())
                            .or(Predicates.abilities(IntentionHatchPartMachine.INTENTION)))
                    .build();
            })
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model((ctx, prov, builder) -> {
                var overlays = WorkableOverlays.get(GTCEu.id("block/multiblock/research_station"), prov.getExistingFileHelper());
                builder.forAllStates(state -> {
                    // Tier-ring colors never change the controller's black casing.
                    var casing = GregECore.id("block/casings/world_eater/black");
                    return GTMachineModels.addWorkableOverlays(overlays,
                            state.getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS), prov.models().nested()
                                    .parent(prov.models().getExistingFile(GregECore.id("block/world_eater_controller_template")))
                                    .texture("all", casing)
                                    .texture("glow", casing.withSuffix("_emissive")));
                });
                builder.addTextureOverride("all", GregECore.id("block/casings/world_eater/black"));
                builder.addDynamicRenderer(WorldEaterPartRender::new);
            })
            .tooltips(REGISTRATE.addRawLang("gregecore.tooltip.world_eater_tamer.0",
                            "Taming a World Eater is not an easy or simple task, but a steady mind and dedication bring results").withStyle(ChatFormatting.BLUE, ChatFormatting.ITALIC),
                    REGISTRATE.addRawLang("gregecore.tooltip.world_eater_tamer.1",
                            "To start, build it in the center of your factory with enough machines around it and follow the instructions from the controller"))
            .register();

    public IncompleteWorldEaterTamer(IMachineBlockEntity holder, Object... args) { super(holder, args); }

    private static TraceabilityPredicate block(String id) {
        return Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(id)));
    }

    /** Inner ring casing: black gives tier 1, a ring of only colored casings gives tier 2. */
    private static TraceabilityPredicate tierCasingPredicate() {
        return new TraceabilityPredicate(state -> {
            var block = state.getBlockState().getBlock();
            int tier;
            if (block == GreggyBlocks.WORLD_EATER_TAMER_CASING.get()) tier = 1;
            else if (block == GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.get()
                    || block == GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.get()
                    || block == GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.get()) tier = 2;
            else return false;
            var context = state.getMatchContext();
            context.set(CASING_TIER, Math.min(context.getOrDefault(CASING_TIER, 2), tier));
            return true;
        }, () -> new BlockInfo[]{new BlockInfo(GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState()),
                new BlockInfo(GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.getDefaultState()),
                new BlockInfo(GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.getDefaultState()),
                new BlockInfo(GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.getDefaultState())});
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var context = getMultiblockState().getMatchContext();
        casingTier = context.getInt(CASING_TIER);
        // Formation can precede managed storage's initial sync baseline.
        getHolder().getRootStorage().markAllDirty();
        for (var part : getParts()) part.self().getHolder().getRootStorage().markAllDirty();
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.refresh(this);
    }

    @Override
    public void onStructureInvalid() {
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.clear(this);
        casingTier = 0;
        super.onStructureInvalid();
    }

    @Override
    public void onUnload() {
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.clear(this);
        super.onUnload();
    }

    public int getCasingTier() {
        return isFormed() && !getMultiblockState().hasError() && (casingTier == 1 || casingTier == 2)
                && getMultiblockState().getMatchContext().getInt(CASING_TIER) == casingTier ? casingTier : 0;
    }

    public Vec3 getStructureCenter() {
        return Vec3.atCenterOf(getPos().relative(getFrontFacing().getOpposite(), 7));
    }

    public Intention getIntention() {
        if (getCasingTier() == 0) return Intention.NONE;
        for (var part : getParts()) {
            if (part instanceof IntentionHatchPartMachine hatch) return hatch.getIntention();
        }
        return Intention.NONE;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) { return new WorldEaterTamerRecipeLogic(this); }

    @Override
    public void setVoidingMode(VoidingMode mode) { super.setVoidingMode(VoidingMode.VOID_NONE); }

    @Override
    public VoidingMode getVoidingMode() { return VoidingMode.VOID_NONE; }

    @Override
    public void addDisplayText(List<Component> text) {
        IDisplayUIMachine.super.addDisplayText(text);
        if (isFormed()) text.add(((WorldEaterTamerRecipeLogic) getRecipeLogic()).getCustomProgressLine());
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 190, 125)
                .addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                        .addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText)
                                .textSupplier(getLevel().isClientSide ? null : this::addDisplayText)
                                .setMaxWidthLimit(174).clickHandler(this::handleDisplayClick)))
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    public static void init() { WorldEaterLang.init(); }
}
