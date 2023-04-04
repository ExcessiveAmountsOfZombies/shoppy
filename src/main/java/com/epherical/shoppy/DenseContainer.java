package com.epherical.shoppy;

import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DenseContainer implements Container {

    private final int size;
    private final NonNullList<ItemStack> items;

    private List<ContainerListener> listeners;

    public DenseContainer(int size) {
        this.size = size;
        this.items = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    public DenseContainer(ItemStack... pItems) {
        this.size = pItems.length;
        this.items = NonNullList.of(ItemStack.EMPTY, pItems);
    }

    public void addListener(ContainerListener pListener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }

        this.listeners.add(pListener);
    }

    public void removeListener(ContainerListener pListener) {
        if (this.listeners != null) {
            this.listeners.remove(pListener);
        }

    }



    @Override
    public int getContainerSize() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : this.items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < this.items.size() ? this.items.get(slot) : net.minecraft.world.item.ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return null;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }

    @Override
    public void clearContent() {

    }
}
