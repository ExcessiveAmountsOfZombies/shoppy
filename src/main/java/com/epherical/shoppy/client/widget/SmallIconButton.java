package com.epherical.shoppy.client.widget;

import com.epherical.shoppy.client.screens.BarteringScreenOwner;
import com.epherical.shoppy.objects.Action;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class SmallIconButton extends Button {

    public boolean opened = false;
    private Action.Icon icon;


    protected SmallIconButton(int $$0, int $$1, int $$2, int $$3, Component $$4, OnPress $$5, CreateNarration $$6, Action.Icon icon) {
        super($$0, $$1, $$2, $$3, $$4, $$5, $$6);
        this.icon = icon;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        int offset = this.getTextureY();
        graphics.blit(BarteringScreenOwner.CONTAINER_BACKGROUND, getX(), getY(), 208, 7 + offset * 9, 9, 9);
        graphics.blit(BarteringScreenOwner.CONTAINER_BACKGROUND, getX() + 2, getY() + 2, 224, 16 + icon.ordinal() * 5, 5, 5);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private int getTextureY() {
        int offset = 1;
        if (!this.active) {
            offset = 0;
        } else if (this.isHovered()) {
            offset = 2;
        }

        return offset;
    }

    public static Builder buttonBuilder(@NotNull Component component, Button.@NotNull OnPress press) {
        return new Builder(component, press);
    }

    public static class Builder {
        private final Component message;
        private final Button.OnPress onPress;
        @Nullable
        private Tooltip tooltip;
        private int x;
        private int y;
        private int width = 9;
        private int height = 9;
        private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

        private Action.Icon icon = Action.Icon.INCREMENT;

        public Builder(Component $$0, Button.OnPress $$1) {
            this.message = $$0;
            this.onPress = $$1;
        }

        public SmallIconButton.Builder pos(int x, int y) {
            this.x = x;
            this.y = y;
            return this;
        }

        public Builder setIcon(Action.Icon icon) {
            this.icon = icon;
            return this;
        }

        public SmallIconButton.Builder tooltip(@Nullable Tooltip $$0) {
            this.tooltip = $$0;
            return this;
        }

        public SmallIconButton.Builder createNarration(Button.CreateNarration $$0) {
            this.createNarration = $$0;
            return this;
        }

        public SmallIconButton build() {
            SmallIconButton button = new SmallIconButton(this.x, this.y, this.width, this.height, this.message, this.onPress, this.createNarration, icon);
            button.setTooltip(this.tooltip);
            return button;
        }
    }

    public boolean isOpened() {
        return opened;
    }
}
