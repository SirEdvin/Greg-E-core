package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.ComponentPanelWidget;
import net.minecraft.world.entity.player.Player;
import java.util.List;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregERecipeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class IncompleteWorldEaterTamer extends WorkableMultiblockMachine implements IFancyUIMachine, IDisplayUIMachine {
    public static final MachineDefinition INCOMPLETE_WORLD_EATER_TAMER = REGISTRATE
            .multiblock("incomplete_world_eater_tamer", IncompleteWorldEaterTamer::new)
            .langValue("Incomplete World Eater tamer")
            .rotationState(RotationState.NON_Y_AXIS)
            .appearanceBlock(GTBlocks.MACHINE_CASING_UHV)
            .recipeTypes(GregERecipeTypes.WORLD_EATER_TAMER)
            .pattern(definition -> FactoryBlockPattern.start(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.BACK)
                    .aisle("CCSCC", "C###C", "C###C", "C###C", "CCCCC")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("C###C", "#####", "#####", "#####", "C###C")
                    .aisle("CCCCC", "C###C", "C###C", "C###C", "CCCCC")
                    .where('S', Predicates.controller(Predicates.blocks(definition.get())))
                    .where('#', Predicates.any())
                    .where('C', Predicates.blocks(GTBlocks.MACHINE_CASING_UHV.get())
                            .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(5))
                            .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(5))
                            .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(5))
                            .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(5)))
                    .build())
            .sidedWorkableCasingModel(GTCEu.id("block/casings/voltage/uhv"), GTCEu.id("block/multiblock/research_station"))
            .tooltips(Component.literal("Collects infrastructure information without power."),
                    Component.literal("Scans the surrounding 9×9 chunks over 120 seconds."),
                    Component.literal("A complete survey of at least 500 machines awakens the tamer."))
            .register();

    public IncompleteWorldEaterTamer(IMachineBlockEntity holder, Object... args) { super(holder, args); }

    public Vec3 getStructureCenter() {
        return Vec3.atCenterOf(getPos().relative(getFrontFacing().getOpposite(), 2).above(2));
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) { return new WorldEaterTamerRecipeLogic(this); }

    @Override
    public void setVoidingMode(VoidingMode mode) { super.setVoidingMode(VoidingMode.VOID_NONE); }

    @Override
    public VoidingMode getVoidingMode() { return VoidingMode.VOID_NONE; }

    @Override
    public void addDisplayText(List<Component> text) {
        IDisplayUIMachine.super.addDisplayText(text);
        text.add(getRecipeLogic().getCustomProgressLine());
        text.add(((WorldEaterTamerRecipeLogic) getRecipeLogic()).getCurrentChunkLine());
    }

    @Override
    public Widget createUIWidget() {
        return new WidgetGroup(0, 0, 190, 125)
                .addWidget(new DraggableScrollableWidgetGroup(4, 4, 182, 117).setBackground(getScreenTexture())
                        .addWidget(new ComponentPanelWidget(4, 5, this::addDisplayText)
                                .textSupplier(getLevel().isClientSide ? null : this::addDisplayText)
                                .setMaxWidthLimit(174).clickHandler(this::handleDisplayClick)))
                .setBackground(GuiTextures.BACKGROUND_INVERSE);
    }

    @Override
    public ModularUI createUI(Player player) {
        return new ModularUI(198, 208, this, player).widget(new FancyMachineUIWidget(this, 198, 208));
    }

    public static void init() {}
}
