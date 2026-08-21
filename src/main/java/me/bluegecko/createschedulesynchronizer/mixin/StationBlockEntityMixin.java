package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.logistics.depot.DepotBehaviour;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Predicate;

@Mixin(value = StationBlockEntity.class, remap = false)
public abstract class StationBlockEntityMixin {
    @Shadow
    public abstract ItemStack getAutoSchedule();

    /**
     * Allow station depots to accept synchronized train schedule
     */
    @Redirect(
            method = "addBehaviours",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;" + "onlyAccepts(Ljava/util/function/Predicate;)" + "Lcom/simibubi/create/content/logistics/depot/DepotBehaviour;"
            )
    )
    private DepotBehaviour replaceScheduleFilter(
            DepotBehaviour depot,
            Predicate<ItemStack> ignored
    ) {
        return depot.onlyAccepts(ScheduleCompatibility::isSchedule);
    }

    @Inject(method = "applyAutoSchedule", at = @At("HEAD"))
    private void css$syncBeforeStationScheduleRead(CallbackInfo callback) {
        Level level = ((BlockEntity) (Object) this).getLevel();

        if (level instanceof ServerLevel serverLevel) {
            ScheduleCompatibility.syncFromStoreIfPossible(
                    getAutoSchedule(),
                    serverLevel
            );
        }
    }

    /**
     * Allow synchronized train schedule on depots to be applied to trains
     */
    @Redirect(
            method = "applyAutoSchedule",
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
            method = "applyAutoSchedule",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/trains/schedule/"
                            + "ScheduleRuntime;"
                            + "setSchedule("
                            + "Lcom/simibubi/create/content/trains/schedule/Schedule;"
                            + "Z)V"
            )
    )
    private void css$rememberStationScheduleSource(
            ScheduleRuntime runtime,
            Schedule schedule,
            boolean auto
    ) {
        ScheduleCompatibility.rememberSource(
                runtime,
                getAutoSchedule()
        );

        runtime.setSchedule(schedule, auto);

        Level level = ((BlockEntity) (Object) this).getLevel();

        if (level instanceof ServerLevel serverLevel) {
            ScheduleCompatibility.syncTrainIdentityAfterScheduleApplied(
                    runtime,
                    serverLevel
            );
        }
    }
}
