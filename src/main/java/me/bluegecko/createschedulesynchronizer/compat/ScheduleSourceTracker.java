package me.bluegecko.createschedulesynchronizer.compat;

import net.minecraft.world.item.ItemStack;

public interface ScheduleSourceTracker {
    void css$setScheduleSource(ItemStack source);

    boolean css$isSynchronizedSchedule();
}
