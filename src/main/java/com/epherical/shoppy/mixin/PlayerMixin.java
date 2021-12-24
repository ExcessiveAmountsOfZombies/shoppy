package com.epherical.shoppy.mixin;

import com.epherical.shoppy.block.entity.AbstractTradingBlockEntity;
import com.mojang.authlib.GameProfile;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow public abstract GameProfile getGameProfile();

    @Inject(method = "blockActionRestricted", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameType;isBlockPlacingRestricted()Z"), cancellable = true)
    public void restrictsBlockAction(Level level, BlockPos blockPos, GameType gameType, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity entity = level.getBlockEntity(blockPos);
        if (entity instanceof AbstractTradingBlockEntity trading) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                ServerPlayer player = (ServerPlayer) (Object) this;
                // Whether or not the player is an Operator goes first, there might not be any permission providers installed on the server.
                if ((!trading.getOwner().equals(player.getUUID())) && (!player.hasPermissions(4) || !Permissions.check(player, "shoppy.admin.break_shop", 4))) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
