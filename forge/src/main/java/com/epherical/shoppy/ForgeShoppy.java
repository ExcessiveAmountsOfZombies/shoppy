package com.epherical.shoppy;

import com.epherical.epherolib.networking.ForgeNetworking;
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
import com.epherical.shoppy.menu.bartering.BarteringMenu;
import com.epherical.shoppy.menu.bartering.BarteringMenuOwner;
import com.epherical.shoppy.menu.shopping.ShoppingMenu;
import com.epherical.shoppy.menu.shopping.ShoppingMenuOwner;
import net.minecraft.Util;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
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

import java.util.Comparator;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod("shoppy")
public class ForgeShoppy extends ShoppyMod {

    public static final PermissionNode<Boolean> ADMIN_BREAK = new PermissionNode<>("shoppy", "admin.break_shop", PermissionTypes.BOOLEAN, (player, playerUUID, context) -> {
        return player != null && player.hasPermissions(4);
    });

    public ForgeShoppy() {
        super(new ForgeNetworking(MOD_CHANNEL, "1", s -> true, s -> true));
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(ForgeClient::clientSetup);
        bus.addListener(ForgeClient::registerRenderers);

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
            ServerLevel level = (ServerLevel) event.getEntity().level();
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
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.MENU_TYPES)) {
                BARTERING_MENU = new MenuType<>((pContainerId, pPlayerInventory) -> new BarteringMenu(BARTERING_MENU, pContainerId, pPlayerInventory), FeatureFlags.VANILLA_SET);
                BARTERING_MENU_OWNER = new MenuType<>((pContainerId, pPlayerInventory) -> new BarteringMenuOwner(BARTERING_MENU_OWNER, pContainerId, pPlayerInventory), FeatureFlags.VANILLA_SET);
                SHOPPING_MENU = new MenuType<>((pContainerId, pPlayerInventory) -> new ShoppingMenu(SHOPPING_MENU, pContainerId, pPlayerInventory), FeatureFlags.VANILLA_SET);
                SHOPPING_MENU_OWNER = new MenuType<>((pContainerId, pPlayerInventory) -> new ShoppingMenuOwner(SHOPPING_MENU_OWNER, pContainerId, pPlayerInventory), FeatureFlags.VANILLA_SET);
                event.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation("shoppy", "bartering_menu"), () -> BARTERING_MENU);
                event.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation("shoppy", "bartering_menu_owner"), () -> BARTERING_MENU_OWNER);
                event.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation("shoppy", "shopping_menu"), () -> SHOPPING_MENU);
                event.register(ForgeRegistries.Keys.MENU_TYPES, new ResourceLocation("shoppy", "shopping_menu_owner"), () -> SHOPPING_MENU_OWNER);
            }
            if (event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) {
                CreativeModeTab.builder().title(Component.translatable("itemGroup.shoppy"))
                        .displayItems((featureFlagSet, output) ->
                                BuiltInRegistries.ITEM.entrySet().stream()
                                        .filter(entry -> entry.getKey().location().getNamespace().equals("shoppy"))
                                        .sorted(Comparator.comparing(entry -> BuiltInRegistries.ITEM.getId(entry.getValue())))
                                        .forEach(entry -> output.accept(entry.getValue())))
                        .icon(() -> new ItemStack(BARTING_STATION_ITEM))
                        .build();
                BARTING_STATION_ITEM = new BlockItem(BARTERING_STATION, new Item.Properties());
                SHOP_BLOCK_ITEM = new BlockItem(SHOP_BLOCK, new Item.Properties());
                CREATIVE_BARTERING_STATION_ITEM = new BlockItem(CREATIVE_BARTERING_STATION, new Item.Properties());
                CREATIVE_SHOP_BLOCK_ITEM = new BlockItem(CREATIVE_SHOP_BLOCK, new Item.Properties());

                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "bartering_station"), () -> BARTING_STATION_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "shop_block"), () -> SHOP_BLOCK_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "creative_bartering_station"), () -> CREATIVE_BARTERING_STATION_ITEM);
                event.register(ForgeRegistries.Keys.ITEMS, new ResourceLocation("shoppy", "creative_shop_block"), () -> CREATIVE_SHOP_BLOCK_ITEM);
            } else if (event.getRegistryKey().equals(ForgeRegistries.Keys.BLOCKS)) {
                BARTERING_STATION = new BarteringBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion());
                SHOP_BLOCK = new ShopBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion(), false);
                CREATIVE_BARTERING_STATION = new CreativeBarteringBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F)
                        .sound(SoundType.WOOD).noOcclusion());
                CREATIVE_SHOP_BLOCK = new CreativeShopBlock(BlockBehaviour.Properties.of().strength(2.5F, 1200F)
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
