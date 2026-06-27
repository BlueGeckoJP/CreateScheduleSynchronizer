package me.bluegecko.createschedulesynchronizer.sync

import com.simibubi.create.AllDataComponents
import com.simibubi.create.content.trains.schedule.Schedule
import me.bluegecko.createschedulesynchronizer.compat.RunningTrainScheduleSync
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import java.util.*

object ScheduleSyncManager {
    /**
     * Called immediately before open/use/apply.
     *
     * - If SavedData has the canonical copy:
     *   apply the canonical Schedule to the ItemStack.
     *
     * - If SavedData does not have a canonical copy yet:
     *   register the Schedule on the ItemStack as the initial canonical copy, if present.
     *
     * - If there is no sync_id:
     *   do nothing.
     */
    @JvmStatic
    fun syncItemFromStoreOrInitialize(stack: ItemStack, level: ServerLevel, owner: UUID): Boolean {
        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return false
        val store = ScheduleSyncSavedData.get(level)

        SynchronizedScheduleItem.setSyncOwner(stack, owner)

        val storedSchedule = store.getScheduleTag(owner, syncId)
        if (storedSchedule != null) {
            stack.set(AllDataComponents.TRAIN_SCHEDULE, storedSchedule.copy())
            return true
        }

        val localSchedule = stack.get(AllDataComponents.TRAIN_SCHEDULE)
        if (localSchedule != null) {
            store.putScheduleTag(owner, syncId, localSchedule.copy())
            return true
        }

        return false
    }

    @JvmStatic
    fun syncItemFromStore(stack: ItemStack, level: ServerLevel): Boolean {
        val owner = SynchronizedScheduleItem.getSyncOwner(stack) ?: return false
        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return false

        val storeSchedule = ScheduleSyncSavedData.get(level).getScheduleTag(owner, syncId) ?: return false

        stack.set(AllDataComponents.TRAIN_SCHEDULE, storeSchedule.copy())
        return true
    }

    @JvmStatic
    fun saveMainHandScheduleToStore(player: ServerPlayer, scheduleTag: CompoundTag): SaveResult {
        return saveItemScheduleTagToStore(
            stack = player.mainHandItem,
            level = player.serverLevel(),
            owner = player.uuid,
            scheduleTag = scheduleTag
        )
    }

