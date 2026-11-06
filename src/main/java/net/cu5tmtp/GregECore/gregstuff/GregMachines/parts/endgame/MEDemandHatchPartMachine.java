package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.gregtechceu.gtceu.common.data.machines.GTAEMachines;
import com.gregtechceu.gtceu.integration.ae2.machine.MEStockingBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemList;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.mojang.logging.LogUtils;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.phys.BlockHitResult;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

/** Loaded only behind the AE2 availability guard. */
public class MEDemandHatchPartMachine extends MEStockingBusPartMachine {
    public static final PartAbility DEMAND = new PartAbility("gregecore_me_demand");
    public static final MachineDefinition HATCH = REGISTRATE.machine("me_demand_hatch", MEDemandHatchPartMachine::new)
            .langValue("ME Demand Hatch")
            .tier(GTValues.LuV)
            .rotationState(RotationState.NON_Y_AXIS)
            .abilities(DEMAND)
            .modelProperty(GTMachineModelProperties.IS_FORMED, false)
            .colorOverlayTieredHullModel(GregECore.id("block/overlay/me_demand"))
            .tooltips(Component.literal("Optional World Eater tamer input: one per frame, at any ordinary I/O position."),
                    Component.literal("Craft shapelessly: 1 ME Stocking Input Bus + 4 Tamed world eater essense ingots."),
                    Component.literal("Mirrors only the current paid real-run demand; preparation tests are not automated."),
                    Component.literal("Uses ME stock; one startup autocrafting attempt for missing items, unless matching production is already scheduled."),
                    Component.literal("No retries: offline networks, missing patterns or ingredients, and busy CPUs can leave the demand unmet."),
                    Component.literal("Fire-and-forget: submitted jobs are never tracked or cancelled. Late products stay in ME storage."),
                    Component.literal("Manual delivery still works. Intention requirements, deadlines and timeout consequences are unchanged."))
            .register();

    public MEDemandHatchPartMachine(IMachineBlockEntity holder, Object... args) { super(holder, args); }

    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(
            MEDemandHatchPartMachine.class, MEStockingBusPartMachine.MANAGED_FIELD_HOLDER);
    @DescSynced
    private ItemStack request = ItemStack.EMPTY;
    private PendingCraft pendingCraft;

    private record PendingCraft(IncompleteWorldEaterTamer controller, GTRecipe run, IGrid grid,
                                AEItemKey key, Future<ICraftingPlan> calculation) {}

    @Override
    public ManagedFieldHolder getFieldHolder() { return FIELDS; }

    public ItemStack getRequest() { return request.copy(); }

    private IncompleteWorldEaterTamer owner() {
        if (getControllers().size() != 1) return null;
        var controller = getControllers().iterator().next();
        return controller instanceof IncompleteWorldEaterTamer tamer && tamer.getCasingTier() != 0
                && tamer.getParts().contains(this) ? tamer : null;
    }

    private static MEDemandHatchPartMachine hatch(IncompleteWorldEaterTamer controller) {
        for (var part : controller.getParts()) if (part instanceof MEDemandHatchPartMachine hatch) return hatch;
        return null;
    }

    public static void refresh(IncompleteWorldEaterTamer controller) {
        var hatch = hatch(controller);
        if (hatch != null && !hatch.isRemote()) hatch.refreshRequest();
    }

    public static void clear(IncompleteWorldEaterTamer controller) {
        var hatch = hatch(controller);
        if (hatch != null && !hatch.isRemote()) hatch.clearRequest();
    }

    private void clearCalculation() {
        if (pendingCraft != null) {
            pendingCraft.calculation().cancel(false);
            pendingCraft = null;
        }
    }

    private void clearRequest() {
        clearCalculation();
        request = ItemStack.EMPTY;
        aeItemHandler.clearInventory(0);
    }

