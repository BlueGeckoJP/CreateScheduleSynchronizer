package me.bluegecko.createschedulesynchronizer.recipe;

import com.simibubi.create.AllDataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;

import java.util.function.Predicate;

public abstract class ScheduleConversionRecipe extends CustomRecipe {
    protected ScheduleConversionRecipe(CraftingBookCategory category) {
        super(category);
    }

    protected static ItemStack findSingleSource(
            CraftingInput input,
            Predicate<ItemStack> sourcePredicate,
            Predicate<ItemStack> extraPredicate
    ) {
        ItemStack source = ItemStack.EMPTY;
        int sourceCount = 0;
        int extraCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (sourcePredicate.test(stack)) {
                source = stack;
                sourceCount++;
                continue;
            }

            if (extraPredicate.test(stack)) {
                extraCount++;
                continue;
            }

            return ItemStack.EMPTY;
        }

        return sourceCount == 1 && extraCount == 1 ? source : ItemStack.EMPTY;
    }

    protected static ItemStack findSingleSourceOnly(
            CraftingInput input,
            Predicate<ItemStack> sourcePredicate
    ) {
        ItemStack source = ItemStack.EMPTY;
        int sourceCount = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (sourcePredicate.test(stack)) {
                source = stack;
                sourceCount++;
                continue;
            }

            return ItemStack.EMPTY;
        }

        return sourceCount == 1 ? source : ItemStack.EMPTY;
    }

    protected static boolean hasScheduleData(ItemStack stack) {
        return stack.has(AllDataComponents.TRAIN_SCHEDULE);
    }

    protected static void copyScheduleData(ItemStack source, ItemStack result) {
        CompoundTag scheduleTag = source.get(AllDataComponents.TRAIN_SCHEDULE);

        if (scheduleTag != null) {
            result.set(
                    AllDataComponents.TRAIN_SCHEDULE,
                    scheduleTag.copy()
            );
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1;
    }
}
