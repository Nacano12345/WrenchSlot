package com.proto781m.wrenchslot.logic;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.network.WrenchActionPayload;
import com.proto781m.wrenchslot.util.WrenchPickup;
import com.proto781m.wrenchslot.util.WrenchUseContext;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Validates and executes wrench actions requested by the client.
 */
public final class WrenchActions {

    private WrenchActions() {
    }

    public static void perform(ServerPlayer player, WrenchActionPayload payload) {
        if (!Config.enabled() || !player.mayBuild() || player.isSpectator()) {
            return;
        }

        Level level = player.level();
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || !player.canInteractWithBlock(pos, 1.0D)) {
            return;
        }

        BlockState state = level.getBlockState(pos);
        boolean wrenchBlock = state.getBlock() instanceof IWrenchable
                || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
        if (!wrenchBlock) {
            return;
        }

        ItemStack wrench = CurioWrench.find(player).orElse(ItemStack.EMPTY);
        if (wrench.isEmpty()) {
            return;
        }

        BlockHitResult hitResult = new BlockHitResult(payload.hit(), payload.face(), pos, payload.inside());
        UseOnContext context = new WrenchUseContext(level, player, InteractionHand.MAIN_HAND,
                wrench, hitResult);

        InteractionResult result;
        if (state.getBlock() instanceof IWrenchable wrenchable) {
            result = payload.rotate()
                    ? wrenchable.onWrenched(state, context)
                    : wrenchable.onSneakWrenched(state, context);
        } else {
            result = payload.rotate() ? InteractionResult.PASS : WrenchPickup.pickup(context);
        }

        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }
    }
}
