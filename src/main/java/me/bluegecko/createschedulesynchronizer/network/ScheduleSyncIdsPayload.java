package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ScheduleSyncIdsPayload(List<UUID> ids) implements CustomPacketPayload {
    public static final Type<ScheduleSyncIdsPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "schedule_sync_ids"
            ));

    public static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC =
            StreamCodec.of(
                    (buffer, uuid) -> {
                        buffer.writeLong(uuid.getMostSignificantBits());
                        buffer.writeLong(uuid.getLeastSignificantBits());
                    },
                    buffer -> new UUID(buffer.readLong(), buffer.readLong())
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSyncIdsPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeVarInt(payload.ids().size());
                        for (UUID id : payload.ids()) {
                            UUID_CODEC.encode(buffer, id);
                        }
                    },
                    buffer -> {
                        int size = buffer.readVarInt();
                        List<UUID> ids = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            ids.add(UUID_CODEC.decode(buffer));
                        }
                        return new ScheduleSyncIdsPayload(ids);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(
            ScheduleSyncIdsPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> ScheduleSyncClientState.setIds(payload.ids()));
    }
}