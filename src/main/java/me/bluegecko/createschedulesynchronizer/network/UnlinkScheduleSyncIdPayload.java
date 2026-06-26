package me.bluegecko.createschedulesynchronizer.network;

import com.simibubi.create.content.trains.schedule.Schedule;
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

public record UnlinkScheduleSyncIdPayload() implements CustomPacketPayload {
    public static final Type<UnlinkScheduleSyncIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "unlink_schedule_sync_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnlinkScheduleSyncIdPayload> STREAM_CODEC =
            StreamCodec.unit(new UnlinkScheduleSyncIdPayload());

    public static void handle(
            UnlinkScheduleSyncIdPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncManager.UnlinkResult result =
                    ScheduleSyncManager.unlinkMainHand(player);

            switch (result) {
                case UNLINKED -> {
                    PacketDistributor.sendToPlayer(
                            player,
                            new ApplyScheduleSyncPayload(
                                    new Schedule().write(player.registryAccess())
                            )
                    );

                    ScheduleSyncNetworkHelper.sendSyncPanelState(player);

                    player.displayClientMessage(
                            Component.literal("Synchronized schedule unlinked."),
                            true
                    );
                }

                case NOT_SYNCHRONIZED_SCHEDULE -> player.displayClientMessage(
                        Component.literal("Hold a Synchronized Train Schedule to unlink."),
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