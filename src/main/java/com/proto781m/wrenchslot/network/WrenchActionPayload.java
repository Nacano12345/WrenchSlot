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
 * An empty-hand wrench action requested by the client.
 *
 * <p>The client cancels the vanilla interaction and sends this packet instead,
 * so the action is independent of the target block's own client-side use
 * result.
 */
public record WrenchActionPayload(BlockPos pos, Direction face, Vec3 hit, boolean inside, boolean rotate)
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
                        buf.writeBoolean(payload.rotate());
                    },
                    buf -> new WrenchActionPayload(
                            BlockPos.STREAM_CODEC.decode(buf),
                            Direction.STREAM_CODEC.decode(buf),
                            new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                            buf.readBoolean(),
                            buf.readBoolean()));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(WrenchActionPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                WrenchActions.perform(player, payload);
            }
        });
    }
}
