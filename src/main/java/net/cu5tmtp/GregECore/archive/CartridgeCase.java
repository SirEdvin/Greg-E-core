/*
package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.common.data.GCYMBlocks;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT3PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogicCartridge;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.recipe.GregERecipeTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class CartridgeCase extends WorkableElectricMultiblockMachine {

    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            CartridgeCase.class,
            WorkableElectricMultiblockMachine.MANAGED_FIELD_HOLDER);

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    public static final GTRecipeType[] ALL_POSSIBLE_RECIPES = new GTRecipeType[]{
            GTRecipeTypes.EXTRUDER_RECIPES, GTRecipeTypes.BENDER_RECIPES, GTRecipeTypes.COMPRESSOR_RECIPES,
            GTRecipeTypes.FORGE_HAMMER_RECIPES, GTRecipeTypes.FORMING_PRESS_RECIPES, GTRecipeTypes.WIREMILL_RECIPES,
            GTRecipeTypes.ASSEMBLER_RECIPES, GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES,
            GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES, GTRecipeTypes.ORE_WASHER_RECIPES, GTRecipeTypes.CHEMICAL_BATH_RECIPES,
            GTRecipeTypes.EXTRACTOR_RECIPES, GTRecipeTypes.CANNER_RECIPES, GTRecipeTypes.AUTOCLAVE_RECIPES,
            GTRecipeTypes.SIFTER_RECIPES, GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES, GTRecipeTypes.CENTRIFUGE_RECIPES,
            GTRecipeTypes.POLARIZER_RECIPES, GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES, GTRecipeTypes.ELECTROLYZER_RECIPES,
            GTRecipeTypes.MACERATOR_RECIPES, GTRecipeTypes.MIXER_RECIPES, GTRecipeTypes.LASER_ENGRAVER_RECIPES,
            GTRecipeTypes.CUTTER_RECIPES, GTRecipeTypes.LATHE_RECIPES, GTRecipeTypes.ARC_FURNACE_RECIPES,
            GTRecipeTypes.BREWING_RECIPES, GTRecipeTypes.FERMENTING_RECIPES, GTRecipeTypes.FLUID_HEATER_RECIPES
    };

    public boolean canBeThreaded = false;
    private TickableSubscription logicSubscription;
    private Set<GTRecipeType> allowedRecipeTypes = new LinkedHashSet<>(Set.of(GregERecipeTypes.CARTRIDGECASENEEDSTOBEEMPTY));

    private GTRecipeType currentActiveType = GregERecipeTypes.CARTRIDGECASENEEDSTOBEEMPTY;

    private BlockPos[] cachedCheckPositions;

    @DescSynced
    @Persisted
    public int disabledRecipeTypesMask = 0;

    @DescSynced
    @Persisted
    public int uiTypeIndex = 0;

    @DescSynced
    @Persisted
    public int activeThreadsCount = 0;

    public CartridgeCase(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    public Set<GTRecipeType> getAllowedRecipeTypes() {
        return this.allowedRecipeTypes;
    }

    @Override
    public GTRecipeType getRecipeType() {
        return this.currentActiveType;
    }

    public void setAutoMachineMode(GTRecipeType type) {
        this.currentActiveType = type;
    }

    public void cycleUIType() {
        Set<GTRecipeType> allCartridgeTypes = getCurrentlyInsertedTypes();
        if (allCartridgeTypes.isEmpty()) return;

        List<GTRecipeType> list = new ArrayList<>(allCartridgeTypes);

        GTRecipeType current = null;
        if (uiTypeIndex >= 0 && uiTypeIndex < ALL_POSSIBLE_RECIPES.length) {
            current = ALL_POSSIBLE_RECIPES[uiTypeIndex];
        }

        int currentListIdx = list.indexOf(current);
        int nextIdx = (currentListIdx + 1) % list.size();

        uiTypeIndex = getRecipeTypeIndex(list.get(nextIdx));
    }

    public void toggleCurrentUIType() {
        if (uiTypeIndex < 0 || uiTypeIndex >= ALL_POSSIBLE_RECIPES.length) return;

        disabledRecipeTypesMask ^= (1 << uiTypeIndex);

        updateAllowedRecipeTypes();
    }

    private BlockPos[] getCheckPositions() {
        if (this.cachedCheckPositions == null) {
            BlockPos center = getPos();
            Direction left = getFrontFacing().getClockWise();
            Direction right = left.getOpposite();

            this.cachedCheckPositions = new BlockPos[]{
                    center.relative(left, 4),
                    center.relative(right, 4),
                    center.above(4),
                    center.below(4),
                    center.relative(left, 4).above(4),
                    center.relative(right, 4).above(4),
                    center.relative(left, 4).below(4),
                    center.relative(right, 4).below(4)
            };
        }
        return this.cachedCheckPositions;
    }

    public Map<GTRecipeType, Integer> getRecipeTypeCounts() {
        Map<GTRecipeType, Integer> counts = new HashMap<>();
        if (getLevel() == null) return counts;

        for (BlockPos checkPos : getCheckPositions()) {
            if (getLevel().getBlockEntity(checkPos) instanceof IMachineBlockEntity mbe) {
                if (mbe.getMetaMachine() instanceof BoxMachines cartridge) {
                    if (cartridge.isFormed()) {
                        Set<GTRecipeType> cartridgeTypes = new LinkedHashSet<>();
                        addRecipeTypesForCartridge(cartridge.getDefinition(), cartridgeTypes);

                        for (GTRecipeType type : cartridgeTypes) {
                            counts.put(type, counts.getOrDefault(type, 0) + 1);
                        }
                    }
                }
            }
        }
        return counts;
    }

    private Set<GTRecipeType> getCurrentlyInsertedTypes() {
        return getRecipeTypeCounts().keySet();
    }


    private void updateAllowedRecipeTypes() {
        Set<GTRecipeType> allCartridgeTypes = getCurrentlyInsertedTypes();
        Set<GTRecipeType> newTypes = new LinkedHashSet<>();

        for (GTRecipeType type : allCartridgeTypes) {
            int idx = getRecipeTypeIndex(type);
            if ((disabledRecipeTypesMask & (1 << idx)) == 0) {
                newTypes.add(type);
            }
        }

        if (newTypes.isEmpty()) {
            newTypes.add(GregERecipeTypes.CARTRIDGECASENEEDSTOBEEMPTY);
        }

        if (!newTypes.equals(this.allowedRecipeTypes)) {

            this.allowedRecipeTypes = newTypes;

            if (!this.allowedRecipeTypes.contains(this.currentActiveType)) {
                this.currentActiveType = this.allowedRecipeTypes.iterator().next();
            }

            if (getRecipeLogic() != null) {
                getRecipeLogic().markLastRecipeDirty();
                GTRecipe currentRecipe = getRecipeLogic().getLastRecipe();

                if (getRecipeLogic().isWorking() && currentRecipe != null) {
                    if (!this.allowedRecipeTypes.contains(currentRecipe.getType())) {
                        getRecipeLogic().setProgress(0);
                    }
                }
            }
        }
    }

    private void addRecipeTypesForCartridge(MachineDefinition def, Set<GTRecipeType> types) {
        switch (def.getName()) {
            case "machine_one":
                types.add(GTRecipeTypes.EXTRUDER_RECIPES);
                types.add(GTRecipeTypes.BENDER_RECIPES);
                types.add(GTRecipeTypes.COMPRESSOR_RECIPES);
                types.add(GTRecipeTypes.FORGE_HAMMER_RECIPES);
                types.add(GTRecipeTypes.FORMING_PRESS_RECIPES);
                types.add(GTRecipeTypes.WIREMILL_RECIPES);
                break;
            case "machine_two":
                types.add(GTRecipeTypes.ASSEMBLER_RECIPES);
                types.add(GTRecipeTypes.CIRCUIT_ASSEMBLER_RECIPES);
                break;
            case "machine_three":
                types.add(GTRecipeTypes.FLUID_SOLIDFICATION_RECIPES);
                types.add(GTRecipeTypes.ORE_WASHER_RECIPES);
                types.add(GTRecipeTypes.CHEMICAL_BATH_RECIPES);
                types.add(GTRecipeTypes.EXTRACTOR_RECIPES);
                types.add(GTRecipeTypes.CANNER_RECIPES);
                types.add(GTRecipeTypes.AUTOCLAVE_RECIPES);
                break;
            case "machine_four":
                types.add(GTRecipeTypes.SIFTER_RECIPES);
                types.add(GTRecipeTypes.THERMAL_CENTRIFUGE_RECIPES);
                types.add(GTRecipeTypes.CENTRIFUGE_RECIPES);
                break;
            case "machine_five":
                types.add(GTRecipeTypes.POLARIZER_RECIPES);
                types.add(GTRecipeTypes.ELECTROMAGNETIC_SEPARATOR_RECIPES);
                types.add(GTRecipeTypes.ELECTROLYZER_RECIPES);
                break;
            case "machine_six":
                types.add(GTRecipeTypes.MACERATOR_RECIPES);
                break;
            case "machine_seven":
                types.add(GTRecipeTypes.MIXER_RECIPES);
                break;
            case "machine_eight":
                types.add(GTRecipeTypes.LASER_ENGRAVER_RECIPES);
                break;
            case "machine_nine":
                types.add(GTRecipeTypes.CUTTER_RECIPES);
                types.add(GTRecipeTypes.LATHE_RECIPES);
                break;
            case "machine_ten":
                types.add(GTRecipeTypes.ARC_FURNACE_RECIPES);
                break;
            case "machine_eleven":
                types.add(GTRecipeTypes.BREWING_RECIPES);
                types.add(GTRecipeTypes.FERMENTING_RECIPES);
                types.add(GTRecipeTypes.FLUID_HEATER_RECIPES);
                break;
        }
    }

    public static ModifierFunction recipeModifier(MetaMachine machine, @NotNull GTRecipe recipe) {
        if (machine instanceof CartridgeCase cartridgeCase) {
            if (!cartridgeCase.allowedRecipeTypes.contains(recipe.getType())) {
                return r -> null;
            }

            Map<GTRecipeType, Integer> typeCounts = cartridgeCase.getRecipeTypeCounts();
            int cartridgeCount = typeCounts.getOrDefault(recipe.getType(), 0);

            if (cartridgeCount > 0) {
                int parallels = cartridgeCount * 256;

                double speedBoost = Math.max(0.1, 1.0 - (cartridgeCount * 0.1));

                return ModifierFunction.builder()
                        .modifyAllContents(ContentModifier.multiplier(parallels))
                        .durationMultiplier(speedBoost)
                        .parallels(parallels)
                        .eutMultiplier(0.5)
                        .build();
            }
        }
        return r -> null;
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogicCartridge(this, 8);
    }

    @Override
    public void onStructureFormed() {
        super.onStructureFormed();

        boolean hasThreadPart = false;

        for (IMultiPart part : getParts()) {

            if (part instanceof ThreadT3PartMachine) {
                hasThreadPart = true;
                break;
            }
        }

        this.canBeThreaded = hasThreadPart;

        if (getRecipeLogic() instanceof MultiThreadedRecipeLogicCartridge logic) {
            logic.setMultiThreaded(this.canBeThreaded);
        }

        updateAllowedRecipeTypes();

        if (this.logicSubscription == null) {
            this.logicSubscription = this.subscribeServerTick(this::manageLogic);
        }
    }

    @Override
    public void onStructureInvalid() {

        this.canBeThreaded = false;

        this.cachedCheckPositions = null;

        if (getRecipeLogic() instanceof MultiThreadedRecipeLogicCartridge logic) {
            logic.setMultiThreaded(false);
        }

        this.allowedRecipeTypes = new LinkedHashSet<>(Set.of(GregERecipeTypes.CARTRIDGECASENEEDSTOBEEMPTY));
        this.currentActiveType = GregERecipeTypes.CARTRIDGECASENEEDSTOBEEMPTY;

        if (this.logicSubscription != null) {
            this.logicSubscription.unsubscribe();
            this.logicSubscription = null;
        }
        super.onStructureInvalid();
    }

    private void manageLogic() {
        if (isFormed && getOffsetTimer() % 100 == 0) {
            updateAllowedRecipeTypes();
        }
    }

    public int getRecipeTypeIndex(GTRecipeType type) {
        for (int i = 0; i < ALL_POSSIBLE_RECIPES.length; i++) {
            if (ALL_POSSIBLE_RECIPES[i] == type) return i;
        }
        return -1;
    }

    @Override
    public Widget createUIWidget() {
        WidgetGroup group = (WidgetGroup) super.createUIWidget();

        WidgetGroup sidePanel = new WidgetGroup(-120, 85, 115, 70);
        sidePanel.setBackground(ResourceBorderTexture.BORDERED_BACKGROUND);

        LabelWidget titleLabel = new LabelWidget(5, 5, Component.literal("Recipe Type:").withStyle(ChatFormatting.GOLD));

        LabelWidget typeLabel = new LabelWidget(5, 18, "") {
            @Override
            public void updateScreen() {
                super.updateScreen();
                Set<GTRecipeType> insertedTypes = getCurrentlyInsertedTypes();
                if (!insertedTypes.isEmpty()) {
                    GTRecipeType currentType = ALL_POSSIBLE_RECIPES[Math.min(Math.max(0, uiTypeIndex), ALL_POSSIBLE_RECIPES.length - 1)];
                    if (!insertedTypes.contains(currentType)) {
                        uiTypeIndex = getRecipeTypeIndex(insertedTypes.iterator().next());
                        currentType = ALL_POSSIBLE_RECIPES[uiTypeIndex];
                    }

                    String cleanName = currentType.toString().replace("gtceu:", "").replace("_recipes", "").replace("_", " ").toUpperCase();
                    this.setComponent(Component.literal(cleanName).withStyle(ChatFormatting.AQUA));
                } else {
                    this.setComponent(Component.literal("NONE").withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        };

        LabelWidget statusLabel = new LabelWidget(5, 31, "") {
            @Override
            public void updateScreen() {
                super.updateScreen();
                Set<GTRecipeType> insertedTypes = getCurrentlyInsertedTypes();
                if (!insertedTypes.isEmpty()) {
                    boolean isDisabled = (disabledRecipeTypesMask & (1 << uiTypeIndex)) != 0;
                    if (isDisabled) {
                        this.setComponent(Component.literal("Status: ").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("DISABLED").withStyle(ChatFormatting.RED)));
                    } else {
                        this.setComponent(Component.literal("Status: ").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal("ENABLED").withStyle(ChatFormatting.GREEN)));
                    }
                } else {
                    this.setComponent(Component.literal(""));
                }
            }
        };

        ButtonWidget cycleBtn = new ButtonWidget(5, 45, 50, 20,
                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Cycle")),
                clickData -> cycleUIType());

        ButtonWidget toggleBtn = new ButtonWidget(60, 45, 50, 20,
                new GuiTextureGroup(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Toggle")),
                clickData -> toggleCurrentUIType());

        sidePanel.addWidget(titleLabel);
        sidePanel.addWidget(typeLabel);
        sidePanel.addWidget(statusLabel);
        sidePanel.addWidget(cycleBtn);
        sidePanel.addWidget(toggleBtn);

        group.addWidget(sidePanel);

        return group;
    }

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        textList.removeIf(component -> component.getString().contains("Active Machine Mode"));

        if (isFormed()) {
            textList.add(Component.literal("Active Cartridges:").withStyle(ChatFormatting.AQUA));
            boolean foundAny = false;

            if (getLevel() != null) {
                Map<GTRecipeType, Integer> counts = getRecipeTypeCounts();
                Set<String> displayedCartridges = new HashSet<>();

                for (BlockPos checkPos : getCheckPositions()) {
                    if (getLevel().getBlockEntity(checkPos) instanceof IMachineBlockEntity mbe) {
                        if (mbe.getMetaMachine() instanceof BoxMachines cartridge) {
                            if (cartridge.isFormed()) {
                                String cartName = cartridge.getDefinition().getName();

                                if(!displayedCartridges.contains(cartName)) {
                                    int count = 0;
                                    Set<GTRecipeType> typesForThisCartridge = new HashSet<>();
                                    addRecipeTypesForCartridge(cartridge.getDefinition(), typesForThisCartridge);

                                    if(!typesForThisCartridge.isEmpty()) {
                                        count = counts.getOrDefault(typesForThisCartridge.iterator().next(), 1);
                                    }

                                    textList.add(Component.literal(" - ")
                                            .append(Component.literal(count + "x ").withStyle(ChatFormatting.YELLOW))
                                            .append(Component.translatable("block.gregecore." + cartName))
                                            .withStyle(ChatFormatting.GRAY));
                                    displayedCartridges.add(cartName);
                                    foundAny = true;
                                }
                            }
                        }
                    }
                }
            }

            if (!foundAny) {
                textList.add(Component.literal(" - None").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    public static MachineDefinition CARTRIDGECASE = REGISTRATE.multiblock("cartridgecase", CartridgeCase::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeModifiers(GTRecipeModifiers.PARALLEL_HATCH, GTRecipeModifiers.OC_PERFECT, CartridgeCase::recipeModifier)
            .recipeTypes(ALL_POSSIBLE_RECIPES)
            .appearanceBlock(GCYMBlocks.CASING_ATOMIC)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("aaaaaaaaaaaaa", "acccacccaccca", "acccacccaccca", "acccacccaccca", "aaaaaaaaaaaaa", "acccacccaccca", "acccacccaccca", "acccacccaccca", "aaaaaaaaaaaaa", "acccacccaccca", "acccacccaccca", "acccacccaccca", "aaaaaaaaaaaaa")
                        .aisle("acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca", "cbbbcdddcbbbc", "cbbbcdddcbbbc", "cbbbcdddcbbbc", "acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca")
                        .aisle("acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca", "cbbbcdddcbbbc", "cbbbcdedcbbbc", "cbbbcdddcbbbc", "acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca")
                        .aisle("acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca", "cbbbcdddcbbbc", "cbbbcdfdcbbbc", "cbbbcdddcbbbc", "acccacccaccca", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "cbbbcbbbcbbbc", "acccacccaccca")
                        .aisle("aaaaaaaaaaaaa", "abbbabbbabbba", "abbbgbbbgbbba", "abbbabbbabbba", "aagaaaaaaagaa", "abbbabbbabbba", "abbbabbbabbba", "abbbabbbabbba", "aagaaaaaaagaa", "abbbabbbabbba", "abbbgbbbgbbba", "abbbabbbabbba", "aaaaaaaaaaaaa")

                        .where("a", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:large_scale_assembler_casing")))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(16).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(16).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2).setPreviewCount(1))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2).setPreviewCount(2))
                                .or(Predicates.abilities(ThreadT3PartMachine.getPartAbility()).setMinGlobalLimited(1).setPreviewCount(1)))
                        .where("b", Predicates.any())
                        .where("c", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:sturdy_machine_casing"))))
                        .where("d", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("gtceu:atomic_casing"))))
                        .where("e", Predicates.blocks(ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse("ae2:controller"))))
                        .where("f", Predicates.controller(blocks(definition.getBlock())))
                        .where("g", Predicates.blocks(GreggyBlocks.ASSEMBLY_ENGINE_INTAKE.get()))
                        .build();
            })
            .workableCasingModel(
                    GTCEu.id("block/casings/gcym/large_scale_assembling_casing"),
                    GTCEu.id("block/multiblock/fusion_reactor"))
            .tooltips(Component.literal("----------------------------------------").withStyle(s -> s.withColor(0xff0000)))
            .tooltips(Component.literal("Abilities: Cartridges, Perfect Overclock and Threading").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.literal("----------------------------------------").withStyle(s -> s.withColor(0xff0000)))
            .tooltips(Component.literal("Highly configurable machine, you define what can get crafted!").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.literal("----------------------------------------").withStyle(s -> s.withColor(0xff0000)))
            .tooltips(Component.literal("This multiblock cannot do any crafting on its own, you have to add Cartridges to it. Each cartridge gives the machine different recipe type.").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.literal("Each Cartridge adds 10% reduction to recipe time and 256 parallels to the recipe type, which it is focused on.").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.literal("Do not change the recipe type with the default GregTech change. Instead, use the custom GUI to disable recipe types, which you don't want.").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.literal("Needs Threading Core T3.").withStyle(ChatFormatting.LIGHT_PURPLE))
            .register();

    public static void init() {

    }
}
*/