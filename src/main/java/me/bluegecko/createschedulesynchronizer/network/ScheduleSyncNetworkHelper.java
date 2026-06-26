package me.bluegecko.createschedulesynchronizer.network;

import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import me.bluegecko.createschedulesynchronizer.sync.ScheduleSyncSavedData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ScheduleSyncNetworkHelper {
    private ScheduleSyncNetworkHelper() {
    }

    public static void sendSyncPanelState(ServerPlayer player) {
        ItemStack stack = player.getMainHandItem();

        PacketDistributor.sendToPlayer(
                player,
                new ScheduleSyncIdsPayload(
                        ScheduleSyncSavedData.get(player.serverLevel()).namedEntries()
                )
        );

        PacketDistributor.sendToPlayer(
                player,
                new ScheduleSyncCurrentIdPayload(
                        SynchronizedScheduleItem.getSyncId(stack)
                )
        );
    }
}
