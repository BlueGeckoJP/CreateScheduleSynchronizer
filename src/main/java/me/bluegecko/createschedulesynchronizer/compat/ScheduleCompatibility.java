package me.bluegecko.createschedulesynchronizer.compat;

import com.simibubi.create.content.trains.schedule.ScheduleItem;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import net.minecraft.world.item.ItemStack;

public final class ScheduleCompatibility {
    private ScheduleCompatibility() {
    }

    public static boolean isSchedule(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ScheduleItem;
    }

    public static boolean isSynchronizedSchedule(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SynchronizedScheduleItem;
    }

    public static void rememberSource(
            Object runtime,
            ItemStack source
    ) {
        ((ScheduleSourceTracker) runtime).css$setScheduleSource(source);
    }
}
