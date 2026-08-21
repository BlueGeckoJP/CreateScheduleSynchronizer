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
        var syncTrainIdentity: Boolean = false,
        var syncTrainColor: Int? = null,
    )

    private val schedulesByOwner: MutableMap<UUID, MutableMap<UUID, StoredSchedule>> = linkedMapOf()

    private fun schedulesOf(owner: UUID): MutableMap<UUID, StoredSchedule> =
        schedulesByOwner.getOrPut(owner) { linkedMapOf() }

    private fun schedulesOfOrNull(owner: UUID): MutableMap<UUID, StoredSchedule>? =
        schedulesByOwner[owner]

    fun ids(owner: UUID): List<UUID> = schedulesOfOrNull(owner)?.keys?.toList() ?: emptyList()

    fun contains(owner: UUID, id: UUID): Boolean = schedulesOfOrNull(owner)?.containsKey(id) == true

    fun getScheduleTag(owner: UUID, id: UUID): CompoundTag? = schedulesOfOrNull(owner)?.get(id)?.scheduleTag?.copy()

    fun putScheduleTag(owner: UUID, id: UUID, scheduleTag: CompoundTag, name: String? = null) {
        val schedules = schedulesOf(owner)
        val existing = schedules[id]

        val displayName = sanitizeDisplayName(
            name ?: existing?.name ?: defaultDisplayName(id),
            id
        )

        schedules[id] = StoredSchedule(
            name = displayName,
            scheduleTag = scheduleTag.copy(),
            syncTrainIdentity = existing?.syncTrainIdentity ?: false,
            syncTrainColor = existing?.syncTrainColor
        )

        setDirty()
    }

    fun getDisplayName(owner: UUID, id: UUID): String? = schedulesOfOrNull(owner)?.get(id)?.name

    fun setDisplayName(owner: UUID, id: UUID, name: String) {
        val stored = schedulesOfOrNull(owner)?.get(id) ?: return
        stored.name = sanitizeDisplayName(name, id)
        setDirty()
    }

    fun namedEntries(owner: UUID): List<ScheduleSyncEntry> {
        return schedulesOfOrNull(owner)?.map { (id, stored) ->
            ScheduleSyncEntry(id, stored.name, stored.syncTrainIdentity)
        } ?: emptyList()
    }

    fun removeSchedule(owner: UUID, id: UUID): Boolean {
        val removed = schedulesOfOrNull(owner)?.remove(id) != null
        if (removed) {
            setDirty()
        }
        return removed
    }

    fun clear(owner: UUID) {
        val schedules = schedulesOfOrNull(owner)
        if (!schedules.isNullOrEmpty()) {
            schedules.clear()
            setDirty()
        }
    }

    fun getSchedule(owner: UUID, id: UUID, registries: HolderLookup.Provider): Schedule? {
        val stored = schedulesOfOrNull(owner)?.get(id) ?: return null
        return Schedule.fromTag(registries, stored.scheduleTag.copy())
    }

    fun putSchedule(
        owner: UUID,
        id: UUID,
        schedule: Schedule,
        registries: HolderLookup.Provider,
        name: String? = null
    ) {
        putScheduleTag(owner, id, schedule.write(registries), name)
    }

    fun isTrainIdentitySyncEnabled(owner: UUID, id: UUID): Boolean =
        schedulesOfOrNull(owner)?.get(id)?.syncTrainIdentity ?: false

    fun setTrainIdentitySyncEnabled(owner: UUID, id: UUID, enabled: Boolean): Boolean {
        val stored = schedulesOfOrNull(owner)?.get(id) ?: return false

        if (stored.syncTrainIdentity != enabled) {
            stored.syncTrainIdentity = enabled
            setDirty()
        }

        return true
    }

    fun getSyncTrainColor(owner: UUID, id: UUID): Int? =
        schedulesOfOrNull(owner)?.get(id)?.syncTrainColor

    fun setSyncTrainColor(owner: UUID, id: UUID, color: Int): Boolean {
        val stored = schedulesOfOrNull(owner)?.get(id) ?: return false

        if (stored.syncTrainColor != color) {
            stored.syncTrainColor = color
            setDirty()
        }

        return true
    }

    override fun save(tag: CompoundTag, registries: HolderLookup.Provider): CompoundTag {
        val owners = ListTag()

        for ((owner, schedules) in schedulesByOwner) {
            val ownerEntry = CompoundTag()
            ownerEntry.putUUID("Owner", owner)

            val schedulesList = ListTag()

            for ((id, stored) in schedules) {
                val entry = CompoundTag()
                entry.putUUID("Id", id)
                entry.putString("Name", stored.name)
                entry.put("Schedule", stored.scheduleTag.copy())
                entry.putBoolean("SyncTrainIdentity", stored.syncTrainIdentity)
                stored.syncTrainColor?.let {
                    entry.putInt("SyncTrainColor", it)
                }
                schedulesList.add(entry)
            }

            ownerEntry.put("Schedules", schedulesList)
            owners.add(ownerEntry)
        }

        tag.put("Owners", owners)
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

            val owners = tag.getList("Owners", Tag.TAG_COMPOUND.toInt())

            for (ownerIndex in owners.indices) {
                val ownerEntry = owners.getCompound(ownerIndex)

                if (!ownerEntry.hasUUID("Owner")) {
                    continue
                }

                val owner = ownerEntry.getUUID("Owner")
                val schedules = data.schedulesOf(owner)

                val list = ownerEntry.getList("Schedules", Tag.TAG_COMPOUND.toInt())

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

                    schedules[id] = StoredSchedule(
                        name = sanitizeDisplayName(rawName, id),
                        scheduleTag = entry.getCompound("Schedule").copy(),
                        syncTrainIdentity =
                            if (entry.contains("SyncTrainIdentity", Tag.TAG_BYTE.toInt())) {
                                entry.getBoolean("SyncTrainIdentity")
                            } else {
                                entry.getBoolean("SyncTrainName")
                            },
                        syncTrainColor =
                            if (entry.contains("SyncTrainColor", Tag.TAG_INT.toInt())) {
                                entry.getInt("SyncTrainColor")
                            } else {
                                null
                            },
                    )
                }
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
