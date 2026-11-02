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
import net.minecraft.client.renderer.texture.OverlayTexture;

import org.joml.Matrix4f;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.BabylonWeaponEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class BabylonWeaponRenderer extends EntityRenderer<BabylonWeaponEntity, BabylonWeaponRenderer.BabylonWeaponRenderState> {

	public BabylonWeaponRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@Override
	public BabylonWeaponRenderState createRenderState() {
		return new BabylonWeaponRenderState();
	}

	@Override
	public void extractRenderState(BabylonWeaponEntity weapon, BabylonWeaponRenderState state, float partialTicks) {
		super.extractRenderState(weapon, state, partialTicks);
		state.rotation = weapon.getRotation();
		state.liveTicks = weapon.getLiveTicks();
		state.delay = weapon.getDelay();
		state.chargeTicks = weapon.getChargeTicks();
		state.variety = weapon.getVariety();
		state.randSeed = weapon.getUUID().getMostSignificantBits();
		state.tickCount = weapon.tickCount;
		state.partialTick = partialTicks;
	}

	@Override
	public void submit(BabylonWeaponRenderState state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState camera) {
		super.submit(state, ms, collector, camera);

		float partialTicks = state.partialTick;
		ms.pushPose();
		ms.mulPose(VecHelper.rotateY(state.rotation));

		int live = state.liveTicks;
		int delay = state.delay;
		float charge = Math.min(10F, Math.max(live, state.chargeTicks) + partialTicks);
		float chargeMul = charge / 10F;

		ms.pushPose();
		ms.translate(-0.75, 0, 1); // X shifts the weapon hilt to the center of the circle, Z makes it intersect it.
		float s = 1.5F;
		ms.scale(s, s, s);
		ms.mulPose(VecHelper.rotateY(90F)); // Rotate to make it match facing, instead of perpendicular to the circle
		ms.mulPose(VecHelper.rotateZ(-45F)); // Perpendicular to the ground, instead of the rising 45 deg of the sprite

		var model = MiscellaneousModels.INSTANCE.kingKeyWeaponModels[state.variety];
		RenderHelper.renderBlockModel(ms, collector, null, model, 0xF000F0, OverlayTexture.NO_OVERLAY);
		ms.popPose();

		Random rand = new Random(state.randSeed);
		ms.mulPose(VecHelper.rotateX(-90F)); // Lay the circle horizontally
		ms.translate(0F, -0.3F + rand.nextFloat() * 0.1F, 0F); // Randomly offset how deep the item is in the circle

		s = chargeMul;
		if (live > delay) {
			s -= Math.min(1F, (live - delay + partialTicks) * 0.2F);
		}
		s *= 2F;
		ms.scale(s, s, s);

		ms.mulPose(VecHelper.rotateY(charge * 9F + (state.tickCount + partialTicks) * 0.5F + rand.nextFloat() * 360F));

		final float cm = chargeMul;
		collector.submitCustomGeometry(ms, RenderHelper.BABYLON_ICON, (pose, buffer) -> {
			Matrix4f mat = pose.pose();
			// 26.1: BABYLON_ICON runs on the ITEM_CUTOUT pipeline whose vertex format is
			// DefaultVertexFormat.ENTITY, so overlay (UV1), lightmap (UV2) and normal must be set.
			buffer.addVertex(mat, -1, 0, -1).setColor(1, 1, 1, cm).setUv(0, 0)
					.setOverlay(OverlayTexture.NO_OVERLAY).setUv2(240, 240).setNormal(pose, 0, 1, 0);
			buffer.addVertex(mat, -1, 0, 1).setColor(1, 1, 1, cm).setUv(0, 1)
					.setOverlay(OverlayTexture.NO_OVERLAY).setUv2(240, 240).setNormal(pose, 0, 1, 0);
			buffer.addVertex(mat, 1, 0, 1).setColor(1, 1, 1, cm).setUv(1, 1)
					.setOverlay(OverlayTexture.NO_OVERLAY).setUv2(240, 240).setNormal(pose, 0, 1, 0);
			buffer.addVertex(mat, 1, 0, -1).setColor(1, 1, 1, cm).setUv(1, 0)
					.setOverlay(OverlayTexture.NO_OVERLAY).setUv2(240, 240).setNormal(pose, 0, 1, 0);
		});

		ms.popPose();
	}

	public static class BabylonWeaponRenderState extends EntityRenderState {
		public float rotation;
		public int liveTicks;
		public int delay;
		public int chargeTicks;
		public int variety;
		public long randSeed;
		public int tickCount;
		public float partialTick;
	}

}
