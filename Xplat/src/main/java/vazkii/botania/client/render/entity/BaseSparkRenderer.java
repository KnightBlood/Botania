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
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.data.AtlasIds;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.state.level.CameraRenderState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.entity.SparkBaseEntity;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.Objects;
import java.util.Random;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public abstract class BaseSparkRenderer<T extends SparkBaseEntity> extends EntityRenderer<T, BaseSparkRenderer.SparkRenderState> {

	private final TextureAtlasSprite starSprite;
	private final TextureAtlasSprite worldSprite;

	public BaseSparkRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
		var atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);
		this.starSprite = Objects.requireNonNull(
				atlas.getSprite(prefix("item/corporea_spark_star"))
		);
		this.worldSprite = Objects.requireNonNull(
				atlas.getSprite(prefix("item/spark"))
		);
	}

	@Override
	public SparkRenderState createRenderState() {
		return new SparkRenderState();
	}

	@Override
	public void extractRenderState(T entity, SparkRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.baseIcon = getBaseIcon(entity);
		state.spinningIcon = getSpinningIcon(entity);
		state.time = (entity.level().getGameTime() % 24000) + partialTicks + new Random(entity.getId()).nextInt(200);
		state.networkColor = ColorHelper.getColorValue(entity.getNetwork());
	}

	@Override
	public void submit(SparkRenderState state, PoseStack ms, SubmitNodeCollector collector, CameraRenderState camera) {
		super.submit(state, ms, collector, camera);

		TextureAtlasSprite iicon = state.baseIcon;
		ClientXplatAbstractions.instance().markSpriteActive(iicon);

		double time = state.time;
		float a = 0.1F + (state.isInvisible ? 0 : 1) * 0.8F;

		int alpha = (int) ((0.7 + 0.3 * (Math.sin(time / 5.0) + 0.5) * 2) * a * 255.0);
		int iconColor = 0xFFFFFF | (alpha << 24);

		float scale = 0.75F + 0.1F * (float) Math.sin(time / 10);
		ms.pushPose();
		ms.scale(scale, scale, scale);

		// 26.1: EntityRenderDispatcher.cameraOrientation() was removed
		Quaternionf camRot = this.entityRenderDispatcher.camera != null
				? this.entityRenderDispatcher.camera.rotation()
				: new Quaternionf().rotationYXZ(-(float) Math.PI / 2, (float) Math.PI, 0);
		ms.pushPose();
		ms.mulPose(camRot);
		ms.mulPose(VecHelper.rotateY(180));

		var buffer = collector;
		buffer.submitCustomGeometry(ms, RenderHelper.SPARK, (pose, buf) -> renderIcon(pose, buf, iicon, iconColor));

		ms.pushPose();
		ms.translate(-0.02 + Math.sin(time / 20) * 0.2, 0.24 + Math.cos(time / 20) * 0.2, 0.005);
		ms.scale(0.2F, 0.2F, 0.2F);
		int starColor = state.networkColor | ((int) (a * 255.0F) << 24);
		buffer.submitCustomGeometry(ms, RenderHelper.SPARK, (pose, buf) -> renderIcon(pose, buf, this.starSprite, starColor));
		ms.popPose();

		TextureAtlasSprite spinningIcon = state.spinningIcon;
		if (spinningIcon != null) {
			ms.translate(-0.02 + Math.sin(time / 20) * -0.2, 0.24 + Math.cos(time / 20) * -0.2, 0.005);
			ms.scale(0.2F, 0.2F, 0.2F);
			buffer.submitCustomGeometry(ms, RenderHelper.SPARK, (pose, buf) -> renderIcon(pose, buf, spinningIcon, iconColor));
		}
		ms.popPose();

		ms.popPose();
	}

	protected TextureAtlasSprite getBaseIcon(T entity) {
		return this.worldSprite;
	}

	@Nullable
	protected TextureAtlasSprite getSpinningIcon(T entity) {
		return null;
	}

	public static class SparkRenderState extends EntityRenderState {
		public TextureAtlasSprite baseIcon;
		public @Nullable TextureAtlasSprite spinningIcon;
		public double time;
		public int networkColor;
	}

	private void renderIcon(PoseStack.Pose pose, VertexConsumer buffer, TextureAtlasSprite icon, int color) {
		float f = icon.getU0();
		float f1 = icon.getU1();
		float f2 = icon.getV0();
		float f3 = icon.getV1();
		float f4 = 1.0F;
		float f5 = 0.5F;
		float f6 = 0.25F;
		int fullbright = 0xF000F0;
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		Matrix4f mat = pose.pose();
		buffer.addVertex(mat, 0.0F - f5, 0.0F - f6, 0.0F).setColor(r, g, b, a).setUv(f, f3).setLight(fullbright);
		buffer.addVertex(mat, f4 - f5, 0.0F - f6, 0.0F).setColor(r, g, b, a).setUv(f1, f3).setLight(fullbright);
		buffer.addVertex(mat, f4 - f5, f4 - f6, 0.0F).setColor(r, g, b, a).setUv(f1, f2).setLight(fullbright);
		buffer.addVertex(mat, 0.0F - f5, f4 - f6, 0.0F).setColor(r, g, b, a).setUv(f, f2).setLight(fullbright);
	}

}
