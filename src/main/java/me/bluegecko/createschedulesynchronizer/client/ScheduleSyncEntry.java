package me.bluegecko.createschedulesynchronizer.client;

import java.util.UUID;

public record ScheduleSyncEntry(UUID id, String name, boolean syncTrainName) {
}
