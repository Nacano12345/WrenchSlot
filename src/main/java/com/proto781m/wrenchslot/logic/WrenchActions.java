package com.proto781m.wrenchslot.logic;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.network.WrenchActionPayload;
import com.proto781m.wrenchslot.util.WrenchPickup;
import com.proto781m.wrenchslot.util.WrenchUseContext;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
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
        WrenchSlot.LOGGER.debug("Wrench action request from {} at {}", player.getGameProfile().getName(),
                pos);
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

        boolean clientEmpty = payload.emptyHand();
        boolean serverEmpty = player.getMainHandItem().isEmpty();
        if (clientEmpty != serverEmpty) {
            // The client is showing/using an item the server does not know
            // about (or vice versa). Force a full inventory sync so ghost items
            // disappear and both sides agree on the selected hotbar slot.
            WrenchSlot.LOGGER.debug("Hand state mismatch (client empty={}, server empty={}); resyncing inventory",
                    clientEmpty, serverEmpty);
            resyncInventory(player);
        }

        if (!Config.allowNonEmptyHand() && !serverEmpty) {
            WrenchSlot.LOGGER.debug("Wrench action skipped: server main hand is not empty ({})",
                    player.getMainHandItem());
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

        // The client sends the modifier state with the action packet; the
        // server's own sneaking state can lag behind by a tick or more.
        boolean dismantle = payload.dismantle();

        BlockHitResult hitResult = new BlockHitResult(payload.hit(), payload.face(), pos, payload.inside());
        UseOnContext context = new WrenchUseContext(level, player, InteractionHand.MAIN_HAND,
                wrench, hitResult);

        InteractionResult result;
        boolean failed = false;
        try {
            if (state.getBlock() instanceof IWrenchable wrenchable) {
                result = dismantle
                        ? wrenchable.onSneakWrenched(state, context)
                        : wrenchable.onWrenched(state, context);
            } else {
                result = dismantle ? WrenchPickup.pickup(context) : InteractionResult.PASS;
            }
        } catch (RuntimeException ex) {
            WrenchSlot.LOGGER.error("Wrench action failed at {}", pos, ex);
            result = InteractionResult.FAIL;
            failed = true;
        }

        // Some Create blocks intentionally turn into another block when
        // dismantled (encased shafts, cart assemblers, ...). If the state did
        // not change at all, the dismantle failed half-way: items may already
        // have been added, so make sure the block is gone.
        BlockState after = level.getBlockState(pos);
        if (dismantle && (result.consumesAction() || failed) && after.equals(state)) {
            WrenchSlot.LOGGER.warn("Dismantle left the block unchanged at {} ({}); removing it",
                    pos, state);
            level.destroyBlock(pos, false);
            after = level.getBlockState(pos);
        }

        WrenchSlot.LOGGER.debug("Wrench {} at {}: result={}, state after={}",
                dismantle ? "dismantle" : "rotate", pos, result, after);

        if (result.consumesAction()) {
            player.swing(InteractionHand.MAIN_HAND, true);
            resyncBlock(player, level, pos);
            resyncInventory(player);
        }
    }

    /**
     * The wrench action can change the player's inventory (dismantle drops).
     * Send a full inventory state, and a second one next tick so client-side
     * predictions cannot leave a ghost item or a stale selected slot.
     */
    private static void resyncInventory(ServerPlayer player) {
        player.inventoryMenu.broadcastFullState();
        player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
        MinecraftServer server = player.server;
        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            if (!player.isRemoved()) {
                player.inventoryMenu.broadcastFullState();
                player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
            }
        }));
    }

    /**
     * The action runs outside the vanilla use packet's block-change sequence,
     * so explicitly resync the block. The delayed update lands after the
     * client's prediction acknowledgement and clears any ghost block.
     */
    private static void resyncBlock(ServerPlayer player, Level level, BlockPos pos) {
        player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
        MinecraftServer server = player.server;
        server.tell(new TickTask(server.getTickCount() + 1, () -> {
            if (!player.isRemoved()) {
                player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
            }
        }));
    }
}
