/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.FloatingFlowerBlockEntity;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;
import vazkii.botania.common.lib.LibMisc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * In MC 26.1 the NeoForge-specific {@code IUnbakedGeometry}/{@code ModelData} system is gone (geometry handling is vanilla now).
 * The {@code botania:floating_flower} loader JSON is still parsed by {@link Loader} to
 * produce a static flower + grass island geometry for item rendering, while the actual
 * per-position island selection is done by {@link Baked}, a {@link DynamicBlockStateModel}
 * installed over floating flower blocks' blockstate models at {@code ModelEvent.ModifyBakingResult}.
 * The flower and every island type are pre-baked through NeoForge's standalone models.
 */
public final class NeoForgeFloatingFlowerModel {
	private static final Transformation MOVE_FLOWER = new Transformation(
			new Vector3f(0F, 0.2F, 0F), null, new Vector3f(0.5F, 0.5F, 0.5F), null);

	private NeoForgeFloatingFlowerModel() {}

	public static Map<FloatingFlower.IslandType, Identifier> islandModels() {
		return BotaniaAPIClient.instance().getRegisteredIslandTypeModels();
	}

	/** Same derivation the datagen {@code FloatingFlowerModelProvider} uses. */
	public static Identifier flowerModelId(FloatingFlowerBlock block) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		String name = id.getPath();
		String nonFloat;
		if (name.endsWith("_floating_flower")) {
			nonFloat = name.replace("_floating_flower", "_mystical_flower");
		} else {
			nonFloat = name.replace("floating_", "");
		}
		return prefix("block/" + nonFloat);
	}

	public static ModelState flowerState(ModelState outer) {
		Transformation mul = MOVE_FLOWER.compose(outer.transformation());
		return new ModelState() {
			@Override
			public Transformation transformation() {
				return mul;
			}
		};
	}

	private static QuadCollection bakeModel(ModelBaker baker, Identifier id, ModelState state) {
		ResolvedModel model = baker.getModel(id);
		return model.bakeTopGeometry(model.getTopTextureSlots(), baker, state);
	}

	@Nullable
	public static FloatingFlower.IslandType islandTypeAt(BlockAndTintGetter level, BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		FloatingFlower data = null;
		if (be instanceof FloatingFlowerBlockEntity floating) {
			data = floating.getFloatingData();
		} else if (be instanceof SpecialFlowerBlockEntity special && special.isFloating()) {
			data = special.getFloatingData();
		}
		return data == null ? null : data.getIslandType();
	}

	/**
	 * Loader for the {@code botania:floating_flower} model JSON. Produces a static
	 * flower + grass island geometry, which is what item rendering uses.
	 */
	public enum Loader implements UnbakedModelLoader<Loader.Unbaked> {
		INSTANCE;

		@Override
		public Unbaked read(JsonObject json, JsonDeserializationContext ctx) {
			JsonObject flower = json.getAsJsonObject("flower");
			Identifier flowerId = Identifier.parse(flower.get("parent").getAsString());
			return new Unbaked(flowerId);
		}

		public static final class Unbaked implements UnbakedModel {
			private final Identifier flowerId;

			Unbaked(Identifier flowerId) {
				this.flowerId = flowerId;
			}

			/**
			 * Declaring the flower model as the parent makes the resolution chain inherit
			 * its texture slots (notably the particle sprite) and transforms; the custom
			 * {@link #geometry()} below replaces the inherited geometry. Without this the
			 * baked model's particle falls back to the missing sprite.
			 */
			@Override
			public Identifier parent() {
				return this.flowerId;
			}

			@Override
			public void resolveDependencies(ResolvableModel.Resolver resolver) {
				// The flower is reached through the parent chain; only the island
				// models consumed by geometry() need explicit dependency marking.
				for (var e : islandModels().entrySet()) {
					resolver.markDependency(e.getValue());
				}
			}

			@Override
			public UnbakedGeometry geometry() {
				return (slots, baker, state, name) -> {
					QuadCollection flower = bakeModel(baker, this.flowerId, NeoForgeFloatingFlowerModel.flowerState(state));
					var grassId = islandModels().get(FloatingFlower.IslandType.GRASS);
					QuadCollection island = grassId == null ? QuadCollection.EMPTY
							: bakeModel(baker, grassId, state);
					return new QuadCollection.Builder().addAll(flower).addAll(island).build();
				};
			}
		}
	}

	/**
	 * Per-position island selection, replacing the old {@code ModelData}/{@code ModelProperty} mechanism.
	 */
	public static final class Baked implements DynamicBlockStateModel {
		private final BlockStateModel fallback;
		private final BlockStateModel flower;
		private final Map<FloatingFlower.IslandType, BlockStateModel> islands;

		public Baked(BlockStateModel fallback, BlockStateModel flower,
				Map<FloatingFlower.IslandType, BlockStateModel> islands) {
			this.fallback = fallback;
			this.flower = flower;
			this.islands = islands;
		}

		@Override
		public void collectParts(RandomSource rand, List<BlockStateModelPart> parts) {
			this.fallback.collectParts(rand, parts);
		}

		@Override
		public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
				RandomSource rand, List<BlockStateModelPart> parts) {
			FloatingFlower.IslandType type = islandTypeAt(level, pos);
			List<BlockStateModelPart> combined = new ArrayList<>();
			this.flower.collectParts(rand, combined);
			BlockStateModel island = this.islands.get(type == null ? FloatingFlower.IslandType.GRASS : type);
			if (island != null) {
				island.collectParts(rand, combined);
			}
			parts.addAll(combined);
		}

		@Override
		public Material.Baked particleMaterial() {
			return this.fallback.particleMaterial();
		}

		@Override
		public int materialFlags() {
			int flags = this.fallback.materialFlags();
			for (BlockStateModel island : this.islands.values()) {
				flags |= island.materialFlags();
			}
			return flags;
		}
	}

	/** Registers the flower/island standalone models used by {@link Baked}. */
	public static void forEachStandaloneModel(BiConsumer<Identifier, SimpleUnbakedStandaloneModel<BlockStateModel>> consumer) {
		for (var e : islandModels().entrySet()) {
			consumer.accept(e.getValue(), SimpleUnbakedStandaloneModel.blockStateModel(e.getValue()));
		}
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block instanceof FloatingFlowerBlock floating) {
				Identifier flowerId = flowerModelId(floating);
				consumer.accept(flowerId,
						SimpleUnbakedStandaloneModel.blockStateModel(flowerId, flowerState(IDENTITY_STATE)));
			}
		}
	}

	private static final ModelState IDENTITY_STATE = new ModelState() {
		@Override
		public Transformation transformation() {
			return Transformation.IDENTITY;
		}
	};

	/** Replaces the baked model for every floating flower blockstate. */
	public static Map<BlockState, BlockStateModel> replaceFloatingFlowerModels(
			Map<BlockState, BlockStateModel> models, Map<Identifier, BlockStateModel> standaloneById) {
		for (Block block : BuiltInRegistries.BLOCK) {
			if (block instanceof FloatingFlowerBlock floating) {
				Identifier flowerId = flowerModelId(floating);
				BlockStateModel flower = standaloneById.get(flowerId);
				if (flower == null) {
					continue;
				}
				Map<FloatingFlower.IslandType, BlockStateModel> islands = new HashMap<>();
				islandModels().forEach((type, islandId) -> {
					var island = standaloneById.get(islandId);
					if (island != null) {
						islands.put(type, island);
					}
				});
				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					BlockStateModel original = models.get(state);
					if (original != null) {
						models.put(state, new Baked(original, flower, islands));
					}
				}
			}
		}
		return models;
	}
}
