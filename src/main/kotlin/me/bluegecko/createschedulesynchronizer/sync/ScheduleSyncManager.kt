package me.bluegecko.createschedulesynchronizer.sync

import com.simibubi.create.AllDataComponents
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem
import net.minecraft.server.level.ServerLevel
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
}