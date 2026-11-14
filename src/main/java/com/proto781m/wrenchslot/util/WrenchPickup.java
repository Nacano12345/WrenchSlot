package com.proto781m.wrenchslot.util;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.Create;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Re-implementation of Create's wrench pickup behaviour for blocks that are in
 * the {@code create:wrench_pickup} tag but do not implement {@code IWrenchable}.
 */
public final class WrenchPickup {

    private WrenchPickup() {
    }

    public static InteractionResult pickup(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        if (player != null && !player.isCreative()) {
            Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos), player,
                            context.getItemInHand())
                    .forEach(stack -> player.getInventory().placeItemBackInInventory(stack));
        }

        state.spawnAfterBreak(serverLevel, pos, ItemStack.EMPTY, true);
        level.destroyBlock(pos, false);
        AllSoundEvents.WRENCH_REMOVE.playOnServer(level, pos, 1,
                Create.RANDOM.nextFloat() * 0.5F + 0.5F);
        return InteractionResult.SUCCESS;
    }
}
