package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.networking.packets.AttemptPurchase;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BarteringScreen extends AbstractContainerScreen<BarteringMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("shoppy", "textures/gui/container/bartering_page.png");


    public BarteringScreen(BarteringMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;
    }

    @Override
    protected void init() {
        super.init();
        Connection connection = Minecraft.getInstance().getConnection().getConnection();
        this.addRenderableWidget(Button.builder(Component.nullToEmpty("Barter"),var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new AttemptPurchase(), connection);
        }).size(42, 20).pos(leftPos + 84, topPos + 42).build());
    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(graphics, pMouseX, pMouseY, pPartialTick);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = leftPos;
        int top = topPos;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0, 0, 176, 147);
        graphics.drawString(font, "x" + menu.getContainerData().get(0), leftPos + 19, topPos + 22, 0xFFFFFF);
        graphics.drawString(font, "x" + menu.getContainerData().get(1), leftPos + 143, topPos + 18, 0xFFFFFF);
        graphics.drawString(font, "for", leftPos + 75, topPos + 28, 0xFFFFFF);
        //this.blit(pPoseStack, left + 79, top + 34, 0, 126, this.imageWidth, 16);
    }
}
