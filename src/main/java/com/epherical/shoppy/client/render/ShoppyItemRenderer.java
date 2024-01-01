package com.epherical.shoppy.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ShoppyItemRenderer extends EntityRenderer<ItemEntity> {

    private final ItemRenderer itemRenderer;
    private final RandomSource random = RandomSource.create();

    public ShoppyItemRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
    }

    public ResourceLocation getTextureLocation(ItemEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

    public void render(ItemEntity entity, float ff, float tick, PoseStack pose, MultiBufferSource buffer, int i) {
        pose.pushPose();
        ItemStack itemStack = entity.getItem();
        int rand = itemStack.isEmpty() ? 187 : Item.getId(itemStack.getItem()) + itemStack.getDamageValue();
        this.random.setSeed(rand);
        BakedModel model = this.itemRenderer.getModel(itemStack, entity.level(), null, entity.getId());
        boolean is3D = model.isGui3d();
        float offset = Mth.sin(((float)entity.getAge() + tick) / 10.0F + entity.bobOffs) * 0.1F + 0.1F;
        float transform = model.getTransforms().getTransform(ItemDisplayContext.GROUND).scale.y();
        pose.translate(0.0F, offset + 0.25F * transform, 0.0F);
        float currentSpin = entity.getSpin(tick);
        pose.mulPose(Axis.YP.rotation(currentSpin));
        float xScale = model.getTransforms().ground.scale.x();
        float yScale = model.getTransforms().ground.scale.y();
        float zScale = model.getTransforms().ground.scale.z();
        float y;
        float z;
        if (!is3D) {
            float x = -0.0F * (0) * 0.5F * xScale;
            y = -0.0F  * 0.5F * yScale;
            z = -0.09375F *  0.5F * zScale;
            pose.translate(x, y, z);
        }

        pose.pushPose();
        this.itemRenderer.render(itemStack, ItemDisplayContext.GROUND, false, pose, buffer, i, OverlayTexture.NO_OVERLAY, model);
        pose.popPose();
        if (!is3D) {
            pose.translate(0.0F * xScale, 0.0F * yScale, 0.09375F * zScale);
        }

        pose.popPose();
        super.render(entity, ff, tick, pose, buffer, i);
    }
}
