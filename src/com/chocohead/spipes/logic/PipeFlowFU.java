package com.chocohead.spipes.logic;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.transport.pipe.IPipe;

import com.chocohead.spipes.logic.PipeFlowAbstractFU.SidePower;

public class PipeFlowFU extends PipeFlowAbstractFU<SidePower> {
	protected int capacity = -1;

	public PipeFlowFU(IPipe pipe) {
		super(pipe, SidePower::new);
	}

	public PipeFlowFU(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt, SidePower::new);
	}

	@Override
	public void writePayload(int id, PacketBuffer buffer, Side side) {
		super.writePayload(id, buffer, side);

		if (side.isServer()) {

		}
	}

	@Override
	public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
		super.readPayload(id, buffer, side);

		if (side.isClient()) {

		}
	}


	@Override
	protected void updateServer() {
		FlowDirector.register(this);
	}

	/** Calculates the possible through put of the current pipe */
	protected void realiseCapacity() {
		capacity = 320;
	}

	public int getCapacity() {
		if (capacity < 0) realiseCapacity();

		return capacity;
	}

	public boolean canPull() {
		return pipe.getBehaviour() instanceof PipeBehaviourPowerInput;
	}
}