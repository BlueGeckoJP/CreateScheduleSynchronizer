package me.bluegecko.createschedulesynchronizer.compat;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.AddTrainPacket;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;

import java.util.UUID;

public final class RunningTrainScheduleSync {
    private RunningTrainScheduleSync() {
    }

    public static ApplyResult applyToLinkedTrains(
            ServerLevel level,
            UUID syncId,
            CompoundTag scheduleTag
    ) {
        GlobalRailwayManager railways = Create.RAILWAYS.sided(level);

        int applied = 0;
        int queued = 0;

        for (Train train : railways.trains.values()) {
            ScheduleRuntime runtime = train.runtime;

            if (!(runtime instanceof ScheduleSourceTracker tracker)) {
                continue;
            }

            if (!syncId.equals(tracker.css$getScheduleSyncId())) {
                continue;
            }

            if (runtime.state == ScheduleRuntime.State.IN_TRANSIT) {
                tracker.css$queueScheduleUpdate(scheduleTag);
                queued++;
                continue;
            }

            applyNow(
                    level.registryAccess(),
                    runtime,
                    tracker,
                    syncId,
                    scheduleTag
            );

            applied++;
        }

        if (applied > 0 || queued > 0) {
            railways.markTracksDirty();
        }

        return new ApplyResult(applied, queued);
    }

    public static void applyNow(
            HolderLookup.Provider registries,
            ScheduleRuntime runtime,
            ScheduleSourceTracker tracker,
            UUID syncId,
            CompoundTag scheduleTag
    ) {
        Schedule schedule = Schedule.fromTag(registries, scheduleTag.copy());

        if (schedule.entries.isEmpty()) {
            schedule.savedProgress = 0;
        } else {
            schedule.savedProgress = Math.clamp(schedule.entries.size() - 1,
                    0, runtime.currentEntry);
        }

        boolean auto = runtime.isAutoSchedule;

        runtime.setSchedule(schedule, auto);
        tracker.css$setSynchronizedScheduleSource(syncId);
    }

    public record ApplyResult(int applied, int queued) {
    }
}
