package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.ShoppyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CreativeBarteringBlockEntity extends BarteringBlockEntity implements CreativeBlock {


    public CreativeBarteringBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.CREATIVE_BARTERING_STATION_ENTITY, blockPos, blockState);
        this.currencyStored = maxStorage;
        this.storedSellingItems = maxStorage;
    }

    @Override
    public void clearContent() {
        super.clearContent();
        this.currencyStored = maxStorage;
        this.storedSellingItems = maxStorage;
    }

    @Override
    public void clearShop(BlockHitResult result) {
        super.clearShop(result);
        this.currencyStored = maxStorage;
        this.storedSellingItems = maxStorage;
    }

    @Override
    public boolean attemptPurchase(Player player, ItemStack currencyInHand, boolean creativeBlock) {
        boolean value = super.attemptPurchase(player, currencyInHand, true);
        this.currencyStored = maxStorage;
        this.storedSellingItems = maxStorage;
        return value;
    }

    @Override
    public NonNullList<ItemStack> dropItems() {
        return NonNullList.create();
    }


}
