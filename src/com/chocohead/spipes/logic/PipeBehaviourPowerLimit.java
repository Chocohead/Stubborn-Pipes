package com.chocohead.spipes.logic;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

public class PipeBehaviourPowerLimit extends PipeBehaviour implements IPowerLimit {
	protected final int limit;

	public PipeBehaviourPowerLimit(IPipe pipe, int limit) {
		super(pipe);

		this.limit = limit;
	}

	public PipeBehaviourPowerLimit(IPipe pipe, NBTTagCompound nbt, int limit) {
		super(pipe, nbt);

		this.limit = limit;
	}

	@Override
	public int getPipeCapacity() {
		return limit;
	}


	public static PipeBehaviour cobble(IPipe pipe) {
		return new PipeBehaviourPowerLimit(pipe, 80);
	}

	public static PipeBehaviour cobble(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerLimit(pipe, nbt, 80);
	}

	public static PipeBehaviour stone(IPipe pipe) {
		return new PipeBehaviourPowerLimit(pipe, 160);
	}

	public static PipeBehaviour stone(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerLimit(pipe, nbt, 160);
	}

	public static PipeBehaviour quartz(IPipe pipe) {
		return new PipeBehaviourPowerLimit(pipe, 640);
	}

	public static PipeBehaviour quartz(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerLimit(pipe, nbt, 640);
	}

	public static PipeBehaviour gold(IPipe pipe) {
		return new PipeBehaviourPowerLimit(pipe, 2560);
	}

	public static PipeBehaviour gold(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerLimit(pipe, nbt, 2560);
	}

	public static PipeBehaviour diamond(IPipe pipe) {
		return new PipeBehaviourPowerLimit(pipe, 10240);
	}

	public static PipeBehaviour diamond(IPipe pipe, NBTTagCompound nbt) {
		return new PipeBehaviourPowerLimit(pipe, nbt, 10240);
	}
}