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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();;
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
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String msg = event.getMessage();
        if (awaitingResponse.containsKey(player.getUUID())) {
            ShopBlockEntity shopBlock = awaitingResponse.get(player.getUUID());
            try {
                int number = Integer.parseInt(msg);
                if (number >= 0) {
                    shopBlock.setPrice(number);
                    Component compText = economyInstance.getDefaultCurrency().format(number);
                    Component success = new TranslatableComponent("shop.pricing.owner.update_complete", compText).setStyle(APPROVAL_STYLE);
                    player.sendMessage(success, Util.NIL_UUID);
                    awaitingResponse.remove(player.getUUID());
                    event.setCanceled(true);
                }
            } catch (NumberFormatException ignored) {
                awaitingResponse.remove(player.getUUID());
                Component message = new TranslatableComponent("shop.pricing.owner.update_fail").setStyle(ERROR_STYLE);
                player.sendMessage(message, Util.NIL_UUID);
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
            Player player = event.getPlayer();
            BlockPos pos = event.getPos();
            ServerLevel level = (ServerLevel) event.getPlayer().getLevel();
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
        public static void blockRegister(RegistryEvent.Register<Block> blockRegister) {
            BARTERING_STATION = new BarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                    .sound(SoundType.WOOD).noOcclusion());
            SHOP_BLOCK = new ShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                    .sound(SoundType.WOOD).noOcclusion(), false);
            CREATIVE_BARTERING_STATION = new CreativeBarteringBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                    .sound(SoundType.WOOD).noOcclusion());
            CREATIVE_SHOP_BLOCK = new CreativeShopBlock(BlockBehaviour.Properties.of(Material.WOOD).strength(2.5F, 1200F)
                    .sound(SoundType.WOOD).noOcclusion(), true);

            BARTERING_STATION.setRegistryName(new ResourceLocation("shoppy", "bartering_station"));
            SHOP_BLOCK.setRegistryName(new ResourceLocation("shoppy", "shop_block"));
            CREATIVE_BARTERING_STATION.setRegistryName(new ResourceLocation("shoppy", "creative_bartering_station"));
            CREATIVE_SHOP_BLOCK.setRegistryName(new ResourceLocation("shoppy", "creative_shop_block"));

            blockRegister.getRegistry().registerAll(BARTERING_STATION, SHOP_BLOCK, CREATIVE_BARTERING_STATION, CREATIVE_SHOP_BLOCK);

        }

        @SubscribeEvent
        public static void itemRegister(RegistryEvent.Register<Item> itemRegister) {
            BARTING_STATION_ITEM = new BlockItem(BARTERING_STATION, new Item.Properties().tab(ITEM_GROUP));
            SHOP_BLOCK_ITEM = new BlockItem(SHOP_BLOCK, new Item.Properties().tab(ITEM_GROUP));
            CREATIVE_BARTERING_STATION_ITEM = new BlockItem(CREATIVE_BARTERING_STATION, new Item.Properties().tab(ITEM_GROUP));
            CREATIVE_SHOP_BLOCK_ITEM = new BlockItem(CREATIVE_SHOP_BLOCK, new Item.Properties().tab(ITEM_GROUP));

            BARTING_STATION_ITEM.setRegistryName(new ResourceLocation("shoppy", "bartering_station"));
            SHOP_BLOCK_ITEM.setRegistryName(new ResourceLocation("shoppy", "shop_block"));
            CREATIVE_BARTERING_STATION_ITEM.setRegistryName(new ResourceLocation("shoppy", "creative_bartering_station"));
            CREATIVE_SHOP_BLOCK_ITEM.setRegistryName(new ResourceLocation("shoppy", "creative_shop_block"));

            itemRegister.getRegistry().registerAll(BARTING_STATION_ITEM, SHOP_BLOCK_ITEM, CREATIVE_BARTERING_STATION_ITEM, CREATIVE_SHOP_BLOCK_ITEM);
        }

        @SubscribeEvent
        public static void blockEntityRegister(RegistryEvent.Register<BlockEntityType<?>> blockEntityTypeRegister) {
            BARTING_STATION_ENTITY = BlockEntityType.Builder.of(BarteringBlockEntity::new, BARTERING_STATION).build(null);
            SHOP_BLOCK_ENTITY = BlockEntityType.Builder.of(ShopBlockEntity::new, SHOP_BLOCK).build(null);
            CREATIVE_BARTERING_STATION_ENTITY = BlockEntityType.Builder.of(CreativeBarteringBlockEntity::new, CREATIVE_BARTERING_STATION).build(null);
            CREATIVE_SHOP_BLOCK_ENTITY = BlockEntityType.Builder.of(CreativeShopBlockEntity::new, CREATIVE_SHOP_BLOCK).build(null);

            BARTING_STATION_ENTITY.setRegistryName(new ResourceLocation("shoppy", "bartering_station_entity"));
            SHOP_BLOCK_ENTITY.setRegistryName(new ResourceLocation("shoppy", "shop_block_entity"));
            CREATIVE_BARTERING_STATION_ENTITY.setRegistryName(new ResourceLocation("shoppy", "creative_bartering_station_entity"));
            CREATIVE_SHOP_BLOCK_ENTITY.setRegistryName(new ResourceLocation("shoppy", "creative_shop_block_entity"));

            blockEntityTypeRegister.getRegistry().registerAll(BARTING_STATION_ENTITY, SHOP_BLOCK_ENTITY,
                    CREATIVE_BARTERING_STATION_ENTITY, CREATIVE_SHOP_BLOCK_ENTITY);
        }
    }


}
