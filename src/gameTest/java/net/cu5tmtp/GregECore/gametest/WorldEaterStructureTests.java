package net.cu5tmtp.GregECore.gametest;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import net.cu5tmtp.GregECore.block.GreggyBlocks;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.part.ItemBusPartMachine;
import net.cu5tmtp.GregECore.gregstuff.GregMachines.machines.endgame.IncompleteWorldEaterTamer;
import net.cu5tmtp.GregECore.gregstuff.GregRecipeLogic.WorldEaterTamerRecipeLogic;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.GregECore;
import net.cu5tmtp.GregECore.gregstuff.GregUtils.notCoreStuff.ModCreativeModTabs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(GregECore.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldEaterStructureTests {
    static final BlockPos CONTROLLER = new BlockPos(3, 3, 1);

    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing) {
        return shell(helper, facing, CONTROLLER);
    }

    static IncompleteWorldEaterTamer shell(GameTestHelper helper, Direction facing, BlockPos relativeCenter) {
        BlockPos center = helper.absolutePos(relativeCenter);
        Direction right = facing.getClockWise();
        for (int back = 0; back < 5; back++) {
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos = center.relative(facing.getOpposite(), back).relative(right, x).above(y);
                    int boundaries = (back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0);
                    boolean edge = boundaries >= 2;
                    helper.getLevel().setBlockAndUpdate(pos, edge ? GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState() : Blocks.STONE.defaultBlockState());
                }
            }
        }
        helper.getLevel().setBlockAndUpdate(center, IncompleteWorldEaterTamer.INCOMPLETE_WORLD_EATER_TAMER.defaultBlockState());
        var controller = (IncompleteWorldEaterTamer) MetaMachine.getMachine(helper.getLevel(), center);
        controller.setFrontFacing(facing);
        controller.getRecipeLogic().setWorkingEnabled(false);
        return controller;
    }

    static List<BlockPos> shellPositions(IncompleteWorldEaterTamer controller) {
        List<BlockPos> positions = new ArrayList<>();
        Direction facing = controller.getFrontFacing(), right = facing.getClockWise();
        for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) {
            if (back == 0 && x == 0 && y == 0) continue;
            if ((back == 0 || back == 4 ? 1 : 0) + (Math.abs(x) == 2 ? 1 : 0) + (y == 0 || y == 4 ? 1 : 0) >= 2)
                positions.add(controller.getPos().relative(facing.getOpposite(), back).relative(right, x).above(y));
        }
        return positions;
    }

    static ItemBusPartMachine bus(GameTestHelper helper, BlockPos pos, MachineDefinition definition) {
        helper.getLevel().setBlockAndUpdate(pos, definition.defaultBlockState());
        return (ItemBusPartMachine) MetaMachine.getMachine(helper.getLevel(), pos);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void structureLimits(GameTestHelper helper) {
        var machine = shell(helper, Direction.NORTH);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPattern(), "Edge-only frame forms with arbitrary face panels and interior");
            var positions = shellPositions(machine);
            helper.assertTrue(positions.size() == 43, "Exactly 44 frame blocks including the edge-centered controller");
            for (int back = 0; back < 5; back++) for (int x = -2; x <= 2; x++) for (int y = 0; y < 5; y++) {
                var pos = machine.getPos().south(back).east(x).above(y);
                if (!pos.equals(machine.getPos()) && !positions.contains(pos))
                    helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            helper.assertTrue(machine.checkPattern(), "Open faces and empty interior form");
            for (var pos : positions) {
                helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Every frame edge and corner is required: " + pos);
                helper.getLevel().setBlockAndUpdate(pos, GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            }
            MachineDefinition[] types = {GTMachines.ITEM_IMPORT_BUS[GTValues.LV], GTMachines.ITEM_EXPORT_BUS[GTValues.LV],
                    GTMachines.FLUID_IMPORT_HATCH[GTValues.LV], GTMachines.FLUID_EXPORT_HATCH[GTValues.LV]};
            for (int type = 0; type < types.length; type++) for (int count = 0; count < 5; count++)
                helper.getLevel().setBlockAndUpdate(positions.get(type * 5 + count), types[type].defaultBlockState());
            helper.assertTrue(machine.checkPattern(), "Five of EACH part type form");
            for (var type : types) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), type.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Reject sixth " + type.getId());
                helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GTMachines.ENERGY_INPUT_HATCH[GTValues.LV].defaultBlockState());
            helper.assertTrue(!machine.checkPattern(), "Reject energy hatch");
            for (var rejected : new net.minecraft.world.level.block.Block[]{GTBlocks.MACHINE_CASING_UHV.get(),
                    GreggyBlocks.WORLD_EATER_TAMER_CASING_PURPLE.get(), GreggyBlocks.WORLD_EATER_TAMER_CASING_GREEN.get(),
                    GreggyBlocks.WORLD_EATER_TAMER_CASING_ORANGE.get()}) {
                helper.getLevel().setBlockAndUpdate(positions.get(20), rejected.defaultBlockState());
                helper.assertTrue(!machine.checkPattern(), "Reject non-black frame casing: " + rejected);
            }
            helper.getLevel().setBlockAndUpdate(positions.get(20), GreggyBlocks.WORLD_EATER_TAMER_CASING.getDefaultState());
            helper.assertTrue(machine.checkPattern(), "Restored shell forms");
            machine.onStructureFormed();
            var logic = (WorldEaterTamerRecipeLogic) machine.getRecipeLogic();
            helper.assertTrue(!logic.isCountableInterface(machine), "Exclude self");
            for (var part : machine.getParts()) helper.assertTrue(!logic.isCountableInterface(part.self()), "Exclude attached part");
            machine.onStructureInvalid();
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void rotatedStructureAndCreativeTab(GameTestHelper helper) {
        var machine = shell(helper, Direction.EAST);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(machine.checkPattern(), "Rotated shell forms");
            helper.assertTrue(machine.getStructureCenter().equals(net.minecraft.world.phys.Vec3.atCenterOf(machine.getPos().west(2).above(2))), "Rotated geometric center is two blocks behind and above the controller");
            var tab = ModCreativeModTabs.GREGE_TAB.get();
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
            helper.assertTrue(tab.getDisplayItems().stream().anyMatch(stack -> stack.is(machine.getDefinition().getItem())), "Creative registration");
            helper.succeed();
        });
    }
}
