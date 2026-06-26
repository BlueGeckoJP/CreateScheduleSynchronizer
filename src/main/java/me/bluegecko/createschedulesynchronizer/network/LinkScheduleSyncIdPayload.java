package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record LinkScheduleSyncIdPayload(UUID syncId) implements CustomPacketPayload {
    public static final Type<LinkScheduleSyncIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "link_schedule_sync_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, LinkScheduleSyncIdPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeLong(payload.syncId().getMostSignificantBits());
                        buffer.writeLong(payload.syncId().getLeastSignificantBits());
                    },
                    buffer -> new LinkScheduleSyncIdPayload(
                            new UUID(buffer.readLong(), buffer.readLong())
                    )
            );

    public static void handle(
            LinkScheduleSyncIdPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncManager.LinkWithTagResult result = ScheduleSyncManager.linkMainHandToScheduleIdAndGetTag(player, payload.syncId());

            switch (result.getResult()) {
                case LINKED -> {
                    if (result.getScheduleTag() != null) {
                        PacketDistributor.sendToPlayer(
                                player,
                                new ApplyScheduleSyncPayload(result.getScheduleTag())
                        );
                    }

                    ScheduleSyncNetworkHelper.sendSyncPanelState(player);

                    player.displayClientMessage(
                            Component.literal("Linked synchronized schedule."),
                            true
                    );


                }

                case NOT_FOUND -> player.displayClientMessage(
                        Component.literal("Selected sync ID was not found."),
                        true
                );

                case NOT_SYNCHRONIZED_SCHEDULE -> player.displayClientMessage(
                        Component.literal("Hold a Synchronized Train Schedule to link."),
                        true
                );
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
