package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import me.bluegecko.createschedulesynchronizer.network.NewScheduleSyncIdPayload;
import me.bluegecko.createschedulesynchronizer.network.SaveScheduleSyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScheduleScreen.class, remap = false)
public abstract class ScheduleScreenMixin {
    @Unique
    private static final int CSS_PANEL_WIDTH = 92;

    @Unique
    private static final int CSS_PANEL_HEIGHT = 78;

    @Unique
    private static final int CSS_BUTTON_WIDTH = 72;

    @Unique
    private static final int CSS_BUTTON_HEIGHT = 18;

    @Shadow
    private Schedule schedule;

    @Inject(method = "render", at = @At("TAIL"))
    private void css$renderSyncPanel(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick,
            CallbackInfo callback
    ) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();

        int panelX = css$panelX(screen);
        int panelY = css$panelY(screen);

        graphics.fill(
                panelX,
                panelY,
                panelX + CSS_PANEL_WIDTH,
                panelY + CSS_PANEL_HEIGHT,
                0xAA101010
        );

        graphics.renderOutline(
                panelX,
                panelY,
                CSS_PANEL_WIDTH,
                CSS_PANEL_HEIGHT,
                0xFF606060
        );

        graphics.drawString(
                minecraft.font,
                Component.literal("Sync"),
                panelX + 6,
                panelY + 6,
                0xFFE0E0E0,
                false
        );

        int buttonX = css$saveButtonX(screen);
        int buttonY = css$saveButtonY(screen);
        boolean hovered = css$isInside(
                mouseX,
                mouseY,
                buttonX,
                buttonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        );

        graphics.fill(
                buttonX,
                buttonY,
                buttonX + CSS_BUTTON_WIDTH,
                buttonY + CSS_BUTTON_HEIGHT,
                hovered ? 0xFF5A6F8F : 0xFF3E4A5C
        );

        graphics.renderOutline(
                buttonX,
                buttonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("Save"),
                buttonX + CSS_BUTTON_WIDTH / 2,
                buttonY + 5,
                0xFFFFFFFF
        );

        int newButtonX = css$newButtonX(screen);
        int newButtonY = css$newButtonY(screen);
        boolean newHovered = css$isInside(
                mouseX,
                mouseY,
                newButtonX,
                newButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        );

        graphics.fill(
                newButtonX,
                newButtonY,
                newButtonX + CSS_BUTTON_WIDTH,
                newButtonY + CSS_BUTTON_HEIGHT,
                newHovered ? 0xFF6F8F5A : 0xFF4A5C3E
        );

        graphics.renderOutline(
                newButtonX,
                newButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("New ID"),
                newButtonX + CSS_BUTTON_WIDTH / 2,
                newButtonY + 5,
                0xFFFFFFFF
        );
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void css$handleSyncPanelClick(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (button != 0) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        CompoundTag scheduleTag = schedule.write(
                Minecraft.getInstance().player.registryAccess()
        );

        int saveButtonX = css$saveButtonX(screen);
        int saveButtonY = css$saveButtonY(screen);

        if (css$isInside(
                mouseX,
                mouseY,
                saveButtonX,
                saveButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        )) {
            PacketDistributor.sendToServer(new SaveScheduleSyncPayload(scheduleTag));
            callback.setReturnValue(true);
            return;
        }

        int newButtonX = css$newButtonX(screen);
        int newButtonY = css$newButtonY(screen);

        if (css$isInside(
                mouseX,
                mouseY,
                newButtonX,
                newButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        )) {
            PacketDistributor.sendToServer(new NewScheduleSyncIdPayload(scheduleTag));
            callback.setReturnValue(true);
        }
    }

    @Unique
    private static int css$panelX(AbstractContainerScreen<?> screen) {
        return screen.getGuiLeft() + screen.getXSize() + 8;
    }

    @Unique
    private static int css$panelY(AbstractContainerScreen<?> screen) {
        return screen.getGuiTop() + 12;
    }

    @Unique
    private static int css$saveButtonX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + 10;
    }

    @Unique
    private static int css$saveButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + 26;
    }

    @Unique
    private static boolean css$isInside(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    @Unique
    private static int css$newButtonX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + 10;
    }

    @Unique
    private static int css$newButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + 50;
    }
}
