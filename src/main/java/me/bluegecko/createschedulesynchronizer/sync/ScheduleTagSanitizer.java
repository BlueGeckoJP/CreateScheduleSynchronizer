package me.bluegecko.createschedulesynchronizer.sync;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public final class ScheduleTagSanitizer {
    private ScheduleTagSanitizer() {
    }

    public static CompoundTag forSyncStore(
            HolderLookup.Provider registries,
            CompoundTag rawTag
    ) {
        Schedule schedule = Schedule.fromTag(registries, rawTag.copy());
        schedule.savedProgress = 0;
        return schedule.write(registries);
    }

    public static Schedule forRuntimeUpdate(
            HolderLookup.Provider registries,
            CompoundTag rawTag,
            ScheduleRuntime runtime
    ) {
        Schedule schedule = Schedule.fromTag(
                registries,
                forSyncStore(registries, rawTag)
        );

        if (schedule.entries.isEmpty()) {
            schedule.savedProgress = 0;
        } else {
            schedule.savedProgress = Math.clamp(
                    runtime.currentEntry,
                    0,
                    schedule.entries.size() - 1
            );
        }

        return schedule;
    }

    public static CompoundTag forItemView(
            HolderLookup.Provider registries,
            CompoundTag canonicalTag,
            CompoundTag currentItemTag
    ) {
        Schedule canonical = Schedule.fromTag(registries, canonicalTag.copy());

        if (currentItemTag != null) {
            Schedule current = Schedule.fromTag(registries, currentItemTag.copy());

            if (canonical.entries.isEmpty()) {
                canonical.savedProgress = 0;
            } else {
                canonical.savedProgress = Math.clamp(
                        current.savedProgress,
                        0,
                        canonical.entries.size() - 1
                );
            }
        } else {
            canonical.savedProgress = 0;
        }

        return canonical.write(registries);
    }
}
