/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.forge.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;

/**
 * Camouflage wrapper for platform blocks. In MC 26.1 {@code ModelData} is gone; the
 * disguised state is resolved per position inside {@link #collectParts}.
 */
public class ForgePlatformModel extends DelegateBlockStateModel {
	public ForgePlatformModel(BlockStateModel originalModel) {
		super(originalModel);
	}

	@Nullable
	private BlockStateModel disguiseModel(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		if (world.getBlockEntity(pos) instanceof PlatformBlockEntity platform) {
			var data = new PlatformBlockEntity.PlatformData(platform);
			BlockState heldState = data.state();
			if (heldState != null && !heldState.is(BotaniaTags.Blocks.UNSUPPORTED_PLATFORM_DISGUISE)) {
				return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(heldState);
			}
		}
		return null;
	}

	@Override
	public void collectParts(BlockAndTintGetter world, BlockPos pos, BlockState state,
			RandomSource rand, List<BlockStateModelPart> parts) {
		if (!(state.getBlock() instanceof PlatformBlock)) {
			super.collectParts(world, pos, state, rand, parts);
			return;
		}
		BlockStateModel disguise = disguiseModel(world, pos, state);
		if (disguise == null) {
			super.collectParts(world, pos, state, rand, parts);
		} else {
			disguise.collectParts(rand, parts);
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		BlockStateModel disguise = disguiseModel(world, pos, state);
		return disguise != null ? disguise.particleMaterial() : super.particleMaterial(world, pos, state);
	}

	@Override
	public int materialFlags(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		BlockStateModel disguise = disguiseModel(world, pos, state);
		return disguise != null ? disguise.materialFlags() : super.materialFlags(world, pos, state);
	}
}
