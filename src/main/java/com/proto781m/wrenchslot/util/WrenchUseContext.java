package com.proto781m.wrenchslot.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link UseOnContext} that carries an arbitrary item stack (the wrench from
 * the Curios slot) instead of the stack held in the player's hand.
 */
public final class WrenchUseContext extends UseOnContext {

    public WrenchUseContext(Level level, @Nullable Player player, InteractionHand hand,
                            ItemStack stack, BlockHitResult hitResult) {
        super(level, player, hand, stack, hitResult);
    }
}
