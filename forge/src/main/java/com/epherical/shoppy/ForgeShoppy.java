package com.epherical.shoppy;

import com.epherical.octoecon.api.event.EconomyChangeEvent;
import com.epherical.shoppy.block.BarteringBlock;
import com.epherical.shoppy.block.CreativeBarteringBlock;
import com.epherical.shoppy.block.CreativeShopBlock;
import com.epherical.shoppy.block.ShopBlock;
import com.epherical.shoppy.block.entity.AbstractTradingBlockEntity;
import com.epherical.shoppy.block.entity.BarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeBarteringBlockEntity;
import com.epherical.shoppy.block.entity.CreativeShopBlockEntity;
import com.epherical.shoppy.block.entity.ShopBlockEntity;
import net.minecraft.Util;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod("shoppy")
public class ForgeShoppy extends ShoppyMod {

    public static final PermissionNode<Boolean> ADMIN_BREAK = new PermissionNode<>("shoppy", "admin.break_shop", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
        return player != null && player.hasPermissions(4);
    });


    public static CreativeModeTab ITEM_GROUP;

    public ForgeShoppy() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ForgeClient::clientSetup);
        bus.addListener(ForgeClient::registerRenderers);

        ITEM_GROUP = new CreativeModeTab("shoppy") {
            @Override
            public ItemStack makeIcon() {
                return new ItemStack(ShoppyMod.BARTING_STATION_ITEM);
            }
        };

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void economyChange(EconomyChangeEvent event) {
        economyInstance = event.getEconomy();
        ADMIN = economyInstance.getOrCreatePlayerAccount(Util.NIL_UUID);
        if (ADMIN != null) {
            ADMIN.depositMoney(economyInstance.getDefaultCurrency(), 240204204, "admin account");
        }
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent.Submitted event) {
        ServerPlayer player = event.getPlayer();
        String msg = event.getMessage().getString();
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
                    event.setCanceled(true);
                }
            } catch (NumberFormatException ignored) {
                awaitingResponse.remove(player.getUUID());
                Component message = Component.translatable("shop.pricing.owner.update_fail", msg).setStyle(ERROR_STYLE);
                MutableComponent error = Component.literal("");
                for (char c : msg.toCharArray()) {
                    if (Character.isDigit(c)) {
                        error.append(Component.literal(String.valueOf(c)).setStyle(APPROVAL_STYLE));
                    } else {
                        error.append(Component.literal(String.valueOf(c)).setStyle(ERROR_STYLE));
                    }
                }
                Component otherMessage = Component.translatable("Errors Indicated in red: %s", error).setStyle(CONSTANTS_STYLE);
                player.sendSystemMessage(message);
                player.sendSystemMessage(otherMessage);
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(literal("shoppy")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(4))
                .then(literal("admin_shop")
                        .then(argument("block", BlockPosArgument.blockPos())
                                .executes(this::createAdminShop)))
                .then(literal("npc_shop")
                        .then(argument("block", BlockPosArgument.blockPos())
                                .executes(this::createNPCShop))));
    }

    @SubscribeEvent
    public void registerPermissions(PermissionGatherEvent.Nodes event) {
        event.addNodes(ADMIN_BREAK);
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        if (FMLEnvironment.dist.isDedicatedServer()) {
            Player player = event.getEntity();
            BlockPos pos = event.getPos();
            ServerLevel level = (ServerLevel) event.getEntity().getLevel();
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof AbstractTradingBlockEntity trading) {
                if ((!trading.getOwner().equals(player.getUUID())) && (!player.hasPermissions(4) || PermissionAPI.getPermission((ServerPlayer) player, ADMIN_BREAK))) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void registerEvent(RegisterEvent event) {
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
                BARTING_STATION_ITEM = new BlockItem(BARTERING_STATION, new Item.Properties().tab(ITEM_GROUP));
                SHOP_BLOCK_ITEM = new BlockItem(SHOP_BLOCK, new Item.Properties().tab(ITEM_GROUP));
                CREATIVE_BARTERING_STATION_ITEM = new BlockItem(CREATIVE_BARTERING_STATION, new Item.Properties().tab(ITEM_GROUP));
                CREATIVE_SHOP_BLOCK_ITEM = new BlockItem(CREATIVE_SHOP_BLOCK, new Item.Properties().tab(ITEM_GROUP));

                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "bartering_station"), () -> BARTING_STATION_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "shop_block"), () -> SHOP_BLOCK_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "creative_bartering_station"), () -> CREATIVE_BARTERING_STATION_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "creative_shop_block"), () -> CREATIVE_SHOP_BLOCK_ITEM);
            } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
                BARTERING_STATION = new BarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion());
                SHOP_BLOCK = new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion(), false);
                CREATIVE_BARTERING_STATION = new CreativeBarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion());
                CREATIVE_SHOP_BLOCK = new CreativeShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion(), true);
                event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("shoppy", "bartering_station"), () -> BARTERING_STATION);
                event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("shoppy", "shop_block"), () -> SHOP_BLOCK);
                event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("shoppy", "creative_bartering_station"), () -> CREATIVE_BARTERING_STATION);
                event.register(ForgeRegistries.Keys.BLOCKS, new ResourceLocation("shoppy", "creative_shop_block"), () -> CREATIVE_SHOP_BLOCK);

            } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES)) {
                BARTING_STATION_ENTITY = BlockEntityType.Builder.of(BarteringBlockEntity::new, BARTERING_STATION).build(null);
                SHOP_BLOCK_ENTITY = BlockEntityType.Builder.of(ShopBlockEntity::new, SHOP_BLOCK).build(null);
                CREATIVE_BARTERING_STATION_ENTITY = BlockEntityType.Builder.of(CreativeBarteringBlockEntity::new, CREATIVE_BARTERING_STATION).build(null);
                CREATIVE_SHOP_BLOCK_ENTITY = BlockEntityType.Builder.of(CreativeShopBlockEntity::new, CREATIVE_SHOP_BLOCK).build(null);

                event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation("shoppy", "bartering_station_entity"), () -> BARTING_STATION_ENTITY);
                event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation("shoppy", "shop_block_entity"), () -> SHOP_BLOCK_ENTITY);
                event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation("shoppy", "creative_bartering_station_entity"), () -> CREATIVE_BARTERING_STATION_ENTITY);
                event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, new ResourceLocation("shoppy", "creative_shop_block_entity"), () -> CREATIVE_SHOP_BLOCK_ENTITY);
            }
        }
    }


}
