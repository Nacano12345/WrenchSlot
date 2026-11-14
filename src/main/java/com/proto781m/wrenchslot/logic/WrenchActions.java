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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates and executes wrench actions requested by the client.
 */
public final class WrenchActions {

    /** The vanilla use packet follows the action packet immediately; 1s is plenty. */
    private static final long PENDING_TTL_NANOS = 1_000_000_000L;

    private record Pending(BlockPos pos, long requestedAt) {
    }

    private static final Map<UUID, Pending> PENDING = new ConcurrentHashMap<>();

    private WrenchActions() {
    }

    /**
     * Called from the network thread as soon as the action packet arrives, so
     * the server-side vanilla interaction can be cancelled even if the queued
     * action task runs after it.
     */
    public static void markPending(ServerPlayer player, BlockPos pos) {
        PENDING.put(player.getUUID(), new Pending(pos, System.nanoTime()));
    }

    public static boolean hasPending(Player player, BlockPos pos) {
        Pending pending = PENDING.get(player.getUUID());
        return pending != null
                && pending.pos().equals(pos)
                && System.nanoTime() - pending.requestedAt() <= PENDING_TTL_NANOS;
    }

    public static void clearPending(Player player) {
        PENDING.remove(player.getUUID());
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

        // This mod's primary modifier is handled client-side (only Ctrl-click
        // sends the packet). Create's original primary modifier, sneaking, is
        // the secondary modifier here.
        boolean dismantle = player.isShiftKeyDown();

        BlockHitResult hitResult = new BlockHitResult(payload.hit(), payload.face(), pos, payload.inside());
        UseOnContext context = new WrenchUseContext(level, player, InteractionHand.MAIN_HAND,
                wrench, hitResult);

        InteractionResult result;
        if (state.getBlock() instanceof IWrenchable wrenchable) {
            result = dismantle
                    ? wrenchable.onSneakWrenched(state, context)
                    : wrenchable.onWrenched(state, context);
        } else {
            result = dismantle ? WrenchPickup.pickup(context) : InteractionResult.PASS;
        }

        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND, true);
        }
    }
}
