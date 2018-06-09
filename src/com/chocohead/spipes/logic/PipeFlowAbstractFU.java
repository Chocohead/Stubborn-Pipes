package com.chocohead.spipes.logic;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.energy.CapabilityEnergy;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;

import com.chocohead.spipes.logic.PipeFlowAbstractFU.SidePower;

public abstract class PipeFlowAbstractFU<T extends SidePower> extends PipeFlow {
	public final Map<EnumFacing, T> sections;
	protected int pipeCapacity = -1;

	public PipeFlowAbstractFU(IPipe pipe, Function<EnumFacing, T> sectionConstructor) {
		super(pipe);

		sections = new EnumMap<>(Arrays.stream(EnumFacing.VALUES).collect(Collectors.toMap(Function.identity(), sectionConstructor)));
	}

	public PipeFlowAbstractFU(IPipe pipe, NBTTagCompound nbt, Function<EnumFacing, T> sectionConstructor) {
		super(pipe, nbt);

		sections = new EnumMap<>(Arrays.stream(EnumFacing.VALUES).collect(Collectors.toMap(Function.identity(), sectionConstructor)));
	}


	@Override
	public boolean canConnect(EnumFacing face, PipeFlow other) {
		return other instanceof PipeFlowAbstractFU;
	}

	@Override
	public boolean canConnect(EnumFacing face, TileEntity tile) {
		return tile.hasCapability(CapabilityEnergy.ENERGY, face.getOpposite());
	}


	@Override
	public final void onTick() {
		if (pipe.getHolder().getPipeWorld().isRemote) {
			updateClient();
		} else {
			updateServer();
		}
	}

	protected void updateClient() {
	}

	protected void updateServer() {
	}

	public void resetCapacity() {
		pipeCapacity = -1;
	}


	public static class SidePower {
		public final EnumFacing side;
		public int entry, exit;
		int stored = 0;

		public SidePower(EnumFacing side) {
			this.side = side;
		}
	}
}