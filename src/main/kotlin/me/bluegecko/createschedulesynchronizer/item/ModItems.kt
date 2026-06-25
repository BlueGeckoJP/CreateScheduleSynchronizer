package me.bluegecko.createschedulesynchronizer.item

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

object ModItems {
    val REGISTRY = DeferredRegister.createItems(Createschedulesynchronizer.ID)

    val SYNCHRONIZED_SCHEDULE: DeferredItem<SynchronizedScheduleItem> =
        REGISTRY.registerItem("synchronized_schedule") { properties ->
            SynchronizedScheduleItem(properties)
        }
}
