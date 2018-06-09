package com.chocohead.spipes.item;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.chocohead.spipes.block.BlockMJTester.Type;

public class ItemMJTester extends ItemBlock {
	public ItemMJTester(Block block) {
		super(block);

		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + Type.valueOf(stack.getMetadata()).getName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
		switch (stack.getMetadata()) {
		case 0:
			tooltip.add(I18n.format("tile.stubborn_pipes.mj_tester.mj.tooltip"));
			break;

		case 1:
			tooltip.add(I18n.format("tile.stubborn_pipes.mj_tester.fu.tooltip"));
			break;
		}
	}
}