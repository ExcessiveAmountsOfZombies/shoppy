package com.epherical.shoppy;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractShoppyMenu {

    public static BarteringMenu realContainer(int pContainerId, Inventory playerInventory, Container container, ContainerData containerData) {
        return new BarteringMenu(ShoppyMod.BARTERING_MENU, pContainerId, playerInventory, container, containerData);
    }

    protected BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory) {
        this(pMenuType, pContainerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(2));
    }

    protected BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container, ContainerData data) {
        super(pMenuType, pContainerId, playerInventory, data);
        this.addSlot(new Slot(container, 0, 19, 32) {
            @Override
            public boolean mayPickup(Player $$0) {

                return true;
            }

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return true;
            }
        });
        this.addSlot(new Slot(container, 1, 143, 32) {
            @Override
            public boolean mayPickup(Player $$0) {
                return true;
            }

            @Override
            public boolean mayPlace(ItemStack $$0) {
                return true;
            }
        });

        for(int rows = 0; rows < 3; ++rows) {
            for(int slots = 0; slots < 9; ++slots) {
                this.addSlot(new Slot(playerInventory, slots + rows * 9 + 9, 8 + slots * 18, 65 + rows * 18));
            }
        }

        for(int slots = 0; slots < 9; ++slots) {
            this.addSlot(new Slot(playerInventory, slots, 8 + slots * 18, 123));
        }
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack item = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(pIndex);
        if (clickedSlot.hasItem()) {
            ItemStack clickedItem = clickedSlot.getItem();
            item = clickedSlot.getItem().copy();
            if (pIndex == 1 || pIndex == 0) {
                if (!this.moveItemStackTo(clickedItem, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }

            } else {
                if (!this.moveItemStackTo(clickedItem, 0, 1, false)) {
                    return ItemStack.EMPTY;
                } else if (!this.moveItemStackTo(clickedItem, 1, 2, false)) {
                    return ItemStack.EMPTY;
                } else if (pIndex >= 2 && pIndex < 29) {
                    if (!this.moveItemStackTo(clickedItem, 29, 38, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (pIndex >= 29 && pIndex < 38 && !this.moveItemStackTo(clickedItem, 2, 29, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (clickedItem.isEmpty()) {
                clickedSlot.set(ItemStack.EMPTY);
            } else {
                clickedSlot.setChanged();
            }

            if (clickedItem.getCount() == item.getCount()) {
                return ItemStack.EMPTY;
            }

            clickedSlot.onTake(pPlayer, clickedItem);
        }
        System.out.println(pIndex);
        System.out.println(pPlayer);
        return item;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
