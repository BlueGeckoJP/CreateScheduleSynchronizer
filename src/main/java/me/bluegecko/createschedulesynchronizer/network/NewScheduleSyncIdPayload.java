package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record NewScheduleSyncIdPayload(CompoundTag scheduleTag) implements CustomPacketPayload {
    public static final Type<NewScheduleSyncIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "new_schedule_sync_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, NewScheduleSyncIdPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeNbt(payload.scheduleTag()),
                    buffer -> new NewScheduleSyncIdPayload(buffer.readNbt())
            );

    public static void handle(
            NewScheduleSyncIdPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncManager.NewIdResult result =
                    ScheduleSyncManager.createNewSyncIdForMainHand(
                            player,
                            payload.scheduleTag()
                    );

            switch (result.getStatus()) {
                case CREATED -> {
                    ScheduleSyncNetworkHelper.sendSyncPanelState(player);
                    player.displayClientMessage(
                            Component.literal("New sync ID created: " + result.getSyncId()),
                            true
                    );
                }

                case NOT_SYNCHRONIZED_SCHEDULE -> player.displayClientMessage(
                        Component.literal("Hold a Synchronized Train Schedule to create a new sync ID."),
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