    @JvmStatic
    fun saveItemScheduleTagToStore(
        stack: ItemStack,
        level: ServerLevel,
        owner: UUID,
        scheduleTag: CompoundTag
    ): SaveResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return SaveResult.NOT_SYNCHRONIZED_SCHEDULE
        }

        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return SaveResult.NOT_LINKED

        SynchronizedScheduleItem.setSyncOwner(stack, owner)
        stack.set(AllDataComponents.TRAIN_SCHEDULE, scheduleTag.copy())
        ScheduleSyncSavedData.get(level).putScheduleTag(owner, syncId, scheduleTag.copy())

        RunningTrainScheduleSync.applyToLinkedTrains(
            level,
            owner,
            syncId,
            scheduleTag.copy()
        )

        return SaveResult.SAVED
    }

    @JvmStatic
    fun saveItemToStore(
        stack: ItemStack,
        level: ServerLevel,
        owner: UUID
    ): SaveResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return SaveResult.NOT_SYNCHRONIZED_SCHEDULE
        }

        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return SaveResult.NOT_LINKED

        val scheduleTag = stack.get(AllDataComponents.TRAIN_SCHEDULE) ?: Schedule().write(level.registryAccess())

        SynchronizedScheduleItem.setSyncOwner(stack, owner)
        ScheduleSyncSavedData.get(level).putScheduleTag(owner, syncId, scheduleTag.copy())

        RunningTrainScheduleSync.applyToLinkedTrains(
            level,
            owner,
            syncId,
            scheduleTag.copy()
        )

        return SaveResult.SAVED
    }

    enum class SaveResult {
        NOT_SYNCHRONIZED_SCHEDULE,
        NOT_LINKED,
        SAVED
    }


    @JvmStatic
    fun createNewSyncIdForMainHand(
        player: ServerPlayer,
        scheduleTag: CompoundTag,
    ): NewIdResult {
        return createNewSyncIdForItem(
            stack = player.mainHandItem,
            level = player.serverLevel(),
            owner = player.uuid,
            scheduleTag = scheduleTag
        )
    }

    @JvmStatic
    fun createNewSyncIdForItem(
        stack: ItemStack,
        level: ServerLevel,
        owner: UUID,
        scheduleTag: CompoundTag
    ): NewIdResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return NewIdResult(null, NewIdStatus.NOT_SYNCHRONIZED_SCHEDULE)
        }

        val syncId = UUID.randomUUID()
        val store = ScheduleSyncSavedData.get(level)

        store.putScheduleTag(owner, syncId, scheduleTag.copy())

        val displayName = store.getDisplayName(owner, syncId) ?: "Schedule ${syncId.toString().substring(0, 8)}"

        SynchronizedScheduleItem.setSyncOwner(stack, owner)
        SynchronizedScheduleItem.setSyncName(stack, displayName)
        SynchronizedScheduleItem.setSyncId(stack, syncId)
        stack.set(AllDataComponents.TRAIN_SCHEDULE, scheduleTag.copy())

        return NewIdResult(syncId, NewIdStatus.CREATED)
    }

    data class NewIdResult(
        val syncId: UUID?,
        val status: NewIdStatus
    )

    enum class NewIdStatus {
        CREATED,
        NOT_SYNCHRONIZED_SCHEDULE,
    }

    @JvmStatic
    fun linkMainHandToScheduleId(
        player: ServerPlayer,
        syncId: UUID,
    ): LinkResult {
        return linkItemToScheduleId(
            stack = player.mainHandItem,
            level = player.serverLevel(),
            owner = player.uuid,
            syncId = syncId,
        )
    }

    @JvmStatic
    fun linkItemToScheduleId(
        stack: ItemStack,
        level: ServerLevel,
        owner: UUID,
        syncId: UUID,
    ): LinkResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return LinkResult.NOT_SYNCHRONIZED_SCHEDULE
        }

        val store = ScheduleSyncSavedData.get(level)

        val storedSchedule = store.getScheduleTag(owner, syncId) ?: return LinkResult.NOT_FOUND

        val displayName = store.getDisplayName(owner, syncId) ?: "Schedule ${syncId.toString().substring(0, 8)}"

        SynchronizedScheduleItem.setSyncOwner(stack, owner)
        SynchronizedScheduleItem.setSyncId(stack, syncId)
        SynchronizedScheduleItem.setSyncName(stack, displayName)
        stack.set(AllDataComponents.TRAIN_SCHEDULE, storedSchedule.copy())

        return LinkResult.LINKED
    }

    enum class LinkResult {
        LINKED,
        NOT_FOUND,
        NOT_SYNCHRONIZED_SCHEDULE,
    }

    @JvmStatic
    fun linkMainHandToScheduleIdAndGetTag(
        player: ServerPlayer,
        syncId: UUID,
    ): LinkWithTagResult {
        return linkItemToScheduleIdAndGetTag(
            stack = player.mainHandItem,
            level = player.serverLevel(),
            owner = player.uuid,
            syncId = syncId,
        )
    }

    @JvmStatic
    fun linkItemToScheduleIdAndGetTag(
        stack: ItemStack,
        level: ServerLevel,
        owner: UUID,
        syncId: UUID,
    ): LinkWithTagResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return LinkWithTagResult(LinkResult.NOT_SYNCHRONIZED_SCHEDULE, null)
        }

        val store = ScheduleSyncSavedData.get(level)

        val storedSchedule = store.getScheduleTag(owner, syncId) ?: return LinkWithTagResult(
            LinkResult.NOT_FOUND, null
        )

        val displayName = store.getDisplayName(owner, syncId) ?: "Schedule ${syncId.toString().substring(0, 8)}"

        SynchronizedScheduleItem.setSyncOwner(stack, owner)
        SynchronizedScheduleItem.setSyncId(stack, syncId)
        SynchronizedScheduleItem.setSyncName(stack, displayName)
        stack.set(AllDataComponents.TRAIN_SCHEDULE, storedSchedule.copy())

        return LinkWithTagResult(LinkResult.LINKED, storedSchedule.copy())
    }

    data class LinkWithTagResult(
        val result: LinkResult,
        val scheduleTag: CompoundTag?
    )

    @JvmStatic
    fun unlinkMainHand(player: ServerPlayer): UnlinkResult {
        return unlinkItem(player.mainHandItem)
    }

    @JvmStatic
    fun unlinkItem(stack: ItemStack): UnlinkResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return UnlinkResult.NOT_SYNCHRONIZED_SCHEDULE
        }

        SynchronizedScheduleItem.clearSyncId(stack)
        stack.remove(AllDataComponents.TRAIN_SCHEDULE)

        return UnlinkResult.UNLINKED
    }

    enum class UnlinkResult {
        NOT_SYNCHRONIZED_SCHEDULE,
        UNLINKED,
    }
}