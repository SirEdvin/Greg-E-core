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
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine.Intention;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.thingsToRender.WorldEaterPartRender;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IncompleteWorldEaterTamer extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(
            IncompleteWorldEaterTamer.class, WorkableMultiblockMachine.MANAGED_FIELD_HOLDER);
    private static final String CASING_TIER = "gregecore:world_eater_casing_tier";
    private static final String CASING_COLORS = "gregecore:world_eater_casing_colors";
    private int casingTier;
    @DescSynced
    @UpdateListener(methodName = "onCasingAppearanceChanged")
    private BlockState casingAppearance = GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState();

    public static final MachineDefinition INCOMPLETE_WORLD_EATER_TAMER = REGISTRATE
            .multiblock("incomplete_world_eater_tamer", IncompleteWorldEaterTamer::new)
            .langValue("Incomplete World Eater tamer")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GreggyBlocks.WORLD_EATER_TAMER_CASING)
            .partAppearance((controller, part, side) -> ((IncompleteWorldEaterTamer) controller).casingAppearance)
            .recipeTypes(GregERecipeTypes.WORLD_EATER_TAMER)
            .recipeModifiers((machine, recipe) -> {
                if (machine instanceof IncompleteWorldEaterTamer tamer
                            && ((WorldEaterTamerRecipeLogic) tamer.getRecipeLogic()).getProgressionState() == WorldEaterTamerRecipeLogic.State.ACTIVE
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
                return FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK)
                    .aisle("CCSCC", "C###C", "C###C", "C###C", "CCCCC")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("CCICC", "C###C", "C###C", "C###C", "CCCCC")
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('#', Predicates.any())
                    .where('C', frame)
                    .where('I', frame.or(Predicates.abilities(IntentionHatchPartMachine.INTENTION)))
                    .build();
            })
            .modelProperty(GTMachineModelProperties.RECIPE_LOGIC_STATUS, RecipeLogic.Status.IDLE)
            .model((ctx, prov, builder) -> {
                var casing = GregECore.id("block/casings/world_eater/black");
                var overlays = WorkableOverlays.get(GTCEu.id("block/multiblock/research_station"), prov.getExistingFileHelper());
                builder.forAllStates(state -> GTMachineModels.addWorkableOverlays(overlays,
                        state.getValue(GTMachineModelProperties.RECIPE_LOGIC_STATUS), prov.models().nested()
                                .parent(prov.models().getExistingFile(GregECore.id("block/world_eater_controller_template")))
                                .texture("all", casing)
                                .texture("glow", casing.withSuffix("_emissive"))));
                builder.addTextureOverride("all", casing);
                builder.addDynamicRenderer(WorldEaterPartRender::new);
            })
            .tooltips(Component.literal("Collects infrastructure information without power."),
                    Component.literal("Scans the surrounding 9×9 chunks over 120 seconds."),
                    Component.literal("A complete survey of at least 500 machines awakens the tamer."),
                    Component.literal("Frame: any mix of black, purple, green, or orange World Eater Tamer Casings."),
                    Component.literal("Any black frame casing: tier 1; all colored: tier 2. Reversible; progression is retained."),
                    Component.literal("ACTIVE: 1 black casing + 16 matching terracotta → 1 colored casing in 5 seconds."),
                    Component.literal("ACTIVE, tier 2: 1 signed acknowledgement book → 1 Bravery Hatch in 60 seconds, without power."),
                    Component.literal("Accepts one optional parallel hatch; surveys always run singly."),
                    Component.literal("Optional Safety or Bravery Hatch: lower rear edge center, opposite the controller."))
            .register();

    public IncompleteWorldEaterTamer(IMachineBlockEntity holder, Object... args) { super(holder, args); }

    @Override
    public ManagedFieldHolder getFieldHolder() { return FIELDS; }

    private void onCasingAppearanceChanged(BlockState newValue, BlockState oldValue) {
        if (isRemote()) for (var part : getParts()) part.self().scheduleRenderUpdate();
    }

    private static TraceabilityPredicate casingPredicate() {
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
            // Key by position so repeated predicate checks cannot inflate the vote.
            context.<Map<BlockPos, Block>>getOrCreate(CASING_COLORS, HashMap::new).put(state.getPos().immutable(), block);
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
        // Formation can precede managed storage's initial sync baseline.
        getHolder().getRootStorage().markAllDirty();
        for (var part : getParts()) part.self().getHolder().getRootStorage().markAllDirty();
    }

    @Override
    public void onStructureInvalid() {
        casingTier = 0;
        casingAppearance = GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState();
        super.onStructureInvalid();
    }

    public int getCasingTier() {
        return isFormed() && !getMultiblockState().hasError() && (casingTier == 1 || casingTier == 2)
                && getMultiblockState().getMatchContext().getInt(CASING_TIER) == casingTier ? casingTier : 0;
    }

    public Vec3 getStructureCenter() {
        return Vec3.atCenterOf(getPos().relative(getFrontFacing().getOpposite(), 2).above(2));
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
