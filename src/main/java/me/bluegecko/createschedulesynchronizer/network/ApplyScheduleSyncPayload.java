package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ApplyScheduleSyncPayload(CompoundTag scheduleTag) implements CustomPacketPayload {
    public static final Type<ApplyScheduleSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "apply_schedule_sync"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, ApplyScheduleSyncPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> buffer.writeNbt(payload.scheduleTag()),
                    buffer -> new ApplyScheduleSyncPayload(buffer.readNbt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ApplyScheduleSyncPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() ->
                ScheduleSyncClientState.setPendingScheduleTag(payload.scheduleTag())
        );
    }
}
