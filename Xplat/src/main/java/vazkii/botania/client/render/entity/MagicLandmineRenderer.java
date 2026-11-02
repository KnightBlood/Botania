/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;

import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.MagicLandmineEntity;

public class MagicLandmineRenderer extends EntityRenderer<MagicLandmineEntity, MagicLandmineRenderer.MagicLandmineRenderState> {

	public MagicLandmineRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public MagicLandmineRenderState createRenderState() {
		return new MagicLandmineRenderState();
	}

	@Override
	public void extractRenderState(MagicLandmineEntity e, MagicLandmineRenderState state, float partialTicks) {
		super.extractRenderState(e, state, partialTicks);
		state.tickCount = e.tickCount;
		state.partialTick = partialTicks;
		state.relativeBounds = e.getBoundingBox().move(e.position().scale(-1));
	}

	@Override
	public void submit(MagicLandmineRenderState state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState camera) {
		super.submit(state, ms, collector, camera);

		ms.pushPose();
		AABB aabb = state.relativeBounds;

		float gs = (float) (Math.sin(ClientTickHandler.total() / 20) + 1) * 0.2F + 0.6F;
		int r = (int) (105 * gs);
		int g = (int) (25 * gs);
		int b = (int) (145 * gs);

		int alpha = 32;
		if (state.tickCount < 8) {
			alpha = (int) (alpha * Math.min((state.tickCount + state.partialTick) / 8F, 1F));
		} else if (state.tickCount > 47) {
			alpha = (int) (alpha * Math.min(1F - (state.tickCount - 47 + state.partialTick) / 8F, 1F));
		}

		ms.translate(aabb.minX, aabb.minY + RenderHelper.getOffY(), aabb.minZ);

		float f = 1F / 16F;
		float x = (float) (aabb.getXsize() - f);
		float z = (float) (aabb.getZsize() - f);

		final float ff = f;
		final float fx = x;
		final float fz = z;
		final int falpha = alpha;
		collector.submitCustomGeometry(ms, RenderHelper.RECTANGLE, (pose, buffer) -> {
			Matrix4f mat = pose.pose();
			RenderHelper.flatRectangle(buffer, mat, ff, fx, 0, ff, fz, r, g, b, falpha);
		});
		RenderHelper.incrementOffY();
		ms.popPose();
	}

	public static class MagicLandmineRenderState extends EntityRenderState {
		public int tickCount;
		public float partialTick;
		public AABB relativeBounds;
	}
}
