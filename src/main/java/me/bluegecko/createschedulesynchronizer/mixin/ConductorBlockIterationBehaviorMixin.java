package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.api.behaviour.interaction.ConductorBlockInteractionBehavior;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ConductorBlockInteractionBehavior.class, remap = false)
public abstract class ConductorBlockIterationBehaviorMixin {
    @Redirect(
            method = "handlePlayerInteraction",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;" + "isIn(Lnet/minecraft/world/item/ItemStack;)Z"
            )
    )
    private boolean acceptsCompatibleSchedule(
            ItemEntry<?> ignored,
            ItemStack stack
    ) {
        return ScheduleCompatibility.isSchedule(stack);
    }

    @Redirect(
            method = "handlePlayerInteraction",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/schedule/"
                            + "ScheduleRuntime;"
                            + "setSchedule("
                            + "Lcom/simibubi/create/content/trains/schedule/Schedule;"
                            + "Z)V"
            )
    )
    private void css$rememberConductorScheduleSource(
            ScheduleRuntime runtime,
            Schedule schedule,
            boolean auto,
            Player player,
            InteractionHand activeHand,
            BlockPos localPos,
            AbstractContraptionEntity contraptionEntity
    ) {
        ItemStack source = player.getItemInHand(activeHand);

        ScheduleCompatibility.rememberSource(runtime, source);
        runtime.setSchedule(schedule, auto);
    }
}
