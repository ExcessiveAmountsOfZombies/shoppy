package com.epherical.shoppy;

import net.minecraft.world.Container;
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
        this(pMenuType, pContainerId, playerInventory, new DenseContainer(2), new SimpleContainerData(2));
    }

    protected BarteringMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, Container container, ContainerData data) {
        super(pMenuType, pContainerId, playerInventory, data);
        this.addSlot(new Slot(container, 0, 19, 32) {
            @Override
            public boolean mayPickup(Player $$0) {
                return false;
            }
        });
        this.addSlot(new Slot(container, 1, 143, 32) {
            @Override
            public boolean mayPickup(Player $$0) {
                return false;
            }
        });
    }


    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return true;
    }
}
