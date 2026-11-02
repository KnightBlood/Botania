/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import org.apache.commons.lang3.mutable.MutableObject;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.helper.IngredientHelper;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.recipes.BotaniaFinishedRecipe;
import vazkii.botania.data.recipes.BotaniaRecipeProvider;
import vazkii.botania.data.recipes.GogAlternationResult;

import static vazkii.botania.data.recipes.CraftingRecipeProvider.*;

public class FabricRecipeProvider extends BotaniaRecipeProvider {
	public FabricRecipeProvider(net.minecraft.data.PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		// 26.1: recipe builders resolve tags through the HolderGetter they were created with, and the
		// static registries have no tag bindings during datagen, so every builder goes through
		// TAG_TOLERANT_ITEMS (unknown tags become empty named sets that still serialize as tag refs).
		// Quartz tag
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.azulejo0)
				.requires(Items.BLUE_DYE)
				.requires(FabricItemTagProvider.QUARTZ_BLOCKS)
				.unlockedBy("has_item", conditionsFromItem(Items.BLUE_DYE))
				.save(output);

		// Chest tag
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.baubleBox)
				.define('C', FabricItemTagProvider.WOODEN_CHESTS)
				.define('G', Items.GOLD_INGOT)
				.define('M', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern(" M ")
				.pattern("MCM")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);

		// uses colorless glass tags on Forge
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.TOOLS, BotaniaItems.glassPick)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('G', Items.GLASS)
				.define('I', BotaniaTags.Items.INGOTS_MANASTEEL)
				.pattern("GIG")
				.pattern(" T ")
				.pattern(" T ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.lensNormal)
				.define('S', BotaniaTags.Items.INGOTS_MANASTEEL)
				.define('G', Ingredient.of(Items.GLASS, Items.GLASS_PANE))
				.pattern(" S ")
				.pattern("SGS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);
		ShapedRecipeBuilder.shaped(TAG_TOLERANT_ITEMS, RecipeCategory.DECORATIONS, BotaniaBlocks.prism)
				.define('P', Items.PRISMARINE_CRYSTALS)
				.define('S', BotaniaBlocks.spectralPlatform)
				.define('G', Items.GLASS)
				.pattern("GPG")
				.pattern("GSG")
				.pattern("GPG")
				.unlockedBy("has_item", conditionsFromItem(Items.PRISMARINE_CRYSTALS))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaBlocks.spectralPlatform))
				.save(output);

		registerRedStringBlock(output, BotaniaBlocks.redStringContainer, IngredientHelper.of(FabricItemTagProvider.WOODEN_CHESTS), conditionsFromTag(FabricItemTagProvider.WOODEN_CHESTS));
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaRetainer)
				.requires(FabricItemTagProvider.WOODEN_CHESTS)
				.requires(BotaniaItems.corporeaSpark)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.phantomInk, 4)
				.requires(BotaniaItems.manaPearl)
				.requires(ConventionalItemTags.DYES)
				.requires(ConventionalItemTags.GLASS_BLOCKS)
				.requires(Items.GLASS_BOTTLE, 4)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);

		// 26.1: the old Consumer<FinishedRecipe> MutableObject capture pattern is
		// replaced by RecipeOutput proxies (see BotaniaRecipeProvider.capturing).
		MutableObject<BotaniaFinishedRecipe> base = new MutableObject<>();
		MutableObject<BotaniaFinishedRecipe> gog = new MutableObject<>();
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.fertilizer)
				.requires(Items.BONE_MEAL)
				.requires(IngredientHelper.of(ConventionalItemTags.DYES), 4)
				.unlockedBy("has_item", conditionsFromTag(ConventionalItemTags.DYES))
				.save(capturing(base::setValue), "botania:fertilizer_dye");
		ShapelessRecipeBuilder.shapeless(TAG_TOLERANT_ITEMS, RecipeCategory.MISC, BotaniaItems.fertilizer, 3)
				.requires(Items.BONE_MEAL)
				.requires(IngredientHelper.of(ConventionalItemTags.DYES), 4)
				.unlockedBy("has_item", conditionsFromTag(ConventionalItemTags.DYES))
				.save(capturing(gog::setValue), "botania:fertilizer_dye");
		saveFinishedRecipe(output, new GogAlternationResult(gog.getValue(), base.getValue()));
	}

	@Override
	public String getName() {
		return "Botania recipes (Fabric-specific)";
	}
}
