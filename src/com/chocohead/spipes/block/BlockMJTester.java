package com.chocohead.spipes.block;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import com.chocohead.spipes.tile.TileEntityFUTester;
import com.chocohead.spipes.tile.TileEntityMJTester;
import com.chocohead.spipes.tile.TileEntityTester;

public class BlockMJTester extends Block {
	public enum Type implements IStringSerializable {
		MJ, FU;

		@Override
		public String getName() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		public static Type valueOf(int ord) {
			return VALUES[ord % VALUES.length];
		}

		private static final Type[] VALUES = values();
	}
	public static final IProperty<Type> TYPE = PropertyEnum.create("type", Type.class);

	public BlockMJTester() {
		super(Material.IRON);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, TYPE);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).ordinal();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(TYPE, Type.valueOf(meta));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		switch (state.getValue(TYPE)) {
		case MJ:
			return new TileEntityMJTester();

		case FU:
			return new TileEntityFUTester();

		default:
			return null;
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!world.isRemote && hand == EnumHand.MAIN_HAND) {
			TileEntity te = world.getTileEntity(pos);
			if (!(te instanceof TileEntityTester)) return false;

			String text;
			if (player.isSneaking()) {
				((TileEntityTester) te).switchSetting();
				text = "tile.stubborn_pipes.mj_tester.switch";
			} else {
				text = "tile.stubborn_pipes.mj_tester.info";
			}
			player.sendMessage(new TextComponentTranslation(text, ((TileEntityTester) te).getSetting().asText()));

			return true;
		} else {
			return false;
		}
	}
}