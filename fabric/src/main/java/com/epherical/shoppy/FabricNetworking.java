package com.epherical.shoppy;

import com.epherical.shoppy.networking.AbstractNetworking;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayChannelHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricNetworking extends AbstractNetworking<ClientPlayNetworking.PlayChannelHandler, PlayChannelHandler> {
    private boolean client;
    private ResourceLocation modChannel;

    private final Int2ObjectArrayMap<PacketWrapper<?, ?>> indices;
    private final Object2ObjectArrayMap<Class<?>, PacketWrapper<?, ?>> classToResponseMap;

    public FabricNetworking(ResourceLocation modChannel, boolean client) {
        this.modChannel = modChannel;
        this.client = client;
        indices = new Int2ObjectArrayMap<>();
        classToResponseMap = new Object2ObjectArrayMap<>();
        registerDecode();
    }

    private <T> void registerDecode() {
        if (client) {
            ClientPlayNetworking.registerGlobalReceiver(modChannel, (client1, handler, buf, responseSender) -> {
                PacketWrapper<T, ClientPlayNetworking.PlayChannelHandler> packetWrapper = (PacketWrapper<T, ClientPlayNetworking.PlayChannelHandler>) indices.get(buf.readVarInt());
                T apply = packetWrapper.decoder.apply(buf);
                packetWrapper.consumer.accept(apply, new Context<>(Side.CLIENT, null));
            });
        } else {
            ServerPlayNetworking.registerGlobalReceiver(modChannel, (server, player, handler, buf, responseSender) -> {
                PacketWrapper<T, PlayChannelHandler> packetWrapper = (PacketWrapper<T, PlayChannelHandler>) indices.get(buf.readVarInt());
                T apply = packetWrapper.decoder.apply(buf);
                packetWrapper.consumer.accept(apply, new Context<>(Side.SERVER, player));
            });
        }
    }

    @Override
    public <T> void registerServerToClient(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                           BiConsumer<T, Context<ClientPlayNetworking.PlayChannelHandler>> consumer) {
        classToResponseMap.put(type, new PacketWrapper<>(id, encoder, decoder, consumer));
    }

    @Override
    public <T> void registerClientToServer(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Context<PlayChannelHandler>> consumer) {
        classToResponseMap.put(type, new PacketWrapper<>(id, encoder, decoder, consumer));
    }

    public <T> void sendToClient(T type, ServerPlayer serverPlayer) {
        if (ServerPlayNetworking.canSend(serverPlayer, modChannel)) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            assemblePacket(type, buf);
            ServerPlayNetworking.send(serverPlayer, modChannel, buf);
        }
    }

    public <T> void sendToServer(T type) {
        if (ClientPlayNetworking.canSend(modChannel)) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            assemblePacket(type, buf);
            ClientPlayNetworking.send(modChannel, buf);
        }
    }

    private <T> void assemblePacket(T type, FriendlyByteBuf buf) {
        PacketWrapper<T, ?> packetWrapper = (PacketWrapper<T, ?>) classToResponseMap.get(type.getClass());
        if (packetWrapper == null) {
            // todo; proper logger message
            System.out.println("NULL MESSAGE, NOT REGISTERED WOOPSIES");
            throw new IllegalArgumentException();
        }
        buf.writeVarInt(packetWrapper.id);
        packetWrapper.encoder.accept(type, buf);
    }

    private class PacketWrapper<T, CON> {

        private int id;
        private BiConsumer<T, Context<CON>> consumer;

        private BiConsumer<T, FriendlyByteBuf> encoder;
        private Function<FriendlyByteBuf, T> decoder;

        public PacketWrapper(int id, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Context<CON>> consumer) {
            this.encoder = encoder;
            this.decoder = decoder;
            this.consumer = consumer;
            this.id = id;
            indices.put(id, this);
        }
    }
}
