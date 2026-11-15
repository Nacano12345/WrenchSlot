package com.proto781m.wrenchslot.event;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.network.WrenchActionPayload;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Client-side input handling for the empty-hand wrench actions.
 *
 * <p>This mod's primary modifier (Ctrl by default) must be held; Create's
 * original primary modifier (sneak) acts as the secondary modifier and is
 * evaluated server-side.
 */
@EventBusSubscriber(modid = WrenchSlot.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.isCanceled()) {
            return;
        }
        if (!Config.enabled()) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        boolean wrenchBlock = state.getBlock() instanceof IWrenchable
                || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
        if (!wrenchBlock) {
            return;
        }

        Player player = event.getEntity();
        boolean curio = CurioWrench.has(player);
        boolean modifier = Keybinds.isModifierDown();

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            WrenchSlot.LOGGER.info("Client wrench offhand: item={}, curio={}, modifier={}, block={}",
                    event.getItemStack(), curio, modifier, state);
            return;
        }

        WrenchSlot.LOGGER.info("Client wrench: item={}, curio={}, modifier={}, block={}, pos={}",
                event.getItemStack(), curio, modifier, state, event.getPos());

        if (!modifier || !curio) {
            return;
        }

        // Always consume the vanilla interaction while the modifier is held,
        // even if the main hand is not empty. Otherwise the held item would be
        // used (e.g. a block would be placed) before the wrench action runs.
        // The server decides whether the wrench action itself is allowed.
        BlockHitResult hit = event.getHitVec();
        WrenchSlot.LOGGER.info("Sending wrench action: pos={}, hand={}",
                hit.getBlockPos(), event.getHand());
        PacketDistributor.sendToServer(new WrenchActionPayload(
                hit.getBlockPos(),
                hit.getDirection(),
                hit.getLocation(),
                hit.isInside()));

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
