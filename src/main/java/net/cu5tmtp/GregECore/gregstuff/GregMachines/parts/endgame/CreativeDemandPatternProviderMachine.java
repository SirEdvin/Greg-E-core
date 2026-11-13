package net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame;

import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.integration.ae2.machine.MEBusPartMachine;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterDemands;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

/** Creative-only test provider; loaded only behind the AE2 availability guard. */
public class CreativeDemandPatternProviderMachine extends MEBusPartMachine implements ICraftingProvider {
    public static final int DELAY = 40;
    public static final MachineDefinition PROVIDER = REGISTRATE.machine("creative_demand_pattern_provider", CreativeDemandPatternProviderMachine::new)
            .langValue("Creative Demand Pattern Provider")
            .tier(GTValues.LuV)
            .rotationState(RotationState.ALL)
            .colorOverlayTieredHullModel(GTCEu.id("block/overlay/appeng/me_buffer_hatch"))
            .tooltips(GregECore.tooltips("creative_demand_pattern_provider", 2))
            .register();

    private static List<IPatternDetails> patterns;
    private final List<Delivery> deliveries = new ArrayList<>();
    private TickableSubscription deliverySubs;

    private record Delivery(GenericStack stack, long due) {}

    public CreativeDemandPatternProviderMachine(IMachineBlockEntity holder, Object... args) {
        super(holder, IO.IN, args);
        getMainNode().addService(ICraftingProvider.class, this);
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    @Override
    public void onRotated(Direction oldFacing, Direction newFacing) {
        super.onRotated(oldFacing, newFacing);
        getMainNode().setExposedOnSides(EnumSet.allOf(Direction.class));
    }

    /** One native processing pattern per demand pool item: 1 Iron Ingot -> 1 item. */
    public static List<IPatternDetails> patterns(Level level) {
        if (patterns == null) {
            var built = new ArrayList<IPatternDetails>();
            var iron = new GenericStack[]{new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)};
            for (var entry : WorldEaterDemands.pool()) {
                var encoded = PatternDetailsHelper.encodeProcessingPattern(iron, new GenericStack[]{new GenericStack(AEItemKey.of(entry.stack(1)), 1)});
                built.add(PatternDetailsHelper.decodePattern(encoded, level));
            }
            patterns = List.copyOf(built);
        }
        return patterns;
    }

    @Override
    public List<IPatternDetails> getAvailablePatterns() {
        return isOnline() && getLevel() != null ? patterns(getLevel()) : List.of();
    }

    @Override
    public boolean pushPattern(IPatternDetails details, KeyCounter[] inputHolder) {
        if (!isOnline() || getLevel() == null) return false;
        // Inputs (the iron) are voided by simply not returning them.
        long due = getLevel().getGameTime() + DELAY;
        for (var output : details.getOutputs()) deliveries.add(new Delivery(output, due));
        updateDeliverySubscription();
        return true;
    }

    @Override
    public boolean isBusy() { return false; }

    public int pendingDeliveries() { return deliveries.size(); }

    private void deliver() {
        var grid = getMainNode().getGrid();
        if (grid != null && isOnline()) {
            long now = getLevel().getGameTime();
            var storage = grid.getStorageService().getInventory();
            for (int i = 0; i < deliveries.size(); i++) {
                var delivery = deliveries.get(i);
                if (delivery.due() > now) continue;
                var stack = delivery.stack();
                long left = stack.amount() - storage.insert(stack.what(), stack.amount(), Actionable.MODULATE, actionSource);
                if (left <= 0) deliveries.remove(i--);
                else deliveries.set(i, new Delivery(new GenericStack(stack.what(), left), delivery.due()));
            }
        }
        updateDeliverySubscription();
    }

    private void updateDeliverySubscription() {
        if (deliveries.isEmpty()) {
            if (deliverySubs != null) { deliverySubs.unsubscribe(); deliverySubs = null; }
        } else if (deliverySubs == null) {
            deliverySubs = subscribeServerTick(this::deliver);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!isRemote()) updateDeliverySubscription();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        ICraftingProvider.requestUpdate(getMainNode());
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        if (forDrop) return;
        var list = new ListTag();
        for (var delivery : deliveries) {
            var entry = GenericStack.writeTag(delivery.stack());
            entry.putLong("due", delivery.due());
            list.add(entry);
        }
        tag.put("deliveries", list);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        deliveries.clear();
        for (var entry : tag.getList("deliveries", Tag.TAG_COMPOUND)) {
            var compound = (CompoundTag) entry;
            var stack = GenericStack.readTag(compound);
            if (stack != null) deliveries.add(new Delivery(stack, compound.getLong("due")));
        }
    }

    @Override
    protected int getInventorySize() { return 0; }

    @Override
    protected NotifiableItemStackHandler createCircuitItemHandler(Object... args) {
        return super.createCircuitItemHandler(IO.NONE);
    }

    @Override
    public void attachConfigurators(ConfiguratorPanel panel) {}

    @Override
    public Widget createUIWidget() {
        Consumer<List<Component>> text = lines -> {
            lines.add(Component.translatable(isOnline() ? "gregecore.me.online" : "gregecore.me.offline"));
            lines.add(Component.translatable("gregecore.creative_demand_pattern_provider.pending", deliveries.size()));
        };
        return new WidgetGroup(0, 0, 190, 80)
                .addWidget(new ComponentPanelWidget(6, 6, text).textSupplier(isRemote() ? null : text).setMaxWidthLimit(178))
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    public static void init() {}
}
