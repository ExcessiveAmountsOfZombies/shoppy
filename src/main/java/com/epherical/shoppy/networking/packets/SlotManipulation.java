package com.epherical.shoppy.networking.packets;

import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.menu.BarteringMenu;
import com.epherical.shoppy.menu.BarteringMenuOwner;
import com.epherical.shoppy.networking.AbstractNetworking;
import com.epherical.shoppy.objects.Action;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;


public record SlotManipulation(int slot, Action action) {



    public static void handle(SlotManipulation slotManipulation, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                if (player.containerMenu instanceof BarteringMenuOwner menu
                        && menu.getContainer() instanceof BarteringBlockEntity blockEntity
                        && blockEntity.getOwner().equals(player.getUUID())) {
                    switch (slotManipulation.action()) {
                        case INCREMENT -> slotManipulation.handleIncrement(blockEntity);
                        case DECREMENT -> slotManipulation.handleDecrement(blockEntity);
                        case INSERT_SLOT -> slotManipulation.insertSingleSlot(player, blockEntity);
                        case INSERT_ALL -> slotManipulation.insertAll(player, blockEntity);
                        case REMOVE_ALL -> slotManipulation.removeAllSlots(player, blockEntity);
                        case REMOVE_STACK -> slotManipulation.removeSingleSlot(player, blockEntity);
                    }
                } else {
                    // todo; something isn't right
                }
            });
        }
    }

    private void handleIncrement(BarteringBlockEntity blockEntity) {
        switch (this.slot()) {
            case BarteringMenuOwner.CURRENCY_ITEM -> blockEntity.getCurrency().grow(1);
            case BarteringMenuOwner.SOLD_ITEMS -> blockEntity.getSelling().grow(1);
        }
        blockEntity.markUpdated();
    }

    private void handleDecrement(BarteringBlockEntity blockEntity) {
        switch (this.slot()) {
            case BarteringMenuOwner.CURRENCY_ITEM -> {
                if (blockEntity.getCurrency().getCount() > 1) {
                    blockEntity.getCurrency().shrink(1);
                }
            }
            case BarteringMenuOwner.SOLD_ITEMS -> {
                if (blockEntity.getSelling().getCount() > 1) {
                    blockEntity.getSelling().shrink(1);
                }
            }
        }
        blockEntity.markUpdated();
    }

    private void insertSingleSlot(ServerPlayer player, BarteringBlockEntity blockEntity) {
        if (this.slot == BarteringMenuOwner.SELLING_STORED) {
            if (!blockEntity.getSelling().isEmpty() && blockEntity.remainingCurrencySpaces() != 0) {
                int slotMatchingItem = player.getInventory().findSlotMatchingItem(blockEntity.getSelling());
                if (slotMatchingItem != -1) {
                    blockEntity.putItemIntoShop(player.getInventory().getItem(slotMatchingItem));
                }
            }
        }
    }

    private void insertAll(ServerPlayer player, BarteringBlockEntity blockEntity) {
        if (this.slot == BarteringMenuOwner.SELLING_STORED) {
            if (!blockEntity.getSelling().isEmpty() && blockEntity.remainingCurrencySpaces() != 0) {
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
            case BarteringMenuOwner.SELLING_STORED -> blockEntity.emptyItemsToBeSold(player);
            case BarteringMenu.CURRENCY_STORED -> blockEntity.emptyProfits(player);
        }
        blockEntity.markUpdated();
    }

    private void removeAllSlots(ServerPlayer player, BarteringBlockEntity blockEntity) {
        switch (this.slot) {
            case BarteringMenuOwner.SELLING_STORED -> {
                while (blockEntity.emptyItemsToBeSold(player));
            }
            case BarteringMenu.CURRENCY_STORED -> {
                while (blockEntity.emptyProfits(player));
            }
        }
        blockEntity.markUpdated();
    }
}
