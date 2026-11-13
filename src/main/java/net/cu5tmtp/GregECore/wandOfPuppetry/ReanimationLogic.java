package net.cu5tmtp.GregECore.wandOfPuppetry;

import net.cu5tmtp.GregECore.entity.ModEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ReanimationLogic {
    public static InteractionResult tryReanimate(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();

        if (!level.isClientSide() && !state.isAir() && state.getBlock() != Blocks.BEDROCK) {

            AnimatedBlockEntity animatedBlock = new AnimatedBlockEntity(ModEntity.ANIMATED_BLOCK.get(), level);
            animatedBlock.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            animatedBlock.setBlockState(state);

            level.addFreshEntity(animatedBlock);

            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());

            if (player != null && !player.isCreative()) {
                context.getItemInHand().hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(context.getHand()));
            }

            return InteractionResult.SUCCESS;
        }

        return level.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }
}
