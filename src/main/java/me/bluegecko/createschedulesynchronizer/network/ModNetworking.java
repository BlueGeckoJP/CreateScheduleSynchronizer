package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {
    private ModNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(Createschedulesynchronizer.ID);

        registrar.playToServer(
                SaveScheduleSyncPayload.TYPE,
                SaveScheduleSyncPayload.STREAM_CODEC,
                SaveScheduleSyncPayload::handle
        );

        registrar.playToServer(
                NewScheduleSyncIdPayload.TYPE,
                NewScheduleSyncIdPayload.STREAM_CODEC,
                NewScheduleSyncIdPayload::handle
        );

        registrar.playToServer(
                RequestScheduleSyncIdsPayload.TYPE,
                RequestScheduleSyncIdsPayload.STREAM_CODEC,
                RequestScheduleSyncIdsPayload::handle
        );

        registrar.playToClient(
                ScheduleSyncIdsPayload.TYPE,
                ScheduleSyncIdsPayload.STREAM_CODEC,
                ScheduleSyncIdsPayload::handle
        );

        registrar.playToServer(
                LinkScheduleSyncIdPayload.TYPE,
                LinkScheduleSyncIdPayload.STREAM_CODEC,
                LinkScheduleSyncIdPayload::handle
        );
    }
}
