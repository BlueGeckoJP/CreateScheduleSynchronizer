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
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(value = ScheduleScreen.class, remap = false)
public abstract class ScheduleScreenMixin extends AbstractSimiContainerScreen<ScheduleMenu> implements RenameOverlayHandler {
    @Unique
    private static final int CSS_PANEL_WIDTH = 108;
    @Unique
    private static final int CSS_BUTTON_WIDTH = 88;
    @Unique
    private static final int CSS_BUTTON_HEIGHT = 18;
    @Unique
    private static final int CSS_RENAME_OVERLAY_BUTTON_WIDTH = 72;
    @Unique
    private static final int CSS_PANEL_GAP = 8;
    @Unique
    private static final int CSS_SCREEN_MARGIN = 4;
    @Unique
    private static final int CSS_BUTTON_X_OFFSET = 10;
    @Unique
    private static final int CSS_SAVE_BUTTON_Y_OFFSET = 32;
    @Unique
    private static final int CSS_NEW_BUTTON_Y_OFFSET = 56;
    @Unique
    private static final int CSS_UNLINK_BUTTON_Y_OFFSET = 80;
    @Unique
    private static final int CSS_ID_LIST_X_OFFSET = 8;
    @Unique
    private static final int CSS_ID_LIST_Y_OFFSET = 142;
    @Unique
    private static final int CSS_ID_ROW_HEIGHT = 10;
    @Unique
    private static final int CSS_VISIBLE_ID_ROWS = 4;
    @Unique
    private static final int CSS_RENAME_EDIT_BOX_WIDTH = 220;
    @Unique
    private static final int CSS_RENAME_EDIT_BOX_HEIGHT = 20;
    @Unique
    private static final int CSS_RENAME_MAX_LENGTH = 32;
    @Unique
    private static final int CSS_BUTTON_TEXT_Y_OFFSET = 5;
    @Unique
    private static final int CSS_RENAME_OVERLAY_Z = 500;
    @Unique
    private static final int CSS_RENAME_PANEL_WIDTH = 260;
    @Unique
    private static final int CSS_RENAME_PANEL_HEIGHT = 94;
    @Unique
    private static final int CSS_RENAME_TITLE_X_OFFSET = 12;
    @Unique
    private static final int CSS_RENAME_TITLE_Y_OFFSET = 10;
    @Unique
    private static final int CSS_RENAME_BUTTON_X_OFFSET = 52;
    @Unique
    private static final int CSS_CANCEL_BUTTON_X_OFFSET = 138;
    @Unique
    private static final int CSS_RENAME_BUTTON_Y_OFFSET = 62;
    @Unique
    private static final int CSS_PRIMARY_MOUSE_BUTTON = 0;
    @Unique
    private static final int CSS_SECONDARY_MOUSE_BUTTON = 1;
    @Unique
    private static final int CSS_ESCAPE_KEY = 256;
    @Unique
    private static final int CSS_ENTER_KEY = 257;
    @Unique
    private static final int CSS_NUMPAD_ENTER_KEY = 335;
    @Unique
    private static final int CSS_COLOR_WHITE = 0xFFFFFFFF;
    @Unique
    private static final int CSS_COLOR_OVERLAY_DIM = 0xAA000000;
    @Unique
    private static final int CSS_COLOR_RENAME_PANEL = 0xFF202020;
    @Unique
    private static final int CSS_COLOR_PANEL_BACKGROUND = 0xAA101010;
    @Unique
    private static final int CSS_COLOR_PANEL_BORDER = 0xFF606060;
    @Unique
    private static final int CSS_COLOR_BUTTON = 0xFF3E4A5C;
    @Unique
    private static final int CSS_COLOR_BUTTON_HOVERED = 0xFF5A6F8F;
    @Unique
    private static final int CSS_COLOR_NEW_BUTTON = 0xFF4A5C3E;
    @Unique
    private static final int CSS_COLOR_NEW_BUTTON_HOVERED = 0xFF6F8F5A;
    @Unique
    private static final int CSS_COLOR_UNLINK_BUTTON = 0xFF5C3E3E;
    @Unique
    private static final int CSS_COLOR_UNLINK_BUTTON_HOVERED = 0xFF8F5A5A;
    @Unique
    private static final int CSS_COLOR_DISABLED_TEXT = 0xFF808080;
    @Unique
    private static final int CSS_COLOR_CURRENT_TEXT = 0xFF90D090;
    @Unique
    private static final int CSS_COLOR_CURRENT_UNDERLINE = 0x8090D090;
    @Unique
    private static final int CSS_COLOR_HOVERED_ROW = 0x553E6A9E;
    @Unique
    private static final int CSS_COLOR_SELECTED_ROW = 0x664A8F4A;
    @Unique
    private static final int CSS_COLOR_SELECTED_TEXT = 0xFF90FF90;
    @Unique
    private static final int CSS_COLOR_ROW_TEXT = 0xFFB0B0B0;
    @Unique
    private static final int CSS_COLOR_SECTION_TEXT = 0xFFE0E0E0;
    @Unique
    private static final int CSS_TEXT_X_OFFSET = 6;
    @Unique
    private static final int CSS_CURRENT_TEXT_Y_OFFSET = 16;
    @Unique
    private static final int CSS_LIST_TITLE_Y_OFFSET = 130;
    @Unique
    private static final int CSS_EMPTY_LIST_X_INSET = 44;
    @Unique
    private static final int CSS_EMPTY_LIST_Y_OFFSET = 130;
    @Unique
    private static final int CSS_ROW_HORIZONTAL_PADDING = 2;
    @Unique
    private static final int CSS_ROW_VERTICAL_PADDING = 1;
    @Unique
    private static final int CSS_TRAIN_NAME_SYNC_BUTTON_Y_OFFSET = 104;
    @Unique
    private static final int CSS_COLOR_DISABLED_BUTTON = 0xFF303030;
    @Unique
    private int css$idScrollOffset;
    @Unique
    private boolean css$renameOverlayOpen;
    @Unique
    private ScheduleSyncEntry css$renameTarget;
    @Unique
    private EditBox css$renameEditBox;
    @Unique
    private UUID css$requestedTrainCountId;
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
                hovered ? CSS_COLOR_BUTTON_HOVERED : CSS_COLOR_BUTTON
        );

        graphics.renderOutline(x, y, width, height, CSS_COLOR_WHITE);

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal(label),
                x + width / 2,
                y + CSS_BUTTON_TEXT_Y_OFFSET,
                CSS_COLOR_WHITE
        );
    }

    @Unique
    private static int css$trainNameSyncButtonX(
            AbstractContainerScreen<?> screen
    ) {
        return css$panelX(screen) + CSS_BUTTON_X_OFFSET;
    }

    @Unique
    private static int css$trainNameSyncButtonY(
            AbstractContainerScreen<?> screen
    ) {
        return css$panelY(screen) + CSS_TRAIN_NAME_SYNC_BUTTON_Y_OFFSET;
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
    private static int css$panelHeight(AbstractContainerScreen<?> screen) {
        return screen.getYSize();
    }

    @Unique
    private static int css$saveButtonX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + CSS_BUTTON_X_OFFSET;
    }

    @Unique
    private static int css$saveButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + CSS_SAVE_BUTTON_Y_OFFSET;
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
        return css$panelX(screen) + CSS_BUTTON_X_OFFSET;
    }

    @Unique
    private static int css$newButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + CSS_NEW_BUTTON_Y_OFFSET;
    }

    @Unique
    private static int css$idListX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + CSS_ID_LIST_X_OFFSET;
    }

    @Unique
    private static int css$idListY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + CSS_ID_LIST_Y_OFFSET;
    }

    @Unique
    private static int css$idRowWidth() {
        return CSS_PANEL_WIDTH - CSS_ID_LIST_X_OFFSET * 2;
    }

    @Unique
    private static int css$idRowHeight() {
        return CSS_ID_ROW_HEIGHT;
    }

    @Unique
    private static int css$unlinkButtonX(AbstractContainerScreen<?> screen) {
        return css$panelX(screen) + CSS_BUTTON_X_OFFSET;
    }

    @Unique
    private static int css$unlinkButtonY(AbstractContainerScreen<?> screen) {
        return css$panelY(screen) + CSS_UNLINK_BUTTON_Y_OFFSET;
    }

    @Unique
    private void css$openRenameOverlay(ScheduleSyncEntry target) {
        Minecraft minecraft = Minecraft.getInstance();

        css$renameOverlayOpen = true;
        css$renameTarget = target;

        int boxWidth = CSS_RENAME_EDIT_BOX_WIDTH;
        int boxHeight = CSS_RENAME_EDIT_BOX_HEIGHT;
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

        css$renameEditBox.setMaxLength(CSS_RENAME_MAX_LENGTH);
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
        int maxRows = CSS_VISIBLE_ID_ROWS;
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
        graphics.pose().translate(0, 0, CSS_RENAME_OVERLAY_Z);

        graphics.fill(0, 0, screen.width, screen.height, CSS_COLOR_OVERLAY_DIM);

        int panelWidth = CSS_RENAME_PANEL_WIDTH;
        int panelHeight = CSS_RENAME_PANEL_HEIGHT;
        int panelX = (screen.width - panelWidth) / 2;
        int panelY = (screen.height - panelHeight) / 2;

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, CSS_COLOR_RENAME_PANEL);
        graphics.renderOutline(panelX, panelY, panelWidth, panelHeight, CSS_COLOR_WHITE);

        graphics.drawString(
                minecraft.font,
                Component.literal("Rename schedule"),
                panelX + CSS_RENAME_TITLE_X_OFFSET,
                panelY + CSS_RENAME_TITLE_Y_OFFSET,
                CSS_COLOR_WHITE,
                false
        );

        if (css$renameEditBox != null) {
            css$renameEditBox.renderWidget(graphics, mouseX, mouseY, 0);
        }

        int renameX = panelX + CSS_RENAME_BUTTON_X_OFFSET;
        int cancelX = panelX + CSS_CANCEL_BUTTON_X_OFFSET;
        int buttonY = panelY + CSS_RENAME_BUTTON_Y_OFFSET;

        css$drawOverlayButton(
                graphics,
                minecraft,
                renameX,
                buttonY,
                CSS_RENAME_OVERLAY_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                "Rename",
                css$isInside(mouseX, mouseY, renameX, buttonY, CSS_RENAME_OVERLAY_BUTTON_WIDTH, CSS_BUTTON_HEIGHT)
        );

        css$drawOverlayButton(
                graphics,
                minecraft,
                cancelX,
                buttonY,
                CSS_RENAME_OVERLAY_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                "Cancel",
                css$isInside(mouseX, mouseY, cancelX, buttonY, CSS_RENAME_OVERLAY_BUTTON_WIDTH, CSS_BUTTON_HEIGHT)
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
                        css$panelHeight(screen)
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
        css$requestedTrainCountId = null;
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
        int maxRows = CSS_VISIBLE_ID_ROWS;
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
                panelY + css$panelHeight(screen),
                CSS_COLOR_PANEL_BACKGROUND
        );

        graphics.renderOutline(
                panelX,
                panelY,
                CSS_PANEL_WIDTH,
                css$panelHeight(screen),
                CSS_COLOR_PANEL_BORDER
        );

        ScheduleSyncEntry currentEntry = ScheduleSyncClientState.getCurrentEntry();

        ScheduleSyncEntry hoveredEntry = null;

        String currentScheduleText = currentEntry == null ? "none" : currentEntry.name();
        int currentTextColor = currentEntry == null ? CSS_COLOR_DISABLED_TEXT : CSS_COLOR_CURRENT_TEXT;
        int currentTextMaxWidth = CSS_PANEL_WIDTH - CSS_TEXT_X_OFFSET * 2;
        String displayedCurrentText = css$ellipsize(
                minecraft.font,
                currentScheduleText,
                currentTextMaxWidth
        );

        int currentTextX = panelX + CSS_TEXT_X_OFFSET;
        int currentTextY = panelY + CSS_CURRENT_TEXT_Y_OFFSET;
        int displayedCurrentTextWidth = minecraft.font.width(displayedCurrentText);

        boolean currentTextHovered =
                currentEntry != null && css$isInside(
                        mouseX,
                        mouseY,
                        currentTextX,
                        currentTextY,
                        displayedCurrentTextWidth,
                        minecraft.font.lineHeight
                );

        if (currentTextHovered) {
            graphics.fill(
                    currentTextX - CSS_ROW_HORIZONTAL_PADDING,
                    currentTextY - CSS_ROW_VERTICAL_PADDING,
                    currentTextX + displayedCurrentTextWidth + CSS_ROW_HORIZONTAL_PADDING,
                    currentTextY + minecraft.font.lineHeight,
                    CSS_COLOR_HOVERED_ROW
            );

            hoveredEntry = currentEntry;
        }

        graphics.drawString(
                minecraft.font,
                Component.literal("Current:"),
                panelX + CSS_TEXT_X_OFFSET,
                panelY + CSS_TEXT_X_OFFSET,
                currentTextColor,
                false
        );
        graphics.drawString(
                minecraft.font,
                Component.literal(displayedCurrentText),
                currentTextX,
                currentTextY,
                currentTextHovered ? CSS_COLOR_WHITE : currentTextColor,
                false
        );

        if (currentEntry != null) {
            int underlineY = currentTextY + minecraft.font.lineHeight;

            graphics.fill(
                    currentTextX,
                    underlineY,
                    currentTextX + displayedCurrentTextWidth,
                    underlineY + 1,
                    currentTextHovered ? CSS_COLOR_WHITE : CSS_COLOR_CURRENT_UNDERLINE
            );
        }

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
                hovered ? CSS_COLOR_BUTTON_HOVERED : CSS_COLOR_BUTTON
        );

        graphics.renderOutline(
                buttonX,
                buttonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                CSS_COLOR_WHITE
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("Save"),
                buttonX + CSS_BUTTON_WIDTH / 2,
                buttonY + CSS_BUTTON_TEXT_Y_OFFSET,
                CSS_COLOR_WHITE
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
                newHovered ? CSS_COLOR_NEW_BUTTON_HOVERED : CSS_COLOR_NEW_BUTTON
        );

        graphics.renderOutline(
                newButtonX,
                newButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                CSS_COLOR_WHITE
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("New ID"),
                newButtonX + CSS_BUTTON_WIDTH / 2,
                newButtonY + CSS_BUTTON_TEXT_Y_OFFSET,
                CSS_COLOR_WHITE
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
                unlinkHovered ? CSS_COLOR_UNLINK_BUTTON_HOVERED : CSS_COLOR_UNLINK_BUTTON
        );

        graphics.renderOutline(
                unlinkButtonX,
                unlinkButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                CSS_COLOR_WHITE
        );

        graphics.drawCenteredString(
                minecraft.font,
                Component.literal("Unlink"),
                unlinkButtonX + CSS_BUTTON_WIDTH / 2,
                unlinkButtonY + CSS_BUTTON_TEXT_Y_OFFSET,
                CSS_COLOR_WHITE
        );

        int trainNameSyncButtonX = css$trainNameSyncButtonX(screen);
        int trainNameSyncButtonY = css$trainNameSyncButtonY(screen);

        boolean trainNameSyncAvailable = currentEntry != null;
        boolean trainNameSyncHovered =
                trainNameSyncAvailable
                        && css$isInside(
                        mouseX,
                        mouseY,
                        trainNameSyncButtonX,
                        trainNameSyncButtonY,
                        CSS_BUTTON_WIDTH,
                        CSS_BUTTON_HEIGHT
                );

        int trainNameSyncColor;

        if (!trainNameSyncAvailable) {
            trainNameSyncColor = CSS_COLOR_DISABLED_BUTTON;
        } else if (trainNameSyncHovered) {
            trainNameSyncColor = CSS_COLOR_BUTTON_HOVERED;
        } else {
            trainNameSyncColor = CSS_COLOR_BUTTON;
        }

        graphics.fill(
                trainNameSyncButtonX,
                trainNameSyncButtonY,
                trainNameSyncButtonX + CSS_BUTTON_WIDTH,
                trainNameSyncButtonY + CSS_BUTTON_HEIGHT,
                trainNameSyncColor
        );

        graphics.renderOutline(
                trainNameSyncButtonX,
                trainNameSyncButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT,
                trainNameSyncAvailable ? CSS_COLOR_WHITE : CSS_COLOR_DISABLED_TEXT
        );

        Component trainNameSyncText;

        if (currentEntry == null) {
            trainNameSyncText = Component.translatable(
                    "gui.createschedulesynchronizer.train_name_sync.unavailable"
            );
        } else if (currentEntry.syncTrainName()) {
            trainNameSyncText = Component.translatable(
                    "gui.createschedulesynchronizer.train_name_sync.on"
            );
        } else {
            trainNameSyncText = Component.translatable(
                    "gui.createschedulesynchronizer.train_name_sync.off"
            );
        }

        graphics.drawCenteredString(
                minecraft.font,
                trainNameSyncText,
                trainNameSyncButtonX + CSS_BUTTON_WIDTH / 2,
                trainNameSyncButtonY + CSS_BUTTON_TEXT_Y_OFFSET,
                trainNameSyncAvailable ? CSS_COLOR_WHITE : CSS_COLOR_DISABLED_TEXT
        );


        css$clampIdScrollOffset();
        List<ScheduleSyncEntry> entries = ScheduleSyncClientState.getEntries();

        graphics.drawString(
                minecraft.font,
                Component.literal("IDs"),
                panelX + CSS_TEXT_X_OFFSET,
                panelY + CSS_LIST_TITLE_Y_OFFSET,
                CSS_COLOR_SECTION_TEXT,
                false
        );

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);

        int maxRows = CSS_VISIBLE_ID_ROWS;
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

            if (idHovered) {
                hoveredEntry = entry;
            }

            if (idHovered || selected) {
                graphics.fill(
                        listX - CSS_ROW_HORIZONTAL_PADDING,
                        rowY - CSS_ROW_VERTICAL_PADDING,
                        listX + css$idRowWidth(),
                        rowY + css$idRowHeight(),
                        selected ? CSS_COLOR_SELECTED_ROW : CSS_COLOR_HOVERED_ROW
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
                    selected ? CSS_COLOR_SELECTED_TEXT : idHovered ? CSS_COLOR_WHITE : CSS_COLOR_ROW_TEXT,
                    false
            );
        }

        if (entries.size() > maxRows) {
            String scrollText = (css$idScrollOffset + 1) + "-" + (css$idScrollOffset + visibleCount) + "/" + entries.size();

            graphics.drawString(
                    minecraft.font,
                    Component.literal(scrollText),
                    panelX + CSS_PANEL_WIDTH - CSS_EMPTY_LIST_X_INSET,
                    panelY + CSS_EMPTY_LIST_Y_OFFSET,
                    CSS_COLOR_DISABLED_TEXT,
                    false
            );
        }

        if (hoveredEntry != null && !css$renameOverlayOpen) {
            UUID hoveredId = hoveredEntry.id();

            if (!hoveredId.equals(css$requestedTrainCountId)) {
                css$requestedTrainCountId = hoveredId;
                ScheduleSyncClientState.clearTrainCount(hoveredId);

                PacketDistributor.sendToServer(
                        new RequestScheduleSyncTrainCountPayload(hoveredId)
                );
            }

            Integer trainCount = ScheduleSyncClientState.getTrainCount(hoveredId);

            Component trainCountText = trainCount == null ? Component.translatable(
                    "tooltip.createschedulesynchronizer.train_count.loading"
            ) : Component.translatable(
                    "tooltip.createschedulesynchronizer.train_count",
                    trainCount
            );

            graphics.renderTooltip(
                    minecraft.font,
                    Component.literal(hoveredEntry.name()).append(Component.literal(" - ")).append(trainCountText),
                    mouseX,
                    mouseY
            );
        } else {
            css$requestedTrainCountId = null;
        }

        if (css$renameOverlayOpen) {
            css$renderRenameOverlay(graphics, mouseX, mouseY);
        }
    }

    @ModifyArg(
            method = "renderForeground",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/createmod/catnip/gui/element/GuiGameElement;of(Lnet/minecraft/world/item/ItemStack;)Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;"
            ),
            index = 0
    )
    private ItemStack css$hideLargeScheduleIcon(ItemStack stack) {
        return css$isSyncScheduleScreen() ? ItemStack.EMPTY : stack;
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

            int panelWidth = CSS_RENAME_PANEL_WIDTH;
            int panelHeight = CSS_RENAME_PANEL_HEIGHT;
            int panelX = (screen.width - panelWidth) / 2;
            int panelY = (screen.height - panelHeight) / 2;

            int renameX = panelX + CSS_RENAME_BUTTON_X_OFFSET;
            int cancelX = panelX + CSS_CANCEL_BUTTON_X_OFFSET;
            int buttonY = panelY + CSS_RENAME_BUTTON_Y_OFFSET;

            if (css$renameEditBox != null && css$renameEditBox.mouseClicked(mouseX, mouseY, button)) {
                callback.setReturnValue(true);
                return;
            }

            if (button == CSS_PRIMARY_MOUSE_BUTTON && css$isInside(
                    mouseX, mouseY, renameX, buttonY, CSS_RENAME_OVERLAY_BUTTON_WIDTH, CSS_BUTTON_HEIGHT
            )) {
                css$submitRenameOverlay();
                callback.setReturnValue(true);
                return;
            }

            if (button == CSS_PRIMARY_MOUSE_BUTTON && css$isInside(
                    mouseX, mouseY, cancelX, buttonY, CSS_RENAME_OVERLAY_BUTTON_WIDTH, CSS_BUTTON_HEIGHT
            )) {
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

        if (button == CSS_PRIMARY_MOUSE_BUTTON && css$isInside(
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

        if (button == CSS_PRIMARY_MOUSE_BUTTON && css$isInside(
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

        if (button == CSS_PRIMARY_MOUSE_BUTTON && css$isInside(
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

        int trainNameSyncButtonX = css$trainNameSyncButtonX(screen);
        int trainNameSyncButtonY = css$trainNameSyncButtonY(screen);

        ScheduleSyncEntry currentEntry =
                ScheduleSyncClientState.getCurrentEntry();

        if (button == CSS_PRIMARY_MOUSE_BUTTON
                && currentEntry != null
                && css$isInside(
                mouseX,
                mouseY,
                trainNameSyncButtonX,
                trainNameSyncButtonY,
                CSS_BUTTON_WIDTH,
                CSS_BUTTON_HEIGHT
        )) {
            PacketDistributor.sendToServer(
                    new ToggleTrainNameSyncPayload(
                            currentEntry.id(),
                            !currentEntry.syncTrainName()
                    )
            );

            callback.setReturnValue(true);
            return;
        }

        List<ScheduleSyncEntry> entries = ScheduleSyncClientState.getEntries();

        int listX = css$idListX(screen);
        int listY = css$idListY(screen);

        int maxRows = CSS_VISIBLE_ID_ROWS;
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

            if (button == CSS_SECONDARY_MOUSE_BUTTON) {
                css$openRenameOverlay(selectedEntry);
                callback.setReturnValue(true);
                return;
            }

            if (button == CSS_PRIMARY_MOUSE_BUTTON) {
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

        if (keyCode == CSS_ESCAPE_KEY) { // ESC
            css$closeRenameOverlay();
            callback.setReturnValue(true);
            return;
        }

        if (keyCode == CSS_ENTER_KEY || keyCode == CSS_NUMPAD_ENTER_KEY) { // ENTER / NUMPAD_ENTER
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
