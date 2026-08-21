package me.bluegecko.createschedulesynchronizer.network;

import com.simibubi.create.content.trains.schedule.Schedule;
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncManager;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncSavedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RemoveScheduleSyncIdPayload(UUID syncId) implements CustomPacketPayload {
    public static final Type<RemoveScheduleSyncIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "remove_schedule_sync_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveScheduleSyncIdPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeLong(payload.syncId().getMostSignificantBits());
                        buffer.writeLong(payload.syncId().getLeastSignificantBits());
                    },
                    buffer -> new RemoveScheduleSyncIdPayload(
                            new UUID(buffer.readLong(), buffer.readLong())
                    )
            );

    public static void handle(
            RemoveScheduleSyncIdPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncSavedData data = ScheduleSyncSavedData.get(player.serverLevel());
            UUID owner = player.getUUID();

            if (!data.removeSchedule(owner, payload.syncId())) {
                player.displayClientMessage(
                        Component.literal("Selected schedule no longer exists."),
                        true
                );
                ScheduleSyncNetworkHelper.sendSyncPanelState(player);
                return;
            }

            ScheduleSyncManager.unlinkMainHand(player);

            PacketDistributor.sendToPlayer(
                    player,
                    new ApplyScheduleSyncPayload(
                            new Schedule().write(player.registryAccess())
                    )
            );
            ScheduleSyncNetworkHelper.sendSyncPanelState(player);

            player.displayClientMessage(
                    Component.literal("Schedule removed."),
                    true
            );
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
