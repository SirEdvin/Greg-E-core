package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IRedstoneSignalMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.StarFeederPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class StarMaykr extends WorkableElectricMultiblockMachine implements IRedstoneSignalMachine {

    public StarMaykr(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Persisted
    private double weight = 1;
    private double recipeWeight = 0;

    double weightGain = 0;
    private final List<IItemHandler> cachedStarFeederHandler = new ArrayList<>();
    private TickableSubscription weightSubscription;

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            StarMaykr.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedStarFeederHandler.clear();

        for (IMultiPart part : getParts()) {

            if (!(part instanceof StarFeederPartMachine)) {
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.cachedStarFeederHandler::add);
            }
        }

        if (weightSubscription != null) {
            weightSubscription.unsubscribe();
            weightSubscription = null;
        }

        weightSubscription = this.subscribeServerTick(this::manageWeight);
    }

    @Override
    public void onStructureInvalid() {
        if (weightSubscription != null) {
            weightSubscription.unsubscribe();
            weightSubscription = null;
        }
        super.onStructureInvalid();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        assert recipe != null;
        recipeWeight = recipe.data.getDouble("weight");

        return super.beforeWorking(recipe);
    }

    @Override
    public void afterWorking() {
        weight -= recipeWeight;
        super.afterWorking();
    }

    private void manageWeight() {
        if (getOffsetTimer() % 5 != 0) return;

        if (getRecipeLogic().isActive()) {
            this.updateSignal();
            applyWorkingWeightLogic();
        } else {
            this.updateSignal();
            applyIdleWeightLogic();
        }
    }

    private void applyWorkingWeightLogic(){
        tryAddWeight();
        if(weight < 0 || weight > 500){
            explodeMachine();
        }
    }

    private void applyIdleWeightLogic(){
        tryAddWeight();
        if(weight < 0 || weight > 500){
            explodeMachine();
        }
    }

    private void tryAddWeight() {
        for (IItemHandler handler : this.cachedStarFeederHandler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);

                Item item = stack.getItem();

                if (item == ModItems.BRASS_PELLET.get()) weightGain = 0.1;
                else if (item == ModItems.AMERICIUM_PELLET.get()) weightGain = 3.0;
                else if (item == ModItems.NEUTRONIUM_PELLET.get()) weightGain = 10.0;

                if (weightGain > 0) {
                    ItemStack extracted = handler.extractItem(i, 1, false);
                    if (!extracted.isEmpty()) {
                        this.weight += weightGain;
                        return;
                    }
                }

                weightGain = 0;
            }
        }
    }

    private void explodeMachine() {
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            var pos = getPos();
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15.0F, Level.ExplosionInteraction.BLOCK);
        }
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        int signal = (int) (weight / 30);
        return Math.min(15, signal);
    }

    @Override
    public int getAnalogOutputSignal() {
        return getOutputSignal(null);
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return true;
    }

    public static MachineDefinition STAR_MAYKR = REGISTRATE
            .multiblock("starmaykr", StarMaykr::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GregERecipeTypes.STAR_MAYKR_SINGULARITIES)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(GCYMBlocks.CASING_ATOMIC)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("  CCCCCCC  ", " CCDDDDDCC ", "CCDFFFFFDCC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CCDFFFFFDCC", " CCDDDDDCC ", "  CCCCCCC  ")
                        .aisle(" CCDDDDDCC ", "CEE     EEC", "CE       EC", "D         D", "D         D", "D         D", "D         D", "D         D", "CE       EC", "CEE     EEC", " CCDDDDDCC ")
                        .aisle("CCDFFFFFDCC", "CE       EC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CE       EC", "CCDFFFFFDCC")
                        .aisle("CDFFFFFFFDC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CDFFFFFFFDC")
                        .aisle("CDFFFFFFFDC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CDFFFFFFFDC")
                        .aisle("CDFFFFFFFDC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CDFFFFFFFDC")
                        .aisle("CDFFFFFFFDC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CDFFFFFFFDC")
                        .aisle("CDFFFFFFFDC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CDFFFFFFFDC")
                        .aisle("CCDFFFFFDCC", "CE       EC", "D         D", "F         F", "F         F", "F         F", "F         F", "F         F", "D         D", "CE       EC", "CCDFFFFFDCC")
                        .aisle(" CCDDDDDCC ", "CEE     EEC", "CE       EC", "D         D", "D         D", "D         D", "D         D", "D         D", "CE       EC", "CEE     EEC", " CCDDDDDCC ")
                        .aisle("  BBBABBB  ", " CCDDDDDCC ", "CCDFFFFFDCC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CDFFFFFFFDC", "CCDFFFFFDCC", " CCDDDDDCC ", "  CCCCCCC  ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(GCYMBlocks.CASING_ATOMIC.get())
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(StarFeederPartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('C', Predicates.blocks(GCYMBlocks.CASING_ATOMIC.get()))
                        .where('D', Predicates.blocks(GCYMBlocks.CASING_VIBRATION_SAFE.get()))
                        .where('E', Predicates.blocks(GCYMBlocks.CASING_INDUSTRIAL_STEAM.get()))
                        .where('F', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("connectedglass:clear_glass"))))
                        .where(' ', Predicates.any())
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/atomic_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createStarMaykrRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.starmaykr1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.starmaykr2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.starmaykr3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.starmaykr4").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.starmaykr5").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.starmaykr6").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.starmaykr7").withStyle(ChatFormatting.GOLD)
                    .append(Component.translatable("info.gregecore.starmaykr8").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.starmaykr9").withStyle(style -> style.withColor(0x90EE90)))
                    .append(Component.translatable("info.gregecore.starmaykr10").withStyle(ChatFormatting.RED))
                    .append(Component.translatable("info.gregecore.starmaykr11").withStyle(style -> style.withColor(0x90EE90))))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.starmaykr12").append((int) weight + " x 10³⁰").withStyle(ChatFormatting.AQUA));
            textList.add(Component.translatable("info.gregecore.starmaykr13").append(getOutputSignal(null) + "").withStyle(ChatFormatting.RED));
        }
    }

    public static void init() {
    }
}
