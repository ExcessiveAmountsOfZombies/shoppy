package com.epherical.shoppy.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractShoppyMenu extends AbstractContainerMenu {

    private final ContainerData containerData;
    protected AbstractShoppyMenu(@Nullable MenuType<?> pMenuType, int pContainerId, Inventory playerInventory, ContainerData containerData) {
        super(pMenuType, pContainerId);
        this.containerData = containerData;

        this.addDataSlots(containerData);
    }

    public ContainerData getContainerData() {
        return containerData;
    }
}
