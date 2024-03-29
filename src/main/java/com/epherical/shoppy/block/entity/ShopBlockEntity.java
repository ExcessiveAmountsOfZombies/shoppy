package com.epherical.shoppy.block.entity;

import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.shopping.ShoppingMenu;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import static com.epherical.shoppy.menu.bartering.BarteringMenu.CURRENCY_STORED;
import static com.epherical.shoppy.menu.shopping.ShoppingMenu.SELLING_STORED;

public class ShopBlockEntity extends AbstractTradingBlockEntity {


    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isBuyingFromPlayer;
    private int price;

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int data) {
            return switch (data) {
                case 0 -> selling.getCount();
                case 1 -> storedSellingItems;
                case 2 -> price;
                case 3 -> isBuyingFromPlayer ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int key, int value) {
            switch (key) {
                case 0 -> selling.setCount(value);
                case 1 -> storedSellingItems = value;
                case 2 -> price = value;
                case 3 -> isBuyingFromPlayer = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };


    public ShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.SHOP_BLOCK_ENTITY, blockPos, blockState);
        this.isBuyingFromPlayer = false;
        this.price = 0;
    }

    public ShopBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
        this.isBuyingFromPlayer = false;
        this.price = 0;
    }

    @Override
    public void load(CompoundTag compoundTag) {
        super.load(compoundTag);
        this.isBuyingFromPlayer = compoundTag.getBoolean("buyingFromPlayer");
        this.price = compoundTag.getInt("price");
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag) {
        super.saveAdditional(compoundTag);
        compoundTag.putBoolean("buyingFromPlayer", isBuyingFromPlayer);
        compoundTag.putInt("price", price);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.shoppy.shop_block");
    }

    @Override
    public void clearContent() {
        super.clearContent();
        isBuyingFromPlayer = false;
        markUpdated();
    }

    @Override
    public boolean attemptPurchase(Player player, boolean creativeBlock) {
        ItemStack currencyInHand = ItemStack.EMPTY;
        try {
            Inventory inventory = player.getInventory();
            currencyInHand = inventory.getItem(inventory.findSlotMatchingItem(selling));
        } catch (ArrayIndexOutOfBoundsException ignored) {}

        Player owner = level.getServer().getPlayerList().getPlayer(this.owner);
        if (ShoppyMod.economyInstance == null) {
            Component noEconomy = Component.translatable("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(noEconomy);
            return false;
        }

        UniqueUser ownerUser = ShoppyMod.economyInstance.getOrCreatePlayerAccount(this.owner);
        UniqueUser user = ShoppyMod.economyInstance.getOrCreatePlayerAccount(player.getUUID());
        if (user != null && ownerUser != null) {
            if (isBuyingFromPlayer) {
                if (creativeBlock && user.hasAmount(ShoppyMod.economyInstance.getDefaultCurrency(), price)) {
                    return shopBuyFromPlayer(player, currencyInHand, ownerUser, user, owner, true);
                }
                if (ownerUser.hasAmount(ShoppyMod.economyInstance.getDefaultCurrency(), price)) {
                    return shopBuyFromPlayer(player, currencyInHand, ownerUser, user, owner, false);
                } else {
                    Component msg = Component.translatable("shop.buying.not_enough_funds").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendSystemMessage(msg);
                    return false;
                }
            } else {
                if (creativeBlock && user.hasAmount(ShoppyMod.economyInstance.getDefaultCurrency(), price)) {
                    return shopSellToPlayer(player, currencyInHand, ownerUser, user, owner, true);
                }
                if (user.hasAmount(ShoppyMod.economyInstance.getDefaultCurrency(), price)) {
                    return shopSellToPlayer(player, currencyInHand, ownerUser, user, owner, false);
                } else {
                    Component notEnoughMoney = Component.translatable("shop.purchase.not_enough_money").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendSystemMessage(notEnoughMoney);
                    return false;
                }
            }
        } else {
            Component noAccounts = Component.translatable("shop.purchase.no_account").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(noAccounts);
            LOGGER.error("Missing accounts for a shop transaction!! Player involved {}, UUIDs involved (PURCHASER {}) (OWNER {})", player.getScoreboardName(), player.getUUID(), getOwner());
            LOGGER.error("This could be a result of an ShoppyMod.economyInstance implementation being unable to handle offline players?");
        }
        return false;
    }


    private boolean shopSellToPlayer(Player player, ItemStack currencyInHand, UniqueUser ownerUser, UniqueUser playerShopping, Player owner, boolean creative) {
        int amountToGive = selling.getCount();
        if (amountToGive > storedSellingItems) {
            Component buyerMsg = Component.translatable("shop.purchase.shop_empty").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(buyerMsg);
            if (owner != null) {
                Component location = Component.translatable("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component ownerMsg = Component.translatable("shop.purchase.owner.shop_empty", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                owner.sendSystemMessage(ownerMsg);
            }
            return false;
        } else if (player.getInventory().getFreeSlot() == -1) {
            Component component = Component.translatable("common.purchase.full_inventory").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(component);
            return false;
        } else {
            player.addItem(selling.copy());
            if (creative) {
                playerShopping.withdrawMoney(ShoppyMod.economyInstance.getDefaultCurrency(), price, "creative shop purchase");
            } else {
                playerShopping.sendTo(ownerUser, ShoppyMod.economyInstance.getDefaultCurrency(), price);
                storedSellingItems -= amountToGive;
            }
            Component buyer = Component.translatable("shop.purchase.transaction_success", selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
            player.sendSystemMessage(buyer);
            if (owner != null) {
                Component sellerMsg = Component.translatable("shop.purchase.owner.transaction_success", player.getDisplayName(), selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                owner.sendSystemMessage(sellerMsg);
            }
        }
        return true;
    }

    private boolean shopBuyFromPlayer(Player player, ItemStack currencyInHand, UniqueUser ownerUser, UniqueUser playerShopping, Player owner, boolean creative) {
        int moneyToGiveToPlayer = price;
        int storageLeft = remainingItemStorage() - selling.getCount();
        if (storageLeft < 0) {
            Component notEnoughSpace = Component.translatable("shop.buying.full").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(notEnoughSpace);
            return false;
        } else if (currencyInHand.isEmpty() || !ItemStack.isSameItemSameTags(currencyInHand, selling)) {
            Component notSameItem = Component.translatable("shop.buying.no_held_item").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(notSameItem);
            return false;
        } else if (currencyInHand.getCount() < selling.getCount()) {
            Component notEnough = Component.translatable("shop.buying.not_enough_items").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(notEnough);
            return false;
        } else {
            currencyInHand.shrink(selling.getCount());
            if (creative) {
                playerShopping.depositMoney(ShoppyMod.economyInstance.getDefaultCurrency(), moneyToGiveToPlayer, "creative mode shop");
            } else {
                ownerUser.sendTo(playerShopping, ShoppyMod.economyInstance.getDefaultCurrency(), moneyToGiveToPlayer);
                storedSellingItems += selling.getCount();
            }
            Component priceComp = ShoppyMod.economyInstance.getDefaultCurrency().format(price);
            Component seller = Component.translatable("shop.buying.player.success", selling.getDisplayName(), priceComp).setStyle(ShoppyMod.APPROVAL_STYLE);
            player.sendSystemMessage(seller);
            if (owner != null) {
                Component buyerMsg = Component.translatable("shop.buying.owner.success", player.getDisplayName(), selling.getDisplayName(), priceComp).setStyle(ShoppyMod.APPROVAL_STYLE);
                owner.sendSystemMessage(buyerMsg);
            }
        }
        return true;
    }

    @Override
    public void sendInformationToOwner(Player player) {
        if (ShoppyMod.economyInstance == null) {
            Component noEconomy = Component.translatable("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(noEconomy);
            return;
        }

        Component type;
        if (isBuyingFromPlayer) {
            type = Component.translatable("shop.status.buy").setStyle(ShoppyMod.VARIABLE_STYLE);
        } else {
            type = Component.translatable("shop.status.sell").setStyle(ShoppyMod.VARIABLE_STYLE);
        }
        Component items = Component.literal("" + storedSellingItems).setStyle(ShoppyMod.VARIABLE_STYLE);

        Component contents = Component.translatable("shop.information.owner.contents", type, selling.getDisplayName(), items).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendSystemMessage(contents);
    }

    @Override
    public InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Vec3 hit = blockHitResult.getLocation().subtract(Vec3.atLowerCornerOf(blockPos));
        ItemStack item = player.getMainHandItem();

        if (ShoppyMod.economyInstance == null) {
            Component noEconomy = Component.translatable("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(noEconomy);
            return InteractionResult.CONSUME;
        }

        if (hit.y() > 0.5 && player.isCrouching()) {
            clearShop(blockHitResult);
            return InteractionResult.SUCCESS;
        }

        if (hit.y() > 0.5) {
            if (this.getSelling().isEmpty()) {
                this.addSellingItem(item.copy());
                Component message;
                if (isBuyingFromPlayer) {
                    message = Component.translatable("shop.setup.owner.add_buying", item.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                } else {
                    message = Component.translatable("shop.setup.owner.add_selling", item.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                }
                player.sendSystemMessage(message);
            } else if (ItemStack.isSameItemSameTags(item, this.getSelling())) {
                this.putItemIntoShop(item);
            }

            return InteractionResult.SUCCESS;
        } else if (hit.y() <= 0.5) {
            if (player.isCrouching()) {
                updateTradingStatus(!isBuyingFromPlayer);
                Component status;
                if (isBuyingFromPlayer) {
                    status = Component.translatable("shop.status.buying").setStyle(ShoppyMod.VARIABLE_STYLE);
                } else {
                    status = Component.translatable("shop.status.selling").setStyle(ShoppyMod.VARIABLE_STYLE);
                }
                Component update = Component.translatable("shop.status.update", status).setStyle(ShoppyMod.CONSTANTS_STYLE);
                player.sendSystemMessage(update);
            } else {
                Component message = Component.translatable("shop.pricing.owner.update").setStyle(ShoppyMod.APPROVAL_STYLE);
                player.sendSystemMessage(message);
                ShoppyMod.awaitingResponse.put(player.getUUID(), this);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (ShoppyMod.economyInstance == null) {
            Component noEconomy = Component.translatable("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendSystemMessage(noEconomy);
            return;
        }
        Component amountBeingSold = Component.literal("x" + this.getSelling().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingSold = this.getSelling().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getSelling())));
            return style;
        });
        Component currencyAmount = ShoppyMod.economyInstance.getDefaultCurrency().format(price);

        Component component;
        if (isBuyingFromPlayer) {
            component = Component.translatable("shop.information.user.buying_info", amountBeingSold, itemBeingSold, currencyAmount).setStyle(ShoppyMod.CONSTANTS_STYLE);
        } else {
            component = Component.translatable("shop.information.user.selling_info", amountBeingSold, itemBeingSold, currencyAmount).setStyle(ShoppyMod.CONSTANTS_STYLE);
        }

        player.sendSystemMessage(component);
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("buyingFromPlayer", isBuyingFromPlayer);
        tag.put("selling", selling.save(new CompoundTag()));
        tag.putInt("price", price);
        tag.putUUID("owner", owner);
        return tag;
    }

    public void updateTradingStatus(boolean value) {
        this.isBuyingFromPlayer = value;
        markUpdated();
    }

    public void setPrice(int price) {
        this.price = price;
        markUpdated();
    }

    public int getPrice() {
        return price;
    }

    public boolean isBuyingFromPlayer() {
        return isBuyingFromPlayer;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (getOwner().equals(player.getUUID())) {
            return ShoppingMenuOwner.realContainer(i, inventory, this, data);
        } else {
            return ShoppingMenu.realContainer(i, inventory, this, data);
        }
    }

    /**
     * UNUSED DO NOT USE
     * @return ALWAYS RETURNS NULL.
     */
    @Override
    protected AbstractContainerMenu createMenu(int i, Inventory inventory) {
        return null;
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
        return selling.isEmpty();
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

        if (pSlot == SELLING_STORED) {
            take = storedSellingItems;
            item = getSelling().copy();
        } else if (pSlot == ShoppingMenuOwner.INSERTED_ITEM) {
            item = getSelling();
            take = getSelling().getCount();
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
        if (slot == SELLING_STORED) {
            storedSellingItems -= amountToRemove;
        }
        markUpdated();
        return item.split(amountToRemove);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return removeItem(slot, 64);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == ShoppingMenuOwner.INSERTED_ITEM) {
            selling = stack;
        }

        markUpdated();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    private static final int[] SLOTS_FOR_REST = new int[]{SELLING_STORED};

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return new int[]{};
        } else {
            return SLOTS_FOR_REST;
        }
    }

    /**
     * this is not how this was meant to be used.
     */
    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        if (storedSellingItems <= maxStorage && i == SELLING_STORED && direction != null && direction != Direction.DOWN
                && ItemStack.isSameItemSameTags(itemStack, selling)) {
            setItem(i, itemStack);
            return true;
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return false;
    }
}
