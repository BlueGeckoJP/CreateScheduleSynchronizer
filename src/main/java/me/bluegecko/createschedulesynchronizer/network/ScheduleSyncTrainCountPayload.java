package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ScheduleSyncTrainCountPayload(UUID syncId, int trainCount) implements CustomPacketPayload {
    public static final Type<ScheduleSyncTrainCountPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "schedule_sync_train_count"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSyncTrainCountPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeLong(payload.syncId().getMostSignificantBits());
                        buffer.writeLong(payload.syncId().getLeastSignificantBits());
                        buffer.writeVarInt(payload.trainCount());
                    },
                    buffer -> new ScheduleSyncTrainCountPayload(
                            new UUID(buffer.readLong(), buffer.readLong()),
                            buffer.readVarInt()
                    )
            );

    public static void handle(
            ScheduleSyncTrainCountPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> {
            ScheduleSyncClientState.setTrainCount(
                    payload.syncId(),
                    payload.trainCount()
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
