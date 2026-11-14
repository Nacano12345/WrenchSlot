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
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!event.getItemStack().isEmpty() || !Config.enabled()) {
            return;
        }

        Player player = event.getEntity();
        if (!CurioWrench.has(player)) {
            return;
        }

        BlockState state = event.getLevel().getBlockState(event.getPos());
        if (!(state.getBlock() instanceof IWrenchable)
                && !AllTags.AllBlockTags.WRENCH_PICKUP.matches(state)) {
            return;
        }

        BlockHitResult hit = event.getHitVec();
        PacketDistributor.sendToServer(new WrenchActionPayload(
                hit.getBlockPos(),
                hit.getDirection(),
                hit.getLocation(),
                hit.isInside(),
                Keybinds.ROTATE.isDown()));

        // Consume the client interaction; the server executes the action from
        // our own packet and cancels the vanilla use there.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
