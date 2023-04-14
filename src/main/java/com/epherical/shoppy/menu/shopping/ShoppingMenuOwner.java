package com.epherical.shoppy.menu.shopping;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.menu.DenseContainer;
import com.epherical.shoppy.menu.MenuOwner;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShoppingMenuOwner extends ShoppingMenu implements MenuOwner {

    public static final int INSERTED_ITEM = 1;

    public ShoppingMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory) {
        this(pMenuType, pContainerId, playerInventory, new DenseContainer(2), new SimpleContainerData(3));
    }

    public ShoppingMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container, ContainerData data) {
        super(pMenuType, pContainerId, playerInventory, container, data);
    }

    public static ShoppingMenuOwner realContainer(int pContainerId, Inventory playerInventory, Container container, ContainerData containerData) {
        return new ShoppingMenuOwner(ShoppyMod.SHOPPING_MENU_OWNER, pContainerId, playerInventory, container, containerData);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        this.addSlot(new Slot(container, INSERTED_ITEM, 71, 32) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                if (getContainerData().get(1) <= 0) {
                    set(ItemStack.EMPTY);
                }
                return false;
            }

            @Override
            public boolean mayPlace(@NotNull ItemStack item) {
                set(item.copy());
                setChanged();
                return false;
            }
        });
    }
}
