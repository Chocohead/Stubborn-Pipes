package com.chocohead.spipes.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourSandstone extends PipeBehaviourPowerLimit {
	public PipeBehaviourSandstone(IPipe pipe) {
		super(pipe, 320);
	}

	public PipeBehaviourSandstone(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt, 320);
	}

	@Override
	public boolean canConnect(EnumFacing face, PipeBehaviour other) {
		return true;
	}

	@Override
	public boolean canConnect(EnumFacing face, TileEntity oTile) {
		return false;
	}
}