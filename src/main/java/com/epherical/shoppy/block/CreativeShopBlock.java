package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeShopBlock extends ShopBlock {

    public CreativeShopBlock(Properties properties, boolean creative) {
        super(properties, creative);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreativeShopBlockEntity(blockPos, blockState);
    }
}
