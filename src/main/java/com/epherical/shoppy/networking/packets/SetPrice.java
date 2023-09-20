package com.epherical.shoppy.networking.packets;


import com.epherical.epherolib.networking.AbstractNetworking;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import net.minecraft.server.level.ServerPlayer;

public record SetPrice(int price) {
    // todo; the method for handling everything became poorly designed once I added shop blocks. whoops.


    public static void handle(SetPrice slotManipulation, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                if (player.containerMenu instanceof ShoppingMenuOwner owner
                        && owner.getContainer() instanceof ShopBlockEntity blockEntity
                        && blockEntity.getOwner().equals(player.getUUID())) {
                    blockEntity.setPrice(slotManipulation.price);
                }
            });
        }
    }
}
