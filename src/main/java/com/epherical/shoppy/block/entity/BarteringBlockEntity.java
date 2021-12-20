package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.block.AbstractTradingBlock;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BarteringBlockEntity extends AbstractTradingBlockEntity {

    private ItemStack currency;
    private int currencyStored;


    public BarteringBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.BARTING_STATION_ENTITY, blockPos, blockState);
        this.currency = ItemStack.EMPTY;
        this.currencyStored = 0;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.currency = ItemStack.of(compoundTag.getCompound("currency"));
        this.currencyStored = compoundTag.getInt("storedCurrency");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.put("currency", currency.save(new CompoundTag()));
        compoundTag.putInt("storedCurrency", currencyStored);
    }

    @Override
    public void clearContent() {
        super.clearContent();
        currency = ItemStack.EMPTY;
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

    public void addSellingItem(ItemStack item) {
        this.selling = item;
        markUpdated();
    }

    public void addCurrencyItem(ItemStack item) {
        this.currency = item;
        markUpdated();
    }

    public void clearShop() {
        super.clearShop();
        this.currency = ItemStack.EMPTY;
        this.currencyStored = 0;
        markUpdated();
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean attemptPurchase(Player player, ItemStack currencyInHand) {
        Player owner = level.getServer().getPlayerList().getPlayer(this.owner);
        if (ItemStack.isSameItemSameTags(currencyInHand, currency)) {
            int price = currency.getCount();
            if (currencyInHand.getCount() >= price) {
                int amountToGive = selling.getCount();
                if (amountToGive > storedSellingItems) {
                    Component buyerMsg = new TranslatableComponent("barter.purchase.shop_empty").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendMessage(buyerMsg, Util.NIL_UUID);
                    if (owner != null) {
                        Component location = new TranslatableComponent("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                        Component ownerMsg = new TranslatableComponent("barter.purchase.owner.shop_empty", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                        owner.sendMessage(ownerMsg, Util.NIL_UUID);
                    }
                } else if (remainingCurrencySpaces() <= 0) {
                    Component buyerMsg = new TranslatableComponent("barter.purchase.currency_full", currency.getDisplayName()).setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendMessage(buyerMsg, Util.NIL_UUID);
                    if (owner != null) {
                        Component location = new TranslatableComponent("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                        Component ownerMsg = new TranslatableComponent("barter.purchase.owner.currency_full", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                        owner.sendMessage(ownerMsg, Util.NIL_UUID);
                    }
                } else if (player.getInventory().getFreeSlot() == -1) {
                    Component component = new TranslatableComponent("common.purchase.full_inventory").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendMessage(component, Util.NIL_UUID);
                } else {
                    currencyInHand.shrink(price);
                    player.addItem(selling.copy());
                    Component buyer = new TranslatableComponent("barter.purchase.transaction_success", selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                    player.sendMessage(buyer, Util.NIL_UUID);

                    if (owner != null) {
                        Component sellerMsg = new TranslatableComponent("barter.purchase.owner.transaction_success", player.getDisplayName(), selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                        player.sendMessage(sellerMsg, Util.NIL_UUID);
                    }
                    return true;
                }
            } else {
                Component required = new TextComponent("x" + price).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component had = new TextComponent("x" + currencyInHand.getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
                TranslatableComponent component = new TranslatableComponent("barter.purchase.not_enough_items", required, had);
                component.setStyle(ShoppyMod.ERROR_STYLE);
                player.sendMessage(component, Util.NIL_UUID);
            }
            return false;
        } else {
            TranslatableComponent component = new TranslatableComponent("barter.purchase.no_held_item");
            component.setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(component, Util.NIL_UUID);
        }
        return false;
    }

    public int putItemIntoShop(boolean isCurrency, ItemStack item) {
        if (!item.isEmpty()) {
            if (!isCurrency) {
                if (this.storedSellingItems > maxStorage) {
                    return 0;
                }

                int itemsInserted = Math.min(item.getCount(), remainingItemStorage());

                this.storedSellingItems += itemsInserted;
                item.shrink(itemsInserted);

                return itemsInserted;
            }
        }
        return 0;
    }

    public NonNullList<ItemStack> dropItems() {
        NonNullList<ItemStack> list = NonNullList.create();
        list.addAll(super.dropItems());
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
        return list;
    }

    public int remainingCurrencySpaces() {
        return maxStorage - currencyStored;
    }

    public int remainingItemStorage() {
        return maxStorage - storedSellingItems;
    }

    public ItemStack getCurrency() {
        return currency;
    }

    public ItemStack getSelling() {
        return selling;
    }

    public void sendInformationToOwner(Player player) {
        Component component = new TranslatableComponent("barter.information.owner.extraction").setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendMessage(component, Util.NIL_UUID);
        Component profits = new TextComponent("" + currencyStored).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component translatedProfits = new TranslatableComponent("barter.information.owner.profits", profits).setStyle(ShoppyMod.CONSTANTS_STYLE);
        Component storedItems = new TextComponent("" + storedSellingItems).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component toBeSold = new TranslatableComponent("barter.information.owner.barter", storedItems).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendMessage(translatedProfits, Util.NIL_UUID);
        player.sendMessage(toBeSold, Util.NIL_UUID);
    }

    @Override
    public InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Vec3 hit = blockHitResult.getLocation().subtract(Vec3.atLowerCornerOf(blockPos));
        ItemStack item = player.getMainHandItem();
        if (hit.y() <= 0.5 && (this.getCurrency().isEmpty()) || this.getCurrency().sameItem(item)) {
            this.addCurrencyItem(item.copy());
            Component setup = new TranslatableComponent("barter.setup.owner.add_currency", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
            player.sendMessage(setup, Util.NIL_UUID);
            return InteractionResult.SUCCESS;
        } else if (hit.y() > 0.5) {
            if (this.getSelling().isEmpty()) {
                this.addSellingItem(item.copy());
                Component setup = new TranslatableComponent("barter.setup.owner.add_selling", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
                player.sendMessage(setup, Util.NIL_UUID);
            } else if (ItemStack.isSameItemSameTags(item, this.getSelling())) {
                this.putItemIntoShop(false, item);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        // send message to player about what is being sold for and what must be given
        Component amountBeingSold = new TextComponent("x" + this.getSelling().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingSold = this.getSelling().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getSelling())));
            return style;
        });
        Component currencyAmount = new TextComponent("x" + this.getCurrency().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingTraded = this.getCurrency().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getCurrency())));
            return style;
        });

        Component component = new TranslatableComponent("barter.information.user.selling_info", amountBeingSold, itemBeingSold, currencyAmount, itemBeingTraded).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendMessage(component, Util.NIL_UUID);
    }

    public void extractItemsFromShop(Level level, BlockPos pos) {
        // extract money first
        if (remainingCurrencySpaces() != maxStorage) {
            int itemsToTake = Math.min(64, currencyStored);
            ItemStack currency = getCurrency().copy();
            currency.setCount(itemsToTake);
            AbstractTradingBlock.popResource(level, pos, currency);
            currencyStored -= itemsToTake;
        }
    }
}
