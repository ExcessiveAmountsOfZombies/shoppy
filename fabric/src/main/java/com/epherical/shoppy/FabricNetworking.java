package com.epherical.shoppy;

import com.epherical.shoppy.networking.AbstractNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricNetworking extends AbstractNetworking {

    private int id = 0;

    private boolean client;
    private ResourceLocation modChannel;

    private final Map<Integer, Class<?>> intToClassMap;
    private final Map<Class<?>, ?> classToResponseMap;

    public FabricNetworking(ResourceLocation modChannel, boolean client) {
        intToClassMap = new HashMap<>();
        classToResponseMap = new HashMap<>();
        if (client) {
            ClientPlayNetworking.registerGlobalReceiver(modChannel, (client1, handler, buf, responseSender) -> {


            });
        } else {
            ServerPlayNetworking.registerGlobalReceiver(modChannel, (server, player, handler, buf, responseSender) -> {


            });
        }

        registerClientToServer(SentPacket.class, (message, friendlyByteBuf) -> {

        }, friendlyByteBuf -> {
            return new SentPacket();
        });



    }

    @Override
    public <T> void registerServerToClient(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder) {
        intToClassMap.put(id++, type);





    }

    @Override
    public <T> void registerClientToServer(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder) {

    }

    public <T> void sendPacket(Class<T> type) {
        if (client) {
            ClientPlayNetworking.send(modChannel);
        }

    }
}
