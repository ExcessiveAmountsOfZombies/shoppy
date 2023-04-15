package com.epherical.shoppy.networking.packets;

import com.epherical.shoppy.block.entity.AbstractTradingBlockEntity;
import com.epherical.shoppy.menu.ContainerMenu;
import com.epherical.shoppy.networking.AbstractNetworking;
import net.minecraft.server.level.ServerPlayer;

public record AttemptPurchase() {

    public static void handle(AttemptPurchase purchase, AbstractNetworking.Context<?> context) {
        ServerPlayer player = context.getPlayer();
        if (player != null) {
            player.getServer().execute(() -> {
                if (player.containerMenu instanceof ContainerMenu owner && owner.getContainer() instanceof AbstractTradingBlockEntity tradingBlock) {
                    tradingBlock.attemptPurchase(player, false);
                    // todo; throw error. someone may be trying to do something they shouldn't.
                } else {
                    // todo; throw error. someone may be trying to do something they shouldn't.
                }
            });
        }
    }
}
