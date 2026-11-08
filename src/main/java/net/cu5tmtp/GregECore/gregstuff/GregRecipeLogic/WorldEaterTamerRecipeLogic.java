package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.GTCEu;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.MEDemandHatchPartMachine;
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

import static net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterScanDomain.SCAN_TICKS;

public class WorldEaterTamerRecipeLogic extends RecipeLogic {
    public enum State { DORMANT, ACTIVE, PREPARED }
    public static final String OPERATION = "information_collection";
    public static final String CASING_OPERATION = "casing_conversion";
    public static final String BRAVERY_OPERATION = "bravery_crafting";
    public static final String TEST_OPERATION = "preparation_test";
    public static final String REAL_OPERATION = "essence_production";
    private static final int REQUIRED_TIER = 2;
    private static final int ACTIVATION_COUNT = 500;
    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY = 20;

    /** The only recipe shapes this machine runs; each is validated once, here. */
    enum Op {
        SURVEY(OPERATION, State.DORMANT, SCAN_TICKS, false),
        CASING(CASING_OPERATION, State.ACTIVE, 100, false),
        BRAVERY(BRAVERY_OPERATION, State.ACTIVE, 1200, true),
        TEST(TEST_OPERATION, State.ACTIVE, 600, true),
        REAL(REAL_OPERATION, State.PREPARED, 600, true);

        final String id;
        final State state;
        final int duration;
        final boolean needsTier;

        Op(String id, State state, int duration, boolean needsTier) {
            this.id = id;
            this.state = state;
            this.duration = duration;
            this.needsTier = needsTier;
        }

        boolean crafting() { return this == CASING || this == BRAVERY; }
        boolean demand() { return this == TEST || this == REAL; }

        @Nullable
        static Op of(@Nullable GTRecipe recipe) {
            if (recipe == null) return null;
            String id = recipe.data.getString("operation");
            for (Op op : values()) if (op.id.equals(id)) return op.matches(recipe) ? op : null;
            return null;
        }

        private boolean matches(GTRecipe recipe) {
            if (!state.name().equals(recipe.data.getString("required_state")) || recipe.duration != duration
                    || (this == CASING ? recipe.parallels < 1 : recipe.parallels != 1)
                    || recipe.batchParallels != 1 || recipe.subtickParallels != 1 || recipe.hasTick()) return false;
            // JSON recipe decoding may narrow integer metadata to a byte tag.
            if (needsTier && (!recipe.data.contains("required_casing_tier", Tag.TAG_ANY_NUMERIC)
                    || recipe.data.getDouble("required_casing_tier") != REQUIRED_TIER)) return false;
            if (!demand()) return true;
            if (recipe.ocLevel != 0 || recipe.inputs.size() != 1 || !isSingleItem(recipe.getInputContents(ItemRecipeCapability.CAP),
                    ChemicalHelper.get(TagPrefix.gem, this == TEST ? GreggyItems.DESARIUM : GreggyItems.IMPLEMENTIUM))) return false;
            return this == TEST ? recipe.outputs.isEmpty() : recipe.outputs.size() == 1 && isSingleItem(
                    recipe.getOutputContents(ItemRecipeCapability.CAP), ChemicalHelper.get(TagPrefix.ingot, GreggyItems.TAMED_WORLD_EATER_ESSENSE));
        }
    }

    private static final ManagedFieldHolder FIELDS = new ManagedFieldHolder(
            WorldEaterTamerRecipeLogic.class, RecipeLogic.MANAGED_FIELD_HOLDER);
    private final IncompleteWorldEaterTamer controller;
    @DescSynced
    private State progression = State.DORMANT;
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
    private boolean craftingConsidered;

