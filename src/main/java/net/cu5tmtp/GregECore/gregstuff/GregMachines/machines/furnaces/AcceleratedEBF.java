package net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.furnaces;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.pattern.FactoryBlockPattern;
import com.gregtechceu.gtceu.api.pattern.Predicates;
import com.gregtechceu.gtceu.common.data.GTRecipeModifiers;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.parts.threadParts.ThreadT1PartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.MultiThreadedRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEModifiers;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.GregEPredicates;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.gregtechceu.gtceu.api.pattern.Predicates.blocks;
import static com.gregtechceu.gtceu.common.data.GTBlocks.CASING_INVAR_HEATPROOF;
import static net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore.REGISTRATE;

public class AcceleratedEBF extends WorkableElectricMultiblockMachine {

    public AcceleratedEBF(IMachineBlockEntity holder, Object... args) {
        super(holder, args);
    }

    private int coilTemp = 0;

    public boolean canBeThreaded = false;

    @Override
    public void onStructureFormed() {

        super.onStructureFormed();

        for (IMultiPart part : getParts()) {

            if (part instanceof ThreadT1PartMachine) {
                canBeThreaded = true;
                if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
                    logic.setMultiThreaded(canBeThreaded);
                }
            }
        }
        this.coilTemp = GregEPredicates.getMagicalCoilTemperature(getMultiblockState().getMatchContext());
    }

    @Override
    public void onStructureInvalid() {
        canBeThreaded = false;
        if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic) {
            logic.setMultiThreaded(false);
        }

        this.coilTemp = 0;
        super.onStructureInvalid();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new MultiThreadedRecipeLogic(this, 2);
    }

    public int getMaxTemp() {
        return this.coilTemp;
    }

    public static MachineDefinition ACCELERATEDEBF = REGISTRATE
            .multiblock("acceleratedebf", AcceleratedEBF::new)
            .rotationState(RotationState.NON_Y_AXIS)
            .recipeType(GTRecipeTypes.BLAST_RECIPES)
            .recipeModifiers(GregEModifiers::acceleratedEBFModifier, GTRecipeModifiers.OC_PERFECT)
            .appearanceBlock(CASING_INVAR_HEATPROOF)
            .pattern(definition -> {
                return FactoryBlockPattern.start()
                        .aisle("BBB", "DDD", "DDD", "BBB")
                        .aisle("BBB", "D D", "D D", "BCB")
                        .aisle("BAB", "DDD", "DDD", "BBB")
                        .where('A', Predicates.controller(blocks(definition.getBlock())))
                        .where('B', Predicates.blocks(CASING_INVAR_HEATPROOF.get())
                                .or(Predicates.abilities(PartAbility.MAINTENANCE).setExactLimit(1))
                                .or(Predicates.abilities(PartAbility.IMPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_FLUIDS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.IMPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.EXPORT_ITEMS).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(PartAbility.INPUT_ENERGY).setMaxGlobalLimited(2))
                                .or(Predicates.abilities(ThreadT1PartMachine.getPartAbility()).setMaxGlobalLimited(1).setPreviewCount(1)))
                        .where('C', Predicates.abilities(PartAbility.MUFFLER).setMaxGlobalLimited(1))
                        .where('D', GregEPredicates.tierOneMagicalCoils())
                        .where(' ', Predicates.any())

                        .build();
            })
            .workableCasingModel(GTCEu.id("block/casings/solid/machine_casing_heatproof"),
                                 GTCEu.id("block/multiblock/electric_blast_furnace"))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.aebf1").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.aebf2").withStyle(style -> style.withColor(0xFFD700)))
            .tooltips(Component.translatable("info.gregecore.dashline").withStyle(s -> s.withColor(0xff0000)))
            .tooltips(Component.translatable("info.gregecore.aebf3").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.dashline"))
            .tooltips(Component.translatable("info.gregecore.aebf4").withStyle(style -> style.withColor(0x90EE90)))
            .tooltips(Component.translatable("info.gregecore.threading1"))
            .tooltips()
            .register();

    @Override
    public void addDisplayText(@NotNull List<Component> textList) {
        super.addDisplayText(textList);

        if (isFormed()) {

            if (getRecipeLogic() instanceof MultiThreadedRecipeLogic logic && logic.isMultiThreaded()) {
                List<MultiThreadedRecipeLogic.RecipeThread> threads = logic.getActiveThreads();
                for (int i = 0; i < threads.size(); i++) {
                    var thread = threads.get(i);
                    int percent = thread.duration > 0 ? (int) (((float) thread.progress / thread.duration) * 100) : 0;
                    textList.add(Component.literal("  Thread " + (i + 1) + ": " + percent + "%").withStyle(ChatFormatting.LIGHT_PURPLE));
                }
            }

            textList.add(Component.translatable("info.gregecore.aebf5").append(coilTemp + "K").withStyle(ChatFormatting.AQUA));
            switch (coilTemp){
                case 1800 -> textList.add(Component.translatable("info.gregecore.aebf6" ).withStyle(ChatFormatting.GREEN));
                case 3600 -> textList.add(Component.translatable("info.gregecore.aebf7").withStyle(ChatFormatting.GREEN));
                case 5400 -> textList.add(Component.translatable("info.gregecore.aebf8").withStyle(ChatFormatting.GREEN));
                default -> textList.add(Component.translatable("info.gregecore.aebf9").withStyle(ChatFormatting.RED));
            }
        }
    }

    public static void init() {}
}
