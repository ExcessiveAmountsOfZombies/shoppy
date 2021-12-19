package com.epherical.shoppy;

import com.epherical.shoppy.block.BarteringBlock;
import com.epherical.shoppy.block.BarteringBlockEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

public class ShoppyMod implements ModInitializer {

    public static BlockEntityType<BarteringBlockEntity> BARTING_STATION_ENTITY;
    public static BarteringBlock BARTERING_STATION;
    public static Item BARTING_STATION_ITEM;

    public static final CreativeModeTab SHOPPY_ITEM_GROUP = FabricItemGroupBuilder.create(new ResourceLocation("shoppy", "shoppy"))
            .icon(() -> new ItemStack(BARTING_STATION_ITEM))
            .build();

    public static final Style CONSTANTS_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#999999"));
    public static final Style VARIABLE_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#ffd500"));
    public static final Style APPROVAL_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#6ba4ff"));
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#b31717"));

    @Override
    public void onInitialize() {
        BARTERING_STATION = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "bartering_station"), new BarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        BARTING_STATION_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "bartering_station"), new BlockItem(BARTERING_STATION, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        BARTING_STATION_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(BarteringBlockEntity::new, BARTERING_STATION).build());
    }
}
