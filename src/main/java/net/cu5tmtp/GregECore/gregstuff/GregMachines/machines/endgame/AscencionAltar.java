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
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.endgame.AscencionPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class AscencionAltar extends WorkableElectricMultiblockMachine implements IRedstoneSignalMachine {

    public AscencionAltar(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private final List<IItemHandler> cachedAscencionHandler = new ArrayList<>();
    public boolean[] areTomesIn = new boolean[5];
    String[] tomeNames = {"Forbidden Tome Of Extraterrestrial Planets", "Forbidden Tome Of Mighty Beings", "Forbidden Tome Of Hidden Lifeforms", "Forbidden Tome Of Rare Blocks", "Forbidden Tome Of Rare Items"};

    int neededBook;
    private int animationTick = 0;

    private TickableSubscription checkingSubscription;

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.cachedAscencionHandler.clear();

        for (IMultiPart part : getParts()) {

            if (!(part instanceof AscencionPartMachine)) {
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.cachedAscencionHandler::add);
            }
        }

        if (checkingSubscription != null) {
            checkingSubscription.unsubscribe();
            checkingSubscription = null;
        }
        checkingSubscription = this.subscribeServerTick(this::checkWhichTomesAreIn);
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {

        assert recipe != null;
        // -1 because if there is 0 in addData in kubejs, recipe crashes
        neededBook = recipe.data.getInt("tome") - 1;

        if (neededBook >= 0 && neededBook < areTomesIn.length) {
            if (!areTomesIn[neededBook]) {
                return false;
            }
        }
        return super.beforeWorking(recipe);
    }

    @Override
    public boolean onWorking() {

        if (neededBook >= 0 && neededBook < areTomesIn.length) {
            if (!areTomesIn[neededBook]) {
                return false;
            }
        }

        animationTick++;

        return super.onWorking();
    }

    @Override
    public void onStructureInvalid() {
        animationTick = 0;
        if (checkingSubscription != null) {
            checkingSubscription.unsubscribe();
            checkingSubscription = null;
        }
        super.onStructureInvalid();
    }

    private void checkWhichTomesAreIn() {

        if (getOffsetTimer() % 10 == 0){
            return;
        }

        Arrays.fill(areTomesIn, false);

        for (IItemHandler handler : this.cachedAscencionHandler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);
                Item item = stack.getItem();

                if (item == ModItems.TOME1.get()) areTomesIn[0] = true;
                else if (item == ModItems.TOME2.get()) areTomesIn[1] = true;
                else if (item == ModItems.TOME3.get()) areTomesIn[2] = true;
                else if (item == ModItems.TOME4.get()) areTomesIn[3] = true;
                else if (item == ModItems.TOME5.get()) areTomesIn[4] = true;
            }
        }
    }

    public static MachineDefinition ASCENCION_ALTAR = REGISTRATE
            .multiblock("ascencionaltar", AscencionAltar::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GregERecipeTypes.ASCENCION_ALTAR_DONATION)
            .recipeModifiers(GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(GCYMBlocks.CASING_ATOMIC)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaacacaaa", "aaacaiaaa", "aaaiaaaaa")
                        .aisle("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaacafaaa", "acaaaaaaa", "aiaaaaaaa", "aaaaaaaaa")
                        .aisle("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaacacaaa", "aeacacaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aacccaaaa", "acccdcaaa", "acaaaaaaa", "aaaaaacaa", "aaaaaaiaa", "aaaaaaaaa")
                        .aisle("aaacgaaaa", "aaaccaaaa", "aaaccaaaa", "aaaecaaaa", "acccccaaa", "adcccccaa", "aaaaaacaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaccccaaa", "aaeaacaaa", "aacaacaaa", "acccccaaa", "accccccaa", "aaaaacfaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaccccaaa", "aacaacaaa", "aacaaeaaa", "acccccaaa", "aajhjccaa", "aaaaaacaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaccccaaa", "aacaacaaa", "aafaacaaa", "acccccaaa", "aaaaaccca", "aaaaaaccc", "aaaaaaaai", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaaccaaaa", "aaacfaaaa", "aaaccaaaa", "aagccaaaa", "aaaaacgaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .aisle("aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa", "aaaaaaaaa")
                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:air"))))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:blankrune"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:dislocationrune"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:sacrificerune"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:bettercapacityrune"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:altarcapacityrune"))))
                        .where('h', Predicates.controller(blocks(definition.getBlock())))
                        .where('i', Predicates.abilities(AscencionPartMachine.getPartAbility()).setMaxGlobalLimited(5).setPreviewCount(5))
                        .where("j", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:sacrificerune")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    GregECore.id("block/blankrune"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createAscencionAltarRender)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.ascencionaltar1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.ascencionaltar2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.ascencionaltar3").withStyle(style -> style.withColor(0x90EE90)))
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {

        for (int i = 0; i < areTomesIn.length; i++) {
            ChatFormatting color = areTomesIn[i] ? ChatFormatting.GREEN : ChatFormatting.RED;
            String status = areTomesIn[i] ? " is present." : " is missing.";

            textList.add(Component.literal(tomeNames[i] + status).withStyle(color));
        }

        super.addDisplayText(textList);
    }

    public static void init() {
    }
}
