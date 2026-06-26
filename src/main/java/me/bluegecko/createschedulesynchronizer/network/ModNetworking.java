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
    }
}
