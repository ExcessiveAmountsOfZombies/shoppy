package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.ShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ShopBlock extends AbstractTradingBlock {


    public ShopBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShopBlockEntity(blockPos, blockState);
    }
}
