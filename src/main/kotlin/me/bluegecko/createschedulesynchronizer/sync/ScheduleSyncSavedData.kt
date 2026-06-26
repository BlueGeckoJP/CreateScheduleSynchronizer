package me.bluegecko.createschedulesynchronizer.sync

import com.simibubi.create.content.trains.schedule.Schedule
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncEntry
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.saveddata.SavedData
import java.util.*

class ScheduleSyncSavedData : SavedData() {
    data class StoredSchedule(
        var name: String,
        var scheduleTag: CompoundTag,
    )

    private val schedules: MutableMap<UUID, StoredSchedule> = linkedMapOf()

    fun ids(): List<UUID> = schedules.keys.toList()

    fun contains(id: UUID): Boolean = id in schedules.keys

    fun getScheduleTag(id: UUID): CompoundTag? = schedules[id]?.scheduleTag?.copy()

    fun putScheduleTag(id: UUID, scheduleTag: CompoundTag, name: String? = null) {
        val existing = schedules[id]

        val displayName = sanitizeDisplayName(
            name ?: existing?.name ?: defaultDisplayName(id),
            id
        )

        schedules[id] = StoredSchedule(
            name = displayName,
            scheduleTag = scheduleTag.copy()
        )

        setDirty()
    }

    fun getDisplayName(id: UUID): String? = schedules[id]?.name

    fun setDisplayName(id: UUID, name: String) {
        val stored = schedules[id] ?: return
        stored.name = sanitizeDisplayName(name, id)
        setDirty()
    }

    fun namedEntries(): List<ScheduleSyncEntry> {
        return schedules.map { (id, stored) ->
            ScheduleSyncEntry(id, stored.name)
        }
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
        val stored = schedules[id] ?: return null
        return Schedule.fromTag(registries, stored.scheduleTag.copy())
    }

    fun putSchedule(id: UUID, schedule: Schedule, registries: HolderLookup.Provider, name: String? = null) {
        putScheduleTag(id, schedule.write(registries), name)
    }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val list = ListTag()

        for ((id, stored) in schedules) {
            val entry = CompoundTag()
            entry.putUUID("Id", id)
            entry.putString("Name", stored.name)
            entry.put("Schedule", stored.scheduleTag.copy())
            list.add(entry)
        }

        tag.put("Schedules", list)
        return tag
    }

    companion object {
        private const val DATA_NAME = Createschedulesynchronizer.ID + "_schedule_sync"

        @JvmStatic
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

                val rawName = if (entry.contains("Name", Tag.TAG_STRING.toInt())) {
                    entry.getString("Name")
                } else {
                    defaultDisplayName(id)
                }

                data.schedules[id] = StoredSchedule(
                    name = sanitizeDisplayName(rawName, id),
                    scheduleTag = entry.getCompound("Schedule").copy(),
                )
            }

            return data
        }

        private fun defaultDisplayName(id: UUID): String = "Schedule ${id.toString().substring(0, 8)}"

        private fun sanitizeDisplayName(
            name: String,
            id: UUID
        ): String {
            val trimmed = name.trim()

            if (trimmed.isEmpty()) {
                return defaultDisplayName(id)
            }

            val maxCodePoints = 32
            val codePoints = trimmed.codePoints().toArray()

            return if (codePoints.size <= maxCodePoints) {
                trimmed
            } else {
                String(codePoints, 0, maxCodePoints)
            }
        }
    }

}