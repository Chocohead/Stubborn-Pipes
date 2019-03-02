package com.chocohead.spipes.pretty;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.BufferBuilder;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipeFlowRenderer;

public enum PipeFlowPrettyFU implements IPipeFlowRenderer<PrettyPipeFlow> {
	INSTANCE;

	@Override
	public void render(PrettyPipeFlow pipe, double x, double y, double z, float partialTicks, BufferBuilder buffer) {
		buffer.setTranslation(x, y, z);

		for (Pair<Predicate<IPipe>, Consumer<BufferBuilder>> con : pipe.getRender()) {
			if (con.getLeft().test(pipe.pipe)) {
				con.getRight().accept(buffer);
			}
		}

		buffer.setTranslation(0, 0, 0);
	}
}