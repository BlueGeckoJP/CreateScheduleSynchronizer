package me.bluegecko.createschedulesynchronizer.compat;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncManager;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public final class ScheduleCompatibility {
    private ScheduleCompatibility() {
    }

    public static boolean isSchedule(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ScheduleItem;
    }

    public static boolean isSynchronizedSchedule(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof SynchronizedScheduleItem;
    }

    public static UUID getSyncId(ItemStack stack) {
        if (!isSynchronizedSchedule(stack)) {
            return null;
        }

        return SynchronizedScheduleItem.getSyncId(stack);
    }

    public static void setSyncId(ItemStack stack, UUID syncId) {
        if (syncId != null && isSynchronizedSchedule(stack)) {
            SynchronizedScheduleItem.setSyncId(stack, syncId);
        }
    }

    public static UUID getSyncOwner(ItemStack stack) {
        if (!isSynchronizedSchedule(stack)) {
            return null;
        }

        return SynchronizedScheduleItem.getSyncOwner(stack);
    }

    public static void setSyncOwner(ItemStack stack, UUID owner) {
        if (owner != null && isSynchronizedSchedule(stack)) {
            SynchronizedScheduleItem.setSyncOwner(stack, owner);
        }
    }

    public static void syncFromStoreIfPossible(ItemStack stack, ServerLevel level) {
        if (isSynchronizedSchedule(stack)) {
            ScheduleSyncManager.syncItemFromStore(stack, level);
        }
    }

    public static void rememberSource(
            Object runtime,
            ItemStack source
    ) {
        ((ScheduleSourceTracker) runtime).css$setScheduleSource(source);
    }

    /*
     * Immediately after a schedule is assigned to a train,
     * update the train name and color if synchronization is enabled
     *
     * Manual name and color changes are not monitored, so player-defined
     * values are preserved until this process is called again
     */
    public static boolean syncTrainIdentityAfterScheduleApplied(
            ScheduleRuntime runtime,
            ServerLevel level
    ) {
        if (!(runtime instanceof ScheduleSourceTracker tracker)) {
            return false;
        }

        if (!tracker.css$isSynchronizedSchedule()) {
            return false;
        }

        UUID owner = tracker.css$getScheduleSyncOwner();
        UUID syncId = tracker.css$getScheduleSyncId();

        if (owner == null || syncId == null) {
            return false;
        }

        ScheduleSyncSavedData data = ScheduleSyncSavedData.get(level);

        if (!data.isTrainIdentitySyncEnabled(owner, syncId)) {
            return false;
        }

        String scheduleName = data.getDisplayName(owner, syncId);
        int syncTrainColor = data.getSyncTrainColor(owner, syncId);


        if (scheduleName == null || scheduleName.isBlank()) {
            return false;
        }

        boolean changed = RunningTrainScheduleSync.syncTrainIdentity(
                runtime.train,
                scheduleName,
                syncTrainColor
        );

        if (changed) {
            Create.RAILWAYS.sided(level).markTracksDirty();
        }

        return changed;
    }
}
