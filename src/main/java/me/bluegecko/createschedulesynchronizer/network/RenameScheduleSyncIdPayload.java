package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncSavedData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record RenameScheduleSyncIdPayload(UUID syncId, String name) implements CustomPacketPayload {
    public static final Type<RenameScheduleSyncIdPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "rename_schedule_sync_id"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameScheduleSyncIdPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeLong(payload.syncId().getMostSignificantBits());
                        buffer.writeLong(payload.syncId().getLeastSignificantBits());
                        buffer.writeUtf(payload.name(), 64);
                    },
                    buffer -> new RenameScheduleSyncIdPayload(
                            new UUID(buffer.readLong(), buffer.readLong()),
                            buffer.readUtf(64)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            RenameScheduleSyncIdPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        context.enqueueWork(() -> {
            ScheduleSyncSavedData data = ScheduleSyncSavedData.get(player.serverLevel());

            if (!data.contains(payload.syncId())) {
                player.displayClientMessage(
                        Component.literal("Selected schedule no longer exists."),
                        true
                );
                return;
            }

            data.setDisplayName(payload.syncId(), payload.name());

            String displayName = data.getDisplayName(payload.syncId());

            ItemStack stack = player.getMainHandItem();
            UUID currentId = SynchronizedScheduleItem.getSyncId(stack);

            if (displayName != null && payload.syncId().equals(currentId)) {
                SynchronizedScheduleItem.setSyncName(stack, displayName);
            }

            ScheduleSyncNetworkHelper.sendSyncPanelState(player);

            player.displayClientMessage(
                    Component.literal("Schedule name updated."),
                    true
            );
        });
    }
}