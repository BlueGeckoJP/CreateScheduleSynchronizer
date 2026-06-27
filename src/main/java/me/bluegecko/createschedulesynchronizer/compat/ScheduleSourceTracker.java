package me.bluegecko.createschedulesynchronizer.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public interface ScheduleSourceTracker {
    void css$setScheduleSource(ItemStack source);

    void css$setSynchronizedScheduleSource(UUID owner, UUID syncId);

    boolean css$isSynchronizedSchedule();

    UUID css$getScheduleSyncOwner();

    UUID css$getScheduleSyncId();

    void css$queueScheduleUpdate(CompoundTag scheduleTag);

    boolean css$hasPendingScheduleUpdate();
}
