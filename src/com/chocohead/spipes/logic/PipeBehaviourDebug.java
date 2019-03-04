package com.chocohead.spipes.logic;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.misc.EntityUtil;

public class PipeBehaviourDebug extends PipeBehaviour {
	protected final int[] capacities = new int[EnumPipePart.VALUES.length - 1];

	public PipeBehaviourDebug(IPipe pipe) {
		super(pipe);

		Arrays.fill(capacities, 20); //Default
	}

	public PipeBehaviourDebug(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt);

		int[] saved = nbt.getIntArray("capacities");
		if (saved.length == 0) {
			int old = nbt.getInteger("capacity");
			Arrays.fill(capacities, old == 0 ? 20 : old); //Default
		} else {
			System.arraycopy(saved, 0, capacities, 0, saved.length);
		}
	}

	@Override
	public NBTTagCompound writeToNbt() {
		NBTTagCompound nbt = super.writeToNbt();

		nbt.setIntArray("capacities", capacities);

		return nbt;
	}

	public int[] getPipeCapacities() {
		return capacities;
	}

	@Override
	public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
		if (EntityUtil.getWrenchHand(player) != null && part != EnumPipePart.CENTER) {
			EntityUtil.activateWrench(player, trace);

			int segment = part.ordinal();
			if (capacities[segment] == 100) {
				capacities[segment] = 10;
			} else {
				capacities[segment] += 10;
			}
			((PipeFlowDebug) pipe.getFlow()).updateFlow(!player.world.isRemote);

			return true;
		}
		return false;
	}
}