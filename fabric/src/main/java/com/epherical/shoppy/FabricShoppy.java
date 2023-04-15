package com.epherical.shoppy;

import com.epherical.octoecon.api.event.EconomyEvents;
import com.epherical.shoppy.block.BarteringBlock;
import com.epherical.shoppy.block.CreativeBarteringBlock;
import com.epherical.shoppy.block.CreativeShopBlock;
import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeShopBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.menu.shopping.ShoppingMenu;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import java.util.Comparator;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class FabricShoppy extends ShoppyMod implements ModInitializer {

    public static final CreativeModeTab CROPTOPIA_ITEM_GROUP = FabricItemGroup.builder(new ResourceLocation("shoppy", "shoppy_group"))
            .title(Component.translatable("itemGroup.shoppy"))
            .displayItems((featureFlagSet, output, bl) ->
                    BuiltInRegistries.ITEM.entrySet().stream()
                            .filter(entry -> entry.getKey().location().getNamespace().equals("shoppy"))
                            .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getId(entry.getValue())))
                            .forEach(entry -> output.accept(entry.getValue())))
            .icon(() -> new ItemStack(BARTING_STATION_ITEM))
            .build();

    public FabricShoppy() {
        super(new FabricNetworking(MOD_CHANNEL, FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT));
    }


    @Override
    public void onInitialize() {
        int value = 0;

        BARTERING_STATION = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("shoppy", "bartering_station"),
                new BarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));
        BARTING_STATION_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("shoppy", "bartering_station"),
                new BlockItem(BARTERING_STATION, new Item.Properties()));
        BARTING_STATION_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "bartering_station_entity"),
                FabricBlockEntityTypeBuilder.create(BarteringBlockEntity::new, BARTERING_STATION).build());

        SHOP_BLOCK = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("shoppy", "shop_block"),
                new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion(), false));
        SHOP_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("shoppy", "shop_block"),
                new BlockItem(SHOP_BLOCK, new Item.Properties()));
        SHOP_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, SHOP_BLOCK).build());

        CREATIVE_BARTERING_STATION = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("shoppy", "creative_bartering_station"),
                new CreativeBarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));
        CREATIVE_BARTERING_STATION_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("shoppy", "creative_bartering_station"),
                new BlockItem(CREATIVE_BARTERING_STATION, new Item.Properties()));
        CREATIVE_BARTERING_STATION_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "creative_bartering_station_entity"),
                FabricBlockEntityTypeBuilder.create(CreativeBarteringBlockEntity::new, CREATIVE_BARTERING_STATION).build());

        CREATIVE_SHOP_BLOCK = Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation("shoppy", "creative_shop_block"),
                new CreativeShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion(), true));
        CREATIVE_SHOP_BLOCK_ITEM = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation("shoppy", "creative_shop_block"),
                new BlockItem(CREATIVE_SHOP_BLOCK, new Item.Properties()));
        CREATIVE_SHOP_BLOCK_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "creative_shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(CreativeShopBlockEntity::new, CREATIVE_SHOP_BLOCK).build());

        BARTERING_MENU = Registry.register(BuiltInRegistries.MENU, new ResourceLocation("shoppy", "bartering_menu"),
                new MenuType<>((pContainerId, pPlayerInventory) -> new BarteringMenu(BARTERING_MENU, pContainerId, pPlayerInventory)));
        BARTERING_MENU_OWNER = Registry.register(BuiltInRegistries.MENU, new ResourceLocation("shoppy", "bartering_menu_owner"),
                new MenuType<>((pContainerId, pPlayerInventory) -> new BarteringMenuOwner(BARTERING_MENU_OWNER, pContainerId, pPlayerInventory)));
        SHOPPING_MENU = Registry.register(BuiltInRegistries.MENU, new ResourceLocation("shoppy", "shopping_menu"),
                new MenuType<>((pContainerId, pPlayerInventory) -> new ShoppingMenu(SHOPPING_MENU, pContainerId, pPlayerInventory)));
        SHOPPING_MENU_OWNER = Registry.register(BuiltInRegistries.MENU, new ResourceLocation("shoppy", "shopping_menu_owner"),
                new MenuType<>((pContainerId, pPlayerInventory) -> new ShoppingMenuOwner(SHOPPING_MENU_OWNER, pContainerId, pPlayerInventory)));


        EconomyEvents.ECONOMY_CHANGE_EVENT.register(economy -> {
            economyInstance = economy;
            ADMIN = economyInstance.getOrCreatePlayerAccount(Util.NIL_UUID);
            if (ADMIN != null) {
                ADMIN.depositMoney(economyInstance.getDefaultCurrency(), 240204204, "admin account");
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, commandBuildContext, commandSelection) -> {
            dispatcher.register(literal("shoppy")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                    .then(literal("admin_shop")
                            .then(argument("block", BlockPosArgument.blockPos())
                                    .executes(this::createAdminShop)))
                    .then(literal("npc_shop")
                            .then(argument("block", BlockPosArgument.blockPos())
                                    .executes(this::createNPCShop))));
        });
    }
}
