package com.epherical.shoppy.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractNetworking<CLI, SER> {


    public abstract <T> void registerServerToClient(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Context<CLI>> consumer);

    public abstract <T> void registerClientToServer(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Context<SER>> consumer);

    public abstract <T> void sendToClient(T type, ServerPlayer serverPlayer);

    public abstract <T> void sendToServer(T type);


    public static class Context<T> {

        @NotNull
        private Side side;

        @Nullable
        private ServerPlayer player;

        public Context(@NotNull Side side, @Nullable ServerPlayer player) {
            this.side = side;
            this.player = player;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        public Side getSide() {
            return side;
        }
    }

    public enum Side {
        CLIENT,
        SERVER;
    }

}
