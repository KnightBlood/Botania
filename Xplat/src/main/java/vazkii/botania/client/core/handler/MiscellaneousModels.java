/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.CorporeaCrystalCubeBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaPumpBlockEntityRenderer;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.KeyOfTheKingsLawItem;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class MiscellaneousModels {
	private static final Identifier goldfishModelId = prefix("icon/goldfish");
	private static final Identifier phiFlowerModelId = prefix("icon/phiflower");
	private static final Identifier nerfBatModelId = prefix("icon/nerfbat");
	private static final Identifier bloodPendantChainId = prefix("icon/blood_pendant_chain");
	private static final Identifier bloodPendantGemId = prefix("icon/blood_pendant_gem");
	private static final Identifier[] kingKeyWeaponModelIds = IntStream.range(0, KeyOfTheKingsLawItem.WEAPON_TYPES)
			.mapToObj(i -> prefix("icon/gate_weapon_" + i)).toArray(Identifier[]::new);
	private static final Identifier terrasteelHelmWillModelId = prefix("icon/will_flame");
	private static final Identifier[] thirdEyeLayerIds = IntStream.range(0, ThirdEyeItem.Renderer.NUM_LAYERS)
			.mapToObj(i -> prefix("icon/third_eye_" + i)).toArray(Identifier[]::new);
	private static final Identifier pyroclastGemId = prefix("icon/lava_pendant_gem");
	private static final Identifier crimsonGemId = prefix("icon/super_lava_pendant_gem");
	private static final Identifier itemFinderGemId = prefix("icon/itemfinder_gem");
	private static final Identifier cirrusGemId = prefix("icon/cloud_pendant_gem");
	private static final Identifier nimbusGemId = prefix("icon/super_cloud_pendant_gem");
	private static final Identifier snowflakePendantGemId = prefix("icon/ice_pendant_gem");
	private static final Identifier[] tiaraWingIconIds = IntStream.range(0, FlugelTiaraItem.WING_TYPES)
			.mapToObj(i -> prefix("icon/tiara_wing_" + (i + 1))).toArray(Identifier[]::new);
	private static final Identifier corporeaCrystalCubeGlassId = prefix("block/corporea_crystal_cube_glass");
	private static final Identifier manaPumpHead = prefix("block/pump_head");
	private static final Identifier elvenSpreaderCoreId = prefix("block/elven_spreader_core");
	private static final Identifier gaiaSpreaderCoreId = prefix("block/gaia_spreader_core");
	private static final Identifier manaSpreaderCoreId = prefix("block/mana_spreader_core");
	private static final Identifier redstoneSpreaderCoreId = prefix("block/redstone_spreader_core");
	private static final Identifier manaSpreaderScaffoldingId = prefix("block/mana_spreader_scaffolding");
	private static final Identifier elvenSpreaderScaffoldingId = prefix("block/elven_spreader_scaffolding");
	private static final Identifier gaiaSpreaderScaffoldingId = prefix("block/gaia_spreader_scaffolding");
	private static final Map<DyeColor, Identifier> spreaderPaddingIds = new EnumMap<>(ColorHelper.supportedColors().collect(Collectors.toMap(Function.identity(), color -> prefix("block/" + color.getSerializedName() + "_spreader_padding"))));

	public static final MiscellaneousModels INSTANCE = new MiscellaneousModels();

	private final Map<Identifier, Function<BlockStateModel, BlockStateModel>> afterBakeModifiers;
	private final Map<Identifier, Consumer<BlockStateModel>> modelConsumers;

	public boolean registeredModels = false;

	public final BlockStateModel[] tiaraWingIcons;
	public final BlockStateModel[] thirdEyeLayers;

	public BlockStateModel goldfishModel,
			phiFlowerModel,
			nerfBatModel,
			bloodPendantChain,
			bloodPendantGem,
			snowflakePendantGem,
			itemFinderGem,
			pyroclastGem,
			crimsonGem,
			cirrusGem,
			nimbusGem,
			terrasteelHelmWillModel,
			elvenSpreaderCore,
			gaiaSpreaderCore,
			manaSpreaderCore,
			redstoneSpreaderCore,
			manaSpreaderScaffolding,
			elvenSpreaderScaffolding,
			gaiaSpreaderScaffolding;

	private final Map<Identifier, BlockStateModel> taterModels = new HashMap<>();

	public final HashMap<DyeColor, BlockStateModel> spreaderPaddings = new HashMap<>();

	public final BlockStateModel[] kingKeyWeaponModels;

	public void onModelRegister(ResourceManager rm, Consumer<Identifier> consumer) {
		modelConsumers.keySet().forEach(consumer);

		registerIslands();
		registerTaters(rm, consumer);

		if (!registeredModels) {
			registeredModels = true;
		}
	}

	private static void registerIslands() {
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GRASS, prefix("block/islands/island_grass"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.PODZOL, prefix("block/islands/island_podzol"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MYCEL, prefix("block/islands/island_mycel"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SNOW, prefix("block/islands/island_snow"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.DRY, prefix("block/islands/island_dry"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GOLDEN, prefix("block/islands/island_golden"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.VIVID, prefix("block/islands/island_vivid"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SCORCHED, prefix("block/islands/island_scorched"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.INFUSED, prefix("block/islands/island_infused"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MUTATED, prefix("block/islands/island_mutated"));
	}

	private static void registerTaters(ResourceManager rm, Consumer<Identifier> consumer) {
		for (Identifier model : rm.listResources(ResourcesLib.PREFIX_MODELS + ResourcesLib.PREFIX_TINY_POTATO, s -> s.getPath().endsWith(ResourcesLib.ENDING_JSON)).keySet()) {
			if (LibMisc.MOD_ID.equals(model.getNamespace())) {
				String path = model.getPath();
				path = path.substring(ResourcesLib.PREFIX_MODELS.length(), path.length() - ResourcesLib.ENDING_JSON.length());
				consumer.accept(Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, path));
			}
		}
	}

	public void onModelBake(Map<Identifier, BlockStateModel> map) {
		if (!registeredModels) {
			BotaniaAPI.LOGGER.error("Additional models failed to register! Aborting baking models to avoid early crashing.");
			return;
		}
		afterBakeModifiers.forEach((Identifier, afterBakeModifier) -> map.computeIfPresent(Identifier, (resourceLoc, bakedModel) -> afterBakeModifier.apply(bakedModel)));
		map.forEach((Identifier, bakedModel) -> collectTaterModel(Identifier, bakedModel));
		modelConsumers.forEach((Identifier, bakedModelConsumer) -> bakedModelConsumer.accept(map.get(Identifier)));
	}

	public Map<Identifier, BlockStateModel> taterModels() {
		return taterModels;
	}

	public BlockStateModel modifyModelAfterbake(BlockStateModel bakedModel, Identifier id) {
		collectTaterModel(id, bakedModel);
		modelConsumers.getOrDefault(id, model -> {}).accept(bakedModel);
		return afterBakeModifiers.getOrDefault(id, Function.identity()).apply(bakedModel);
	}

	/**
	 * Records every baked model that lives under {@code models/tiny_potato/}, so that the tiny potato
	 * renderer can look up a model by its name without access to the vanilla baked model registry.
	 */
	public void collectTaterModel(Identifier id, BlockStateModel model) {
		if (id.getPath().startsWith(ResourcesLib.PREFIX_TINY_POTATO + "/")) {
			taterModels.put(id, model);
		}
	}

	private MiscellaneousModels() {
		afterBakeModifiers = new HashMap<>();
		afterBakeModifiers.put(prefix(LibBlockNames.PLATFORM_ABSTRUSE), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);
		afterBakeModifiers.put(prefix(LibBlockNames.PLATFORM_SPECTRAL), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);
		afterBakeModifiers.put(prefix(LibBlockNames.PLATFORM_INFRANGIBLE), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);

		modelConsumers = new HashMap<>();
		modelConsumers.put(elvenSpreaderCoreId, bakedModel -> this.elvenSpreaderCore = bakedModel);
		modelConsumers.put(gaiaSpreaderCoreId, bakedModel -> this.gaiaSpreaderCore = bakedModel);
		modelConsumers.put(manaSpreaderCoreId, bakedModel -> this.manaSpreaderCore = bakedModel);
		modelConsumers.put(redstoneSpreaderCoreId, bakedModel -> this.redstoneSpreaderCore = bakedModel);
		modelConsumers.put(manaSpreaderScaffoldingId, bakedModel -> this.manaSpreaderScaffolding = bakedModel);
		modelConsumers.put(elvenSpreaderScaffoldingId, bakedModel -> this.elvenSpreaderScaffolding = bakedModel);
		modelConsumers.put(gaiaSpreaderScaffoldingId, bakedModel -> this.gaiaSpreaderScaffolding = bakedModel);
		for (var color : spreaderPaddingIds.keySet()) {
			modelConsumers.put(spreaderPaddingIds.get(color), bakedModel -> spreaderPaddings.put(color, bakedModel));
		}

		modelConsumers.put(corporeaCrystalCubeGlassId, bakedModel -> CorporeaCrystalCubeBlockEntityRenderer.cubeModel = bakedModel);
		modelConsumers.put(manaPumpHead, bakedModel -> ManaPumpBlockEntityRenderer.headModel = bakedModel);

		modelConsumers.put(goldfishModelId, bakedModel -> this.goldfishModel = bakedModel);
		modelConsumers.put(phiFlowerModelId, bakedModel -> this.phiFlowerModel = bakedModel);
		modelConsumers.put(nerfBatModelId, bakedModel -> this.nerfBatModel = bakedModel);
		modelConsumers.put(bloodPendantChainId, bakedModel -> this.bloodPendantChain = bakedModel);
		modelConsumers.put(bloodPendantGemId, bakedModel -> this.bloodPendantGem = bakedModel);
		modelConsumers.put(terrasteelHelmWillModelId, bakedModel -> this.terrasteelHelmWillModel = bakedModel);
		modelConsumers.put(pyroclastGemId, bakedModel -> this.pyroclastGem = bakedModel);
		modelConsumers.put(crimsonGemId, bakedModel -> this.crimsonGem = bakedModel);
		modelConsumers.put(itemFinderGemId, bakedModel -> this.itemFinderGem = bakedModel);
		modelConsumers.put(cirrusGemId, bakedModel -> this.cirrusGem = bakedModel);
		modelConsumers.put(nimbusGemId, bakedModel -> this.nimbusGem = bakedModel);
		modelConsumers.put(snowflakePendantGemId, bakedModel -> this.snowflakePendantGem = bakedModel);

		kingKeyWeaponModels = getBakedModels(modelConsumers, kingKeyWeaponModelIds);
		thirdEyeLayers = getBakedModels(modelConsumers, thirdEyeLayerIds);
		tiaraWingIcons = getBakedModels(modelConsumers, tiaraWingIconIds);
	}

	private static BlockStateModel[] getBakedModels(Map<Identifier, Consumer<BlockStateModel>> consumers, Identifier[] ids) {
		final BlockStateModel[] bakedModels = new BlockStateModel[ids.length];
		for (int i = 0; i < ids.length; i++) {
			int index = i;
			consumers.put(ids[index], bakedModel -> bakedModels[index] = bakedModel);
		}
		return bakedModels;
	}
}
