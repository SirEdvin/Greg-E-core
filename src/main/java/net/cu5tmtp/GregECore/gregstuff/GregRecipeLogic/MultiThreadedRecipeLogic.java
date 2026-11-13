package net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("all")
public class MultiThreadedRecipeLogic extends RecipeLogic {

    private int maxThreads;
    private final List<RecipeThread> activeThreads = new ArrayList<>();
    private int recipeSearchCooldown = 0;
    private boolean isMultiThreaded = false;

    public MultiThreadedRecipeLogic(IRecipeLogicMachine machine, int maxThreads) {
        super(machine);
        this.maxThreads = maxThreads;
    }

    public boolean isMultiThreaded() {
        return this.isMultiThreaded;
    }

    public void setMultiThreaded(boolean multiThreaded) {
        if (this.isMultiThreaded != multiThreaded) {
            this.isMultiThreaded = multiThreaded;
            if (!multiThreaded) {
                this.activeThreads.clear();
            } else {
                if (this.progress > 0) super.interruptRecipe();
            }
        }
    }

    public int getMaxThreads() {
        return this.maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public void interruptRecipe() {
        super.interruptRecipe();
        this.activeThreads.clear();
    }

    public List<RecipeThread> getActiveThreads() {
        return this.activeThreads;
    }

    @Override
    public void serverTick() {
        if (!isMultiThreaded) {
            super.serverTick();
            return;
        }

        if (isSuspend()) return;
        if (!isWorkingEnabled()) return;

        boolean isWorking = false;

        if (!activeThreads.isEmpty()) {
            Iterator<RecipeThread> iterator = activeThreads.iterator();

            boolean handledFirst = false;
            boolean powerSuccess = false;
            Component failReason = null;

            while (iterator.hasNext()) {
                RecipeThread thread = iterator.next();
                boolean thisThreadSuccess = false;

                if (!handledFirst) {
                    var tickHandle = handleTickRecipe(thread.recipe);
                    if (tickHandle.isSuccess()) {
                        if (!machine.onWorking()) {
                            break;
                        }
                        powerSuccess = true;
                        thisThreadSuccess = true;
                    } else {
                        powerSuccess = false;
                        failReason = tickHandle.reason();
                    }
                    handledFirst = true;
                } else {
                    thisThreadSuccess = powerSuccess;
                }

                if (thisThreadSuccess) {
                    thread.progress++;
                    isWorking = true;

                    if (thread.progress >= thread.duration) {
                        handleRecipeIO(thread.recipe, IO.OUT);
                        iterator.remove();
                        machine.afterWorking();
                    }
                } else {
                    if (failReason != null) {
                        setWaiting(failReason);
                    }
                }
            }
        }

        if (activeThreads.size() < maxThreads) {
            if (recipeSearchCooldown <= 0) {
                findAndStartNewRecipes();
            } else {
                recipeSearchCooldown--;
            }
        }

        if (!activeThreads.isEmpty()) {
            this.progress = activeThreads.get(0).progress;
            this.duration = activeThreads.get(0).duration;
        } else {
            this.progress = 0;
            this.duration = 0;
        }

        if (isWorking) {
            setStatus(Status.WORKING);
        } else if (activeThreads.isEmpty() && getStatus() != Status.SUSPEND) {
            setStatus(Status.IDLE);
        }
    }

    private void findAndStartNewRecipes() {
        Iterator<GTRecipe> matches = searchRecipe();
        boolean foundAny = false;

        while (matches.hasNext() && activeThreads.size() < maxThreads) {
            GTRecipe match = matches.next();
            if (match == null) continue;

            while (activeThreads.size() < maxThreads) {
                GTRecipe modified = machine.fullModifyRecipe(match.copy());

                if (modified == null) {
                    break;
                }

                var checkResult = checkRecipe(modified);

                if (checkResult.isSuccess()) {
                    if (!machine.beforeWorking(modified)) {
                        break;
                    }

                    var ioResult = handleRecipeIO(modified, IO.IN);

                    if (ioResult.isSuccess()) {
                        activeThreads.add(new RecipeThread(modified, modified.duration));
                        foundAny = true;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        if (!foundAny) {
            recipeSearchCooldown = 10;
        }
    }

    @Override
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);

        tag.putBoolean("IsMultiThreaded", isMultiThreaded);

        ListTag threadsTag = new ListTag();
        for (RecipeThread thread : activeThreads) {
            CompoundTag threadTag = new CompoundTag();
            threadTag.putString("RecipeId", thread.recipe.id.toString());
            threadTag.putInt("Progress", thread.progress);
            threadTag.putInt("Duration", thread.duration);
            threadsTag.add(threadTag);
        }
        tag.put("ActiveThreads", threadsTag);
        tag.putInt("SearchCooldown", recipeSearchCooldown);
    }

    @Override
    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        super.loadCustomPersistedData(tag);

        this.isMultiThreaded = tag.getBoolean("IsMultiThreaded");

        activeThreads.clear();
        if (tag.contains("ActiveThreads", Tag.TAG_LIST)) {
            ListTag threadsTag = tag.getList("ActiveThreads", Tag.TAG_COMPOUND);
            for (int i = 0; i < threadsTag.size(); i++) {
                CompoundTag threadTag = threadsTag.getCompound(i);
                ResourceLocation recipeId = new ResourceLocation(threadTag.getString("RecipeId"));

                var optRecipe = getRecipeManager().byKey(recipeId);
                if (optRecipe.isPresent() && optRecipe.get() instanceof GTRecipe gtRecipe) {
                    GTRecipe modified = machine.fullModifyRecipe(gtRecipe.copy());

                    if (modified != null) {
                        RecipeThread thread = new RecipeThread(modified, threadTag.getInt("Duration"));
                        thread.progress = threadTag.getInt("Progress");
                        activeThreads.add(thread);
                    }
                }
            }
        }
        this.recipeSearchCooldown = tag.getInt("SearchCooldown");
    }

    @Override
    public boolean hasCustomProgressLine() {
        return this.isMultiThreaded;
    }

    @Override
    public Component getCustomProgressLine() {
        if (!this.isMultiThreaded || activeThreads.isEmpty()) return null;

        return Component.literal("Multi-Threading Active: ")
                .append(Component.literal(activeThreads.size() + " / " + maxThreads).withStyle(ChatFormatting.LIGHT_PURPLE));
    }

    public static class RecipeThread {
        public final GTRecipe recipe;
        public int progress;
        public final int duration;

        public RecipeThread(GTRecipe recipe, int duration) {
            this.recipe = recipe;
            this.duration = duration;
            this.progress = 0;
        }
    }
}