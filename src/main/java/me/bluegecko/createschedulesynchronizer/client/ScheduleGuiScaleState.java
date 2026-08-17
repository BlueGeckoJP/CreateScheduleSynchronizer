package me.bluegecko.createschedulesynchronizer.client;

import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public final class ScheduleGuiScaleState {
    // JEI's left area: 6 + 20 + 2 + 20 + 6 = 54px
    private static final int JEI_LEFT_WIDTH = 54;

    private static final int CREATE_SCHEDULE_WIDTH = 256;
    private static final int SYNC_PANEL_GAP = 8;
    private static final int SYNC_PANEL_WIDTH = 92;
    private static final int RIGHT_MARGIN = 4;

    public static final int REQUIRED_GUI_WIDTH =
            JEI_LEFT_WIDTH + CREATE_SCHEDULE_WIDTH + SYNC_PANEL_GAP + SYNC_PANEL_WIDTH + RIGHT_MARGIN;

    private static boolean synchronizedScheduleOpen;

    private ScheduleGuiScaleState() {
    }

    public static boolean isSynchronizedScheduleOpen() {
        return synchronizedScheduleOpen;
    }

    public static void setScreen(Screen screen) {
        if (!(screen instanceof ScheduleScreen scheduleScreen)) {
            synchronizedScheduleOpen = false;
            return;
        }

        ItemStack stack =
                scheduleScreen.getMenu().contentHolder;

        synchronizedScheduleOpen = !stack.isEmpty() && stack.getItem() instanceof SynchronizedScheduleItem;
    }
}
