/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import net.minecraft.advancements.Criterion;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import vazkii.botania.common.block.BotaniaBlocks;

public class SmeltingProvider extends BotaniaRecipeProvider {
	public SmeltingProvider(PackOutput packOutput) {
		super(packOutput);
	}

	private static Criterion<?> conditionsFromItem(ItemLike item) {
		return CraftingRecipeProvider.conditionsFromItem(item);
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneForest),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneForest, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneForest))
				.save(output, "botania:smelting/metamorphic_forest_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestonePlains),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStonePlains, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestonePlains))
				.save(output, "botania:smelting/metamorphic_plains_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneMountain),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneMountain, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneMountain))
				.save(output, "botania:smelting/metamorphic_mountain_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneFungal),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneFungal, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneFungal))
				.save(output, "botania:smelting/metamorphic_fungal_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneSwamp),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneSwamp, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneSwamp))
				.save(output, "botania:smelting/metamorphic_swamp_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneDesert),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneDesert, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneDesert))
				.save(output, "botania:smelting/metamorphic_desert_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneTaiga),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneTaiga, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneTaiga))
				.save(output, "botania:smelting/metamorphic_taiga_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.biomeCobblestoneMesa),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.biomeStoneMesa, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.biomeCobblestoneMesa))
				.save(output, "botania:smelting/metamorphic_mesa_stone");
		SimpleCookingRecipeBuilder.smelting(Ingredient.of(BotaniaBlocks.livingrockBrick),
				RecipeCategory.BUILDING_BLOCKS, CookingBookCategory.BLOCKS, BotaniaBlocks.livingrockBrickCracked, 0.1f, 200)
				.unlockedBy("has_item", conditionsFromItem(BotaniaBlocks.livingrockBrick))
				.save(output, "botania:smelting/cracked_livingrock_bricks");
	}

	@Override
	public String getName() {
		return "Botania smelting recipes";
	}
}
