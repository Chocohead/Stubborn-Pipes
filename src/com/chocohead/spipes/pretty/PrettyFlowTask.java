package com.chocohead.spipes.pretty;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.vecmath.Point3f;

import com.google.common.base.Predicates;
import com.google.common.primitives.Doubles;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.transport.pipe.IPipe;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.transport.BCTransportSprites;

public class PrettyFlowTask implements Callable<List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>>> {
	private static class FaceSideCenterRadius {
		public final Predicate<IPipe> condition;
		public final EnumFacing face, side;
		public final Point3f centre, radius;

		public FaceSideCenterRadius(EnumFacing face, float centre, float radius) {
			this(Predicates.alwaysTrue(), face, null, new Point3f(centre, centre, centre), new Point3f(radius, radius, radius));
		}

		public FaceSideCenterRadius(Predicate<IPipe> condition, EnumFacing face, EnumFacing side, Point3f centre, Point3f radius) {
			this.condition = condition;
			this.face = face;
			this.side = side;
			this.centre = centre;
			this.radius = radius;
		}
	}
	private final double[] flow;

	public PrettyFlowTask(double[] flow) {
		this.flow = flow;
	}

	@Override
	public List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> call() throws Exception {
		List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> out = new ArrayList<>();

		List<FaceSideCenterRadius> facesSidesCentersRadiuses = new ArrayList<>();
		for (EnumPipePart part : EnumPipePart.VALUES) {
			if (part == EnumPipePart.CENTER) {
				double f = Doubles.max(flow);
				if (f <= 0) break; //If the max flow is 0 there is nothing to render
				float r = (float) (f / 4.1D);

				for (EnumFacing face : EnumFacing.VALUES) {
					facesSidesCentersRadiuses.add(new FaceSideCenterRadius(face, 0.5F, r));
				}
			} else {
				EnumFacing side = part.face;

				double f = flow[side.getIndex()];
				if (f <= 0) continue; //Skip when no power is flowing this way
				float r = (float) (f / 4.1D);

				for (EnumFacing face : EnumFacing.VALUES) {
					facesSidesCentersRadiuses.add(new FaceSideCenterRadius(
							pipe -> pipe.isConnected(side),
							face, side,
							new Point3f(
									0.5F + side.getFrontOffsetX() * (0.25F + r / 2),
									0.5F + side.getFrontOffsetY() * (0.25F + r / 2),
									0.5F + side.getFrontOffsetZ() * (0.25F + r / 2)
									),
							new Point3f(
									side.getAxis() == EnumFacing.Axis.X ? 0.25F - r / 2 : r,
									side.getAxis() == EnumFacing.Axis.Y ? 0.25F - r / 2 : r,
									side.getAxis() == EnumFacing.Axis.Z ? 0.25F - r / 2 : r
									)
							));
				}
			}
		}

		for (FaceSideCenterRadius faceSideCenterRadius : facesSidesCentersRadiuses) {
			EnumFacing face = faceSideCenterRadius.face;
			EnumFacing side = faceSideCenterRadius.side;
			Point3f center = faceSideCenterRadius.centre;
			Point3f radius = faceSideCenterRadius.radius;

			ModelUtil.UvFaceData uvs = null;
			switch (face.getAxis()) {
			case X:
				uvs = new ModelUtil.UvFaceData(
						center.getZ() - radius.getZ(),
						center.getY() - radius.getY(),
						center.getZ() + radius.getZ(),
						center.getY() + radius.getY()
						);
				break;
			case Y:
				uvs = new ModelUtil.UvFaceData(
						center.getX() - radius.getX(),
						center.getZ() - radius.getZ(),
						center.getX() + radius.getX(),
						center.getZ() + radius.getZ()
						);
				break;
			case Z:
				uvs = new ModelUtil.UvFaceData(
						center.getX() - radius.getX(),
						center.getY() - radius.getY(),
						center.getX() + radius.getX(),
						center.getY() + radius.getY()
						);
				break;
			}

			if (side != null) {
				if (face.getAxis() == EnumFacing.Axis.X && side.getAxis() == EnumFacing.Axis.Y ||
						face.getAxis() == EnumFacing.Axis.Y && side.getAxis() == EnumFacing.Axis.Z ||
						face.getAxis() == EnumFacing.Axis.Z && side.getAxis() == EnumFacing.Axis.Y) {
					//Invert the face data
					uvs = new ModelUtil.UvFaceData(
							1 - uvs.maxU,
							1 - uvs.maxV,
							1 - uvs.minU,
							1 - uvs.minV
					);
				}
			}

			uvs = new ModelUtil.UvFaceData(
					BCTransportSprites.POWER_FLOW.getInterpU(uvs.minU),
					BCTransportSprites.POWER_FLOW.getInterpV(uvs.minV),
					BCTransportSprites.POWER_FLOW.getInterpU(uvs.maxU),
					BCTransportSprites.POWER_FLOW.getInterpV(uvs.maxV)
					);

			out.add(Pair.of(faceSideCenterRadius.condition, ModelUtil.createFace(face, center, radius, uvs).lighti(15, 15)::render));
		}

		return out;
	}
}