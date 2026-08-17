package me.bluegecko.createschedulesynchronizer.mixin;

import com.mojang.blaze3d.platform.Window;
import me.bluegecko.createschedulesynchronizer.client.ScheduleGuiScaleState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void css$updateScheduleGuiScale(
            @Nullable Screen nextScreen,
            CallbackInfo callback
    ) {
        Minecraft minecraft = (Minecraft) (Object) this;

        // Set the state before `Window.calculateScale()` is called
        ScheduleGuiScaleState.setScreen(nextScreen);

        Window window = minecraft.getWindow();

        int calculatedScale = window.calculateScale(
                minecraft.options.guiScale().get(),
                minecraft.isEnforceUnicode()
        );

        if (Double.compare(
                window.getGuiScale(),
                calculatedScale
        ) == 0) {
            return;
        }

        window.setGuiScale(calculatedScale);

        /*
         * Notify NeoForge's GUI layer of the new logical resolution as well
         * The `Screen` itself will be initialized with the new size after this injection
         */
        ClientHooks.resizeGuiLayers(
                minecraft,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight()
        );
    }
}
