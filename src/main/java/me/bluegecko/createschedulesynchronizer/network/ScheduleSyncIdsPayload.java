package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncEntry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ScheduleSyncIdsPayload(List<ScheduleSyncEntry> entries) implements CustomPacketPayload {
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

    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSyncEntry> ENTRY_CODEC =
            StreamCodec.of(
                    (buffer, entry) -> {
                        UUID_CODEC.encode(buffer, entry.id());
                        buffer.writeUtf(entry.name(), 64);
                        buffer.writeBoolean(entry.syncTrainIdentity());
                        buffer.writeVarInt(entry.syncTrainColor());
                    },
                    buffer -> new ScheduleSyncEntry(
                            UUID_CODEC.decode(buffer),
                            buffer.readUtf(64),
                            buffer.readBoolean(),
                            buffer.readVarInt()
                    )
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, ScheduleSyncIdsPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buffer, payload) -> {
                        buffer.writeVarInt(payload.entries().size());
                        for (ScheduleSyncEntry entry : payload.entries()) {
                            ENTRY_CODEC.encode(buffer, entry);
                        }
                    },
                    buffer -> {
                        int size = buffer.readVarInt();
                        List<ScheduleSyncEntry> entries = new ArrayList<>(size);
                        for (int i = 0; i < size; i++) {
                            entries.add(ENTRY_CODEC.decode(buffer));
                        }
                        return new ScheduleSyncIdsPayload(entries);
                    }
            );

    public static void handle(
            ScheduleSyncIdsPayload payload,
            IPayloadContext context
    ) {
        context.enqueueWork(() -> ScheduleSyncClientState.setEntries(payload.entries()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
