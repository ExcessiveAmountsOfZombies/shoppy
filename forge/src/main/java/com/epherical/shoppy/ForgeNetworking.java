package com.epherical.shoppy;

import com.epherical.shoppy.networking.AbstractNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class ForgeNetworking extends AbstractNetworking {

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
    public <T> void registerServerToClient(Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder) {
        INSTANCE.registerMessage(id++, type, encoder, decoder, (t, contextSupplier) -> {

        });

    }
}
