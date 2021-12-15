package com.epherical.shoppy.block;

import com.epherical.shoppy.ShoppyMod;
import net.fabricmc.fabric.impl.event.interaction.InteractionEventsRouter;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public class ShopBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape GLASS_BOX = Block.box(3.0D, 8.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape SHAPE = Shapes.or(BASE, GLASS_BOX);

    public ShopBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public VoxelShape getInteractionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return SHAPE;
    }

    // if owner and top empty, right click with item to set what will be given to the purchaser
    // if owner and bottom empty, right click with item to set what the purchaser must give to the block
    // if owner and either has stuff, shift right click to clear. should drop items inside too?

    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Vec3 hit = blockHitResult.getLocation().subtract(Vec3.atLowerCornerOf(blockPos));
        ItemStack item = player.getMainHandItem();
        ShopBlockEntity shopBlock = level.getBlockEntity(blockPos, ShoppyMod.SHOP_BLOCK_ENTITY).orElse(null);

        if (shopBlock != null && (shopBlock.getOwner().equals(player.getUUID()) || player.hasPermissions(4)) && !level.isClientSide) {
            if (player.isShiftKeyDown()) {
                Containers.dropContents(level, blockPos, shopBlock.dropItems());
                shopBlock.addCurrencyItem(ItemStack.EMPTY);
                shopBlock.addSellingItem(ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            } else if (hit.y() <= 0.5 && shopBlock.getCurrency().isEmpty()) {
                shopBlock.addCurrencyItem(item.copy());
                return InteractionResult.SUCCESS;
            } else if (hit.y() > 0.5) {
                if (shopBlock.getSelling().isEmpty()) {
                    shopBlock.addSellingItem(item.copy());
                } else if (ItemStack.isSameItemSameTags(item, shopBlock.getSelling())) {
                    shopBlock.putItemIntoShop(false, item);
                }

                return InteractionResult.SUCCESS;
            }
            return InteractionResult.CONSUME;
        } else {
            // someone purchasing
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        super.attack(blockState, level, blockPos, player);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof ShopBlockEntity) {
                Containers.dropContents(level, blockPos, ((ShopBlockEntity) blockEntity).dropItems());
            }
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity != null) {
            ShopBlockEntity shop = (ShopBlockEntity) level.getBlockEntity(blockPos);
            shop.setOwner(livingEntity.getUUID());
        }
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShopBlockEntity(blockPos, blockState);
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    public boolean isPathfindable(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, PathComputationType pathComputationType) {
        return false;
    }
}
