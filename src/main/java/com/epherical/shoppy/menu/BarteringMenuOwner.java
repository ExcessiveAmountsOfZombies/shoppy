package com.epherical.shoppy.menu;

import com.epherical.shoppy.ShoppyMod;
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

public class BarteringMenuOwner extends BarteringMenu {

    public static final int CURRENCY_ITEM = 2;
    public static final int SOLD_ITEMS = 3;


    public BarteringMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory) {
        this(pMenuType, pContainerId, playerInventory, new DenseContainer(4), new SimpleContainerData(4));
    }

    public BarteringMenuOwner(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container, ContainerData data) {
        super(pMenuType, pContainerId, playerInventory, container, data);
    }

    public static BarteringMenuOwner realContainer(int pContainerId, Inventory playerInventory, Container container, ContainerData containerData) {
        return new BarteringMenuOwner(ShoppyMod.BARTERING_MENU_OWNER, pContainerId, playerInventory, container, containerData);
    }

    @Override
    protected void addSlots() {
        super.addSlots();
        this.addSlot(new Slot(container, CURRENCY_ITEM, 71, 32) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                if (getContainerData().get(2) <= 0) {
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
        this.addSlot(new Slot(container, SOLD_ITEMS, 89, 32) {
            @Override
            public boolean mayPickup(@NotNull Player player) {
                if (getContainerData().get(3) <= 0) {
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
