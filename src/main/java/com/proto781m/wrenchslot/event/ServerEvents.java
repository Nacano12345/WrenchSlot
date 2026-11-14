package com.proto781m.wrenchslot.event;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.curio.CurioWrench;
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
     * The client always sends the vanilla use packet as well (1.21.1 uses
     * client-side prediction). The wrench action itself is handled by
     * {@code WrenchActionPayload}; this handler only cancels the vanilla
     * interaction so a wrenchable block is not used twice.
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!event.getItemStack().isEmpty() || !Config.enabled()) {
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
        if (state.getBlock() instanceof IWrenchable
                || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state)) {
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
        if (!player.getMainHandItem().isEmpty()) {
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
}
