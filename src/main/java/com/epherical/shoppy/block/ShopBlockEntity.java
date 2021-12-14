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
    private NonNullList<ItemStack> currencyStorage;
    private NonNullList<ItemStack> itemStorage;


    public ShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.SHOP_BLOCK_ENTITY, blockPos, blockState);
        this.currency = ItemStack.EMPTY;
        this.selling = ItemStack.EMPTY;
        this.currencyStorage = NonNullList.withSize(27, ItemStack.EMPTY);
        this.itemStorage = NonNullList.withSize(54, ItemStack.EMPTY);
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.transactions = compoundTag.getInt("transactions");
        this.owner = compoundTag.getUUID("owner");
        this.currency = ItemStack.of(compoundTag.getCompound("currency"));
        this.selling = ItemStack.of(compoundTag.getCompound("selling"));
        ContainerHelper.loadAllItems(compoundTag, currencyStorage);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putInt("transactions", transactions);
        compoundTag.putUUID("owner", owner);
        ContainerHelper.saveAllItems(compoundTag, currencyStorage, true);
        compoundTag.put("currency", currency.save(new CompoundTag()));
        compoundTag.put("selling", selling.save(new CompoundTag()));
    }

    @Override
    public void clearContent() {
        currency = ItemStack.EMPTY;
        selling = ItemStack.EMPTY;
        currencyStorage.clear();
        itemStorage.clear();
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

    public ItemStack getCurrency() {
        return currency;
    }

    public ItemStack getSelling() {
        return selling;
    }
}
