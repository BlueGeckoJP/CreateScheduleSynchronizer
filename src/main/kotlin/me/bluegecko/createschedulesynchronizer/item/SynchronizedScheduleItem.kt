package me.bluegecko.createschedulesynchronizer.item

import com.simibubi.create.content.trains.schedule.ScheduleItem
import me.bluegecko.createschedulesynchronizer.data.ModDataComponents
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import java.util.*

class SynchronizedScheduleItem(properties: Properties) : ScheduleItem(properties) {
    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        super.appendHoverText(stack, context, tooltip, flag)

        val syncId = getSyncId(stack)

        if (syncId != null) {
            tooltip.add(
                Component.literal("Sync ID: $syncId").withStyle(ChatFormatting.DARK_GRAY)
            )
        } else if (flag.isAdvanced) {
            tooltip.add(
                Component.literal("Sync ID: <not assigned>").withStyle(ChatFormatting.GRAY)
            )
        }
    }

    companion object {
        fun getSyncId(stack: ItemStack): UUID? {
            return stack.get(ModDataComponents.SYNC_ID.get())
        }

        fun setSyncId(stack: ItemStack, syncId: UUID) {
            stack.set(ModDataComponents.SYNC_ID.get(), syncId)
        }

        fun clearSyncId(stack: ItemStack) {
            stack.remove(ModDataComponents.SYNC_ID.get())
        }

        fun isLinked(stack: ItemStack): Boolean {
            return getSyncId(stack) != null
        }
    }
}
