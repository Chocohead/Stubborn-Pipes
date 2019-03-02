package com.chocohead.spipes.logic;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;

import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.data.AverageInt;
import buildcraft.transport.pipe.flow.PipeFlowPower;

import com.chocohead.spipes.logic.PipeFlowFUMJ.Section;
import com.chocohead.spipes.pretty.PrettyFlowTask;

public class PipeFlowFUMJ extends PipeFlowAbstractFU<Section> implements IDebuggable {
	private Future<List<Pair<Predicate<IPipe>,Consumer<BufferBuilder>>>> renderTask;
	private long currentWorldTime;
	private int[] liveTransfers;

	public PipeFlowFUMJ(IPipe pipe) {
		super(pipe, Section::new);

		sections.values().forEach(section -> section.giveCapacity(() -> pipeCapacity));
		updateCapacity();
	}

	public PipeFlowFUMJ(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt, Section::new);

		sections.values().forEach(section -> section.giveCapacity(() -> pipeCapacity));
		updateCapacity();
	}


	@Override
	public void writePayload(int id, PacketBuffer buffer, Side side) {
		super.writePayload(id, buffer, side);

		if (side == Side.SERVER) {
			if (id == PipeFlowPower.NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
				for (EnumFacing face : EnumFacing.VALUES) {
					Section section = sections.get(face);

					//buffer.writeDouble((section.entry + section.exit) / pipeCapacity);
					buffer.writeDouble(section.powerAverage.getAverage() / pipeCapacity);
				}
			}
		}
	}

	@Override
	public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
		super.readPayload(id, buffer, side);

		if (side == Side.CLIENT) {
			if (id == PipeFlowPower.NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
				double[] flows = new double[EnumFacing.VALUES.length];

				for (EnumFacing face : EnumFacing.VALUES) {
					flows[face.getIndex()] = buffer.readDouble();
				}

				//Debug to set to max, akin to MJ rendering
				Arrays.fill(flows, Doubles.max(flows));

				renderTask = RenderPool.queue(new PrettyFlowTask(flows));
			}
		}
	}

	@Override
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
	public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
		left.add("maxPower = " + pipeCapacity);
		left.add("stored = " + arrayToString(s -> s.stored) + " <- " + arrayToString(s -> s.entry));
		left.add("- demand: " + arrayToString(s -> s.demand) + " <- " + arrayToString(s -> s.nextDemand));
		left.add("- power: IN " + arrayToString(s -> s.entry) + ", OUT " + arrayToString(s -> s.exit));
		left.add("- average: " + averageToString(s -> s.powerAverage.getAverage()));
	}

	private String arrayToString(ToIntFunction<Section> getter) {
		return Arrays.toString(Arrays.stream(EnumFacing.VALUES).map(sections::get).mapToInt(getter).toArray());
	}

	private String averageToString(ToDoubleFunction<Section> getter) {
		return Arrays.toString(Arrays.stream(EnumFacing.VALUES).map(sections::get).mapToDouble(getter).toArray());
	}

	@Override
	public boolean onFlowActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
		if (EntityUtil.getWrenchHand(player) != null) {
			EntityUtil.activateWrench(player, trace);

			if (!player.world.isRemote) {
				sendPayload(PipeFlowPower.NET_POWER_AMOUNTS);
				player.sendMessage(new TextComponentTranslation("item.pipe.stubborn_pipes.update"));
			}

			return true;
		}
		return false;
	}


	private void updateCapacity() {
		pipeCapacity = ((IPowerLimit) pipe.getBehaviour()).getPipeCapacity();
	}

	@Override
	protected void updateServer() {
		if (pipeCapacity < 0) updateCapacity();

		flow();

		for (EnumFacing face : EnumFacing.VALUES) {
			Section section = sections.get(face);

			if (section.stored > 0) {
				long totalDemand = 0;
				for (EnumFacing side : EnumFacing.VALUES) {
					if (face != side) {
						totalDemand += sections.get(side).demand;
					}
				}

				if (totalDemand > 0) {
					for (EnumFacing side : EnumFacing.VALUES) {
						if (face == side) continue;

						Section exitSection = sections.get(side);
						if (exitSection.demand > 0) {
							//Total power split in the ratio of side demand to total demanded
							int watts = (int) Math.min(section.stored * exitSection.demand / totalDemand, exitSection.demand);
							int used = 0;

							totalDemand -= exitSection.demand;
							IPipe neighbour = pipe.getConnectedPipe(side);
							if (neighbour != null && neighbour.getFlow() instanceof PipeFlowFUMJ && neighbour.isConnected(side.getOpposite())) {
								PipeFlowFUMJ flow = (PipeFlowFUMJ) neighbour.getFlow();
								//flow.sections.get(side.getOpposite()).entry += watts;
								used = flow.sections.get(side.getOpposite()).receivePowerInternal(watts);
								exitSection.exit += used;
							} else {
								IEnergyStorage receiver = pipe.getHolder().getCapabilityFromPipe(side, CapabilityEnergy.ENERGY);
								if (receiver != null && receiver.canReceive()) {
									used = receiver.receiveEnergy(watts, false);
								}
							}

							section.stored -= used;
							exitSection.demand -= used;
							//exitSection.debugPowerOutput += used;

							section.powerAverage.push(used);
							exitSection.powerAverage.push(used);
						}
					}
				}
			}
		}

		// Tick the average power calculations
		for (Section section : sections.values()) {
			section.powerAverage.tick();
		}

		// Compute the tiles requesting power that are not power pipes
		for (EnumFacing face : EnumFacing.VALUES) {
			if (pipe.getConnectedType(face) == ConnectedType.TILE) {
				IEnergyStorage receiver = pipe.getHolder().getCapabilityFromPipe(face, CapabilityEnergy.ENERGY);
				if (receiver != null && receiver.canReceive()) {
					int possible = receiver.receiveEnergy(pipeCapacity, true);
					if (possible > 0) {
						//sections.get(face).demand += possible; //Add to demand
						requestPower(face, possible);
					}
				}
			}
		}

		// Sum the amount of power requested on each side
		int[] nextTransfers = new int[6];
		for (EnumFacing face : EnumFacing.VALUES) {
			if (!pipe.isConnected(face)) continue;

			long totalDemand = 0;
			for (EnumFacing side : EnumFacing.VALUES) {
				if (face != side) {
					totalDemand += sections.get(side).demand;
				}
			}

			assert totalDemand < Integer.MAX_VALUE; //Let's not overflow
			nextTransfers[face.getIndex()] = (int) totalDemand;
		}

		// Transfer requested power to neighbouring pipes
		for (EnumFacing face : EnumFacing.VALUES) {
			int demand = nextTransfers[face.getIndex()];
			if (demand <= 0 || !pipe.isConnected(face)) {
				continue; //No demand this direction, or nothing to demand from
			}

			IPipe pipe = this.pipe.getHolder().getNeighbourPipe(face);
			if (pipe == null || !(pipe.getFlow() instanceof PipeFlowFUMJ)) {
				continue; //Mystery pipe we can't request from (or a non-pipe tile)
			}

			PipeFlowFUMJ flow = (PipeFlowFUMJ) pipe.getFlow();
			//flow.sections.get(face.getOpposite()).demand += demand;
			flow.requestPower(face.getOpposite(), demand);
		}


		// Networking
		if (!Arrays.equals(liveTransfers, nextTransfers)) {
			sendPayload(PipeFlowPower.NET_POWER_AMOUNTS);

			liveTransfers = nextTransfers;
		}
	}

	void flow() {
		long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();

		if (currentWorldTime != now) {
			currentWorldTime = now;

			sections.values().forEach(Section::flow);
		}
	}

	private void requestPower(EnumFacing from, int amount) {
		flow();

		Section section = sections.get(from); //Limit demand to 10x capacity
		//Allow over demanding to encourage source starved networks to stabilise
		section.nextDemand = Math.min(section.nextDemand + Math.min(amount, pipeCapacity * 3), pipeCapacity * 10);

		assert section.nextDemand <= pipeCapacity * 10;
	}


	public static class Section extends PipeFlowAbstractFU.SidePower {
		public final AverageInt powerAverage = new AverageInt(10);
		private IntSupplier capacityGetter;
		int demand, nextDemand;

		public Section(EnumFacing side) {
			super(side);
		}

		void giveCapacity(IntSupplier getter) {
			capacityGetter = getter;
		}

		void flow() {
			demand = nextDemand;
			nextDemand = 0;

			int backFlow = stored;
			stored = entry;
			entry = backFlow;

			exit = 0;
		}

		public int receivePowerInternal(int sent) {
			if (sent > 0) {
				int capacity = capacityGetter.getAsInt();
				int start = entry;

				entry = Math.min(entry + Math.min(sent, capacity), capacity * 10);
				assert entry >= start: "Storage fell back?! From " + start + " to " + entry + " after given " + sent + " limited by " + capacity;
				assert entry <= capacity * 10: "Storage overflowed by " + (entry - capacity * 10);
				//debugPowerOffered += moved;

				return entry - start;
			}
			return 0;
		}
	}
}