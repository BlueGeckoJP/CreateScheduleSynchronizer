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

        registrar.playToClient(
                ApplyScheduleSyncPayload.TYPE,
                ApplyScheduleSyncPayload.STREAM_CODEC,
                ApplyScheduleSyncPayload::handle
        );

        registrar.playToServer(
                UnlinkScheduleSyncIdPayload.TYPE,
                UnlinkScheduleSyncIdPayload.STREAM_CODEC,
                UnlinkScheduleSyncIdPayload::handle
        );

        registrar.playToClient(
                ScheduleSyncCurrentIdPayload.TYPE,
                ScheduleSyncCurrentIdPayload.STREAM_CODEC,
                ScheduleSyncCurrentIdPayload::handle
        );

        registrar.playToServer(
                RenameScheduleSyncIdPayload.TYPE,
                RenameScheduleSyncIdPayload.STREAM_CODEC,
                RenameScheduleSyncIdPayload::handle
        );

        registrar.playToServer(
                RequestScheduleSyncTrainCountPayload.TYPE,
                RequestScheduleSyncTrainCountPayload.STREAM_CODEC,
                RequestScheduleSyncTrainCountPayload::handle
        );

        registrar.playToClient(
                ScheduleSyncTrainCountPayload.TYPE,
                ScheduleSyncTrainCountPayload.STREAM_CODEC,
                ScheduleSyncTrainCountPayload::handle
        );

        registrar.playToServer(
                ToggleTrainIdentitySyncPayload.TYPE,
                ToggleTrainIdentitySyncPayload.STREAM_CODEC,
                ToggleTrainIdentitySyncPayload::handle
        );
    }
}
