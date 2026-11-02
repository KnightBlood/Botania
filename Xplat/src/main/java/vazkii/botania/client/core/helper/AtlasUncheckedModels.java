/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone/extra-model bake helpers that skip vanilla's atlas validation.
 * <p>
 * Vanilla's {@link SimpleModelWrapper#bake} rejects any model whose quads contain
 * sprites from outside the block atlas (botania's {@code icon/*} models use item
 * atlas sprites). Wrappers here bake the same geometry without that check; call
 * {@link #renderTypeFor} when drawing so the quads get a render type bound to the
 * atlas they actually live in.
 */
public final class AtlasUncheckedModels {

	/**
	 * Bakes a resolved model into a {@link BlockStateModel} without verifying that
	 * all sprites live in the block atlas. Mirrors
	 * {@code SimpleModelWrapper.bake} minus the rejection.
	 */
	public static BlockStateModel bakeUnchecked(ResolvedModel model, ModelBaker baker) {
		TextureSlots slots = model.getTopTextureSlots();
		boolean ambientOcclusion = model.getTopAmbientOcclusion();
		var particleMaterial = model.resolveParticleMaterial(slots, baker);
		QuadCollection geometry = model.bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);
		return new SingleVariant(new SimpleModelWrapper(geometry, ambientOcclusion, particleMaterial));
	}

	/**
	 * Picks a render type matching the atlas the model's quads are sampled from.
	 * Returns {@code fallback} when the model has no quads or everything lives in
	 * the block atlas (the common case, where the previous moving-block sheet
	 * behavior is preserved).
	 */
	public static net.minecraft.client.renderer.rendertype.RenderType renderTypeFor(BlockStateModel model,
			net.minecraft.client.renderer.rendertype.RenderType fallback) {
		for (var quad : firstQuads(model)) {
			var info = quad.materialInfo();
			if (info != null && info.sprite() != null
					&& !info.sprite().atlasLocation().equals(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS)) {
				return info.itemRenderType();
			}
		}
		return fallback;
	}

	private static List<net.minecraft.client.resources.model.geometry.BakedQuad> firstQuads(BlockStateModel model) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(net.minecraft.util.RandomSource.create(), parts);
		List<net.minecraft.client.resources.model.geometry.BakedQuad> quads = new ArrayList<>();
		for (BlockStateModelPart part : parts) {
			collectQuads(part, null, quads);
			for (Direction dir : Direction.values()) {
				collectQuads(part, dir, quads);
			}
			if (!quads.isEmpty()) {
				break;
			}
		}
		return quads;
	}

	private static void collectQuads(BlockStateModelPart part, @Nullable Direction dir,
			List<net.minecraft.client.resources.model.geometry.BakedQuad> out) {
		try {
			out.addAll(part.getQuads(dir));
		} catch (RuntimeException ignored) {
			// missing models may throw on any access; fall through to the fallback sheet
		}
	}

	private AtlasUncheckedModels() {}
}
