package me.bluegecko.createschedulesynchronizer.item

import com.simibubi.create.content.trains.schedule.ScheduleItem
import me.bluegecko.createschedulesynchronizer.data.ModDataComponents
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncManager
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import java.util.*

class SynchronizedScheduleItem(properties: Properties) : ScheduleItem(properties) {
    override fun use(level: Level, player: Player, usedHand: InteractionHand): InteractionResultHolder<ItemStack?> {
        if (level is ServerLevel) {
            ScheduleSyncManager.syncItemFromStoreOrInitialize(
                player.getItemInHand(usedHand),
                level
            )
        }

        return super.use(level, player, usedHand)
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level

        if (level is ServerLevel) {
            ScheduleSyncManager.syncItemFromStoreOrInitialize(
                context.itemInHand,
                level
            )
        }

        return super.useOn(context)
    }

    override fun handScheduleTo(
        stack: ItemStack,
        player: Player,
        interactionTarget: LivingEntity,
        usedHand: InteractionHand,
    ): InteractionResult {
        val level = player.level()

        if (level is ServerLevel) {
            ScheduleSyncManager.syncItemFromStoreOrInitialize(stack, level)
        }

        return super.handScheduleTo(stack, player, interactionTarget, usedHand)
    }

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
        @JvmStatic
        fun getSyncId(stack: ItemStack): UUID? {
            return stack.get(ModDataComponents.SYNC_ID.get())
        }

        @JvmStatic
        fun setSyncId(stack: ItemStack, syncId: UUID) {
            stack.set(ModDataComponents.SYNC_ID.get(), syncId)
        }

        @JvmStatic
        fun clearSyncId(stack: ItemStack) {
            stack.remove(ModDataComponents.SYNC_ID.get())
        }

        @JvmStatic
        fun isLinked(stack: ItemStack): Boolean {
            return getSyncId(stack) != null
        }
    }
}
