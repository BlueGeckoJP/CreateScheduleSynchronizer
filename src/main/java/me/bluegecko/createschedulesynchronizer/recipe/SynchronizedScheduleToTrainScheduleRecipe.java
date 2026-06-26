package me.bluegecko.createschedulesynchronizer.recipe;

import com.simibubi.create.AllItems;
import me.bluegecko.createschedulesynchronizer.item.SynchronizedScheduleItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class SynchronizedScheduleToTrainScheduleRecipe extends ScheduleConversionRecipe {
    public SynchronizedScheduleToTrainScheduleRecipe(CraftingBookCategory category) {
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

        ItemStack result = AllItems.SCHEDULE.asStack();
        copyScheduleData(source, result);

        return result;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return AllItems.SCHEDULE.asStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SYNC_TO_TRAIN.get();
    }

    private static ItemStack findSource(CraftingInput input) {
        return findSingleSourceOnly(
                input,
                stack -> stack.getItem() instanceof SynchronizedScheduleItem && hasScheduleData(stack)
        );
    }
}
