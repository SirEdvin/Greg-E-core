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
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.RepairPartsInputPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class FornaxUniversi extends WorkableElectricMultiblockMachine implements IRedstoneSignalMachine {

    public FornaxUniversi(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private TickableSubscription logicSubscription;

    private int repairTimer = 0;
    private int recipeTicks = 0;
    private boolean needsRepair = false;
    private boolean hasTriggeredFailureThisRecipe = false;
    private static final int REPAIR_TIME_LIMIT = 100;
    private int failureTriggerTick;
    private static ItemStack[] requiredItems;
    private int whichItem;
    private final Random random = new Random();

    private final List<IItemHandler> cachedRepairPartHandler = new ArrayList<>();

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedRepairPartHandler.clear();

        for (IMultiPart part : getParts()) {

            if (!(part instanceof RepairPartsInputPartMachine)) {
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.cachedRepairPartHandler::add);
            }
        }

        if (logicSubscription != null) {
            logicSubscription.unsubscribe();
            logicSubscription = null;
        }
        logicSubscription = this.subscribeServerTick(this::manageLogic);

        whichItem = 0;

        getRepairItems();
    }

    private void manageLogic() {

        if (this.needsRepair) {

            this.updateSignal();

            if (this.repairTimer > 0) {
                this.repairTimer--;
                if (consumeRepairItem()) {
                    this.needsRepair = false;
                    this.repairTimer = 0;
                    this.recipeLogic.setStatus(RecipeLogic.Status.WORKING);
                }
            } else {
                this.explodeMachine();
            }
            return;
        }

        if (this.recipeLogic.isWorking()) {

            this.updateSignal();

            this.recipeTicks++;

            if (!this.hasTriggeredFailureThisRecipe && this.recipeTicks >= failureTriggerTick) {
                this.needsRepair = true;
                this.hasTriggeredFailureThisRecipe = true;
                this.repairTimer = REPAIR_TIME_LIMIT;
                this.recipeLogic.setStatus(RecipeLogic.Status.SUSPEND);
            }
        }
    }

    @Override
    public void afterWorking() {
        this.updateSignal();
        this.recipeTicks = 0;
        this.hasTriggeredFailureThisRecipe = false;
        getRandomRepairItem();
        super.afterWorking();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        assert recipe != null;
        this.failureTriggerTick = recipe.duration / 2;

        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureInvalid() {
        if (logicSubscription != null) {
            logicSubscription.unsubscribe();
            logicSubscription = null;
        }
        super.onStructureInvalid();
    }

    private boolean consumeRepairItem() {
        for (IItemHandler handler : cachedRepairPartHandler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stackInSlot = handler.getStackInSlot(i);
                if (ItemStack.isSameItem(stackInSlot, requiredItems[whichItem]) && stackInSlot.getCount() >= 1) {
                    handler.extractItem(i, 1, false);
                    return true;
                }
            }
        }
        return false;
    }

    private void explodeMachine() {
        Level level = getLevel();
        if (level != null && !level.isClientSide) {
            var pos = getPos();
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15.0F, Level.ExplosionInteraction.BLOCK);
        }
    }

    private void getRandomRepairItem(){
        whichItem = random.nextInt(3);
    }

    private void getRepairItems() {
        if (requiredItems == null) {
            requiredItems = new ItemStack[] {
                    new ItemStack(ModItems.SERVER_RACK.get()),
                    new ItemStack(ModItems.ROCKET_CONE.get()),
                    new ItemStack(ModItems.QUANTUM_ACCELERATOR.get())
            };
        }
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if(getRecipeLogic().isSuspend()) {
            switch (whichItem) {
                case 0 -> {
                    return 5;
                }
                case 1 -> {
                    return 10;
                }
                case 2 -> {
                    return 15;
                }
            }
        }
        return 0;
    }

    @Override
    public int getAnalogOutputSignal() {
        return getOutputSignal(null);
    }

    @Override
    public boolean canConnectRedstone(Direction side) {
        return true;
    }

    public static MachineDefinition FORNAX_UNIVERSI = REGISTRATE
            .multiblock("fornaxuniversi", FornaxUniversi::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GregERecipeTypes.FORNAX_UNIVERSI_ACCELERETION)
            .appearanceBlock(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "C           C", "E           E", "C           C", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                        .aisle("             ", "             ", "             ", "             ", "             ", "             ", " C         C ", " E         E ", "             ", "             ", "             ", " E         E ", " C         C ", "             ", "             ", "             ", "             ", "             ", "             ")
                        .aisle("  DDD   DDD  ", "             ", "             ", "             ", "  C       C  ", "  E       E  ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "  E       E  ", "  C       C  ", "             ", "             ", "             ", "  DDD   DDD  ")
                        .aisle("  DDDBBBDDD  ", "   C     C   ", "   C     C   ", "   E     E   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "   E     E   ", "   C     C   ", "   C     C   ", "  DDDCCCDDD  ")
                        .aisle("  DDDBBBDDD  ", "     C C     ", "     C C     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     C C     ", "     C C     ", "  DDDCCCDDD  ")
                        .aisle("   BBBBBBB   ", "    C   C    ", "    C   C    ", "     C C     ", "     C C     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     C C     ", "     C C     ", "    C   C    ", "    C   C    ", "   CCCCCCC   ")
                        .aisle("   BBBBBBB   ", "      C      ", "      G      ", "      G      ", "      C      ", "      C      ", "      F      ", "             ", "             ", "             ", "             ", "             ", "      F      ", "      C      ", "      C      ", "      G      ", "      G      ", "      C      ", "   CCCCCCC   ")
                        .aisle("   BBBBBBB   ", "    C   C    ", "    C   C    ", "     C C     ", "     C C     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     C C     ", "     C C     ", "    C   C    ", "    C   C    ", "   CCCCCCC   ")
                        .aisle("  DDDBBBDDD  ", "     C C     ", "     C C     ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "     C C     ", "     C C     ", "  DDDCCCDDD  ")
                        .aisle("  DDDBABDDD  ", "   C     C   ", "   C     C   ", "   E     E   ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "   E     E   ", "   C     C   ", "   C     C   ", "  DDDCCCDDD  ")
                        .aisle("  DDD   DDD  ", "             ", "             ", "             ", "  C       C  ", "  E       E  ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "  E       E  ", "  C       C  ", "             ", "             ", "             ", "  DDD   DDD  ")
                        .aisle("             ", "             ", "             ", "             ", "             ", "             ", " C         C ", " E         E ", "             ", "             ", "             ", " E         E ", " C         C ", "             ", "             ", "             ", "             ", "             ", "             ")
                        .aisle("             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "C           C", "E           E", "C           C", "             ", "             ", "             ", "             ", "             ", "             ", "             ", "             ")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get())
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(RepairPartsInputPartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('C', Predicates.blocks(GCYMBlocks.CASING_HIGH_TEMPERATURE_SMELTING.get()))
                        .where('D', Predicates.blocks(GCYMBlocks.CASING_STRESS_PROOF.get()))
                        .where('E', Predicates.blocks(GTBlocks.CASING_HSSE_STURDY.get()))
                        .where('F', Predicates.blocks(GTBlocks.CASING_POLYTETRAFLUOROETHYLENE_PIPE.get()))
                        .where('G', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:chain"))))
                        .where(' ', Predicates.any())
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/gcym/high_temperature_smelting_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createFornaxUniversiRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fornax1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fornax2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fornax3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.fornax4").withStyle(ChatFormatting.GOLD))
            .tooltips(Component.translatable("info.gregecore.fornax5").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.fornax6").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.fornax7").withStyle(ChatFormatting.LIGHT_PURPLE))
            .tooltips(Component.translatable("info.gregecore.fornax8").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
        
        if (isFormed()) {
            textList.add(Component.translatable("info.gregecore.fornax9").append(getOutputSignal(null) + "").withStyle(ChatFormatting.RED));
        }
    }

    public static void init() {
    }
}
