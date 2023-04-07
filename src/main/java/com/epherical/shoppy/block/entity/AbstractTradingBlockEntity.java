package com.epherical.shoppy.block.entity;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

public abstract class AbstractTradingBlockEntity extends BaseContainerBlockEntity implements Clearable, MenuConstructor, Container {

    protected int transaction;
    protected UUID owner = Util.NIL_UUID;
    protected ItemStack selling;
    protected int storedSellingItems;
    protected int maxStorage;

    public AbstractTradingBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.selling = ItemStack.EMPTY;
        this.storedSellingItems = 0;
        this.maxStorage = 54 * 64;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.transaction = compoundTag.getInt("transactions");
        this.owner = compoundTag.getUUID("owner");
        this.selling = ItemStack.of(compoundTag.getCompound("selling"));
        this.storedSellingItems = compoundTag.getInt("storedItems");
        this.maxStorage = compoundTag.getInt("maxStorage");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("transactions", transaction);
        compoundTag.putUUID("owner", owner);
        compoundTag.put("selling", selling.save(new CompoundTag()));
        compoundTag.putInt("storedItems", storedSellingItems);
        compoundTag.putInt("maxStorage", maxStorage);
    }

    @Override
    public void clearContent() {
        transaction = 0;
        storedSellingItems = 0;
        selling = ItemStack.EMPTY;
    }

    public void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public abstract boolean attemptPurchase(Player player, ItemStack currencyInHand, boolean creativeBlock);

    public abstract void sendInformationToOwner(Player player);

    public abstract InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult);

    public abstract void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player);

    public int putItemIntoShop(ItemStack item) {
        if (!item.isEmpty()) {
            if (this.storedSellingItems > maxStorage) {
                return 0;
            }

            int itemsInserted = Math.min(item.getCount(), remainingItemStorage());

            this.storedSellingItems += itemsInserted;
            item.shrink(itemsInserted);

            markUpdated();
            return itemsInserted;
        }
        return 0;
    }

    public void extractItemsFromShop(Level level, BlockPos pos) {
        int itemsToTake = Math.min(64, storedSellingItems);
        ItemStack selling = getSelling().copy();
        selling.setCount(itemsToTake);
        Block.popResource(level, pos, selling);
        storedSellingItems -= itemsToTake;
    }

    public void clearShop(BlockHitResult result) {
        for (ItemStack stack : dropItems()) {
            Block.popResourceFromFace(level, getBlockPos(), result.getDirection(), stack);
        }
        this.selling = ItemStack.EMPTY;
        this.storedSellingItems = 0;
        markUpdated();
    }

    public NonNullList<ItemStack> dropItems() {
        NonNullList<ItemStack> list = NonNullList.create();

        int itemsStored = this.storedSellingItems;

        int maxStackSize = this.selling.getMaxStackSize();
        while (itemsStored != 0) {
            ItemStack copy = this.selling.copy();
            if (itemsStored >= maxStackSize) {
                copy.setCount(maxStackSize);
                list.add(copy);
                itemsStored -= maxStackSize;
            } else {
                copy.setCount(itemsStored);
                list.add(copy);
                itemsStored -= itemsStored;
            }
        }

        return list;
    }

    public void addSellingItem(ItemStack item) {
        this.selling = item;
        markUpdated();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public ItemStack getSelling() {
        return selling;
    }

    public int remainingItemStorage() {
        return maxStorage - storedSellingItems;
    }
}
