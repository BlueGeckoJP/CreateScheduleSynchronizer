package me.bluegecko.createschedulesynchronizer.mixin;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.ScheduleMenu;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncClientState;
import me.bluegecko.createschedulesynchronizer.client.ScheduleSyncEntry;
import me.bluegecko.createschedulesynchronizer.compat.RenameOverlayHandler;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import me.bluegecko.createschedulesynchronizer.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ScheduleScreen.class, remap = false)
public abstract class ScheduleScreenMixin extends AbstractSimiContainerScreen<ScheduleMenu> implements RenameOverlayHandler {
    @Unique
    private static final int CSS_PANEL_WIDTH = 92;
    @Unique
    private static final int CSS_PANEL_HEIGHT = 172;
    @Unique
    private static final int CSS_BUTTON_WIDTH = 72;
    @Unique
    private static final int CSS_BUTTON_HEIGHT = 18;
    @Unique
    private static final int CSS_PANEL_GAP = 8;
    @Unique
    private static final int CSS_SCREEN_MARGIN = 4;
    @Unique
    private int css$idScrollOffset;
    @Unique
    private boolean css$renameOverlayOpen;
    @Unique
    private ScheduleSyncEntry css$renameTarget;
    @Unique
    private EditBox css$renameEditBox;
    @Shadow
    private Schedule schedule;

