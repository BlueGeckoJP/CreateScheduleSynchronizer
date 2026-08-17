package me.bluegecko.createschedulesynchronizer.compat.jei;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.schedule.ScheduleScreen;
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.item.ModItems;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public final class CreateScheduleSynchronizerJeiPlugin implements IModPlugin {
    private static final int JEI_BUTTON_SIZE = 20;
    private static final int JEI_BUTTON_GAP = 2;
    private static final int JEI_BORDER_MARGIN = 6;

    // The minimum width required by the JEI BookmarkOverlay: 6 + 20 + 2 + 20 + 6 = 54px
    private static final int JEI_LEFT_OVERLAY_WIDTH =
            JEI_BORDER_MARGIN * 2 + JEI_BUTTON_SIZE * 2 + JEI_BUTTON_GAP;

    private static final ResourceLocation PLUGIN_ID =
            ResourceLocation.fromNamespaceAndPath(
                    Createschedulesynchronizer.ID,
                    "jei_plugin"
            );

    private static RecipeHolder<CraftingRecipe> createTrainScheduleToSynchronizedScheduleDisplay() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(Ingredient.of(AllItems.SCHEDULE.get()));
        ingredients.add(Ingredient.of(Items.ENDER_PEARL));

        ShapelessRecipe recipe = new ShapelessRecipe(
                "",
                CraftingBookCategory.MISC,
                new ItemStack(
                        ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().get()
                ),
                ingredients
        );

        return new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(
                        Createschedulesynchronizer.ID,
                        "jei_train_schedule_to_synchronized_schedule"
                ),
                recipe
        );
    }

    private static RecipeHolder<CraftingRecipe> createSynchronizedScheduleToTrainScheduleDisplay() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(
                Ingredient.of(
                        ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().get())
        );

        ShapelessRecipe recipe = new ShapelessRecipe(
                "",
                CraftingBookCategory.MISC,
                AllItems.SCHEDULE.asStack(),
                ingredients
        );

        return new RecipeHolder<>(
                ResourceLocation.fromNamespaceAndPath(
                        Createschedulesynchronizer.ID,
                        "jei_synchronized_schedule_to_train_schedule"
                ),
                recipe
        );
    }

    private static IGuiProperties createScheduleGuiProperties(
            ScheduleScreen screen
    ) {
        int physicalLeft = screen.getGuiLeft();
        int physicalRight = physicalLeft + screen.getXSize();

        ItemStack stack = screen.getMenu().contentHolder;
        boolean synchronizedSchedule = !stack.isEmpty() && stack.getItem() instanceof SynchronizedScheduleItem;

        /*
         * Do not interfere with the normal Train Schedule
         *
         * For Synchronized Schedules, even if the actual Create UI extends further left than 54px,
         * report to JEI as if its left edge were at 54px
         * This allows JEI's buttons to be rendered on top of the Create UI
         */
        int logicalLeft = synchronizedSchedule ? Math.max(physicalLeft, JEI_LEFT_OVERLAY_WIDTH) : physicalLeft;

        /*
         * Keep the right edge aligned with the actual Create UI
         * Since only `logicalLeft` is shifted to the right,
         * reduce the logical GUI width by the same amount
         */
        int logicalWidth = Math.max(1, physicalRight - logicalLeft);

        return new ScheduleGuiProperties(
                screen.getClass(),
                logicalLeft,
                screen.getGuiTop(),
                logicalWidth,
                screen.getYSize(),
                screen.width,
                screen.height
        );
    }

    @Override
    @NotNull
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                RecipeTypes.CRAFTING,
                List.of(
                        createTrainScheduleToSynchronizedScheduleDisplay(),
                        createSynchronizedScheduleToTrainScheduleDisplay()
                )
        );
    }

    @Override
    public void registerGuiHandlers(
            IGuiHandlerRegistration registration
    ) {
        registration.addGuiScreenHandler(
                ScheduleScreen.class,
                CreateScheduleSynchronizerJeiPlugin::createScheduleGuiProperties
        );
    }

    private record ScheduleGuiProperties(
            Class<? extends Screen> screenClass,
            int guiLeft,
            int guiTop,
            int guiXSize,
            int guiYSize,
            int screenWidth,
            int screenHeight
    ) implements IGuiProperties {
    }
}
