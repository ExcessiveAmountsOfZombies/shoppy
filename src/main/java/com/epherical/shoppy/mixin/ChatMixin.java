package com.epherical.shoppy.mixin;

import com.epherical.shoppy.ChatEvent;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixin {

    @Shadow public abstract ServerPlayer getPlayer();

    @Inject(method = "handleChat(Lnet/minecraft/network/protocol/game/ServerboundChatPacket;Lnet/minecraft/server/network/FilteredText;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/ServerboundChatPacket;getSignature(Ljava/util/UUID;)Lnet/minecraft/network/chat/MessageSignature;"),
    cancellable = true)
    public void onChat(ServerboundChatPacket serverboundChatPacket, FilteredText<String> filteredText, CallbackInfo ci) {
        boolean wasCancelled = ChatEvent.PRE_CHAT_EVENT.invoker().onPlayerChat(filteredText.filtered(), getPlayer());
        if (wasCancelled) {
            ci.cancel();
        }
    }
}
