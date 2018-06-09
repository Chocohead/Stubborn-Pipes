package com.chocohead.spipes.logic;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourPowerInput extends PipeBehaviourPowerLimit {
	public PipeBehaviourPowerInput(IPipe pipe, int limit) {
		super(pipe, limit);
	}

	public PipeBehaviourPowerInput(IPipe pipe, NBTTagCompound nbt, int limit) {
		super(pipe, nbt, limit);
	}

	@Override
	public boolean canConnect(EnumFacing face, PipeBehaviour other) {
		return !(other instanceof PipeBehaviourPowerInput);
	}

	@Override
	public boolean canConnect(EnumFacing face, TileEntity tile) {
		return tile.hasCapability(CapabilityEnergy.ENERGY, face);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && facing != null) {
			return CapabilityEnergy.ENERGY.cast(new Energy(facing));
		} else {
			return super.getCapability(capability, facing);
		}
	}

	@Override
	public int getTextureIndex(EnumFacing face) {
		if (face == null) return 0;

		if (pipe.getConnectedPipe(face) != null) {
			return 0;
		}

		TileEntity tile = pipe.getConnectedTile(face);
		if (tile == null) return 0;

		//assert canConnect(face, tile): "Can't connect to " + tile + " on side " + face;
		// ^ Fails for certain neighbouring pipes as they don't implement the Energy Cap.

		return 1;
	}


	public static PipeBehaviour wood(IPipe pipe) {
		return new PipeBehaviourPowerInput(pipe, 320);
	}

	public static PipeBehaviour wood(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerInput(pipe, nbt, 320);
	}

	public static PipeBehaviour emerald(IPipe pipe) {
		return new PipeBehaviourPowerInput(pipe, 2560);
	}

	public static PipeBehaviour emerald(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerInput(pipe, nbt, 2560);
	}

	private class Energy implements IEnergyStorage {
		final EnumFacing face;

		public Energy(EnumFacing face) {
			this.face = face;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			PipeFlowFUMJ flow = (PipeFlowFUMJ) pipe.getFlow();

			if (!simulate) flow.flow();
			return flow.sections.get(face.getOpposite()).receivePowerInternal(maxReceive);
		}

		@Override
		public int getEnergyStored() {
			return 0;
		}

		@Override
		public int getMaxEnergyStored() {
			return 0;
		}

		@Override
		public boolean canExtract() {
			return false;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			return 0;
		}
	}
}