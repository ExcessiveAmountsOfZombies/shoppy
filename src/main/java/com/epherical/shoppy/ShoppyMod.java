package com.epherical.shoppy;

import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.ShopBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class ShoppyMod implements ModInitializer {

    public static BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY;
    public static ShopBlock SHOP_BLOCK;
    public static Item SHOP_ITEM;

    public static final CreativeModeTab SHOPPY_ITEM_GROUP = FabricItemGroupBuilder.create(new ResourceLocation("shoppy", "shoppy"))
            .icon(() -> new ItemStack(SHOP_ITEM))
            .build();

    public static final Style CONSTANTS_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#999999"));
    public static final Style VARIABLE_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#ffd500"));
    public static final Style APPROVAL_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#6ba4ff"));
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#b31717"));

    @Override
    public void onInitialize() {
        SHOP_BLOCK = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "shop_block"), new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        SHOP_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "shop_block"), new BlockItem(SHOP_BLOCK, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        SHOP_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, SHOP_BLOCK).build());
    }
}
