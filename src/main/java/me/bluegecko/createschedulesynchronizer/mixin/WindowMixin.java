package me.bluegecko.createschedulesynchronizer.mixin;

import com.mojang.blaze3d.platform.Window;
import me.bluegecko.createschedulesynchronizer.client.ScheduleGuiScaleState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.awt.*;

@Mixin(Window.class)
public abstract class WindowMixin {
    // Change Minecraft's standard minimum GUI width of 320px to 414px only while the Synchronized Schedule is displayed
    @ModifyConstant(
            method = "calculateScale",
            constant = @Constant(intValue = 320)
    )
    private int css$includeSyncScheduleInGuiScale(
            int originalMinimumWidth
    ) {
        if (ScheduleGuiScaleState.isSynchronizedScheduleOpen()) {
            return ScheduleGuiScaleState.REQUIRED_GUI_WIDTH;
        }

        return originalMinimumWidth;
    }
}
