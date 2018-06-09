package com.chocohead.spipes.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEntityFUTester extends TileEntityTester implements ITickable, IEnergyStorage {
	public TileEntityFUTester() {
	}

	@Override
	public boolean canExtract() {
		return setting != Setting.IN;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return canExtract() ? maxExtract : 0;
	}

	@Override
	public void update() {
		if (!world.isRemote && setting != Setting.IN) {
			for (EnumFacing side : EnumFacing.VALUES) {
				TileEntity tile = world.getTileEntity(pos.offset(side));
				if (tile == null) continue;

				IEnergyStorage energy = tile.getCapability(CapabilityEnergy.ENERGY, side);
				if (energy != null && energy.canReceive()) {
					energy.receiveEnergy(100, false);
				}

				/*if (tile instanceof TilePipeHolder) {
					IPipe pipe = ((TilePipeHolder) tile).getPipe();

					if (pipe != null && pipe.getFlow() instanceof PipeFlowFUMJ) {
						PipeFlowFUMJ flow = (PipeFlowFUMJ) pipe.getFlow();

						flow.sections.get(side.getOpposite()).entry = 100;
					}
				}*/
			}
		}
	}

	@Override
	public int getEnergyStored() {
		return 10;
	}

	@Override
	public int getMaxEnergyStored() {
		return 10000;
	}

	@Override
	public boolean canReceive() {
		return setting == Setting.IN;
	}

	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		return canReceive() ? maxReceive : 0;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityEnergy.ENERGY ? CapabilityEnergy.ENERGY.cast(this) : super.getCapability(capability, facing);
	}
}