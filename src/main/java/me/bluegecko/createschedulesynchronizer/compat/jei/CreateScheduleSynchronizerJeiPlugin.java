package me.bluegecko.createschedulesynchronizer.compat.jei;

import com.simibubi.create.AllItems;
import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import me.bluegecko.createschedulesynchronizer.item.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@JeiPlugin
public final class CreateScheduleSynchronizerJeiPlugin implements IModPlugin {
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
}
