/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.block_entity.FloatingFlowerBlockEntity;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * Fabric-side implementation of the {@code botania:floating_flower} model loader. The NeoForge
 * counterpart is {@code ForgeFloatingFlowerModel}; the two mirror each other because 26.1 wires
 * custom model loading differently per loader:
 * <ul>
 * <li>Model JSON dispatch: NeoForge keys off the {@code "loader"} JSON key via
 * {@code UnbakedModelLoader}; Fabric keys off {@code "fabric:type"} via
 * {@link UnbakedModelDeserializer}. The datagen JSON carries both keys. The resulting
 * {@link LoaderModel} merges the flower (lifted and half-scaled) with the grass island into one
 * static geometry, which is what item rendering uses.</li>
 * <li>Per-position island selection: NeoForge swaps in a {@code DynamicBlockStateModel} at
 * {@code ModelEvent.ModifyBakingResult}; Fabric wraps the baked model through
 * {@code modifyBlockModelAfterBake} and selects the island per position inside
 * {@link Baked#emitQuads}.</li>
 * </ul>
 * Dependencies: the flower model is declared through {@link LoaderModel#parent()} (vanilla's
 * {@code ModelDiscovery} resolves parent chains itself), and the island models are already
 * registered as extra models by {@code MiscellaneousModels}, whose {@code resolveDependencies}
 * marks them before {@code ModelDiscovery.resolve()} runs.
 */
public final class FabricFloatingFlowerModel {
	private static final Transformation MOVE_FLOWER = new Transformation(
			new Vector3f(0F, 0.2F, 0F), null, new Vector3f(0.5F, 0.5F, 0.5F), null);

	/** Baked flower part per flower model id, shared by all states of a floating flower block. */
	private static final Map<Identifier, BlockStateModelPart> FLOWER_PARTS = new ConcurrentHashMap<>();
	/** Baked island parts per island type, shared by every floating flower block. */
	private static final Map<FloatingFlower.IslandType, BlockStateModelPart> ISLAND_PARTS = new ConcurrentHashMap<>();

	private FabricFloatingFlowerModel() {}

	/** Registers the model JSON deserializer for the {@code botania:floating_flower} loader id. */
	public static void init() {
		UnbakedModelDeserializer.register(ClientXplatAbstractions.FLOATING_FLOWER_MODEL_LOADER_ID,
				FabricFloatingFlowerModel::deserialize);
	}

	private static UnbakedModel deserialize(JsonObject json, JsonDeserializationContext ctx) {
		JsonObject flower = json.getAsJsonObject("flower");
		Identifier flowerId = Identifier.parse(flower.get("parent").getAsString());
		return new LoaderModel(flowerId);
	}

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

	/** Mirrors vanilla {@code SimpleModelWrapper.bake} for an explicitly resolved model. */
	private static BlockStateModelPart bakePart(ModelBaker baker, Identifier id, ModelState state) {
		ResolvedModel model = baker.getModel(id);
		TextureSlots slots = model.getTopTextureSlots();
		return new SimpleModelWrapper(model.bakeTopGeometry(slots, baker, state),
				model.getTopAmbientOcclusion(), model.resolveParticleMaterial(slots, baker));
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
	 * Wraps a floating flower blockstate's baked model with per-position island selection,
	 * pre-baking the shared flower and island parts while the {@link ModelBaker} is still
	 * available (bake phase; the static caches make this a one-off per model id).
	 */
	public static BlockStateModel wrapBlockModel(BlockStateModel original, ModelBaker baker, BlockState state) {
		FloatingFlowerBlock block = (FloatingFlowerBlock) state.getBlock();
		Identifier flowerId = flowerModelId(block);
		BlockStateModelPart flower = FLOWER_PARTS.computeIfAbsent(flowerId,
				id -> bakePart(baker, id, flowerState(BlockModelRotation.IDENTITY)));
		for (var e : islandModels().entrySet()) {
			ISLAND_PARTS.computeIfAbsent(e.getKey(), type -> bakePart(baker, e.getValue(), BlockModelRotation.IDENTITY));
		}
		return new Baked(original, flower);
	}

	/**
	 * Loader model for the {@code botania:floating_flower} JSON. {@link #parent()} pointing at the
	 * flower model lets vanilla's {@code ModelDiscovery} pull the flower (and its parent chain,
	 * textures and display transforms) into the resolved set; {@link #geometry()} merges the
	 * transformed flower with the grass island, which is what item rendering shows.
	 */
	private record LoaderModel(Identifier flowerId) implements UnbakedModel {
		@Override
		public Identifier parent() {
			return this.flowerId;
		}

		@Override
		public UnbakedGeometry geometry() {
			return (slots, baker, state, name) -> {
				QuadCollection flower = bakePartGeometry(baker, this.flowerId, flowerState(state));
				var grassId = islandModels().get(FloatingFlower.IslandType.GRASS);
				QuadCollection island = grassId == null ? QuadCollection.EMPTY
						: bakePartGeometry(baker, grassId, state);
				return new QuadCollection.Builder().addAll(flower).addAll(island).build();
			};
		}
	}

	/** Bakes a resolved model's top geometry, mirroring NeoForge's {@code Loader.Unbaked.bakeModel}. */
	private static QuadCollection bakePartGeometry(
			ModelBaker baker, Identifier id, ModelState state) {
		ResolvedModel model = baker.getModel(id);
		return model.bakeTopGeometry(model.getTopTextureSlots(), baker, state);
	}

	/**
	 * Per-position island selection, replacing the old {@code ModelData}/{@code ModelProperty}
	 * mechanism (upstream's {@code FabricFloatingFlowerModel.Baked.emitBlockQuads}).
	 */
	private static final class Baked implements BlockStateModel, FabricBlockStateModel {
		private final BlockStateModel fallback;
		private final BlockStateModelPart flower;

		Baked(BlockStateModel fallback, BlockStateModelPart flower) {
			this.fallback = fallback;
			this.flower = flower;
		}

		@Override
		public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state,
				RandomSource rand, Predicate<Direction> cull) {
			FloatingFlower.IslandType type = islandTypeAt(level, pos);
			BlockStateModelPart island = ISLAND_PARTS.get(type == null ? FloatingFlower.IslandType.GRASS : type);
			if (island == null) {
				// The island type has no registered model (or wrap ran without islands); emit the
				// static model as-is rather than rendering the position empty.
				List<BlockStateModelPart> parts = new ArrayList<>();
				this.fallback.collectParts(rand, parts);
				for (BlockStateModelPart part : parts) {
					part.emitQuads(emitter, cull);
				}
			} else {
				island.emitQuads(emitter, cull);
				this.flower.emitQuads(emitter, cull);
			}
		}

		@Override
		public void collectParts(RandomSource rand, List<BlockStateModelPart> parts) {
			this.fallback.collectParts(rand, parts);
		}

		@Override
		public Material.Baked particleMaterial() {
			return this.fallback.particleMaterial();
		}

		@Override
		public int materialFlags() {
			return this.fallback.materialFlags();
		}
	}
}
