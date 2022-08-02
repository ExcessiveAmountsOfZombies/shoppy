package com.epherical.shoppy;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public final class ChatEvent {

    public static final Event<Cancelled> CANCELLED_CHAT_EVENT = EventFactory.createArrayBacked(Cancelled.class, chats -> (msg, player) -> {
        for (Cancelled chat : chats) {
            chat.onCancelled(msg, player);
        }
    });

    public static final Event<PreChat> PRE_CHAT_EVENT = EventFactory.createArrayBacked(PreChat.class, preChats -> (msg, player) -> {
        boolean cancelled = false;
        for (PreChat preChat : preChats) {
            if (preChat.onPlayerChat(msg, player)) {
                cancelled = true;
                CANCELLED_CHAT_EVENT.invoker().onCancelled(msg, player);
            }
        }
        return cancelled;
    });


    public interface PreChat {
        /**
         *
         * @param msg the already filtered text message
         * @param player the player who sent the message
         * @return return true if the chat message should not be sent to anyone else on the server.
         */
        boolean onPlayerChat(String msg, ServerPlayer player);
    }

    public interface Cancelled {
         void onCancelled(String msg, ServerPlayer player);
    }
}
