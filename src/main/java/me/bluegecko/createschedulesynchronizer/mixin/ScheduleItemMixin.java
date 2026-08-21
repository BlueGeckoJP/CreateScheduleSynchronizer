package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleItem;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScheduleItem.class, remap = false)
public abstract class ScheduleItemMixin {
    @Inject(method = "handScheduleTo", at = @At("HEAD"))
    private void css$syncBeforeEntityScheduleRead(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> callback
    ) {
        if (player.level() instanceof ServerLevel serverLevel) {
            ScheduleCompatibility.syncFromStoreIfPossible(stack, serverLevel);
        }
    }

    @Redirect(method = "handScheduleTo", at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/content/trains/schedule/ScheduleRuntime;" + "setSchedule(Lcom/simibubi/create/content/trains/schedule/Schedule;Z)V"
    ))
    private void css$rememberEntityScheduleSource(
            ScheduleRuntime runtime,
            Schedule schedule,
            boolean auto,
            ItemStack source,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        ScheduleCompatibility.rememberSource(runtime, source);
        runtime.setSchedule(schedule, auto);

        if (player.level() instanceof ServerLevel serverLevel) {
            ScheduleCompatibility.syncTrainNameAfterScheduleApplied(
                    runtime,
                    serverLevel
            );
        }
    }
}
