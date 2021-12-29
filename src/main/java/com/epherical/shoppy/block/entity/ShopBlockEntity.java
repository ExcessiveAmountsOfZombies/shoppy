package com.epherical.shoppy.block.entity;

import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.shoppy.ShoppyMod;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ShopBlockEntity extends AbstractTradingBlockEntity {


    private static final Logger LOGGER = LogManager.getLogger();

    private boolean isBuyingFromPlayer;
    private int price;

    private Economy economy;

    public ShopBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ShoppyMod.SHOP_BLOCK_ENTITY, blockPos, blockState);
        this.isBuyingFromPlayer = false;
        this.price = 0;
        this.economy = ShoppyMod.economyInstance;
    }

    public ShopBlockEntity(BlockEntityType<?> blockEntity, BlockPos blockPos, BlockState blockState) {
        super(blockEntity, blockPos, blockState);
        this.isBuyingFromPlayer = false;
        this.price = 0;
        this.economy = ShoppyMod.economyInstance;
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
    public void clearContent() {
        super.clearContent();
        isBuyingFromPlayer = false;
        markUpdated();
    }

    @Override
    public boolean attemptPurchase(Player player, ItemStack currencyInHand, boolean creativeBlock) {
        Player owner = level.getServer().getPlayerList().getPlayer(this.owner);
        if (economy == null) {
            Component noEconomy = new TranslatableComponent("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(noEconomy, Util.NIL_UUID);
            return false;
        }

        UniqueUser ownerUser = economy.getOrCreatePlayerAccount(this.owner);
        UniqueUser user = economy.getOrCreatePlayerAccount(player.getUUID());
        if (user != null && ownerUser != null) {
            if (isBuyingFromPlayer) {
                if (creativeBlock  && user.hasAmount(economy.getDefaultCurrency(), price)) {
                    return shopBuyFromPlayer(player, currencyInHand, ownerUser, user, owner, true);
                }
                if (ownerUser.hasAmount(economy.getDefaultCurrency(), price)) {
                    return shopBuyFromPlayer(player, currencyInHand, ownerUser, user, owner, false);
                } else {
                    Component msg = new TranslatableComponent("shop.buying.not_enough_funds").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendMessage(msg, Util.NIL_UUID);
                    return false;
                }
            } else {
                if (creativeBlock && user.hasAmount(economy.getDefaultCurrency(), price)) {
                    return shopSellToPlayer(player, currencyInHand, ownerUser, user, owner, true);
                }
                if (user.hasAmount(economy.getDefaultCurrency(), price)) {
                    return shopSellToPlayer(player, currencyInHand, ownerUser, user, owner, false);
                } else {
                    Component notEnoughMoney = new TranslatableComponent("shop.purchase.not_enough_money").setStyle(ShoppyMod.ERROR_STYLE);
                    player.sendMessage(notEnoughMoney, Util.NIL_UUID);
                    return false;
                }
            }
        } else {
            Component noAccounts = new TranslatableComponent("shop.purchase.no_account").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(noAccounts, Util.NIL_UUID);
            LOGGER.error("Missing accounts for a shop transaction!! Player involved {}, UUIDs involved (PURCHASER {}) (OWNER {})", player.getScoreboardName(), player.getUUID(), getOwner());
            LOGGER.error("This could be a result of an economy implementation being unable to handle offline players?");
        }
        return false;
    }



    private boolean shopSellToPlayer(Player player, ItemStack currencyInHand, UniqueUser ownerUser, UniqueUser playerShopping, Player owner, boolean creative) {
        int amountToGive = selling.getCount();
        if (amountToGive > storedSellingItems) {
            Component buyerMsg = new TranslatableComponent("shop.purchase.shop_empty").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(buyerMsg, Util.NIL_UUID);
            if (owner != null) {
                Component location = new TranslatableComponent("X: %s, Y: %s, Z: %s", getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ()).setStyle(ShoppyMod.VARIABLE_STYLE);
                Component ownerMsg = new TranslatableComponent("shop.purchase.owner.shop_empty", location).setStyle(ShoppyMod.CONSTANTS_STYLE);
                owner.sendMessage(ownerMsg, Util.NIL_UUID);
            }
            return false;
        } else if (player.getInventory().getFreeSlot() == -1) {
            Component component = new TranslatableComponent("common.purchase.full_inventory").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(component, Util.NIL_UUID);
            return false;
        } else {
            player.addItem(selling.copy());
            if (creative) {
                playerShopping.withdrawMoney(economy.getDefaultCurrency(), price, "creative shop purchase");
            } else {
                playerShopping.sendTo(ownerUser, economy.getDefaultCurrency(), price);
                storedSellingItems -= amountToGive;
            }
            Component buyer = new TranslatableComponent("shop.purchase.transaction_success", selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
            player.sendMessage(buyer, Util.NIL_UUID);
            if (owner != null) {
                Component sellerMsg = new TranslatableComponent("shop.purchase.owner.transaction_success", player.getDisplayName(), selling.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                owner.sendMessage(sellerMsg, Util.NIL_UUID);
            }
        }
        return true;
    }

    private boolean shopBuyFromPlayer(Player player, ItemStack currencyInHand, UniqueUser ownerUser, UniqueUser playerShopping, Player owner, boolean creative) {
        int moneyToGiveToPlayer = price;
        int storageLeft = remainingItemStorage() - selling.getCount();
        if (storageLeft < 0) {
            Component notEnoughSpace = new TranslatableComponent("shop.buying.full").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(notEnoughSpace, Util.NIL_UUID);
            return false;
        } else if (currencyInHand.isEmpty() || !ItemStack.isSameItemSameTags(currencyInHand, selling)) {
            Component notSameItem = new TranslatableComponent("shop.buying.no_held_item").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(notSameItem, Util.NIL_UUID);
            return false;
        } else if (currencyInHand.getCount() < selling.getCount()) {
            Component notEnough = new TranslatableComponent("shop.buying.not_enough_items").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(notEnough, Util.NIL_UUID);
            return false;
        } else {
            currencyInHand.shrink(selling.getCount());
            if (creative) {
                playerShopping.depositMoney(economy.getDefaultCurrency(), moneyToGiveToPlayer, "creative mode shop");
            } else {
                ownerUser.sendTo(playerShopping, economy.getDefaultCurrency(), moneyToGiveToPlayer);
                storedSellingItems += selling.getCount();
            }
            Component priceComp = economy.getDefaultCurrency().format(price);
            Component seller = new TranslatableComponent("shop.buying.player.success", selling.getDisplayName(), priceComp).setStyle(ShoppyMod.APPROVAL_STYLE);
            player.sendMessage(seller, Util.NIL_UUID);
            if (owner != null) {
                Component buyerMsg = new TranslatableComponent("shop.buying.owner.success", player.getDisplayName(), selling.getDisplayName(), priceComp).setStyle(ShoppyMod.APPROVAL_STYLE);
                owner.sendMessage(buyerMsg, Util.NIL_UUID);
            }
        }
        return true;
    }

    @Override
    public void sendInformationToOwner(Player player) {
        if (economy == null) {
            Component noEconomy = new TranslatableComponent("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(noEconomy, Util.NIL_UUID);
            return;
        }

        Component type;
        if (isBuyingFromPlayer) {
            type = new TranslatableComponent("shop.status.buy").setStyle(ShoppyMod.VARIABLE_STYLE);
        } else {
            type = new TranslatableComponent("shop.status.sell").setStyle(ShoppyMod.VARIABLE_STYLE);
        }
        Component items = new TextComponent("" + storedSellingItems).setStyle(ShoppyMod.VARIABLE_STYLE);

        Component contents = new TranslatableComponent("shop.information.owner.contents", type, selling.getDisplayName(), items).setStyle(ShoppyMod.CONSTANTS_STYLE);
        player.sendMessage(contents, Util.NIL_UUID);
    }

    @Override
    public InteractionResult interactWithTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Vec3 hit = blockHitResult.getLocation().subtract(Vec3.atLowerCornerOf(blockPos));
        ItemStack item = player.getMainHandItem();

        if (economy == null) {
            Component noEconomy = new TranslatableComponent("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(noEconomy, Util.NIL_UUID);
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
                    message = new TranslatableComponent("shop.setup.owner.add_buying", item.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                } else {
                    message = new TranslatableComponent("shop.setup.owner.add_selling", item.getDisplayName()).setStyle(ShoppyMod.APPROVAL_STYLE);
                }
                player.sendMessage(message, Util.NIL_UUID);
            } else if (ItemStack.isSameItemSameTags(item, this.getSelling())) {
                this.putItemIntoShop(item);
            }

            return InteractionResult.SUCCESS;
        } else if (hit.y() <= 0.5) {
            if (player.isCrouching()) {
                updateTradingStatus(!isBuyingFromPlayer);
                Component status;
                if (isBuyingFromPlayer) {
                    status = new TranslatableComponent("shop.status.buying").setStyle(ShoppyMod.VARIABLE_STYLE);
                } else {
                    status = new TranslatableComponent("shop.status.selling").setStyle(ShoppyMod.VARIABLE_STYLE);
                }
                Component update = new TranslatableComponent("shop.status.update", status).setStyle(ShoppyMod.CONSTANTS_STYLE);
                player.sendMessage(update, Util.NIL_UUID);
            } else {
                Component message = new TranslatableComponent("shop.pricing.owner.update").setStyle(ShoppyMod.APPROVAL_STYLE);
                player.sendMessage(message, Util.NIL_UUID);
                ShoppyMod.awaitingResponse.put(player.getUUID(), this);
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    public void userLeftClickTradingBlock(BlockState blockState, Level level, BlockPos blockPos, Player player) {
        if (economy == null) {
            Component noEconomy = new TranslatableComponent("shop.error.no_economy").setStyle(ShoppyMod.ERROR_STYLE);
            player.sendMessage(noEconomy, Util.NIL_UUID);
            return;
        }
        Component amountBeingSold = new TextComponent("x" + this.getSelling().getCount()).setStyle(ShoppyMod.VARIABLE_STYLE);
        Component itemBeingSold = this.getSelling().getDisplayName().copy().withStyle(style -> {
            style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new HoverEvent.ItemStackInfo(this.getSelling())));
            return style;
        });
        Component currencyAmount = economy.getDefaultCurrency().format(price);

        Component component;
        if (isBuyingFromPlayer) {
            component = new TranslatableComponent("shop.information.user.buying_info", amountBeingSold, itemBeingSold, currencyAmount).setStyle(ShoppyMod.CONSTANTS_STYLE);
        } else {
            component = new TranslatableComponent("shop.information.user.selling_info", amountBeingSold, itemBeingSold, currencyAmount).setStyle(ShoppyMod.CONSTANTS_STYLE);
        }

        player.sendMessage(component, Util.NIL_UUID);
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

    private void updateTradingStatus(boolean value) {
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
}
