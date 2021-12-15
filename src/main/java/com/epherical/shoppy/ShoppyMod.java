package com.epherical.shoppy;

import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.ShopBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class ShoppyMod implements ModInitializer {

    public static BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY;
    public static ShopBlock SHOP_BLOCK;

    @Override
    public void onInitialize() {
        SHOP_BLOCK = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "shop_block"), new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        SHOP_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, SHOP_BLOCK).build());
    }
}
