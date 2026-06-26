package me.bluegecko.createschedulesynchronizer.client;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ScheduleSyncClientState {
    private static final List<ScheduleSyncEntry> ENTRIES = new ArrayList<>();
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
        currentId = null;
        pendingScheduleTag = null;
    }
}
