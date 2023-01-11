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
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class FabricShoppy extends ShoppyMod implements ModInitializer {

    public static final CreativeModeTab SHOPPY_ITEM_GROUP = FabricItemGroupBuilder.create(new ResourceLocation("shoppy", "shoppy"))
            .icon(() -> new ItemStack(BARTING_STATION_ITEM))
            .build();


    @Override
    public void onInitialize() {
        BARTERING_STATION = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "bartering_station"),
                new BarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));
        BARTING_STATION_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "bartering_station"),
                new BlockItem(BARTERING_STATION, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        BARTING_STATION_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "bartering_station_entity"),
                FabricBlockEntityTypeBuilder.create(BarteringBlockEntity::new, BARTERING_STATION).build());

        SHOP_BLOCK = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "shop_block"),
                new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion(), false));
        SHOP_BLOCK_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "shop_block"),
                new BlockItem(SHOP_BLOCK, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        SHOP_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(ShopBlockEntity::new, SHOP_BLOCK).build());

        CREATIVE_BARTERING_STATION = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "creative_bartering_station"),
                new CreativeBarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion()));
        CREATIVE_BARTERING_STATION_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "creative_bartering_station"),
                new BlockItem(CREATIVE_BARTERING_STATION, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        CREATIVE_BARTERING_STATION_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "creative_bartering_station_entity"),
                FabricBlockEntityTypeBuilder.create(CreativeBarteringBlockEntity::new, CREATIVE_BARTERING_STATION).build());

        CREATIVE_SHOP_BLOCK = Registry.register(Registry.BLOCK, new ResourceLocation("shoppy", "creative_shop_block"),
                new CreativeShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F).sound(SoundType.WOOD).noOcclusion(), true));
        CREATIVE_SHOP_BLOCK_ITEM = Registry.register(Registry.ITEM, new ResourceLocation("shoppy", "creative_shop_block"),
                new BlockItem(CREATIVE_SHOP_BLOCK, new Item.Properties().tab(SHOPPY_ITEM_GROUP)));
        CREATIVE_SHOP_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new ResourceLocation("shoppy", "creative_shop_block_entity"),
                FabricBlockEntityTypeBuilder.create(CreativeShopBlockEntity::new, CREATIVE_SHOP_BLOCK).build());


        EconomyEvents.ECONOMY_CHANGE_EVENT.register(economy -> {
            economyInstance = economy;
            ADMIN = economyInstance.getOrCreatePlayerAccount(Util.NIL_UUID);
            if (ADMIN != null) {
                ADMIN.depositMoney(economyInstance.getDefaultCurrency(), 240204204, "admin account");
            }
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((msg, player, params) -> {
            if (awaitingResponse.containsKey(player.getUUID())) {
                ShopBlockEntity shopBlock = awaitingResponse.get(player.getUUID());
                try {
                    int number = Integer.parseInt(msg.serverContent().getString());
                    if (number >= 0) {
                        shopBlock.setPrice(number);
                        Component compText = economyInstance.getDefaultCurrency().format(number);
                        Component success = Component.translatable("shop.pricing.owner.update_complete", compText).setStyle(APPROVAL_STYLE);
                        player.sendSystemMessage(success);
                        awaitingResponse.remove(player.getUUID());
                        return false;
                    }
                } catch (NumberFormatException ignored) {
                    awaitingResponse.remove(player.getUUID());
                    Component message = Component.translatable("shop.pricing.owner.update_fail", msg.serverContent().getString()).setStyle(ERROR_STYLE);
                    String content = msg.serverContent().getString();
                    MutableComponent error = Component.literal("");
                    for (char c : content.toCharArray()) {
                        if (Character.isDigit(c)) {
                            error.append(Component.literal(String.valueOf(c)).setStyle(APPROVAL_STYLE));
                        } else {
                            error.append(Component.literal(String.valueOf(c)).setStyle(ERROR_STYLE));
                        }
                    }
                    Component otherMessage = Component.translatable("Errors Indicated in red: %s", error).setStyle(CONSTANTS_STYLE);
                    player.sendSystemMessage(message);
                    player.sendSystemMessage(otherMessage);
                    return false;
                }
            }
            return true;
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
