package me.bluegecko.createschedulesynchronizer.item

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

object ModCreativeTabs {
    val REGISTRY: DeferredRegister<CreativeModeTab> =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Createschedulesynchronizer.ID)

    val MAIN: DeferredHolder<CreativeModeTab, CreativeModeTab> =
        REGISTRY.register("main", Supplier {
            CreativeModeTab.builder().title(Component.translatable("itemGroup.createschedulesynchronizer.main"))
                .icon { ModItems.SYNCHRONIZED_SCHEDULE.get().defaultInstance }.displayItems { _, output ->
                    output.accept(
                        ModItems.SYNCHRONIZED_SCHEDULE.get()
                    )
                }.build()
        })
}
