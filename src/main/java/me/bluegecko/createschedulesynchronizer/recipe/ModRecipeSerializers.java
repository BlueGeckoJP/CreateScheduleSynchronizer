package me.bluegecko.createschedulesynchronizer.recipe;

import me.bluegecko.createschedulesynchronizer.Createschedulesynchronizer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> REGISTRY =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Createschedulesynchronizer.ID);

    public static final DeferredHolder<RecipeSerializer<?>,
            SimpleCraftingRecipeSerializer<TrainScheduleToSynchronizedScheduleRecipe>> TRAIN_TO_SYNC =
            REGISTRY.register(
                    "crafting_special_train_schedule_to_synchronized_schedule",
                    () -> new SimpleCraftingRecipeSerializer<>(TrainScheduleToSynchronizedScheduleRecipe::new)
            );

    public static final DeferredHolder<RecipeSerializer<?>,
            SimpleCraftingRecipeSerializer<SynchronizedScheduleToTrainScheduleRecipe>> SYNC_TO_TRAIN =
            REGISTRY.register(
                    "crafting_special_synchronized_schedule_to_train_schedule",
                    () -> new SimpleCraftingRecipeSerializer<>(SynchronizedScheduleToTrainScheduleRecipe::new)
            );

    private ModRecipeSerializers() {
    }
}
