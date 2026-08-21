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

public record SaveScheduleSyncPayload(CompoundTag scheduleTag, int syncTrainColor) implements CustomPacketPayload {
    public static final int UNCHANGED_TRAIN_COLOR = -1;

    public static final Type<SaveScheduleSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "save_schedule_sync"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveScheduleSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buffer, payload) -> {
                buffer.writeNbt(payload.scheduleTag());
                buffer.writeVarInt(payload.syncTrainColor());
            },
            buffer -> new SaveScheduleSyncPayload(
                    buffer.readNbt(),
                    buffer.readVarInt()
            )
    );

    public static void handle(
            SaveScheduleSyncPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncManager.SaveResult result = ScheduleSyncManager.saveMainHandScheduleToStore(
                    player,
                    payload.scheduleTag(),
                    payload.syncTrainColor()
            );

            switch (result) {
                case SAVED -> {
                    ScheduleSyncNetworkHelper.sendSyncPanelState(player);
                    player.displayClientMessage(
                            Component.literal("Synchronized schedule saved."),
                            true
                    );
                }

                case NOT_LINKED -> player.displayClientMessage(
                        Component.literal("This schedule is not linked to a sync ID."),
                        true
                );

                case NOT_SYNCHRONIZED_SCHEDULE -> player.displayClientMessage(
                        Component.literal("Hold a Synchronized Train Schedule to save."),
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