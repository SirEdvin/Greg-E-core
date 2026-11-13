package net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff;

import com.gregtechceu.gtceu.api.pattern.TraceabilityPredicate;
import com.gregtechceu.gtceu.api.pattern.error.PatternStringError;
import com.gregtechceu.gtceu.api.pattern.util.PatternMatchContext;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.lowdragmc.lowdraglib.utils.BlockInfo;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.misc.PressureChamber;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Supplier;

public class GregEPredicates {

    private static final String MAGICAL_COIL_TYPE = "MagicalCoilType";

    private static final String PRESSURE_GLASS_TYPE = "PressureGlassType";

    private static final MagicalCoil MANASTEEL = new MagicalCoil(GreggyBlocks.MANASTEEL_COIL, 1800);
    private static final MagicalCoil TWILIGHT = new MagicalCoil(GreggyBlocks.TWILIGHT_COIL, 3600);
    private static final MagicalCoil DESH = new MagicalCoil(GreggyBlocks.DESH_COIL, 5400);
    private static final MagicalCoil MALACHITE = new MagicalCoil(GreggyBlocks.MALACHITE_COIL, 7400);
    private static final MagicalCoil FORGOTTEN = new MagicalCoil(GreggyBlocks.FORGOTTEN_COIL, 9300);
    private static final MagicalCoil SUPERELEMENT = new MagicalCoil(GreggyBlocks.SUPERELEMENT_COIL, 11000);

    private static final PressureGlass TEMPERED = new PressureGlass(GTBlocks.CASING_TEMPERED_GLASS, 30.0);
    private static final PressureGlass LAMINATED = new PressureGlass(GTBlocks.CASING_LAMINATED_GLASS, 60.0);
    private static final PressureGlass FUSION = new PressureGlass(GTBlocks.FUSION_GLASS, 100.0);

    public static TraceabilityPredicate tierOneMagicalCoils() {
        return magicalCoils(MANASTEEL, TWILIGHT, DESH);
    }

    public static TraceabilityPredicate tierTwoMagicalCoils() {
        return magicalCoils(MALACHITE, FORGOTTEN, SUPERELEMENT);
    }

    public static TraceabilityPredicate pressureGlass() {
        return pressureGlass(TEMPERED, LAMINATED, FUSION);
    }

    private static TraceabilityPredicate magicalCoils(MagicalCoil... coils) {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (MagicalCoil coil : coils) {
                if (blockState.is(coil.block().get())) {
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut(MAGICAL_COIL_TYPE, coil);
                    if (!currentCoil.equals(coil)) {
                        blockWorldState.setError(new PatternStringError("gtceu.multiblock.pattern.error.coils"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> Arrays.stream(coils)
                .sorted(Comparator.comparingInt(MagicalCoil::temperature))
                .map(coil -> BlockInfo.fromBlockState(coil.block().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.translatable("gtceu.multiblock.pattern.error.coils"));
    }

    private static TraceabilityPredicate pressureGlass(PressureGlass... coils) {
        return new TraceabilityPredicate(blockWorldState -> {
            var blockState = blockWorldState.getBlockState();
            for (PressureGlass glass : coils) {
                if (blockState.is(glass.block().get())) {
                    Object currentCoil = blockWorldState.getMatchContext().getOrPut(PRESSURE_GLASS_TYPE, glass);
                    if (!currentCoil.equals(glass)) {
                        blockWorldState.setError(new PatternStringError("gregecore.error.glass"));
                        return false;
                    }
                    return true;
                }
            }
            return false;
        }, () -> Arrays.stream(coils)
                .sorted(Comparator.comparingDouble(PressureGlass::maxPressure))
                .map(coil -> BlockInfo.fromBlockState(coil.block().get().defaultBlockState()))
                .toArray(BlockInfo[]::new))
                .addTooltips(Component.literal("All of the glass must be the same."));
    }

    public static int getMagicalCoilTemperature(PatternMatchContext context) {
        Object coil = context.get(MAGICAL_COIL_TYPE);
        return coil instanceof MagicalCoil magicalCoil ? magicalCoil.temperature() : 0;
    }

    public static double getMaxGlassPressure(PatternMatchContext context) {
        Object glass = context.get(PRESSURE_GLASS_TYPE);
        return glass instanceof PressureGlass pressureGlass ? pressureGlass.maxPressure() : 0;
    }

    private record MagicalCoil(Supplier<? extends Block> block, int temperature) {}

    private record PressureGlass(Supplier<? extends Block> block, double maxPressure) {}
}
