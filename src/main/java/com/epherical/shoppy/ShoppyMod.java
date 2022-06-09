package com.epherical.shoppy;

import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.event.EconomyEvents;
import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.shoppy.block.AbstractTradingBlock;
import com.epherical.shoppy.block.BarteringBlock;
import com.epherical.shoppy.block.CreativeBarteringBlock;
import com.epherical.shoppy.block.CreativeShopBlock;
import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBlock;
import com.epherical.shoppy.block.entity.CreativeShopBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.*;

public class ShoppyMod implements ModInitializer {

    public static BlockEntityType<BarteringBlockEntity> BARTING_STATION_ENTITY;
    public static AbstractTradingBlock BARTERING_STATION;
    public static Item BARTING_STATION_ITEM;

    public static BlockEntityType<CreativeBarteringBlockEntity> CREATIVE_BARTERING_STATION_ENTITY;
    public static CreativeBarteringBlock CREATIVE_BARTERING_STATION;
    public static Item CREATIVE_BARTERING_STATION_ITEM;

    public static BlockEntityType<ShopBlockEntity> SHOP_BLOCK_ENTITY;
    public static ShopBlock SHOP_BLOCK;
    public static Item SHOP_BLOCK_ITEM;

    public static BlockEntityType<CreativeShopBlockEntity> CREATIVE_SHOP_BLOCK_ENTITY;
    public static CreativeShopBlock CREATIVE_SHOP_BLOCK;
    public static Item CREATIVE_SHOP_BLOCK_ITEM;

    public static final CreativeModeTab SHOPPY_ITEM_GROUP = FabricItemGroupBuilder.create(new ResourceLocation("shoppy", "shoppy"))
            .icon(() -> new ItemStack(BARTING_STATION_ITEM))
            .build();

    public static final Style CONSTANTS_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#999999"));
    public static final Style VARIABLE_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#ffd500"));
    public static final Style APPROVAL_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#6ba4ff"));
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#b31717"));

    public static Economy economyInstance;
    public static Map<UUID, ShopBlockEntity> awaitingResponse = Maps.newHashMap();

    @Nullable
    public static UniqueUser ADMIN = null;

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

        ChatEvent.PRE_CHAT_EVENT.register((msg, player) -> {
            if (awaitingResponse.containsKey(player.getUUID())) {
                ShopBlockEntity shopBlock = awaitingResponse.get(player.getUUID());
                try {
                    int number = Integer.parseInt(msg);
                    if (number >= 0) {
                        shopBlock.setPrice(number);
                        Component compText = economyInstance.getDefaultCurrency().format(number);
                        Component success = Component.translatable("shop.pricing.owner.update_complete", compText).setStyle(APPROVAL_STYLE);
                        player.sendSystemMessage(success);
                        awaitingResponse.remove(player.getUUID());
                        return true;
                    }
                } catch (NumberFormatException ignored) {
                    awaitingResponse.remove(player.getUUID());
                    Component message = Component.translatable("shop.pricing.owner.update_fail").setStyle(ERROR_STYLE);
                    player.sendSystemMessage(message);
                    return true;
                }
            }
            return false;
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
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

    private int createNPCShop(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        BlockPos possibleShopPos = BlockPosArgument.getLoadedBlockPos(stack, "block");
        ServerPlayer player = stack.getSource().getPlayerOrException();
        BlockEntity blockEntity = player.getLevel().getBlockEntity(possibleShopPos);
        if (blockEntity instanceof CreativeBlock creativeBlock) {
            if (ADMIN != null) {
                creativeBlock.setOwner(ADMIN.getUserID());
            } else {
                stack.getSource().sendFailure(Component.nullToEmpty("Admin account could not be found. You are still the owner."));
            }
        }
        return 1;
    }

    private int createAdminShop(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        BlockPos possibleShopPos = BlockPosArgument.getLoadedBlockPos(stack, "block");
        ServerPlayer player = stack.getSource().getPlayerOrException();
        BlockEntity blockEntity = player.getLevel().getBlockEntity(possibleShopPos);
        if (ADMIN != null && blockEntity instanceof CreativeBlock creativeBlock && creativeBlock.getOwner().equals(ADMIN.getUserID())) {
            creativeBlock.setOwner(player.getUUID());
        }
        return 1;
    }
}
