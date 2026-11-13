package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.PedestalPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.essentiaParts.*;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static java.util.Arrays.fill;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class InfusionAltar extends WorkableElectricMultiblockMachine {


    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            InfusionAltar.class, WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER
    );

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
    protected final List<IItemHandler> cachedPedestalHandler = new ArrayList<>();
    @DescSynced
    public final List<ItemStack> itemsForRenderer = new ArrayList<>();
    @DescSynced
    public ItemStack outputItem = ItemStack.EMPTY;
    @DescSynced
    public final boolean[] essentiaToRender = new boolean[6];

    public InfusionAltar(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        fill(this.essentiaToRender, false);

        assert recipe != null;

        String str = recipe.data.getString("essentia");

        if (!str.isEmpty()) {
            String[] parts = str.split(",");
            for (int i = 0; i < Math.min(parts.length, 6); i++) {
                try {
                    int value = Integer.parseInt(parts[i].trim());
                    this.essentiaToRender[i] = value > 0;
                } catch (NumberFormatException e) {
                    this.essentiaToRender[i] = false;
                }
            }
        }

        List<ItemStack> currentInputItems = new ArrayList<>();

        for (IItemHandler handler : cachedPedestalHandler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stackInSlot = handler.getStackInSlot(i);

                if (!stackInSlot.isEmpty()) {
                    ItemStack renderStack = stackInSlot.copy();
                    renderStack.setCount(1);
                    currentInputItems.add(renderStack);
                }
            }
        }

        boolean changed = currentInputItems.size() != this.itemsForRenderer.size();
        if (!changed) {
            for (int i = 0; i < currentInputItems.size(); i++) {
                if (!ItemStack.isSameItemSameTags(currentInputItems.get(i), this.itemsForRenderer.get(i))) {
                    changed = true;
                    break;
                }
            }
        }

        if (changed) {
            this.itemsForRenderer.clear();
            this.itemsForRenderer.addAll(currentInputItems);
        }

        outputItem = ItemStack.EMPTY;

        List<Content> itemOutputs = recipe.outputs.get(ItemRecipeCapability.CAP);

        if (itemOutputs != null && !itemOutputs.isEmpty()) {
            Object contentObj = itemOutputs.get(0).getContent();

            if (contentObj instanceof SizedIngredient sizedIngredient) {
                ItemStack[] items = sizedIngredient.getItems();
                if (items.length > 0) {
                    outputItem = items[0].copy();
                    outputItem.setCount(1);
                }
            }
            else if (contentObj instanceof ItemStack itemStack) {
                outputItem = itemStack.copy();
            }
        }

        return super.beforeWorking(recipe);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedPedestalHandler.clear();

        for (IMultiPart part : getParts()) {
            if (!(part instanceof PedestalPartMachine)) {
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.cachedPedestalHandler::add);
            }
        }
    }

    @Override
    public void onStructureInvalid() {
        this.cachedPedestalHandler.clear();
        this.itemsForRenderer.clear();
        super.onStructureInvalid();
    }

    @Override
    public void afterWorking() {
        this.itemsForRenderer.clear();
        super.afterWorking();
    }

    public static MachineDefinition INFUSION_ALTAR = REGISTRATE
            .multiblock("infusion_altar", InfusionAltar::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GregERecipeTypes.INFUSION_ALTAR_INFUSING)
            .appearanceBlock(GTBlocks.CASING_STEEL_SOLID)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aabbbbbaa", "aaaaaaaaa")
                        .aisle("abbbbbbba", "aaadddaaa")
                        .aisle("bbbbbbbbb", "aaaaaaaaa")
                        .aisle("ebbbbbbbi", "adaaaaada")
                        .aisle("gbbbbbbbj", "adaaaaada")
                        .aisle("hbbbbbbbk", "adaaaaada")
                        .aisle("bbbbbbbbb", "aaaaaaaaa")
                        .aisle("abbbbbbba", "aaadddaaa")
                        .aisle("aabfcfbaa", "aaaaaaaaa")
                        .where("a", Predicates.any())
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing"))))
                        .where("d", Predicates.abilities(PedestalPartMachine.getPartAbility()))
                        .where("e", Predicates.abilities(AerInputPartMachine.getPartAbility()))
                        .where('c', Predicates.controller(blocks(definition.getBlock())))
                        .where('f', Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:solid_machine_casing")))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where("g", Predicates.abilities(TerraInputPartMachine.getPartAbility()))
                        .where("h", Predicates.abilities(AquaInputPartMachine.getPartAbility()))
                        .where("i", Predicates.abilities(OrdoInputPartMachine.getPartAbility()))
                        .where("j", Predicates.abilities(PerditioInputPartMachine.getPartAbility()))
                        .where("k", Predicates.abilities(IgnisInputPartMachine.getPartAbility()))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GTCEu.id("block/casings/solid/machine_casing_solid_steel"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createInfusionAltarRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.infusionaltar1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.infusionaltar2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.infusionaltar3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.infusionaltar4").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.infusionaltar5").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);
    }

    public static void init() {
    }
}
