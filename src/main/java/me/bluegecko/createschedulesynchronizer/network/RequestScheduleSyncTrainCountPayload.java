package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.compat.RunningTrainScheduleSync;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RequestScheduleSyncTrainCountPayload(UUID syncId) implements CustomPacketPayload {
    public static final Type<RequestScheduleSyncTrainCountPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "request_schedule_sync_train_count"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScheduleSyncTrainCountPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {
                        buffer.writeLong(payload.syncId().getMostSignificantBits());
                        buffer.writeLong(payload.syncId().getLeastSignificantBits());
                    },
                    buffer -> new RequestScheduleSyncTrainCountPayload(
                            new UUID(buffer.readLong(), buffer.readLong())
                    )
            );

    public static void handle(
            RequestScheduleSyncTrainCountPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            int trainCount = RunningTrainScheduleSync.countLinkedTrains(
                    player.serverLevel(),
                    player.getUUID(),
                    payload.syncId()
            );

            PacketDistributor.sendToPlayer(
                    player,
                    new ScheduleSyncTrainCountPayload(
                            payload.syncId(),
                            trainCount
                    )
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
