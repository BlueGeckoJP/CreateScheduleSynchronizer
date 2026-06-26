package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestScheduleSyncIdsPayload() implements CustomPacketPayload {
    public static final Type<RequestScheduleSyncIdsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "request_schedule_sync_ids"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestScheduleSyncIdsPayload> STREAM_CODEC =
            StreamCodec.unit(new RequestScheduleSyncIdsPayload());

    public static void handle(
            RequestScheduleSyncIdsPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncNetworkHelper.sendSyncPanelState(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
