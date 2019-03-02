package com.chocohead.spipes.logic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.transport.pipe.flow.PipeFlowPower;

import com.chocohead.spipes.pretty.PrettyFlowTask;

public class PipeFlowDebug extends PipeFlow implements IDebuggable {
	private Future<List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>>> renderTask;
	protected int flow;

	public PipeFlowDebug(IPipe pipe) {
		super(pipe);

		updateFlow(false);
	}

	public PipeFlowDebug(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt);

		updateFlow(false);
	}


	@Override
	public void writePayload(int id, PacketBuffer buffer, Side side) {
		super.writePayload(id, buffer, side);

		if (side == Side.SERVER) {
			if (id == PipeFlowPower.NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
				buffer.writeDouble(flow / 100D);
			}
		}
	}

	@Override
	public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
		super.readPayload(id, buffer, side);

		if (side == Side.CLIENT) {
			if (id == PipeFlowPower.NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
				double[] flows = new double[EnumFacing.VALUES.length];

				Arrays.fill(flows, buffer.readDouble());
				buffer.release();

				renderTask = RenderPool.queue(new PrettyFlowTask(flows));
			}
		}
	}

	public List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> getRender() {
		try {//Will force the current task to be calculated if not already done
			return renderTask != null ? renderTask.get() : Collections.emptyList();
		} catch (CancellationException e) {
			System.out.println("Flow render cancelled!");
			return Collections.emptyList(); //Cancelled?
		} catch (ExecutionException e) {
			throw new RuntimeException("Error calculating renderer", e);
		} catch (InterruptedException e) {
			throw new RuntimeException("Interupted getting renderer", e);
		}
	}


	@Override
	public boolean canConnect(EnumFacing face, PipeFlow other) {
		return other instanceof PipeFlowDebug;
	}

	@Override
	public boolean canConnect(EnumFacing face, TileEntity tile) {
		return false;
	}


	public void updateFlow(boolean tellClient) {
		flow = ((IPowerLimit) pipe.getBehaviour()).getPipeCapacity();
		if (tellClient) sendPayload(PipeFlowPower.NET_POWER_AMOUNTS);
	}

	@Override
	public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
		left.add("flow = " + flow);
	}
}