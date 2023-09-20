package com.epherical.shoppy.networking.packets;

import com.epherical.epherolib.networking.AbstractNetworking;
import com.epherical.shoppy.block.entity.AbstractTradingBlockEntity;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import com.epherical.shoppy.menu.MenuOwner;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import com.epherical.shoppy.objects.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record SlotManipulation(int slot, Action action) {
    // todo; the method for handling everything became poorly designed once I added shop blocks. whoops.


    public static void handle(SlotManipulation slotManipulation, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                if (player.containerMenu instanceof MenuOwner menu) {
                    if (menu.getContainer() instanceof BarteringBlockEntity blockEntity
                            && blockEntity.getOwner().equals(player.getUUID())) {
                        switch (slotManipulation.action()) {
                            case INCREMENT -> slotManipulation.handleIncrement(blockEntity);
                            case DECREMENT -> slotManipulation.handleDecrement(blockEntity);
                            case INSERT_SLOT -> slotManipulation.insertSingleSlot(player, blockEntity);
                            case INSERT_ALL -> slotManipulation.insertAll(player, blockEntity);
                            case REMOVE_ALL -> slotManipulation.removeAllSlots(player, blockEntity);
                            case REMOVE_STACK -> slotManipulation.removeSingleSlot(player, blockEntity);
                        }
                        return;
                    } else if (menu.getContainer() instanceof ShopBlockEntity blockEntity
                            && blockEntity.getOwner().equals(player.getUUID())) {
                        switch (slotManipulation.action()) {
                            case INCREMENT -> {
                                if (slotManipulation.slot == ShoppingMenuOwner.INSERTED_ITEM) {
                                    slotManipulation.abstractIncrement(blockEntity);
                                    blockEntity.markUpdated();
                                }
                            }
                            case DECREMENT -> {
                                if (slotManipulation.slot == ShoppingMenuOwner.INSERTED_ITEM) {
                                    slotManipulation.abstractDecrement(blockEntity);
                                    blockEntity.markUpdated();
                                }
                            }
                            case INSERT_SLOT -> slotManipulation.insertSingleSlotAbstract(player, blockEntity);
                            case INSERT_ALL -> slotManipulation.abstractInsertAll(player, blockEntity);
                            case REMOVE_ALL -> {
                                if (slotManipulation.slot == ShoppingMenuOwner.SELLING_STORED) {
                                    slotManipulation.abstractRemoveAllSlots(player, blockEntity);
                                    blockEntity.markUpdated();
                                }
                            }
                            case REMOVE_STACK -> {
                                if (slotManipulation.slot == ShoppingMenuOwner.SELLING_STORED) {
                                    slotManipulation.abstractRemoveSingle(player, blockEntity);
                                    blockEntity.markUpdated();
                                }
                            }
                        }
                        return;
                    }
                }
                // todo; something isn't right.
            });
        }
    }

    private void handleIncrement(BarteringBlockEntity blockEntity) {
        switch (this.slot()) {
            case BarteringMenuOwner.CURRENCY_ITEM -> blockEntity.getCurrency().grow(1);
            case BarteringMenuOwner.SOLD_ITEMS -> abstractIncrement(blockEntity);
        }
        blockEntity.markUpdated();
    }

    private void abstractIncrement(AbstractTradingBlockEntity blockEntity) {
        blockEntity.getSelling().grow(1);
    }

    private void handleDecrement(BarteringBlockEntity blockEntity) {
        switch (this.slot()) {
            case BarteringMenuOwner.CURRENCY_ITEM -> {
                if (blockEntity.getCurrency().getCount() > 1) {
                    blockEntity.getCurrency().shrink(1);
                }
            }
            case BarteringMenuOwner.SOLD_ITEMS -> {
                abstractDecrement(blockEntity);
            }
        }
        blockEntity.markUpdated();
    }

    private void abstractDecrement(AbstractTradingBlockEntity blockEntity) {
        if (blockEntity.getSelling().getCount() > 1) {
            blockEntity.getSelling().shrink(1);
        }
    }

    private void insertSingleSlot(ServerPlayer player, BarteringBlockEntity blockEntity) {
        insertSingleSlotAbstract(player, blockEntity);
    }

    private void insertSingleSlotAbstract(ServerPlayer player, AbstractTradingBlockEntity blockEntity) {
        if (this.slot == BarteringMenuOwner.SELLING_STORED) {
            if (!blockEntity.getSelling().isEmpty() && blockEntity.remainingItemStorage() != 0) {
                int slotMatchingItem = player.getInventory().findSlotMatchingItem(blockEntity.getSelling());
                if (slotMatchingItem != -1) {
                    blockEntity.putItemIntoShop(player.getInventory().getItem(slotMatchingItem));
                }
            }
        }
    }

    private void insertAll(ServerPlayer player, BarteringBlockEntity blockEntity) {
        abstractInsertAll(player, blockEntity);
    }

    private void abstractInsertAll(ServerPlayer player, AbstractTradingBlockEntity blockEntity) {
        if (this.slot == BarteringMenuOwner.SELLING_STORED) {
            if (!blockEntity.getSelling().isEmpty() && blockEntity.remainingItemStorage() != 0) {
                for (ItemStack item : player.getInventory().items) {
                    if (!item.isEmpty() && ItemStack.isSameItemSameTags(item, blockEntity.getSelling())) {
                        blockEntity.putItemIntoShop(item);
                    }
                }
            }
        }
    }

    private void removeSingleSlot(ServerPlayer player, BarteringBlockEntity blockEntity) {
        switch (this.slot) {
            case BarteringMenuOwner.SELLING_STORED -> abstractRemoveSingle(player, blockEntity);
            case BarteringMenu.CURRENCY_STORED -> blockEntity.emptyProfits(player);
        }
        blockEntity.markUpdated();
    }

    private void abstractRemoveSingle(ServerPlayer player, AbstractTradingBlockEntity blockEntity) {
        blockEntity.emptyItemsToBeSold(player);
    }

    private void removeAllSlots(ServerPlayer player, BarteringBlockEntity blockEntity) {
        switch (this.slot) {
            case BarteringMenuOwner.SELLING_STORED -> {
                abstractRemoveAllSlots(player, blockEntity);
            }
            case BarteringMenu.CURRENCY_STORED -> {
                while (blockEntity.emptyProfits(player));
            }
        }
        blockEntity.markUpdated();
    }

    private void abstractRemoveAllSlots(ServerPlayer player, AbstractTradingBlockEntity blockEntity) {
        while (blockEntity.emptyItemsToBeSold(player));
    }
}
