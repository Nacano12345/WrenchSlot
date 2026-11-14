package com.proto781m.wrenchslot.network;

import com.proto781m.wrenchslot.WrenchSlot;
import com.proto781m.wrenchslot.logic.WrenchActions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * An empty-hand wrench action requested by the client while this mod's primary
 * modifier is held.
 *
 * <p>The action type is decided server-side from the player's sneaking state:
 * not sneaking rotates, sneaking dismantles.
 */
public record WrenchActionPayload(BlockPos pos, Direction face, Vec3 hit, boolean inside)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<WrenchActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(WrenchSlot.MOD_ID, "wrench_action"));

    public static final StreamCodec<ByteBuf, WrenchActionPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        BlockPos.STREAM_CODEC.encode(buf, payload.pos());
                        Direction.STREAM_CODEC.encode(buf, payload.face());
                        buf.writeDouble(payload.hit().x);
                        buf.writeDouble(payload.hit().y);
                        buf.writeDouble(payload.hit().z);
                        buf.writeBoolean(payload.inside());
                    },
                    buf -> new WrenchActionPayload(
                            BlockPos.STREAM_CODEC.decode(buf),
                            Direction.STREAM_CODEC.decode(buf),
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WrenchActionPayload payload, IPayloadContext context) {
        if (context.player() instanceof ServerPlayer player) {
            // Mark the pending action immediately (before enqueueWork) so the
            // vanilla use packet that follows can be recognised and cancelled.
            WrenchActions.markPending(player, payload.pos());
            context.enqueueWork(() -> WrenchActions.perform(player, payload));
        }
    }
}
