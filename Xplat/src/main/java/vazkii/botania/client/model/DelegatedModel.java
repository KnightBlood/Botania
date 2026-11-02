/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;

import java.util.List;

/**
 * Base class for Botania's block state model wrappers. In MC 26.1 {@code BakedModel} was replaced by
 * {@link BlockStateModel}, which only exposes the geometry collection methods.
 */
public class DelegatedModel implements BlockStateModel {
	protected final BlockStateModel originalModel;

	public DelegatedModel(BlockStateModel originalModel) {
		this.originalModel = originalModel;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> output) {
		originalModel.collectParts(random, output);
	}

	@Override
	public Material.Baked particleMaterial() {
		return originalModel.particleMaterial();
	}

	@Override
	public int materialFlags() {
		return originalModel.materialFlags();
	}
}
