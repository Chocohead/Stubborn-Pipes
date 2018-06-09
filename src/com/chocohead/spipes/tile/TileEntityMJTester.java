package com.chocohead.spipes.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import net.minecraftforge.common.capabilities.Capability;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjCapabilityHelper;

public class TileEntityMJTester extends TileEntityTester implements ITickable, IMjConnector, IMjPassiveProvider, IMjReceiver {
	protected final MjCapabilityHelper helper = new MjCapabilityHelper(this);

	public TileEntityMJTester() {
	}


	@Override
	public boolean canConnect(IMjConnector other) {
		return !(other instanceof TileEntityMJTester);
	}

	@Override
	public long extractPower(long min, long max, boolean simulate) {
		assert !world.isRemote;
		switch (setting) {
		case MIN_OUT:
			return min;

		case MAX_OUT:
			return max;

		case IN:
		default:
			return 0;
		}
	}

	@Override
	public void update() {
		if (!world.isRemote && setting != Setting.IN) {
			for (EnumFacing side : EnumFacing.VALUES) {
				TileEntity tile = world.getTileEntity(pos.offset(side));
				if (tile == null) continue;

				IMjReceiver rec = tile.getCapability(MjAPI.CAP_RECEIVER, side.getOpposite());
				if (rec != null && rec.canConnect(this) && canConnect(rec) && rec.canReceive()) {
					/*long rejected = */rec.receivePower(MjAPI.MJ, false);
					//System.out.println("Inject: " + rejected);
				}
			}
		}
	}

	@Override
	public long getPowerRequested() {
		assert !world.isRemote;
		return setting == Setting.IN ? MjAPI.MJ : 0;
	}

	@Override
	public long receivePower(long microJoules, boolean simulate) {
		assert !world.isRemote;
		return setting == Setting.IN ? 0 : microJoules; //Nom
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return helper.hasCapability(capability, facing) || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		T cap = helper.getCapability(capability, facing);
		return cap != null ? cap : super.getCapability(capability, facing);
	}
}