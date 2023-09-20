package com.epherical.shoppy;

import com.epherical.epherolib.networking.AbstractNetworking;
import com.epherical.octoecon.api.Economy;
import com.epherical.octoecon.api.user.UniqueUser;
import com.epherical.shoppy.block.AbstractTradingBlock;
import com.epherical.shoppy.block.CreativeBarteringBlock;
import com.epherical.shoppy.block.CreativeShopBlock;
import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBlock;
import com.epherical.shoppy.block.entity.CreativeShopBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.menu.shopping.ShoppingMenu;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import com.epherical.shoppy.networking.packets.AttemptPurchase;
import com.epherical.shoppy.networking.packets.SetPrice;
import com.epherical.shoppy.networking.packets.SlotManipulation;
import com.epherical.shoppy.objects.Action;
import com.google.common.collect.Maps;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

public abstract class ShoppyMod {

    public static final Style CONSTANTS_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#999999"));
    public static final Style VARIABLE_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#ffd500"));
    public static final Style APPROVAL_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#6ba4ff"));
    public static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.parseColor("#b31717"));

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

    public static MenuType<BarteringMenu> BARTERING_MENU;
    public static MenuType<BarteringMenuOwner> BARTERING_MENU_OWNER;
    public static MenuType<ShoppingMenu> SHOPPING_MENU;
    public static MenuType<ShoppingMenuOwner> SHOPPING_MENU_OWNER;

    public static Economy economyInstance;
    public static Map<UUID, ShopBlockEntity> awaitingResponse = Maps.newHashMap();


    public static final ResourceLocation MOD_CHANNEL = new ResourceLocation("shoppy", "communication");

    public static ShoppyMod MOD;

    @Nullable
    public static UniqueUser ADMIN = null;


    private final AbstractNetworking<?, ?> networking;

    public ShoppyMod(AbstractNetworking<?, ?> networking) {
        this.networking = networking;
        int id = 0;
        networking.registerClientToServer(id++, AttemptPurchase.class, (attemptPurchase, buf) -> {}, buf -> {
            return new AttemptPurchase();
        }, AttemptPurchase::handle);
        networking.registerClientToServer(id++, SlotManipulation.class, (slotManipulation, buf) -> {
            buf.writeVarInt(slotManipulation.slot());
            buf.writeEnum(slotManipulation.action());
        }, buf -> new SlotManipulation(buf.readVarInt(), buf.readEnum(Action.class)), SlotManipulation::handle);
        networking.registerClientToServer(id++, SetPrice.class, (setPrice, buf) -> {
            buf.writeVarInt(setPrice.price());
        }, buf -> new SetPrice(buf.readVarInt()), SetPrice::handle);
        MOD = this;

    }

    protected int createNPCShop(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        BlockPos possibleShopPos = BlockPosArgument.getLoadedBlockPos(stack, "block");
        ServerPlayer player = stack.getSource().getPlayerOrException();
        BlockEntity blockEntity = player.level().getBlockEntity(possibleShopPos);
        if (blockEntity instanceof CreativeBlock creativeBlock) {
            if (ADMIN != null) {
                creativeBlock.setOwner(ADMIN.getUserID());
            } else {
                stack.getSource().sendFailure(Component.nullToEmpty("Admin account could not be found. You are still the owner."));
            }
        }
        return 1;
    }

    protected int createAdminShop(CommandContext<CommandSourceStack> stack) throws CommandSyntaxException {
        BlockPos possibleShopPos = BlockPosArgument.getLoadedBlockPos(stack, "block");
        ServerPlayer player = stack.getSource().getPlayerOrException();
        BlockEntity blockEntity = player.level().getBlockEntity(possibleShopPos);
        if (ADMIN != null && blockEntity instanceof CreativeBlock creativeBlock && creativeBlock.getOwner().equals(ADMIN.getUserID())) {
            creativeBlock.setOwner(player.getUUID());
        }
        return 1;
    }

    public AbstractNetworking<?, ?> getNetworking() {
        return networking;
    }
}
