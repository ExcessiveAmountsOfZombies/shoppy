package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.ShoppyMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ShopBlockEntity extends AbstractTradingBlockEntity {


    public ShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.SHOP_BLOCK_ENTITY, blockPos, blockState);
    }



    @Override
    public boolean attemptPurchase(Player player, ItemStack currencyInHand) {
        return false;
    }

    @Override
    public void sendInformationToOwner(Player player) {

    }

    @Override
    public InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        return null;
    }

    @Override
    public void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {

    }
}
