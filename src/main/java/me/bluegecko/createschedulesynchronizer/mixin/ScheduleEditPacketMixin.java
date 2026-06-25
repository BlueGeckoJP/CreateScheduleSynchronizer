package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.ScheduleEditPacket;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ScheduleEditPacket.class, remap = false)
public abstract class ScheduleEditPacketMixin {
    @Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/util/entry/ItemEntry;" + "isIn(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean acceptsCompatibleSchedule(
            ItemEntry<?> ignored,
            ItemStack stack
    ) {
        return ScheduleCompatibility.isSchedule(stack);
    }
}