package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CreativeBarteringBlock extends BarteringBlock {

    public CreativeBarteringBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CreativeBarteringBlockEntity(blockPos, blockState);
    }
}
