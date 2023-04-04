package com.epherical.shoppy.block.entity;

import com.epherical.shoppy.BarteringMenu;
import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.block.AbstractTradingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BarteringBlockEntity extends AbstractTradingBlockEntity {

    ItemStack currency;
    int currencyStored;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int data) {
            switch (data) {
                case 0: return currency.getCount();
                case 1: return selling.getCount();
            }
            return 0;
        }

        @Override
        public void set(int key, int value) {
            switch (key) {
                case 0: currency.setCount(value); break;
                case 1: selling.setCount(value); break;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };


    public BarteringBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.BARTING_STATION_ENTITY, blockPos, blockState);
        this.currency = ItemStack.EMPTY;
        this.currencyStored = 0;
    }

    public BarteringBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
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
    protected Component getDefaultName() {
        // TODO ; add configurable name
        return Component.literal("Bartering Station");
    }

    @Override
    public void clearContent() {
        super.clearContent();
        currency = ItemStack.EMPTY;
        currencyStored = 0;
        markUpdated();
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

    public void addCurrencyItem(ItemStack item) {
        this.currency = item;
        markUpdated();
    }

    public void clearShop(BlockHitResult result) {
        super.clearShop(result);
        this.currency = ItemStack.EMPTY;
        this.currencyStored = 0;
        markUpdated();
    }

    @Override
    public boolean attemptPurchase(Player player, ItemStack currencyInHand, boolean creativeBlock) {
        Player owner = level.getServer().getPlayerList().getPlayer(this.owner);
        if (ItemStack.isSameItemSameTags(currencyInHand, currency)) {
            int price = currency.getCount();
            if (currencyInHand.getCount() >= price) {
                int amountToGive = selling.getCount();
                if (amountToGive > storedSellingItems) {
                    Component buyerMsg = Component.translatable("barter.purchase.shop_empty").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendSystemMessage(buyerMsg);
                    if (owner != null) {
                        Component location = Component.translatable("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                        Component ownerMsg = Component.translatable("barter.purchase.owner.shop_empty", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                        owner.sendSystemMessage(ownerMsg);
                    }
                } else if (remainingCurrencySpaces() <= 0 && !creativeBlock) {
                    Component buyerMsg = Component.translatable("barter.purchase.currency_full", currency.getDisplayName()).setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendSystemMessage(buyerMsg);
                    if (owner != null && !creativeBlock) {
                        Component location = Component.translatable("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                        Component ownerMsg = Component.translatable("barter.purchase.owner.currency_full", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                        owner.sendSystemMessage(ownerMsg);
                    }
                } else if (player.getInventory().getFreeSlot() == -1) {
                    Component component = Component.translatable("common.purchase.full_inventory").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendSystemMessage(component);
                } else {
                    currencyInHand.shrink(price);
                    player.addItem(selling.copy());
                    Component buyer = Component.translatable("barter.purchase.transaction_success", selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                    player.sendSystemMessage(buyer);
                    if (!creativeBlock) {
                        storedSellingItems -= amountToGive;
                        currencyStored += price;
                    }

                    if (owner != null) {
                        Component sellerMsg = Component.translatable("barter.purchase.owner.transaction_success", player.getDisplayName(), selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                        owner.sendSystemMessage(sellerMsg);
                    }
                    markUpdated();
                    return true;
                }
            } else {
                Component required = Component.literal("x" + price).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component had = Component.literal("x" + currencyInHand.getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
                MutableComponent component = Component.translatable("barter.purchase.not_enough_items", required, had);
                component.setStyle(ShoppyMod.ERROR_STYLE);
                player.sendSystemMessage(component);
            }
            markUpdated();
            return false;
        } else {
            MutableComponent component = Component.translatable("barter.purchase.no_held_item");
            component.setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(component);
        }
        markUpdated();
        return false;
    }

    public NonNullList<ItemStack> dropItems() {
        NonNullList<ItemStack> list = NonNullList.create();
        list.addAll(super.dropItems());
        int currency = this.currencyStored;

        int maxStackSize = this.currency.getMaxStackSize();
        while (currency != 0) {
            ItemStack copy = this.currency.copy();
            if (currency >= maxStackSize) {
                copy.setCount(maxStackSize);
                list.add(copy);
                currency -= maxStackSize;
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

    public void sendInformationToOwner(Player player) {
        Component component = Component.translatable("barter.information.owner.extraction").setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendSystemMessage(component);
        Component profits = Component.literal("" + currencyStored).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component translatedProfits = Component.translatable("barter.information.owner.profits", profits).setStyle(ShoppyMod.CONSTANTS_STYLE);
        Component storedItems = Component.literal("" + storedSellingItems).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component toBeSold = Component.translatable("barter.information.owner.barter", storedItems).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendSystemMessage(translatedProfits);
        player.sendSystemMessage(toBeSold);
    }

    @Override
    public InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Vec3 hit = blockHitResult.getLocation().subtract(Vec3.atLowerCornerOf(blockPos));
        ItemStack item = player.getMainHandItem();

        if (player.isCrouching() && hit.y() > 0.5) {
            clearShop(blockHitResult);
            return InteractionResult.SUCCESS;
        }

        boolean sameItem = this.getCurrency().sameItem(item);
        if (hit.y() <= 0.5 && (this.getCurrency().isEmpty() || sameItem)) {
            if (item.getCount() == 1 && sameItem) {
                if (currency.getCount() < currency.getMaxStackSize())
                currency.setCount(currency.getCount() + 1);
                markUpdated();
                return InteractionResult.SUCCESS;
            } else {
                this.addCurrencyItem(item.copy());
            }

            Component setup = Component.translatable("barter.setup.owner.add_currency", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
            player.sendSystemMessage(setup);
            return InteractionResult.SUCCESS;
        } else if (hit.y() > 0.5) {
            if (this.getSelling().isEmpty()) {
                this.addSellingItem(item.copy());
                Component setup = Component.translatable("barter.setup.owner.add_selling", item.getDisplayName()).setStyle(ShoppyMod.CONSTANTS_STYLE);
                player.sendSystemMessage(setup);
            } else if (ItemStack.isSameItemSameTags(item, this.getSelling())) {
                this.putItemIntoShop(item);
            }

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        // send message to player about what is being sold for and what must be given
        Component amountBeingSold = Component.literal("x" + this.getSelling().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingSold = this.getSelling().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getSelling())));
            return style;
        });
        Component currencyAmount = Component.literal("x" + this.getCurrency().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingTraded = this.getCurrency().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getCurrency())));
            return style;
        });

        Component component = Component.translatable("barter.information.user.selling_info", amountBeingSold, itemBeingSold, currencyAmount, itemBeingTraded).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendSystemMessage(component);
    }

    public void extractItemsFromShop(Level level, BlockPos pos) {
        // extract money first
        if (remainingCurrencySpaces() != maxStorage) {
            int itemsToTake = Math.min(64, currencyStored);
            ItemStack currency = getCurrency().copy();
            currency.setCount(itemsToTake);
            AbstractTradingBlock.popResource(level, pos, currency);
            currencyStored -= itemsToTake;
        } else {
            super.extractItemsFromShop(level, pos);
        }
    }

    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return BarteringMenu.realContainer(i, inventory, this, data);
    }

    /**
     * This container only 'stores' 2 things in it, a currency and an item to be traded.
     * The items can be stacked above 64, however.
     * @return 2
     */
    @Override
    public int getContainerSize() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return currency.isEmpty() && selling.isEmpty();
    }

    /**
     *
     * @param pSlot The slot to check.
     * @return An ItemStack that shows how many items, up to 64, are in the trading block.
     */
    @Override
    public ItemStack getItem(int pSlot) {
        int take;
        ItemStack item;

        if (pSlot == 0) {
            take = currencyStored;
            item = getCurrency().copy();
        } else if (pSlot == 1) {
            take = storedSellingItems;
            item = getSelling().copy();
        } else {
            return ItemStack.EMPTY;
        }
        item.setCount(take);
        return item;
    }

    /**
     * Given a slot, we will remove up to 64 items, based on how much of that item is stored.
     * @param amountToRemove ignored. Every removal is considered to be up to a full stack.
     * @return The ItemStack of the item to be removed.
     */
    @Override
    public ItemStack removeItem(int slot, int amountToRemove) {
        ItemStack item = getItem(slot);
        if (slot == 0) {
            currencyStored -= amountToRemove;
        } else if (slot == 1) {
            storedSellingItems -= amountToRemove;
        }
        // todo; this could be wrong
        return item.split(amountToRemove);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 64);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {

    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
