package com.chocohead.spipes.logic;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.misc.EntityUtil;

public class PipeBehaviourDebug extends PipeBehaviour implements IPowerLimit {
	protected int capacity = 20; //Default

	public PipeBehaviourDebug(IPipe pipe) {
		super(pipe);
	}

	public PipeBehaviourDebug(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt);

		capacity = nbt.getInteger("capacity");
	}

	@Override
	public NBTTagCompound writeToNbt() {
		NBTTagCompound nbt = super.writeToNbt();

		nbt.setInteger("capacity", capacity);

		return nbt;
	}

	@Override
	public int getPipeCapacity() {
		return capacity;
	}

	@Override
	public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
		if (EntityUtil.getWrenchHand(player) != null) {
			EntityUtil.activateWrench(player, trace);

			if (capacity == 100) {
				capacity = 10;
			} else {
				capacity += 10;
			}
			((PipeFlowDebug) pipe.getFlow()).updateFlow(!player.world.isRemote);

			return true;
		}
		return false;
	}
}