package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import me.bluegecko.createschedulesynchronizer.compat.RenameOverlayHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ContainerEventHandler.class)
public interface ScreenCharTypedMixin {
    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void css$handleScheduleRenameOverlayCharTyped(
            char codePoint,
            int modifiers,
            CallbackInfoReturnable<Boolean> callback
    ) {
        Object self = this;

        if (!(self instanceof ScheduleScreen)) {
            return;
        }

        if (self instanceof RenameOverlayHandler handler) {
            if (handler.css$charTypedRenameOverlay(codePoint, modifiers)) {
                callback.setReturnValue(true);
            }
        }
    }
}