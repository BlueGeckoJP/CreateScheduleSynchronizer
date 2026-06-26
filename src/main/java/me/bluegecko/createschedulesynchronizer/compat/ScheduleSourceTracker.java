package me.bluegecko.createschedulesynchronizer.compat;

import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public interface ScheduleSourceTracker {
    void css$setScheduleSource(ItemStack source);

    boolean css$isSynchronizedSchedule();

    UUID css$getScheduleSyncId();
}
