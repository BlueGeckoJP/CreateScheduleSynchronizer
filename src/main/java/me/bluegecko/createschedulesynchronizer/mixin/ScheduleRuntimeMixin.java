package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.tterrag.registrate.util.entry.ItemEntry;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleCompatibility;
import me.bluegecko.createschedulesynchronizer.compat.ScheduleSourceTracker;
import me.bluegecko.createschedulesynchronizer.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScheduleRuntime.class, remap = false)
public abstract class ScheduleRuntimeMixin implements ScheduleSourceTracker {
    @Unique
    private static final String CSS_SOURCE_KEY = "CreateScheduleSynchronizerSynchronized";

    @Unique
    private boolean css$synchronizedSchedule;

    @Override
    public void css$setScheduleSource(ItemStack source) {
        css$synchronizedSchedule = ScheduleCompatibility.isSynchronizedSchedule(source);
    }

    @Override
    public boolean css$isSynchronizedSchedule() {
        return css$synchronizedSchedule;
    }

    /**
     * If it originates from original, generate an original train schedule;
     * if it originates from the synchronized, generate a synchronized train schedule.
     */
    @Redirect(
            method = "returnSchedule",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/entry/ItemEntry;" + "asStack()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack createSynchronizedSchedule(ItemEntry<?> ignored) {
        if (css$synchronizedSchedule) {
            return ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().toStack();
        }

        return ignored.asStack();
    }

    /**
     * Save the source provider to the train save NBT.
     */
    @Inject(method = "write", at = @At("RETURN"))
    private void css$writeSource(
            HolderLookup.Provider registries,
            CallbackInfoReturnable<CompoundTag> callback
    ) {
        callback.getReturnValue().putBoolean(CSS_SOURCE_KEY, css$synchronizedSchedule);
    }

    /**
     * Restore the source provider when loading the train.
     */
    @Inject(method = "read", at = @At("RETURN"))
    private void css$readSource(
            HolderLookup.Provider registries,
            CompoundTag tag,
            CallbackInfo callback
    ) {
        css$synchronizedSchedule = tag.getBoolean(CSS_SOURCE_KEY);
    }

    /**
     * Do not keep stale source provider information after the Schedule is discarded.
     */
    @Inject(method = "discardSchedule", at = @At("RETURN"))
    private void css$clearSource(CallbackInfo callback) {
        css$synchronizedSchedule = false;
    }
}
