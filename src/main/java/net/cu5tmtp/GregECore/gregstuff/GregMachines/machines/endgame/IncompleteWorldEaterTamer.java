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
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.UpdateListener;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IncompleteWorldEaterTamer extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(
            IncompleteWorldEaterTamer.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    private static final String CASING_TIER = "gregecore:world_eater_casing_tier";
    private static final String CASING_COLORS = "gregecore:world_eater_casing_colors";
    public static final EnumProperty<DyeColor> CASING_COLOR = EnumProperty.create("casing_color", DyeColor.class,
            DyeColor.BLACK, DyeColor.PURPLE, DyeColor.GREEN, DyeColor.ORANGE);
    private int casingTier;
    @DescSynced
    @UpdateListener(methodName = "onCasingAppearanceChanged")
    private BlockState casingAppearance = GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState();

    // Aisles run front to back, strings bottom to top; S is the controller, I the optional intention hatch.
    public static final String[][] SHAPE = {
            {"bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb", "bbbbbbacabbbbbb", "bbbbbbadabbbbbb", "bbbbbbedebbbbbb", "bbaaaeedeeaaabb", "bbccdddSdddccbb", "bbaaaeedeeaaabb", "bbbbbbedebbbbbb", "bbbbbbadabbbbbb", "bbbbbbacabbbbbb", "bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb"},
            {"bbbbbbbbbbbbbbb", "bbbbbbagabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "babbbbhdhbbbbab", "bgbbbbdidbbbbgb", "babbbbhdhbbbbab", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbagabbbbbb", "bbbbbbbbbbbbbbb"},
            {"bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "cbbbbbhihbbbbbc", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb"},
            {"bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "cbbbbbhihbbbbbc", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb"},
            {"bbbbbbadabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "dbbbbbhihbbbbbd", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbadabbbbbb"},
            {"bbbbbbadabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbjjjjjbbbbb", "ebbbbjbbbjbbbbe", "dbbbbjbbbjbbbbd", "ebbbbjbbbjbbbbe", "bbbbbjjjjjbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbedebbbbbb"},
            {"bbaaaaadaaaaabb", "babbbbhdhbbbbab", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "ebbbbjbbbjbbbbe", "ehhhhbbbbbhhhhe", "ddhhhbbbbbhhhdd", "ehhhhbbbbbhhhhe", "ebbbbjbbbjbbbbe", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "babbbbhdhbbbbab", "bbaaaeedeeaaabb"},
            {"bbccdddfdddccbb", "bgbbbbdidbbbbgb", "cbbbbbhihbbbbbc", "cbbbbbhihbbbbbc", "dbbbbbhihbbbbbd", "dbbbbjbbbjbbbbd", "ddhhhbbbbbhhhdd", "fiiiibbbbbiiiif", "ddhhhbbbbbhhhdd", "dbbbbjbbbjbbbbd", "dbbbbbhihbbbbbd", "cbbbbbhihbbbbbc", "cbbbbbhihbbbbbc", "bgbbbbdidbbbbgb", "bbccdddfdddccbb"},
            {"bbaaaaadaaaaabb", "babbbbhdhbbbbab", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "ebbbbjbbbjbbbbe", "ehhhhbbbbbhhhhe", "ddhhhbbbbbhhhdd", "ehhhhbbbbbhhhhe", "ebbbbjbbbjbbbbe", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "abbbbbhhhbbbbba", "babbbbhdhbbbbab", "bbaaaeedeeaaabb"},
            {"bbbbbbadabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbjjjjjbbbbb", "ebbbbjbbbjbbbbe", "dbbbbjbbbjbbbbd", "ebbbbjbbbjbbbbe", "bbbbbjjjjjbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbedebbbbbb"},
            {"bbbbbbadabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "dbbbbbhihbbbbbd", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbadabbbbbb"},
            {"bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "cbbbbbhihbbbbbc", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb"},
            {"bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "abbbbbhhhbbbbba", "cbbbbbhihbbbbbc", "abbbbbhhhbbbbba", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb"},
            {"bbbbbbbbbbbbbbb", "bbbbbbagabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "babbbbhdhbbbbab", "bgbbbbdidbbbbgb", "babbbbhdhbbbbab", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbagabbbbbb", "bbbbbbbbbbbbbbb"},
            {"bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbacabbbbbb", "bbbbbbacabbbbbb", "bbbbbbadabbbbbb", "bbbbbbedebbbbbb", "bbaaaeedeeaaabb", "bbccdddIdddccbb", "bbaaaeedeeaaabb", "bbbbbbedebbbbbb", "bbbbbbadabbbbbb", "bbbbbbacabbbbbb", "bbbbbbacabbbbbb", "bbbbbbbbbbbbbbb", "bbbbbbbbbbbbbbb"}
    };

    public static final MachineDefinition INCOMPLETE_WORLD_EATER_TAMER = REGISTRATE
            .multiblock("incomplete_world_eater_tamer", IncompleteWorldEaterTamer::new)
            .langValue("Incomplete World Eater tamer")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GreggyBlocks.WORLD_EATER_TAMER_CASING)
            .partAppearance((controller, part, side) -> ((IncompleteWorldEaterTamer) controller).casingAppearance)
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
                var frame = casingPredicate()
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
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('a', frame)
                    .where('b', Predicates.any())
                    .where('c', block("gtceu:inert_machine_casing"))
                    .where('d', block("gtceu:large_scale_assembler_casing"))
                    .where('e', block("gtceu:high_temperature_smelting_casing"))
                    .where('f', Predicates.blocks(GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get()))
                    .where('g', Predicates.blocks(GreggyBlocks.PTFE_ENGINE_INTAKE.get()))
                    .where('h', block("gtceu:laminated_glass"))
                    .where('i', block("gtceu:superconducting_coil"))
                    .where('j', block("gtceu:shock_proof_cutting_casing"))
                    .where('I', Predicates.blocks(GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get())
                            .or(Predicates.abilities(IntentionHatchPartMachine.INTENTION)))
                    .build();
            })
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .modelProperty(CASING_COLOR, DyeColor.BLACK)
            .model((ctx, prov, builder) -> {
                var overlays = WorkableOverlays.get(GTCEu.id("block/multiblock/research_station"), prov.getExistingFileHelper());
                builder.forAllStates(state -> {
                    var color = state.getValue(GTMachineModelProperties.IS_FORMED) ? state.getValue(CASING_COLOR) : DyeColor.BLACK;
                    var casing = GregECore.id("block/casings/world_eater/" + color.getName());
                    return GTMachineModels.addWorkableOverlays(overlays,
                            state.getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS), prov.models().nested()
                                    .parent(prov.models().getExistingFile(GregECore.id("block/world_eater_controller_template")))
                                    .texture("all", casing)
                                    .texture("glow", casing.withSuffix("_emissive")));
                });
                builder.addTextureOverride("all", GregECore.id("block/casings/world_eater/black"));
                builder.addDynamicRenderer(WorldEaterPartRender::new);
            })
            .tooltips(Component.literal("Collects infrastructure information without power."),
                    Component.literal("Scans the surrounding 9×9 chunks over 120 seconds."),
                    Component.literal("A complete survey of at least 500 machines awakens the tamer."),
                    Component.literal("Frame: World Eater Tamer Casing."),
                    Component.literal("ACTIVE or PREPARED: 1 black casing + 16 matching terracotta → 1 colored casing in 5 seconds."),
                    Component.literal("ACTIVE or PREPARED, tier 2: 1 signed acknowledgement book → 1 Bravery Hatch in 60 seconds, without power."),
                    Component.literal("Tier 2 + intention: Desarium tests ACTIVE → PREPARED; Implementium produces essence in PREPARED."),
                    Component.literal("Deliver the displayed 1–5 items to any input bus for immediate success; no power or fluids."),
                    Component.literal("Deadline: 30 seconds per voltage tier (LV–UEV); quantity does not change it."),
                    Component.literal("Only real timeout has consequences: Safety sacrifices itself; Bravery sacrifices one nearby machine. No collateral damage."),
                    Component.literal("Test failures and interrupted runs are harmless. Preparation is permanent."),
                    Component.literal("Accepts one optional parallel hatch; only casing conversions run in parallel."),
                    Component.literal("Optional Safety or Bravery Hatch: rear face center, opposite the controller."))
            .tooltips(Component.literal("Optional ME Demand Hatch: one at any ordinary frame I/O position, for real runs only."),
                    Component.literal("One startup autocrafting attempt for missing demand; existing matching jobs suppress it. No retries."),
                    Component.literal("Fire-and-forget jobs finish into ME storage, even after the run ends. Deadlines and timeout risks are unchanged."))
            .register();

    public IncompleteWorldEaterTamer(IMachineBlockEntity holder, Object... args) { super(holder, args); }

    @Override
    public ManagedFieldHolder getFieldHolder() { return FIELDS; }

    private void onCasingAppearanceChanged(BlockState newValue, BlockState oldValue) {
        if (isRemote()) {
            scheduleRenderUpdate();
            for (var part : getParts()) part.self().scheduleRenderUpdate();
        }
    }

    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         BlockState sourceState, BlockPos sourcePos) {
        var cover = getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        return cover != null ? cover : casingAppearance;
    }

    private static TraceabilityPredicate block(String id) {
        return Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(id)));
    }

    private static TraceabilityPredicate casingPredicate() {
        return new TraceabilityPredicate(state -> {
            if (state.getBlockState().getBlock() != GreggyBlocks.WORLD_EATER_TAMER_CASING.get()) return false;
            // Colored casings are no longer accepted, so tier 2 stays dormant.
            state.getMatchContext().set(CASING_TIER, 1);
            return true;
        }, () -> new BlockInfo[]{new BlockInfo(GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState())});
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        var context = getMultiblockState().getMatchContext();
        casingTier = context.getInt(CASING_TIER);
        casingAppearance = GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState();
        if (casingTier == 2) {
            var casings = context.<Map<BlockPos, Block>>getOrCreate(CASING_COLORS, HashMap::new).values();
            long most = 0;
            for (var color : List.of(GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.get(),
                    GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.get(), GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.get())) {
                long count = casings.stream().filter(block -> block == color).count();
                if (count > most) {
                    most = count;
                    casingAppearance = color.defaultBlockState();
                }
            }
        }
        var color = casingAppearance.is(GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.get()) ? DyeColor.PURPLE
                : casingAppearance.is(GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.get()) ? DyeColor.GREEN
                : casingAppearance.is(GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.get()) ? DyeColor.ORANGE : DyeColor.BLACK;
        setRenderState(getRenderState().setValue(CASING_COLOR, color));
        // Formation can precede managed storage's initial sync baseline.
        getHolder().getRootStorage().markAllDirty();
        for (var part : getParts()) part.self().getHolder().getRootStorage().markAllDirty();
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.refresh(this);
    }

    @Override
    public void onStructureInvalid() {
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.clear(this);
        casingTier = 0;
        casingAppearance = GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState();
        setRenderState(getRenderState().setValue(CASING_COLOR, DyeColor.BLACK));
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
        if (getCasingTier() == 2) text.add(Component.literal("Casing tier: 2"));
        var logic = (WorldEaterTamerRecipeLogic) getRecipeLogic();
        text.add(logic.getCustomProgressLine());
        if (logic.getProgressionState() == WorldEaterTamerRecipeLogic.State.DORMANT)
            text.add(logic.getCurrentChunkLine());
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

    public static void init() {}
}
