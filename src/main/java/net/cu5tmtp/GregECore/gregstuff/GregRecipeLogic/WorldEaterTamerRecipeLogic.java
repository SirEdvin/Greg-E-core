package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorldEaterTamerRecipeLogic extends RecipeLogic {
    public enum State { DORMANT, ACTIVE }
    public static final String OPERATION = "information_collection";
    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(
            WorldEaterTamerRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    private final IncompleteWorldEaterTamer controller;
    @DescSynced
    private String progression = State.DORMANT.name();
    @DescSynced
    private int elapsedDelays;
    @DescSynced
    private int delayRemaining;
    @DescSynced
    private int attempts;
    @DescSynced
    private boolean reportPending;
    private WorldEaterScanDomain domain;
    private String dimension = "";
    private final Map<ResourceLocation, Long> counts = new TreeMap<>();
    private boolean retryPending;
    private int missingX;
    private int missingZ;
    private ItemStack pendingBook = ItemStack.EMPTY;
    private boolean successful;

    public WorldEaterTamerRecipeLogic(IncompleteWorldEaterTamer controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() { return FIELDS; }

    public State getProgressionState() { return State.valueOf(progression); }
    public Map<ResourceLocation, Long> getCounts() { return Map.copyOf(counts); }
    public int getElapsedDelays() { return elapsedDelays; }
    public int getDelayRemaining() { return delayRemaining; }
    public int getRetryAttempts() { return attempts; }
    public boolean hasPendingReport() { return reportPending; }
    public ItemStack getPendingBook() { return pendingBook.copy(); }

    private boolean accepts(GTRecipe recipe) {
        return recipe != null && recipe.recipeType == controller.getRecipeType()
                && recipe.data.contains("required_state", Tag.TAG_STRING)
                && recipe.data.getString("required_state").equals(progression)
                && progression.equals(State.DORMANT.name())
                && OPERATION.equals(recipe.data.getString("operation"))
                && recipe.duration == WorldEaterScanDomain.SCAN_TICKS
                && recipe.parallels == 1 && recipe.batchParallels == 1 && recipe.subtickParallels == 1
                && !recipe.hasTick();
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        if (reportPending || !accepts(recipe)) return ActionResult.FAIL_NO_REASON;
        return super.checkRecipe(recipe);
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        if (reportPending || !accepts(recipe) || !machine.isRecipeLogicAvailable()
                || !(controller.getLevel() instanceof ServerLevel level)) return;
        clearRun();
        GTRecipe run = recipe.copy();
        run.data = recipe.data.copy();
        super.setupRecipe(run);
        if (lastRecipe == run && isWorking()) {
            var chunk = controller.getPos();
            domain = new WorldEaterScanDomain(chunk.getX() >> 4, chunk.getZ() >> 4,
                    level.getMinBuildHeight(), level.getMaxBuildHeight());
            dimension = level.dimension().location().toString();
            onChanged();
        }
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        suspendAfterFinish = false;
        setStatus(enabled ? (domain != null || reportPending ? Status.WORKING : Status.IDLE) : Status.SUSPEND);
        onChanged();
    }

    @Override
    public void serverTick() {
        if (isSuspend() || !machine.isRecipeLogicAvailable()) return;
        if (reportPending) {
            deliverReport();
            return;
        }
        if (domain != null && !accepts(lastRecipe)) {
            interruptRecipe();
            return;
        }
        super.serverTick();
    }

    @Override
    public void handleRecipeWorking() {
        if (domain == null || !accepts(lastRecipe)
                || !(controller.getLevel() instanceof ServerLevel level)
                || !dimension.equals(level.dimension().location().toString())
                || domain.minY() != level.getMinBuildHeight() || domain.maxY() != level.getMaxBuildHeight()) {
            interruptRecipe();
            return;
        }
        var conditions = RecipeHelper.checkConditions(lastRecipe, this);
        if (!conditions.isSuccess()) { setWaiting(conditions.reason()); return; }
        if (!machine.onWorking()) { interruptRecipe(); return; }
        setStatus(Status.WORKING);
        if (delayRemaining > 0) {
            delayTick();
            return;
        }
        int scanTick = progress - elapsedDelays;
        if (scanTick < 0 || scanTick >= WorldEaterScanDomain.SCAN_TICKS) {
            interruptRecipe();
            return;
        }
        long start = domain.boundary(scanTick);
        long end = domain.boundary(scanTick + 1);
        Map<Integer, LevelChunk> chunks = new HashMap<>();
        for (long index = start; index < end;) {
            int chunkIndex = domain.chunkIndex(index);
            LevelChunk chunk = level.getChunkSource().getChunkNow(domain.chunkX(index), domain.chunkZ(index));
            if (chunk == null) {
                unavailable(domain.chunkX(index), domain.chunkZ(index));
                return;
            }
            chunks.put(chunkIndex, chunk);
            index = Math.min(end, (chunkIndex + 1L) * 256 * (domain.maxY() - domain.minY()));
        }
        retryPending = false;
        attempts = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (long index = start; index < end; index++) {
            pos.set(domain.x(index), domain.y(index), domain.z(index));
            MetaMachine candidate = MetaMachine.getMachine(chunks.get(domain.chunkIndex(index)), pos);
            if (isCountableInterface(candidate)) counts.merge(candidate.getDefinition().getId(), 1L, Long::sum);
        }
        progress++;
        totalContinuousRunningTime++;
        onChanged();
        if (scanTick + 1 == WorldEaterScanDomain.SCAN_TICKS) finalizeReport(true);
    }

    /** Controllers take precedence over generic part recognition; never follow parts outside the square. */
    public boolean isCountableInterface(MetaMachine candidate) {
        if (candidate == null || candidate == controller || candidate.getPos().equals(controller.getPos())) return false;
        for (IMultiPart part : controller.getParts()) {
            if (part.self().getPos().equals(candidate.getPos())) return false;
        }
        return candidate instanceof IMultiController || !(candidate instanceof IMultiPart);
    }

    private void unavailable(int x, int z) {
        if (!retryPending || missingX != x || missingZ != z) {
            retryPending = true;
            missingX = x;
            missingZ = z;
            attempts = 0;
        }
        if (attempts == 5) {
            finalizeReport(false);
            return;
        }
        attempts++;
        duration = Math.addExact(duration, 20);
        delayRemaining = 20;
        delayTick(); // Detection tick is the first of the 20 non-scanning ticks.
    }

    private void delayTick() {
        delayRemaining--;
        elapsedDelays++;
        progress++;
        onChanged();
    }

    private void finalizeReport(boolean success) {
        successful = success;
        pendingBook = WorldEaterReport.create(counts, success, missingX, missingZ);
        reportPending = true;
        onChanged();
        deliverReport();
    }

    @Override
    public void onRecipeFinish() {
        // super.serverTick may call this after the last batch. Never finalize/deliver twice.
        if (reportPending) deliverReport();
    }

    private void deliverReport() {
        if (!reportPending || isSuspend() || !machine.isRecipeLogicAvailable()) return;
        GTRecipe output = lastRecipe != null ? lastRecipe.copy() : controller.getRecipeType().recipeBuilder(
                net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.id("pending_survey"))
                .duration(1).buildRawRecipe();
        output.outputs.clear();
        output.outputs.put(ItemRecipeCapability.CAP,
                List.of(new Content(StrictNBTIngredient.of(pendingBook.copy()), 10000, 10000, 0)));
        var simulation = RecipeHelper.handleRecipe(machine, output, IO.OUT, output.outputs, chanceCaches, false, true);
        if (!simulation.isSuccess()) { setWaiting(Component.literal("Survey complete — report output blocked")); return; }
        var delivered = handleRecipeIO(output, IO.OUT);
        if (!delivered.isSuccess()) { setWaiting(delivered.reason()); return; }
        if (successful && counts.values().stream().mapToLong(Long::longValue).sum() >= 500) {
            progression = State.ACTIVE.name();
        }
        machine.afterWorking();
        boolean suspend = suspendAfterFinish;
        clearRun();
        super.resetRecipeLogic();
        if (suspend) setStatus(Status.SUSPEND);
        onChanged();
    }

    private void clearRun() {
        domain = null;
        dimension = "";
        counts.clear();
        elapsedDelays = delayRemaining = attempts = 0;
        retryPending = false;
        missingX = missingZ = 0;
        pendingBook = ItemStack.EMPTY;
        reportPending = successful = false;
    }

    @Override
    public void resetRecipeLogic() {
        int completedProgress = progress;
        int completedDuration = duration;
        GTRecipe completedRecipe = lastRecipe;
        super.resetRecipeLogic();
        if (reportPending) {
            // A completed report is independent of a subsequent shell break.
            progress = completedProgress;
            duration = completedDuration;
            lastRecipe = completedRecipe;
        } else {
            clearRun();
        }
        onChanged();
    }

    @Override
    public void interruptRecipe() {
        machine.afterWorking();
        resetRecipeLogic();
    }

    @Override
    public boolean hasCustomProgressLine() { return true; }

    @Override
    public Component getCustomProgressLine() {
        return Component.literal("State: " + progression + (reportPending ? " | Report pending" :
                " | Scan: " + Math.max(0, progress - elapsedDelays) + "/2400")
                + (retryPending ? " | Retry " + attempts + "/5, delay " + delayRemaining + " ticks" : ""));
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag data = new CompoundTag();
        data.putInt("Version", 1);
        data.putString("State", progression);
        if (domain != null) {
            data.putInt("CenterX", domain.centerX()); data.putInt("CenterZ", domain.centerZ());
            data.putInt("MinY", domain.minY()); data.putInt("MaxY", domain.maxY());
            data.putString("Dimension", dimension);
            data.putBoolean("Running", true);
        }
        CompoundTag savedCounts = new CompoundTag();
        counts.forEach((id, count) -> savedCounts.putLong(id.toString(), count));
        data.put("Counts", savedCounts);
        data.putInt("ElapsedDelays", elapsedDelays); data.putInt("DelayRemaining", delayRemaining);
        data.putInt("Attempts", attempts); data.putBoolean("RetryPending", retryPending);
        data.putInt("MissingX", missingX); data.putInt("MissingZ", missingZ);
        data.putBoolean("ReportPending", reportPending); data.putBoolean("Successful", successful);
        if (reportPending) data.put("Book", pendingBook.save(new CompoundTag()));
        tag.put("WorldEaterTamer", data);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        clearRun();
        progression = State.DORMANT.name();
        if (!tag.contains("WorldEaterTamer", Tag.TAG_COMPOUND)) return;
        CompoundTag data = tag.getCompound("WorldEaterTamer");
        try {
            if (data.getInt("Version") != 1) throw new IllegalArgumentException("Unknown format");
            progression = State.valueOf(data.getString("State")).name();
            if (data.getBoolean("Running")) {
                domain = new WorldEaterScanDomain(data.getInt("CenterX"), data.getInt("CenterZ"),
                        data.getInt("MinY"), data.getInt("MaxY"));
                dimension = data.getString("Dimension");
                if (ResourceLocation.tryParse(dimension) == null) throw new IllegalArgumentException("Invalid dimension");
            }
            elapsedDelays = data.getInt("ElapsedDelays"); delayRemaining = data.getInt("DelayRemaining");
            attempts = data.getInt("Attempts"); retryPending = data.getBoolean("RetryPending");
            missingX = data.getInt("MissingX"); missingZ = data.getInt("MissingZ");
            successful = data.getBoolean("Successful"); reportPending = data.getBoolean("ReportPending");
            if (reportPending && (domain == null || !progression.equals(State.DORMANT.name())))
                throw new IllegalArgumentException("Orphan completed report");
            if (reportPending) pendingBook = ItemStack.of(data.getCompound("Book"));
            CompoundTag savedCounts = data.getCompound("Counts");
            long total = 0;
            for (String key : savedCounts.getAllKeys()) {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id == null || !savedCounts.contains(key, Tag.TAG_LONG) || savedCounts.getLong(key) <= 0)
                    throw new IllegalArgumentException("Invalid count");
                long count = savedCounts.getLong(key);
                total = Math.addExact(total, count);
                counts.put(id, count);
            }
            if (domain != null) {
                int scanned = progress - elapsedDelays;
                if (elapsedDelays < 0 || progress < elapsedDelays || scanned > 2400 || duration < progress
                        || duration != 2400L + elapsedDelays + delayRemaining
                        || attempts < 0 || attempts > 5 || delayRemaining < 0 || delayRemaining > 19
                        || (!retryPending && (attempts != 0 || delayRemaining != 0))
                        || (reportPending && !successful && (!retryPending || attempts != 5 || delayRemaining != 0))
                        || (retryPending && (attempts == 0 || missingX < domain.centerX() - 4 || missingX > domain.centerX() + 4
                        || missingZ < domain.centerZ() - 4 || missingZ > domain.centerZ() + 4))
                        || total > domain.boundary(scanned) || (!reportPending && !accepts(lastRecipe))
                        || (successful && (!reportPending || scanned != 2400)))
                    throw new IllegalArgumentException("Inconsistent run");
            } else if (!counts.isEmpty() || elapsedDelays != 0 || delayRemaining != 0 || retryPending || attempts != 0 || successful) {
                throw new IllegalArgumentException("Orphan run data");
            }
            if (reportPending && (!pendingBook.is(Items.WRITTEN_BOOK) || pendingBook.getCount() != 1
                    || !net.minecraft.world.item.WrittenBookItem.makeSureTagIsValid(pendingBook.getTag())))
                throw new IllegalArgumentException("Invalid report");
        } catch (IllegalArgumentException | ArithmeticException exception) {
            progression = State.DORMANT.name();
            clearRun();
            super.resetRecipeLogic();
        }
    }
}
