/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.client.data.models.model.DelegatedModel;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.SkullBlock;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.TEISR;
import vazkii.botania.common.block.*;
import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.block.decor.FloatingFlowerBlock;
import vazkii.botania.common.block.decor.FlowerMotifBlock;
import vazkii.botania.common.block.decor.PetalBlock;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.item.BottledManaItem;
import vazkii.botania.common.item.brew.BaseBrewItem;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.common.item.material.MysticalPetalItem;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.mixin.TextureSlotAccessor;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static vazkii.botania.common.item.BotaniaItems.*;
import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ItemModelProvider implements DataProvider {
	private static final TextureSlot LAYER1 = TextureSlotAccessor.make("layer1");
	private static final TextureSlot LAYER2 = TextureSlotAccessor.make("layer2");
	private static final TextureSlot LAYER3 = TextureSlotAccessor.make("layer3");
	private static final ModelTemplate GENERATED_1 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
	private static final ModelTemplate GENERATED_2 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1, LAYER2);
	private static final ModelTemplate HANDHELD_1 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/handheld")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
	private static final ModelTemplate HANDHELD_3 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/handheld")), Optional.empty(), TextureSlot.LAYER0, LAYER1, LAYER2, LAYER3);
	private static final ModelTemplate WALL_INVENTORY = new ModelTemplate(Optional.of(prefix("block/shapes/wall_inventory")), Optional.empty(), TextureSlot.TOP, TextureSlot.BOTTOM, TextureSlot.WALL);
	private static final ModelTemplate WALL_INVENTORY_CHECKERED = new ModelTemplate(Optional.of(prefix("block/shapes/wall_inventory_checkered")), Optional.empty(), TextureSlot.NORTH, TextureSlot.SIDE);
	private static final TextureSlot OUTSIDE = TextureSlotAccessor.make("outside");
	private static final TextureSlot CORE = TextureSlotAccessor.make("core");
	private static final ModelTemplate SPREADER = new ModelTemplate(Optional.of(prefix("block/shapes/spreader_item")), Optional.empty(), TextureSlot.SIDE, TextureSlot.BACK, TextureSlot.INSIDE, OUTSIDE, CORE);
	private static final ModelTemplate GENERATED_OVERRIDES = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0);
	private static final ModelTemplate GENERATED_OVERRIDES_1 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/generated")), Optional.empty(), TextureSlot.LAYER0, LAYER1);
	private static final ModelTemplate HANDHELD_OVERRIDES = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/handheld")), Optional.empty(), TextureSlot.LAYER0);
	private static final ModelTemplate HANDHELD_OVERRIDES_2 = new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("item/handheld")), Optional.empty(), TextureSlot.LAYER0, LAYER1, LAYER2);

	private final PackOutput packOutput;

	public ItemModelProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		Set<Item> items = BuiltInRegistries.ITEM.stream().filter(i -> LibMisc.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
				.collect(Collectors.toSet());
		Map<Identifier, Supplier<JsonElement>> map = new HashMap<>();
		Map<Identifier, ClientItem> clientItems = new HashMap<>();
		BiConsumer<Identifier, ModelInstance> models = (id, inst) -> map.put(id, inst);
		BiConsumer<Identifier, ClientItem> clientModels = (id, item) -> clientItems.put(clientItemId(id), item);

		// Block items leave the set here: their models are the block models, so the flat item model
		// fallback in registerItems must never see them again (it would overwrite models/item/<block>
		// with an item/generated model pointing at a texture that only exists as a block texture).
		Set<BlockItem> itemBlocks = new HashSet<>();
		items.removeIf(i -> {
			if (i instanceof BlockItem blockItem) {
				itemBlocks.add(blockItem);
				return true;
			}
			return false;
		});
		registerItemBlocks(itemBlocks, models, clientModels);
		registerItemOverrides(items, models, clientModels);
		registerItems(items, models, clientModels);
		ensureEveryItemHasDefinition(clientItems, clientModels);

		PackOutput.PathProvider modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
		PackOutput.PathProvider itemInfoPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
		List<CompletableFuture<?>> output = new ArrayList<>();

		for (Map.Entry<Identifier, Supplier<JsonElement>> e : map.entrySet()) {
			Identifier id = e.getKey();
			output.add(DataProvider.saveStable(cache, e.getValue().get(), modelPathProvider.json(id)));
		}
		for (Map.Entry<Identifier, ClientItem> e : clientItems.entrySet()) {
			ClientItem clientItem = new ClientItem(e.getValue().model(), e.getValue().properties());
			output.add(DataProvider.saveStable(cache, ClientItem.CODEC, clientItem, itemInfoPathProvider.json(e.getKey())));
		}

		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	/**
	 * 26.1 needs a client item definition for every single item; one without a definition has a blank
	 * icon and no model at all. Anything the dedicated passes above did not cover gets a plain
	 * definition pointing at its own model location, and is logged so the gap stays visible.
	 */
	private static void ensureEveryItemHasDefinition(Map<Identifier, ClientItem> clientItems,
			BiConsumer<Identifier, ClientItem> clientModels) {
		List<Identifier> uncovered = new ArrayList<>();
		BuiltInRegistries.ITEM.stream()
				.filter(i -> LibMisc.MOD_ID.equals(BuiltInRegistries.ITEM.getKey(i).getNamespace()))
				.forEach(i -> {
					Identifier model = ModelLocationUtils.getModelLocation(i);
					if (!clientItems.containsKey(clientItemId(model))) {
						uncovered.add(BuiltInRegistries.ITEM.getKey(i));
						plainModel(model, model, clientModels);
					}
				});
		if (!uncovered.isEmpty()) {
			BotaniaAPI.LOGGER.warn("{} items had no dedicated client item definition and got a plain one: {}",
					uncovered.size(), uncovered);
		}
	}

	private static void registerItems(Set<Item> items, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		// Written manually
		items.remove(manaGun);
		// 26.1: botania:mana_gun is an item model *type* dispatched by the "type" key of the item
		// definition, not a model JSON loader. NeoForge registers the codec through
		// RegisterItemModelsEvent, Fabric through an ItemModels accessor mixin; the old
		// models/item/mana_gun.json loader shim is gone.
		clientModels.accept(ModelLocationUtils.getModelLocation(manaGun), new ClientItem(
				new vazkii.botania.client.model.ManaBlasterBakedModel.Unbaked(
						prefix("item/mana_gun_noclip"), prefix("item/mana_gun_clip")),
				ClientItem.Properties.DEFAULT));
		// Its item model is a hand-made Cubik Studio model (see registerItemBlocks); the generic
		// pass below would emit a flat item/generated model under the same path.
		items.remove(BotaniaBlocks.corporeaCrystalCube.asItem());

		takeAll(items, i -> i instanceof LensItem).forEach(i -> {
			Identifier lens;
			if (i == lensTime || i == lensWarp || i == lensFire || i == lensTripwire) {
				// To avoid z-fighting
				lens = prefix("item/lens_small");
			} else {
				lens = prefix("item/lens");
			}
			Identifier id = GENERATED_1.create(ModelLocationUtils.getModelLocation(i),
					TextureMapping.layer0(new Material(lens)).put(LAYER1, TextureMapping.getItemTexture(i)), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		GENERATED_1.create(ModelLocationUtils.getModelLocation(bloodPendant),
				TextureMapping.layer0(TextureMapping.getItemTexture(bloodPendant))
						.put(LAYER1, TextureMapping.getItemTexture(bloodPendant, "_overlay")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(bloodPendant), ModelLocationUtils.getModelLocation(bloodPendant), clientModels);
		items.remove(bloodPendant);

		HANDHELD_1.create(ModelLocationUtils.getModelLocation(enderDagger),
				TextureMapping.layer0(TextureMapping.getItemTexture(enderDagger))
						.put(LAYER1, TextureMapping.getItemTexture(enderDagger, "_overlay")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(enderDagger), ModelLocationUtils.getModelLocation(enderDagger), clientModels);
		items.remove(enderDagger);

		GENERATED_1.create(ModelLocationUtils.getModelLocation(incenseStick),
				TextureMapping.layer0(TextureMapping.getItemTexture(incenseStick))
						.put(LAYER1, TextureMapping.getItemTexture(incenseStick, "_overlay")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(incenseStick), ModelLocationUtils.getModelLocation(incenseStick), clientModels);
		items.remove(incenseStick);

		GENERATED_1.create(ModelLocationUtils.getModelLocation(manaMirror),
				TextureMapping.layer0(TextureMapping.getItemTexture(manaMirror))
						.put(LAYER1, TextureMapping.getItemTexture(manaMirror, "_overlay")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(manaMirror), ModelLocationUtils.getModelLocation(manaMirror), clientModels);
		items.remove(manaMirror);

		GENERATED_1.create(ModelLocationUtils.getModelLocation(manaTablet),
				TextureMapping.layer0(TextureMapping.getItemTexture(manaTablet))
						.put(LAYER1, TextureMapping.getItemTexture(manaTablet, "_overlay")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(manaTablet), ModelLocationUtils.getModelLocation(manaTablet), clientModels);
		items.remove(manaTablet);

		GENERATED_2.create(ModelLocationUtils.getModelLocation(thirdEye),
				new TextureMapping().put(TextureSlot.LAYER0, TextureMapping.getItemTexture(thirdEye, "_0"))
						.put(LAYER1, TextureMapping.getItemTexture(thirdEye, "_1"))
						.put(LAYER2, TextureMapping.getItemTexture(thirdEye, "_2")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(thirdEye), ModelLocationUtils.getModelLocation(thirdEye), clientModels);
		items.remove(thirdEye);

		takeAll(items, cobbleRod, dirtRod, diviningRod, elementiumAxe, elementiumPick, elementiumShovel, elementiumHoe, elementiumSword,
				exchangeRod, fireRod, glassPick, gravityRod, manasteelAxe, manasteelPick, manasteelShears, manasteelShovel, manasteelHoe,
				missileRod, obedienceStick, rainbowRod, smeltRod, starSword, terraSword, terraformRod, thunderSword, waterRod,
				kingKey, skyDirtRod).forEach(i -> {
					Identifier id = ModelTemplates.FLAT_HANDHELD_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i), consumer);
					plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
				});

		takeAll(items, i -> true).forEach(i -> {
			Identifier id = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});
	}

	private static void singleGeneratedOverride(Item item, Identifier overrideModel, Identifier predicate, double value,
			BiConsumer<Identifier, ModelInstance> consumer, BiConsumer<Identifier, ClientItem> clientModels) {
		Identifier overrideFile = ModelTemplates.FLAT_ITEM.create(overrideModel, TextureMapping.layer0(new Material(overrideModel)), consumer);
		Identifier base = GENERATED_OVERRIDES.create(ModelLocationUtils.getModelLocation(item),
				TextureMapping.layer0(item), consumer);
		rangeDispatch(ModelLocationUtils.getModelLocation(item), predicate, base, clientModels,
				ItemModelUtils.override(plain(overrideFile), (float) value));
	}

	private static void singleGeneratedSuffixOverride(Item item, String suffix, String predicate, double value,
			BiConsumer<Identifier, ModelInstance> consumer, BiConsumer<Identifier, ClientItem> clientModels) {
		singleGeneratedOverride(item, ModelLocationUtils.getModelLocation(item, suffix), prefix(predicate), value, consumer, clientModels);
	}

	private static void singleHandheldOverride(Item item, Identifier overrideModel, Identifier predicate, double value,
			BiConsumer<Identifier, ModelInstance> consumer, BiConsumer<Identifier, ClientItem> clientModels) {
		Identifier overrideFile = ModelTemplates.FLAT_HANDHELD_ITEM.create(overrideModel, TextureMapping.layer0(new Material(overrideModel)), consumer);
		Identifier base = HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(item),
				TextureMapping.layer0(item), consumer);
		rangeDispatch(ModelLocationUtils.getModelLocation(item), predicate, base, clientModels,
				ItemModelUtils.override(plain(overrideFile), (float) value));
	}

	private static void singleHandheldSuffixOverride(Item item, String suffix, String predicate, double value,
			BiConsumer<Identifier, ModelInstance> consumer, BiConsumer<Identifier, ClientItem> clientModels) {
		singleHandheldOverride(item, ModelLocationUtils.getModelLocation(item, suffix), prefix(predicate), value, consumer, clientModels);
	}

	private static void registerItemOverrides(Set<Item> items, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		// Written manually
		items.remove(livingwoodBow);
		items.remove(crystalBow);

		singleGeneratedSuffixOverride(baubleBox, "_open", "open", 1.0, consumer, clientModels);
		items.remove(baubleBox);

		singleGeneratedSuffixOverride(blackHoleTalisman, "_active", "active", 1.0, consumer, clientModels);
		items.remove(blackHoleTalisman);

		registerBrewContainerOverrides((BaseBrewItem) brewFlask, flask, consumer, clientModels);
		items.remove(brewFlask);

		registerBrewContainerOverrides((BaseBrewItem) brewVial, vial, consumer, clientModels);
		items.remove(brewVial);

		singleHandheldOverride(elementiumShears, prefix("item/dammitreddit"), prefix("reddit"), 1, consumer, clientModels);
		items.remove(elementiumShears);

		Identifier vuvuzela = prefix("item/vuvuzela");
		Identifier vuvuzelaModelId = ModelTemplates.FLAT_HANDHELD_ITEM.create(vuvuzela, TextureMapping.layer0(new Material(vuvuzela)), consumer);
		for (Item i : new Item[] { grassHorn, leavesHorn, snowHorn }) {
			Identifier id = ModelLocationUtils.getModelLocation(i);
			Identifier base = GENERATED_OVERRIDES.create(id, TextureMapping.layer0(i), consumer);
			rangeDispatch(id, prefix("vuvuzela"), base, clientModels,
					ItemModelUtils.override(plain(vuvuzelaModelId), 1.0F));
		}
		items.remove(grassHorn);
		items.remove(leavesHorn);
		items.remove(snowHorn);

		singleGeneratedOverride(infiniteFruit, prefix("item/dasboot"), prefix("boot"), 1, consumer, clientModels);
		items.remove(infiniteFruit);

		singleGeneratedSuffixOverride(lexicon, "_elven", "elven", 1.0, consumer, clientModels);
		items.remove(lexicon);

		singleGeneratedSuffixOverride(magnetRing, "_active", "active_ring", 1.0, consumer, clientModels);
		items.remove(magnetRing);

		singleGeneratedSuffixOverride(magnetRingGreater, "_active", "active_ring", 1.0, consumer, clientModels);
		items.remove(magnetRingGreater);

		List<RangeSelectItemModel.Entry> bottleEntries = new ArrayList<>();
		for (int i = 1; i < BottledManaItem.SWIGS; i++) {
			Identifier overrideModel = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(manaBottle, "_" + i),
					TextureMapping.layer0(new Material(ModelLocationUtils.getModelLocation(manaBottle, "_" + i))), consumer);
			bottleEntries.add(ItemModelUtils.override(plain(overrideModel), (float) i / (BottledManaItem.SWIGS - 1)));
		}
		Identifier bottleBase = GENERATED_OVERRIDES.create(ModelLocationUtils.getModelLocation(manaBottle),
				TextureMapping.layer0(manaBottle), consumer);
		rangeDispatch(ModelLocationUtils.getModelLocation(manaBottle), prefix("swigs_taken"), bottleBase, clientModels,
				bottleEntries.toArray(RangeSelectItemModel.Entry[]::new));
		items.remove(manaBottle);

		singleGeneratedOverride(manaCookie, prefix("item/totalbiscuit"), prefix("totalbiscuit"), 1.0, consumer, clientModels);
		items.remove(manaCookie);

		singleHandheldOverride(manasteelSword, prefix("item/elucidator"), prefix("elucidator"), 1.0, consumer, clientModels);
		items.remove(manasteelSword);

		for (Item manaweave : new Item[] { manaweaveHelm, manaweaveChest, manaweaveLegs, manaweaveBoots }) {
			singleGeneratedSuffixOverride(manaweave, "_holiday", "holiday", 1.0, consumer, clientModels);
		}
		items.remove(manaweaveHelm);
		items.remove(manaweaveChest);
		items.remove(manaweaveLegs);
		items.remove(manaweaveBoots);

		singleGeneratedSuffixOverride(slimeBottle, "_active", "active_slime", 1.0, consumer, clientModels);
		items.remove(slimeBottle);

		singleGeneratedSuffixOverride(spawnerMover, "_full", "full", 1.0, consumer, clientModels);
		items.remove(spawnerMover);

		singleGeneratedSuffixOverride(temperanceStone, "_active", "active_temperance", 1.0, consumer, clientModels);
		items.remove(temperanceStone);

		singleHandheldSuffixOverride(terraAxe, "_active", "terra_axe_active", 1.0, consumer, clientModels);
		items.remove(terraAxe);

		singleGeneratedSuffixOverride(autocraftingHalo, "_active", "active_halo", 1.0, consumer, clientModels);
		items.remove(autocraftingHalo);

		// Terra pick: active + tipped, two independent predicates -> nested range dispatch
		Identifier enabledModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(ModelLocationUtils.getModelLocation(terraPick, "_active"),
				TextureMapping.layer0(new Material(ModelLocationUtils.getModelLocation(terraPick, "_active"))), consumer);
		Identifier tippedModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(ModelLocationUtils.getModelLocation(terraPick, "_tipped"),
				TextureMapping.layer0(new Material(ModelLocationUtils.getModelLocation(terraPick, "_tipped"))), consumer);
		Identifier tippedEnabledModel = ModelTemplates.FLAT_HANDHELD_ITEM.create(ModelLocationUtils.getModelLocation(terraPick, "_tipped_active"),
				TextureMapping.layer0(new Material(ModelLocationUtils.getModelLocation(terraPick, "_tipped_active"))), consumer);
		Identifier terraPickBase = HANDHELD_OVERRIDES.create(ModelLocationUtils.getModelLocation(terraPick),
				TextureMapping.layer0(terraPick), consumer);
		// inner: tipped at 1.0 -> tippedEnabledModel, else enabledModel
		ItemModel.Unbaked activeBranch = ItemModelUtils.rangeSelect(BotaniaItemProperties.get(prefix("tipped")), plain(enabledModel),
				ItemModelUtils.override(plain(tippedEnabledModel), 1.0F));
		// outer: active at 1.0 -> activeBranch, else tipped dispatch over base
		ItemModel.Unbaked terraPickModel = ItemModelUtils.rangeSelect(BotaniaItemProperties.get(prefix("terra_pick_active")),
				ItemModelUtils.rangeSelect(BotaniaItemProperties.get(prefix("tipped")), plain(terraPickBase), ItemModelUtils.override(plain(tippedModel), 1.0F)),
				ItemModelUtils.override(activeBranch, 1.0F));
		clientModels.accept(BuiltInRegistries.ITEM.getKey(terraPick), new ClientItem(terraPickModel, ClientItem.Properties.DEFAULT));
		items.remove(terraPick);

		singleHandheldSuffixOverride(tornadoRod, "_active", "active_tornado", 1.0, consumer, clientModels);
		items.remove(tornadoRod);

		registerWandOverrides(twigWand, consumer, clientModels);
		items.remove(twigWand);

		registerWandOverrides(dreamwoodWand, consumer, clientModels);
		items.remove(dreamwoodWand);

		registerBowOverrides(livingwoodBow, consumer, clientModels);
		registerBowOverrides(crystalBow, consumer, clientModels);
	}

	private static void registerWandOverrides(Item wand, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		TextureMapping wandTextures = TextureMapping.layer0(wand)
				.put(LAYER1, TextureMapping.getItemTexture(wand, "_top"))
				.put(LAYER2, TextureMapping.getItemTexture(wand, "_bottom"));
		Identifier wandBind = ModelLocationUtils.getModelLocation(wand, "_bind");
		HANDHELD_3.create(wandBind, wandTextures.copyAndUpdate(LAYER3, TextureMapping.getItemTexture(wand, "_bind")), consumer);
		Identifier wandBase = HANDHELD_OVERRIDES_2.create(ModelLocationUtils.getModelLocation(wand), wandTextures, consumer);
		rangeDispatch(ModelLocationUtils.getModelLocation(wand), prefix("bindmode"), wandBase, clientModels,
				ItemModelUtils.override(plain(wandBind), 1.0F));
	}

	/**
	 * [VanillaCopy] {@code ItemModelGenerators#generateBow}, but with Botania's own {@code pull}
	 * property that accounts for the bow's charge velocity multiplier.
	 */
	private static void registerBowOverrides(Item bow, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		ItemModel.Unbaked bowModel = plain(ModelLocationUtils.getModelLocation(bow));
		int stages = bow == crystalBow ? 5 : 3;
		List<RangeSelectItemModel.Entry> pullEntries = new ArrayList<>();
		ItemModel.Unbaked pulling0 = null;
		for (int i = 1; i <= stages; i++) {
			// Botania's pulling models and textures are 1-based (crystal: 1..5, livingwood: 1..3) and the
			// models are hand-written with the bow as parent so they inherit its display transforms.
			Identifier modelId = ModelLocationUtils.getModelLocation(bow, "_pulling_" + i);
			ItemModel.Unbaked model = plain(modelId);
			if (i == 1) {
				pulling0 = model;
			} else {
				float threshold = bow == crystalBow
						? new float[] { 0.35F, 0.65F, 0.8F, 0.9F }[i - 2]
						: new float[] { 0.65F, 0.9F }[i - 2];
				pullEntries.add(ItemModelUtils.override(model, threshold));
			}
		}
		ItemModel.Unbaked pulling = ItemModelUtils.rangeSelect(BotaniaItemProperties.get(prefix("pull")), pulling0,
				pullEntries.toArray(RangeSelectItemModel.Entry[]::new));
		clientModels.accept(BuiltInRegistries.ITEM.getKey(bow), new ClientItem(
				ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), pulling, bowModel), ClientItem.Properties.DEFAULT));
	}

	private static void registerBrewContainerOverrides(BaseBrewItem brewItem, Item emptyItem,
			BiConsumer<Identifier, ModelInstance> consumer, BiConsumer<Identifier, ClientItem> clientModels) {
		List<RangeSelectItemModel.Entry> flaskEntries = new ArrayList<>();
		for (int i = 1; i < brewItem.getSwigs(); i++) {
			Identifier overrideModel = ModelLocationUtils.getModelLocation(brewItem, "_" + i);
			GENERATED_1.create(overrideModel,
					TextureMapping.layer0(emptyItem).put(LAYER1, new Material(overrideModel)),
					consumer);
			flaskEntries.add(ItemModelUtils.override(plain(overrideModel), (float) i / (brewItem.getSwigs() - 1)));
		}
		Identifier base = GENERATED_OVERRIDES_1.create(ModelLocationUtils.getModelLocation(brewItem),
				TextureMapping.layer0(emptyItem).put(LAYER1, TextureMapping.getItemTexture(brewItem, "_0")),
				consumer);
		rangeDispatch(ModelLocationUtils.getModelLocation(brewItem), prefix("swigs_taken"), base, clientModels,
				flaskEntries.toArray(RangeSelectItemModel.Entry[]::new));
	}

	private void registerItemBlocks(Set<BlockItem> itemBlocks, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		// Manually written
		itemBlocks.remove(BotaniaBlocks.corporeaCrystalCube.asItem());
		registerManualModel(BotaniaBlocks.corporeaCrystalCube.asItem(), clientModels);

		// Generated by FloatingFlowerModelProvider, which writes the item model with a custom loader.
		// Their ClientItem definition still has to be emitted here, pointing at that same model; the
		// generic block item pass below would instead overwrite the custom loader model with a
		// delegated one, so they must not stay in the set.
		takeAll(itemBlocks, i -> i.getBlock() instanceof FloatingFlowerBlock).forEach(i -> {
			Identifier model = ModelLocationUtils.getModelLocation(i);
			plainModel(model, model, clientModels);
		});

		GENERATED_1.create(ModelLocationUtils.getModelLocation(BotaniaBlocks.animatedTorch.asItem()),
				TextureMapping.layer0(Blocks.REDSTONE_TORCH).put(LAYER1, new Material(prefix("block/animated_torch_glimmer"))), consumer);
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.animatedTorch.asItem()),
				ModelLocationUtils.getModelLocation(BotaniaBlocks.animatedTorch.asItem()), clientModels);
		itemBlocks.remove(BotaniaBlocks.animatedTorch.asItem());

		// 3D skull item, mirroring vanilla head items (26.1 data-driven "minecraft:head" special
		// model). GaiaHeadBlock registers its type into SkullBlock.Type.TYPES, so "kind": "gaia"
		// resolves through SkullSpecialRenderer.Unbaked's codec; SkullBlockRendererMixin supplies
		// the viewer-mirrored texture at bake time.
		Identifier gaiaHead = ModelLocationUtils.getModelLocation(BotaniaBlocks.gaiaHead.asItem());
		// Same transform as vanilla BlockModelGenerators.SKULL_TRANSFORM (private there, inlined here)
		var skullTransform = new com.mojang.math.Transformation(
				new org.joml.Vector3f(0.5F, 0.0F, 0.5F), new org.joml.Quaternionf().rotationX((float) Math.PI), null, null);
		clientModels.accept(gaiaHead, new ClientItem(
				ItemModelUtils.specialModel(ModelLocationUtils.decorateItemModelLocation("template_skull"),
						skullTransform, new SkullSpecialRenderer.Unbaked(GaiaHeadBlock.GAIA_TYPE)),
				ClientItem.Properties.DEFAULT));
		itemBlocks.remove(BotaniaBlocks.gaiaHead.asItem());

		takeAll(itemBlocks, i -> i.getBlock() instanceof BotaniaDoubleFlowerBlock).forEach(i -> {
			Identifier id = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i),
					TextureMapping.layer0(TextureMapping.getBlockTexture(i.getBlock(), "_top")), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		takeAll(itemBlocks, i -> i.getBlock() instanceof PetalBlock).forEach(i -> {
			consumer.accept(ModelLocationUtils.getModelLocation(i), new DelegatedModel(prefix("block/petal_block")));
			plainModel(ModelLocationUtils.getModelLocation(i), prefix("block/petal_block"), clientModels);
		});

		takeAll(itemBlocks, BotaniaBlocks.livingwoodFramed.asItem(), BotaniaBlocks.dreamwoodFramed.asItem()).forEach(i -> {
			String name = i == BotaniaBlocks.livingwoodFramed.asItem() ? "livingwood" : "dreamwood";
			consumer.accept(ModelLocationUtils.getModelLocation(i), new DelegatedModel(prefix("block/framed_" + name + "_horizontal_z")));
			plainModel(ModelLocationUtils.getModelLocation(i), prefix("block/framed_" + name + "_horizontal_z"), clientModels);
		});
		itemBlocks.remove(BotaniaBlocks.livingwoodFramed.asItem());
		itemBlocks.remove(BotaniaBlocks.dreamwoodFramed.asItem());

		takeAll(itemBlocks, i -> i.getBlock() instanceof IronBarsBlock).forEach(i -> {
			String name = BuiltInRegistries.ITEM.getKey(i).getPath();
			String baseName = name.substring(0, name.length() - "_pane".length());
			Identifier id = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(new Material(prefix("block/" + baseName))), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		Predicate<BlockItem> defaultGenerated = i -> {
			Block b = i.getBlock();
			return XplatAbstractions.INSTANCE.isSpecialFlowerBlock(b)
					|| b instanceof BotaniaMushroomBlock
					|| b instanceof LuminizerBlock
					|| b instanceof BotaniaFlowerBlock
					|| b == BotaniaBlocks.ghostRail;
		};
		takeAll(itemBlocks, defaultGenerated).forEach(i -> {
			Identifier id = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(i.getBlock()), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		takeAll(itemBlocks, b -> b.getBlock() instanceof FlowerMotifBlock).forEach(i -> {
			String name = BuiltInRegistries.ITEM.getKey(i).getPath();
			Identifier texName = prefix("block/" + name.replace("_motif", ""));
			Identifier id = ModelTemplates.FLAT_ITEM.create(ModelLocationUtils.getModelLocation(i), TextureMapping.layer0(new Material(texName)), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		takeAll(itemBlocks, i -> i.getBlock() instanceof ManaPoolBlock).forEach(i -> {
			Identifier fullModel = ModelLocationUtils.getModelLocation(i.getBlock(), "_full");
			Identifier baseModel = ModelLocationUtils.getModelLocation(i.getBlock());
			consumer.accept(ModelLocationUtils.getModelLocation(i), new DelegatedModel(baseModel));
			rangeDispatch(ModelLocationUtils.getModelLocation(i), prefix("full"), baseModel, clientModels,
					ItemModelUtils.override(plain(fullModel), 1.0F));
		});

		takeAll(itemBlocks, Stream.of(BotaniaBlocks.livingwoodWall, BotaniaBlocks.livingwoodStrippedWall,
				BotaniaBlocks.dreamwoodWall, BotaniaBlocks.dreamwoodStrippedWall)
				.map(b -> (BlockItem) b.asItem())
				.toArray(BlockItem[]::new)).forEach(i -> {
					String name = BuiltInRegistries.ITEM.getKey(i).getPath();
					String baseName = name.substring(0, name.length() - "_wall".length()) + "_log";
					Identifier id = ModelTemplates.WALL_INVENTORY.create(ModelLocationUtils.getModelLocation(i),
							new TextureMapping().put(TextureSlot.WALL, new Material(prefix("block/" + baseName))), consumer);
					plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
				});

		takeAll(itemBlocks, i -> i.getBlock() instanceof WallBlock).forEach(i -> {
			String name = BuiltInRegistries.ITEM.getKey(i).getPath();
			String baseName = name.substring(0, name.length() - "_wall".length());
			Identifier id = ModelTemplates.WALL_INVENTORY.create(ModelLocationUtils.getModelLocation(i),
					new TextureMapping().put(TextureSlot.WALL, new Material(prefix("block/" + baseName))), consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		takeAll(itemBlocks, i -> i.getBlock() instanceof ManaSpreaderBlock).forEach(i -> {
			String name = BuiltInRegistries.ITEM.getKey(i).getPath();
			String outside;
			if (i.getBlock() == BotaniaBlocks.elvenSpreader) {
				outside = "dreamwood_log_3";
			} else if (i.getBlock() == BotaniaBlocks.gaiaSpreader) {
				outside = name + "_outside";
			} else {
				outside = "livingwood_log";
			}
			String inside;
			if (i.getBlock() == BotaniaBlocks.elvenSpreader) {
				inside = "stripped_dreamwood_log_3";
			} else if (i.getBlock() == BotaniaBlocks.gaiaSpreader) {
				inside = name + "_inside";
			} else {
				inside = "stripped_livingwood_log";
			}
			Identifier id = SPREADER.create(ModelLocationUtils.getModelLocation(i),
					new TextureMapping()
							.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(i.getBlock(), "_side"))
							.put(OUTSIDE, new Material(prefix("block/" + outside)))
							.put(TextureSlot.BACK, TextureMapping.getBlockTexture(i.getBlock(), "_back"))
							.put(TextureSlot.INSIDE, new Material(prefix("block/" + inside)))
							.put(CORE, TextureMapping.getBlockTexture(i.getBlock(), "_core")),
					consumer);
			plainModel(ModelLocationUtils.getModelLocation(i), id, clientModels);
		});

		takeAll(itemBlocks, BotaniaBlocks.avatar.asItem(), BotaniaBlocks.bellows.asItem(),
				BotaniaBlocks.brewery.asItem(), BotaniaBlocks.corporeaIndex.asItem(), BotaniaBlocks.gaiaPylon.asItem(),
				BotaniaBlocks.hourglass.asItem(), BotaniaBlocks.manaPylon.asItem(), BotaniaBlocks.naturaPylon.asItem())
						.forEach(i -> builtinEntity(i, consumer, clientModels));

		takeAll(itemBlocks, BotaniaBlocks.teruTeruBozu.asItem())
				.forEach(i -> builtinEntity(i, consumer, clientModels, 2.5));

		

		Identifier dreamwoodFence = ModelTemplates.FENCE_INVENTORY.create(ModelLocationUtils.getModelLocation(BotaniaBlocks.dreamwoodFence.asItem()),
				TextureMapping.defaultTexture(BotaniaBlocks.dreamwoodPlanks), consumer);
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.dreamwoodFence.asItem()), dreamwoodFence, clientModels);
		itemBlocks.remove(BotaniaBlocks.dreamwoodFence.asItem());

		Identifier livingwoodFence = ModelTemplates.FENCE_INVENTORY.create(ModelLocationUtils.getModelLocation(BotaniaBlocks.livingwoodFence.asItem()),
				TextureMapping.defaultTexture(BotaniaBlocks.livingwoodPlanks), consumer);
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.livingwoodFence.asItem()), livingwoodFence, clientModels);
		itemBlocks.remove(BotaniaBlocks.livingwoodFence.asItem());

		consumer.accept(ModelLocationUtils.getModelLocation(BotaniaBlocks.elfGlass.asItem()), new DelegatedModel(prefix("block/elf_glass_0")));
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.elfGlass.asItem()), prefix("block/elf_glass_0"), clientModels);
		itemBlocks.remove(BotaniaBlocks.elfGlass.asItem());

		Identifier plainsWall = WALL_INVENTORY.create(ModelLocationUtils.getModelLocation(BotaniaBlocks.biomeBrickPlainsWall.asItem()),
				new TextureMapping()
						.put(TextureSlot.TOP, TextureMapping.getBlockTexture(BotaniaBlocks.biomeBrickPlains, "_top"))
						.put(TextureSlot.BOTTOM, TextureMapping.getBlockTexture(BotaniaBlocks.biomeBrickPlains, "_top"))
						.put(TextureSlot.WALL, TextureMapping.getBlockTexture(BotaniaBlocks.biomeBrickPlains)),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.biomeBrickPlainsWall.asItem()), plainsWall, clientModels);
		itemBlocks.remove(BotaniaBlocks.biomeBrickPlainsWall.asItem());

		Identifier mesaWall = WALL_INVENTORY_CHECKERED.create(ModelLocationUtils.getModelLocation(BotaniaBlocks.biomeBrickMesaWall.asItem()),
				new TextureMapping()
						.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(BotaniaBlocks.biomeBrickMesa))
						.put(TextureSlot.NORTH, TextureMapping.getBlockTexture(BotaniaBlocks.biomeBrickMesa, "_mirrored")),
				consumer);
		plainModel(ModelLocationUtils.getModelLocation(BotaniaBlocks.biomeBrickMesaWall.asItem()), mesaWall, clientModels);
		itemBlocks.remove(BotaniaBlocks.biomeBrickMesaWall.asItem());

		itemBlocks.forEach(i -> {
			Identifier blockModel = ModelLocationUtils.getModelLocation(i.getBlock());
			consumer.accept(ModelLocationUtils.getModelLocation(i), new DelegatedModel(blockModel));
			plainModel(ModelLocationUtils.getModelLocation(i), blockModel, clientModels);
		});
	}

	private static ItemModel.Unbaked plain(Identifier modelId) {
		return ItemModelUtils.plainModel(modelId);
	}

	/**
	 * 26.1 stores client item definitions at {@code assets/<ns>/items/<itemPath>.json}, keyed by the
	 * <em>item</em> id, while Botania's call sites pass model locations ({@code <ns>:item/<itemPath>}).
	 * Reconciling them here keeps every {@code ClientItem} from landing in {@code items/item/…}, where
	 * nothing would ever load it (blank icons).
	 */
	private static Identifier clientItemId(Identifier modelLocation) {
		String path = modelLocation.getPath();
		return path.startsWith("item/")
				? Identifier.fromNamespaceAndPath(modelLocation.getNamespace(), path.substring("item/".length()))
				: modelLocation;
	}

	private static void plainModel(Identifier itemId, Identifier modelId, BiConsumer<Identifier, ClientItem> clientModels) {
		clientModels.accept(itemId, new ClientItem(plain(modelId), ClientItem.Properties.DEFAULT));
	}

	/**
	 * Emits only the client item definition for an item whose model file is hand-written under
	 * {@code assets/botania/models/item/}. 26.1 needs the definition for every item, even when the
	 * model itself is not generated.
	 */
	private static void registerManualModel(Item item, BiConsumer<Identifier, ClientItem> clientModels) {
		plainModel(ModelLocationUtils.getModelLocation(item), ModelLocationUtils.getModelLocation(item), clientModels);
	}

	private static void rangeDispatch(Identifier itemId, Identifier propertyId, Identifier fallbackModel,
			BiConsumer<Identifier, ClientItem> clientModels, RangeSelectItemModel.Entry... entries) {
		clientModels.accept(itemId, new ClientItem(
				new RangeSelectItemModel.Unbaked(Optional.empty(), BotaniaItemProperties.get(propertyId), 1.0F,
						List.of(entries), Optional.of(plain(fallbackModel))),
				ClientItem.Properties.DEFAULT));
	}

	// [VanillaCopy] item/chest.json
	// Scuffed af.....but it works :wacko:
	private static final String BUILTIN_ENTITY_DISPLAY_STR =
			"""
					{
						"gui": {
							"rotation": [30, 45, 0],
							"translation": [0, 0, 0],
							"scale": [0.625, 0.625, 0.625]
						},
						"ground": {
							"rotation": [0, 0, 0],
							"translation": [0, 3, 0],
							"scale": [0.25, 0.25, 0.25]
						},
						"head": {
							"rotation": [0, 180, 0],
							"translation": [0, 0, 0],
							"scale": [1, 1, 1]
						},
						"fixed": {
							"rotation": [0, 180, 0],
							"translation": [0, 0, 0],
							"scale": [0.5, 0.5, 0.5]
						},
						"thirdperson_righthand": {
							"rotation": [75, 315, 0],
							"translation": [0, 2.5, 0],
							"scale": [0.375, 0.375, 0.375]
						},
						"firstperson_righthand": {
							"rotation": [0, 315, 0],
							"translation": [0, 0, 0],
							"scale": [0.4, 0.4, 0.4]
						}
					}""";
	private static final JsonElement BUILTIN_ENTITY_DISPLAY = new Gson().fromJson(BUILTIN_ENTITY_DISPLAY_STR, JsonElement.class);

	protected void builtinEntity(Item i, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels) {
		builtinEntity(i, consumer, clientModels, 0.0);
	}

	protected void builtinEntity(Item i, BiConsumer<Identifier, ModelInstance> consumer,
			BiConsumer<Identifier, ClientItem> clientModels, double handYOffset) {
		final JsonElement display;
		if (handYOffset == 0.0) {
			display = BUILTIN_ENTITY_DISPLAY;
		} else {
			display = BUILTIN_ENTITY_DISPLAY.deepCopy();
			display.getAsJsonObject().getAsJsonObject("firstperson_righthand")
					.getAsJsonArray("translation").set(1, new JsonPrimitive(handYOffset));
		}
		Identifier modelId = ModelLocationUtils.getModelLocation(i);
		consumer.accept(modelId, () -> {
			JsonObject json = new JsonObject();
			// 1.20.1 used the virtual "minecraft:builtin/entity" parent here, which no longer
			// exists in 26.1. The item is drawn by TEISR (see the specialModel below), so the
			// parent's geometry is never used - it only provides the texture slot chain
			// (particle) and keeps the new model validation from warning.
			json.addProperty("parent", ModelLocationUtils.getModelLocation(((BlockItem) i).getBlock()).toString());
			json.add("display", display);
			return json;
		});
		clientModels.accept(modelId, new ClientItem(
				ItemModelUtils.specialModel(modelId, new TEISR.Unbaked(BuiltInRegistries.BLOCK.getKey(
						((BlockItem) i).getBlock()))),
				ClientItem.Properties.DEFAULT));
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	public static <T> Collection<T> takeAll(Set<? extends T> src, T... items) {
		List<T> ret = Arrays.asList(items);
		for (T item : items) {
			if (!src.contains(item)) {
				BotaniaAPI.LOGGER.warn("Item {} not found in set", item);
			}
		}
		if (!src.removeAll(ret)) {
			BotaniaAPI.LOGGER.warn("takeAll array didn't yield anything ({})", Arrays.toString(items));
		}
		return ret;
	}

	public static <T> Collection<T> takeAll(Set<T> src, Predicate<T> pred) {
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
			BotaniaAPI.LOGGER.warn("takeAll predicate yielded nothing", new Throwable());
		}
		return ret;
	}

	@NotNull
	@Override
	public String getName() {
		return "Botania item models";
	}
}
