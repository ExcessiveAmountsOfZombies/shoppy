package com.epherical.shoppy.block;

import com.epherical.shoppy.ShoppyMod;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Clearable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShopBlockEntity extends BlockEntity implements Clearable {

    private int transactions;
    private UUID owner = Util.NIL_UUID;
    private ItemStack currency;
    private ItemStack selling;
    private int currencyStored;
    private int itemsStored;
    private int maxStorage;


    public ShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.SHOP_BLOCK_ENTITY, blockPos, blockState);
        this.currency = ItemStack.EMPTY;
        this.selling = ItemStack.EMPTY;
        this.currencyStored = 0;
        this.itemsStored = 0;
        this.maxStorage = 54 * 64;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.transactions = compoundTag.getInt("transactions");
        this.owner = compoundTag.getUUID("owner");
        this.currency = ItemStack.of(compoundTag.getCompound("currency"));
        this.selling = ItemStack.of(compoundTag.getCompound("selling"));
        this.currencyStored = compoundTag.getInt("storedCurrency");
        this.itemsStored = compoundTag.getInt("storedItems");
        this.maxStorage = compoundTag.getInt("maxStorage");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("transactions", transactions);
        compoundTag.putUUID("owner", owner);
        compoundTag.put("currency", currency.save(new CompoundTag()));
        compoundTag.put("selling", selling.save(new CompoundTag()));
        compoundTag.putInt("storedCurrency", currencyStored);
        compoundTag.putInt("storedItems", itemsStored);
        compoundTag.putInt("maxStorage", maxStorage);
    }

    @Override
    public void clearContent() {
        currency = ItemStack.EMPTY;
        selling = ItemStack.EMPTY;
        itemsStored = 0;
        currencyStored = 0;
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.put("currency", currency.save(new CompoundTag()));
        tag.put("selling", selling.save(new CompoundTag()));
        tag.putUUID("owner", owner);
        return tag;
    }

    private void markUpdated() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public void addSellingItem(ItemStack item) {
        this.selling = item;
        markUpdated();
    }

    public void addCurrencyItem(ItemStack item) {
        this.currency = item;
        markUpdated();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public int putItemIntoShop(boolean isCurrency, ItemStack item) {
        if (!item.isEmpty()) {
            if (!isCurrency) {
                if (this.itemsStored > maxStorage) {
                    return 0;
                }

                int itemsInserted = Math.min(item.getCount(), remainingItemStorage());

                this.itemsStored += itemsInserted;
                item.shrink(itemsInserted);

                return itemsInserted;
            }
        }
        return 0;
    }

    public NonNullList<ItemStack> dropItems() {
        NonNullList<ItemStack> list = NonNullList.create();
        int currency = this.currencyStored;

        while (currency != 0) {
            ItemStack copy = this.currency.copy();
            if (currency >= 64) {
                copy.setCount(64);
                list.add(copy);
                currency -= 64;
            } else {
                copy.setCount(currency);
                list.add(copy);
                currency -= currency;
            }
        }

        int itemsStored = this.itemsStored;

        while (itemsStored != 0) {
            ItemStack copy = this.selling.copy();
            if (itemsStored >= 64) {
                copy.setCount(64);
                list.add(copy);
                itemsStored -= 64;
            } else {
                copy.setCount(itemsStored);
                list.add(copy);
                itemsStored -= itemsStored;
            }
        }

        return list;
    }

    public int remainingCurrencySpaces() {
        return maxStorage - currencyStored;
    }

    public int remainingItemStorage() {
        return maxStorage - itemsStored;
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public ItemStack getSelling() {
        return selling;
    }
}
