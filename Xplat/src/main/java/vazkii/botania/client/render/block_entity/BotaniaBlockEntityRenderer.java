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

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Adapts the pre-1.21.9 immediate-mode block entity rendering style to the extract/submit pipeline.
 * <p>
 * Subclasses keep implementing {@link #render} with a {@link BotaniaRenderContext} and a packed
 * light value; the recorded geometry is replayed into the {@link SubmitNodeCollector} during
 * {@link #submit}.
 */
public abstract class BotaniaBlockEntityRenderer<T extends BlockEntity>
		implements BlockEntityRenderer<T, BotaniaBlockEntityRenderState<T>> {

	@Override
	public BotaniaBlockEntityRenderState<T> createRenderState() {
		return new BotaniaBlockEntityRenderState<>();
	}

	@Override
	public void extractRenderState(T blockEntity, BotaniaBlockEntityRenderState<T> state, float partialTicks,
			Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		state.blockEntity = blockEntity;
		state.partialTicks = partialTicks;
	}

	@Override
	public final void submit(BotaniaBlockEntityRenderState<T> state, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		BotaniaRenderContext buffers = new BotaniaRenderContext(camera);
		render(state.blockEntity, state.partialTicks, poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
		buffers.flush(submitNodeCollector);
	}

	public abstract void render(T blockEntity, float partialTicks, PoseStack poseStack,
			BotaniaRenderContext buffers, int light, int overlay);

	/**
	 * Type-erasing bridge for callers that only have a {@code BlockEntity} reference, such as item
	 * renderers that drive a block entity renderer with a dummy block entity.
	 */
	@SuppressWarnings("unchecked")
	public final void renderUntyped(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
			BotaniaRenderContext buffers, int light, int overlay) {
		render((T) blockEntity, partialTicks, poseStack, buffers, light, overlay);
	}
}
