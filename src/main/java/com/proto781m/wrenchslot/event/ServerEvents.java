package com.proto781m.wrenchslot.event;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.logic.WrenchActions;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Server-side behaviour for the Curios wrench slot.
 */
@EventBusSubscriber(modid = WrenchSlot.MOD_ID)
public final class ServerEvents {

    private static final String WRENCH_SLOT = "wrench";

    private ServerEvents() {
    }

    /**
     * Logs whether the Curios slot data was loaded, which makes misconfigured
     * slot/entity JSON files easier to diagnose.
     */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        CuriosApi.getSlot(WRENCH_SLOT, false).ifPresentOrElse(
                slot -> WrenchSlot.LOGGER.info("Curios wrench slot loaded (validators: {})",
                        slot.getValidators()),
                () -> WrenchSlot.LOGGER.error("Curios wrench slot is missing!"));

        WrenchSlot.LOGGER.debug("Player curio slots: {}", CuriosApi.getPlayerSlots(false).keySet());
    }

    /**
     * 1.21.1 sends the vanilla use packet through client-side prediction even
     * when the client cancels the interaction. Cancel that packet server-side
     * when it corresponds to a wrench action we already received, so the block
     * is not used twice.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!Config.enabled()) {
            return;
        }

        Player player = event.getEntity();
        if (!player.mayBuild() || player.isSpectator() || !CurioWrench.has(player)) {
            return;
        }
        if (!player.canInteractWithBlock(event.getPos(), 1.0D)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        boolean wrenchBlock = state.getBlock() instanceof IWrenchable
                || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
        if (!wrenchBlock) {
            return;
        }

        boolean pending = WrenchActions.hasPending(player, event.getPos());
        WrenchSlot.LOGGER.info("Server wrench use: item={}, pending={}, block={}, pos={}",
                event.getItemStack(), pending, state, event.getPos());

        if (pending) {
            WrenchActions.clearPending(player);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        }
    }

    /**
     * Empty-hand attacks instantly destroy minecarts, mirroring
     * {@code WrenchItem#wrenchInstaKillsMinecarts}.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || event.isCanceled() || !Config.minecartKillEnabled()) {
            return;
        }
        if (!Config.allowNonEmptyHand() && !player.getMainHandItem().isEmpty()) {
            return;
        }
        if (!(event.getTarget() instanceof AbstractMinecart minecart)) {
            return;
        }
        if (player.isCreative() || !CurioWrench.has(player)) {
            return;
        }

        minecart.hurt(minecart.damageSources().playerAttack(player), 100);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        WrenchActions.clearPending(event.getEntity());
    }
}
