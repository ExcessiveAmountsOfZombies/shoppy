package com.epherical.shoppy.mixin;

import com.epherical.shoppy.ChatEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ChatMixin {

    @Shadow
    public abstract ServerPlayer getPlayer();

    @Inject(method = "handleChat(Lnet/minecraft/server/network/TextFilter$FilteredText;)V", at = @At(value = "INVOKE", target = "Ljava/lang/String;isEmpty()Z"), cancellable = true)
    public void onChat(TextFilter.FilteredText filteredText, CallbackInfo ci) {
        boolean wasCancelled = ChatEvent.PRE_CHAT_EVENT.invoker().onPlayerChat(filteredText.getFiltered(), getPlayer());
        if (wasCancelled) {
            ci.cancel();
        }
    }
}
