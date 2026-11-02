/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;


import com.mojang.math.Quadrant;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.properties.*;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.api.state.enums.AlfheimPortalState;
import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.common.block.*;
import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.block.decor.BuriedPetalBlock;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;
import vazkii.botania.common.block.decor.FlowerMotifBlock;
import vazkii.botania.common.block.decor.PetalBlock;
import vazkii.botania.common.block.decor.panes.BotaniaPaneBlock;
import vazkii.botania.common.block.red_string.RedStringBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.mixin.TextureSlotAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static net.minecraft.client.data.models.model.ModelLocationUtils.getModelLocation;
import static vazkii.botania.common.block.BotaniaBlocks.*;
import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BlockstateProvider implements DataProvider {
	/**
	 * 26.1: {@code ROTATION_HORIZONTAL_FACING} became private; recreate it locally
	 * (dispatch on {@code HORIZONTAL_FACING} with the matching Y rotation, NORTH being the identity).
	 */
	private static final PropertyDispatch<VariantMutator> ROTATION_HORIZONTAL_FACING = PropertyDispatch
			.modify(BlockStateProperties.HORIZONTAL_FACING)
			.select(Direction.NORTH, BlockModelGenerators.NOP)
			.select(Direction.EAST, BlockModelGenerators.Y_ROT_90)
			.select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
			.select(Direction.WEST, BlockModelGenerators.Y_ROT_270);

	/**
	 * 26.1: {@code ROTATION_FACING} became private; recreate it locally
	 * (dispatch on {@code FACING}, NORTH being the identity, verticals using X rotations).
	 */
	private static final PropertyDispatch<VariantMutator> ROTATION_FACING = PropertyDispatch
			.modify(BlockStateProperties.FACING)
			.select(Direction.DOWN, BlockModelGenerators.X_ROT_90)
			.select(Direction.UP, BlockModelGenerators.X_ROT_270)
			.select(Direction.NORTH, BlockModelGenerators.NOP)
			.select(Direction.SOUTH, BlockModelGenerators.Y_ROT_180)
			.select(Direction.WEST, BlockModelGenerators.Y_ROT_270)
			.select(Direction.EAST, BlockModelGenerators.Y_ROT_90);

	protected final PackOutput packOutput;

	protected final List<BlockModelDefinitionGenerator> blockstates = new ArrayList<>();

	protected final Map<Identifier, ModelInstance> models = new HashMap<>();
	protected final BiConsumer<Identifier, ModelInstance> modelOutput = models::put;

	public BlockstateProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	protected Logger getLogger() {
		return BotaniaAPI.LOGGER;
	}

	@NotNull
	@Override
	public String getName() {
		return "Botania Blockstates and Models";
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		try {
			registerStatesAndModels();
		} catch (Exception e) {
			getLogger().error("Error registering states and models", e);
		}

		PackOutput.PathProvider blockstatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
		PackOutput.PathProvider modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
		List<CompletableFuture<?>> output = new ArrayList<>();

		for (BlockModelDefinitionGenerator state : blockstates) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(state.block());
			Path path = blockstatePathProvider.json(id);
			output.add(DataProvider.saveStable(cache, BlockStateModelDispatcher.CODEC, state.create(), path));
		}

		for (Map.Entry<Identifier, ModelInstance> e : models.entrySet()) {
			Identifier modelId = e.getKey();
			Path path = modelPathProvider.json(modelId);
			output.add(DataProvider.saveStable(cache, e.getValue().get(), path));
		}
		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	protected void registerStatesAndModels() {
		Set<Block> remainingBlocks = BuiltInRegistries.BLOCK.stream()
				.filter(b -> LibMisc.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(b).getNamespace()))
				.collect(Collectors.toSet());

		// Manually written blockstate + models
		remainingBlocks.remove(ghostRail);
		remainingBlocks.remove(solidVines);

		// Manually written model, generated blockstate
		manualModel(remainingBlocks, cocoon);
		manualModel(remainingBlocks, corporeaCrystalCube);
		manualModel(remainingBlocks, distributor);
		manualModel(remainingBlocks, prism);
		manualModel(remainingBlocks, runeAltar);
		manualModel(remainingBlocks, spawnerClaw);

		// Single blocks
		var alfPortalModel = ModelTemplates.CUBE_ALL.create(getModelLocation(alfPortal), TextureMapping.cube(alfPortal), this.modelOutput);
		var alfPortalActivatedModel = ModelTemplates.CUBE_ALL.create(getModelLocation(alfPortal, "_activated"), TextureMapping.cube(new Material(getModelLocation(alfPortal, "_activated"))), this.modelOutput);
		this.blockstates.add(
				MultiVariantGenerator.dispatch(alfPortal).with(
						PropertyDispatch.initial(BotaniaStateProperties.ALFPORTAL_STATE)
								.select(AlfheimPortalState.OFF, multiVariant(new Variant(alfPortalModel)))
								.select(AlfheimPortalState.ON_X, multiVariant(new Variant(alfPortalActivatedModel)))
								.select(AlfheimPortalState.ON_Z, multiVariant(new Variant(alfPortalActivatedModel)))
				));
		remainingBlocks.remove(alfPortal);

		singleVariantBlockState(bifrostPerm,
				ModelTemplates.CUBE_ALL.create(
						getModelLocation(bifrostPerm),
						TextureMapping.cube(bifrost), this.modelOutput));
		remainingBlocks.remove(bifrostPerm);

		singleVariantBlockState(cacophonium,
				ModelTemplates.CUBE_TOP.create(cacophonium, (new TextureMapping())
						.put(TextureSlot.SIDE, getBlockTexture(Blocks.NOTE_BLOCK))
						.put(TextureSlot.TOP, getBlockTexture(cacophonium, "_top")), this.modelOutput));
		remainingBlocks.remove(cacophonium);

		var crateTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/crate")), Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.SIDE);
		var craftCrateBottomTex = getBlockTexture(craftCrate, "_bottom");
		var crateDispatch = PropertyDispatch.initial(BotaniaStateProperties.CRATE_PATTERN);
		for (var pattern : CraftyCratePattern.values()) {
			String suffix = pattern == CraftyCratePattern.NONE ? "" : "_" + pattern.getSerializedName().substring("crafty_".length());
			var model = crateTemplate.create(getModelLocation(craftCrate, suffix),
					new TextureMapping().put(TextureSlot.BOTTOM, craftCrateBottomTex)
							.put(TextureSlot.SIDE, getBlockTexture(craftCrate, suffix)),
					this.modelOutput);
			crateDispatch = crateDispatch.select(pattern, multiVariant(new Variant(model)));
		}
		this.blockstates.add(MultiVariantGenerator.dispatch(craftCrate).with(crateDispatch));
		remainingBlocks.remove(craftCrate);

		Material corpSlabSide = new Material(prefix("block/corporea_slab_side"));
		Material corpBlock = getBlockTexture(corporeaBlock);
		var corpSlabBottomModel = ModelTemplates.SLAB_BOTTOM.create(corporeaSlab,
				new TextureMapping()
						.put(TextureSlot.BOTTOM, corpBlock).put(TextureSlot.TOP, corpBlock).put(TextureSlot.SIDE, corpBlock),
				this.modelOutput);
		var corpSlabTopModel = ModelTemplates.SLAB_TOP.create(getModelLocation(corporeaSlab, "_top"),
				new TextureMapping()
						.put(TextureSlot.BOTTOM, corpBlock).put(TextureSlot.TOP, corpBlock).put(TextureSlot.SIDE, corpBlock),
				this.modelOutput);
		var corpSlabDoubleModel = ModelTemplates.CUBE_BOTTOM_TOP.create(prefix("block/corporea_double_slab"),
				new TextureMapping()
						.put(TextureSlot.SIDE, corpSlabSide).put(TextureSlot.BOTTOM, corpBlock).put(TextureSlot.TOP, corpBlock),
				this.modelOutput);
		blockstates.add(BlockModelGenerators.createSlab(corporeaSlab, BlockModelGenerators.plainVariant(corpSlabBottomModel), BlockModelGenerators.plainVariant(corpSlabTopModel), BlockModelGenerators.plainVariant(corpSlabDoubleModel)));
		remainingBlocks.remove(corporeaSlab);

		stairsBlock(remainingBlocks, corporeaStairs, corpBlock, corpBlock, corpBlock);

		this.blockstates.add(MultiVariantGenerator.dispatch(elfGlass, multiVariant(IntStream.rangeClosed(0, 3)
				.mapToObj(i -> {
					var model = ModelTemplates.CUBE_ALL.create(
							getModelLocation(elfGlass, "_" + i),
							TextureMapping.cube(getBlockTexture(elfGlass, "_" + i)),
							this.modelOutput);
					return weighted(1, new Variant(model));
				})
				.toList())));
		remainingBlocks.remove(elfGlass);

		singleVariantBlockState(enchantedSoil, ModelTemplates.CUBE_BOTTOM_TOP.create(
				enchantedSoil,
				TextureMapping.cubeBottomTop(enchantedSoil).put(TextureSlot.BOTTOM, getBlockTexture(Blocks.DIRT)),
				this.modelOutput
		));
		remainingBlocks.remove(enchantedSoil);

		var pumpkinModel = ModelTemplates.CUBE_ORIENTABLE.create(felPumpkin, new TextureMapping()
				.put(TextureSlot.SIDE, getBlockTexture(Blocks.PUMPKIN, "_side"))
				.put(TextureSlot.FRONT, getBlockTexture(felPumpkin))
				.put(TextureSlot.TOP, getBlockTexture(Blocks.PUMPKIN, "_top")),
				this.modelOutput
		);
		this.blockstates.add(MultiVariantGenerator.dispatch(felPumpkin, multiVariant(new Variant(pumpkinModel)))
				.with(ROTATION_HORIZONTAL_FACING));
		remainingBlocks.remove(felPumpkin);

		ModelTemplate eightByEightTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/eightbyeight")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.NORTH, TextureSlot.SOUTH, TextureSlot.WEST, TextureSlot.EAST);
		singleVariantBlockState(forestEye, eightByEightTemplate.create(forestEye,
				new TextureMapping()
						.put(TextureSlot.BOTTOM, getBlockTexture(forestEye, "_bottom"))
						.put(TextureSlot.TOP, getBlockTexture(forestEye, "_top"))
						.put(TextureSlot.NORTH, getBlockTexture(forestEye, "_north"))
						.put(TextureSlot.SOUTH, getBlockTexture(forestEye, "_south"))
						.put(TextureSlot.WEST, getBlockTexture(forestEye, "_west"))
						.put(TextureSlot.EAST, getBlockTexture(forestEye, "_east")),
				this.modelOutput));
		remainingBlocks.remove(forestEye);

		var plateFile = getModelLocation(incensePlate);
		this.blockstates.add(MultiVariantGenerator.dispatch(incensePlate, multiVariant(new Variant(plateFile)))
				.with(ROTATION_HORIZONTAL_FACING));
		remainingBlocks.remove(incensePlate);

		var fourHighBottomTopTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/four_high_bottom_top")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
		singleVariantBlockState(lightLauncher, fourHighBottomTopTemplate.create(lightLauncher,
				new TextureMapping()
						.put(TextureSlot.BOTTOM, getBlockTexture(lightLauncher, "_end"))
						.put(TextureSlot.TOP, getBlockTexture(lightLauncher, "_end"))
						.put(TextureSlot.SIDE, getBlockTexture(lightLauncher, "_side")),
				this.modelOutput
		));
		remainingBlocks.remove(lightLauncher);

		singleVariantBlockState(openCrate,
				crateTemplate.create(openCrate, new TextureMapping()
						.put(TextureSlot.SIDE, getBlockTexture(openCrate))
						.put(TextureSlot.BOTTOM, getBlockTexture(openCrate, "_bottom")),
						this.modelOutput
				));
		remainingBlocks.remove(openCrate);

		var threeHighBottomTopTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/three_high_bottom_top")),
				Optional.empty(),
				TextureSlot.BOTTOM, TextureSlot.TOP, TextureSlot.SIDE);
		singleVariantBlockState(sparkChanger, threeHighBottomTopTemplate.create(sparkChanger,
				TextureMapping.cubeBottomTop(sparkChanger),
				this.modelOutput));
		remainingBlocks.remove(sparkChanger);

		singleVariantBlockState(starfield, fourHighBottomTopTemplate.create(starfield,
				TextureMapping.cubeBottomTop(starfield), this.modelOutput));
		remainingBlocks.remove(starfield);

		singleVariantBlockState(terraPlate, threeHighBottomTopTemplate.create(terraPlate,
				TextureMapping.cubeBottomTop(terraPlate), this.modelOutput));
		remainingBlocks.remove(terraPlate);

		var tenByTenAllTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/tenbyten_all")),
				Optional.empty(),
				TextureSlot.ALL);
		singleVariantBlockState(tinyPlanet, tenByTenAllTemplate.create(tinyPlanet,
				TextureMapping.cube(tinyPlanet), this.modelOutput));
		remainingBlocks.remove(tinyPlanet);

		singleVariantBlockState(turntable, ModelTemplates.CUBE_BOTTOM_TOP.create(turntable,
				TextureMapping.cubeBottomTop(turntable),
				this.modelOutput
		));
		remainingBlocks.remove(turntable);

		Material[] topTexs = new Material[6];
		Material[] sideTexs = new Material[6];
		Material[] topStrippedTexs = new Material[6];
		Material[] sideStrippedTexs = new Material[6];
		Material[] sideGlimmeringTexs = new Material[6];
		Material[] sideGlimmeringStrippedTexs = new Material[6];
		Identifier[] logModels = new Identifier[6];
		Identifier[] strippedLogModels = new Identifier[6];

		for (int i = 0; i < 6; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			sideTexs[i] = getBlockTexture(dreamwoodLog, suffix);
			topTexs[i] = getBlockTexture(dreamwoodLog, "_top");
			sideStrippedTexs[i] = getBlockTexture(dreamwoodLogStripped, suffix);
			topStrippedTexs[i] = getBlockTexture(dreamwoodLogStripped, "_top");
			sideGlimmeringTexs[i] = getBlockTexture(dreamwoodLogGlimmering, suffix);
			sideGlimmeringStrippedTexs[i] = getBlockTexture(dreamwoodLogStrippedGlimmering, suffix);
			logModels[i] = getModelLocation(dreamwood, suffix);
			strippedLogModels[i] = getModelLocation(dreamwoodStripped, suffix);
		}

		pillarWithVariants(remainingBlocks, dreamwoodLog, topTexs, sideTexs);
		pillarWithVariants(remainingBlocks, dreamwood, sideTexs, sideTexs);
		pillarWithVariants(remainingBlocks, dreamwoodLogStripped, topStrippedTexs, sideStrippedTexs);
		pillarWithVariants(remainingBlocks, dreamwoodStripped, sideStrippedTexs, sideStrippedTexs);
		pillarWithVariants(remainingBlocks, dreamwoodLogGlimmering, topTexs, sideGlimmeringTexs);
		pillarWithVariants(remainingBlocks, dreamwoodGlimmering, sideGlimmeringTexs, sideGlimmeringTexs);
		pillarWithVariants(remainingBlocks, dreamwoodLogStrippedGlimmering, topStrippedTexs, sideGlimmeringStrippedTexs);
		pillarWithVariants(remainingBlocks, dreamwoodStrippedGlimmering, sideGlimmeringStrippedTexs, sideGlimmeringStrippedTexs);

		stairsBlockWithVariants(remainingBlocks, dreamwoodStairs, sideTexs, sideTexs, sideTexs);
		stairsBlockWithVariants(remainingBlocks, dreamwoodStrippedStairs, sideStrippedTexs, sideStrippedTexs, sideStrippedTexs);
		slabBlockWithVariants(remainingBlocks, dreamwoodSlab, logModels, sideTexs, sideTexs, sideTexs);
		slabBlockWithVariants(remainingBlocks, dreamwoodStrippedSlab, strippedLogModels, sideStrippedTexs, sideStrippedTexs, sideStrippedTexs);
		wallBlockWithVariants(remainingBlocks, dreamwoodWall, sideTexs);
		wallBlockWithVariants(remainingBlocks, dreamwoodStrippedWall, sideStrippedTexs);

		pillar(remainingBlocks, livingwoodLog, getBlockTexture(livingwoodLog, "_top"), getBlockTexture(livingwoodLog));
		pillar(remainingBlocks, livingwood, getBlockTexture(livingwoodLog), getBlockTexture(livingwoodLog));
		pillar(remainingBlocks, livingwoodLogStripped, getBlockTexture(livingwoodLogStripped, "_top"), getBlockTexture(livingwoodLogStripped));
		pillar(remainingBlocks, livingwoodStripped, getBlockTexture(livingwoodLogStripped), getBlockTexture(livingwoodLogStripped));
		pillar(remainingBlocks, livingwoodLogGlimmering, getBlockTexture(livingwoodLog, "_top"), getBlockTexture(livingwoodLogGlimmering));
		pillar(remainingBlocks, livingwoodGlimmering, getBlockTexture(livingwoodLogGlimmering), getBlockTexture(livingwoodLogGlimmering));
		pillar(remainingBlocks, livingwoodLogStrippedGlimmering, getBlockTexture(livingwoodLogStripped, "_top"), getBlockTexture(livingwoodLogStrippedGlimmering));
		pillar(remainingBlocks, livingwoodStrippedGlimmering, getBlockTexture(livingwoodLogStrippedGlimmering), getBlockTexture(livingwoodLogStrippedGlimmering));

		pillarAlt(remainingBlocks, livingwoodFramed, getBlockTexture(livingwoodPatternFramed), getBlockTexture(livingwoodFramed));
		pillarAlt(remainingBlocks, dreamwoodFramed, getBlockTexture(dreamwoodPatternFramed), getBlockTexture(dreamwoodFramed));

		stairsBlock(remainingBlocks, livingwoodStairs, getBlockTexture(livingwoodLog), getBlockTexture(livingwoodLog), getBlockTexture(livingwoodLog));
		stairsBlock(remainingBlocks, livingwoodStrippedStairs, getBlockTexture(livingwoodLogStripped), getBlockTexture(livingwoodLogStripped), getBlockTexture(livingwoodLogStripped));
		slabBlock(remainingBlocks, livingwoodSlab, getModelLocation(livingwood), getBlockTexture(livingwoodLog), getBlockTexture(livingwoodLog), getBlockTexture(livingwoodLog));
		slabBlock(remainingBlocks, livingwoodStrippedSlab, getModelLocation(livingwoodStripped), getBlockTexture(livingwoodLogStripped), getBlockTexture(livingwoodLogStripped), getBlockTexture(livingwoodLogStripped));
		wallBlock(remainingBlocks, livingwoodWall, getBlockTexture(livingwoodLog));
		wallBlock(remainingBlocks, livingwoodStrippedWall, getBlockTexture(livingwoodLogStripped));

		fenceBlock(remainingBlocks, dreamwoodFence, getBlockTexture(dreamwoodPlanks));
		fenceGateBlock(remainingBlocks, dreamwoodFenceGate, getBlockTexture(dreamwoodPlanks));
		fenceBlock(remainingBlocks, livingwoodFence, getBlockTexture(livingwoodPlanks));
		fenceGateBlock(remainingBlocks, livingwoodFenceGate, getBlockTexture(livingwoodPlanks));

		rotatedMirrored(remainingBlocks, livingrock, getBlockTexture(livingrock));

		Material polishedLivingrockTexture = getBlockTexture(livingrockPolished);
		Material polishedLivingrockSlabSideTexture = getBlockTexture(livingrockPolishedSlab);
		Identifier polishedLivingrockSlabDoubleModel = ModelTemplates.CUBE_COLUMN.create(
				getModelLocation(livingrockPolishedSlab, "_double"),
				new TextureMapping()
						.put(TextureSlot.SIDE, polishedLivingrockSlabSideTexture)
						.put(TextureSlot.END, polishedLivingrockTexture),
				this.modelOutput
		);
		slabBlock(remainingBlocks, livingrockPolishedSlab, polishedLivingrockSlabDoubleModel, polishedLivingrockSlabSideTexture, polishedLivingrockTexture, polishedLivingrockTexture);

		Material conjurationTexture = getBlockTexture(conjurationCatalyst);
		Material conjurationMirrored = getBlockTexture(conjurationCatalyst, "_mirrored");
		checkeredBlockWithBlockstate(remainingBlocks, conjurationCatalyst, conjurationTexture, conjurationMirrored);

		// block entities with only particles
		particleOnly(remainingBlocks, animatedTorch, getBlockTexture(Blocks.REDSTONE_TORCH));
		particleOnly(remainingBlocks, avatar, getBlockTexture(livingwoodLog));
		particleOnly(remainingBlocks, bellows, getBlockTexture(livingwoodLog));
		particleOnly(remainingBlocks, brewery, getBlockTexture(livingrock));
		particleOnly(remainingBlocks, corporeaIndex, getBlockTexture(corporeaBlock));
		particleOnly(remainingBlocks, lightRelayDetector, getBlockTexture(lightRelayDetector));
		singleVariantBlockState(fakeAir, new ModelTemplate(Optional.empty(), Optional.empty()).create(fakeAir, new TextureMapping(), this.modelOutput));
		remainingBlocks.remove(fakeAir);
		particleOnly(remainingBlocks, lightRelayFork, getBlockTexture(lightRelayFork));
		particleOnly(remainingBlocks, gaiaHead, getBlockTexture(Blocks.SOUL_SAND));
		particleOnly(remainingBlocks, gaiaHeadWall, getBlockTexture(Blocks.SOUL_SAND));
		particleOnly(remainingBlocks, gaiaPylon, getBlockTexture(elementiumBlock));
		particleOnly(remainingBlocks, hourglass, getBlockTexture(manaGlass));
		particleOnly(remainingBlocks, lightRelayDefault, getBlockTexture(lightRelayDefault));
		particleOnly(remainingBlocks, manaFlame, new Material(Identifier.parse("block/fire_0")));
		particleOnly(remainingBlocks, manaPylon, getBlockTexture(manasteelBlock));
		particleOnly(remainingBlocks, naturaPylon, getBlockTexture(terrasteelBlock));
		particleOnly(remainingBlocks, teruTeruBozu, getBlockTexture(Blocks.WHITE_WOOL));
		particleOnly(remainingBlocks, lightRelayToggle, getBlockTexture(lightRelayToggle));

		// Block groups
		Predicate<Block> flowers = b -> XplatAbstractions.INSTANCE.isSpecialFlowerBlock(b)
				|| b instanceof BotaniaMushroomBlock
				|| b instanceof BotaniaFlowerBlock;
		ModelTemplate crossTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/cross")), Optional.empty(), TextureSlot.CROSS);
		takeAll(remainingBlocks, flowers).forEach(b -> singleVariantBlockState(b, crossTemplate.create(b, TextureMapping.cross(b), this.modelOutput))
		);

		takeAll(remainingBlocks, b -> b instanceof FlowerMotifBlock).forEach(b -> {
			String name = BuiltInRegistries.BLOCK.getKey(b).getPath().replace("_motif", "");
			singleVariantBlockState(b, crossTemplate.create(b, new TextureMapping()
					.put(TextureSlot.CROSS, new Material(prefix("block/" + name))),
					this.modelOutput));
		});

		takeAll(remainingBlocks, corporeaFunnel, corporeaInterceptor, corporeaRetainer).forEach(b -> {
			singleVariantBlockState(b, ModelTemplates.CUBE_COLUMN.create(b,
					TextureMapping.column(getBlockTexture(b, "_side"), getBlockTexture(b, "_end")),
					this.modelOutput));
		});

		var drumModelTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/drum")), Optional.empty(), TextureSlot.TOP, TextureSlot.SIDE);
		takeAll(remainingBlocks, gatheringDrum, canopyDrum, wildDrum).forEach(b -> {
			singleVariantBlockState(b, drumModelTemplate.create(b,
					new TextureMapping()
							.put(TextureSlot.TOP, new Material(prefix("block/drum_top")))
							.put(TextureSlot.SIDE, getBlockTexture(b)),
					this.modelOutput));
		});

		var outsideSlot = TextureSlotAccessor.make("outside");
		var coreSlot = TextureSlotAccessor.make("core");
		var spreaderTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/spreader")), Optional.empty(),
				TextureSlot.SIDE, TextureSlot.BACK, TextureSlot.INSIDE, outsideSlot);
		var spreaderCoreTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/spreader_core")), Optional.of("_core"),
				coreSlot);
		var spreaderPaddingTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/spreader_padding")),
				Optional.empty(), TextureSlot.FRONT, TextureSlot.BACK, TextureSlot.SIDE);
		var spreaderScaffoldingTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/spreader_scaffolding")),
				Optional.of("_scaffolding"), TextureSlot.TOP, TextureSlot.SIDE, TextureSlot.BOTTOM);
		takeAll(remainingBlocks, manaSpreader, redstoneSpreader, gaiaSpreader, elvenSpreader).forEach(b -> {
			Material outside;
			if (b == redstoneSpreader || b == manaSpreader) {
				outside = getBlockTexture(livingwoodLog);
			} else if (b == elvenSpreader) {
				outside = getBlockTexture(dreamwoodLog, "_3");
			} else {
				outside = getBlockTexture(b, "_outside");
			}
			Material inside;
			if (b == redstoneSpreader || b == manaSpreader) {
				inside = getBlockTexture(livingwoodLogStripped);
			} else if (b == elvenSpreader) {
				inside = getBlockTexture(dreamwoodLogStripped, "_3");
			} else {
				inside = getBlockTexture(b, "_inside");
			}
			singleVariantBlockState(b, spreaderTemplate.create(b, new TextureMapping()
					.put(TextureSlot.SIDE, getBlockTexture(b, "_side"))
					.put(TextureSlot.BACK, getBlockTexture(b, "_back"))
					.put(TextureSlot.INSIDE, inside)
					.put(outsideSlot, outside), this.modelOutput));
			spreaderCoreTemplate.create(b, new TextureMapping()
					.put(coreSlot, getBlockTexture(b, "_core")), this.modelOutput);
			if (b != redstoneSpreader) {
				spreaderScaffoldingTemplate.create(b, new TextureMapping()
						.put(TextureSlot.TOP, getBlockTexture(b, "_scaffolding_top"))
						.put(TextureSlot.SIDE, getBlockTexture(b, "_scaffolding_side"))
						.put(TextureSlot.BOTTOM, getBlockTexture(b, "_scaffolding_bottom")), this.modelOutput);
			}
		});
		ColorHelper.supportedColors().forEach(color -> {
			Block wool = ColorHelper.WOOL_MAP.apply(color);
			spreaderPaddingTemplate.create(prefix("block/" + color.getName() + "_spreader_padding"),
					new TextureMapping()
							.put(TextureSlot.FRONT, getBlockTexture(wool))
							.put(TextureSlot.BACK, getBlockTexture(wool))
							.put(TextureSlot.SIDE, getBlockTexture(wool)),
					this.modelOutput);
		});

		var manaSlot = TextureSlotAccessor.make("mana");
		TextureSlot[] manaPoolSlots = new TextureSlot[] { TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.INSIDE };
		TextureSlot[] manaPoolFullSlots = new TextureSlot[] { TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.INSIDE, manaSlot };
		var poolTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/mana_pool")), Optional.empty(), manaPoolSlots);
		var dilutedPoolTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/diluted_mana_pool")), Optional.empty(), manaPoolSlots);
		var creativePoolTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/creative_mana_pool")), Optional.empty(), manaPoolSlots);
		var poolFullTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/mana_pool_full")), Optional.of("_full"), manaPoolFullSlots);
		var dilutedPoolFullTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/diluted_mana_pool_full")), Optional.of("_full"), manaPoolFullSlots);
		var creativePoolFullTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/creative_mana_pool_full")), Optional.of("_full"), manaPoolFullSlots);
		takeAll(remainingBlocks, manaPool, dilutedPool, fabulousPool, creativePool).forEach(b -> {
			Block blockForTexture = b == fabulousPool ? manaPool : b;
			Material side = getBlockTexture(blockForTexture, "_side");
			Material top = getBlockTexture(blockForTexture, "_top");
			Material bottom = b == dilutedPool
					? getBlockTexture(manaPool, "_bottom") : getBlockTexture(blockForTexture, "_bottom");
			Material inside = getBlockTexture(blockForTexture, "_inside");
			ModelTemplate template = b == dilutedPool
					? dilutedPoolTemplate
					: b == creativePool
							? creativePoolTemplate
					: poolTemplate;
			ModelTemplate fullTemplate = b == dilutedPool
					? dilutedPoolFullTemplate
					: b == creativePool
							? creativePoolFullTemplate
					: poolFullTemplate;
			TextureMapping mapping = new TextureMapping()
					.put(TextureSlot.SIDE, side)
					.put(TextureSlot.TOP, top)
					.put(TextureSlot.BOTTOM, bottom)
					.put(TextureSlot.INSIDE, inside);

			singleVariantBlockState(b, template.create(b, mapping, this.modelOutput));
			fullTemplate.create(b, mapping.put(manaSlot, new Material(prefix("block/mana_water"))), this.modelOutput);
		});

		takeAll(remainingBlocks, pump, tinyPotato).forEach(b -> this.blockstates.add(MultiVariantGenerator.dispatch(b, multiVariant(new Variant(getModelLocation(b))))
				.with(ROTATION_HORIZONTAL_FACING))
		);

		takeAll(remainingBlocks, enderEye, manaDetector).forEach(b -> {
			var offModel = ModelTemplates.CUBE_ALL.create(b, TextureMapping.cube(b), this.modelOutput);
			var onModel = ModelTemplates.CUBE_ALL.create(getModelLocation(b, "_powered"),
					TextureMapping.cube(getBlockTexture(b, "_powered")), this.modelOutput);
			this.blockstates.add(MultiVariantGenerator.dispatch(b).with(
					PropertyDispatch.initial(BlockStateProperties.POWERED)
							.select(false, multiVariant(new Variant(offModel)))
							.select(true, multiVariant(new Variant(onModel)))
			));
		});

		var petalBlockModel = new ModelTemplate(Optional.of(prefix("block/shapes/cube_all_tinted")), Optional.empty(), TextureSlot.ALL)
				.create(prefix("block/petal_block"), new TextureMapping().put(TextureSlot.ALL, new Material(prefix("block/petal_block"))), this.modelOutput);
		takeAll(remainingBlocks, b -> b instanceof PetalBlock).forEach(b -> singleVariantBlockState(b, petalBlockModel));

		takeAll(remainingBlocks, b -> b instanceof BotaniaGrassBlock).forEach(b -> {
			var model = ModelTemplates.CUBE_BOTTOM_TOP.create(b, new TextureMapping()
					.put(TextureSlot.SIDE, getBlockTexture(b, "_side"))
					.put(TextureSlot.BOTTOM, getBlockTexture(Blocks.DIRT))
					.put(TextureSlot.TOP, getBlockTexture(b, "_top")),
					this.modelOutput
			);
			this.blockstates.add(MultiVariantGenerator.dispatch(b, BlockModelGenerators.createRotatedVariants(new Variant(model))));
		});

		takeAll(remainingBlocks, b -> b instanceof RedStringBlock).forEach(this::redStringBlock);

		takeAll(remainingBlocks, b -> b instanceof BotaniaDoubleFlowerBlock).forEach(b -> {
			var bottom = ModelTemplates.CROSS.create(b, TextureMapping.cross(b), this.modelOutput);
			var top = ModelTemplates.CROSS.create(getModelLocation(b, "_top"), TextureMapping.cross(getBlockTexture(b, "_top")), this.modelOutput);
			this.blockstates.add(
					MultiVariantGenerator.dispatch(b)
							.with(PropertyDispatch.initial(TallFlowerBlock.HALF)
									.select(DoubleBlockHalf.LOWER, multiVariant(new Variant(bottom)))
									.select(DoubleBlockHalf.UPPER, multiVariant(new Variant(top)))
							)
			);
		});

		var mountainTextures = new Material[] { getBlockTexture(biomeStoneMountain), getBlockTexture(biomeStoneMountain, "_1") };
		var mountainModels = new Identifier[] { getModelLocation(biomeStoneMountain), getModelLocation(biomeStoneMountain, "_1") };
		var mountainWeights = new Integer[] { 5, 1 };
		rotatedMirroredWithVariants(remainingBlocks, biomeStoneMountain, mountainTextures, mountainWeights);
		stairsBlockWithVariants(remainingBlocks, biomeStoneMountainStairs, mountainTextures, mountainTextures, mountainTextures, mountainWeights);
		slabBlockWithVariants(remainingBlocks, biomeStoneMountainSlab, mountainModels, mountainTextures, mountainTextures, mountainTextures, mountainWeights);
		wallBlockWithVariants(remainingBlocks, biomeStoneMountainWall, mountainTextures, mountainWeights);

		var mountainBrickTextures = new Material[] {
				getBlockTexture(biomeBrickMountain),
				getBlockTexture(biomeBrickMountain, "_1"),
				getBlockTexture(biomeBrickMountain, "_2"),
				getBlockTexture(biomeBrickMountain, "_3"),
				getBlockTexture(biomeBrickMountain, "_4"),
				getBlockTexture(biomeBrickMountain, "_5")
		};
		var mountainBrickModels = new Identifier[] {
				getModelLocation(biomeBrickMountain),
				getModelLocation(biomeBrickMountain, "_1"),
				getModelLocation(biomeBrickMountain, "_2"),
				getModelLocation(biomeBrickMountain, "_3"),
				getModelLocation(biomeBrickMountain, "_4"),
				getModelLocation(biomeBrickMountain, "_5"),
		};
		cubeAllWithVariants(remainingBlocks, biomeBrickMountain, mountainBrickTextures);
		stairsBlockWithVariants(remainingBlocks, biomeBrickMountainStairs, mountainBrickTextures, mountainBrickTextures, mountainBrickTextures);
		slabBlockWithVariants(remainingBlocks, biomeBrickMountainSlab, mountainBrickModels, mountainBrickTextures, mountainBrickTextures, mountainBrickTextures);
		wallBlockWithVariants(remainingBlocks, biomeBrickMountainWall, mountainBrickTextures);

		var taigaTextures = new Material[] { getBlockTexture(biomeStoneTaiga), getBlockTexture(biomeStoneTaiga, "_1") };
		var taigaModels = new Identifier[] { getModelLocation(biomeStoneTaiga), getModelLocation(biomeStoneTaiga, "_1") };
		rotatedMirroredWithVariants(remainingBlocks, biomeStoneTaiga, taigaTextures);
		stairsBlockWithVariants(remainingBlocks, biomeStoneTaigaStairs, taigaTextures, taigaTextures, taigaTextures);
		slabBlockWithVariants(remainingBlocks, biomeStoneTaigaSlab, taigaModels, taigaTextures, taigaTextures, taigaTextures);
		wallBlockWithVariants(remainingBlocks, biomeStoneTaigaWall, taigaTextures);

		Material plainsBrickSide = getBlockTexture(biomeBrickPlains);
		Material plainsBrickTop = getBlockTexture(biomeBrickPlains, "_top");
		pillarAlt(remainingBlocks, biomeBrickPlains, plainsBrickTop, plainsBrickSide);
		stairsBlock(remainingBlocks, biomeBrickPlainsStairs, plainsBrickSide, plainsBrickTop, plainsBrickTop);
		slabBlock(remainingBlocks, biomeBrickPlainsSlab, getModelLocation(biomeBrickPlains), plainsBrickSide, plainsBrickTop, plainsBrickTop);
		wallBlock(remainingBlocks, biomeBrickPlainsWall, plainsBrickSide, plainsBrickTop, plainsBrickTop);

		var forestBrickTextures = new Material[] { getBlockTexture(biomeBrickForest), getBlockTexture(biomeBrickForest, "_1") };
		var forestBrickModels = new Identifier[] { getModelLocation(biomeBrickForest), getModelLocation(biomeBrickForest, "_1") };
		var forestBrickWeights = new Integer[] { 2, 1 };
		cubeAllWithVariants(remainingBlocks, biomeBrickForest, forestBrickTextures, forestBrickWeights);
		stairsBlockWithVariants(remainingBlocks, biomeBrickForestStairs, forestBrickTextures, forestBrickTextures, forestBrickTextures, forestBrickWeights);
		slabBlockWithVariants(remainingBlocks, biomeBrickForestSlab, forestBrickModels, forestBrickTextures, forestBrickTextures, forestBrickTextures, forestBrickWeights);
		wallBlockWithVariants(remainingBlocks, biomeBrickForestWall, forestBrickTextures, forestBrickWeights);

		var fungalBrickTextures = new Material[] { getBlockTexture(biomeBrickFungal), getBlockTexture(biomeBrickFungal, "_1") };
		var fungalBrickModels = new Identifier[] { getModelLocation(biomeBrickFungal), getModelLocation(biomeBrickFungal, "_1") };
		cubeAllWithVariants(remainingBlocks, biomeBrickFungal, fungalBrickTextures);
		stairsBlockWithVariants(remainingBlocks, biomeBrickFungalStairs, fungalBrickTextures, fungalBrickTextures, fungalBrickTextures);
		slabBlockWithVariants(remainingBlocks, biomeBrickFungalSlab, fungalBrickModels, fungalBrickTextures, fungalBrickTextures, fungalBrickTextures);
		wallBlockWithVariants(remainingBlocks, biomeBrickFungalWall, fungalBrickTextures);

		var swampBrickTopTextures = new Material[] {
				getBlockTexture(biomeBrickSwamp, "_top"),
				getBlockTexture(biomeBrickSwamp, "_top_1")
		};
		var swampBrickBottomTextures = new Material[] {
				getBlockTexture(biomeBrickSwamp, "_bottom"),
				getBlockTexture(biomeBrickSwamp, "_bottom")
		};
		var swampBrickSideTextures = new Material[] {
				getBlockTexture(biomeBrickSwamp),
				getBlockTexture(biomeBrickSwamp)
		};
		var swampBrickModels = new Identifier[] {
				getModelLocation(biomeBrickSwamp),
				getModelLocation(biomeBrickSwamp, "_1")
		};
		directionalPillarWithVariants(remainingBlocks, biomeBrickSwamp, swampBrickTopTextures, swampBrickBottomTextures, swampBrickSideTextures);
		stairsBlockWithVariants(remainingBlocks, biomeBrickSwampStairs, swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);
		slabBlockWithVariants(remainingBlocks, biomeBrickSwampSlab, swampBrickModels, swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);
		wallBlockWithVariants(remainingBlocks, biomeBrickSwampWall, swampBrickSideTextures, swampBrickBottomTextures, swampBrickTopTextures);

		var swampChiseledBrickTopTextures = new Material[] {
				getBlockTexture(biomeChiseledBrickSwamp, "_top"),
				getBlockTexture(biomeChiseledBrickSwamp, "_top_1")
		};
		var swampChiseledBrickBottomTextures = new Material[] {
				getBlockTexture(biomeChiseledBrickSwamp, "_bottom"),
				getBlockTexture(biomeChiseledBrickSwamp, "_bottom")
		};
		var swampChiseledBrickSideTextures = new Material[] {
				getBlockTexture(biomeChiseledBrickSwamp),
				getBlockTexture(biomeChiseledBrickSwamp)
		};
		directionalPillarWithVariants(remainingBlocks, biomeChiseledBrickSwamp, swampChiseledBrickTopTextures, swampChiseledBrickBottomTextures, swampChiseledBrickSideTextures);

		var swampCobblestoneTextures = new Material[] { getBlockTexture(biomeCobblestoneSwamp), getBlockTexture(biomeCobblestoneSwamp, "_1") };
		var swampCobblestoneModels = new Identifier[] { getModelLocation(biomeCobblestoneSwamp), getModelLocation(biomeCobblestoneSwamp, "_1") };
		cubeAllWithVariants(remainingBlocks, biomeCobblestoneSwamp, swampCobblestoneTextures);
		stairsBlockWithVariants(remainingBlocks, biomeCobblestoneSwampStairs, swampCobblestoneTextures, swampCobblestoneTextures, swampCobblestoneTextures);
		slabBlockWithVariants(remainingBlocks, biomeCobblestoneSwampSlab, swampCobblestoneModels, swampCobblestoneTextures, swampCobblestoneTextures, swampCobblestoneTextures);
		wallBlockWithVariants(remainingBlocks, biomeCobblestoneSwampWall, swampCobblestoneTextures);

		Material mesaBrick = getBlockTexture(biomeBrickMesa);
		Material mesaBrickMirrored = getBlockTexture(biomeBrickMesa, "_mirrored");
		var mesaBrickModel = checkeredBlockWithBlockstate(remainingBlocks, biomeBrickMesa, mesaBrick, mesaBrickMirrored);
		checkeredSlabBlock(remainingBlocks, biomeBrickMesaSlab, mesaBrickModel, mesaBrick, mesaBrickMirrored);
		checkeredStairsBlock(remainingBlocks, biomeBrickMesaStairs, mesaBrick, mesaBrickMirrored);
		checkeredWallBlock(remainingBlocks, biomeBrickMesaWall, mesaBrick, mesaBrickMirrored);

		Material mesaChiseledBrickSide = getBlockTexture(biomeChiseledBrickMesa);
		Material mesaChiseledBrickTop = getBlockTexture(biomeChiseledBrickMesa, "_top");
		pillarAlt(remainingBlocks, biomeChiseledBrickMesa, mesaChiseledBrickTop, mesaChiseledBrickSide);

		// Remaining slabs, stairs, walls are handled automatically.
		for (Block stone : new Block[] { biomeStoneDesert, biomeStoneForest, biomeStoneFungal, biomeStoneMesa, biomeStonePlains, biomeStoneSwamp }) {
			rotatedMirrored(remainingBlocks, stone, getBlockTexture(stone));
		}

		for (String variant : new String[] { "dark", "mana", "blaze", "lavender", "red", "elf", "sunny" }) {
			Identifier quartzId = prefix(variant + "_quartz");
			Block quartz = BuiltInRegistries.BLOCK.getValue(quartzId);
			singleVariantBlockState(quartz,
					ModelTemplates.CUBE_BOTTOM_TOP.create(quartz, TextureMapping.cubeBottomTop(quartz), this.modelOutput));

			Identifier pillarId = prefix(variant + "_quartz_pillar");
			Block pillar = BuiltInRegistries.BLOCK.getValue(pillarId);
			var pillarModel = ModelTemplates.CUBE_COLUMN.create(pillar,
					TextureMapping.column(getBlockTexture(pillar, "_side"), getBlockTexture(pillar, "_end")),
					this.modelOutput);
			this.blockstates.add(BlockModelGenerators.createAxisAlignedPillarBlock(pillar, BlockModelGenerators.plainVariant(pillarModel)));

			Identifier chiseledId = prefix("chiseled_" + variant + "_quartz");
			Block chiseled = BuiltInRegistries.BLOCK.getValue(chiseledId);
			singleVariantBlockState(chiseled,
					ModelTemplates.CUBE_COLUMN.create(chiseled, new TextureMapping()
							.put(TextureSlot.SIDE, getBlockTexture(chiseled, "_side"))
							.put(TextureSlot.END, getBlockTexture(chiseled, "_end")), this.modelOutput));

			remainingBlocks.remove(quartz);
			remainingBlocks.remove(pillar);
			remainingBlocks.remove(chiseled);
		}

		takeAll(remainingBlocks, b -> b instanceof BuriedPetalBlock).forEach(b -> {
			DyeColor color = ((BuriedPetalBlock) b).color;
			Material wool = new Material(Identifier.parse("block/" + color.getSerializedName() + "_wool"));
			particleOnly(remainingBlocks, b, wool);
		});

		var apothecaryTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/petal_apothecary")), Optional.empty(),
				TextureSlot.SIDE, TextureSlot.TOP, TextureSlot.BOTTOM);
		takeAll(remainingBlocks, b -> b instanceof PetalApothecaryBlock).forEach(b -> singleVariantBlockState(b,
				apothecaryTemplate.create(b, new TextureMapping()
						.put(TextureSlot.SIDE, getBlockTexture(b, "_side"))
						.put(TextureSlot.TOP, getBlockTexture(b, "_top"))
						.put(TextureSlot.BOTTOM, getBlockTexture(b, "_bottom")), this.modelOutput))
		);

		takeAll(remainingBlocks, b -> b instanceof FloatingFlowerBlock || b instanceof FlowerPotBlock).forEach(b -> {
			// Models generated by FloatingFlowerModelProvider or PottedPlantModelProvider
			singleVariantBlockState(b, getModelLocation(b));
		});

		takeAll(remainingBlocks, b -> b instanceof BotaniaPaneBlock).forEach(b -> {
			String name = BuiltInRegistries.BLOCK.getKey(b).getPath();
			var mapping = new TextureMapping()
					.put(TextureSlot.EDGE, getBlockTexture(b))
					.put(TextureSlot.PANE, new Material(prefix("block/" + name.substring(0, name.length() - "_pane".length()))));
			Identifier postModel = ModelTemplates.STAINED_GLASS_PANE_POST.create(b, mapping, this.modelOutput);
			Identifier sideModel = ModelTemplates.STAINED_GLASS_PANE_SIDE.create(b, mapping, this.modelOutput);
			Identifier sideAltModel = ModelTemplates.STAINED_GLASS_PANE_SIDE_ALT.create(b, mapping, this.modelOutput);
			Identifier noSideModel = ModelTemplates.STAINED_GLASS_PANE_NOSIDE.create(b, mapping, this.modelOutput);
			Identifier noSideAltModel = ModelTemplates.STAINED_GLASS_PANE_NOSIDE_ALT.create(b, mapping, this.modelOutput);

			// [VanillaCopy] BlockModelGenerator glass panes
			this.blockstates.add(MultiPartGenerator.multiPart(b)
					.with(multiVariant(new Variant(postModel)))
					.with(new ConditionBuilder().term(BlockStateProperties.NORTH, true), multiVariant(new Variant(sideModel)))
					.with(new ConditionBuilder().term(BlockStateProperties.EAST, true), multiVariant(new Variant(sideModel).withYRot(Quadrant.R90)))
					.with(new ConditionBuilder().term(BlockStateProperties.SOUTH, true), multiVariant(new Variant(sideAltModel)))
					.with(new ConditionBuilder().term(BlockStateProperties.WEST, true), multiVariant(new Variant(sideAltModel).withYRot(Quadrant.R90)))
					.with(new ConditionBuilder().term(BlockStateProperties.NORTH, false), multiVariant(new Variant(noSideModel)))
					.with(new ConditionBuilder().term(BlockStateProperties.EAST, false), multiVariant(new Variant(noSideAltModel)))
					.with(new ConditionBuilder().term(BlockStateProperties.SOUTH, false), multiVariant(new Variant(noSideAltModel).withYRot(Quadrant.R90)))
					.with(new ConditionBuilder().term(BlockStateProperties.WEST, false), multiVariant(new Variant(noSideModel).withYRot(Quadrant.R270))));
		});

		takeAll(remainingBlocks, b -> b instanceof StairBlock).forEach(b -> {
			String name = BuiltInRegistries.BLOCK.getKey(b).getPath();
			String baseName = name.substring(0, name.length() - LibBlockNames.STAIR_SUFFIX.length());
			boolean quartz = name.contains("quartz");
			if (quartz) {
				Material side = new Material(prefix("block/" + baseName + "_side"));
				Material bottom = new Material(prefix("block/" + baseName + "_bottom"));
				Material top = new Material(prefix("block/" + baseName + "_top"));
				stairsBlock(new HashSet<>(), b, side, bottom, top);
			} else {
				var tex = new Material(prefix("block/" + baseName));
				stairsBlock(new HashSet<>(), b, tex, tex, tex);
			}
		});

		takeAll(remainingBlocks, b -> b instanceof SlabBlock).forEach(slabBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(slabBlock).getPath();
			String baseName = name.substring(0, name.length() - LibBlockNames.SLAB_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.getValue(prefix(baseName));
			boolean quartz = name.contains("quartz");
			if (quartz) {
				var side = getBlockTexture(base, "_side");
				var bottom = getBlockTexture(base, "_bottom");
				var top = getBlockTexture(base, "_top");
				var doubleModel = getModelLocation(base);
				slabBlock(new HashSet<>(), slabBlock, doubleModel, side, bottom, top);
			} else {
				var baseTex = getBlockTexture(base);
				var doubleModel = getModelLocation(base);
				slabBlock(new HashSet<>(), slabBlock, doubleModel, baseTex, baseTex, baseTex);
			}
		});

		takeAll(remainingBlocks, b -> b instanceof WallBlock).forEach(wallBlock -> {
			String name = BuiltInRegistries.BLOCK.getKey(wallBlock).getPath();
			String baseName = name.substring(0, name.length() - LibBlockNames.WALL_SUFFIX.length());
			Block base = BuiltInRegistries.BLOCK.getValue(prefix(baseName));
			var baseTexture = getBlockTexture(base);
			wallBlock(new HashSet<>(), wallBlock, baseTexture);
		});

		remainingBlocks.forEach(this::cubeAllNoRemove);
	}

	protected void particleOnly(Set<Block> blocks, Block b, Material particle) {
		singleVariantBlockState(b, ModelTemplates.PARTICLE_ONLY.create(b, TextureMapping.particle(particle), this.modelOutput));
		blocks.remove(b);
	}

	protected void manualModel(Set<Block> blocks, Block b) {
		singleVariantBlockState(b, getModelLocation(b));
		blocks.remove(b);
	}

	protected void stairsBlock(Set<Block> blocks, Block block, Material sideTex, Material bottomTex, Material topTex) {
		stairsBlockWithVariants(blocks, block, new Material[] { sideTex }, new Material[] { bottomTex }, new Material[] { topTex });
	}

	protected void checkeredStairsBlock(Set<Block> blocks, Block block, Material texture, Material mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate = (model, suffix) -> new ModelTemplate(Optional.of(prefix("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, texture).put(TextureSlot.NORTH, mirroredTexture);

		var checkeredStairsModel = checkeredTemplate.apply("stairs_checkered", Optional.empty()).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);
		var checkeredStairsModelRot90 = checkeredTemplate.apply("stairs_checkered_90deg", Optional.of("_90deg")).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);
		var checkeredStairsOuterModel = checkeredTemplate.apply("stairs_outer_checkered", Optional.of("_outer")).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);
		var checkeredStairsOuterModelRot90 = checkeredTemplate.apply("stairs_outer_checkered_90deg", Optional.of("_outer_90deg")).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);
		var checkeredStairsInnerModel = checkeredTemplate.apply("stairs_inner_checkered", Optional.of("_inner")).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);
		var checkeredStairsInnerModelRot90 = checkeredTemplate.apply("stairs_inner_checkered_90deg", Optional.of("_inner_90deg")).create(biomeBrickMesaStairs, checkeredMapping, this.modelOutput);

		stairsBlockWithModels(blocks, block, checkeredStairsInnerModel, checkeredStairsInnerModelRot90, checkeredStairsModel, checkeredStairsModelRot90, checkeredStairsOuterModel, checkeredStairsOuterModelRot90);
	}

	protected void stairsBlockWithVariants(Set<Block> blocks, Block block, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		stairsBlockWithVariants(blocks, block, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void stairsBlockWithVariants(Set<Block> blocks, Block block, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures, Integer[] weights) {
		int length = sideTextures.length;
		if (length != topTextures.length || length != bottomTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] innerModels = new Identifier[length];
		Identifier[] straightModels = new Identifier[length];
		Identifier[] outerModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.SIDE, sideTextures[i])
					.put(TextureSlot.BOTTOM, bottomTextures[i])
					.put(TextureSlot.TOP, topTextures[i]);
			Identifier modelIdInner = getModelLocation(block, "_inner" + suffix);
			Identifier modelIdStraight = getModelLocation(block, suffix);
			Identifier modelIdOuter = getModelLocation(block, "_outer" + suffix);
			innerModels[i] = ModelTemplates.STAIRS_INNER.create(modelIdInner, mapping, this.modelOutput);
			straightModels[i] = ModelTemplates.STAIRS_STRAIGHT.create(modelIdStraight, mapping, this.modelOutput);
			outerModels[i] = ModelTemplates.STAIRS_OUTER.create(modelIdOuter, mapping, this.modelOutput);
		}
		stairsBlockWithModels(blocks, block, innerModels, straightModels, outerModels, weights);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels, Identifier[] straightModels, Identifier[] outerModels, Integer[] weights) {
		stairsBlockWithModels(blocks, block, innerModels, straightModels, outerModels, weights, true);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier innerModel, Identifier innerModelRot90, Identifier straightModel, Identifier straightModelRot90, Identifier outerModel, Identifier outerModelRot90) {
		stairsBlockWithModels(blocks, block, new Identifier[] { innerModel }, new Identifier[] { innerModelRot90 }, new Identifier[] { straightModel }, new Identifier[] { straightModelRot90 }, new Identifier[] { outerModel }, new Identifier[] { outerModelRot90 }, new Integer[] { 1 }, true);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels, Identifier[] straightModels, Identifier[] outerModels, Integer[] weights, Boolean uvlock) {
		stairsBlockWithModels(blocks, block, innerModels, innerModels, straightModels, straightModels, outerModels, outerModels, weights, uvlock);
	}

	protected void stairsBlockWithModels(Set<Block> blocks, Block block, Identifier[] innerModels, Identifier[] innerModelsRot90, Identifier[] straightModels, Identifier[] straightModelsRot90, Identifier[] outerModels, Identifier[] outerModelsRot90, Integer[] weights, Boolean uvlock) {
		int length = innerModels.length;
		if (length != straightModels.length || length != outerModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var propertyDispatch = PropertyDispatch.initial(
				BlockStateProperties.HORIZONTAL_FACING,
				BlockStateProperties.HALF,
				BlockStateProperties.STAIRS_SHAPE
		);
		for (Direction direction : Direction.Plane.HORIZONTAL) {
			for (Half half : Half.values()) {
				for (StairsShape stairsShape : StairsShape.values()) {
					// Stair blockstates are super weird. If it's left and bottom, you need to rotate it 90deg ccw compared to
					// usual, and if it's right and top, you need to rotate it 90deg cw. This is the cleanest way I could think
					// of to do that.

					// Additionally, we're using an alternate model for everything that's rotated 90/270 degrees to be able to
					// have a consistent "checkered" texture for some stairs. Normal stairs just provide the same model twice
					// which makes no impact in the generated blockstate.
					boolean isLeft = stairsShape == StairsShape.INNER_LEFT || stairsShape == StairsShape.OUTER_LEFT;
					boolean isRight = stairsShape == StairsShape.INNER_RIGHT || stairsShape == StairsShape.OUTER_RIGHT;
					int rotationOffset = isLeft && half == Half.BOTTOM ? -1 : isRight && half == Half.TOP ? 1 : 0;

					Quadrant[] rotations = Quadrant.values();
					Quadrant yRot = switch (direction) {
						case EAST -> rotations[(4 + rotationOffset) % 4];
						case WEST -> rotations[(2 + rotationOffset) % 4];
						case SOUTH -> rotations[(1 + rotationOffset) % 4];
						case NORTH -> rotations[(3 + rotationOffset) % 4];
						default -> throw new IllegalStateException();
					};
					Quadrant xRot = switch (half) {
						case BOTTOM -> Quadrant.R0;
						case TOP -> Quadrant.R180;
					};
					boolean rotatedModel = yRot == Quadrant.R90 || yRot == Quadrant.R270;
					Identifier[] models = switch (stairsShape) {
						case STRAIGHT -> rotatedModel ? straightModelsRot90 : straightModels;
						case OUTER_RIGHT, OUTER_LEFT -> rotatedModel ? outerModelsRot90 : outerModels;
						case INNER_RIGHT, INNER_LEFT -> rotatedModel ? innerModelsRot90 : innerModels;
					};
					var indices = IntStream.range(0, length).boxed();
					propertyDispatch.select(direction, half, stairsShape, multiVariant(indices.map(i -> weighted(weights[i], maybeUVLock(uvlock, maybeYRot(yRot, maybeXRot(xRot, new Variant(models[i])
					))))
					).toList()));
				}
			}
		}
		this.blockstates.add(MultiVariantGenerator.dispatch(block).with(propertyDispatch));
		blocks.remove(block);
	}

	protected void slabBlock(Set<Block> blocks, Block block, Identifier doubleModel, Material side, Material bottom, Material top) {
		slabBlockWithVariants(blocks, block, new Identifier[] { doubleModel }, new Material[] { side }, new Material[] { bottom }, new Material[] { top });
	}

	protected void checkeredSlabBlock(Set<Block> blocks, Block block, Identifier doubleModel, Material texture, Material mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate = (model, suffix) -> new ModelTemplate(Optional.of(prefix("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, texture).put(TextureSlot.NORTH, mirroredTexture);

		var slabModel = checkeredTemplate.apply("slab_checkered", Optional.empty()).create(biomeBrickMesaSlab, checkeredMapping, this.modelOutput);
		var slabTopModel = checkeredTemplate.apply("slab_top_checkered", Optional.of("_top")).create(biomeBrickMesaSlab, checkeredMapping, this.modelOutput);
		slabBlockWithModels(blocks, block, slabModel, slabTopModel, doubleModel);
	}

	protected void slabBlockWithVariants(Set<Block> blocks, Block block, Identifier[] doubleModels, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		slabBlockWithVariants(blocks, block, doubleModels, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void slabBlockWithVariants(Set<Block> blocks, Block block, Identifier[] doubleModels, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures, Integer[] weights) {
		int length = sideTextures.length;
		if (length != topTextures.length || length != bottomTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] bottomModels = new Identifier[length];
		Identifier[] topModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.SIDE, sideTextures[i])
					.put(TextureSlot.BOTTOM, bottomTextures[i])
					.put(TextureSlot.TOP, topTextures[i]);
			Identifier modelIdBottom = getModelLocation(block, suffix);
			Identifier modelIdTop = getModelLocation(block, "_top" + suffix);
			bottomModels[i] = ModelTemplates.SLAB_BOTTOM.create(modelIdBottom, mapping, this.modelOutput);
			topModels[i] = ModelTemplates.SLAB_TOP.create(modelIdTop, mapping, this.modelOutput);
		}
		slabBlockWithModels(blocks, block, bottomModels, topModels, doubleModels, weights);
	}

	protected void slabBlockWithModels(Set<Block> blocks, Block block, Identifier bottomModel, Identifier topModel, Identifier doubleModel) {
		slabBlockWithModels(blocks, block, new Identifier[] { bottomModel }, new Identifier[] { topModel }, new Identifier[] { doubleModel }, new Integer[] { 1 });
	}

	protected void slabBlockWithModels(Set<Block> blocks, Block block, Identifier[] bottomModels, Identifier[] topModels, Identifier[] doubleModels, Integer[] weights) {
		int length = doubleModels.length;
		if (length != topModels.length || length != bottomModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesBottom = IntStream.range(0, length).boxed();
		var indicesTop = IntStream.range(0, length).boxed();
		var indicesDouble = IntStream.range(0, length).boxed();
		this.blockstates.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.SLAB_TYPE)
						.select(SlabType.BOTTOM, multiVariant(indicesBottom.map(i -> weighted(weights[i], new Variant(bottomModels[i]))).toList()))
						.select(SlabType.TOP, multiVariant(indicesTop.map(i -> weighted(weights[i], new Variant(topModels[i]))).toList()))
						.select(SlabType.DOUBLE, multiVariant(indicesDouble.map(i -> weighted(weights[i], new Variant(doubleModels[i]))).toList()))
		));
		blocks.remove(block);
	}

	protected void wallBlock(Set<Block> blocks, Block block, Material texture) {
		wallBlock(blocks, block, texture, texture, texture);
	}

	protected void wallBlock(Set<Block> blocks, Block block, Material sideTexture, Material bottomTexture, Material topTexture) {
		wallBlockWithVariants(blocks, block, new Material[] { sideTexture }, new Material[] { bottomTexture }, new Material[] { topTexture });
	}

	protected void checkeredWallBlock(Set<Block> blocks, Block block, Material texture, Material mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate = (model, suffix) -> new ModelTemplate(Optional.of(prefix("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, texture).put(TextureSlot.NORTH, mirroredTexture);

		var checkeredWallPostModel = checkeredTemplate.apply("wall_post_checkered", Optional.of("_post")).create(biomeBrickMesaWall, checkeredMapping, this.modelOutput);
		var checkeredWallSideModel = checkeredTemplate.apply("wall_side_checkered", Optional.of("_side")).create(biomeBrickMesaWall, checkeredMapping, this.modelOutput);
		var checkeredWallSideModelRot90 = checkeredTemplate.apply("wall_side_checkered_90deg", Optional.of("_side_90deg")).create(biomeBrickMesaWall, checkeredMapping, this.modelOutput);
		var checkeredWallSideTallModel = checkeredTemplate.apply("wall_side_tall_checkered", Optional.of("_side_tall")).create(biomeBrickMesaWall, checkeredMapping, this.modelOutput);
		var checkeredWallSideTallModelRot90 = checkeredTemplate.apply("wall_side_tall_checkered_90deg", Optional.of("_side_tall_90deg")).create(biomeBrickMesaWall, checkeredMapping, this.modelOutput);

		wallBlockWithModels(blocks, block, checkeredWallPostModel, checkeredWallSideModel, checkeredWallSideModelRot90, checkeredWallSideTallModel, checkeredWallSideTallModelRot90);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Material[] textures) {
		wallBlockWithVariants(blocks, block, textures, textures, textures);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Material[] textures, Integer[] weights) {
		wallBlockWithVariants(blocks, block, textures, textures, textures, weights);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures) {
		var weights = new Integer[sideTextures.length];
		Arrays.fill(weights, 1);
		wallBlockWithVariants(blocks, block, sideTextures, bottomTextures, topTextures, weights);
	}

	protected void wallBlockWithVariants(Set<Block> blocks, Block block, Material[] sideTextures, Material[] bottomTextures, Material[] topTextures, Integer[] weights) {
		int length = sideTextures.length;
		if (length != bottomTextures.length && length != topTextures.length && length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] postModels = new Identifier[length];
		Identifier[] lowModels = new Identifier[length];
		Identifier[] tallModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			var mapping = new TextureMapping()
					.put(TextureSlot.WALL, sideTextures[i])
					.put(TextureSlot.BOTTOM, bottomTextures[i])
					.put(TextureSlot.TOP, topTextures[i]);
			Identifier modelIdPost = getModelLocation(block, "_post" + suffix);
			Identifier modelIdLow = getModelLocation(block, "_side" + suffix);
			Identifier modelIdTall = getModelLocation(block, "_side_tall" + suffix);
			var postTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/wall_post")), Optional.of("_post"),
					TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			var sideTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/wall_side")), Optional.of("_side"),
					TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			var sideTallTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/wall_side_tall")), Optional.of("_side_tall"),
					TextureSlot.WALL, TextureSlot.BOTTOM, TextureSlot.TOP);
			postModels[i] = postTemplate.create(modelIdPost, mapping, this.modelOutput);
			lowModels[i] = sideTemplate.create(modelIdLow, mapping, this.modelOutput);
			tallModels[i] = sideTallTemplate.create(modelIdTall, mapping, this.modelOutput);
		}
		wallBlockWithModels(blocks, block, postModels, lowModels, tallModels, weights);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels, Identifier[] lowModels, Identifier[] tallModels, Integer[] weights) {
		wallBlockWithModels(blocks, block, postModels, lowModels, tallModels, weights, true);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier postModel, Identifier lowModel, Identifier lowModelRot90, Identifier tallModel, Identifier tallodelRot90) {
		wallBlockWithModels(blocks, block, new Identifier[] { postModel }, new Identifier[] { lowModel }, new Identifier[] { lowModelRot90 }, new Identifier[] { tallModel }, new Identifier[] { tallodelRot90 }, new Integer[] { 1 }, true);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels, Identifier[] lowModels, Identifier[] tallModels, Integer[] weights, Boolean uvlock) {
		wallBlockWithModels(blocks, block, postModels, lowModels, lowModels, tallModels, tallModels, weights, uvlock);
	}

	protected void wallBlockWithModels(Set<Block> blocks, Block block, Identifier[] postModels, Identifier[] lowModels, Identifier[] lowModelsRot90, Identifier[] tallModels, Identifier[] tallodelsRot90, Integer[] weights, Boolean uvlock) {
		int length = postModels.length;
		if (length != lowModels.length || length != tallModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var multiPartGenerator = MultiPartGenerator.multiPart(block);
		var indicesPost = IntStream.range(0, length).boxed();
		multiPartGenerator.with(new ConditionBuilder().term(BlockStateProperties.UP, true), multiVariant(indicesPost.map(i -> weighted(weights[i], new Variant(postModels[i]))).toList()));
		var wallSides = List.of(BlockStateProperties.EAST_WALL, BlockStateProperties.WEST_WALL, BlockStateProperties.SOUTH_WALL, BlockStateProperties.NORTH_WALL);

		// We're using an alternate model for everything that's rotated 90/270 degrees to be able to
		// have a consistent "checkered" texture for some walls. Normal walls just provide the same model twice
		// which makes no impact in the generated blockstate.
		for (EnumProperty<WallSide> wallSide : wallSides) {
			Quadrant yRot =
					wallSide == BlockStateProperties.EAST_WALL ? Quadrant.R90
							: wallSide == BlockStateProperties.WEST_WALL ? Quadrant.R270
							: wallSide == BlockStateProperties.SOUTH_WALL ? Quadrant.R180
							: Quadrant.R0;
			boolean rotatedModel = yRot == Quadrant.R90 || yRot == Quadrant.R270;
			var indicesLow = IntStream.range(0, length).boxed();
			var indicesTall = IntStream.range(0, length).boxed();
			multiPartGenerator
					.with(new ConditionBuilder().term(wallSide, WallSide.LOW), multiVariant(indicesLow.map(i -> weighted(weights[i], maybeUVLock(uvlock, maybeYRot(yRot,
							new Variant(rotatedModel ? lowModelsRot90[i] : lowModels[i])
					)))).toList()))
					.with(new ConditionBuilder().term(wallSide, WallSide.TALL), multiVariant(indicesTall.map(i -> weighted(weights[i], maybeUVLock(uvlock, maybeYRot(yRot,
							new Variant(rotatedModel ? tallodelsRot90[i] : tallModels[i])
					)))).toList()));
		}
		this.blockstates.add(multiPartGenerator);
		blocks.remove(block);
	}

	protected void fenceBlock(Set<Block> blocks, Block block, Material tex) {
		var mapping = TextureMapping.defaultTexture(tex);
		var postModel = ModelTemplates.FENCE_POST.create(block, mapping, this.modelOutput);
		var sideModel = ModelTemplates.FENCE_SIDE.create(block, mapping, this.modelOutput);
		this.blockstates.add(BlockModelGenerators.createFence(block, BlockModelGenerators.plainVariant(postModel), BlockModelGenerators.plainVariant(sideModel)));
		blocks.remove(block);
	}

	protected void fenceGateBlock(Set<Block> blocks, Block block, Material tex) {
		var mapping = TextureMapping.defaultTexture(tex);
		var openModel = ModelTemplates.FENCE_GATE_OPEN.create(block, mapping, this.modelOutput);
		var closedModel = ModelTemplates.FENCE_GATE_CLOSED.create(block, mapping, this.modelOutput);
		var openWallModel = ModelTemplates.FENCE_GATE_WALL_OPEN.create(block, mapping, this.modelOutput);
		var closedWallModel = ModelTemplates.FENCE_GATE_WALL_CLOSED.create(block, mapping, this.modelOutput);
		this.blockstates.add(BlockModelGenerators.createFenceGate(block, BlockModelGenerators.plainVariant(openModel), BlockModelGenerators.plainVariant(closedModel), BlockModelGenerators.plainVariant(openWallModel), BlockModelGenerators.plainVariant(closedWallModel), false));
		blocks.remove(block);
	}

	protected void cubeAllNoRemove(Block block) {
		cubeAll(new HashSet<>(), block);
	}

	protected void cubeAll(Set<Block> blocks, Block block) {
		Material texture = getBlockTexture(block);
		cubeAllWithVariants(blocks, block, new Material[] { texture });
	}

	protected Identifier checkeredBlockWithBlockstate(Set<Block> blocks, Block block, Material texture, Material mirroredTexture) {
		BiFunction<String, Optional<String>, ModelTemplate> checkeredTemplate = (model, suffix) -> new ModelTemplate(Optional.of(prefix("block/shapes/" + model)), suffix, TextureSlot.SIDE, TextureSlot.NORTH);
		TextureMapping checkeredMapping = new TextureMapping().put(TextureSlot.SIDE, texture).put(TextureSlot.NORTH, mirroredTexture);

		var blockModel = checkeredTemplate.apply("cube_checkered", Optional.empty()).create(block, checkeredMapping, this.modelOutput);
		cubeAllWithModels(blocks, block, new Identifier[] { blockModel }, new Integer[] { 1 });
		return blockModel;
	}

	protected void cubeAllWithVariants(Set<Block> blocks, Block block, Material[] textures) {
		var weights = new Integer[textures.length];
		Arrays.fill(weights, 1);
		cubeAllWithVariants(blocks, block, textures, weights);
	}

	protected void cubeAllWithVariants(Set<Block> blocks, Block block, Material[] textures, Integer[] weights) {
		int length = textures.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] models = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelId = getModelLocation(block, suffix);
			models[i] = ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(textures[i]), this.modelOutput);
		}
		cubeAllWithModels(blocks, block, models, weights);
	}

	protected void cubeAllWithModels(Set<Block> blocks, Block block, Identifier[] models, Integer[] weights) {
		int length = models.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indices = IntStream.range(0, length).boxed();
		this.blockstates.add(MultiVariantGenerator.dispatch(block, multiVariant(indices.map(i -> weighted(weights[i], new Variant(models[i]))
		).toList())));
		blocks.remove(block);
	}

	protected void singleVariantBlockState(Block b, Identifier model) {
		this.blockstates.add(MultiVariantGenerator.dispatch(b, multiVariant(new Variant(model))));
	}

	protected void rotatedMirrored(Set<Block> blocks, Block block, Material texture) {
		rotatedMirroredWithVariants(blocks, block, new Material[] { texture });
	}

	protected void rotatedMirroredWithVariants(Set<Block> blocks, Block block, Material[] textures) {
		var weights = new Integer[textures.length];
		Arrays.fill(weights, 1);
		rotatedMirroredWithVariants(blocks, block, textures, weights);
	}

	protected void rotatedMirroredWithVariants(Set<Block> blocks, Block block, Material[] textures, Integer[] weights) {
		int length = textures.length;
		if (length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] models = new Identifier[length];
		Identifier[] mirroredModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelId = getModelLocation(block, suffix);
			Identifier mirriredModelId = getModelLocation(block, "_mirrored" + suffix);
			models[i] = ModelTemplates.CUBE_ALL.create(modelId, TextureMapping.cube(textures[i]), this.modelOutput);
			mirroredModels[i] = ModelTemplates.CUBE_MIRRORED_ALL.create(mirriredModelId, TextureMapping.cube(textures[i]), this.modelOutput);
		}
		rotatedMirroredWithModels(blocks, block, models, mirroredModels, weights);
	}

	protected void rotatedMirroredWithModels(Set<Block> blocks, Block block, Identifier[] models, Identifier[] mirroredModels, Integer[] weights) {
		int length = models.length;
		if (length != mirroredModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indices = IntStream.range(0, length).boxed();
		this.blockstates.add(MultiVariantGenerator.dispatch(block, multiVariant(indices.flatMap(i -> Stream.of(
				weighted(weights[i], new Variant(models[i])),
				weighted(weights[i], new Variant(mirroredModels[i])),
				weighted(weights[i], new Variant(models[i]).withYRot(Quadrant.R180)),
				weighted(weights[i], new Variant(mirroredModels[i]).withYRot(Quadrant.R180))
		)).toList())));
		blocks.remove(block);
	}

	protected void pillar(Set<Block> blocks, Block block, Material top, Material side) {
		pillarWithVariants(blocks, block, new Material[] { top }, new Material[] { side });
	}

	protected void pillarWithVariants(Set<Block> blocks, Block block, Material[] topTextures, Material[] sideTextures) {
		var weights = new Integer[topTextures.length];
		Arrays.fill(weights, 1);
		pillarWithVariants(blocks, block, topTextures, sideTextures, weights);
	}

	protected void pillarWithVariants(Set<Block> blocks, Block block, Material[] topTextures, Material[] sideTextures, Integer[] weights) {
		int length = topTextures.length;
		if (length != sideTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalModels = new Identifier[length];
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontal = getModelLocation(block, "_horizontal" + suffix);
			topModels[i] = ModelTemplates.CUBE_COLUMN.create(modelIdTop, TextureMapping.column(sideTextures[i], topTextures[i]), this.modelOutput);
			horizontalModels[i] = ModelTemplates.CUBE_COLUMN_HORIZONTAL.create(modelIdHorizontal, TextureMapping.column(sideTextures[i], topTextures[i]), this.modelOutput);
		}
		pillarWithModels(blocks, block, topModels, horizontalModels, weights);
	}

	protected void pillarWithModels(Set<Block> blocks, Block block, Identifier[] topModels, Identifier[] horizontalModels, Integer[] weights) {
		int length = topModels.length;
		if (length != horizontalModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesX = IntStream.range(0, length).boxed();
		var indicesY = IntStream.range(0, length).boxed();
		var indicesZ = IntStream.range(0, length).boxed();
		this.blockstates.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
						.select(Direction.Axis.Y, multiVariant(indicesX.map(i -> weighted(weights[i], new Variant(topModels[i]))).toList()))
						.select(Direction.Axis.Z, multiVariant(indicesY.map(i -> weighted(weights[i], new Variant(horizontalModels[i]).withXRot(Quadrant.R90))).toList()))
						.select(Direction.Axis.X, multiVariant(indicesZ.map(i -> weighted(weights[i], new Variant(horizontalModels[i]).withXRot(Quadrant.R90).withYRot(Quadrant.R90))).toList()))
		));
		blocks.remove(block);
	}

	// Alternative pillar model that rotates and mirrors some additional faces
	protected void pillarAlt(Set<Block> blocks, Block block, Material top, Material side) {
		pillarAltWithVariants(blocks, block, new Material[] { top }, new Material[] { side });
	}

	protected void pillarAltWithVariants(Set<Block> blocks, Block block, Material[] topTextures, Material[] sideTextures) {
		int length = topTextures.length;
		if (length != sideTextures.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalXModels = new Identifier[length];
		Identifier[] horizontalZModels = new Identifier[length];
		ModelTemplate horizontalXTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/cube_column_horizontal_x")), Optional.of("_horizontal_x"), TextureSlot.END, TextureSlot.SIDE);
		ModelTemplate horizontalZTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/cube_column_horizontal_z")), Optional.of("_horizontal_z"), TextureSlot.END, TextureSlot.SIDE);
		for (int i = 0; i < length; i++) {
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontalX = getModelLocation(block, "_horizontal_x" + suffix);
			Identifier modelIdHorizontalZ = getModelLocation(block, "_horizontal_z" + suffix);
			topModels[i] = ModelTemplates.CUBE_COLUMN.create(modelIdTop, TextureMapping.column(sideTextures[i], topTextures[i]), this.modelOutput);
			horizontalXModels[i] = horizontalXTemplate.create(modelIdHorizontalX, TextureMapping.column(sideTextures[i], topTextures[i]), this.modelOutput);
			horizontalZModels[i] = horizontalZTemplate.create(modelIdHorizontalZ, TextureMapping.column(sideTextures[i], topTextures[i]), this.modelOutput);
		}
		pillarAltWithModels(blocks, block, topModels, horizontalXModels, horizontalZModels);
	}

	protected void pillarAltWithModels(Set<Block> blocks, Block block, Identifier[] yModels, Identifier[] xModels, Identifier[] zModels) {
		this.blockstates.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.AXIS)
						.select(Direction.Axis.Y, multiVariant(Stream.of(yModels).map(rl -> weighted(1, new Variant(rl))).toList()))
						.select(Direction.Axis.X, multiVariant(Stream.of(xModels).map(rl -> weighted(1, new Variant(rl))).toList()))
						.select(Direction.Axis.Z, multiVariant(Stream.of(zModels).map(rl -> weighted(1, new Variant(rl))).toList()))
		));
		blocks.remove(block);
	}

	protected void directionalPillar(Set<Block> blocks, Block block, Material top, Material bottom, Material side) {
		directionalPillarWithVariants(blocks, block, new Material[] { top }, new Material[] { top }, new Material[] { side });
	}

	protected void directionalPillarWithVariants(Set<Block> blocks, Block block, Material[] topTextures, Material[] bottomTextures, Material[] sideTextures) {
		var weights = new Integer[topTextures.length];
		Arrays.fill(weights, 1);
		directionalPillarWithVariants(blocks, block, topTextures, bottomTextures, sideTextures, weights);
	}

	protected void directionalPillarWithVariants(Set<Block> blocks, Block block, Material[] topTextures, Material[] bottomTextures, Material[] sideTextures, Integer[] weights) {
		int length = topTextures.length;
		if (length != bottomTextures.length || length != sideTextures.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		Identifier[] topModels = new Identifier[length];
		Identifier[] horizontalModels = new Identifier[length];
		ModelTemplate topTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/cube_column_directional")), Optional.empty(), TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
		ModelTemplate horizontalTemplate = new ModelTemplate(Optional.of(prefix("block/shapes/cube_column_directional_horizontal")), Optional.of("_horizontal"), TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.SIDE);
		for (int i = 0; i < length; i++) {
			TextureMapping mapping = new TextureMapping()
					.put(TextureSlot.SIDE, sideTextures[i])
					.put(TextureSlot.TOP, topTextures[i])
					.put(TextureSlot.BOTTOM, bottomTextures[i]);
			String suffix = i == 0 ? "" : "_" + i;
			Identifier modelIdTop = getModelLocation(block, suffix);
			Identifier modelIdHorizontal = getModelLocation(block, "_horizontal" + suffix);
			topModels[i] = topTemplate.create(modelIdTop, mapping, this.modelOutput);
			horizontalModels[i] = horizontalTemplate.create(modelIdHorizontal, mapping, this.modelOutput);
		}
		directionalPillarWithModels(blocks, block, topModels, horizontalModels, weights);
	}

	protected void directionalPillarWithModels(Set<Block> blocks, Block block, Identifier[] topModels, Identifier[] horizontalModels, Integer[] weights) {
		int length = topModels.length;
		if (length != horizontalModels.length || length != weights.length) {
			throw new IllegalArgumentException("Arrays must have equal length");
		}
		var indicesUp = IntStream.range(0, length).boxed();
		var indicesDown = IntStream.range(0, length).boxed();
		var indicesNorth = IntStream.range(0, length).boxed();
		var indicesSouth = IntStream.range(0, length).boxed();
		var indicesEast = IntStream.range(0, length).boxed();
		var indicesWest = IntStream.range(0, length).boxed();
		this.blockstates.add(MultiVariantGenerator.dispatch(block).with(
				PropertyDispatch.initial(BlockStateProperties.FACING)
						.select(Direction.UP, multiVariant(indicesUp.map(i -> weighted(weights[i], new Variant(topModels[i]))).toList()))
						.select(Direction.DOWN, multiVariant(indicesDown.map(i -> weighted(weights[i], new Variant(topModels[i]).withXRot(Quadrant.R180))).toList()))
						.select(Direction.NORTH, multiVariant(indicesNorth.map(i -> weighted(weights[i], new Variant(horizontalModels[i]))).toList()))
						.select(Direction.SOUTH, multiVariant(indicesSouth.map(i -> weighted(weights[i], new Variant(horizontalModels[i]).withYRot(Quadrant.R180))).toList()))
						.select(Direction.EAST, multiVariant(indicesEast.map(i -> weighted(weights[i], new Variant(horizontalModels[i]).withYRot(Quadrant.R90))).toList()))
						.select(Direction.WEST, multiVariant(indicesWest.map(i -> weighted(weights[i], new Variant(horizontalModels[i]).withYRot(Quadrant.R270))).toList()))
		));
		blocks.remove(block);
	}

	protected Variant maybeUVLock(Boolean uvlock, Variant variant) {
		return Boolean.TRUE.equals(uvlock) ? variant.withUvLock(true) : variant;
	}

	protected Weighted<Variant> weighted(int weight, Variant variant) {
		return new Weighted<>(variant, weight);
	}

	protected Variant maybeXRot(Quadrant rotation, Variant variant) {
		return rotation != Quadrant.R0 ? variant.withXRot(rotation) : variant;
	}

	protected Variant maybeYRot(Quadrant rotation, Variant variant) {
		return rotation != Quadrant.R0 ? variant.withYRot(rotation) : variant;
	}

	protected static MultiVariant multiVariant(Variant variant) {
		return new MultiVariant(WeightedList.<Variant>of(new Weighted<>(variant, 1)));
	}

	protected static MultiVariant multiVariant(List<Weighted<Variant>> variants) {
		return new MultiVariant(WeightedList.of(variants));
	}

	protected static Material getBlockTexture(Block block) {
		return TextureMapping.getBlockTexture(block);
	}

	protected static Material getBlockTexture(Block block, String suffix) {
		return TextureMapping.getBlockTexture(block, suffix);
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public final <T> Collection<T> takeAll(Set<T> src, T... items) {
		List<T> ret = Arrays.asList(items);
		for (T item : items) {
			if (!src.contains(item)) {
				getLogger().warn("Item {} not found in set", item);
			}
		}
		if (!src.removeAll(ret)) {
			getLogger().warn("takeAll array didn't yield anything ({})", Arrays.toString(items));
		}
		return ret;
	}

	public final <T> Collection<T> takeAll(Set<T> src, Predicate<T> pred) {
		List<T> ret = new ArrayList<>();

		Iterator<T> iter = src.iterator();
		while (iter.hasNext()) {
			T item = iter.next();
			if (pred.test(item)) {
				iter.remove();
				ret.add(item);
			}
		}

		if (ret.isEmpty()) {
			getLogger().warn("takeAll predicate yielded nothing", new Throwable());
		}
		return ret;
	}

	protected void redStringBlock(Block b) {
		Material selfName = getBlockTexture(b);
		Material front = new Material(prefix("block/red_string_sender"));
		var model = ModelTemplates.CUBE_ORIENTABLE.create(b, new TextureMapping()
				.put(TextureSlot.TOP, selfName)
				.put(TextureSlot.FRONT, front)
				.put(TextureSlot.SIDE, selfName), this.modelOutput);
		this.blockstates.add(MultiVariantGenerator.dispatch(b, multiVariant(new Variant(model)))
				.with(ROTATION_FACING));
	}
}
