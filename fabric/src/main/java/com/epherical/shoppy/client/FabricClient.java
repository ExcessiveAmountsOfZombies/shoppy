package com.epherical.shoppy.client;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.client.render.BarteringBlockRenderer;
import com.epherical.shoppy.client.render.ShopBlockRenderer;
import com.epherical.shoppy.client.screens.BarteringScreen;
import com.epherical.shoppy.client.screens.BarteringScreenOwner;
import com.epherical.shoppy.client.screens.ShoppingScreen;
import com.epherical.shoppy.client.screens.ShoppingScreenOwner;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

@Environment(EnvType.CLIENT)
public class FabricClient extends ShoppyClient implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tick++;
            if (tick == Integer.MAX_VALUE) {
                tick = Integer.MIN_VALUE;
            }
        });
        BlockRenderLayerMap.INSTANCE.putBlock(ShoppyMod.BARTERING_STATION, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ShoppyMod.SHOP_BLOCK, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ShoppyMod.CREATIVE_BARTERING_STATION, RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ShoppyMod.CREATIVE_SHOP_BLOCK, RenderType.cutout());
        BlockEntityRenderers.register(ShoppyMod.BARTING_STATION_ENTITY, BarteringBlockRenderer::new);
        BlockEntityRenderers.register(ShoppyMod.SHOP_BLOCK_ENTITY, ShopBlockRenderer::new);
        BlockEntityRenderers.register(ShoppyMod.CREATIVE_BARTERING_STATION_ENTITY, BarteringBlockRenderer::new);
        BlockEntityRenderers.register(ShoppyMod.CREATIVE_SHOP_BLOCK_ENTITY, ShopBlockRenderer::new);

        MenuScreens.register(ShoppyMod.BARTERING_MENU, BarteringScreen::new);
        MenuScreens.register(ShoppyMod.BARTERING_MENU_OWNER, BarteringScreenOwner::new);
        MenuScreens.register(ShoppyMod.SHOPPING_MENU, ShoppingScreen::new);
        MenuScreens.register(ShoppyMod.SHOPPING_MENU_OWNER, ShoppingScreenOwner::new);
    }
}
