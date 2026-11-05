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
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.particle.ModParticles;
import net.cu5tmtp.GregECore.item.GreggyItems;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.IntentionHatchPartMachine.Intention;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.crafting.StrictNBTIngredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WorldEaterTamerRecipeLogic extends RecipeLogic {
    public enum State { DORMANT, ACTIVE, PREPARED }
    public static final String OPERATION = "information_collection";
    public static final String CASING_OPERATION = "casing_conversion";
    public static final String BRAVERY_OPERATION = "bravery_crafting";
    public static final String TEST_OPERATION = "preparation_test";
    public static final String REAL_OPERATION = "essence_production";
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
    @DescSynced
    private boolean scanning;
    @DescSynced
    private int scanChunkX;
    @DescSynced
    private int scanChunkZ;
    private WorldEaterScanDomain domain;
    private String dimension = "";
    private final Map<ResourceLocation, Long> counts = new TreeMap<>();
    private boolean retryPending;
    private int missingX;
    private int missingZ;
    private ItemStack pendingBook = ItemStack.EMPTY;
    private boolean successful;
    @DescSynced
    private ItemStack demand = ItemStack.EMPTY;
    private int demandTier;
    @DescSynced
    private boolean rewardPending;

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
    public ItemStack getDemand() { return demand.copy(); }
    public boolean hasPendingReward() { return rewardPending; }

    @Nullable
    public ChunkPos getCurrentChunk() { return scanning ? new ChunkPos(scanChunkX, scanChunkZ) : null; }

    public Component getCurrentChunkLine() {
        return scanning ? Component.translatable("gregecore.world_eater.current_chunk", scanChunkX, scanChunkZ)
                : Component.translatable("gregecore.world_eater.no_current_chunk");
    }

    private void updateCurrentChunk() {
        int scanned = progress - elapsedDelays;
        scanning = domain != null && !reportPending && scanned >= 0 && scanned < WorldEaterScanDomain.SCAN_TICKS;
        if (scanning) {
            long index = domain.boundary(scanned);
            scanChunkX = retryPending ? missingX : domain.chunkX(index);
            scanChunkZ = retryPending ? missingZ : domain.chunkZ(index);
        } else {
            scanChunkX = scanChunkZ = 0;
        }
    }

    private boolean accepts(GTRecipe recipe) {
        if (recipe == null || recipe.recipeType != controller.getRecipeType()
                || !recipe.data.contains("required_state", Tag.TAG_STRING)
                || !stateMatches(recipe)) return false;
        if (isCraftingRecipe(recipe)) return !isBraveryRecipe(recipe) || controller.getCasingTier() == 2;
        if (isDemandRecipe(recipe)) return controller.getCasingTier() == 2 && controller.getIntention() != Intention.NONE;
        return progression.equals(State.DORMANT.name()) && OPERATION.equals(recipe.data.getString("operation"))
                && recipe.duration == WorldEaterScanDomain.SCAN_TICKS
                && recipe.parallels == 1 && recipe.batchParallels == 1 && recipe.subtickParallels == 1
                && !recipe.hasTick();
    }

    private boolean stateMatches(GTRecipe recipe) {
        return recipe.data.getString("required_state").equals(progression)
                || progression.equals(State.PREPARED.name()) && isCraftingRecipe(recipe);
    }

    private static boolean hasDemandOperation(GTRecipe recipe) {
        return recipe != null && (TEST_OPERATION.equals(recipe.data.getString("operation"))
                || REAL_OPERATION.equals(recipe.data.getString("operation")));
    }

    public static boolean isDemandRecipe(GTRecipe recipe) {
        if (!hasDemandOperation(recipe)) return false;
        boolean test = TEST_OPERATION.equals(recipe.data.getString("operation"));
        if (!(test ? State.ACTIVE : State.PREPARED).name().equals(recipe.data.getString("required_state"))
                || !recipe.data.contains("required_casing_tier", Tag.TAG_ANY_NUMERIC)
                || recipe.data.getDouble("required_casing_tier") != 2 || recipe.duration != 600
                || recipe.parallels != 1 || recipe.batchParallels != 1 || recipe.subtickParallels != 1
                || recipe.ocLevel != 0 || recipe.hasTick() || recipe.inputs.size() != 1) return false;
        if (!isSingleItem(recipe.getInputContents(ItemRecipeCapability.CAP),
                ChemicalHelper.get(TagPrefix.gem, test ? GreggyItems.DESARIUM : GreggyItems.IMPLEMENTIUM))) return false;
        return test ? recipe.outputs.isEmpty() : recipe.outputs.size() == 1 && isSingleItem(
                recipe.getOutputContents(ItemRecipeCapability.CAP), ChemicalHelper.get(TagPrefix.ingot, GreggyItems.TAMED_WORLD_EATER_ESSENSE));
    }

    private static boolean isSingleItem(List<Content> contents, ItemStack expected) {
        if (expected.isEmpty() || contents.size() != 1 || contents.get(0).isChanced()
                || !(contents.get(0).content instanceof Ingredient ingredient)) return false;
        var stacks = ingredient.getItems();
        return stacks.length == 1 && stacks[0].getCount() == 1 && stacks[0].is(expected.getItem());
    }

    public static boolean isCasingRecipe(GTRecipe recipe) {
        return recipe != null && recipe.data.contains("required_state", Tag.TAG_STRING)
                && State.ACTIVE.name().equals(recipe.data.getString("required_state"))
                && CASING_OPERATION.equals(recipe.data.getString("operation"))
                && recipe.duration == 100 && recipe.parallels >= 1
                && recipe.batchParallels == 1 && recipe.subtickParallels == 1 && !recipe.hasTick();
    }

    private static boolean isBraveryRecipe(GTRecipe recipe) {
        return recipe != null && State.ACTIVE.name().equals(recipe.data.getString("required_state"))
                && BRAVERY_OPERATION.equals(recipe.data.getString("operation"))
                // JSON recipe decoding may narrow integer metadata to a byte tag.
                && recipe.data.contains("required_casing_tier", Tag.TAG_ANY_NUMERIC)
                && recipe.data.getDouble("required_casing_tier") == 2
                && recipe.duration == 1200 && recipe.parallels == 1
                && recipe.batchParallels == 1 && recipe.subtickParallels == 1 && !recipe.hasTick();
    }

    private static boolean isCraftingRecipe(GTRecipe recipe) {
        return isCasingRecipe(recipe) || isBraveryRecipe(recipe);
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        if (rewardPending) return ActionResult.fail(Component.literal("Earned essence must be delivered first."), null, null);
        if (reportPending) return ActionResult.fail(Component.literal(
                "Survey report must be delivered before starting another recipe."), null, null);
        if (accepts(recipe)) {
            if (isDemandRecipe(recipe) && WorldEaterDemands.pool().isEmpty())
                return ActionResult.fail(Component.literal("No registered demand items are available."), null, null);
            return super.checkRecipe(recipe);
        }
        if (recipe != null && recipe.recipeType == controller.getRecipeType()) {
            String requiredState = recipe.data.getString("required_state");
            if ((State.ACTIVE.name().equals(requiredState) || State.DORMANT.name().equals(requiredState)
                    || State.PREPARED.name().equals(requiredState)) && !stateMatches(recipe)) return ActionResult.fail(Component.literal(
                            "Requires " + requiredState + " state (current: " + progression + ")."), null, null);
            if ((isBraveryRecipe(recipe) || isDemandRecipe(recipe)) && controller.getCasingTier() != 2) return ActionResult.fail(Component.literal(
                    "Requires a formed tier-2 frame (all casings colored)."), null, null);
            if (isDemandRecipe(recipe) && controller.getIntention() == Intention.NONE) return ActionResult.fail(Component.literal(
                    "Requires an installed Safety or Bravery Hatch."), null, null);
        }
        return ActionResult.fail(Component.literal("Unsupported or invalid World Eater tamer recipe."), null, null);
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        if (reportPending || rewardPending || !demand.isEmpty() || !accepts(recipe) || !machine.isRecipeLogicAvailable()
                || !(controller.getLevel() instanceof ServerLevel level)) return;
        if ((isCraftingRecipe(recipe) || isDemandRecipe(recipe)) && !checkRecipe(recipe).isSuccess()) return;
        clearRun();
        GTRecipe run = recipe.copy();
        run.data = recipe.data.copy();
        super.setupRecipe(run);
        if (lastRecipe == run && isWorking() && isDemandRecipe(run)) {
            var pool = WorldEaterDemands.pool();
            var entry = pool.get(level.random.nextInt(pool.size()));
            demand = entry.stack(1 + level.random.nextInt(5));
            demandTier = entry.tier();
            duration = entry.deadline();
            onChanged();
        } else if (lastRecipe == run && isWorking() && OPERATION.equals(run.data.getString("operation"))) {
            var chunk = controller.getPos();
            domain = new WorldEaterScanDomain(chunk.getX() >> 4, chunk.getZ() >> 4,
                    level.getMinBuildHeight(), level.getMaxBuildHeight());
            dimension = level.dimension().location().toString();
            updateCurrentChunk();
            onChanged();
        }
    }

    @Override
    public void setWorkingEnabled(boolean enabled) {
        suspendAfterFinish = false;
        setStatus(enabled ? (domain != null || reportPending || lastRecipe != null && duration > 0
                ? Status.WORKING : Status.IDLE) : Status.SUSPEND);
        onChanged();
    }

    @Override
    public void serverTick() {
        if (isSuspend() || !machine.isRecipeLogicAvailable()) return;
        if (rewardPending) { deliverReward(); return; }
        if (reportPending) {
            deliverReport();
            return;
        }
        if ((domain != null || lastRecipe != null && duration > 0) && !accepts(lastRecipe)) {
            interruptRecipe();
            return;
        }
        super.serverTick();
    }

    @Override
    public void handleRecipeWorking() {
        if (!demand.isEmpty()) {
            if (!accepts(lastRecipe)) { interruptRecipe(); return; }
            var conditions = RecipeHelper.checkConditions(lastRecipe, this);
            if (!conditions.isSuccess()) { setWaiting(conditions.reason()); return; }
            if (!machine.onWorking()) { interruptRecipe(); return; }
            setStatus(Status.WORKING);
            if (tryPayDemand()) return;
            progress++;
            totalContinuousRunningTime++;
            onChanged();
            return;
        }
        if (domain == null && accepts(lastRecipe) && isCraftingRecipe(lastRecipe)) {
            super.handleRecipeWorking();
            return;
        }
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
            if (isCountableInterface(candidate)) {
                counts.merge(candidate.getDefinition().getId(), 1L, Long::sum);
                Vec3 source = Vec3.atCenterOf(pos);
                Vec3 travel = controller.getStructureCenter().subtract(source);
                // Count zero transmits one particle with an exact displacement, not random spread.
                for (var player : level.players()) {
                    level.sendParticles(player, ModParticles.WORLD_EATER_SCAN.get(), true,
                            source.x, source.y, source.z, 0, travel.x, travel.y, travel.z, 1);
                }
            }
        }
        progress++;
        totalContinuousRunningTime++;
        updateCurrentChunk();
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
        updateCurrentChunk();
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
        updateCurrentChunk();
        onChanged();
        deliverReport();
    }

    @Override
    public void onRecipeFinish() {
        if (rewardPending) { deliverReward(); return; }
        if (hasDemandOperation(lastRecipe)) {
            if (demand.isEmpty() || progress < duration) return;
            if (!accepts(lastRecipe) || !machine.isRecipeLogicAvailable()) { interruptRecipe(); return; }
            if (isSuspend() || !RecipeHelper.checkConditions(lastRecipe, this).isSuccess()) return;
            if (tryPayDemand()) return;
            MetaMachine victim = null;
            if (REAL_OPERATION.equals(lastRecipe.data.getString("operation"))) {
                if (controller.getIntention() == Intention.SAFETY) {
                    for (var part : controller.getParts()) if (part instanceof IntentionHatchPartMachine hatch
                            && hatch.getIntention() == Intention.SAFETY) { victim = hatch; break; }
                } else if (controller.getLevel() instanceof ServerLevel level) {
                    var region = demandRegion(level);
                    var random = level.random;
                    victim = findBraveryVictim(new BlockPos((region.centerX() - 4) * 16 + random.nextInt(144),
                            region.minY() + random.nextInt(region.maxY() - region.minY()),
                            (region.centerZ() - 4) * 16 + random.nextInt(144)));
                }
            }
            settleDemand(); // Removal can re-enter invalidation; the outcome is already settled.
            if (victim != null) destroyVictim(victim);
            return;
        }
        if (domain == null && accepts(lastRecipe) && isCraftingRecipe(lastRecipe)) {
            // Native completion does not retain outputs if capacity changed mid-run.
            // Preflight before delegating, keeping the paid native job pending.
            var output = RecipeHelper.handleRecipe(machine, lastRecipe, IO.OUT, lastRecipe.outputs,
                    chanceCaches, false, true);
            if (!output.isSuccess()) { setWaiting(output.reason()); return; }
            super.onRecipeFinish();
            return;
        }
        // super.serverTick may call this after the last batch. Never finalize/deliver twice.
        if (reportPending) deliverReport();
    }

    private boolean handleDemand(boolean simulate) {
        List<?> remaining = new ArrayList<>(List.of(SizedIngredient.create(demand.copy())));
        for (var handler : new LinkedHashSet<>(controller.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP))) {
            remaining = handler.handleRecipe(IO.IN, lastRecipe, remaining, simulate);
            if (remaining == null || remaining.isEmpty()) return true;
        }
        return false;
    }

    private boolean tryPayDemand() {
        if (!handleDemand(true)) return false;
        if (!handleDemand(false)) { interruptRecipe(); return true; }
        demand = ItemStack.EMPTY;
        if (TEST_OPERATION.equals(lastRecipe.data.getString("operation"))) {
            progression = State.PREPARED.name();
            settleDemand();
        } else {
            rewardPending = true;
            onChanged();
            deliverReward();
        }
        return true;
    }

    private void deliverReward() {
        if (!rewardPending || isSuspend() || !machine.isRecipeLogicAvailable()) return;
        var simulation = RecipeHelper.handleRecipe(machine, lastRecipe, IO.OUT, lastRecipe.outputs, chanceCaches, false, true);
        if (!simulation.isSuccess()) { setWaiting(Component.literal("Essence earned — output blocked")); return; }
        var delivered = handleRecipeIO(lastRecipe, IO.OUT);
        if (!delivered.isSuccess()) { setWaiting(delivered.reason()); return; }
        settleDemand();
    }

    private void settleDemand() {
        machine.afterWorking();
        boolean suspend = suspendAfterFinish;
        clearRun();
        super.resetRecipeLogic();
        // An abort reset drops polling; completion must recheck already queued inputs.
        markLastRecipeDirty();
        if (suspend) setStatus(Status.SUSPEND);
        onChanged();
    }

    private WorldEaterScanDomain demandRegion(ServerLevel level) {
        return new WorldEaterScanDomain(controller.getPos().getX() >> 4, controller.getPos().getZ() >> 4,
                level.getMinBuildHeight(), level.getMaxBuildHeight());
    }

    @Nullable
    public MetaMachine findBraveryVictim(BlockPos origin) {
        if (!(controller.getLevel() instanceof ServerLevel level)) return null;
        var region = demandRegion(level);
        MetaMachine nearest = null;
        double distance = Double.POSITIVE_INFINITY;
        for (int x = region.centerX() - 4; x <= region.centerX() + 4; x++) {
            for (int z = region.centerZ() - 4; z <= region.centerZ() + 4; z++) {
                var chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (var pos : chunk.getBlockEntities().keySet()) {
                    if (pos.getY() < region.minY() || pos.getY() >= region.maxY()) continue;
                    var candidate = MetaMachine.getMachine(chunk, pos);
                    if (!isCountableInterface(candidate)) continue;
                    double d = pos.distSqr(origin);
                    if (d < distance || d == distance && (nearest == null || pos.compareTo(nearest.getPos()) < 0)) {
                        nearest = candidate;
                        distance = d;
                    }
                }
            }
        }
        return nearest;
    }

    private void destroyVictim(MetaMachine victim) {
        if (!(controller.getLevel() instanceof ServerLevel level)) return;
        var pos = victim.getPos();
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || MetaMachine.getMachine(chunk, pos) != victim) return;
        if (victim instanceof IntentionHatchPartMachine hatch) {
            if (hatch.getIntention() != Intention.SAFETY || !controller.getParts().contains(hatch)) return;
        } else if (!isCountableInterface(victim)) return;
        if (!level.destroyBlock(pos, true)) return;
        level.sendParticles(ParticleTypes.EXPLOSION, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1, 1);
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
        markLastRecipeDirty();
        if (suspend) setStatus(Status.SUSPEND);
        onChanged();
    }

    private void clearRun() {
        demand = ItemStack.EMPTY;
        demandTier = 0;
        rewardPending = false;
        domain = null;
        dimension = "";
        counts.clear();
        elapsedDelays = delayRemaining = attempts = 0;
        retryPending = false;
        missingX = missingZ = 0;
        pendingBook = ItemStack.EMPTY;
        reportPending = successful = false;
        scanning = false;
        scanChunkX = scanChunkZ = 0;
    }

    @Override
    public void resetRecipeLogic() {
        int completedProgress = progress;
        int completedDuration = duration;
        GTRecipe completedRecipe = lastRecipe;
        super.resetRecipeLogic();
        if (reportPending || rewardPending) {
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
        if (rewardPending) return Component.literal("State: " + progression + " | Essence earned — output pending");
        if (!demand.isEmpty()) return Component.literal("State: " + progression + " | Demand: " + demand.getCount() + " x ")
                .append(demand.getHoverName()).append(" | Deadline: " + (Math.max(0, duration - progress) + 19) / 20 + "s remaining");
        if (!progression.equals(State.DORMANT.name())) {
            return Component.literal("State: " + progression + " | " + (lastRecipe != null && duration > 0
                    ? "Craft: " + progress + "/" + duration + " | Parallels: " + lastRecipe.parallels : "Idle"));
        }
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
        if (!demand.isEmpty() || rewardPending) {
            CompoundTag saved = new CompoundTag();
            saved.putString("Operation", lastRecipe.data.getString("operation"));
            saved.put("Item", demand.save(new CompoundTag()));
            saved.putInt("Tier", demandTier);
            saved.putBoolean("RewardPending", rewardPending);
            data.put("Demand", saved);
        }
        tag.put("WorldEaterTamer", data);
    }

    private void loadDemand(CompoundTag data) {
        if (!data.contains("Demand", Tag.TAG_COMPOUND) || !isDemandRecipe(lastRecipe)
                || lastRecipe.recipeType != controller.getRecipeType() || !stateMatches(lastRecipe)
                || data.getBoolean("Running") || data.getBoolean("ReportPending") || data.getBoolean("Successful")
                || !data.getCompound("Counts").isEmpty() || data.getInt("ElapsedDelays") != 0
                || data.getInt("DelayRemaining") != 0 || data.getInt("Attempts") != 0 || data.getBoolean("RetryPending"))
            throw new IllegalArgumentException("Invalid demand context");
        var saved = data.getCompound("Demand");
        if (!saved.contains("Tier", Tag.TAG_INT) || !saved.contains("Item", Tag.TAG_COMPOUND)
                || !saved.contains("RewardPending", Tag.TAG_BYTE) || saved.getByte("RewardPending") < 0
                || saved.getByte("RewardPending") > 1) throw new IllegalArgumentException("Invalid demand fields");
        demandTier = saved.getInt("Tier");
        rewardPending = saved.getBoolean("RewardPending");
        var savedItem = saved.getCompound("Item");
        if (!savedItem.contains("id", Tag.TAG_STRING) || !savedItem.contains("Count", Tag.TAG_BYTE))
            throw new IllegalArgumentException("Invalid saved item fields");
        demand = ItemStack.of(savedItem);
        if (!saved.getString("Operation").equals(lastRecipe.data.getString("operation"))
                || demandTier < 1 || demandTier > 10 || duration != 600 * demandTier || progress < 0 || progress >= duration)
            throw new IllegalArgumentException("Invalid demand deadline or kind");
        if (rewardPending) {
            if (!REAL_OPERATION.equals(saved.getString("Operation"))
                    || !savedItem.equals(ItemStack.EMPTY.save(new CompoundTag())))
                throw new IllegalArgumentException("Invalid earned reward");
        } else if (demand.isEmpty() || demand.getCount() < 1 || demand.getCount() > 5 || demand.hasTag()
                || WorldEaterDemands.pool().stream().noneMatch(e -> e.item() == demand.getItem() && e.tier() == demandTier)) {
            throw new IllegalArgumentException("Invalid or unavailable demand");
        }
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
            if (data.contains("Demand") || hasDemandOperation(lastRecipe)) {
                try { loadDemand(data); }
                catch (IllegalArgumentException exception) {
                    clearRun();
                    super.resetRecipeLogic();
                }
                return;
            }
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
            updateCurrentChunk();
        } catch (IllegalArgumentException | ArithmeticException exception) {
            progression = State.DORMANT.name();
            clearRun();
            super.resetRecipeLogic();
        }
    }
}
