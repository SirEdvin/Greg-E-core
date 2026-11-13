package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.misc.BloodStoragePartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.renderer.renderRegistries.GregERenederRegistries;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.cu5tmtp.GregECore.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.common.data.models.GTMachineModels.createWorkableCasingMachineModel;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class BloodCathedral extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            BloodCathedral.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private final List<IItemHandler> bloodStorageHandler = new ArrayList<>();

    private TickableSubscription bloodSubscription;
    @DescSynced
    @Persisted
    public int blood = 0;
    private int bloodGain;
    private int bloodCost;

    public BloodCathedral(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();
        this.bloodStorageHandler.clear();

        for (IMultiPart part : getParts()) {

            if (!(part instanceof BloodStoragePartMachine)) {
                continue;
            }

            var handlerLists = part.getRecipeHandlers();
            for (var handlerList : handlerLists) {
                handlerList.getCapability(ItemRecipeCapability.CAP).stream()
                        .filter(IItemHandler.class::isInstance)
                        .map(IItemHandler.class::cast)
                        .forEach(this.bloodStorageHandler::add);
            }
        }

        if (bloodSubscription != null) {
            bloodSubscription.unsubscribe();
            bloodSubscription = null;
        }

        bloodSubscription = this.subscribeServerTick(this::manageBlood);
    }

    @Override
    public void onStructureInvalid() {
        if (bloodSubscription != null) {
            bloodSubscription.unsubscribe();
            bloodSubscription = null;
        }
        super.onStructureInvalid();
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        assert recipe != null;
        bloodCost = recipe.data.getInt("bloodcost");
        if (bloodCost > blood) {
            return false;
        } else {
            blood -= bloodCost;
        }
        return super.beforeWorking(recipe);
    }

    private void manageBlood(){
        if (getOffsetTimer() % 20 != 0) return;

        for (IItemHandler handler : this.bloodStorageHandler) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack stack = handler.getStackInSlot(i);

                Item item = stack.getItem();

                if (item == ModItems.TINYBLOOD.get()) bloodGain = 1;
                else if (item == ModItems.MEDIUMBLOOD.get()) bloodGain = 5;
                else if (item == ModItems.LARGEBLOOD.get()) bloodGain = 25;

                if (bloodGain > 0) {
                    ItemStack extracted = handler.extractItem(i, 1, false);
                    if (!extracted.isEmpty()) {
                        this.blood += bloodGain;
                        return;
                    }
                }
            }
        }
    }

    public static MachineDefinition BLODDCATHEDRAL = REGISTRATE
            .multiblock("bloodcathedral", BloodCathedral::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeTypes(GregERecipeTypes.BLOODCATHEDRALCRAFT)
            .appearanceBlock(GCYMBlocks.CASING_ATOMIC)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("bccbaaaaaaaaabccb", "accaaaaaaaaaaacca", "acdaaaaaaaaaaacca", "accaaaaaaaaaaadca", "addaaaaaaaaaaacea", "acdaaaaaaaaaaacca", "accaaaaaaaaaaacda", "acdaaaaaaaaaaaeca", "acdaaaaaaaaaaacda", "adcaaaaaaaaaaacea", "adcaaaaaaaaaaacca", "accaaaaaaaaaaacda", "bccbaaaaaaaaabccb", "affaaaaaaaaaaaffa", "affaaaaaaaaaaaffa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("bccbaaaaaaaaabccb", "adcaaaaaaaaaaadca", "adcaaaaaaaaaaadea", "accaaaaaaaaaaacca", "accdaaaaaaaaadcda", "accdaaaaaaaaadcca", "aecaaaaaaaaaaadca", "aecaaaaaaaaaaacca", "accaaaaaaaaaaacda", "aceaaaaaaaaaaaeca", "aecaaaaaaaaaaacea", "aecaaaaaaaaaaacca", "bccbaaaabaaaabccb", "affaaaaabaaaaaffa", "affaaaaafaaaaaffa", "aaaaaaaabaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aadaaaaaaaaaaadaa", "aaddaaaaaaaaaddaa", "aaagdaaaaaaadgaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaababaaaaaaa", "abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaddaaaaaaaddaaa", "aaaagdaaaaadgaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaafafaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaafafaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaddaaaaaddaaaa", "aaaaagdbbbdgaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaddefeddaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaabeefeebaaaaa", "aaaafaaaaaaafaaaa", "aaabaaaaaaaaabaaa", "aaabaaaaaaaaabaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaabaaaaaaaaabaaa", "aaabaaaaaaaaabaaa", "aaaafaaaaaaafaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaafaaaaaaaa", "aaaaaaaafaaaaaaaa", "aaaaaaabfbaaaaaaa", "aaaaaabafabaaaaaa", "aaaaaabafabaaaaaa", "aaaaabfffffbaaaaa", "aaaaaaaahaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aabaaaaaaaaaaabaa", "aabaaaaaaaaaaabaa", "aafaaaaaaaaaaafaa", "aabaaaaaaaaaaabaa", "aabaaaaaaaaaaabaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaabeefeebaaaaa", "aaaafaaaaaaafaaaa", "aaabaaaaaaaaabaaa", "aaabaaaaaaaaabaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaabaaaaaaaaabaaa", "aaabaaaaaaaaabaaa", "aaaafaaaaaaafaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaddefeddaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaddaaaaaddaaaa", "aaaaagdbibdgaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaddaaaaaaaddaaa", "aaaagdaaaaadgaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaafafaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaafafaaaaaaa")
                        .aisle("abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aadaaaaaaaaaaadaa", "aaddaaaaaaaaaddaa", "aaagdaaaaaaadgaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaababaaaaaaa", "abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaababaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("bccbaaaaaaaaabccb", "accaaaaaaaaaaacda", "aecaaaaaaaaaaacda", "accaaaaaaaaaaacca", "aecdaaaaaaaaadcca", "aecdaaaaaaaaadcea", "accaaaaaaaaaaacca", "acdaaaaaaaaaaaeca", "acdaaaaaaaaaaacda", "adcaaaaaaaaaaaeca", "aecaaaaaaaaaaaeca", "accaaaaaaaaaaacca", "bccbaaaabaaaabccb", "affaaaaabaaaaaffa", "affaaaaafaaaaaffa", "aaaaaaaabaaaaaaaa", "aaaaaaaabaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("bccbaaaaaaaaabccb", "adeaaaaaaaaaaaeca", "aceaaaaaaaaaaaeca", "accaaaaaaaaaaacea", "adcaaaaaaaaaaacda", "adcaaaaaaaaaaaeda", "accaaaaaaaaaaaeca", "aedaaaaaaaaaaacca", "aceaaaaaaaaaaacda", "adcaaaaaaaaaaacca", "aceaaaaaaaaaaacca", "aecaaaaaaaaaaaeca", "bccbaaaaaaaaabccb", "affaaaaaaaaaaaffa", "affaaaaaaaaaaaffa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")
                        .aisle("abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "abbaaaaaaaaaaabba", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaa")

                        .where("a", Predicates.any())
                        .where("b", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:chiseled_polished_blackstone")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(BloodStoragePartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:blackstone"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:polished_blackstone"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:polished_blackstone_bricks"))))
                        .where("f", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:redstone_block"))))
                        .where("g", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("minecraft:glowstone"))))
                        .where("h", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("bloodmagic:altar"))))
                        .where("i", Predicates.controller(Predicates.blocks(definition.get())))
                        .build();
            })
            .model(createWorkableCasingMachineModel(
                    ResourceLocation.parse("minecraft:block/chiseled_polished_blackstone"),
                    GTCEu.id("gtceu:block/multiblock/distillation_tower"))
                    .andThen(b -> b.addDynamicRenderer(GregERenederRegistries::createCathedralRender))
            )
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.cathedral1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.cathedral2").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.cathedral3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.cathedral4").withStyle(ChatFormatting.YELLOW))
            .tooltips(Component.translatable("info.gregecore.cathedral5").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.cathedral6").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.cathedral7").withStyle(style -> style.withColor(0xBF40BF)))
            .tooltips(Component.translatable("info.gregecore.cathedral8").withStyle(ChatFormatting.DARK_GRAY))
            .register();

    @Override
    public void addDisplayText(List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {
            textList.add(Component.literal("Blood avaible: " + blood + "mb").withStyle(ChatFormatting.DARK_RED));
        }
    }

    public int getBlood() {
        return blood;
    }

    public static void init() {
    }
}