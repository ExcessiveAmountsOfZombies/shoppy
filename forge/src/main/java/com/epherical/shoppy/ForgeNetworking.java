package com.epherical.shoppy;

import com.epherical.shoppy.networking.AbstractNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeNetworking extends AbstractNetworking<NetworkEvent.Context, NetworkEvent.Context> {

    private static int id = 0;

    public final SimpleChannel INSTANCE;

    private final ResourceLocation modChannel;
    private final String version;

    public ForgeNetworking(ResourceLocation location, String version, Predicate<String> clientAcceptedVersion,
                           Predicate<String> serverAcceptedVersion) {
        this.modChannel = location;
        this.version = version;

        INSTANCE = NetworkRegistry.newSimpleChannel(location, () -> version, clientAcceptedVersion, serverAcceptedVersion);
    }

    @Override
    public <MSG> void registerServerToClient(int id, Class<MSG> type, BiConsumer<MSG, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, MSG> decoder, BiConsumer<MSG, Context<NetworkEvent.Context>> consumer) {
        INSTANCE.registerMessage(id, type, encoder, decoder, (msg, contextSupplier) -> {
            NetworkEvent.Context context = contextSupplier.get();
            Side side = context.getDirection().getReceptionSide().isServer() ? Side.SERVER : Side.CLIENT;
            consumer.accept(msg, new Context<>(side, context.getSender()));
        });
    }

    @Override
    public <T> void registerClientToServer(int id, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder, BiConsumer<T, Context<NetworkEvent.Context>> consumer) {
        INSTANCE.registerMessage(id, type, encoder, decoder, (msg, contextSupplier) -> {
            NetworkEvent.Context context = contextSupplier.get();
            Side side = context.getDirection().getReceptionSide().isServer() ? Side.SERVER : Side.CLIENT;
            consumer.accept(msg, new Context<>(side, context.getSender()));
        });
    }

    @Override
    public <T> void sendToClient(T type, ServerPlayer serverPlayer) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), type);
    }

    @Override
    public <T> void sendToServer(T type) {
        INSTANCE.sendToServer(type);
    }
}