    private void refreshRequest() {
        var controller = owner();
        var wanted = controller == null ? ItemStack.EMPTY
                : ((WorldEaterTamerRecipeLogic) controller.getRecipeLogic()).getRealDemand();
        if (wanted.isEmpty()) { clearRequest(); return; }
        if (!ItemStack.matches(request, wanted)) {
            clearCalculation();
            request = wanted;
        }
        aeItemHandler.clearInventory(1);
        aeItemHandler.getInventory()[0].setConfig(new GenericStack(AEItemKey.of(request), request.getCount()));
        if (isWorkingEnabled() && updateMEStatus()) syncME();
        else aeItemHandler.getInventory()[0].setStock(null);
    }

    /** Native distinct mode permits a request alongside configured buses; payment deduplicates grids below. */
    @Override
    public void setDistinct(boolean distinct) { super.setDistinct(true); }

    @Override
    public boolean isDistinct() { return true; }

    @Override
    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        return super.createCircuitItemHandler(IO.NONE);
    }

    @Override
    public void setAutoPull(boolean autoPull) {}

    @Override
    public boolean isAutoPull() { return false; }

    @Override
    public int getMinStackSize() { return 1; }

    @Override
    public void attachConfigurators(ConfiguratorPanel panel) {}

    @Override
    protected CompoundTag writeConfigToTag() { return new CompoundTag(); }

    @Override
    protected void readConfigFromTag(CompoundTag tag) {}

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand, Direction side, BlockHitResult hit) {
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public void onLoad() {
        if (!isRemote()) { clearRequest(); setDistinct(true); }
        super.onLoad();
        if (!isRemote()) getHolder().getRootStorage().markAllDirty();
    }

    @Override
    public void onUnload() {
        if (!isRemote()) clearRequest();
        super.onUnload();
    }

    @Override
    public void removedFromController(IMultiController controller) {
        if (!isRemote()) clearRequest();
        super.removedFromController(controller);
    }

    @Override
    public void onControllersUpdated(Set<BlockPos> positions, Set<BlockPos> old) {
        super.onControllersUpdated(positions, old);
        if (isRemote()) setRenderState(getRenderState().setValue(GTMachineModelProperties.IS_FORMED, !positions.isEmpty()));
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        if (pendingCraft != null && (!isOnline() || getMainNode().getGrid() != pendingCraft.grid())) clearCalculation();
    }

    @Override
    public void autoIO() { refreshRequest(); }

    /** Refresh native extraction views and use each grid once, including distinct ordinary buses. */
    public static List<IRecipeHandler<?>> demandInputs(IncompleteWorldEaterTamer controller, ItemStack demand) {
        refresh(controller);
        var result = new ArrayList<IRecipeHandler<?>>();
        var grids = new HashSet<IGrid>();
        var key = AEItemKey.of(demand);
        for (var handler : new LinkedHashSet<>(controller.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP))) {
            if (handler instanceof ExportOnlyAEItemList list && list.getMachine() instanceof MEStockingBusPartMachine bus) {
                var grid = bus.getMainNode().getGrid();
                if (grid == null || !bus.isWorkingEnabled() || !bus.updateMEStatus() || grids.contains(grid)) continue;
                boolean found = false;
                boolean usable = false;
                for (var slot : list.getInventory()) {
                    if (slot.getConfig() == null || !slot.getConfig().what().equals(key)) continue;
                    long available = found ? 0 : grid.getStorageService().getInventory()
                            .extract(key, Long.MAX_VALUE, Actionable.SIMULATE, bus.getActionSource());
                    slot.setStock(available >= bus.getMinStackSize() && available > 0 ? new GenericStack(key, available) : null);
                    usable |= slot.getStock() != null;
                    found = true;
                }
                if (!usable) continue;
                grids.add(grid);
            }
            result.add(handler);
        }
        return result;
    }

    private static boolean alreadyScheduled(IGrid grid, AEItemKey key) {
        var crafting = grid.getCraftingService();
        if (crafting.isRequesting(key)) return true;
        for (var cpu : crafting.getCpus()) {
            var status = cpu.getJobStatus();
            if (status != null && status.crafting().what().equals(key)) return true;
        }
        return false;
    }

    public static void begin(IncompleteWorldEaterTamer controller) {
        var hatch = hatch(controller);
        if (hatch == null || hatch.isRemote()) return;
        hatch.refreshRequest();
        var logic = (WorldEaterTamerRecipeLogic) controller.getRecipeLogic();
        var grid = hatch.getMainNode().getGrid();
        if (hatch.request.isEmpty() || !hatch.isWorkingEnabled() || !hatch.updateMEStatus() || grid == null) return;
        int missing = logic.getDemandRemainder();
        var key = AEItemKey.of(hatch.request);
        if (missing == 0 || alreadyScheduled(grid, key)) return;
        try {
            var calculation = grid.getCraftingService().beginCraftingCalculation(controller.getLevel(),
                    () -> hatch.actionSource, key, missing, CalculationStrategy.REPORT_MISSING_ITEMS);
            hatch.pendingCraft = new PendingCraft(controller, logic.getLastRecipe(), grid, key, calculation);
        } catch (RuntimeException failure) {
            LogUtils.getLogger().warn("ME demand calculation failed; this run will not retry", failure);
        }
    }

    public static void tick(IncompleteWorldEaterTamer controller) {
        var hatch = hatch(controller);
        if (hatch == null || hatch.pendingCraft == null) return;
        var pending = hatch.pendingCraft;
        var logic = (WorldEaterTamerRecipeLogic) controller.getRecipeLogic();
        if (hatch.owner() != pending.controller() || logic.getLastRecipe() != pending.run()
                || !pending.key().equals(AEItemKey.of(logic.getRealDemand())) || !hatch.isWorkingEnabled()
                || !hatch.updateMEStatus() || hatch.getMainNode().getGrid() != pending.grid()) {
            hatch.clearCalculation();
            return;
        }
        if (!pending.calculation().isDone()) return;
        hatch.pendingCraft = null;
        try {
            var plan = pending.calculation().get();
            if (plan == null || plan.simulation() || logic.getDemandRemainder() == 0
                    || alreadyScheduled(pending.grid(), pending.key())) return;
            pending.grid().getCraftingService().submitJob(plan, null, null, false, hatch.actionSource);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | RuntimeException failure) {
            LogUtils.getLogger().warn("ME demand submission failed; this run will not retry", failure);
        }
    }

    @Override
    public Widget createUIWidget() {
        Consumer<List<Component>> text = lines -> {
            lines.add(Component.literal("ME Demand Hatch"));
            lines.add(Component.literal(isOnline() ? "ME network: online" : "ME network: offline"));
            lines.add(request.isEmpty() ? Component.literal("No real-run demand")
                    : Component.literal("Demand: " + request.getCount() + " x ").append(request.getHoverName()));
            lines.add(Component.literal("One startup attempt. No retries."));
            lines.add(Component.literal("Submitted jobs stay with AE2."));
        };
        return new WidgetGroup(0, 0, 190, 100)
                .addWidget(new ComponentPanelWidget(6, 6, text).textSupplier(isRemote() ? null : text).setMaxWidthLimit(178))
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    @Override
    public boolean canShared() { return false; }

    public static void addRecipe(Consumer<FinishedRecipe> provider) {
        var essence = ChemicalHelper.get(TagPrefix.ingot, GreggyItems.TAMED_WORLD_EATER_ESSENSE).getItem();
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, HATCH.getItem())
                .requires(GTAEMachines.STOCKING_IMPORT_BUS_ME.getItem())
                .requires(essence, 4)
                .unlockedBy("has_essence", InventoryChangeTrigger.TriggerInstance.hasItems(essence))
                .save(provider, GregECore.id("me_demand_hatch"));
    }

    public static void init() {}
}
