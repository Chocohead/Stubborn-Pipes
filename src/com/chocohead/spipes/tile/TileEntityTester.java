package com.chocohead.spipes.tile;

import java.util.Locale;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import buildcraft.lib.misc.NBTUtilBC;

public abstract class TileEntityTester extends TileEntity {
	public enum Setting {
		MIN_OUT, MAX_OUT, IN;

		public ITextComponent asText() {
			return new TextComponentTranslation("tile.stubborn_pipes.mj_tester." + name().toLowerCase(Locale.ENGLISH));
		}
	}
	protected Setting setting = Setting.MAX_OUT;

	public TileEntityTester() {
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);

		nbt.setTag("setting", NBTUtilBC.writeEnum(setting));

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		setting = NBTUtilBC.readEnum(nbt.getTag("setting"), Setting.class);
	}

	public Setting getSetting() {
		return setting;
	}

	public void switchSetting() {
		switch (setting) {
		case MIN_OUT:
			setting = Setting.MAX_OUT;
			break;

		case MAX_OUT:
			setting = Setting.IN;
			break;

		case IN:
			setting = Setting.MIN_OUT;
			break;
		}
	}
}