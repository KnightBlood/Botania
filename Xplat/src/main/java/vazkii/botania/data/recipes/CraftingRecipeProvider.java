/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.api.state.enums.CraftyCratePattern;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.crafting.recipe.*;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.IngredientHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibItemNames;
import vazkii.botania.common.lib.ResourceLocationHelper;
import vazkii.botania.mixin.RecipeProviderAccessor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CraftingRecipeProvider extends BotaniaRecipeProvider {
	/**
	 * 26.1: recipe builders resolve TagKey references through a HolderGetter at build time, but
	 * Botania's own tags only exist as datagen output, not in the bootstrap lookup. This wrapper
	 * hands out empty named holder sets for unknown tags, which still serialize to the tag id.
	 */
	public static final HolderGetter<Item> TAG_TOLERANT_ITEMS = new HolderGetter<>() {
		@Override
		public Optional<Holder.Reference<Item>> get(ResourceKey<Item> key) {
			return BuiltInRegistries.ITEM.get(key.identifier());
		}

		@Override
		public Optional<HolderSet.Named<Item>> get(TagKey<Item> tag) {
			return Optional.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
		}
	};

	public CraftingRecipeProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		specialRecipe(output, AncientWillRecipe.SERIALIZER);
		specialRecipe(output, BlackHoleTalismanExtractRecipe.SERIALIZER);
		specialRecipe(output, CompositeLensRecipe.SERIALIZER);
		specialRecipe(output, CosmeticAttachRecipe.SERIALIZER);
		specialRecipe(output, CosmeticRemoveRecipe.SERIALIZER);
		specialRecipe(output, ResoluteIvyRecipe.SERIALIZER);
		specialRecipe(output, LaputaShardUpgradeRecipe.SERIALIZER);
		specialRecipe(output, LensDyeingRecipe.SERIALIZER);
		specialRecipe(output, ManaBlasterClipRecipe.SERIALIZER);
		specialRecipe(output, ManaBlasterLensRecipe.SERIALIZER);
		specialRecipe(output, ManaBlasterRemoveLensRecipe.SERIALIZER);
		specialRecipe(output, MergeVialRecipe.SERIALIZER);
		specialRecipe(output, PhantomInkRecipe.SERIALIZER);
		specialRecipe(output, SpellbindingClothRecipe.SERIALIZER);
		specialRecipe(output, SplitLensRecipe.SERIALIZER);
		specialRecipe(output, TerraShattererTippingRecipe.SERIALIZER);

		registerMain(output);
		registerMisc(output);
		registerTools(output);
		registerTrinkets(output);
		registerLenses(output);
		registerCorporeaAndRedString(output);
		registerFloatingFlowers(output);
		registerConversions(output);
		registerDecor(output);
	}

	public static Criterion<?> conditionsFromItem(ItemLike item) {
		return RecipeProviderAccessor.botania_condition(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, item).build());
	}

	private static Criterion<?> conditionsFromItems(ItemLike... items) {
		ItemPredicate[] preds = new ItemPredicate[items.length];
		for (int i = 0; i < items.length; i++) {
			preds[i] = ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, items[i]).build();
		}

		return RecipeProviderAccessor.botania_condition(preds);
	}

	public static Criterion<?> conditionsFromTag(TagKey<Item> tag) {
		return RecipeProviderAccessor.botania_condition(ItemPredicate.Builder.item().of(TAG_TOLERANT_ITEMS, tag).build());
	}

	/** Addons: override this to return your modid */
	protected Identifier prefix(String path) {
		return ResourceLocationHelper.prefix(path);
	}

	private void registerMain(RecipeOutput output) {
		AtomicReference<BotaniaFinishedRecipe> spreaderBase = new AtomicReference<>();
		AtomicReference<BotaniaFinishedRecipe> spreaderGog = new AtomicReference<>();
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.manaSpreader)
				.define('P', BotaniaTags.Items.PETALS)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.define('C', Items.COPPER_INGOT)
				.pattern("WWW")
				.pattern("CP ")
				.pattern("WWW")
				.group("botania:spreader")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.save(capturing(spreaderBase::set));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.manaSpreader)
				.define('P', BotaniaTags.Items.PETALS)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern("WWW")
				.pattern("WP ")
				.pattern("WWW")
				.group("botania:spreader")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.save(capturing(spreaderGog::set));
		saveFinishedRecipe(output, new GogAlternationResult(spreaderGog.get(), spreaderBase.get()));
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.redstoneSpreader)
				.requires(BotaniaBlocks.manaSpreader)
				.requires(Items.REDSTONE)
				.group("botania:spreader")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.manaSpreader))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.elvenSpreader)
				.define('P', BotaniaTags.Items.PETALS)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('W', BotaniaTags.Items.DREAMWOOD_LOGS)
				.pattern("WWW")
				.pattern("EP ")
				.pattern("WWW")
				.group("botania:spreader")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.DREAMWOOD_LOGS))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.gaiaSpreader)
				.requires(BotaniaBlocks.elvenSpreader)
				.requires(BotaniaTags.Items.GEMS_DRAGONSTONE)
				.requires(BotaniaItems.lifeEssence)
				.group("botania:spreader")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.manaPool)
				.define('R', BotaniaBlocks.livingrock)
				.pattern("R R")
				.pattern("RRR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.dilutedPool)
				.define('R', BotaniaBlocks.livingrockSlab)
				.pattern("R R")
				.pattern("RRR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.fabulousPool)
				.define('R', BotaniaBlocks.shimmerrock)
				.pattern("R R")
				.pattern("RRR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.shimmerrock))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.rainbowRod))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.fabulousPool)
				.define('P', BotaniaBlocks.manaPool)
				.define('B', BotaniaBlocks.bifrostPerm)
				.pattern("BPB")
				.pattern("BBB")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.bifrostPerm))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.rainbowRod))
				.save(output, prefix(BuiltInRegistries.ITEM.getKey(BotaniaBlocks.fabulousPool.asItem()).getPath() + "_upgrade").toString());
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.runeAltar)
				.define('P', BotaniaItems.manaPearl)
				.define('S', BotaniaBlocks.livingrock)
				.pattern("SSS")
				.pattern("SPS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.runeAltar)
				.define('P', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('S', BotaniaBlocks.livingrock)
				.pattern("SSS")
				.pattern("SPS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output, prefix("runic_altar_alt").toString());
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.manaPylon)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('G', Items.GOLD_INGOT)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern(" G ")
				.pattern("MDM")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.naturaPylon)
				.define('P', BotaniaBlocks.manaPylon)
				.define('T', BotaniaTags.Items.NUGGETS_TERRASTEEL)
				.define('E', Items.ENDER_EYE)
				.pattern(" T ")
				.pattern("TPT")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.manaPylon))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.gaiaPylon)
				.define('P', BotaniaBlocks.manaPylon)
				.define('D', BotaniaItems.pixieDust)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern(" D ")
				.pattern("EPE")
				.pattern(" D ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.distributor)
				.define('R', BotaniaBlocks.livingrock)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("RRR")
				.pattern("S S")
				.pattern("RRR")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.manaVoid)
				.define('S', BotaniaBlocks.livingrock)
				.define('O', Items.OBSIDIAN)
				.pattern("SSS")
				.pattern("O O")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.manaDetector)
				.define('R', Items.REDSTONE)
				.define('T', Blocks.TARGET)
				.define('S', BotaniaBlocks.livingrock)
				.pattern("RSR")
				.pattern("STS")
				.pattern("RSR")
				.unlockedBy("has_item", conditionsFromItem(Blocks.TARGET))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.turntable)
				.define('P', Items.STICKY_PISTON)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern("WWW")
				.pattern("WPW")
				.pattern("WWW")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.STICKY_PISTON))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.tinyPlanet)
				.define('P', BotaniaItems.tinyPlanet)
				.define('S', Items.STONE)
				.pattern("SSS")
				.pattern("SPS")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.tinyPlanet))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.alchemyCatalyst)
				.define('P', BotaniaItems.manaPearl)
				.define('B', Items.BREWING_STAND)
				.define('S', BotaniaBlocks.livingrock)
				.define('G', Items.GOLD_INGOT)
				.pattern("SGS")
				.pattern("BPB")
				.pattern("SGS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.BREWING_STAND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.openCrate)
				.define('W', BotaniaBlocks.livingwoodPlanks)
				.pattern("WWW")
				.pattern("W W")
				.pattern("W W")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingwoodPlanks))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.craftCrate)
				.define('C', Items.CRAFTING_TABLE)
				.define('W', BotaniaBlocks.dreamwoodPlanks)
				.pattern("WCW")
				.pattern("W W")
				.pattern("W W")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.dreamwoodPlanks))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.forestEye)
				.define('S', BotaniaBlocks.livingrock)
				.define('E', Items.ENDER_EYE)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("MSM")
				.pattern("SES")
				.pattern("MSM")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.wildDrum)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.define('H', BotaniaItems.grassHorn)
				.define('L', Items.LEATHER)
				.pattern("WLW")
				.pattern("WHW")
				.pattern("WLW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassHorn))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.gatheringDrum)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('W', BotaniaTags.Items.DREAMWOOD_LOGS)
				.define('L', Items.LEATHER)
				.pattern("WLW")
				.pattern("WEW")
				.pattern("WLW")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.DREAMWOOD_LOGS))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.canopyDrum)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.define('H', BotaniaItems.leavesHorn)
				.define('L', Items.LEATHER)
				.pattern("WLW")
				.pattern("WHW")
				.pattern("WLW")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.leavesHorn))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.abstrusePlatform, 2)
				.define('0', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.define('P', BotaniaItems.manaPearl)
				.define('3', BotaniaBlocks.livingwoodFramed)
				.define('4', BotaniaBlocks.livingwoodPatternFramed)
				.pattern("343")
				.pattern("0P0")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.spectralPlatform, 2)
				.define('0', BotaniaTags.Items.DREAMWOOD_LOGS)
				.define('3', BotaniaBlocks.dreamwoodFramed)
				.define('4', BotaniaBlocks.dreamwoodPatternFramed)
				.define('D', BotaniaItems.pixieDust)
				.pattern("343")
				.pattern("0D0")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.alfPortal)
				.define('T', BotaniaTags.Items.NUGGETS_TERRASTEEL)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern("WTW")
				.pattern("WTW")
				.pattern("WTW")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.conjurationCatalyst)
				.define('P', BotaniaBlocks.alchemyCatalyst)
				.define('B', BotaniaItems.pixieDust)
				.define('S', BotaniaBlocks.livingrock)
				.define('G', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("SBS")
				.pattern("GPG")
				.pattern("SGS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.spawnerClaw)
				.define('P', Items.PRISMARINE_BRICKS)
				.define('B', Items.BLAZE_ROD)
				.define('S', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('E', BotaniaItems.enderAirBottle)
				.define('M', BotaniaTags.Items.BLOCKS_MANASTEEL)
				.pattern("BSB")
				.pattern("PMP")
				.pattern("PEP")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.enderAirBottle))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.enderEye)
				.define('R', Items.REDSTONE)
				.define('E', Items.ENDER_EYE)
				.define('O', Items.OBSIDIAN)
				.pattern("RER")
				.pattern("EOE")
				.pattern("RER")
				.unlockedBy("has_item", conditionsFromItem(Items.ENDER_EYE))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.starfield)
				.define('P', BotaniaItems.pixieDust)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('O', Items.OBSIDIAN)
				.pattern("EPE")
				.pattern("EOE")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.rfGenerator)
				.define('R', Items.REDSTONE_BLOCK)
				.define('S', BotaniaBlocks.livingrock)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("SRS")
				.pattern("RMR")
				.pattern("SRS")
				.unlockedBy("has_item", conditionsFromItem(Items.REDSTONE_BLOCK))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.brewery)
				.define('A', BotaniaItems.runeMana)
				.define('R', BotaniaBlocks.livingrock)
				.define('S', Items.BREWING_STAND)
				.define('M', BotaniaTags.Items.BLOCKS_MANASTEEL)
				.pattern("RSR")
				.pattern("RAR")
				.pattern("RMR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeMana))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.BREWING_STAND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.terraPlate)
				.define('0', BotaniaItems.runeWater)
				.define('1', BotaniaItems.runeFire)
				.define('2', BotaniaItems.runeEarth)
				.define('3', BotaniaItems.runeAir)
				.define('8', BotaniaItems.runeMana)
				.define('L', Blocks.LAPIS_BLOCK)
				.define('M', BotaniaTags.Items.BLOCKS_MANASTEEL)
				.pattern("LLL")
				.pattern("0M1")
				.pattern("283")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.RUNES))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.pump)
				.define('B', Items.BUCKET)
				.define('S', BotaniaBlocks.livingrock)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("SSS")
				.pattern("IBI")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.incensePlate)
				.define('S', BotaniaBlocks.livingwoodSlab)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern("SSW")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.hourglass)
				.define('R', Items.REDSTONE)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('G', Items.GOLD_INGOT)
				.define('M', BotaniaBlocks.manaGlass)
				.pattern("GMG")
				.pattern("RSR")
				.pattern("GMG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.manaGlass))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaBlocks.ghostRail)
				.requires(Items.RAIL)
				.requires(BotaniaBlocks.spectralPlatform)
				.unlockedBy("has_item", conditionsFromItem(Items.RAIL))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaBlocks.spectralPlatform))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.sparkChanger)
				.define('R', Items.REDSTONE)
				.define('S', BotaniaBlocks.livingrock)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("ESE")
				.pattern("SRS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.felPumpkin)
				.define('P', Items.PUMPKIN)
				.define('B', Items.BONE)
				.define('S', Items.STRING)
				.define('F', Items.ROTTEN_FLESH)
				.define('G', Items.GUNPOWDER)
				.pattern(" S ")
				.pattern("BPF")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromItem(Items.PUMPKIN))
				.save(output);
		AtomicReference<BotaniaFinishedRecipe> cocoonBase = new AtomicReference<>();
		AtomicReference<BotaniaFinishedRecipe> cocoonGog = new AtomicReference<>();
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaBlocks.cocoon)
				.define('S', Items.STRING)
				.define('C', BotaniaItems.manaweaveCloth)
				.define('P', BotaniaBlocks.felPumpkin)
				.define('D', BotaniaItems.pixieDust)
				.pattern("SSS")
				.pattern("CPC")
				.pattern("SDS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.felPumpkin))
				.save(capturing(cocoonBase::set));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaBlocks.cocoon)
				.define('S', Items.STRING)
				.define('P', BotaniaBlocks.felPumpkin)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("SSS")
				.pattern("SPS")
				.pattern("SIS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.felPumpkin))
				.save(capturing(cocoonGog::set));
		saveFinishedRecipe(output, new GogAlternationResult(cocoonGog.get(), cocoonBase.get()));

		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaBlocks.lightRelayDefault)
				.requires(BotaniaItems.redString)
				.requires(BotaniaTags.Items.GEMS_DRAGONSTONE)
				.requires(Items.GLOWSTONE_DUST)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_DRAGONSTONE))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaBlocks.lightRelayDetector)
				.requires(BotaniaBlocks.lightRelayDefault)
				.requires(Items.REDSTONE)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.lightRelayDefault))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaBlocks.lightRelayFork)
				.requires(BotaniaBlocks.lightRelayDefault)
				.requires(Items.REDSTONE_TORCH)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.lightRelayDefault))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaBlocks.lightRelayToggle)
				.requires(BotaniaBlocks.lightRelayDefault)
				.requires(Items.LEVER)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.lightRelayDefault))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.lightLauncher)
				.define('D', BotaniaTags.Items.DREAMWOOD_LOGS)
				.define('L', BotaniaBlocks.lightRelayDefault)
				.pattern("DDD")
				.pattern("DLD")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.lightRelayDefault))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaBlocks.manaBomb)
				.define('T', Items.TNT)
				.define('G', BotaniaItems.lifeEssence)
				.define('L', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern("LTL")
				.pattern("TGT")
				.pattern("LTL")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.bellows)
				.define('R', BotaniaItems.runeAir)
				.define('S', BotaniaBlocks.livingwoodSlab)
				.define('L', Items.LEATHER)
				.pattern("SSS")
				.pattern("RL ")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeAir))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.bifrostPerm)
				.requires(BotaniaItems.rainbowRod)
				.requires(BotaniaBlocks.elfGlass)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.rainbowRod))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.cellBlock, 3)
				.requires(Items.CACTUS, 3)
				.requires(Items.BEETROOT)
				.requires(Items.CARROT)
				.requires(Items.POTATO)
				.unlockedBy("has_item", conditionsFromItem(BotaniaFlowerBlocks.dandelifeon))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.teruTeruBozu)
				.define('C', BotaniaItems.manaweaveCloth)
				.define('S', Items.SUNFLOWER)
				.pattern("C")
				.pattern("C")
				.pattern("S")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaweaveCloth))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.avatar)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern(" W ")
				.pattern("WDW")
				.pattern("W W")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.animatedTorch)
				.define('D', BotaniaTags.Items.DUSTS_MANA)
				.define('T', Items.REDSTONE_TORCH)
				.pattern("D")
				.pattern("T")
				.unlockedBy("has_item", conditionsFromItem(Items.REDSTONE_TORCH))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.DUSTS_MANA))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.livingwoodTwig)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern(" W")
				.pattern("W ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.redstoneRoot)
				.requires(Items.REDSTONE)
				.requires(Ingredient.of(Items.FERN, Items.SHORT_GRASS))
				.unlockedBy("has_item", conditionsFromItem(Items.REDSTONE))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.dreamwoodTwig)
				.define('W', BotaniaTags.Items.DREAMWOOD_LOGS)
				.pattern(" W")
				.pattern("W ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.DREAMWOOD_LOGS))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.gaiaIngot)
				.define('S', BotaniaItems.lifeEssence)
				.define('I', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.pattern(" S ")
				.pattern("SIS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.manaweaveCloth)
				.define('S', BotaniaItems.manaString)
				.pattern("SS")
				.pattern("SS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaString))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.drySeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.DEAD_BUSH)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.goldenSeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.WHEAT)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.vividSeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.GREEN_DYE)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.scorchedSeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.BLAZE_POWDER)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.infusedSeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.PRISMARINE_SHARD)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.mutatedSeeds)
				.requires(BotaniaItems.grassSeeds)
				.requires(Items.SPIDER_EYE)
				.group("botania:seeds")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassSeeds))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.darkQuartz, 8)
				.define('Q', Items.QUARTZ)
				.define('C', Ingredient.of(Items.COAL, Items.CHARCOAL))
				.pattern("QQQ")
				.pattern("QCQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(Items.QUARTZ))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.blazeQuartz, 8)
				.define('Q', Items.QUARTZ)
				.define('C', Items.BLAZE_POWDER)
				.pattern("QQQ")
				.pattern("QCQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(Items.QUARTZ))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lavenderQuartz, 8)
				.define('Q', Items.QUARTZ)
				.define('C', Ingredient.of(Items.ALLIUM, Items.PINK_TULIP, Items.LILAC, Items.PEONY))
				.pattern("QQQ")
				.pattern("QCQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(Items.QUARTZ))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.redQuartz, 8)
				.define('Q', Items.QUARTZ)
				.define('C', Items.REDSTONE)
				.pattern("QQQ")
				.pattern("QCQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(Items.QUARTZ))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.sunnyQuartz, 8)
				.define('Q', Items.QUARTZ)
				.define('C', Items.SUNFLOWER)
				.pattern("QQQ")
				.pattern("QCQ")
				.pattern("QQQ")
				.unlockedBy("has_item", conditionsFromItem(Items.QUARTZ))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.vineBall)
				.define('V', Items.VINE)
				.pattern("VVV")
				.pattern("VVV")
				.pattern("VVV")
				.unlockedBy("has_item", conditionsFromItem(Items.VINE))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.necroVirus)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.vineBall)
				.requires(Items.MAGMA_CREAM)
				.requires(Items.FERMENTED_SPIDER_EYE)
				.requires(Items.ENDER_EYE)
				.requires(Items.ZOMBIE_HEAD)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.ZOMBIE_HEAD))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.nullVirus)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.vineBall)
				.requires(Items.MAGMA_CREAM)
				.requires(Items.FERMENTED_SPIDER_EYE)
				.requires(Items.ENDER_EYE)
				.requires(Items.SKELETON_SKULL)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.SKELETON_SKULL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaItems.spark)
				.define('P', BotaniaTags.Items.PETALS)
				.define('B', Items.BLAZE_POWDER)
				.define('N', Items.GOLD_NUGGET)
				.pattern(" P ")
				.pattern("BNB")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(Items.BLAZE_POWDER))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaItems.sparkUpgradeDispersive)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaTags.Items.INGOTS_MANASTEEL)
				.requires(BotaniaItems.runeWater)
				.group("botania:spark_upgrade")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.spark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.sparkUpgradeDominant)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaTags.Items.INGOTS_MANASTEEL)
				.requires(BotaniaItems.runeFire)
				.group("botania:spark_upgrade")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.spark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.sparkUpgradeRecessive)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaTags.Items.INGOTS_MANASTEEL)
				.requires(BotaniaItems.runeEarth)
				.group("botania:spark_upgrade")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.spark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.sparkUpgradeIsolated)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaTags.Items.INGOTS_MANASTEEL)
				.requires(BotaniaItems.runeAir)
				.group("botania:spark_upgrade")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.spark))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.vial, 3)
				.define('G', BotaniaBlocks.manaGlass)
				.pattern("G G")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.manaGlass))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaBlocks.brewery))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.flask, 3)
				.define('G', BotaniaBlocks.elfGlass)
				.pattern("G G")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.elfGlass))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.worldSeed, 4)
				.define('S', Items.WHEAT_SEEDS)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('G', Items.SHORT_GRASS)
				.pattern("G")
				.pattern("S")
				.pattern("D")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_DRAGONSTONE))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.thornChakram, 2)
				.define('T', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.define('V', Items.VINE)
				.pattern("VVV")
				.pattern("VTV")
				.pattern("VVV")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.flareChakram, 2)
				.define('P', BotaniaItems.pixieDust)
				.define('B', Items.BLAZE_POWDER)
				.define('C', BotaniaItems.thornChakram)
				.pattern("BBB")
				.pattern("CPC")
				.pattern("BBB")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.thornChakram))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.keepIvy)
				.requires(BotaniaItems.pixieDust)
				.requires(Items.VINE)
				.requires(BotaniaItems.enderAirBottle)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.enderAirBottle))
				.save(output);

	}

	private void registerMisc(RecipeOutput output) {
		Ingredient mushrooms = Ingredient.of(BotaniaBlocks.whiteMushroom, BotaniaBlocks.orangeMushroom,
				BotaniaBlocks.magentaMushroom, BotaniaBlocks.lightBlueMushroom, BotaniaBlocks.yellowMushroom,
				BotaniaBlocks.limeMushroom, BotaniaBlocks.pinkMushroom, BotaniaBlocks.grayMushroom, BotaniaBlocks.lightGrayMushroom,
				BotaniaBlocks.cyanMushroom, BotaniaBlocks.purpleMushroom, BotaniaBlocks.blueMushroom, BotaniaBlocks.brownMushroom,
				BotaniaBlocks.greenMushroom, BotaniaBlocks.redMushroom, BotaniaBlocks.blackMushroom);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.FOOD, Items.MUSHROOM_STEW)
				.requires(mushrooms, 2)
				.requires(Items.BOWL)
				.unlockedBy("has_item", conditionsFromItem(Items.BOWL))
				.unlockedBy("has_orig_recipe", RecipeUnlockedTrigger.unlocked(ResourceKey.create(Registries.RECIPE, Identifier.parse("mushroom_stew"))))
				.save(output, "botania:mushroom_stew");

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, Items.COBWEB)
				.define('S', Items.STRING)
				.define('M', BotaniaItems.manaString)
				.pattern("S S")
				.pattern(" M ")
				.pattern("S S")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaString))
				.save(output, prefix("cobweb").toString());

		petalApothecary(Items.COBBLESTONE, BotaniaBlocks.defaultAltar)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.PETALS))
				.save(output);
		petalApothecary(Items.MOSSY_COBBLESTONE, BotaniaBlocks.mossyAltar)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.PETALS))
				.save(output);
		petalApothecary(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockAltar)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		petalApothecary(Items.COBBLED_DEEPSLATE, BotaniaBlocks.deepslateAltar)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.PETALS))
				.save(output);
		for (String metamorphicVariant : LibBlockNames.METAMORPHIC_VARIANTS) {
			Block apothecary = getBlockOrThrow(prefix("apothecary_" + metamorphicVariant));
			Block cobble = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + metamorphicVariant + "_cobblestone"));
			petalApothecary(cobble, apothecary)
					.group("botania:metamorphic_apothecary")
					.unlockedBy("has_item", conditionsFromItem(cobble))
					.unlockedBy("has_flower_item", conditionsFromItem(BotaniaFlowerBlocks.marimorphosis))
					.save(output);
		}
		ColorHelper.supportedColors().forEach(color -> {
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.getShinyFlower(color))
					.requires(Items.GLOWSTONE_DUST)
					.requires(Items.GLOWSTONE_DUST)
					.requires(BotaniaBlocks.getFlower(color))
					.group("botania:shiny_flower")
					.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.getFlower(color)))
					.save(output);
			ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.getFloatingFlower(color))
					.define('S', BotaniaItems.grassSeeds)
					.define('D', Items.DIRT)
					.define('F', BotaniaBlocks.getShinyFlower(color))
					.pattern("F")
					.pattern("S")
					.pattern("D")
					.group("botania:floating_flowers")
					.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.getShinyFlower(color)))
					.save(output);
			ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.getPetalBlock(color))
					.define('P', BotaniaItems.getPetal(color))
					.pattern("PPP")
					.pattern("PPP")
					.pattern("PPP")
					.group("botania:petal_block")
					.unlockedBy("has_item", conditionsFromItem(BotaniaItems.getPetal(color)))
					.save(output);
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.getMushroom(color))
					.requires(Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM))
					.requires(Ingredient.of(dyeItem(color)))
					.group("botania:mushroom")
					.unlockedBy("has_item", conditionsFromItem(Items.RED_MUSHROOM))
					.unlockedBy("has_alt_item", conditionsFromItem(Items.BROWN_MUSHROOM))
					.save(output, "botania:mushroom_" + color.ordinal());
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.getPetal(color), 4)
					.requires(BotaniaBlocks.getDoubleFlower(color))
					.group("botania:petal_double")
					.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.getDoubleFlower(color)))
					.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.getPetal(color)))
					.save(output, "botania:petal_" + color.getName() + "_double");
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.getPetal(color), 2)
					.requires(BotaniaBlocks.getFlower(color))
					.group("botania:petal")
					.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.getFlower(color)))
					.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.getPetal(color)))
					.save(output, "botania:petal_" + color.getName());
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, dyeItem(color))
					.requires(IngredientHelper.of(BotaniaTags.Items.getPetalTag(color)))
					.group("botania:dye")
					.unlockedBy("has_item", conditionsFromItem(BotaniaItems.getPetal(color)))
					.save(output, "botania:dye_" + color.getName());
		});
	}

	private void registerTools(RecipeOutput output) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.lexicon)
				.requires(ItemTags.SAPLINGS)
				.requires(Items.BOOK)
				.unlockedBy("has_item", conditionsFromTag(ItemTags.SAPLINGS))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.BOOK))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.twigWand)
				.define('P', BotaniaTags.Items.PETALS)
				.define('S', BotaniaItems.livingwoodTwig)
				.pattern(" PS")
				.pattern(" SP")
				.pattern("S  ")
				.group("botania:twig_wand")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.PETALS))
				.save(WrapperResult.ofType(WandOfTheForestRecipe::wrap, output));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.dreamwoodWand)
				.define('P', BotaniaTags.Items.PETALS)
				.define('S', BotaniaItems.dreamwoodTwig)
				.pattern(" PS")
				.pattern(" SP")
				.pattern("S  ")
				.group("botania:twig_wand")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.PETALS))
				.save(WrapperResult.ofType(WandOfTheForestRecipe::wrap, output));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaTablet)
				.define('P', BotaniaItems.manaPearl)
				.define('S', BotaniaBlocks.livingrock)
				.pattern("SSS")
				.pattern("SPS")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaTablet)
				.define('P', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('S', BotaniaBlocks.livingrock)
				.pattern("SSS")
				.pattern("SPS")
				.pattern("SSS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output, prefix("mana_tablet_alt").toString());

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.cacophonium)
				.define('N', Items.NOTE_BLOCK)
				.define('G', Items.COPPER_INGOT)
				.pattern(" G ")
				.pattern("GNG")
				.pattern("GG ")
				.unlockedBy("has_item", conditionsFromItem(Items.NOTE_BLOCK))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.grassHorn)
				.define('S', BotaniaItems.grassSeeds)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.pattern(" W ")
				.pattern("WSW")
				.pattern("WW ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.LIVINGWOOD_LOGS))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.leavesHorn)
				.requires(BotaniaItems.grassHorn)
				.requires(ItemTags.LEAVES)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassHorn))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.snowHorn)
				.requires(BotaniaItems.grassHorn)
				.requires(Items.SNOWBALL)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.grassHorn))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaMirror)
				.define('P', BotaniaItems.manaPearl)
				.define('R', BotaniaBlocks.livingrock)
				.define('S', BotaniaItems.livingwoodTwig)
				.define('T', BotaniaItems.manaTablet)
				.define('I', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.pattern(" PR")
				.pattern(" SI")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaTablet))
				.unlockedBy("has_alt_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.openBucket)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("E E")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.spawnerMover)
				.define('A', BotaniaItems.enderAirBottle)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('E', BotaniaItems.lifeEssence)
				.define('I', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("EIE")
				.pattern("ADA")
				.pattern("EIE")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.slingshot)
				.define('A', BotaniaItems.runeAir)
				.define('T', BotaniaItems.livingwoodTwig)
				.pattern(" TA")
				.pattern(" TT")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeAir))
				.save(output);

		registerSimpleArmorSet(output, IngredientHelper.of(BotaniaTags.Items.INGOTS_MANASTEEL), "manasteel", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL));
		registerSimpleArmorSet(output, IngredientHelper.of(BotaniaTags.Items.INGOTS_ELEMENTIUM), "elementium", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM));
		registerSimpleArmorSet(output, Ingredient.of(BotaniaItems.manaweaveCloth), "manaweave", conditionsFromItem(BotaniaItems.manaweaveCloth));

		registerTerrasteelUpgradeRecipe(output, BotaniaItems.terrasteelHelm, BotaniaItems.manasteelHelm, BotaniaItems.runeSpring);
		registerTerrasteelUpgradeRecipe(output, BotaniaItems.terrasteelChest, BotaniaItems.manasteelChest, BotaniaItems.runeSummer);
		registerTerrasteelUpgradeRecipe(output, BotaniaItems.terrasteelLegs, BotaniaItems.manasteelLegs, BotaniaItems.runeAutumn);
		registerTerrasteelUpgradeRecipe(output, BotaniaItems.terrasteelBoots, BotaniaItems.manasteelBoots, BotaniaItems.runeWinter);

		registerToolSetRecipes(output, IngredientHelper.of(BotaniaTags.Items.INGOTS_MANASTEEL), Ingredient.of(BotaniaItems.livingwoodTwig),
				conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL), BotaniaItems.manasteelSword, BotaniaItems.manasteelPick, BotaniaItems.manasteelAxe,
				BotaniaItems.manasteelHoe, BotaniaItems.manasteelShovel, BotaniaItems.manasteelShears);
		registerToolSetRecipes(output, IngredientHelper.of(BotaniaTags.Items.INGOTS_ELEMENTIUM), Ingredient.of(BotaniaItems.dreamwoodTwig),
				conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM), BotaniaItems.elementiumSword, BotaniaItems.elementiumPick, BotaniaItems.elementiumAxe,
				BotaniaItems.elementiumHoe, BotaniaItems.elementiumShovel, BotaniaItems.elementiumShears);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.terraSword)
				.define('S', BotaniaItems.livingwoodTwig)
				.define('I', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.pattern("I")
				.pattern("I")
				.pattern("S")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.terraPick)
				.define('T', BotaniaItems.manaTablet)
				.define('I', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.define('L', BotaniaItems.livingwoodTwig)
				.pattern("ITI")
				.pattern("ILI")
				.pattern(" L ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(WrapperResult.ofType(ManaUpgradeRecipe::wrap, output));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.terraAxe)
				.define('S', BotaniaItems.livingwoodTwig)
				.define('T', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.define('G', Items.GLOWSTONE)
				.pattern("TTG")
				.pattern("TST")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.starSword)
				.define('A', BotaniaItems.enderAirBottle)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('T', BotaniaItems.terraSword)
				.define('I', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("  I")
				.pattern("AD ")
				.pattern("TA ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.terraAxe))
				.unlockedBy("has_terrasteel", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.thunderSword)
				.define('A', BotaniaItems.enderAirBottle)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('T', BotaniaItems.terraSword)
				.define('I', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("  I")
				.pattern("AD ")
				.pattern("TA ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.terraAxe))
				.unlockedBy("has_terrasteel", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.livingwoodBow)
				.define('S', BotaniaItems.manaString)
				.define('T', BotaniaItems.livingwoodTwig)
				.pattern(" TS")
				.pattern("T S")
				.pattern(" TS")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaString))
				.unlockedBy("has_twig", conditionsFromItem(BotaniaItems.livingwoodTwig))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.crystalBow)
				.define('S', BotaniaItems.manaString)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.pattern(" DS")
				.pattern("T S")
				.pattern(" DS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_DRAGONSTONE))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.enderDagger)
				.define('P', BotaniaItems.manaPearl)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('T', BotaniaItems.livingwoodTwig)
				.pattern("P")
				.pattern("S")
				.pattern("T")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.enderHand)
				.define('P', BotaniaItems.manaPearl)
				.define('E', Items.ENDER_CHEST)
				.define('L', Items.LEATHER)
				.define('O', Items.OBSIDIAN)
				.pattern("PLO")
				.pattern("LEL")
				.pattern("OL ")
				.unlockedBy("has_item", conditionsFromItem(Items.ENDER_CHEST))
				.unlockedBy("has_alt_item", conditionsFromItem(Items.ENDER_EYE))
				.save(output);

		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.placeholder, 32)
				.requires(Items.CRAFTING_TABLE)
				.requires(BotaniaBlocks.livingrock)
				.unlockedBy("has_dreamwood", conditionsFromTag(BotaniaTags.Items.DREAMWOOD_LOGS))
				.unlockedBy("has_crafty_crate", conditionsFromItem(BotaniaBlocks.craftCrate))
				.save(output);

		for (CraftyCratePattern pattern : CraftyCratePattern.values()) {
			if (pattern == CraftyCratePattern.NONE) {
				continue;
			}
			Item item = getItemOrThrow(prefix(LibItemNames.CRAFT_PATTERN_PREFIX + pattern.getSerializedName().split("_", 2)[1]));
			String s = pattern.openSlots.stream().map(bool -> bool ? "R" : "P").collect(Collectors.joining());
			ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, item)
					.define('P', BotaniaItems.placeholder)
					.define('R', Items.REDSTONE)
					.pattern(s.substring(0, 3))
					.pattern(s.substring(3, 6))
					.pattern(s.substring(6, 9))
					.group("botania:craft_pattern")
					.unlockedBy("has_item", conditionsFromItem(BotaniaItems.placeholder))
					.unlockedBy("has_crafty_crate", conditionsFromItem(BotaniaBlocks.craftCrate))
					.save(output);
		}

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaGun)
				.define('S', BotaniaBlocks.redstoneSpreader)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('T', Items.TNT)
				.define('W', BotaniaTags.Items.LIVINGWOOD_LOGS)
				.define('M', BotaniaItems.runeMana)
				.pattern("SMD")
				.pattern(" WT")
				.pattern("  W")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.dirtRod)
				.define('D', Items.DIRT)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('E', BotaniaItems.runeEarth)
				.pattern("  D")
				.pattern(" T ")
				.pattern("E  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeEarth))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.terraformRod)
				.define('A', BotaniaItems.runeAutumn)
				.define('R', BotaniaItems.dirtRod)
				.define('S', BotaniaItems.runeSpring)
				.define('T', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.define('G', BotaniaItems.grassSeeds)
				.define('W', BotaniaItems.runeWinter)
				.define('M', BotaniaItems.runeSummer)
				.pattern(" WT")
				.pattern("ARS")
				.pattern("GM ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.waterRod)
				.define('B', Ingredient.of(Items.POTION))
				.define('R', BotaniaItems.runeWater)
				.define('T', BotaniaItems.livingwoodTwig)
				.pattern("  B")
				.pattern(" T ")
				.pattern("R  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeWater))
				.save(WrapperResult.ofType(WaterBottleMatchingRecipe::wrap, output));

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.rainbowRod)
				.define('P', BotaniaItems.pixieDust)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern(" PD")
				.pattern(" EP")
				.pattern("E  ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_DRAGONSTONE))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.tornadoRod)
				.define('R', BotaniaItems.runeAir)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('F', Items.FEATHER)
				.pattern("  F")
				.pattern(" T ")
				.pattern("R  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeAir))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.fireRod)
				.define('R', BotaniaItems.runeFire)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('F', Items.BLAZE_POWDER)
				.pattern("  F")
				.pattern(" T ")
				.pattern("R  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeFire))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.skyDirtRod)
				.requires(BotaniaItems.dirtRod)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.runeAir)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.diviningRod)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.pattern(" TD")
				.pattern(" TT")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.gravityRod)
				.define('T', BotaniaItems.dreamwoodTwig)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('W', Items.WHEAT)
				.pattern(" TD")
				.pattern(" WT")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_DRAGONSTONE))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.missileRod)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('T', BotaniaItems.dreamwoodTwig)
				.define('G', BotaniaItems.lifeEssence)
				.pattern("GDD")
				.pattern(" TD")
				.pattern("T G")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.cobbleRod)
				.define('C', Items.COBBLESTONE)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('F', BotaniaItems.runeFire)
				.define('W', BotaniaItems.runeWater)
				.pattern(" FC")
				.pattern(" TW")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeFire))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.runeWater))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.smeltRod)
				.define('B', Items.BLAZE_ROD)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('F', BotaniaItems.runeFire)
				.pattern(" BF")
				.pattern(" TB")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeFire))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.exchangeRod)
				.define('R', BotaniaItems.runeSloth)
				.define('S', Items.STONE)
				.define('T', BotaniaItems.livingwoodTwig)
				.pattern(" SR")
				.pattern(" TS")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeSloth))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.laputaShard)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.define('A', BotaniaItems.runeAir)
				.define('S', BotaniaItems.lifeEssence)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('E', BotaniaItems.runeEarth)
				.define('F', BotaniaTags.Items.MUNDANE_FLOATING_FLOWERS)
				.pattern("SFS")
				.pattern("PDP")
				.pattern("ASE")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.craftingHalo)
				.define('P', BotaniaItems.manaPearl)
				.define('C', Items.CRAFTING_TABLE)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern(" P ")
				.pattern("ICI")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.clip)
				.define('D', BotaniaTags.Items.DREAMWOOD_LOGS)
				.pattern(" D ")
				.pattern("D D")
				.pattern("DD ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.DREAMWOOD_LOGS))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.spellCloth)
				.define('P', BotaniaItems.manaPearl)
				.define('C', BotaniaItems.manaweaveCloth)
				.pattern(" C ")
				.pattern("CPC")
				.pattern(" C ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaweaveCloth))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.flowerBag)
				.define('P', BotaniaTags.Items.PETALS)
				.define('W', ItemTags.WOOL)
				.pattern("WPW")
				.pattern("W W")
				.pattern(" W ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.MYSTICAL_FLOWERS))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TRANSPORTATION, BotaniaItems.poolMinecart)
				.requires(Items.MINECART)
				.requires(BotaniaBlocks.manaPool)
				.unlockedBy("has_item", conditionsFromItem(Items.MINECART))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.blackHoleTalisman)
				.define('A', BotaniaItems.enderAirBottle)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('G', BotaniaItems.lifeEssence)
				.pattern(" G ")
				.pattern("EAE")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.temperanceStone)
				.define('R', BotaniaItems.runeEarth)
				.define('S', Items.STONE)
				.pattern(" S ")
				.pattern("SRS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeEarth))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.incenseStick)
				.define('B', Items.BLAZE_POWDER)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('G', Items.GHAST_TEAR)
				.pattern("  G")
				.pattern(" B ")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromItem(Items.GHAST_TEAR))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.obedienceStick)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("  M")
				.pattern(" T ")
				.pattern("T  ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.slimeBottle)
				.define('S', Items.SLIME_BALL)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('G', BotaniaBlocks.elfGlass)
				.pattern("EGE")
				.pattern("ESE")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.autocraftingHalo)
				.requires(BotaniaItems.craftingHalo)
				.requires(BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.sextant)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern(" TI")
				.pattern(" TT")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.astrolabe)
				.define('D', BotaniaTags.Items.DREAMWOOD_LOGS)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('G', BotaniaItems.lifeEssence)
				.pattern(" EG")
				.pattern("EEE")
				.pattern("GED")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);

	}

	private void registerTrinkets(RecipeOutput output) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.tinyPlanet)
				.define('P', BotaniaItems.manaPearl)
				.define('S', Items.STONE)
				.define('L', BotaniaBlocks.livingrock)
				.pattern("LSL")
				.pattern("SPS")
				.pattern("LSL")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaRing)
				.define('T', BotaniaItems.manaTablet)
				.define('I', BotaniaItems.manaSteel)
				.pattern("TI ")
				.pattern("I I")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaTablet))
				.save(WrapperResult.ofType(ManaUpgradeRecipe::wrap, output));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.auraRing)
				.define('R', BotaniaItems.runeMana)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("RI ")
				.pattern("I I")
				.pattern(" I ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.runeMana))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.manaRingGreater)
				.requires(BotaniaTags.Items.INGOTS_TERRASTEEL)
				.requires(BotaniaItems.manaRing)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.terrasteel))
				.save(WrapperResult.ofType(ShapelessManaUpgradeRecipe::wrap, output));
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.auraRingGreater)
				.requires(BotaniaTags.Items.INGOTS_TERRASTEEL)
				.requires(BotaniaItems.auraRing)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.travelBelt)
				.define('A', BotaniaItems.runeAir)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('E', BotaniaItems.runeEarth)
				.define('L', Items.LEATHER)
				.pattern("EL ")
				.pattern("L L")
				.pattern("SLA")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.knockbackBelt)
				.define('A', BotaniaItems.runeFire)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('E', BotaniaItems.runeEarth)
				.define('L', Items.LEATHER)
				.pattern("AL ")
				.pattern("L L")
				.pattern("SLE")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.icePendant)
				.define('R', BotaniaItems.runeWater)
				.define('S', BotaniaItems.manaString)
				.define('W', BotaniaItems.runeWinter)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("WS ")
				.pattern("S S")
				.pattern("MSR")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.lavaPendant)
				.define('S', BotaniaItems.manaString)
				.define('D', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('F', BotaniaItems.runeFire)
				.define('M', BotaniaItems.runeSummer)
				.pattern("MS ")
				.pattern("S S")
				.pattern("DSF")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.magnetRing)
				.define('L', BotaniaItems.lensMagnet)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("LM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.waterRing)
				.define('P', Items.PUFFERFISH)
				.define('C', Items.COD)
				.define('H', Items.HEART_OF_THE_SEA)
				.define('W', BotaniaItems.runeWater)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("WMP")
				.pattern("MHM")
				.pattern("CM ")
				.unlockedBy("has_item", conditionsFromItem(Items.HEART_OF_THE_SEA))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.miningRing)
				.define('P', Items.GOLDEN_PICKAXE)
				.define('E', BotaniaItems.runeEarth)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("EMP")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.divaCharm)
				.define('P', BotaniaItems.tinyPlanet)
				.define('G', Items.GOLD_INGOT)
				.define('H', BotaniaItems.runePride)
				.define('L', BotaniaItems.lifeEssence)
				.pattern("LGP")
				.pattern(" HG")
				.pattern(" GL")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.flightTiara)
				.define('E', BotaniaItems.enderAirBottle)
				.define('F', Items.FEATHER)
				.define('I', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('L', BotaniaItems.lifeEssence)
				.pattern("LLL")
				.pattern("ILI")
				.pattern("FEF")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output, "botania:flighttiara_0");

		// Normal quartz and not Tags.Items.QUARTZ because the recipes conflict.
		Item[] items = { Items.QUARTZ, BotaniaItems.darkQuartz, BotaniaItems.manaQuartz, BotaniaItems.blazeQuartz,
				BotaniaItems.lavenderQuartz, BotaniaItems.redQuartz, BotaniaItems.elfQuartz, BotaniaItems.sunnyQuartz };
		for (int i = 0; i < items.length; i++) {
			int tiaraType = i + 1;
			ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.flightTiara)
					.requires(BotaniaItems.flightTiara)
					.requires(items[i])
					.group("botania:flight_tiara_wings")
					.unlockedBy("has_item", conditionsFromItem(BotaniaItems.flightTiara))
					.save(WrapperResult.ofType(recipe -> {
						CompoundTag tag = new CompoundTag();
						tag.putInt("variant", tiaraType);
						return NbtOutputRecipe.wrap(recipe, tag);
					}, output),
							"botania:flighttiara_" + tiaraType);
		}
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.pixieRing)
				.define('D', BotaniaItems.pixieDust)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("DE ")
				.pattern("E E")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.superTravelBelt)
				.define('S', BotaniaItems.travelBelt)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('L', BotaniaItems.lifeEssence)
				.pattern("E  ")
				.pattern(" S ")
				.pattern("L E")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.reachRing)
				.define('R', BotaniaItems.runePride)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.pattern("RE ")
				.pattern("E E")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.itemFinder)
				.define('E', Items.EMERALD)
				.define('I', Items.IRON_INGOT)
				.define('Y', Items.ENDER_EYE)
				.pattern(" I ")
				.pattern("IYI")
				.pattern("IEI")
				.unlockedBy("has_item", conditionsFromItem(Items.ENDER_EYE))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.superLavaPendant)
				.define('P', BotaniaItems.lavaPendant)
				.define('B', Items.BLAZE_ROD)
				.define('G', BotaniaItems.lifeEssence)
				.define('N', Items.NETHER_BRICK)
				.pattern("BBB")
				.pattern("BPB")
				.pattern("NGN")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.bloodPendant)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('G', Items.GHAST_TEAR)
				.pattern(" P ")
				.pattern("PGP")
				.pattern("DP ")
				.unlockedBy("has_item", conditionsFromItem(Items.GHAST_TEAR))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.holyCloak)
				.define('S', BotaniaItems.lifeEssence)
				.define('W', Items.WHITE_WOOL)
				.define('G', Items.GLOWSTONE_DUST)
				.pattern("WWW")
				.pattern("GWG")
				.pattern("GSG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.unholyCloak)
				.define('R', Items.REDSTONE)
				.define('S', BotaniaItems.lifeEssence)
				.define('W', Items.BLACK_WOOL)
				.pattern("WWW")
				.pattern("RWR")
				.pattern("RSR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, BotaniaItems.balanceCloak)
				.define('R', Items.EMERALD)
				.define('S', BotaniaItems.lifeEssence)
				.define('W', Items.LIGHT_GRAY_WOOL)
				.pattern("WWW")
				.pattern("RWR")
				.pattern("RSR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.monocle)
				.define('G', BotaniaBlocks.manaGlass)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('N', Items.GOLD_NUGGET)
				.pattern("GN")
				.pattern("IN")
				.pattern(" N")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.swapRing)
				.define('C', Items.CLAY)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("CM ")
				.pattern("M M")
				.pattern(" M ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.magnetRingGreater)
				.requires(BotaniaTags.Items.INGOTS_TERRASTEEL)
				.requires(BotaniaItems.magnetRing)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.magnetRing))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.speedUpBelt)
				.define('P', BotaniaItems.grassSeeds)
				.define('B', BotaniaItems.travelBelt)
				.define('S', Items.SUGAR)
				.define('M', Items.MAP)
				.pattern(" M ")
				.pattern("PBP")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromItem(Items.MAP))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.travelBelt))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.dodgeRing)
				.define('R', BotaniaItems.runeAir)
				.define('E', Items.EMERALD)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("EM ")
				.pattern("M M")
				.pattern(" MR")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.invisibilityCloak)
				.define('P', BotaniaItems.manaPearl)
				.define('C', Items.PRISMARINE_CRYSTALS)
				.define('W', Items.WHITE_WOOL)
				.define('G', BotaniaBlocks.manaGlass)
				.pattern("CWC")
				.pattern("GWG")
				.pattern("GPG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.cloudPendant)
				.define('S', BotaniaItems.manaString)
				.define('D', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('F', BotaniaItems.runeAir)
				.define('M', BotaniaItems.runeAutumn)
				.pattern("MS ")
				.pattern("S S")
				.pattern("DSF")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaString))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.superCloudPendant)
				.define('P', BotaniaItems.cloudPendant)
				.define('B', Items.GHAST_TEAR)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('G', BotaniaItems.lifeEssence)
				.define('N', Items.WHITE_WOOL)
				.pattern("BEB")
				.pattern("BPB")
				.pattern("NGN")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.cloudPendant))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.lifeEssence))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.thirdEye)
				.define('Q', Items.QUARTZ_BLOCK)
				.define('R', Items.GOLDEN_CARROT)
				.define('S', BotaniaItems.runeEarth)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.define('E', Items.ENDER_EYE)
				.pattern("RSR")
				.pattern("QEQ")
				.pattern("RDR")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.goddessCharm)
				.define('P', BotaniaTags.Items.PETALS_PINK)
				.define('A', BotaniaItems.runeWater)
				.define('S', BotaniaItems.runeSpring)
				.define('D', BotaniaTags.Items.GEMS_MANA_DIAMOND)
				.pattern(" P ")
				.pattern(" P ")
				.pattern("ADS")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.GEMS_MANA_DIAMOND))
				.save(output);

	}

	private void registerCorporeaAndRedString(RecipeOutput output) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.redString)
				.requires(Items.STRING)
				.requires(Items.REDSTONE_BLOCK)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.enderAirBottle)
				.group("botania:red_string")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.enderAirBottle))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.redString)
				.requires(Items.STRING)
				.requires(Items.REDSTONE_BLOCK)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.enderAirBottle)
				.requires(Items.PUMPKIN)
				.group("botania:red_string")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.enderAirBottle))
				.save(output, "botania:red_string_alt");
		registerRedStringBlock(output, BotaniaBlocks.redStringDispenser, Ingredient.of(Items.DISPENSER), conditionsFromItem(Items.DISPENSER));
		registerRedStringBlock(output, BotaniaBlocks.redStringFertilizer, Ingredient.of(BotaniaItems.fertilizer), conditionsFromItem(BotaniaItems.fertilizer));
		registerRedStringBlock(output, BotaniaBlocks.redStringComparator, Ingredient.of(Items.COMPARATOR), conditionsFromItem(Items.COMPARATOR));
		registerRedStringBlock(output, BotaniaBlocks.redStringRelay, Ingredient.of(BotaniaBlocks.manaSpreader), conditionsFromItem(BotaniaBlocks.manaSpreader));
		registerRedStringBlock(output, BotaniaBlocks.redStringInterceptor, Ingredient.of(Items.STONE_BUTTON), conditionsFromItem(Items.STONE_BUTTON));
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaItems.corporeaSpark, 4)
				.requires(BotaniaItems.spark)
				.requires(BotaniaItems.pixieDust)
				.requires(BotaniaItems.enderAirBottle)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.enderAirBottle))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.pixieDust))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaItems.corporeaSparkMaster)
				.requires(BotaniaItems.corporeaSpark)
				.requires(BotaniaTags.Items.GEMS_DRAGONSTONE)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaIndex)
				.define('A', BotaniaItems.enderAirBottle)
				.define('S', BotaniaItems.corporeaSpark)
				.define('D', BotaniaTags.Items.GEMS_DRAGONSTONE)
				.define('O', Items.OBSIDIAN)
				.pattern("AOA")
				.pattern("OSO")
				.pattern("DOD")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaFunnel)
				.requires(Items.DROPPER)
				.requires(BotaniaItems.corporeaSpark)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaInterceptor)
				.requires(Items.REDSTONE_BLOCK)
				.requires(BotaniaItems.corporeaSpark)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaCrystalCube)
				.define('C', BotaniaItems.corporeaSpark)
				.define('G', BotaniaBlocks.elfGlass)
				.define('W', BotaniaTags.Items.DREAMWOOD_LOGS)
				.pattern("C")
				.pattern("G")
				.pattern("W")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.corporeaBlock, 8)
				.requires(BotaniaBlocks.livingrockPolished)
				.requires(BotaniaItems.corporeaSpark)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		slabShape(BotaniaBlocks.corporeaSlab, BotaniaBlocks.corporeaBlock).save(output);
		stairs(BotaniaBlocks.corporeaStairs, BotaniaBlocks.corporeaBlock).save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.corporeaBrick, 4)
				.define('R', BotaniaBlocks.corporeaBlock)
				.pattern("RR")
				.pattern("RR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.corporeaBlock))
				.save(output);
		slabShape(BotaniaBlocks.corporeaBrickSlab, BotaniaBlocks.corporeaBrick).save(output);
		stairs(BotaniaBlocks.corporeaBrickStairs, BotaniaBlocks.corporeaBrick).save(output);
		wallShape(BotaniaBlocks.corporeaBrickWall, BotaniaBlocks.corporeaBrick, 6).save(output);
	}

	private void registerLenses(RecipeOutput output) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensSpeed)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeAir)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensPower)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeFire)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensTime)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeEarth)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensEfficiency)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeWater)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensBounce)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeSummer)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensGravity)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeWinter)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensMine)
				.define('P', Items.PISTON)
				.define('A', Items.LAPIS_LAZULI)
				.define('R', Items.REDSTONE)
				.define('L', BotaniaItems.lensNormal)
				.pattern(" P ")
				.pattern("ALA")
				.pattern(" R ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensDamage)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeWrath)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensPhantom)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaBlocks.abstrusePlatform)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensMagnet)
				.requires(BotaniaItems.lensNormal)
				.requires(Items.IRON_INGOT)
				.requires(Items.GOLD_INGOT)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensExplosive)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.runeEnvy)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensInfluence)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.define('R', BotaniaItems.runeAir)
				.define('L', BotaniaItems.lensNormal)
				.pattern("PRP")
				.pattern("PLP")
				.pattern("PPP")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensWeight)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.define('R', BotaniaItems.runeWater)
				.define('L', BotaniaItems.lensNormal)
				.pattern("PPP")
				.pattern("PLP")
				.pattern("PRP")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensPaint)
				.define('E', BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.define('W', ItemTags.WOOL)
				.define('L', BotaniaItems.lensNormal)
				.pattern(" E ")
				.pattern("WLW")
				.pattern(" E ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensFire)
				.requires(BotaniaItems.lensNormal)
				.requires(Items.FIRE_CHARGE)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensPiston)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaBlocks.pistonRelay)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensLight)
				.define('F', Items.FIRE_CHARGE)
				.define('G', Items.GLOWSTONE)
				.define('L', BotaniaItems.lensNormal)
				.pattern("GFG")
				.pattern("FLF")
				.pattern("GFG")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensLight)
				.define('F', Items.FIRE_CHARGE)
				.define('G', Items.GLOWSTONE)
				.define('L', BotaniaItems.lensNormal)
				.pattern("FGF")
				.pattern("GLG")
				.pattern("FGF")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output, "botania:lens_light_alt");
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensMessenger)
				.define('P', Items.PAPER)
				.define('L', BotaniaItems.lensNormal)
				.pattern(" P ")
				.pattern("PLP")
				.pattern(" P ")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.lensNormal))
				.save(output);

		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensWarp)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaItems.pixieDust)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.pixieDust))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensRedirect)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaTags.Items.LIVINGWOOD_LOGS)
				.requires(BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensFirework)
				.requires(BotaniaItems.lensNormal)
				.requires(Items.FIREWORK_ROCKET)
				.requires(BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensFlare)
				.requires(BotaniaItems.lensNormal)
				.requires(BotaniaBlocks.elfGlass)
				.requires(BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensTripwire)
				.requires(BotaniaItems.lensNormal)
				.requires(Items.TRIPWIRE_HOOK)
				.requires(BotaniaTags.Items.INGOTS_ELEMENTIUM)
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_ELEMENTIUM))
				.save(output);
	}

	private void registerFloatingFlowers(RecipeOutput output) {
		for (Block block : new Block[] {
				BotaniaFlowerBlocks.pureDaisy, BotaniaFlowerBlocks.manastar, BotaniaFlowerBlocks.hydroangeas, BotaniaFlowerBlocks.endoflame,
				BotaniaFlowerBlocks.thermalily, BotaniaFlowerBlocks.rosaArcana, BotaniaFlowerBlocks.munchdew, BotaniaFlowerBlocks.entropinnyum,
				BotaniaFlowerBlocks.kekimurus, BotaniaFlowerBlocks.gourmaryllis, BotaniaFlowerBlocks.narslimmus, BotaniaFlowerBlocks.spectrolus,
				BotaniaFlowerBlocks.dandelifeon, BotaniaFlowerBlocks.rafflowsia, BotaniaFlowerBlocks.shulkMeNot, BotaniaFlowerBlocks.bellethorn,
				BotaniaFlowerBlocks.bellethornChibi, BotaniaFlowerBlocks.bergamute, BotaniaFlowerBlocks.dreadthorn, BotaniaFlowerBlocks.heiseiDream,
				BotaniaFlowerBlocks.tigerseye, BotaniaFlowerBlocks.jadedAmaranthus, BotaniaFlowerBlocks.orechid, BotaniaFlowerBlocks.fallenKanade,
				BotaniaFlowerBlocks.exoflame, BotaniaFlowerBlocks.agricarnation, BotaniaFlowerBlocks.agricarnationChibi, BotaniaFlowerBlocks.hopperhock,
				BotaniaFlowerBlocks.hopperhockChibi, BotaniaFlowerBlocks.tangleberrie, BotaniaFlowerBlocks.tangleberrieChibi,
				BotaniaFlowerBlocks.jiyuulia, BotaniaFlowerBlocks.jiyuuliaChibi, BotaniaFlowerBlocks.rannuncarpus, BotaniaFlowerBlocks.rannuncarpusChibi,
				BotaniaFlowerBlocks.hyacidus, BotaniaFlowerBlocks.pollidisiac, BotaniaFlowerBlocks.clayconia,
				BotaniaFlowerBlocks.clayconiaChibi, BotaniaFlowerBlocks.loonium, BotaniaFlowerBlocks.daffomill, BotaniaFlowerBlocks.vinculotus,
				BotaniaFlowerBlocks.spectranthemum, BotaniaFlowerBlocks.medumone, BotaniaFlowerBlocks.marimorphosis, BotaniaFlowerBlocks.marimorphosisChibi,
				BotaniaFlowerBlocks.bubbell, BotaniaFlowerBlocks.bubbellChibi, BotaniaFlowerBlocks.solegnolia, BotaniaFlowerBlocks.solegnoliaChibi,
				BotaniaFlowerBlocks.orechidIgnem, BotaniaFlowerBlocks.labellia }) {
			createFloatingFlowerRecipe(output, block);
		}
	}

	private void registerConversions(RecipeOutput output) {
		compression(BotaniaItems.manaSteel, BotaniaTags.Items.NUGGETS_MANASTEEL)
				.save(output, prefix("conversions/manasteel_from_nuggets").toString());
		compression(BotaniaItems.elementium, BotaniaTags.Items.NUGGETS_ELEMENTIUM)
				.save(output, prefix("conversions/elementium_from_nuggets").toString());
		compression(BotaniaItems.terrasteel, BotaniaTags.Items.NUGGETS_TERRASTEEL)
				.save(output, prefix("conversions/terrasteel_from_nugget").toString());
		compression(BotaniaBlocks.manasteelBlock, BotaniaTags.Items.INGOTS_MANASTEEL).save(output);
		compression(BotaniaBlocks.terrasteelBlock, BotaniaTags.Items.INGOTS_TERRASTEEL).save(output);
		compression(BotaniaBlocks.elementiumBlock, BotaniaTags.Items.INGOTS_ELEMENTIUM).save(output);
		compression(BotaniaBlocks.manaDiamondBlock, BotaniaTags.Items.GEMS_MANA_DIAMOND).save(output);
		compression(BotaniaBlocks.dragonstoneBlock, BotaniaTags.Items.GEMS_DRAGONSTONE).save(output);

		AtomicReference<BotaniaFinishedRecipe> blazeBase = new AtomicReference<>();
		AtomicReference<BotaniaFinishedRecipe> blazeGog = new AtomicReference<>();
		compression(BotaniaBlocks.blazeBlock, Items.BLAZE_ROD).save(capturing(blazeBase::set));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.blazeBlock)
				.define('I', Items.BLAZE_POWDER)
				.pattern("III")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromItem(Items.BLAZE_POWDER))
				.save(capturing(blazeGog::set));
		saveFinishedRecipe(output, new GogAlternationResult(blazeGog.get(), blazeBase.get()));

		deconstructPetalBlock(output, BotaniaItems.whitePetal, BotaniaBlocks.petalBlockWhite);
		deconstructPetalBlock(output, BotaniaItems.orangePetal, BotaniaBlocks.petalBlockOrange);
		deconstructPetalBlock(output, BotaniaItems.magentaPetal, BotaniaBlocks.petalBlockMagenta);
		deconstructPetalBlock(output, BotaniaItems.lightBluePetal, BotaniaBlocks.petalBlockLightBlue);
		deconstructPetalBlock(output, BotaniaItems.yellowPetal, BotaniaBlocks.petalBlockYellow);
		deconstructPetalBlock(output, BotaniaItems.limePetal, BotaniaBlocks.petalBlockLime);
		deconstructPetalBlock(output, BotaniaItems.pinkPetal, BotaniaBlocks.petalBlockPink);
		deconstructPetalBlock(output, BotaniaItems.grayPetal, BotaniaBlocks.petalBlockGray);
		deconstructPetalBlock(output, BotaniaItems.lightGrayPetal, BotaniaBlocks.petalBlockSilver);
		deconstructPetalBlock(output, BotaniaItems.cyanPetal, BotaniaBlocks.petalBlockCyan);
		deconstructPetalBlock(output, BotaniaItems.purplePetal, BotaniaBlocks.petalBlockPurple);
		deconstructPetalBlock(output, BotaniaItems.bluePetal, BotaniaBlocks.petalBlockBlue);
		deconstructPetalBlock(output, BotaniaItems.brownPetal, BotaniaBlocks.petalBlockBrown);
		deconstructPetalBlock(output, BotaniaItems.greenPetal, BotaniaBlocks.petalBlockGreen);
		deconstructPetalBlock(output, BotaniaItems.redPetal, BotaniaBlocks.petalBlockRed);
		deconstructPetalBlock(output, BotaniaItems.blackPetal, BotaniaBlocks.petalBlockBlack);

		AtomicReference<BotaniaFinishedRecipe> blazeDeconBase = new AtomicReference<>();
		AtomicReference<BotaniaFinishedRecipe> blazeDeconGog = new AtomicReference<>();
		deconstruct(capturing(blazeDeconBase::set), Items.BLAZE_ROD, BotaniaBlocks.blazeBlock, "blazeblock_deconstruct");
		deconstruct(capturing(blazeDeconGog::set), Items.BLAZE_POWDER, BotaniaBlocks.blazeBlock, "blazeblock_deconstruct");
		saveFinishedRecipe(output, new GogAlternationResult(blazeDeconGog.get(), blazeDeconBase.get()));

		deconstruct(output, BotaniaItems.manaSteel, BotaniaTags.Items.BLOCKS_MANASTEEL, "manasteel_block_deconstruct");
		deconstruct(output, BotaniaItems.manaDiamond, BotaniaBlocks.manaDiamondBlock, "manadiamond_block_deconstruct");
		deconstruct(output, BotaniaItems.terrasteel, BotaniaTags.Items.BLOCKS_TERRASTEEL, "terrasteel_block_deconstruct");
		deconstruct(output, BotaniaItems.elementium, BotaniaTags.Items.BLOCKS_ELEMENTIUM, "elementium_block_deconstruct");
		deconstruct(output, BotaniaItems.dragonstone, BotaniaBlocks.dragonstoneBlock, "dragonstone_block_deconstruct");
		deconstruct(output, BotaniaItems.manasteelNugget, BotaniaTags.Items.INGOTS_MANASTEEL, "manasteel_to_nuggets");
		deconstruct(output, BotaniaItems.terrasteelNugget, BotaniaTags.Items.INGOTS_TERRASTEEL, "terrasteel_to_nugget");
		deconstruct(output, BotaniaItems.elementiumNugget, BotaniaTags.Items.INGOTS_ELEMENTIUM, "elementium_to_nuggets");

		recombineSlab(output, BotaniaBlocks.livingrock, BotaniaBlocks.livingrockSlab);
		recombineSlab(output, BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockPolishedSlab);
		recombineSlab(output, BotaniaBlocks.livingrockBrick, BotaniaBlocks.livingrockBrickSlab);
		recombineSlab(output, BotaniaBlocks.livingwood, BotaniaBlocks.livingwoodSlab);
		recombineSlab(output, BotaniaBlocks.livingwoodPlanks, BotaniaBlocks.livingwoodPlankSlab);
		recombineSlab(output, BotaniaBlocks.dreamwood, BotaniaBlocks.dreamwoodSlab);
		recombineSlab(output, BotaniaBlocks.dreamwoodPlanks, BotaniaBlocks.dreamwoodPlankSlab);
		recombineSlab(output, BotaniaBlocks.shimmerrock, BotaniaBlocks.shimmerrockSlab);
		recombineSlab(output, BotaniaBlocks.shimmerwoodPlanks, BotaniaBlocks.shimmerwoodPlankSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneForest, BotaniaBlocks.biomeStoneForestSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickForest, BotaniaBlocks.biomeBrickForestSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneForest, BotaniaBlocks.biomeCobblestoneForestSlab);
		recombineSlab(output, BotaniaBlocks.biomeStonePlains, BotaniaBlocks.biomeStonePlainsSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickPlains, BotaniaBlocks.biomeBrickPlainsSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestonePlains, BotaniaBlocks.biomeCobblestonePlainsSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneMountain, BotaniaBlocks.biomeStoneMountainSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickMountain, BotaniaBlocks.biomeBrickMountainSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneMountain, BotaniaBlocks.biomeCobblestoneMountainSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneFungal, BotaniaBlocks.biomeStoneFungalSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickFungal, BotaniaBlocks.biomeBrickFungalSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneFungal, BotaniaBlocks.biomeCobblestoneFungalSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneSwamp, BotaniaBlocks.biomeStoneSwampSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickSwamp, BotaniaBlocks.biomeBrickSwampSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneSwamp, BotaniaBlocks.biomeCobblestoneSwampSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneDesert, BotaniaBlocks.biomeStoneDesertSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickDesert, BotaniaBlocks.biomeBrickDesertSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneDesert, BotaniaBlocks.biomeCobblestoneDesertSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneTaiga, BotaniaBlocks.biomeStoneTaigaSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickTaiga, BotaniaBlocks.biomeBrickTaigaSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneTaiga, BotaniaBlocks.biomeCobblestoneTaigaSlab);
		recombineSlab(output, BotaniaBlocks.biomeStoneMesa, BotaniaBlocks.biomeStoneMesaSlab);
		recombineSlab(output, BotaniaBlocks.biomeBrickMesa, BotaniaBlocks.biomeBrickMesaSlab);
		recombineSlab(output, BotaniaBlocks.biomeCobblestoneMesa, BotaniaBlocks.biomeCobblestoneMesaSlab);
		recombineSlab(output, BotaniaBlocks.whitePavement, BotaniaBlocks.whitePavementSlab);
		recombineSlab(output, BotaniaBlocks.blackPavement, BotaniaBlocks.blackPavementSlab);
		recombineSlab(output, BotaniaBlocks.bluePavement, BotaniaBlocks.bluePavementSlab);
		recombineSlab(output, BotaniaBlocks.yellowPavement, BotaniaBlocks.yellowPavementSlab);
		recombineSlab(output, BotaniaBlocks.redPavement, BotaniaBlocks.redPavementSlab);
		recombineSlab(output, BotaniaBlocks.greenPavement, BotaniaBlocks.greenPavementSlab);
		recombineSlab(output, BotaniaBlocks.darkQuartz, BotaniaBlocks.darkQuartzSlab);
		recombineSlab(output, BotaniaBlocks.manaQuartz, BotaniaBlocks.manaQuartzSlab);
		recombineSlab(output, BotaniaBlocks.blazeQuartz, BotaniaBlocks.blazeQuartzSlab);
		recombineSlab(output, BotaniaBlocks.lavenderQuartz, BotaniaBlocks.lavenderQuartzSlab);
		recombineSlab(output, BotaniaBlocks.redQuartz, BotaniaBlocks.redQuartzSlab);
		recombineSlab(output, BotaniaBlocks.elfQuartz, BotaniaBlocks.elfQuartzSlab);
		recombineSlab(output, BotaniaBlocks.sunnyQuartz, BotaniaBlocks.sunnyQuartzSlab);
		recombineSlab(output, BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaSlab);
		recombineSlab(output, BotaniaBlocks.corporeaBrick, BotaniaBlocks.corporeaBrickSlab);
	}

	private void registerDecor(RecipeOutput output) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockPolished, 4)
				.define('R', BotaniaBlocks.livingrock)
				.pattern("RR")
				.pattern("RR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockSlate)
				.define('R', BotaniaBlocks.livingrockSlab)
				.pattern("R")
				.pattern("R")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockBrick, 4)
				.define('R', BotaniaBlocks.livingrockPolished)
				.pattern("RR")
				.pattern("RR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrockPolished))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockBrickChiseled)
				.define('R', BotaniaBlocks.livingrockBrickSlab)
				.pattern("R")
				.pattern("R")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrockBrick))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockBrickMossy)
				.requires(BotaniaBlocks.livingrockBrick)
				.requires(Items.MOSS_BLOCK)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrockBrick))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.livingrockBrickMossy)
				.requires(BotaniaBlocks.livingrockBrick)
				.requires(Items.VINE)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrockBrick))
				.save(output, "botania:mossy_livingrock_bricks_vine");
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.shimmerrock)
				.requires(BotaniaBlocks.livingrock)
				.requires(BotaniaBlocks.bifrostPerm)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.bifrostPerm))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.rainbowRod))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.shimmerwoodPlanks)
				.requires(BotaniaBlocks.dreamwoodPlanks)
				.requires(BotaniaBlocks.bifrostPerm)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.bifrostPerm))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaItems.rainbowRod))
				.save(output);

		registerForQuartz(output, LibBlockNames.QUARTZ_DARK, BotaniaItems.darkQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_MANA, BotaniaItems.manaQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_BLAZE, BotaniaItems.blazeQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_LAVENDER, BotaniaItems.lavenderQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_RED, BotaniaItems.redQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_ELF, BotaniaItems.elfQuartz);
		registerForQuartz(output, LibBlockNames.QUARTZ_SUNNY, BotaniaItems.sunnyQuartz);

		registerForWood(output, LibBlockNames.LIVING_WOOD);
		registerForWood(output, LibBlockNames.DREAM_WOOD);

		stairs(BotaniaBlocks.livingrockStairs, BotaniaBlocks.livingrock).save(output);
		slabShape(BotaniaBlocks.livingrockSlab, BotaniaBlocks.livingrock).save(output);
		wallShape(BotaniaBlocks.livingrockWall, BotaniaBlocks.livingrock, 6).save(output);

		stairs(BotaniaBlocks.livingrockPolishedStairs, BotaniaBlocks.livingrockPolished).save(output);
		slabShape(BotaniaBlocks.livingrockPolishedSlab, BotaniaBlocks.livingrockPolished).save(output);
		wallShape(BotaniaBlocks.livingrockPolishedWall, BotaniaBlocks.livingrockPolished, 6).save(output);

		stairs(BotaniaBlocks.livingrockBrickStairs, BotaniaBlocks.livingrockBrick).save(output);
		slabShape(BotaniaBlocks.livingrockBrickSlab, BotaniaBlocks.livingrockBrick).save(output);
		wallShape(BotaniaBlocks.livingrockBrickWall, BotaniaBlocks.livingrockBrick, 6).save(output);

		stairs(BotaniaBlocks.livingrockBrickMossyStairs, BotaniaBlocks.livingrockBrickMossy).save(output);
		slabShape(BotaniaBlocks.livingrockBrickMossySlab, BotaniaBlocks.livingrockBrickMossy).save(output);
		wallShape(BotaniaBlocks.livingrockBrickMossyWall, BotaniaBlocks.livingrockBrickMossy, 6).save(output);

		stairs(BotaniaBlocks.shimmerrockStairs, BotaniaBlocks.shimmerrock).save(output);
		slabShape(BotaniaBlocks.shimmerrockSlab, BotaniaBlocks.shimmerrock).save(output);
		stairs(BotaniaBlocks.shimmerwoodPlankStairs, BotaniaBlocks.shimmerwoodPlanks).save(output);
		slabShape(BotaniaBlocks.shimmerwoodPlankSlab, BotaniaBlocks.shimmerwoodPlanks).save(output);

		for (String variant : LibBlockNames.METAMORPHIC_VARIANTS) {
			registerForMetamorphic(output, variant);
		}

		Item[] pavementIngredients = { Items.AIR, Items.COAL, Items.LAPIS_LAZULI, Items.REDSTONE, Items.WHEAT, Items.SLIME_BALL };
		for (int i = 0; i < pavementIngredients.length; i++) {
			registerForPavement(output, LibBlockNames.PAVEMENT_VARIANTS[i], pavementIngredients[i]);
		}

		wallShape(BotaniaBlocks.managlassPane, BotaniaBlocks.manaGlass, 16).save(output);
		wallShape(BotaniaBlocks.alfglassPane, BotaniaBlocks.elfGlass, 16).save(output);
		wallShape(BotaniaBlocks.bifrostPane, BotaniaBlocks.bifrostPerm, 16).save(output);

		// azulejo0 recipe in loader-specific datagen

		List<Item> allAzulejos = IntStream.range(0, 16).mapToObj(i -> "azulejo_" + i)
				.map(ResourceLocationHelper::prefix)
				.map(BuiltInRegistries.ITEM::get)
				.flatMap(Optional::stream)
				.map(Holder.Reference::value)
				.toList();
		for (int i = 0; i < allAzulejos.size(); i++) {
			int resultIndex = (i + 1) % allAzulejos.size();
			ShapelessRecipeBuilder builder = ShapelessRecipeBuilder
					.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, allAzulejos.get(resultIndex))
					.requires(allAzulejos.get(i))
					.unlockedBy("has_azulejo", conditionsFromItem(BotaniaBlocks.azulejo0))
					.group("botania:azulejo_cycling");
			// 26.1 rejects an explicit id that equals the default one (the result item's id), which
			// holds for every non-alt step of the cycle; only the azulejo_0 step needs an override.
			if (resultIndex == 0) {
				builder.save(output, prefix("azulejo_0_alt").toString());
			} else {
				builder.save(output);
			}
		}

		cosmeticBauble(output, BotaniaItems.blackBowtie, BotaniaItems.whitePetal);
		cosmeticBauble(output, BotaniaItems.blackTie, BotaniaItems.orangePetal);
		cosmeticBauble(output, BotaniaItems.redGlasses, BotaniaItems.magentaPetal);
		cosmeticBauble(output, BotaniaItems.puffyScarf, BotaniaItems.lightBluePetal);
		cosmeticBauble(output, BotaniaItems.engineerGoggles, BotaniaItems.yellowPetal);
		cosmeticBauble(output, BotaniaItems.eyepatch, BotaniaItems.limePetal);
		cosmeticBauble(output, BotaniaItems.wickedEyepatch, BotaniaItems.pinkPetal);
		cosmeticBauble(output, BotaniaItems.redRibbons, BotaniaItems.grayPetal);
		cosmeticBauble(output, BotaniaItems.pinkFlowerBud, BotaniaItems.lightGrayPetal);
		cosmeticBauble(output, BotaniaItems.polkaDottedBows, BotaniaItems.cyanPetal);
		cosmeticBauble(output, BotaniaItems.blueButterfly, BotaniaItems.purplePetal);
		cosmeticBauble(output, BotaniaItems.catEars, BotaniaItems.bluePetal);
		cosmeticBauble(output, BotaniaItems.witchPin, BotaniaItems.brownPetal);
		cosmeticBauble(output, BotaniaItems.devilTail, BotaniaItems.greenPetal);
		cosmeticBauble(output, BotaniaItems.kamuiEye, BotaniaItems.redPetal);
		cosmeticBauble(output, BotaniaItems.googlyEyes, BotaniaItems.blackPetal);
		cosmeticBauble(output, BotaniaItems.fourLeafClover, Items.WHITE_DYE);
		cosmeticBauble(output, BotaniaItems.clockEye, Items.ORANGE_DYE);
		cosmeticBauble(output, BotaniaItems.unicornHorn, Items.MAGENTA_DYE);
		cosmeticBauble(output, BotaniaItems.devilHorns, Items.LIGHT_BLUE_DYE);
		cosmeticBauble(output, BotaniaItems.hyperPlus, Items.YELLOW_DYE);
		cosmeticBauble(output, BotaniaItems.botanistEmblem, Items.LIME_DYE);
		cosmeticBauble(output, BotaniaItems.ancientMask, Items.PINK_DYE);
		cosmeticBauble(output, BotaniaItems.eerieMask, Items.GRAY_DYE);
		cosmeticBauble(output, BotaniaItems.alienAntenna, Items.LIGHT_GRAY_DYE);
		cosmeticBauble(output, BotaniaItems.anaglyphGlasses, Items.CYAN_DYE);
		cosmeticBauble(output, BotaniaItems.orangeShades, Items.PURPLE_DYE);
		cosmeticBauble(output, BotaniaItems.grouchoGlasses, Items.BLUE_DYE);
		cosmeticBauble(output, BotaniaItems.thickEyebrows, Items.BROWN_DYE);
		cosmeticBauble(output, BotaniaItems.lusitanicShield, Items.GREEN_DYE);
		cosmeticBauble(output, BotaniaItems.tinyPotatoMask, Items.RED_DYE);
		cosmeticBauble(output, BotaniaItems.questgiverMark, Items.BLACK_DYE);
		cosmeticBauble(output, BotaniaItems.thinkingHand, BotaniaBlocks.tinyPotato);
	}

	protected void registerSimpleArmorSet(RecipeOutput output, Ingredient item, String variant,
			Criterion<?> criterion) {
		Item helmet = getItemOrThrow(prefix(variant + "_helmet"));
		Item chestplate = getItemOrThrow(prefix(variant + "_chestplate"));
		Item leggings = getItemOrThrow(prefix(variant + "_leggings"));
		Item boots = getItemOrThrow(prefix(variant + "_boots"));
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, helmet)
				.define('S', item)
				.pattern("SSS")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, chestplate)
				.define('S', item)
				.pattern("S S")
				.pattern("SSS")
				.pattern("SSS")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, leggings)
				.define('S', item)
				.pattern("SSS")
				.pattern("S S")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, boots)
				.define('S', item)
				.pattern("S S")
				.pattern("S S")
				.unlockedBy("has_item", criterion)
				.save(output);
	}

	protected void registerToolSetRecipes(RecipeOutput output, Ingredient item, Ingredient stick,
			Criterion<?> criterion, ItemLike sword, ItemLike pickaxe,
			ItemLike axe, ItemLike hoe, ItemLike shovel, ItemLike shears) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, pickaxe)
				.define('S', item)
				.define('T', stick)
				.pattern("SSS")
				.pattern(" T ")
				.pattern(" T ")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, shovel)
				.define('S', item)
				.define('T', stick)
				.pattern("S")
				.pattern("T")
				.pattern("T")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, axe)
				.define('S', item)
				.define('T', stick)
				.pattern("SS")
				.pattern("ST")
				.pattern(" T")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, hoe)
				.define('S', item)
				.define('T', stick)
				.pattern("SS")
				.pattern(" T")
				.pattern(" T")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, sword)
				.define('S', item)
				.define('T', stick)
				.pattern("S")
				.pattern("S")
				.pattern("T")
				.unlockedBy("has_item", criterion)
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, shears)
				.define('S', item)
				.pattern(" S")
				.pattern("S ")
				.unlockedBy("has_item", criterion)
				.save(output);

	}

	protected void registerTerrasteelUpgradeRecipe(RecipeOutput output, ItemLike result,
			ItemLike upgradedInput, ItemLike runeInput) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.COMBAT, result)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('S', BotaniaTags.Items.INGOTS_TERRASTEEL)
				.define('R', runeInput)
				.define('A', upgradedInput)
				.pattern("TRT")
				.pattern("SAS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_TERRASTEEL))
				.unlockedBy("has_prev_tier", conditionsFromItem(upgradedInput))
				.save(WrapperResult.ofType(ArmorUpgradeRecipe::wrap, output));
	}

	public static void registerRedStringBlock(RecipeOutput output, ItemLike result, Ingredient input, Criterion<?> criterion) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, result)
				.define('R', BotaniaBlocks.livingrock)
				.define('S', BotaniaItems.redString)
				.define('M', input)
				.pattern("RRR")
				.pattern("RMS")
				.pattern("RRR")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.redString))
				.unlockedBy("has_base_block", criterion)
				.save(output);
	}

	protected void createFloatingFlowerRecipe(RecipeOutput output, ItemLike input) {
		Identifier inputName = BuiltInRegistries.ITEM.getKey(input.asItem());
		Item result = getItemOrThrow(Identifier.fromNamespaceAndPath(inputName.getNamespace(), "floating_" + inputName.getPath()));
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, result)
				.requires(BotaniaTags.Items.FLOATING_FLOWERS)
				.requires(input)
				.group("botania:floating_flower")
				.unlockedBy("has_item", conditionsFromItem(input))
				.save(output);
	}

	protected void deconstruct(RecipeOutput output, ItemLike result, ItemLike input, String name) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, result, 9)
				.unlockedBy("has_item", conditionsFromItem(result))
				.requires(input)
				.save(output, prefix("conversions/" + name).toString());
	}

	protected void deconstruct(RecipeOutput output, ItemLike result, TagKey<Item> input, String name) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, result, 9)
				.unlockedBy("has_item", conditionsFromItem(result))
				.requires(input)
				.save(output, prefix("conversions/" + name).toString());
	}

	protected void deconstructPetalBlock(RecipeOutput output, ItemLike result, ItemLike input) {
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, result, 9)
				.unlockedBy("has_item", conditionsFromItem(result))
				.requires(input).group("botania:petal_block_deconstruct")
				.save(output, prefix("conversions/" + BuiltInRegistries.ITEM.getKey(input.asItem()).getPath() + "_deconstruct").toString());
	}

	protected void recombineSlab(RecipeOutput output, ItemLike fullBlock, ItemLike slab) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, fullBlock)
				.define('Q', slab)
				.pattern("QQ")
				.unlockedBy("has_item", conditionsFromItem(slab))
				.save(output, prefix("slab_recombine/" + BuiltInRegistries.ITEM.getKey(fullBlock.asItem()).getPath()).toString());
	}

	protected ShapedRecipeBuilder petalApothecary(ItemLike block, ItemLike apothecary) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, apothecary)
				.define('P', BotaniaTags.Items.PETALS)
				.define('C', block)
				.pattern("CPC")
				.pattern(" C ")
				.pattern("CCC");
	}

	protected void registerForQuartz(RecipeOutput output, String variant, ItemLike baseItem) {
		Block base = getBlockOrThrow(prefix(variant));
		Block slab = getBlockOrThrow(prefix(variant + LibBlockNames.SLAB_SUFFIX));
		Block stairs = getBlockOrThrow(prefix(variant + LibBlockNames.STAIR_SUFFIX));
		Block chiseled = getBlockOrThrow(prefix("chiseled_" + variant));
		Block pillar = getBlockOrThrow(prefix(variant + "_pillar"));

		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, base)
				.define('Q', baseItem)
				.pattern("QQ")
				.pattern("QQ")
				.group("botania:quartz_block")
				.unlockedBy("has_item", conditionsFromItem(baseItem))
				.save(output);
		stairs(stairs, base).group("botania:quartz_stairs").save(output);
		slabShape(slab, base).group("botania:quartz_slab").save(output);
		pillar(pillar, base).group("botania:quartz_pillar").save(output);
		chiseled(chiseled, slab).group("botania:quartz_chiseled")
				.unlockedBy("has_base_item", conditionsFromItem(base)).save(output);
	}

	protected void registerForWood(RecipeOutput output, String variant) {

		TagKey<Item> tag = variant.contains("livingwood") ? BotaniaTags.Items.LIVINGWOOD_LOGS : BotaniaTags.Items.DREAMWOOD_LOGS;
		Block log = getBlockOrThrow(prefix(variant + "_log"));
		Block wood = getBlockOrThrow(prefix(variant));
		Block strippedLog = getBlockOrThrow(prefix("stripped_" + variant + "_log"));
		Block strippedWood = getBlockOrThrow(prefix("stripped_" + variant));
		Block glimmeringLog = getBlockOrThrow(prefix("glimmering_" + variant + "_log"));
		Block glimmeringWood = getBlockOrThrow(prefix("glimmering_" + variant));
		Block glimmeringStrippedLog = getBlockOrThrow(prefix("glimmering_stripped_" + variant + "_log"));
		Block glimmeringStrippedWood = getBlockOrThrow(prefix("glimmering_stripped_" + variant));
		Block stairs = getBlockOrThrow(prefix(variant + "_stairs"));
		Block slab = getBlockOrThrow(prefix(variant + "_slab"));
		Block wall = getBlockOrThrow(prefix(variant + "_wall"));
		Block strippedStairs = getBlockOrThrow(prefix("stripped_" + variant + "_stairs"));
		Block strippedSlab = getBlockOrThrow(prefix("stripped_" + variant + "_slab"));
		Block strippedWall = getBlockOrThrow(prefix("stripped_" + variant + "_wall"));

		Block planks = getBlockOrThrow(prefix(variant + "_planks"));
		Block planksStairs = getBlockOrThrow(prefix(variant + "_planks_stairs"));
		Block planksSlab = getBlockOrThrow(prefix(variant + "_planks_slab"));
		Block mossyPlanks = getBlockOrThrow(prefix("mossy_" + variant + "_planks"));
		Block framed = getBlockOrThrow(prefix("framed_" + variant));
		Block patternFramed = getBlockOrThrow(prefix("pattern_framed_" + variant));
		Block fence = getBlockOrThrow(prefix(variant + "_fence"));
		Block fenceGate = getBlockOrThrow(prefix(variant + "_fence_gate"));

		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, planks, 4).requires(tag).group("planks")
				.unlockedBy("has_item", conditionsFromTag(tag)).save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, wood, 3).group("wood").unlockedBy("has_log", conditionsFromItem(log))
				.define('#', log)
				.pattern("##")
				.pattern("##")
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, strippedWood, 3).group("wood").unlockedBy("has_log", conditionsFromItem(strippedLog))
				.define('#', strippedLog)
				.pattern("##")
				.pattern("##")
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringLog).group("botania:glimmering_" + variant)
				.requires(log)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromItem(log))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringWood).group("botania:glimmering_" + variant)
				.requires(wood)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromItem(wood))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringStrippedLog).group("botania:glimmering_" + variant)
				.requires(strippedLog)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromItem(strippedLog))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringStrippedWood).group("botania:glimmering_" + variant)
				.requires(strippedWood)
				.requires(Items.GLOWSTONE_DUST)
				.unlockedBy("has_item", conditionsFromItem(strippedWood))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringWood, 3).group("botania:glimmering_" + variant)
				.unlockedBy("has_log", conditionsFromItem(glimmeringLog))
				.define('#', glimmeringLog)
				.pattern("##")
				.pattern("##")
				.save(output, prefix("glimmering_" + variant + "_from_log").toString());
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, glimmeringStrippedWood, 3).group("botania:glimmering_" + variant)
				.unlockedBy("has_log", conditionsFromItem(glimmeringStrippedLog))
				.define('#', glimmeringStrippedLog)
				.pattern("##")
				.pattern("##")
				.save(output, prefix("glimmering_stripped_" + variant + "_from_log").toString());

		stairs(stairs, wood).save(output);
		slabShape(slab, wood).save(output);
		wallShape(wall, wood, 6).save(output);
		fence(fence, planks).save(output);
		fenceGate(fenceGate, planks).save(output);

		stairs(strippedStairs, strippedWood).save(output);
		slabShape(strippedSlab, strippedWood).save(output);
		wallShape(strippedWall, strippedWood, 6).save(output);

		stairs(planksStairs, planks).save(output);
		slabShape(planksSlab, planks).save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, mossyPlanks)
				.requires(planks)
				.requires(Items.MOSS_BLOCK)
				.unlockedBy("has_item", conditionsFromItem(planks))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, mossyPlanks)
				.requires(planks)
				.requires(Items.VINE)
				.unlockedBy("has_item", conditionsFromItem(planks))
				.save(output, prefix("mossy_" + variant + "_planks_vine").toString());
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, framed, 4)
				.define('W', planks)
				.pattern("W W")
				.pattern("W W")
				.unlockedBy("has_item", conditionsFromItem(planks))
				.save(output);
		ringShape(patternFramed, planks).save(output);
	}

	private void registerForPavement(RecipeOutput output, String color, Item mainInput) {
		String baseName = color + LibBlockNames.PAVEMENT_SUFFIX;
		Block base = getBlockOrThrow(prefix(baseName));
		Block stair = getBlockOrThrow(prefix(baseName + LibBlockNames.STAIR_SUFFIX));
		Block slab = getBlockOrThrow(prefix(baseName + LibBlockNames.SLAB_SUFFIX));

		ShapelessRecipeBuilder builder = ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, base, 3)
				.requires(BotaniaBlocks.livingrock)
				.requires(Items.COBBLESTONE)
				.requires(Items.GRAVEL)
				.group("botania:pavement")
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrock));
		if (mainInput != Items.AIR) {
			builder.requires(mainInput);
		}
		builder.save(output);

		slabShape(slab, base).group("botania:pavement_slab").save(output);
		stairs(stair, base).group("botania:pavement_stairs").save(output);
	}

	private void registerForMetamorphic(RecipeOutput output, String variant) {
		Block base = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone"));
		Block slab = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.SLAB_SUFFIX));
		Block stair = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.STAIR_SUFFIX));
		Block wall = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.WALL_SUFFIX));
		Block brick = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks"));
		Block brickSlab = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.SLAB_SUFFIX));
		Block brickStair = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.STAIR_SUFFIX));
		Block brickWall = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.WALL_SUFFIX));
		Block chiseledBrick = getBlockOrThrow(prefix("chiseled_" + LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks"));
		Block cobble = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone"));
		Block cobbleSlab = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.SLAB_SUFFIX));
		Block cobbleStair = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.STAIR_SUFFIX));
		Block cobbleWall = getBlockOrThrow(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.WALL_SUFFIX));

		Criterion<?> marimorphosis = conditionsFromItem(BotaniaFlowerBlocks.marimorphosis);
		slabShape(slab, base).group("botania:metamorphic_stone_slab")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		stairs(stair, base).group("botania:metamorphic_stone_stairs")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		wallShape(wall, base, 6).group("botania:metamorphic_stone_wall")
				.unlockedBy("has_flower_item", marimorphosis).save(output);

		brick(brick, base).group("botania:metamorphic_brick")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		slabShape(brickSlab, brick).group("botania:metamorphic_brick_slab")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		stairs(brickStair, brick).group("botania:metamorphic_brick_stairs")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		wallShape(brickWall, brick, 6).group("botania:metamorphic_brick_wall")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		chiseled(chiseledBrick, brickSlab).unlockedBy("has_base_item", conditionsFromItem(brick))
				.unlockedBy("has_flower_item", marimorphosis).save(output);

		slabShape(cobbleSlab, cobble).group("botania:metamorphic_cobble_slab")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		stairs(cobbleStair, cobble).group("botania:metamorphic_cobble_stairs")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
		wallShape(cobbleWall, cobble, 6).group("botania:metamorphic_cobble_wall")
				.unlockedBy("has_flower_item", marimorphosis).save(output);
	}

	private ShapedRecipeBuilder compression(ItemLike output, TagKey<Item> input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, output instanceof Block ? RecipeCategory.BUILDING_BLOCKS : RecipeCategory.MISC, output)
				.define('I', input)
				.pattern("III")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromTag(input));
	}

	protected ShapedRecipeBuilder compression(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, output instanceof Block ? RecipeCategory.BUILDING_BLOCKS : RecipeCategory.MISC, output)
				.define('I', input)
				.pattern("III")
				.pattern("III")
				.pattern("III")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	protected ShapedRecipeBuilder brick(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output, 4)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('Q', input)
				.pattern("QQ")
				.pattern("QQ");
	}

	protected ShapedRecipeBuilder stairs(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output, 4)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('Q', input)
				.pattern("Q  ")
				.pattern("QQ ")
				.pattern("QQQ");
	}

	protected ShapedRecipeBuilder slabShape(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output, 6)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('Q', input)
				.pattern("QQQ");
	}

	protected ShapedRecipeBuilder pillar(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output, 2)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('Q', input)
				.pattern("Q")
				.pattern("Q");
	}

	protected ShapedRecipeBuilder chiseled(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('Q', input)
				.pattern("Q")
				.pattern("Q");
	}

	protected ShapedRecipeBuilder wallShape(ItemLike output, ItemLike input, int amount) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, output, amount)
				.unlockedBy("has_item", conditionsFromItem(input))
				.define('B', input)
				.pattern("BBB")
				.pattern("BBB");
	}

	protected ShapedRecipeBuilder fence(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, output, 3)
				.unlockedBy("has_item", conditionsFromItem(input))
				.group("wooden_fence")
				.define('B', input)
				.define('S', Items.STICK)
				.pattern("BSB")
				.pattern("BSB");
	}

	protected ShapedRecipeBuilder fenceGate(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, output, 1)
				.unlockedBy("has_item", conditionsFromItem(input))
				.group("wooden_fence_gate")
				.define('B', input)
				.define('S', Items.STICK)
				.pattern("SBS")
				.pattern("SBS");
	}

	protected ShapedRecipeBuilder ringShape(ItemLike output, ItemLike input) {
		return ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, output, 4)
				.define('W', input)
				.pattern(" W ")
				.pattern("W W")
				.pattern(" W ")
				.unlockedBy("has_item", conditionsFromItem(input));
	}

	protected void cosmeticBauble(RecipeOutput output, ItemLike result, ItemLike input) {
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, result)
				.define('P', input)
				.define('S', BotaniaItems.manaString)
				.pattern("PPP")
				.pattern("PSP")
				.pattern("PPP")
				.group("botania:cosmetic_bauble")
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaString))
				.save(output);
	}

	protected void specialRecipe(RecipeOutput output, RecipeSerializer<? extends CraftingRecipe> serializer) {
		Identifier name = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
		if (name == null) {
			throw new IllegalStateException("Recipe serializer " + serializer + " is not registered!");
		}
		CraftingRecipe recipe = serializer.codec().codec().parse(JsonOps.INSTANCE, new JsonObject()).getOrThrow();
		output.accept(ResourceKey.create(Registries.RECIPE, prefix("dynamic/" + name.getPath())), recipe, null);
	}

	private static Item dyeItem(DyeColor color) {
		return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath("minecraft", color.getSerializedName() + "_dye")).orElse(Items.AIR);
	}

	protected Block getBlockOrThrow(Identifier location) {
		return BuiltInRegistries.BLOCK.getValueOrThrow(ResourceKey.create(Registries.BLOCK, location));
	}

	protected Item getItemOrThrow(Identifier location) {
		return BuiltInRegistries.ITEM.getValueOrThrow(ResourceKey.create(Registries.ITEM, location));
	}

	@Override
	public String getName() {
		return "Botania crafting recipes";
	}
}