    public WorldEaterTamerRecipeLogic(IncompleteWorldEaterTamer controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public ManagedFieldHolder getFieldHolder() { return FIELDS; }

    public State getProgressionState() { return progression; }
    public Map<ResourceLocation, Long> getCounts() { return Map.copyOf(counts); }
    public int getElapsedDelays() { return elapsedDelays; }
    public int getDelayRemaining() { return delayRemaining; }
    public int getRetryAttempts() { return attempts; }
    public boolean hasPendingReport() { return reportPending; }
    public ItemStack getPendingBook() { return pendingBook.copy(); }
    public ItemStack getDemand() { return demand.copy(); }
    public boolean hasPendingReward() { return rewardPending; }

    public ItemStack getRealDemand() {
        return Op.of(lastRecipe) == Op.REAL && !rewardPending ? demand.copy() : ItemStack.EMPTY;
    }

    @Nullable
    public ChunkPos getCurrentChunk() { return scanning ? new ChunkPos(scanChunkX, scanChunkZ) : null; }

    public Component getCurrentChunkLine() {
        return scanning ? Component.translatable("gregecore.world_eater.current_chunk", scanChunkX, scanChunkZ)
                : Component.translatable("gregecore.world_eater.no_current_chunk");
    }

    private void updateCurrentChunk() {
        int scanned = progress - elapsedDelays;
        scanning = domain != null && !reportPending && scanned >= 0 && scanned < SCAN_TICKS;
        if (scanning) {
            long index = domain.boundary(scanned);
            scanChunkX = retryPending ? missingX : domain.chunkX(index);
            scanChunkZ = retryPending ? missingZ : domain.chunkZ(index);
        } else {
            scanChunkX = scanChunkZ = 0;
        }
    }

    private boolean accepts(GTRecipe recipe) {
        Op op = Op.of(recipe);
        if (op == null || recipe.recipeType != controller.getRecipeType() || !stateMatches(recipe, op)) return false;
        if (op.needsTier && controller.getCasingTier() != REQUIRED_TIER) return false;
        return !op.demand() || controller.getIntention() != Intention.NONE;
    }

    /** Crafting stays available once PREPARED; unrecognized recipes ({@code op == null}) only match literally. */
    private boolean stateMatches(GTRecipe recipe, @Nullable Op op) {
        return recipe.data.getString("required_state").equals(progression.name())
                || progression == State.PREPARED && op != null && op.crafting();
    }

    private static boolean hasDemandOperation(GTRecipe recipe) {
        return recipe != null && (TEST_OPERATION.equals(recipe.data.getString("operation"))
                || REAL_OPERATION.equals(recipe.data.getString("operation")));
    }

    public static boolean isDemandRecipe(GTRecipe recipe) {
        Op op = Op.of(recipe);
        return op != null && op.demand();
    }

    public static boolean isCasingRecipe(GTRecipe recipe) { return Op.of(recipe) == Op.CASING; }

    private static boolean isSingleItem(List<Content> contents, ItemStack expected) {
        if (expected.isEmpty() || contents.size() != 1 || contents.get(0).isChanced()
                || !(contents.get(0).content instanceof Ingredient ingredient)) return false;
        var stacks = ingredient.getItems();
        return stacks.length == 1 && stacks[0].getCount() == 1 && stacks[0].is(expected.getItem());
    }

    @Override
    protected ActionResult checkRecipe(GTRecipe recipe) {
        if (rewardPending) return fail("Earned essence must be delivered first.");
        if (reportPending) return fail("Survey report must be delivered before starting another recipe.");
        if (accepts(recipe)) {
            if (isDemandRecipe(recipe) && WorldEaterDemands.pool().isEmpty())
                return fail("No registered demand items are available.");
            return super.checkRecipe(recipe);
        }
        if (recipe != null && recipe.recipeType == controller.getRecipeType()) {
            Op op = Op.of(recipe);
            String requiredState = recipe.data.getString("required_state");
            for (State state : State.values()) if (state.name().equals(requiredState) && !stateMatches(recipe, op))
                return fail("Requires " + requiredState + " state (current: " + progression + ").");
            if (op != null && op.needsTier && controller.getCasingTier() != REQUIRED_TIER)
                return fail("Requires a formed tier-2 frame (all casings colored).");
            if (op != null && op.demand() && controller.getIntention() == Intention.NONE)
                return fail("Requires an installed Safety or Bravery Hatch.");
        }
        return fail("Unsupported or invalid World Eater tamer recipe.");
    }

    private static ActionResult fail(String reason) {
        return ActionResult.fail(Component.literal(reason), null, null);
    }

    @Override
    public void setupRecipe(GTRecipe recipe) {
        if (reportPending || rewardPending || !demand.isEmpty() || !accepts(recipe) || !machine.isRecipeLogicAvailable()
                || !(controller.getLevel() instanceof ServerLevel level)) return;
        Op op = Op.of(recipe);
        if (op != Op.SURVEY && !checkRecipe(recipe).isSuccess()) return;
        clearRun();
        GTRecipe run = recipe.copy();
        run.data = recipe.data.copy();
        super.setupRecipe(run);
        if (lastRecipe != run || !isWorking()) return;
        if (op.demand()) {
            var pool = WorldEaterDemands.pool();
            var entry = pool.get(level.random.nextInt(pool.size()));
            demand = entry.stack(1 + level.random.nextInt(5));
            demandTier = entry.tier();
            duration = entry.deadline();
            craftingConsidered = op == Op.REAL;
            onChanged();
            if (craftingConsidered && GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.begin(controller);
        } else if (op == Op.SURVEY) {
            domain = region(level);
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
        if (reportPending) { deliverReport(); return; }
        if ((domain != null || lastRecipe != null && duration > 0) && !accepts(lastRecipe)) {
            interruptRecipe();
            return;
        }
        super.serverTick();
    }

    /** Shared per-tick gate for custom runs; false means this tick is already handled. */
    private boolean beginTick() {
        var conditions = RecipeHelper.checkConditions(lastRecipe, this);
        if (!conditions.isSuccess()) { setWaiting(conditions.reason()); return false; }
        if (!machine.onWorking()) { interruptRecipe(); return false; }
        setStatus(Status.WORKING);
        return true;
    }

    @Override
    public void handleRecipeWorking() {
        if (!demand.isEmpty()) {
            if (!accepts(lastRecipe)) { interruptRecipe(); return; }
            if (!beginTick() || tryPayDemand()) return;
            if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.tick(controller);
            progress++;
            totalContinuousRunningTime++;
            onChanged();
            return;
        }
        if (domain == null && accepts(lastRecipe) && Op.of(lastRecipe).crafting()) {
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
        if (!beginTick()) return;
        if (delayRemaining > 0) {
            delayTick();
            return;
        }
        int scanTick = progress - elapsedDelays;
        if (scanTick < 0 || scanTick >= SCAN_TICKS) {
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
        if (scanTick + 1 == SCAN_TICKS) finalizeReport(true);
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
        if (attempts == MAX_RETRIES) {
            finalizeReport(false);
            return;
        }
        attempts++;
        duration = Math.addExact(duration, RETRY_DELAY);
        delayRemaining = RETRY_DELAY;
        updateCurrentChunk();
        delayTick(); // Detection tick is the first of the non-scanning delay ticks.
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
            MetaMachine victim = pickVictim();
            finishRun(); // Removal can re-enter invalidation; the outcome is already settled.
            if (victim != null) destroyVictim(victim);
            return;
        }
        if (domain == null && accepts(lastRecipe) && Op.of(lastRecipe).crafting()) {
            // Native completion does not retain outputs if capacity changed mid-run.
            // Preflight before delegating, keeping the paid native job pending.
            if (outputFits(lastRecipe, null)) super.onRecipeFinish();
            return;
        }
        // super.serverTick may call this after the last batch. Never finalize/deliver twice.
        if (reportPending) deliverReport();
    }

    public int getDemandRemainder() { return demand.isEmpty() ? 0 : handleDemand(true); }

    private int handleDemand(boolean simulate) {
        List<?> remaining = new ArrayList<>(List.of(SizedIngredient.create(demand.copy())));
        var handlers = GTCEu.Mods.isAE2Loaded()
                ? MEDemandHatchPartMachine.demandInputs(controller, demand)
                : new ArrayList<>(new LinkedHashSet<>(controller.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP)));
        for (var handler : handlers) {
            remaining = handler.handleRecipe(IO.IN, lastRecipe, remaining, simulate);
            if (remaining == null || remaining.isEmpty()) return 0;
        }
        return ((SizedIngredient) remaining.get(0)).getAmount();
    }

    private boolean tryPayDemand() {
        if (handleDemand(true) != 0) return false;
        if (handleDemand(false) != 0) { interruptRecipe(); return true; }
        demand = ItemStack.EMPTY;
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.clear(controller);
        if (Op.of(lastRecipe) == Op.TEST) {
            progression = State.PREPARED;
            finishRun();
        } else {
            rewardPending = true;
            onChanged();
            deliverReward();
        }
        return true;
    }

    /** Simulates output first so a blocked result keeps the earned run pending. */
    private boolean outputFits(GTRecipe recipe, @Nullable Component blocked) {
        var simulation = RecipeHelper.handleRecipe(machine, recipe, IO.OUT, recipe.outputs, chanceCaches, false, true);
        if (simulation.isSuccess()) return true;
        setWaiting(blocked != null ? blocked : simulation.reason());
        return false;
    }

    private boolean deliver(GTRecipe recipe, Component blocked) {
        if (!outputFits(recipe, blocked)) return false;
        var delivered = handleRecipeIO(recipe, IO.OUT);
        if (!delivered.isSuccess()) setWaiting(delivered.reason());
        return delivered.isSuccess();
    }

    private void deliverReward() {
        if (!rewardPending || isSuspend() || !machine.isRecipeLogicAvailable()) return;
        if (deliver(lastRecipe, Component.literal("Essence earned — output blocked"))) finishRun();
    }

    private void deliverReport() {
        if (!reportPending || isSuspend() || !machine.isRecipeLogicAvailable()) return;
        GTRecipe output = lastRecipe != null ? lastRecipe.copy() : controller.getRecipeType().recipeBuilder(
                net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.id("pending_survey"))
                .duration(1).buildRawRecipe();
        output.outputs.clear();
        output.outputs.put(ItemRecipeCapability.CAP,
                List.of(new Content(StrictNBTIngredient.of(pendingBook.copy()), 10000, 10000, 0)));
        if (!deliver(output, Component.literal("Survey complete — report output blocked"))) return;
        if (successful && counts.values().stream().mapToLong(Long::longValue).sum() >= ACTIVATION_COUNT) {
            progression = State.ACTIVE;
        }
        finishRun();
    }

    private void finishRun() {
        machine.afterWorking();
        boolean suspend = suspendAfterFinish;
        clearRun();
        super.resetRecipeLogic();
        // An abort reset drops polling; completion must recheck already queued inputs.
        markLastRecipeDirty();
        if (suspend) setStatus(Status.SUSPEND);
        onChanged();
    }

    private WorldEaterScanDomain region(ServerLevel level) {
        return new WorldEaterScanDomain(controller.getPos().getX() >> 4, controller.getPos().getZ() >> 4,
                level.getMinBuildHeight(), level.getMaxBuildHeight());
    }

    /** Essence production destroys the Safety hatch, or for Bravery the machine nearest a random point. */
    @Nullable
    private MetaMachine pickVictim() {
        if (Op.of(lastRecipe) != Op.REAL) return null;
        if (controller.getIntention() == Intention.SAFETY) {
            for (var part : controller.getParts()) if (part instanceof IntentionHatchPartMachine hatch
                    && hatch.getIntention() == Intention.SAFETY) return hatch;
            return null;
        }
        if (!(controller.getLevel() instanceof ServerLevel level)) return null;
        var region = region(level);
        var random = level.random;
        int span = WorldEaterScanDomain.WIDTH * 16;
        return findBraveryVictim(new BlockPos((region.centerX() - 4) * 16 + random.nextInt(span),
                region.minY() + random.nextInt(region.maxY() - region.minY()),
                (region.centerZ() - 4) * 16 + random.nextInt(span)));
    }

    @Nullable
    public MetaMachine findBraveryVictim(BlockPos origin) {
        if (!(controller.getLevel() instanceof ServerLevel level)) return null;
        var region = region(level);
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

    private void clearRun() {
        if (GTCEu.Mods.isAE2Loaded()) MEDemandHatchPartMachine.clear(controller);
        demand = ItemStack.EMPTY;
        craftingConsidered = false;
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
        if (progression != State.DORMANT) {
            return Component.literal("State: " + progression + " | " + (lastRecipe != null && duration > 0
                    ? "Craft: " + progress + "/" + duration + " | Parallels: " + lastRecipe.parallels : "Idle"));
        }
        return Component.literal("State: " + progression + (reportPending ? " | Report pending" :
                " | Scan: " + Math.max(0, progress - elapsedDelays) + "/" + SCAN_TICKS)
                + (retryPending ? " | Retry " + attempts + "/" + MAX_RETRIES + ", delay " + delayRemaining + " ticks" : ""));
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        CompoundTag data = new CompoundTag();
        data.putInt("Version", 1);
        data.putString("State", progression.name());
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
            saved.putBoolean("CraftingConsidered", craftingConsidered);
            data.put("Demand", saved);
        }
        tag.put("WorldEaterTamer", data);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);
        clearRun();
        progression = State.DORMANT;
        if (!tag.contains("WorldEaterTamer", Tag.TAG_COMPOUND)) return;
        CompoundTag data = tag.getCompound("WorldEaterTamer");
        try {
            if (data.getInt("Version") != 1) throw new IllegalArgumentException("Unknown format");
            progression = State.valueOf(data.getString("State"));
            if (data.contains("Demand") || hasDemandOperation(lastRecipe)) {
                try { loadDemand(data); }
                catch (IllegalArgumentException exception) {
                    clearRun();
                    super.resetRecipeLogic();
                }
                return;
            }
            loadScan(data);
        } catch (IllegalArgumentException | ArithmeticException exception) {
            progression = State.DORMANT;
            clearRun();
            super.resetRecipeLogic();
        }
    }

    private void loadDemand(CompoundTag data) {
        if (!data.contains("Demand", Tag.TAG_COMPOUND) || !isDemandRecipe(lastRecipe)
                || lastRecipe.recipeType != controller.getRecipeType() || !stateMatches(lastRecipe, Op.of(lastRecipe))
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
        // Even legacy or malformed automation metadata cannot reopen a paid run's opportunity.
        craftingConsidered = REAL_OPERATION.equals(saved.getString("Operation"));
    }

    private void loadScan(CompoundTag data) {
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
        if (reportPending && (domain == null || progression != State.DORMANT))
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
            if (elapsedDelays < 0 || progress < elapsedDelays || scanned > SCAN_TICKS || duration < progress
                    || duration != (long) SCAN_TICKS + elapsedDelays + delayRemaining
                    || attempts < 0 || attempts > MAX_RETRIES || delayRemaining < 0 || delayRemaining >= RETRY_DELAY
                    || (!retryPending && (attempts != 0 || delayRemaining != 0))
                    || (reportPending && !successful && (!retryPending || attempts != MAX_RETRIES || delayRemaining != 0))
                    || (retryPending && (attempts == 0 || missingX < domain.centerX() - 4 || missingX > domain.centerX() + 4
                    || missingZ < domain.centerZ() - 4 || missingZ > domain.centerZ() + 4))
                    || total > domain.boundary(scanned) || (!reportPending && !accepts(lastRecipe))
                    || (successful && (!reportPending || scanned != SCAN_TICKS)))
                throw new IllegalArgumentException("Inconsistent run");
        } else if (!counts.isEmpty() || elapsedDelays != 0 || delayRemaining != 0 || retryPending || attempts != 0 || successful) {
            throw new IllegalArgumentException("Orphan run data");
        }
        if (reportPending && (!pendingBook.is(Items.WRITTEN_BOOK) || pendingBook.getCount() != 1
                || !net.minecraft.world.item.WrittenBookItem.makeSureTagIsValid(pendingBook.getTag())))
            throw new IllegalArgumentException("Invalid report");
        updateCurrentChunk();
    }
}
