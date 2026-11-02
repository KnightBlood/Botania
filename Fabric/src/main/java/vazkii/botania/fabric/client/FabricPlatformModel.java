/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.client;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.PlatformBlockEntity;
import vazkii.botania.common.lib.BotaniaTags;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Camouflage wrapper for platform blocks. In MC 26.1 {@code ModelData} and the old
 * renderer-v1 {@code ForwardingBakedModel} are gone; the wrapper now implements the
 * vanilla {@link BlockStateModel} plus Fabric's per-position {@link FabricBlockStateModel},
 * and the disguised state is resolved per position inside {@link #emitQuads}.
 */
public class FabricPlatformModel implements BlockStateModel, FabricBlockStateModel {
	private final BlockStateModel original;

	public FabricPlatformModel(BlockStateModel original) {
		this.original = original;
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
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter world, BlockPos pos, BlockState state,
			RandomSource rand, Predicate<Direction> cull) {
		if (!(state.getBlock() instanceof PlatformBlock)) {
			emitAll(emitter, original, rand, cull);
			return;
		}
		BlockStateModel disguise = disguiseModel(world, pos, state);
		emitAll(emitter, disguise != null ? disguise : original, rand, cull);
	}

	private static void emitAll(QuadEmitter emitter, BlockStateModel model, RandomSource rand,
			Predicate<Direction> cull) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(rand, parts);
		for (BlockStateModelPart part : parts) {
			for (Direction face : Direction.values()) {
				if (cull.test(face)) {
					continue;
				}
				emitPart(emitter, part.getQuads(face));
			}
			// un-culled quads without an explicit facing
			for (BakedQuad quad : part.getQuads(null)) {
				if (!cull.test(quad.direction())) {
					emitter.fromBakedQuad(quad);
					emitter.emit();
				}
			}
		}
	}

	private static void emitPart(QuadEmitter emitter, List<BakedQuad> quads) {
		for (BakedQuad quad : quads) {
			emitter.fromBakedQuad(quad);
			emitter.emit();
		}
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter world, BlockPos pos, BlockState state) {
		BlockStateModel disguise = disguiseModel(world, pos, state);
		return disguise != null ? disguise.particleMaterial() : original.particleMaterial();
	}

	@Override
	public int materialFlags(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource rand) {
		BlockStateModel disguise = disguiseModel(world, pos, state);
		return disguise != null ? disguise.materialFlags() : original.materialFlags();
	}

	@Override
	public void collectParts(RandomSource rand, List<BlockStateModelPart> parts) {
		original.collectParts(rand, parts);
	}

	@Override
	public Material.Baked particleMaterial() {
		return original.particleMaterial();
	}

	@Override
	public int materialFlags() {
		return original.materialFlags();
	}
}
