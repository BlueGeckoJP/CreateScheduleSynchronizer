package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.compat.RunningTrainScheduleSync;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncSavedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record ToggleTrainIdentitySyncPayload(UUID syncId, boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleTrainIdentitySyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "toggle_train_identity_sync"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleTrainIdentitySyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeUUID(payload.syncId());
                        buffer.writeBoolean(payload.enabled());
                    },
                    buffer -> new ToggleTrainIdentitySyncPayload(
                            buffer.readUUID(),
                            buffer.readBoolean()
                    )
            );

    public static void handle(
            ToggleTrainIdentitySyncPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            UUID owner = player.getUUID();
            ScheduleSyncSavedData data =
                    ScheduleSyncSavedData.get(player.serverLevel());

            if (!data.setTrainIdentitySyncEnabled(
                    owner,
                    payload.syncId(),
                    payload.enabled()
            )) {
                return;
            }

            if (payload.enabled()) {
                String name = data.getDisplayName(owner, payload.syncId());
                int syncTrainColor = data.getSyncTrainColor(owner, payload.syncId());

                if (name != null) {
                    RunningTrainScheduleSync.syncLinkedTrainIdentities(
                            player.serverLevel(),
                            owner,
                            payload.syncId(),
                            name,
                            syncTrainColor
                    );
                }
            }

            ScheduleSyncNetworkHelper.sendSyncPanelState(player);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
