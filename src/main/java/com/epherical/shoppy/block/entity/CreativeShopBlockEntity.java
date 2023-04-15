package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.ShoppyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CreativeShopBlockEntity extends ShopBlockEntity implements CreativeBlock {

    public CreativeShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.CREATIVE_SHOP_BLOCK_ENTITY, blockPos, blockState);
        keepShopOpen();
    }

    @Override
    public void clearContent() {
        super.clearContent();
        keepShopOpen();
    }

    @Override
    public void clearShop(BlockHitResult result) {
        super.clearShop(result);
        keepShopOpen();
    }

    @Override
    public boolean attemptPurchase(Player player, boolean creativeBlock) {
        boolean value = super.attemptPurchase(player, true);
        keepShopOpen();
        return value;
    }

    private void keepShopOpen() {
        storedSellingItems = maxStorage / 2;
    }
}
