package com.chocohead.spipes.pretty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.vecmath.Point3f;

import com.google.common.base.Predicates;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.IPipe;

import buildcraft.lib.client.model.ModelUtil;
import buildcraft.transport.BCTransportSprites;

public class PrettyFlowTask implements Callable<List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>>> {
	private final double[] flow;

	public PrettyFlowTask(double[] flow) {
		this.flow = flow;
	}

	@Override
	public List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> call() throws Exception {
		List<Pair<Predicate<IPipe>, Consumer<BufferBuilder>>> out = new ArrayList<>();

		List<Predicate<IPipe>> guardConditions = new ArrayList<>();
		List<Triple<Pair<EnumFacing, EnumFacing>, Point3f, Point3f>> facesSidesCentersRadiuses = new ArrayList<>();
		for (EnumFacing face : EnumFacing.VALUES) {
			double f = flow[face.getIndex()];
			if (f <= 0) continue; //Skip when no power is flowing
			float r = (float) (f / 4.1D);

			guardConditions.add(Predicates.alwaysTrue());
			facesSidesCentersRadiuses.add(
					Triple.of(
							Pair.of(face, null),
							new Point3f(0.5F, 0.5F, 0.5F),
							new Point3f(r, r, r)
							)
					);
			for (EnumFacing side : EnumFacing.VALUES) {
				guardConditions.add(pipe -> pipe.isConnected(side));
				facesSidesCentersRadiuses.add(
						Triple.of(
								Pair.of(face, side),
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
								)
						);
			}
		}

		Iterator<Predicate<IPipe>> it = guardConditions.listIterator();
		for (Triple<Pair<EnumFacing, EnumFacing>, Point3f, Point3f> faceSideCenterRadius : facesSidesCentersRadiuses) {
			EnumFacing face = faceSideCenterRadius.getLeft().getLeft();
			EnumFacing side = faceSideCenterRadius.getLeft().getRight();
			Point3f center = faceSideCenterRadius.getMiddle();
			Point3f radius = faceSideCenterRadius.getRight();
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
			boolean invert = false;
			if (side != null) {
				if (face.getAxis() == EnumFacing.Axis.X && side.getAxis() == EnumFacing.Axis.Y ||
						face.getAxis() == EnumFacing.Axis.Y && side.getAxis() == EnumFacing.Axis.Z ||
						face.getAxis() == EnumFacing.Axis.Z && side.getAxis() == EnumFacing.Axis.Y) {
					invert = true;
				}
			}
			if (invert) {
				uvs = new ModelUtil.UvFaceData(
						1 - uvs.maxU,
						1 - uvs.maxV,
						1 - uvs.minU,
						1 - uvs.minV
						);
			}
			uvs = new ModelUtil.UvFaceData(
					BCTransportSprites.POWER_FLOW.getInterpU(uvs.minU),
					BCTransportSprites.POWER_FLOW.getInterpV(uvs.minV),
					BCTransportSprites.POWER_FLOW.getInterpU(uvs.maxU),
					BCTransportSprites.POWER_FLOW.getInterpV(uvs.maxV)
					);
			assert it.hasNext();
			out.add(Pair.of(it.next(), ModelUtil.createFace(face, center, radius, uvs).lighti(15, 15)::render));
		}
		assert !it.hasNext();

		return out;
	}
}