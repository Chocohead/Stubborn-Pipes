package com.chocohead.spipes.logic;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import buildcraft.api.transport.pipe.IPipe.ConnectedType;

public class FlowDirector {
	private static final Map<BlockPos, PipeFriends> LIVE_TARGETS = new HashMap<>();
	private static final Map<BlockPos, PipeFriends> LIVE_CANNONS = new HashMap<>();
	private static final Map<BlockPos, PipeFriends> LIVE_PIPES = new HashMap<>();

	private static class PipeFriends {
		public final PipeFlowFU pipe;
		public final Set<EnumFacing> neighbourPipes; //Will never be null, or empty
		public final Set<EnumFacing> neighbourTiles; //Will be null if there are no neighbouring tiles

		public PipeFriends(PipeFlowFU pipe, Set<EnumFacing> neighbourPipes) {
			this(pipe, neighbourPipes, null);
		}

		public PipeFriends(PipeFlowFU pipe, Set<EnumFacing> neighbourPipes, Set<EnumFacing> neighbourTiles) {
			this.pipe = pipe;
			this.neighbourPipes = neighbourPipes;
			this.neighbourTiles = neighbourTiles;

			assert !neighbourPipes.isEmpty();
			assert neighbourTiles == null || !neighbourTiles.isEmpty();
		}
	}

	public static void register(PipeFlowFU pipe) {
		Set<EnumFacing> neighbourPipes = EnumSet.noneOf(EnumFacing.class);
		Set<EnumFacing> neighbourTiles = EnumSet.noneOf(EnumFacing.class);

		for (EnumFacing side : EnumFacing.VALUES) {
			ConnectedType connection = pipe.pipe.getConnectedType(side);

			if (connection != null) {
				switch (connection) {
				case PIPE:
					neighbourPipes.add(side);
					break;

				case TILE:
					neighbourTiles.add(side);
					break;
				}
			}
		}

		if (!neighbourPipes.isEmpty()) {//Lone pipes can be left alone
			BlockPos pos = pipe.pipe.getHolder().getPipePos();

			if (!neighbourTiles.isEmpty()) {
				(pipe.canPull() ? LIVE_CANNONS : LIVE_TARGETS).put(pos, new PipeFriends(pipe, neighbourPipes));
			} else if (neighbourPipes.size() > 1) {
				LIVE_PIPES.put(pos, new PipeFriends(pipe, neighbourPipes)); //Going between a and b, but isn't either of them
			}
		}
	}

	/**
	 * A {@link Deque} with backing {@link Set} for faster {@link #contains(Object)}
	 *
	 * @param <T> The type of object stored in the stack
	 */
	private static class Stack<T> {
		private final Deque<T> seen = new ArrayDeque<>();
		private final Set<T> backedSeen = new HashSet<>();

		public void push(T pos) {
			seen.push(pos);
			backedSeen.add(pos);
		}

		public boolean contains(T pos) {
			return backedSeen.contains(pos);
		}
	}

	public static void solve() {
		Set<PipeFriends> preSolved = Collections.newSetFromMap(new IdentityHashMap<>());

		for (Entry<BlockPos, PipeFriends> entry : LIVE_TARGETS.entrySet()) {
			PipeFriends start = entry.getValue();
			if (preSolved.contains(start)) continue;

			int throughput = start.pipe.getCapacity();
			if (throughput <= 0) continue; //If we've got no capacity no point finding a route for no power

			World world = start.pipe.pipe.getHolder().getPipeWorld();
			assert !world.isRemote;
			BlockPos startPos = entry.getKey();

			Map<EnumFacing, IEnergyStorage> consumers = new EnumMap<>(EnumFacing.class);
			int target = 0; //Total power to find along all connected pipes

			for (EnumFacing side : start.neighbourTiles) {
				TileEntity te = world.getTileEntity(startPos.offset(side));
				assert te != null; //Come on BuildCraft, don't do us dirty here

				assert te.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite());
				IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());

				if (!energy.canReceive()) continue; //Fine, be like that
				int accepted = energy.receiveEnergy(throughput, true);

				if (accepted > 0) {
					target += accepted;
					consumers.put(side, energy);
				}
			}
			if (target <= 0) continue; //No power necessary

			Stack<BlockPos> seen = new Stack<>();
			seen.push(startPos); //Why not

			for (EnumFacing side : start.neighbourPipes) {
				BlockPos pos = startPos.offset(side);
				seen.push(pos); //No need to check for loops on first go

				PipeFriends friend = LIVE_CANNONS.get(pos);
				if (friend != null) {//Direct sink -> source find
					if (friend.pipe.getCapacity() < throughput) {
						throughput = friend.pipe.getCapacity();
					}

					int[] moved = movePower(world, pos, friend.neighbourTiles, throughput, true);
					if (moved != null) {
						int produced = moved[0];
						assert produced > 0;


					}

					continue;
				}

				friend = LIVE_TARGETS.get(pos);
				if (friend != null) {//Another sink
					if (friend.pipe.getCapacity() < throughput) {
						throughput = friend.pipe.getCapacity();
					}

					preSolved.add(friend);

					continue;
				}

				friend = LIVE_PIPES.get(pos);
				if (friend != null) {//Neighbouring pipe to search from

				} else {//Neighbour pipe is a dead-end

				}
			}
		}
	}

	private static int[] movePower(IBlockAccess world, BlockPos pos, Set<EnumFacing> sides, int amount, boolean simulate) {
		int total = 0;
		int[] moved = new int[EnumFacing.VALUES.length + 1];

		for (EnumFacing side : sides) {
			TileEntity te = world.getTileEntity(pos.offset(side));
			assert te != null; //Come on BuildCraft, don't do us dirty here

			assert te.hasCapability(CapabilityEnergy.ENERGY, side.getOpposite());
			IEnergyStorage energy = te.getCapability(CapabilityEnergy.ENERGY, side.getOpposite());

			if (!energy.canExtract()) continue; //Fine, be like that
			int extracted = energy.extractEnergy(amount, simulate);

			if (extracted > 0) {
				total += extracted;
				moved[side.getIndex() + 1] = extracted;
			}
		}

		if (total > 0) {
			moved[0] = total;

			return moved;
		} else {
			return null;
		}
	}
}