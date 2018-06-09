package com.chocohead.spipes;

import net.minecraft.util.ResourceLocation;

import buildcraft.api.BCModules;
import buildcraft.api.transport.pipe.PipeDefinition.PipeDefinitionBuilder;
import buildcraft.api.transport.pipe.PipeFlowType;

import com.chocohead.spipes.logic.PipeFlowFUMJ;

class DefinitionBuilder extends PipeDefinitionBuilder {
	//private static final PipeFlowType type = new PipeFlowType(PipeFlowFU::new, PipeFlowFU::new);
	private static final PipeFlowType type = new PipeFlowType(PipeFlowFUMJ::new, PipeFlowFUMJ::new);

	public DefinitionBuilder() {
		flow(type);
	}

	@Override
	public DefinitionBuilder id(String post) {
		identifier = new ResourceLocation(StubbornPipes.MODID, post);

		return this;
	}

	@Override
	public DefinitionBuilder texPrefix(String prefix) {
		texturePrefix = BCModules.TRANSPORT.getModId() + ":pipes/" + prefix;

		return this;
	}
}