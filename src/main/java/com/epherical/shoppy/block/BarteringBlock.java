package com.epherical.shoppy.block;

import com.epherical.shoppy.ShoppyMod;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
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


public class BarteringBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    protected static final VoxelShape BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D);
    protected static final VoxelShape GLASS_BOX = Block.box(3.0D, 8.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape SHAPE = Shapes.or(BASE, GLASS_BOX);

    public BarteringBlock(Properties properties) {
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
        BarteringBlockEntity shopBlock = level.getBlockEntity(blockPos, ShoppyMod.BARTING_STATION_ENTITY).orElse(null);

        if (shopBlock != null) {
            if ((shopBlock.getOwner().equals(player.getUUID())/* || player.hasPermissions(4)*/) && !level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    shopBlock.clearShop();
                    return InteractionResult.SUCCESS;
                } else if (hit.y() <= 0.5 && shopBlock.getCurrency().isEmpty()) {
                    shopBlock.addCurrencyItem(item.copy());
                    Component setup = new TranslatableComponent("barter.setup.owner.add_currency", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
                    player.sendMessage(setup, Util.NIL_UUID);
                    return InteractionResult.SUCCESS;
                } else if (hit.y() > 0.5) {
                    if (shopBlock.getSelling().isEmpty()) {
                        shopBlock.addSellingItem(item.copy());
                        Component setup = new TranslatableComponent("barter.setup.owner.add_selling", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
                        player.sendMessage(setup, Util.NIL_UUID);
                    } else if (ItemStack.isSameItemSameTags(item, shopBlock.getSelling())) {
                        shopBlock.putItemIntoShop(false, item);
                    }

                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.CONSUME;
            } else {
                if (!level.isClientSide && !shopBlock.getSelling().isEmpty()) {
                    shopBlock.attemptPurchase(player, item);
                }
            }
        }

        return InteractionResult.CONSUME;
    }

    /**
     * We will use this method to display information to the owner of the shop or a potential buyer.
     */
    @Override
    public void attack(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        BarteringBlockEntity shopBlock = level.getBlockEntity(blockPos, ShoppyMod.BARTING_STATION_ENTITY).orElse(null);

        if (shopBlock != null && !level.isClientSide) {
            // owner
            if (shopBlock.getOwner().equals(player.getUUID())) {
                if (player.isShiftKeyDown()) {
                    shopBlock.extractItemsFromShop(level, blockPos);
                } else {
                    shopBlock.sendInformationToOwner(player);
                }
            } else {
                // send message to player about what is being sold for and what must be given
                Component amountBeingSold = new TextComponent("x" + shopBlock.getSelling().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component itemBeingSold = shopBlock.getSelling().getDisplayName().copy().withStyle(style -> {
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(shopBlock.getSelling())));
                    return style;
                });
                Component currencyAmount = new TextComponent("x" + shopBlock.getCurrency().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component itemBeingTraded = shopBlock.getCurrency().getDisplayName().copy().withStyle(style -> {
                    style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(shopBlock.getCurrency())));
                    return style;
                });

                Component component = new TranslatableComponent("barter.information.user.selling_info", amountBeingSold, itemBeingSold, currencyAmount, itemBeingTraded).setStyle(ShoppyMod.CONSTANTS_STYLE);
                player.sendMessage(component, Util.NIL_UUID);
            }
        }
        super.attack(blockState, level, blockPos, player);
    }

    @Override
    public void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (!blockState.is(blockState2.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof BarteringBlockEntity) {
                Containers.dropContents(level, blockPos, ((BarteringBlockEntity) blockEntity).dropItems());
            }
        }
        super.onRemove(blockState, level, blockPos, blockState2, bl);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if (livingEntity != null) {
            BarteringBlockEntity shop = (BarteringBlockEntity) level.getBlockEntity(blockPos);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BarteringBlockEntity(blockPos, blockState);
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
