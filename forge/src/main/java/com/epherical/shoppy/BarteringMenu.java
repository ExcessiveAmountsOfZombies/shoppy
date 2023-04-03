package com.epherical.shoppy;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BarteringMenu extends AbstractShoppyMenu {

    public static BarteringMenu realContainer(int pContainerId, Inventory playerInventory, Container container) {
        return new BarteringMenu(ForgeShoppy.BARTERING_MENU, pContainerId, playerInventory, container);
    }

    protected BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory) {
        super(pMenuType, pContainerId, playerInventory);
        this.addSlot(new Slot(container, 0, 19, 22));
        this.addSlot(new Slot(container, 1, 143, 22));
    }

    protected BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container) {
        super(pMenuType, pContainerId, playerInventory);
        this.addSlot(new Slot(container, 0, 19, 22));
        this.addSlot(new Slot(container, 1, 143, 22));
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }
}
