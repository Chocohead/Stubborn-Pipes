package com.chocohead.spipes.pretty;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeFlow;

public abstract class PrettyPipeFlow extends PipeFlow {
	public PrettyPipeFlow(IPipe pipe) {
		super(pipe);
	}

	public PrettyPipeFlow(IPipe pipe, NBTTagCompound nbt) {
		super(pipe, nbt);
	}

	public abstract List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> getRender();
}