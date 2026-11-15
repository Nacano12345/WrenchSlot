package com.proto781m.wrenchslot.event;

import com.proto781m.wrenchslot.Config;
import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.curio.CurioWrench;
import com.proto781m.wrenchslot.network.WrenchActionPayload;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.client.gui.screens.Screen;
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
 * Client-side input handling.
 *
 * <p>Holding this mod's primary modifier (Ctrl by default) puts the player into
 * "wrench mode": all right-click interactions are handed to the mod, so the
 * held item is never placed or used by vanilla. The server decides whether the
 * wrench action is actually allowed (block type, main hand, config).
 */
@EventBusSubscriber(modid = WrenchSlot.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {

    private ClientEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.isCanceled() || !Config.enabled()) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!Keybinds.isModifierDown() || !CurioWrench.has(player)) {
            return;
        }
        // Do not hijack the vanilla sprint key while actually sprinting.
        if (player.isSprinting() && Keybinds.isSprintKey()) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        boolean wrenchBlock = state.getBlock() instanceof IWrenchable
                || AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
        if (!wrenchBlock) {
            // Not a wrench target: let vanilla handle chests, placement, etc.
            return;
        }

        BlockHitResult hit = event.getHitVec();
        boolean dismantle = player.isShiftKeyDown() || Screen.hasShiftDown();
        boolean emptyHand = event.getItemStack().isEmpty();
        WrenchSlot.LOGGER.info(
                "Wrench mode: item={}, sneak={}, emptyHand={}, block={}, pos={}",
                event.getItemStack(), dismantle, emptyHand, state, hit.getBlockPos());

        PacketDistributor.sendToServer(new WrenchActionPayload(
                hit.getBlockPos(),
                hit.getDirection(),
                hit.getLocation(),
                hit.isInside(),
                dismantle,
                emptyHand));

        // Consume the interaction in wrench mode, even for non-wrench blocks,
        // so the held item cannot be placed while the modifier is held.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
