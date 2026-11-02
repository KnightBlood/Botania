/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapts the pre-1.21.9 immediate-mode rendering API to the extract/submit pipeline.
 * <p>
 * Renderer bodies keep writing into a {@link MultiBufferSource}; the written vertices are captured
 * with the transform that was active at the time, so the whole geometry ends up in the coordinate
 * space of the {@link PoseStack} that was handed to the renderer. Call {@link #flush} to replay
 * everything into the {@link SubmitNodeCollector}.
 * <p>
 * Entities are forwarded to the {@code EntityRenderDispatcher} instead of being rasterized, because
 * entity rendering is itself deferred in this version.
 */
public class BotaniaRenderContext implements MultiBufferSource {
	private final @Nullable CameraRenderState camera;
	private final List<Action> actions = new ArrayList<>();

	public BotaniaRenderContext(@Nullable CameraRenderState camera) {
		this.camera = camera;
	}

	@Override
	public VertexConsumer getBuffer(RenderType renderType) {
		Segment segment = new Segment(renderType);
		this.actions.add(segment);
		return new Recorder(segment);
	}

	/**
	 * @return the rotation of the camera, for renderers that billboard towards the viewer
	 */
	public Quaternionf cameraOrientation() {
		return this.camera != null ? this.camera.orientation : new Quaternionf();
	}

	/**
	 * Submits an entity using the same transform chain the old
	 * {@code EntityRenderDispatcher#render} call applied.
	 */
	public <E extends Entity> void renderEntity(E entity, float partialTicks, PoseStack poseStack,
			double x, double y, double z) {
		Minecraft mc = Minecraft.getInstance();
		EntityRenderState entityState = mc.getEntityRenderDispatcher().extractEntity(entity, partialTicks);
		this.actions.add(new EntityAction(entityState, poseStack.last().copy(), x, y, z));
	}

	/**
	 * Replays everything that was recorded, in order.
	 */
	public void flush(SubmitNodeCollector collector) {
		Minecraft mc = Minecraft.getInstance();
		for (Action action : this.actions) {
			if (action instanceof Segment segment) {
				if (segment.vertices.isEmpty()) {
					continue;
				}
				List<Vertex> vertices = segment.vertices;
				collector.submitCustomGeometry(new PoseStack(), segment.renderType, (pose, buffer) -> {
					for (Vertex vertex : vertices) {
						buffer.addVertex(vertex.x, vertex.y, vertex.z)
								.setColor(vertex.color)
								.setUv(vertex.u, vertex.v)
								.setOverlay(vertex.overlay)
								.setLight(vertex.light)
								.setNormal(vertex.normalX, vertex.normalY, vertex.normalZ);
					}
				});
			} else if (action instanceof EntityAction entityAction) {
				PoseStack poseStack = new PoseStack();
				poseStack.last().set(entityAction.pose);
				mc.getEntityRenderDispatcher().submit(entityAction.state, this.camera, entityAction.x, entityAction.y,
						entityAction.z, poseStack, collector);
			}
		}
		this.actions.clear();
	}

	private sealed interface Action permits Segment, EntityAction {}

	private record EntityAction(EntityRenderState state, PoseStack.Pose pose, double x, double y, double z)
			implements Action {}

	private static final class Segment implements Action {
		final RenderType renderType;
		final List<Vertex> vertices = new ArrayList<>();

		Segment(RenderType renderType) {
			this.renderType = renderType;
		}
	}

	private static final class Vertex {
		float x;
		float y;
		float z;
		int color = -1;
		float u;
		float v;
		int overlay = OverlayTexture.NO_OVERLAY;
		int light;
		float normalX;
		float normalY;
		float normalZ;
	}

	private static final class Recorder implements VertexConsumer {
		private final Segment segment;
		private @Nullable Vertex last;

		Recorder(Segment segment) {
			this.segment = segment;
		}

		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			Vertex vertex = new Vertex();
			vertex.x = x;
			vertex.y = y;
			vertex.z = z;
			this.segment.vertices.add(vertex);
			this.last = vertex;
			return this;
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			if (this.last != null) {
				this.last.color = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
			}
			return this;
		}

		@Override
		public VertexConsumer setColor(int color) {
			if (this.last != null) {
				this.last.color = color;
			}
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			if (this.last != null) {
				this.last.u = u;
				this.last.v = v;
			}
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			if (this.last != null) {
				this.last.overlay = (v & 0xFFFF) << 16 | u & 0xFFFF;
			}
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			if (this.last != null) {
				this.last.light = (v & 0xFFFF) << 16 | u & 0xFFFF;
			}
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			if (this.last != null) {
				this.last.normalX = x;
				this.last.normalY = y;
				this.last.normalZ = z;
			}
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			return this;
		}
	}
}
