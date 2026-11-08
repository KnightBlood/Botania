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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.OrechidRecipe;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.StateIngredientHelper;
import vazkii.botania.common.lib.BotaniaTags;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class OrechidProvider extends BotaniaRecipeProvider {

	public OrechidProvider(PackOutput packOutput) {
		super(packOutput);
	}

	// TODO: We had an enormous amount of ores defined for mod compat.
	//       The old data needs to be completely revised.
	@Override
	protected void buildRecipes(RecipeOutput output) {
		saveFinishedRecipe(output, stone(Blocks.COAL_ORE, 67415));
		saveFinishedRecipe(output, stone(Blocks.IRON_ORE, 29371));
		saveFinishedRecipe(output, stone(Blocks.REDSTONE_ORE, 7654));
		saveFinishedRecipe(output, stone(Blocks.COPPER_ORE, 7000));
		saveFinishedRecipe(output, stone(Blocks.GOLD_ORE, 2647));
		saveFinishedRecipe(output, stone(Blocks.EMERALD_ORE, 1239));
		saveFinishedRecipe(output, stone(Blocks.LAPIS_ORE, 1079));
		saveFinishedRecipe(output, stone(Blocks.DIAMOND_ORE, 883));

		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_COAL_ORE, 75));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_IRON_ORE, 250));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_REDSTONE_ORE, 150));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_COPPER_ORE, 75));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_GOLD_ORE, 125));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_EMERALD_ORE, 50));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_LAPIS_ORE, 175));
		saveFinishedRecipe(output, deepslate(Blocks.DEEPSLATE_DIAMOND_ORE, 100));

		saveFinishedRecipe(output, netherrack(Blocks.NETHER_QUARTZ_ORE, 19600));
		saveFinishedRecipe(output, netherrack(Blocks.NETHER_GOLD_ORE, 3635));
		saveFinishedRecipe(output, netherrack(Blocks.ANCIENT_DEBRIS, 148));

		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneForest, BotaniaTags.Biomes.MARIMORPHOSIS_FOREST_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStonePlains, BotaniaTags.Biomes.MARIMORPHOSIS_PLAINS_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneMountain, BotaniaTags.Biomes.MARIMORPHOSIS_MOUNTAIN_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneFungal, BotaniaTags.Biomes.MARIMORPHOSIS_FUNGAL_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneSwamp, BotaniaTags.Biomes.MARIMORPHOSIS_SWAMP_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneDesert, BotaniaTags.Biomes.MARIMORPHOSIS_DESERT_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneTaiga, BotaniaTags.Biomes.MARIMORPHOSIS_TAIGA_BONUS));
		saveFinishedRecipe(output, biomeStone(BotaniaBlocks.biomeStoneMesa, BotaniaTags.Biomes.MARIMORPHOSIS_MESA_BONUS));
	}

	protected Identifier orechidId(Block b) {
		return prefix("orechid/" + BuiltInRegistries.BLOCK.getKey(b).getPath());
	}

	protected Identifier ignemId(Block b) {
		return prefix("orechid_ignem/" + BuiltInRegistries.BLOCK.getKey(b).getPath());
	}

	protected Identifier marimorphosisId(Block b) {
		return prefix("marimorphosis/" + BuiltInRegistries.BLOCK.getKey(b).getPath());
	}

	protected Result stone(Block output, int Weighted) {
		return new Result(BotaniaRecipeTypes.ORECHID_SERIALIZER, orechidId(output),
				forBlock(Blocks.STONE), forBlock(output), Weighted);
	}

	protected Result deepslate(Block output, int Weighted) {
		return new Result(BotaniaRecipeTypes.ORECHID_SERIALIZER, orechidId(output),
				forBlock(Blocks.DEEPSLATE), forBlock(output), Weighted);
	}

	protected Result netherrack(Block output, int Weighted) {
		return new Result(BotaniaRecipeTypes.ORECHID_IGNEM_SERIALIZER, ignemId(output),
				forBlock(Blocks.NETHERRACK), forBlock(output), Weighted);
	}

	protected Result biomeStone(Block output, TagKey<Biome> biome) {
		return new BiomeResult(BotaniaRecipeTypes.MARIMORPHOSIS_SERIALIZER, marimorphosisId(output),
				forTag(BotaniaTags.Blocks.MARIMORPHOSIS_CONVERTABLE), forBlock(output), 1, 11, biome);
	}

	protected static StateIngredient forBlock(Block block) {
		return StateIngredientHelper.of(block);
	}

	protected static StateIngredient forTag(TagKey<Block> tag) {
		return StateIngredientHelper.of(tag);
	}

	@Override
	public String getName() {
		return "Botania Orechid and Marimorphosis recipes";
	}

	protected static class Result implements BotaniaFinishedRecipe {
		private final RecipeSerializer<? extends OrechidRecipe> type;
		private final Identifier id;
		private final StateIngredient input;
		private final StateIngredient output;
		private final int Weighted;

		public Result(RecipeSerializer<? extends OrechidRecipe> type, Identifier id,
				StateIngredient input, StateIngredient output, int Weighted) {
			this.type = type;
			this.id = id;
			this.input = input;
			this.output = output;
			this.Weighted = Weighted;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("input", input.serialize());
			json.add("output", output.serialize());
			json.addProperty("weight", Weighted);
		}

		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return type;
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		@Override
		public Identifier getAdvancementId() {
			return null;
		}
	}

	protected static class BiomeResult extends Result {
		private final int bonusWeight;
		private final TagKey<Biome> biome;

		public BiomeResult(RecipeSerializer<? extends OrechidRecipe> type, Identifier id,
				StateIngredient input, StateIngredient output, int Weighted, int bonusWeight,
				TagKey<Biome> biome) {
			super(type, id, input, output, Weighted);
			this.bonusWeight = bonusWeight;
			this.biome = biome;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			super.serializeRecipeData(json);
			json.addProperty("biome_bonus_tag", biome.location().toString());
			json.addProperty("biome_bonus", bonusWeight);
		}
	}

}
