package me.bluegecko.createschedulesynchronizer.compat;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.GlobalRailwayManager;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.station.TrainEditPacket;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleTagSanitizer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

public final class RunningTrainScheduleSync {
    private RunningTrainScheduleSync() {
    }

    public static ApplyResult applyToLinkedTrains(
            ServerLevel level,
            UUID owner,
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

            if (!owner.equals(tracker.css$getScheduleSyncOwner())) {
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
                    owner,
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

    public static int countLinkedTrains(
            ServerLevel level,
            UUID owner,
            UUID syncId
    ) {
        GlobalRailwayManager railways = Create.RAILWAYS.sided(level);
        int count = 0;

        for (Train train : railways.trains.values()) {
            ScheduleRuntime runtime = train.runtime;

            if (!(runtime instanceof ScheduleSourceTracker tracker)) {
                continue;
            }

            if (!tracker.css$isSynchronizedSchedule()) {
                continue;
            }

            if (owner.equals(tracker.css$getScheduleSyncOwner()) && syncId.equals(tracker.css$getScheduleSyncId())) {
                count++;
            }
        }

        return count;
    }

    public static void applyNow(
            HolderLookup.Provider registries,
            ScheduleRuntime runtime,
            ScheduleSourceTracker tracker,
            UUID owner,
            UUID syncId,
            CompoundTag scheduleTag
    ) {
        Schedule schedule = ScheduleTagSanitizer.forRuntimeUpdate(
                registries,
                scheduleTag,
                runtime
        );

        boolean auto = runtime.isAutoSchedule;

        runtime.setSchedule(schedule, auto);
        tracker.css$setSynchronizedScheduleSource(owner, syncId);
    }

    public static int syncLinkedTrainIdentities(
            ServerLevel level,
            UUID owner,
            UUID syncId,
            String scheduleName,
            int syncTrainColor
    ) {
        if (scheduleName == null || scheduleName.isBlank()) {
            return 0;
        }

        GlobalRailwayManager railways = Create.RAILWAYS.sided(level);
        int changed = 0;

        for (Train train : railways.trains.values()) {
            ScheduleRuntime runtime = train.runtime;

            if (!(runtime instanceof ScheduleSourceTracker tracker)) {
                continue;
            }

            if (!tracker.css$isSynchronizedSchedule()
                    || !owner.equals(tracker.css$getScheduleSyncOwner())
                    || !syncId.equals(tracker.css$getScheduleSyncId())) {
                continue;
            }

            if (syncTrainIdentity(train, scheduleName, syncTrainColor)) {
                changed++;
            }
        }

        if (changed > 0) {
            railways.markTracksDirty();
        }

        return changed;
    }

    public static boolean syncTrainIdentity(Train train, String scheduleName, int syncTrainColor) {
        String name = scheduleName.trim();

        boolean nameChanged =
                !name.isEmpty() && !train.name.getString().equals(name);
        boolean colorChanged = train.mapColorIndex != syncTrainColor;

        if (!nameChanged && !colorChanged) {
            return false;
        }

        if (nameChanged) {
            train.name = Component.literal(name);
        }

        if (colorChanged) {
            train.mapColorIndex = syncTrainColor;
        }

        PacketDistributor.sendToAllPlayers(
                new TrainEditPacket.TrainEditReturnPacket(
                        train.id,
                        train.name.getString(),
                        train.icon.getId(),
                        train.mapColorIndex
                )
        );

        return true;
    }

    public record ApplyResult(int applied, int queued) {
    }
}
