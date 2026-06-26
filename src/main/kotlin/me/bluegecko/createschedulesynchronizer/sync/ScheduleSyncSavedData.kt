package me.bluegecko.createschedulesynchronizer.sync

import com.simibubi.create.content.trains.schedule.Schedule
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.*

class ScheduleSyncSavedData : SavedData() {
    private val schedules: MutableMap<UUID, CompoundTag> = linkedMapOf()

    fun ids(): List<UUID> = schedules.keys.toList()

    fun contains(id: UUID): Boolean = id in schedules.keys

    fun getScheduleTag(id: UUID): CompoundTag? = schedules[id]?.copy()

    fun putScheduleTag(id: UUID, scheduleTag: CompoundTag) {
        schedules[id] = scheduleTag.copy()
        setDirty()
    }

    fun removeSchedule(id: UUID): Boolean {
        val removed = schedules.remove(id) != null
        if (removed) {
            setDirty()
        }
        return removed
    }

    fun clear() {
        if (schedules.isNotEmpty()) {
            schedules.clear()
            setDirty()
        }
    }

    fun getSchedule(id: UUID, registries: HolderLookup.Provider): Schedule? {
        val tag = schedules[id] ?: return null
        return Schedule.fromTag(registries, tag)
    }

    fun putSchedule(id: UUID, schedule: Schedule, registries: HolderLookup.Provider) {
        schedules[id] = schedule.write(registries)
        setDirty()
    }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val list = ListTag()

        for ((id, scheduleTag) in schedules) {
            val entry = CompoundTag()
            entry.putUUID("Id", id)
            entry.put("Schedule", scheduleTag.copy())
            list.add(entry)
        }

        tag.put("Schedules", list)
        return tag
    }

    companion object {
        private const val DATA_NAME = Createschedulesynchronizer.ID + "_schedule_sync"

        fun get(level: ServerLevel): ScheduleSyncSavedData {
            val overworld = level.server.getLevel(Level.OVERWORLD) ?: level

            return overworld.dataStorage.computeIfAbsent(
                Factory(
                    { ScheduleSyncSavedData() },
                    { tag, registries -> load(tag, registries) },
                    null
                ),
                DATA_NAME,
            )
        }

        private fun load(tag: CompoundTag, registries: HolderLookup.Provider): ScheduleSyncSavedData {
            val data = ScheduleSyncSavedData()
            val list = tag.getList("Schedules", Tag.TAG_COMPOUND.toInt())

            for (i in list.indices) {
                val entry = list.getCompound(i)

                if (!entry.hasUUID("Id")) {
                    continue
                }

                if (!entry.contains("Schedule", Tag.TAG_COMPOUND.toInt())) {
                    continue
                }

                val id = entry.getUUID("Id")
                val scheduleTag = entry.getCompound("Schedule")

                data.schedules[id] = scheduleTag.copy()
            }

            return data
        }
    }

}