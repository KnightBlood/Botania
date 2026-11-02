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

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.ManaStormEntity;

public class ManaStormRenderer extends EntityRenderer<ManaStormEntity, ManaStormRenderer.ManaStormRenderState> {

	public ManaStormRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public ManaStormRenderState createRenderState() {
		return new ManaStormRenderState();
	}

	@Override
	public void extractRenderState(ManaStormEntity storm, ManaStormRenderState state, float partialTicks) {
		super.extractRenderState(storm, state, partialTicks);
		float maxScale = 1.95F;
		state.scale = 0.05F + ((float) storm.burstsFired / ManaStormEntity.TOTAL_BURSTS
				- (storm.deathTime == 0 ? 0 : storm.deathTime + partialTicks) / ManaStormEntity.DEATH_TIME) * maxScale;
		state.seed = storm.getUUID().getMostSignificantBits();
	}

	@Override
	public void submit(ManaStormRenderState state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState camera) {
		super.submit(state, ms, collector, camera);
		ms.pushPose();
		RenderHelper.renderStar(ms, collector, 0x00FF00, state.scale, state.scale, state.scale, state.seed);
		ms.popPose();
	}

	public static class ManaStormRenderState extends EntityRenderState {
		public float scale;
		public long seed;
	}

}
