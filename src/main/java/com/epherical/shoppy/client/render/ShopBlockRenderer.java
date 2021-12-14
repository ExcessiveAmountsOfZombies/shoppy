package com.epherical.shoppy.client.render;

import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.ShopBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.item.ItemEntity;

@Environment(EnvType.CLIENT)
public class ShopBlockRenderer implements BlockEntityRenderer<ShopBlockEntity> {

    private final ItemRenderer renderer;
    private final Font font;
    private ItemEntity currencyItem;
    private float increment;

    public ShopBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.renderer = Minecraft.getInstance().getItemRenderer();
        this.font = context.getFont();
    }

    @Override
    public void render(ShopBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        Direction direction = blockEntity.getBlockState().getValue(ShopBlock.FACING);
        if (currencyItem == null) {
            currencyItem = new ItemEntity(blockEntity.getLevel(), blockEntity.getBlockPos().getX(), blockEntity.getBlockPos().getY(), blockEntity.getBlockPos().getZ(), blockEntity.getSelling());
        }
        int k = (int)blockEntity.getBlockPos().asLong();
        increment += 0.1;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.55D, 0.5D);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-direction.toYRot()));
        poseStack.scale(0.60F, 0.60F, 0.60F);
        currencyItem.setItem(blockEntity.getSelling());
        if (currencyItem != null) {
            ItemFrame
            Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(currencyItem).render(currencyItem, f, increment, poseStack, multiBufferSource, i);
        }
        if (increment >= 360f) {
            increment = 0f;
        }

        poseStack.pushPose();
        poseStack.translate(-0.350D, -0.55D, -0.74);
        poseStack.mulPose(Vector3f.XP.rotation(22.4f));
        poseStack.mulPose(Vector3f.ZP.rotation(22f));
        poseStack.scale(0.375F, 0.375F, 0.09F);
        renderer.renderStatic(blockEntity.getCurrency(), ItemTransforms.TransformType.GUI, i, j, poseStack, multiBufferSource, k);

        poseStack.pushPose();
        poseStack.translate(-2.0d, 1.3d, 0.4d);
        poseStack.scale(-0.040416667F, -0.040416667F, -0.040416667F);
        poseStack.mulPose(Vector3f.XP.rotation(22.4f));
        poseStack.mulPose(Vector3f.ZP.rotation(22f));

        this.font.drawInBatch("selling x" + blockEntity.getSelling().getCount(), 0, 0, 0, false, poseStack.last().pose(), multiBufferSource, false, 0, i);
        this.font.drawInBatch("for", 20,14,0, false, poseStack.last().pose(), multiBufferSource, false, 0, i);
        String money = "" + blockEntity.getCurrency().getCount();
        int width = this.font.width(money);
        this.font.drawInBatch(money , 26 - (width / 2.0f), 32, 0, false, poseStack.last().pose(), multiBufferSource, false, 0, i);

        poseStack.popPose();
        poseStack.popPose();
        poseStack.popPose();

    }

}
