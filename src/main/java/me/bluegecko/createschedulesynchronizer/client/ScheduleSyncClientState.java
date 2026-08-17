package me.bluegecko.createschedulesynchronizer.client;

import net.minecraft.nbt.CompoundTag;

import java.util.*;

public final class ScheduleSyncClientState {
    private static final List<ScheduleSyncEntry> ENTRIES = new ArrayList<>();
    private static final Map<UUID, Integer> TRAIN_COUNTS = new HashMap<>();
    private static UUID currentId;
    private static CompoundTag pendingScheduleTag;

    private ScheduleSyncClientState() {
    }

    public static List<ScheduleSyncEntry> getEntries() {
        return List.copyOf(ENTRIES);
    }

    public static void setEntries(List<ScheduleSyncEntry> entries) {
        ENTRIES.clear();
        ENTRIES.addAll(entries);
    }

    public static UUID getCurrentId() {
        return currentId;
    }

    public static void setCurrentId(UUID id) {
        currentId = id;
    }

    public static ScheduleSyncEntry getCurrentEntry() {
        if (currentId == null) {
            return null;
        }

        for (ScheduleSyncEntry entry : ENTRIES) {
            if (currentId.equals(entry.id())) {
                return entry;
            }
        }

        return null;
    }

    public static Integer getTrainCount(UUID syncId) {
        return TRAIN_COUNTS.get(syncId);
    }

    public static void setTrainCount(UUID syncId, int trainCount) {
        TRAIN_COUNTS.put(syncId, trainCount);
    }

    public static void clearTrainCount(UUID syncId) {
        TRAIN_COUNTS.remove(syncId);
    }

    public static void setPendingScheduleTag(CompoundTag tag) {
        pendingScheduleTag = tag.copy();
    }

    public static CompoundTag consumePendingScheduleTag() {
        CompoundTag tag = pendingScheduleTag;
        pendingScheduleTag = null;
        return tag == null ? null : tag.copy();
    }

    public static void clear() {
        ENTRIES.clear();
        TRAIN_COUNTS.clear();
        currentId = null;
        pendingScheduleTag = null;
    }
}
