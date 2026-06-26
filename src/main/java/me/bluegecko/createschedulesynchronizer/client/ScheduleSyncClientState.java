package me.bluegecko.createschedulesynchronizer.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ScheduleSyncClientState {
    private static final List<UUID> IDS = new ArrayList<>();

    private ScheduleSyncClientState() {
    }

    public static void setIds(List<UUID> ids) {
        IDS.clear();
        IDS.addAll(ids);
    }

    public static List<UUID> getIds() {
        return List.copyOf(IDS);
    }

    public static void clear() {
        IDS.clear();
    }
}
