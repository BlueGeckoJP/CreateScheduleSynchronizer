package me.bluegecko.createschedulesynchronizer.client;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ScheduleSyncClientState {
    private static final List<UUID> IDS = new ArrayList<>();
    private static CompoundTag pendingScheduleTag;

    private ScheduleSyncClientState() {
    }

    public static void setIds(List<UUID> ids) {
        IDS.clear();
        IDS.addAll(ids);
    }

    public static List<UUID> getIds() {
        return List.copyOf(IDS);
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
        IDS.clear();
        pendingScheduleTag = null;
    }
}
