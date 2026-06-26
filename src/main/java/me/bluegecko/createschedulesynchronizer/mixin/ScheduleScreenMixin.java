package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import me.bluegecko.createschedulesynchronizer.network.*;
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

import java.util.List;
import java.util.UUID;

@Mixin(value = ScheduleScreen.class, remap = false)
public abstract class ScheduleScreenMixin {
    @Unique
    private static final int CSS_PANEL_WIDTH = 92;

    @Unique
    private static final int CSS_PANEL_HEIGHT = 172;

    @Unique
    private static final int CSS_BUTTON_WIDTH = 72;

    @Unique
    private static final int CSS_BUTTON_HEIGHT = 18;

    @Unique
    private int css$idScrollOffset;

    @Unique
    private void css$clampIdScrollOffset() {
        int maxRows = 4;
        int maxOffset = Math.max(0, ScheduleSyncClientState.getIds().size() - maxRows);
        css$idScrollOffset = Math.clamp(css$idScrollOffset, 0, maxOffset);
    }

    @Shadow
    private Schedule schedule;

    @Shadow
    protected abstract void init();

    @Inject(method = "init", at = @At("TAIL"))
    private void css$requestSyncIds(CallbackInfo callback) {
        ScheduleSyncClientState.clear();
        css$idScrollOffset = 0;
        PacketDistributor.sendToServer(new RequestScheduleSyncIdsPayload());
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void css$applyPendingScheduleTag(CallbackInfo callback) {
        CompoundTag pending = ScheduleSyncClientState.consumePendingScheduleTag();
        if (pending == null) {
            return;
        }

        schedule = Schedule.fromTag(
                Minecraft.getInstance().player.registryAccess(),
                pending
        );

        init();
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void css$scrollSyncIdList(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY,
            CallbackInfoReturnable<Boolean> callback
    ) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);
        int maxRows = 4;
        int listHeight = maxRows * css$idRowHeight();

        if (!css$isInside(
                mouseX,
                mouseY,
                listX,
                listY,
                css$idRowWidth(),
                listHeight
        )) {
            return;
        }

        List<UUID> ids = ScheduleSyncClientState.getIds();
        int maxOffset = Math.max(0, ids.size() - maxRows);

        if (maxOffset <= 0) {
            return;
        }

        if (scrollY < 0) {
            css$idScrollOffset++;
        } else if (scrollY > 0) {
            css$idScrollOffset--;
        }

        css$idScrollOffset = Math.clamp(css$idScrollOffset, 0, maxOffset);

        callback.setReturnValue(true);
    }

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

        UUID currentId = ScheduleSyncClientState.getCurrentId();

        String currentText = currentId == null
                ? "Current: none"
                : "Current: " + currentId.toString().substring(0, 8);

        graphics.drawString(
                minecraft.font,
                Component.literal(currentText),
                panelX + 6,
                panelY + 18,
                currentId == null ? 0xFF808080 : 0xFF90D090,
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

        int unlinkButtonX = css$unlinkButtonX(screen);
        int unlinkButtonY = css$unlinkButtonY(screen);
        boolean unlinkHovered = css$isInside(
                mouseX,
                mouseY,
                unlinkButtonX,
                unlinkButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        );

        graphics.fill(
                unlinkButtonX,
                unlinkButtonY,
                unlinkButtonX + CSS_BUTTON_WIDTH,
                unlinkButtonY + CSS_BUTTON_HEIGHT,
                unlinkHovered ? 0xFF8F5A5A : 0xFF5C3E3E
        );

        graphics.renderOutline(
                unlinkButtonX,
                unlinkButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                0xFFFFFFFF
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("Unlink"),
                unlinkButtonX + CSS_BUTTON_WIDTH / 2,
                unlinkButtonY + 5,
                0xFFFFFFFF
        );

        css$clampIdScrollOffset();
        List<UUID> ids = ScheduleSyncClientState.getIds();

        graphics.drawString(
                minecraft.font,
                Component.literal("IDs"),
                panelX + 6,
                panelY + 112,
                0xFFE0E0E0,
                false
        );

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);

        int maxRows = 4;
        int visibleCount = Math.clamp(ids.size() - css$idScrollOffset, 0, maxRows);

        for (int row = 0; row < visibleCount; row++) {
            int index = css$idScrollOffset + row;
            UUID id = ids.get(index);
            int rowY = listY + row * css$idRowHeight();

            boolean selected = id.equals(currentId);
            boolean idHovered = css$isInside(
                    mouseX,
                    mouseY,
                    listX,
                    rowY,
                    css$idRowWidth(),
                    css$idRowHeight()
            );

            if (idHovered || selected) {
                graphics.fill(
                        listX - 2,
                        rowY - 1,
                        listX + css$idRowWidth(),
                        rowY + css$idRowHeight(),
                        selected ? 0x664A8F4A : 0x553E6A9E
                );
            }

            String text = id.toString().substring(0, 8);

            graphics.drawString(
                    minecraft.font,
                    Component.literal(text),
                    listX,
                    rowY,
                    selected ? 0xFF90FF90 : idHovered ? 0xFFFFFFFF : 0xFFB0B0B0,
                    false
            );
        }

        if (ids.size() > maxRows) {
            String scrollText = (css$idScrollOffset + 1) + "-" + (css$idScrollOffset + visibleCount) + "/" + ids.size();

            graphics.drawString(
                    minecraft.font,
                    Component.literal(scrollText),
                    panelX + CSS_PANEL_WIDTH - 44,
                    panelY + 112,
                    0xFF808080,
                    false
            );
        }
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

        int unlinkButtonX = css$unlinkButtonX(screen);
        int unlinkButtonY = css$unlinkButtonY(screen);

        if (css$isInside(
                mouseX,
                mouseY,
                unlinkButtonX,
                unlinkButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        )) {
            PacketDistributor.sendToServer(new UnlinkScheduleSyncIdPayload());
            callback.setReturnValue(true);
            return;
        }

        List<UUID> ids = ScheduleSyncClientState.getIds();

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);

        int maxRows = 4;
        int visibleCount = Math.clamp(ids.size() - css$idScrollOffset, 0, maxRows);

        for (int row = 0; row < visibleCount; row++) {
            int index = css$idScrollOffset + row;
            int rowY = listY + row * css$idRowHeight();

            if (!css$isInside(
                    mouseX,
                    mouseY,
                    listX,
                    rowY,
                    css$idRowWidth(),
                    css$idRowHeight()
            )) {
                continue;
            }

            UUID selectedId = ids.get(index);

            PacketDistributor.sendToServer(new LinkScheduleSyncIdPayload(selectedId));

            callback.setReturnValue(true);
            return;
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
        return css$panelY(screen) + 38;
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
        return css$panelY(screen) + 62;
    }

    @Unique
    private static int css$idListX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + 8;
    }

    @Unique
    private static int css$idListY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + 124;
    }

    @Unique
    private static int css$idRowWidth() {
        return CSS_PANEL_WIDTH - 16;
    }

    @Unique
    private static int css$idRowHeight() {
        return 10;
    }

    @Unique
    private static int css$unlinkButtonX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + 10;
    }

    @Unique
    private static int css$unlinkButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + 86;
    }
}
