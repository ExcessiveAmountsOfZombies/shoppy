package com.epherical.shoppy;

import net.minecraft.world.item.ItemStack;

public class DenseItem {

    public static final DenseItem EMPTY = new DenseItem(ItemStack.EMPTY, 0);

    protected ItemStack singularItem;
    protected int itemCount;

    public DenseItem(ItemStack stack, int itemCount) {
        this.singularItem = stack;
        this.itemCount = itemCount;
    }


    public ItemStack getSingularItem() {
        return singularItem;
    }

    public DenseItem setSingularItem(ItemStack singularItem) {
        this.singularItem = singularItem;
        return this;
    }

    public int getItemCount() {
        return itemCount;
    }

    public DenseItem setItemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }
}
