package com.epherical.shoppy.block;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.block.entity.AbstractTradingBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;


public abstract class AbstractTradingBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape GLASS_BOX = Block.box(3.0D, 8.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape SHAPE = Shapes.or(BASE, GLASS_BOX);

    public AbstractTradingBlock(Properties properties) {
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
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof AbstractTradingBlockEntity tradingBlock) {
            if (!player.isCrouching()) {
                player.openMenu(tradingBlock);
            } else  {
                if (tradingBlock instanceof ShopBlockEntity shopBlock && shopBlock.getOwner().equals(player.getUUID())) {
                    shopBlock.updateTradingStatus(!shopBlock.isBuyingFromPlayer());
                    Component status;
                    if (shopBlock.isBuyingFromPlayer()) {
                        status = Component.translatable("shop.status.buying").setStyle(ShoppyMod.VARIABLE_STYLE);
                    } else {
                        status = Component.translatable("shop.status.selling").setStyle(ShoppyMod.VARIABLE_STYLE);
                    }
                    Component update = Component.translatable("shop.status.update", status).setStyle(ShoppyMod.CONSTANTS_STYLE);
                    player.sendSystemMessage(update);
                }
            }

            return InteractionResult.CONSUME;
            /*if ((shopBlock.getOwner().equals(player.getUUID())*//* || player.hasPermissions(4)*//*) && !level.isClientSide) {
                return shopBlock.interactWithTradingBlock(blockState, level, blockPos, player, interactionHand, blockHitResult);
            } else {
                if (!level.isClientSide && !shopBlock.getSelling().isEmpty()) {
                    shopBlock.attemptPurchase(player, item, false);
                }
            }*/
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof AbstractTradingBlockEntity) {
                Containers.dropContents(level, blockPos, ((AbstractTradingBlockEntity) blockEntity).dropItems());
            }
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity != null) {
            AbstractTradingBlockEntity shop = (AbstractTradingBlockEntity) level.getBlockEntity(blockPos);
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
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState().setValue(FACING, blockPlaceContext.getHorizontalDirection());
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
