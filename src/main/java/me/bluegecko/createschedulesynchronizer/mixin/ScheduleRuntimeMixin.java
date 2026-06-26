package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.bluegecko.createschedulesynchronizer.compat.RunningTrainScheduleSync;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleSourceTracker;
import me.bluegecko.createschedulesynchronizer.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(value = ScheduleRuntime.class, remap = false)
public abstract class ScheduleRuntimeMixin implements ScheduleSourceTracker {
    @Unique
    private static final String CSS_SOURCE_KEY = "CreateScheduleSynchronizerSynchronized";

    @Unique
    private static final String CSS_SYNC_ID_KEY = "CreateScheduleSynchronizerSyncId";

    @Unique
    private static final String CSS_PENDING_SCHEDULE_KEY = "CreateScheduleSynchronizerPendingSchedule";

    @Unique
    private boolean css$synchronizedSchedule;

    @Unique
    private UUID css$syncId;

    @Unique
    private CompoundTag css$pendingScheduleUpdate;

    @Override
    public void css$setScheduleSource(ItemStack source) {
        css$synchronizedSchedule = ScheduleCompatibility.isSynchronizedSchedule(source);
        css$syncId = css$synchronizedSchedule ? ScheduleCompatibility.getSyncId(source) : null;
    }

    @Override
    public void css$setSynchronizedScheduleSource(UUID syncId) {
        css$synchronizedSchedule = syncId != null;
        css$syncId = syncId;
    }

    @Override
    public boolean css$isSynchronizedSchedule() {
        return css$synchronizedSchedule;
    }

    @Override
    public UUID css$getScheduleSyncId() {
        return css$syncId;
    }

    @Override
    public void css$queueScheduleUpdate(CompoundTag scheduleTag) {
        css$pendingScheduleUpdate = scheduleTag.copy();
    }

    @Override
    public boolean css$hasPendingScheduleUpdate() {
        return css$pendingScheduleUpdate != null;
    }

    /**
     * When removing a Schedule from a train, return it as a synchronized item
     * if its source provider is the synchronized system.
     * Also restore the sync_id to the returned ItemStack at this point.
     */
    @Redirect(
            method = "returnSchedule",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;" + "asStack()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack css$createReturnedScheduleStack(ItemEntry<?> originalScheduleEntry) {
        ItemStack returnedStack = css$synchronizedSchedule ? ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().toStack() : originalScheduleEntry.asStack();

        if (css$synchronizedSchedule && css$syncId != null) {
            ScheduleCompatibility.setSyncId(returnedStack, css$syncId);
        }

        return returnedStack;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void css$applyPendingScheduleWhenStopped(
            Level level,
            CallbackInfo callback
    ) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (css$pendingScheduleUpdate == null) {
            return;
        }

        if (!css$synchronizedSchedule || css$syncId == null) {
            css$pendingScheduleUpdate = null;
            return;
        }

        ScheduleRuntime runtime = (ScheduleRuntime) (Object) this;

        if (runtime.state == ScheduleRuntime.State.IN_TRANSIT) {
            return;
        }

        CompoundTag pending = css$pendingScheduleUpdate.copy();
        css$pendingScheduleUpdate = null;

        RunningTrainScheduleSync.applyNow(
                serverLevel.registryAccess(),
                runtime,
                this,
                css$syncId,
                pending
        );

        Create.RAILWAYS.sided(serverLevel).markTracksDirty();
    }

    /**
     * When saving a train, save whether the source provider was the synchronized system
     * and store the sync_id.
     */
    @Inject(method = "write", at = @At("RETURN"))
    private void css$writeSource(
            HolderLookup.Provider registries,
            CallbackInfoReturnable<CompoundTag> callback
    ) {
        CompoundTag tag = callback.getReturnValue();

        if (css$synchronizedSchedule) {
            tag.putBoolean(CSS_SOURCE_KEY, true);

            if (css$syncId != null) {
                tag.putUUID(CSS_SYNC_ID_KEY, css$syncId);
            }
        }

        if (css$pendingScheduleUpdate != null) {
            tag.put(CSS_PENDING_SCHEDULE_KEY, css$pendingScheduleUpdate.copy());
        }
    }

    /**
     * Restore the sync_id to the train runtime when loading the world.
     */
    @Inject(method = "read", at = @At("RETURN"))
    private void css$readSource(
            HolderLookup.Provider registries,
            CompoundTag tag,
            CallbackInfo callback
    ) {
        css$synchronizedSchedule = tag.getBoolean(CSS_SOURCE_KEY);
        css$syncId = css$synchronizedSchedule && tag.hasUUID(CSS_SYNC_ID_KEY) ? tag.getUUID(CSS_SYNC_ID_KEY) : null;

        css$pendingScheduleUpdate = tag.contains(CSS_PENDING_SCHEDULE_KEY, Tag.TAG_COMPOUND) ? tag.getCompound(CSS_PENDING_SCHEDULE_KEY).copy() : null;
    }

    /**
     * Do not leave stale source provider information after discarding the Schedule.
     */
    @Inject(method = "discardSchedule", at = @At("RETURN"))
    private void css$clearSource(CallbackInfo callback) {
        css$synchronizedSchedule = false;
        css$syncId = null;
        css$pendingScheduleUpdate = null;
    }
}
