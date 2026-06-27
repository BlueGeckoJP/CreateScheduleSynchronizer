package me.bluegecko.createschedulesynchronizer.data

import com.mojang.serialization.Codec
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer
import net.minecraft.core.UUIDUtil
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.*

object ModDataComponents {
    val REGISTRY: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Createschedulesynchronizer.ID)

    private val UUID_STREAM_CODEC: StreamCodec<in RegistryFriendlyByteBuf, UUID> =
        UUIDUtil.STREAM_CODEC as StreamCodec<in RegistryFriendlyByteBuf, UUID>

    val SYNC_ID: DeferredHolder<DataComponentType<*>, DataComponentType<UUID>> =
        REGISTRY.registerComponentType("sync_id") { builder: DataComponentType.Builder<UUID> ->
            builder.persistent(UUIDUtil.STRING_CODEC).networkSynchronized(UUID_STREAM_CODEC)
        }

    val SYNC_NAME: DeferredHolder<DataComponentType<*>, DataComponentType<String>> =
        REGISTRY.registerComponentType("sync_name") { builder: DataComponentType.Builder<String> ->
            builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8)
        }

    val SYNC_OWNER = REGISTRY.registerComponentType("sync_owner") { builder ->
        builder.persistent(UUIDUtil.STRING_CODEC).networkSynchronized(UUID_STREAM_CODEC)
    }
}