package me.bluegecko.createschedulesynchronizer.recipe;

import com.simibubi.create.AllItems;
import me.bluegecko.createschedulesynchronizer.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TrainScheduleToSynchronizedScheduleRecipe extends ScheduleConversionRecipe {
    public TrainScheduleToSynchronizedScheduleRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !findSource(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack source = findSource(input);

        if (source.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().get());
        copyScheduleData(source, result);
        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.INSTANCE.getSYNCHRONIZED_SCHEDULE().get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.TRAIN_TO_SYNC.get();
    }

    private static ItemStack findSource(CraftingInput input) {
        return findSingleSource(
                input,
                stack -> stack.is(AllItems.SCHEDULE.get()) && hasScheduleData(stack),
                stack -> stack.is(Items.ENDER_PEARL)
        );
    }
}
