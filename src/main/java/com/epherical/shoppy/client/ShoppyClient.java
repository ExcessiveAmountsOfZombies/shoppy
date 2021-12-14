package com.epherical.shoppy.client;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.client.render.ShopBlockRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.RenderType;

@Environment(EnvType.CLIENT)
public class ShoppyClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ShoppyMod.SHOP_BLOCK, RenderType.cutout());
        BlockEntityRendererRegistry.register(ShoppyMod.SHOP_BLOCK_ENTITY, ShopBlockRenderer::new);
    }
}
