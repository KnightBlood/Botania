/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fc;

/**
 * A pie/progress wedge drawn on top of a GUI item, replacing the stencil based approach of 1.21.
 * <p>
 * The GUI renderer only ever draws quads, so every wedge triangle is emitted as a quad with its last
 * vertex duplicated (the resulting degenerate triangle is discarded by the rasterizer).
 */
public record ProgressPieRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
		float centerX, float centerY, float radius, int degrees, int centerColor, int edgeColor,
		@Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
	public ProgressPieRenderState(Matrix3x2fc pose, float centerX, float centerY, float radius, int degrees,
			int centerColor, int edgeColor, @Nullable ScreenRectangle scissorArea) {
		this(RenderPipelines.GUI, TextureSetup.noTexture(), pose, centerX, centerY, radius, degrees,
				centerColor, edgeColor, scissorArea, computeBounds(centerX, centerY, radius, pose, scissorArea));
	}

	@Override
	public void buildVertices(VertexConsumer consumer) {
		for (int i = 0; i < degrees; i++) {
			float start = (i - 90) / 180F * Mth.PI;
			float end = (i + 1 - 90) / 180F * Mth.PI;
			float x0 = centerX + Mth.cos(start) * radius;
			float y0 = centerY + Mth.sin(start) * radius;
			float x1 = centerX + Mth.cos(end) * radius;
			float y1 = centerY + Mth.sin(end) * radius;

			consumer.addVertexWith2DPose(pose, centerX, centerY).setColor(centerColor);
			consumer.addVertexWith2DPose(pose, x0, y0).setColor(edgeColor);
			consumer.addVertexWith2DPose(pose, x1, y1).setColor(edgeColor);
			consumer.addVertexWith2DPose(pose, x1, y1).setColor(edgeColor);
		}
	}

	private static @Nullable ScreenRectangle computeBounds(float centerX, float centerY, float radius,
			Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
		int minX = Mth.floor(centerX - radius);
		int minY = Mth.floor(centerY - radius);
		int size = Mth.ceil(radius * 2) + 1;
		ScreenRectangle bounds = new ScreenRectangle(minX, minY, size, size).transformMaxBounds(pose);
		return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
	}
}
