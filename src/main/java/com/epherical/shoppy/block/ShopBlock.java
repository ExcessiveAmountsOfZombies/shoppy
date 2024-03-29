package com.epherical.shoppy.block;

import com.epherical.shoppy.block.entity.ShopBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopBlock extends AbstractTradingBlock {

    private final boolean creative;

    public ShopBlock(Properties properties, boolean creative) {
        super(properties);
        this.creative = creative;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ShopBlockEntity(blockPos, blockState);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
        /*Component info = Component.translatable("shop.information.owner.info").setStyle(ShoppyMod.CONSTANTS_STYLE);
        Component insert = Component.translatable("shop.information.owner.insert").setStyle(ShoppyMod.APPROVAL_STYLE);
        Component reset = Component.translatable("shop.information.owner.reset").setStyle(ShoppyMod.CONSTANTS_STYLE);
        Component update = Component.translatable("shop.information.owner.update").setStyle(ShoppyMod.APPROVAL_STYLE);
        Component pricing = Component.translatable("shop.information.owner.pricing").setStyle(ShoppyMod.CONSTANTS_STYLE);
        list.add(info);
        list.add(insert);
        list.add(reset);
        list.add(update);
        list.add(pricing);*/
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);


    }
}
