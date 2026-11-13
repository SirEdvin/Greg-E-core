package net.cu5tmtp.GregECore.wandOfPuppetry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static net.cu5tmtp.GregECore.wandOfPuppetry.ReanimationLogic.tryReanimate;

public class AVARITIAWandOfPuppetry extends Item {

    private final List<String> allowedBlocks = new ArrayList<>();

    public AVARITIAWandOfPuppetry(Properties properties) {
        super(properties);
        allowedBlocks.add("avaritia:infinity");
        allowedBlocks.add("gtceu:neutronium_block");
        allowedBlocks.add("gtceu:nebulon_alpha_block");
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {

        Block clickedBlock = context.getLevel().getBlockState(context.getClickedPos()).getBlock();
        String blockId = BuiltInRegistries.BLOCK.getKey(clickedBlock).toString();

        if(allowedBlocks.contains(blockId)) {
            return tryReanimate(context);
        }
        return InteractionResult.PASS;
    }
}
