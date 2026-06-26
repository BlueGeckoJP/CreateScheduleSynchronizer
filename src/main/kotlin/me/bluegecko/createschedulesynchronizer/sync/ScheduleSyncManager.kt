package me.bluegecko.createschedulesynchronizer.sync

import com.simibubi.create.AllDataComponents
import com.simibubi.create.content.trains.schedule.Schedule
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack

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
    fun syncItemFromStoreOrInitialize(stack: ItemStack, level: ServerLevel): Boolean {
        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return false
        val store = ScheduleSyncSavedData.get(level)

        val storedSchedule = store.getScheduleTag(syncId)
        if (storedSchedule != null) {
            stack.set(AllDataComponents.TRAIN_SCHEDULE, storedSchedule.copy())
            return true
        }

        val localSchedule = stack.get(AllDataComponents.TRAIN_SCHEDULE)
        if (localSchedule != null) {
            store.putScheduleTag(syncId, localSchedule.copy())
            return true
        }

        return false
    }

    @JvmStatic
    fun syncItemFromStore(stack: ItemStack, level: ServerLevel): Boolean {
        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return false
        val storeSchedule = ScheduleSyncSavedData.get(level).getScheduleTag(syncId) ?: return false

        stack.set(AllDataComponents.TRAIN_SCHEDULE, storeSchedule.copy())
        return true
    }

    @JvmStatic
    fun saveMainHandScheduleToStore(player: ServerPlayer): SaveResult {
        return saveHeldScheduleToStore(player, InteractionHand.MAIN_HAND)
    }

    @JvmStatic
    fun saveHeldScheduleToStore(
        player: ServerPlayer,
        hand: InteractionHand
    ): SaveResult {
        return saveItemToStore(
            stack = player.getItemInHand(hand),
            level = player.serverLevel()
        )
    }

    @JvmStatic
    fun saveItemToStore(
        stack: ItemStack,
        level: ServerLevel
    ): SaveResult {
        if (stack.item !is SynchronizedScheduleItem) {
            return SaveResult.NOT_SYNCHRONIZED_SCHEDULE
        }

        val syncId = SynchronizedScheduleItem.getSyncId(stack) ?: return SaveResult.NOT_LINKED

        val scheduleTag = stack.get(AllDataComponents.TRAIN_SCHEDULE) ?: Schedule().write(level.registryAccess())

        ScheduleSyncSavedData.get(level).putScheduleTag(syncId, scheduleTag.copy())

        return SaveResult.SAVED
    }

    enum class SaveResult {
        NOT_SYNCHRONIZED_SCHEDULE,
        NOT_LINKED,
        SAVED
    }
}