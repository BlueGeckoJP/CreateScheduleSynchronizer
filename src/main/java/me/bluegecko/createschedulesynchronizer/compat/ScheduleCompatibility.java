package me.bluegecko.createschedulesynchronizer.compat;

import com.simibubi.create.content.trains.schedule.ScheduleItem;
import net.minecraft.world.item.ItemStack;

public final class ScheduleCompatibility {
    private ScheduleCompatibility() {}

    public static boolean isSchedule(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ScheduleItem;
    }
}
