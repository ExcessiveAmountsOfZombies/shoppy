package com.epherical.shoppy.client.screens;

import com.epherical.shoppy.ShoppyMod;
import com.epherical.shoppy.networking.packets.SlotManipulation;
import com.epherical.shoppy.objects.Action;
import com.epherical.shoppy.client.widget.SmallIconButton;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BarteringScreenOwner extends AbstractContainerScreen<BarteringMenuOwner> {

    public static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("shoppy", "textures/gui/container/bartering_page_owner.png");


    public BarteringScreenOwner(BarteringMenuOwner pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.inventoryLabelY = -3000;
    }

    @Override
    protected void init() {
        super.init();
        Connection connection = Minecraft.getInstance().getConnection().getConnection();
        // Middle blue slot top/bottom
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Up"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.CURRENCY_ITEM, Action.INCREMENT), connection);
        }).pos(leftPos + 74, topPos + 20).setIcon(Action.Icon.INCREMENT).build());
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Down"),var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.CURRENCY_ITEM, Action.DECREMENT), connection);
        }).pos(leftPos + 75, topPos + 50).setIcon(Action.Icon.DECREMENT).build());

        // Middle orange slot top/bottom
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Up"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SOLD_ITEMS, Action.INCREMENT), connection);
        }).pos(leftPos + 93, topPos + 20).setIcon(Action.Icon.INCREMENT).build());
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Down"),var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SOLD_ITEMS, Action.DECREMENT), connection);
        }).pos(leftPos + 92, topPos + 50).setIcon(Action.Icon.DECREMENT).build());


        // Right orange, insert items
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Insert Stack"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SELLING_STORED, Action.INSERT_SLOT), connection);
        }).pos(leftPos + 165, topPos + 30).setIcon(Action.Icon.INCREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Insert a stack."))).build());
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Insert as much as possible"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SELLING_STORED, Action.INSERT_ALL), connection);
        }).pos(leftPos + 165, topPos + 40).setIcon(Action.Icon.INCREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Insert all available items"))).build());

        // Right orange, remove items
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove Stack"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SELLING_STORED, Action.REMOVE_STACK), connection);
        }).pos(leftPos + 127, topPos + 30).setIcon(Action.Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove a stack."))).build());
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove as much as possible"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.SELLING_STORED, Action.REMOVE_ALL), connection);
        }).pos(leftPos + 127, topPos + 40).setIcon(Action.Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove all available items"))).build());

        // Left blue remove "money" gained.
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove Stack"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.CURRENCY_STORED, Action.REMOVE_STACK), connection);
        }).pos(leftPos + 8, topPos + 30).setIcon(Action.Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove a slot of items"))).build());
        this.addRenderableWidget(SmallIconButton.buttonBuilder(Component.nullToEmpty("Remove as much as possible"), var1 -> {
            ShoppyMod.MOD.getNetworking().sendToServer(new SlotManipulation(BarteringMenuOwner.CURRENCY_STORED, Action.REMOVE_ALL), connection);
        }).pos(leftPos + 8, topPos + 40).setIcon(Action.Icon.DECREMENT).tooltip(Tooltip.create(Component.nullToEmpty("Remove all available items"))).build());

    }

    @Override
    public void render(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        this.renderBackground(graphics, pMouseX, pMouseY, pPartialTick);
        super.render(graphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(graphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        int left = leftPos;
        int top = topPos;
        graphics.blit(CONTAINER_BACKGROUND, left, top, 0,0, 176, 147);
        graphics.drawString(font, "Stored", leftPos + 10, topPos + 50, 0xFFFFFF);
        graphics.drawString(font, "Stored", leftPos + 132, topPos + 54, 0xFFFFFF);
        //this.blit(pPoseStack, left + 79, top + 34, 0, 126, this.imageWidth, 16);
    }
}