    protected ScheduleScreenMixin(
            ScheduleMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);
    }

    @Unique
    private static String css$ellipsize(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "..";
        int textWidth = maxWidth - font.width(ellipsis);

        if (textWidth <= 0) {
            return font.plainSubstrByWidth(ellipsis, maxWidth);
        }

        return font.plainSubstrByWidth(text, textWidth) + ellipsis;
    }

    @Unique
    private static void css$drawOverlayButton(
            GuiGraphics graphics,
            Minecraft minecraft,
            int x,
            int y,
            int width,
            int height,
            String label,
            boolean hovered
    ) {
        graphics.fill(
                x,
                y,
                x + width,
                y + height,
                hovered ? 0xFF5A6F8F : 0xFF3E4A5C
        );

        graphics.renderOutline(x, y, width, height, 0xFFFFFFFF);

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal(label),
                x + width / 2,
                y + 5,
                0xFFFFFFFF
        );
    }

    @Unique
    private static int css$panelX(AbstractContainerScreen<?> screen) {
        return screen.getGuiLeft() + screen.getXSize() + CSS_PANEL_GAP;
    }

    @Unique
    private static int css$panelY(AbstractContainerScreen<?> screen) {
        return screen.getGuiTop();
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

    @Unique
    private void css$openRenameOverlay(ScheduleSyncEntry target) {
        Minecraft minecraft = Minecraft.getInstance();

        css$renameOverlayOpen = true;
        css$renameTarget = target;

        int boxWidth = 220;
        int boxHeight = 20;
        Screen screen = this;
        int x = (screen.width - boxWidth) / 2;
        int y = (screen.height - boxHeight) / 2;

        css$renameEditBox = new EditBox(
                minecraft.font,
                x,
                y,
                boxWidth,
                boxHeight,
                Component.literal("Schedule name")
        );

        css$renameEditBox.setMaxLength(32);
        css$renameEditBox.setValue(target.name());
        css$renameEditBox.setFocused(true);
    }

    @Unique
    private void css$closeRenameOverlay() {
        css$renameOverlayOpen = false;
        css$renameTarget = null;
        css$renameEditBox = null;
    }

    @Unique
    private void css$submitRenameOverlay() {
        if (css$renameTarget == null || css$renameEditBox == null) {
            css$closeRenameOverlay();
            return;
        }

        PacketDistributor.sendToServer(
                new RenameScheduleSyncIdPayload(
                        css$renameTarget.id(),
                        css$renameEditBox.getValue()
                )
        );

        css$closeRenameOverlay();
    }

    @Unique
    private void css$clampIdScrollOffset() {
        int maxRows = 4;
        int maxOffset = Math.max(0, ScheduleSyncClientState.getEntries().size() - maxRows);
        css$idScrollOffset = Math.clamp(css$idScrollOffset, 0, maxOffset);
    }

    @Unique
    private void css$renderRenameOverlay(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = this;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 500);

        graphics.fill(0, 0, screen.width, screen.height, 0xAA000000);

        int panelWidth = 260;
        int panelHeight = 94;
        int panelX = (screen.width - panelWidth) / 2;
        int panelY = (screen.height - panelHeight) / 2;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF202020);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, 0xFFFFFFFF);

        graphics.drawString(
                minecraft.font,
                Component.literal("Rename schedule"),
                panelX + 12,
                panelY + 10,
                0xFFFFFFFF,
                false
        );

        if (css$renameEditBox != null) {
            css$renameEditBox.renderWidget(graphics, mouseX, mouseY, 0);
        }

        int renameX = panelX + 52;
        int cancelX = panelX + 138;
        int buttonY = panelY + 62;

        css$drawOverlayButton(
                graphics,
                minecraft,
                renameX,
                buttonY,
                72,
                18,
                "Rename",
                css$isInside(mouseX, mouseY, renameX, buttonY, 72, 18)
        );

        css$drawOverlayButton(
                graphics,
                minecraft,
                cancelX,
                buttonY,
                72,
                18,
                "Cancel",
                css$isInside(mouseX, mouseY, cancelX, buttonY, 72, 18)
        );

        graphics.pose().popPose();
    }

    @Unique
    private boolean css$isSyncScheduleScreen() {
        Object menu = ((AbstractContainerScreen<?>) this).getMenu();

        if (!(menu instanceof ScheduleMenu scheduleMenu)) {
            return false;
        }

        ItemStack stack = scheduleMenu.contentHolder;
        return !stack.isEmpty() && stack.getItem() instanceof SynchronizedScheduleItem;
    }

    @Inject(method = "getExtraAreas", at = @At("RETURN"), cancellable = true)
    private void css$addSyncPanelExtraArea(
            CallbackInfoReturnable<List<Rect2i>> callback
    ) {
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        AbstractContainerScreen<?> screen =
                this;

        List<Rect2i> extraAreas =
                new ArrayList<>(callback.getReturnValue());

        extraAreas.add(
                new Rect2i(
                        css$panelX(screen),
                        css$panelY(screen),
                        CSS_PANEL_WIDTH,
                        CSS_PANEL_HEIGHT
                )
        );

        callback.setReturnValue(extraAreas);
    }

    @Shadow
    protected abstract void init();

    @Shadow
    public abstract int renderScheduleEntry(GuiGraphics graphics, ScheduleEntry entry, int yOffset, int mouseX, int mouseY, float partialTicks);

    @Inject(method = "init", at = @At(
            value = "INVOKE",
            target = "Lcom/simibubi/create/foundation/gui/menu/AbstractSimiContainerScreen;init()V",
            shift = At.Shift.AFTER
    ))
    private void css$shiftScheduleScreenLeftToFitPanel(
            CallbackInfo callback
    ) {
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        AbstractContainerScreen<?> screen =
                this;

        int panelRight = leftPos + screen.getXSize() + CSS_PANEL_GAP + CSS_PANEL_WIDTH;

        int availableRight = screen.width - CSS_SCREEN_MARGIN;
        int overflow = panelRight - availableRight;

        if (overflow > 0) {
            leftPos = Math.max(CSS_SCREEN_MARGIN, leftPos - overflow);
        }
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void css$requestSyncIds(CallbackInfo callback) {
        ScheduleSyncClientState.clear();
        css$idScrollOffset = 0;
        css$closeRenameOverlay();

        if (!css$isSyncScheduleScreen()) {
            return;
        }

        PacketDistributor.sendToServer(new RequestScheduleSyncIdsPayload());
    }

    @Inject(method = "containerTick", at = @At("TAIL"))
    private void css$applyPendingScheduleTag(CallbackInfo callback) {
        if (!css$isSyncScheduleScreen()) {
            return;
        }

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
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        AbstractContainerScreen<?> screen = this;

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

        List<ScheduleSyncEntry> entries = ScheduleSyncClientState.getEntries();
        int maxOffset = Math.max(0, entries.size() - maxRows);

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
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        AbstractContainerScreen<?> screen = this;
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

        ScheduleSyncEntry currentEntry = ScheduleSyncClientState.getCurrentEntry();

        String currentScheduleText = currentEntry == null ? "none" : currentEntry.name();
        int currentTextColor = currentEntry == null ? 0xFF808080 : 0xFF90D090;
        int currentTextMaxWidth = CSS_PANEL_WIDTH - 12;

        graphics.drawString(
                minecraft.font,
                Component.literal("Current:"),
                panelX + 6,
                panelY + 18,
                currentTextColor,
                false
        );
        graphics.drawString(
                minecraft.font,
                Component.literal(css$ellipsize(
                        minecraft.font,
                        currentScheduleText,
                        currentTextMaxWidth
                )),
                panelX + 6,
                panelY + 28,
                currentTextColor,
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
        List<ScheduleSyncEntry> entries = ScheduleSyncClientState.getEntries();

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
        int visibleCount = Math.clamp(entries.size() - css$idScrollOffset, 0, maxRows);

        for (int row = 0; row < visibleCount; row++) {
            int index = css$idScrollOffset + row;
            ScheduleSyncEntry entry = entries.get(index);
            int rowY = listY + row * css$idRowHeight();

            boolean selected = entry.equals(currentEntry);
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

            String text = css$ellipsize(
                    minecraft.font,
                    entry.name(),
                    css$idRowWidth()
            );

            graphics.drawString(
                    minecraft.font,
                    Component.literal(text),
                    listX,
                    rowY,
                    selected ? 0xFF90FF90 : idHovered ? 0xFFFFFFFF : 0xFFB0B0B0,
                    false
            );
        }

        if (entries.size() > maxRows) {
            String scrollText = (css$idScrollOffset + 1) + "-" + (css$idScrollOffset + visibleCount) + "/" + entries.size();

            graphics.drawString(
                    minecraft.font,
                    Component.literal(scrollText),
                    panelX + CSS_PANEL_WIDTH - 44,
                    panelY + 112,
                    0xFF808080,
                    false
            );
        }

        if (css$renameOverlayOpen) {
            css$renderRenameOverlay(graphics, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void css$handleSyncPanelClick(
            double mouseX,
            double mouseY,
            int button,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        if (css$renameOverlayOpen) {
            Screen screen = this;

            int panelWidth = 260;
            int panelHeight = 94;
            int panelX = (screen.width - panelWidth) / 2;
            int panelY = (screen.height - panelHeight) / 2;

            int renameX = panelX + 52;
            int cancelX = panelX + 138;
            int buttonY = panelY + 62;

            if (css$renameEditBox != null && css$renameEditBox.mouseClicked(mouseX, mouseY, button)) {
                callback.setReturnValue(true);
                return;
            }

            if (button == 0 && css$isInside(mouseX, mouseY, renameX, buttonY, 72, 18)) {
                css$submitRenameOverlay();
                callback.setReturnValue(true);
                return;
            }

            if (button == 0 && css$isInside(mouseX, mouseY, cancelX, buttonY, 72, 18)) {
                css$closeRenameOverlay();
                callback.setReturnValue(true);
                return;
            }

            callback.setReturnValue(true);
            return;
        }

        if (button != 0 && button != 1) {
            return;
        }

        AbstractContainerScreen<?> screen = this;

        CompoundTag scheduleTag = schedule.write(
                Minecraft.getInstance().player.registryAccess()
        );

        int saveButtonX = css$saveButtonX(screen);
        int saveButtonY = css$saveButtonY(screen);

        if (button == 0 && css$isInside(
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

        if (button == 0 && css$isInside(
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

        if (button == 0 && css$isInside(
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

        List<ScheduleSyncEntry> entries = ScheduleSyncClientState.getEntries();

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);

        int maxRows = 4;
        int visibleCount = Math.clamp(entries.size() - css$idScrollOffset, 0, maxRows);

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

            ScheduleSyncEntry selectedEntry = entries.get(index);

            if (button == 1) {
                css$openRenameOverlay(selectedEntry);
                callback.setReturnValue(true);
                return;
            }

            if (button == 0) {
                PacketDistributor.sendToServer(new LinkScheduleSyncIdPayload(selectedEntry.id()));
                callback.setReturnValue(true);
                return;
            }
        }

    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void css$handleRenameOverlayKeyPressed(
            int keyCode,
            int scanCode,
            int modifiers,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (!css$isSyncScheduleScreen()) {
            return;
        }

        if (!css$renameOverlayOpen) {
            return;
        }

        if (keyCode == 256) { // ESC
            css$closeRenameOverlay();
            callback.setReturnValue(true);
            return;
        }

        if (keyCode == 257 || keyCode == 335) { // ENTER / NUMPAD_ENTER
            css$submitRenameOverlay();
            callback.setReturnValue(true);
            return;
        }

        if (css$renameEditBox != null && css$renameEditBox.keyPressed(keyCode, scanCode, modifiers)) {
            callback.setReturnValue(true);
            return;
        }

        callback.setReturnValue(true);
    }

    @Override
    public boolean css$charTypedRenameOverlay(char codePoint, int modifiers) {
        if (!css$isSyncScheduleScreen()) {
            return false;
        }

        if (!css$renameOverlayOpen) {
            return false;
        }

        if (css$renameEditBox != null) {
            css$renameEditBox.charTyped(codePoint, modifiers);
        }

        return true;
    }
}
