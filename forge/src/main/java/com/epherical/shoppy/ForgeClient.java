package com.epherical.shoppy;

import com.epherical.shoppy.client.render.BarteringBlockRenderer;
import com.epherical.shoppy.client.render.ShopBlockRenderer;
import com.epherical.shoppy.client.screens.BarteringScreen;
import com.epherical.shoppy.client.screens.BarteringScreenOwner;
import com.epherical.shoppy.client.screens.ShoppingScreen;
import com.epherical.shoppy.client.screens.ShoppingScreenOwner;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static com.epherical.shoppy.client.ShoppyClient.tick;

public class ForgeClient {


    public static void clientSetup(FMLClientSetupEvent event) {
        ItemBlockRenderTypes.setRenderLayer(ShoppyMod.BARTERING_STATION, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShoppyMod.SHOP_BLOCK, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShoppyMod.CREATIVE_BARTERING_STATION, RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(ShoppyMod.CREATIVE_SHOP_BLOCK, RenderType.cutout());

        MenuScreens.register(ForgeShoppy.BARTERING_MENU, BarteringScreen::new);
        MenuScreens.register(ForgeShoppy.BARTERING_MENU_OWNER, BarteringScreenOwner::new);
        MenuScreens.register(ForgeShoppy.SHOPPING_MENU, ShoppingScreen::new);
        MenuScreens.register(ForgeShoppy.SHOPPING_MENU_OWNER, ShoppingScreenOwner::new);

        MinecraftForge.EVENT_BUS.register(new ForgeClient());

    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ShoppyMod.BARTING_STATION_ENTITY, BarteringBlockRenderer::new);
        event.registerBlockEntityRenderer(ShoppyMod.SHOP_BLOCK_ENTITY, ShopBlockRenderer::new);
        event.registerBlockEntityRenderer(ShoppyMod.CREATIVE_BARTERING_STATION_ENTITY, BarteringBlockRenderer::new);
        event.registerBlockEntityRenderer(ShoppyMod.CREATIVE_SHOP_BLOCK_ENTITY, ShopBlockRenderer::new);
    }

    @SubscribeEvent
    public void onEndTick(TickEvent.ClientTickEvent event) {
        tick++;
        if (tick == Integer.MAX_VALUE) {
            tick = Integer.MIN_VALUE;
        }
    }
}
