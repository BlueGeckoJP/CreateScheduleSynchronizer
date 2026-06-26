package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ScheduleSyncCurrentIdPayload(UUID syncId) implements CustomPacketPayload {
    public static final Type<ScheduleSyncCurrentIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "schedule_sync_current_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSyncCurrentIdPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        boolean hasId = payload.syncId() != null;
                        buffer.writeBoolean(hasId);
                        if (hasId) {
                            buffer.writeLong(payload.syncId().getMostSignificantBits());
                            buffer.writeLong(payload.syncId().getLeastSignificantBits());
                        }
                    },
                    buffer -> {
                        if (!buffer.readBoolean()) {
                            return new ScheduleSyncCurrentIdPayload(null);
                        }

                        return new ScheduleSyncCurrentIdPayload(
                                new UUID(buffer.readLong(), buffer.readLong())
                        );
                    }
            );

    public static void handle(
            ScheduleSyncCurrentIdPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() ->
                ScheduleSyncClientState.setCurrentId(payload.syncId())
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}