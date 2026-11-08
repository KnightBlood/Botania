/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.helper.IngredientHelper;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class RunicAltarProvider extends BotaniaRecipeProvider {
	public RunicAltarProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public String getName() {
		return "Botania runic altar recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		final int costTier1 = 5200;
		final int costTier2 = 8000;
		final int costTier3 = 12000;

		Ingredient manaSteel = IngredientHelper.of(BotaniaTags.Items.INGOTS_MANASTEEL);
		Ingredient manaDiamond = IngredientHelper.of(BotaniaTags.Items.GEMS_MANA_DIAMOND);
		Ingredient manaPowder = IngredientHelper.of(BotaniaTags.Items.DUSTS_MANA);
		saveFinishedRecipe(output, new FinishedRecipe(idFor("water"), new ItemStack(BotaniaItems.runeWater, 2), costTier1, manaPowder, manaSteel, Ingredient.of(Items.BONE_MEAL), Ingredient.of(Blocks.SUGAR_CANE), Ingredient.of(Items.FISHING_ROD)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("fire"), new ItemStack(BotaniaItems.runeFire, 2), costTier1, manaPowder, manaSteel, Ingredient.of(Items.NETHER_BRICK), Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.NETHER_WART)));

		Ingredient stone = Ingredient.of(Blocks.STONE);
		Ingredient coalBlock = Ingredient.of(Blocks.COAL_BLOCK);
		saveFinishedRecipe(output, new FinishedRecipe(idFor("earth"), new ItemStack(BotaniaItems.runeEarth, 2), costTier1, manaPowder, manaSteel, stone, coalBlock, Ingredient.of(Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM)));

		saveFinishedRecipe(output, new FinishedRecipe(idFor("air"), new ItemStack(BotaniaItems.runeAir, 2), costTier1, manaPowder, manaSteel, IngredientHelper.of(ItemTags.WOOL_CARPETS), Ingredient.of(Items.FEATHER), Ingredient.of(Items.STRING)));

		Ingredient fire = Ingredient.of(BotaniaItems.runeFire);
		Ingredient water = Ingredient.of(BotaniaItems.runeWater);
		Ingredient earth = Ingredient.of(BotaniaItems.runeEarth);
		Ingredient air = Ingredient.of(BotaniaItems.runeAir);

		Ingredient sapling = IngredientHelper.of(ItemTags.SAPLINGS);
		Ingredient leaves = IngredientHelper.of(ItemTags.LEAVES);
		Ingredient sand = IngredientHelper.of(ItemTags.SAND);
		saveFinishedRecipe(output, new FinishedRecipe(idFor("spring"), new ItemStack(BotaniaItems.runeSpring), costTier2, water, fire, sapling, sapling, sapling, Ingredient.of(Items.WHEAT)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("summer"), new ItemStack(BotaniaItems.runeSummer), costTier2, earth, air, sand, sand, Ingredient.of(Items.SLIME_BALL), Ingredient.of(Items.MELON_SLICE)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("autumn"), new ItemStack(BotaniaItems.runeAutumn), costTier2, fire, air, leaves, leaves, leaves, Ingredient.of(Items.SPIDER_EYE)));

		saveFinishedRecipe(output, new FinishedRecipe(idFor("winter"), new ItemStack(BotaniaItems.runeWinter), costTier2, water, earth, Ingredient.of(Blocks.SNOW_BLOCK), Ingredient.of(Blocks.SNOW_BLOCK), IngredientHelper.of(ItemTags.WOOL), Ingredient.of(Blocks.CAKE)));

		Ingredient spring = Ingredient.of(BotaniaItems.runeSpring);
		Ingredient summer = Ingredient.of(BotaniaItems.runeSummer);
		Ingredient autumn = Ingredient.of(BotaniaItems.runeAutumn);
		Ingredient winter = Ingredient.of(BotaniaItems.runeWinter);

		saveFinishedRecipe(output, new FinishedRecipe(idFor("mana"), new ItemStack(BotaniaItems.runeMana), costTier2, manaSteel, manaSteel, manaSteel, manaSteel, manaSteel, Ingredient.of(BotaniaItems.manaPearl)));

		saveFinishedRecipe(output, new FinishedRecipe(idFor("lust"), new ItemStack(BotaniaItems.runeLust), costTier3, manaDiamond, manaDiamond, summer, air));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("gluttony"), new ItemStack(BotaniaItems.runeGluttony), costTier3, manaDiamond, manaDiamond, winter, fire));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("greed"), new ItemStack(BotaniaItems.runeGreed), costTier3, manaDiamond, manaDiamond, spring, water));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("sloth"), new ItemStack(BotaniaItems.runeSloth), costTier3, manaDiamond, manaDiamond, autumn, air));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("wrath"), new ItemStack(BotaniaItems.runeWrath), costTier3, manaDiamond, manaDiamond, winter, earth));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("envy"), new ItemStack(BotaniaItems.runeEnvy), costTier3, manaDiamond, manaDiamond, winter, water));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("pride"), new ItemStack(BotaniaItems.runePride), costTier3, manaDiamond, manaDiamond, summer, fire));

		saveFinishedRecipe(output, new FinishedHeadRecipe(idFor("head"), new ItemStack(Items.PLAYER_HEAD), 22500, Ingredient.of(Items.SKELETON_SKULL), Ingredient.of(BotaniaItems.pixieDust), Ingredient.of(Items.PRISMARINE_CRYSTALS), Ingredient.of(Items.NAME_TAG, Items.WRITTEN_BOOK), Ingredient.of(Items.GOLDEN_APPLE)));
	}

	private static Identifier idFor(String s) {
		return prefix("runic_altar/" + s);
	}

	protected static class FinishedRecipe implements BotaniaFinishedRecipe {
		private final Identifier id;
		private final ItemStack output;
		private final int mana;
		private final Ingredient[] inputs;

		protected FinishedRecipe(Identifier id, ItemStack output, int mana, Ingredient... inputs) {
			this.id = id;
			this.output = output;
			this.mana = mana;
			this.inputs = inputs;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("output", ItemNBTHelper.serializeStack(output));
			JsonArray ingredients = new JsonArray();
			for (Ingredient ingr : inputs) {
				ingredients.add(IngredientHelper.serialize(ingr));
			}
			json.addProperty("mana", mana);
			json.add("ingredients", ingredients);
		}

		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.RUNE_SERIALIZER;
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

	private static class FinishedHeadRecipe extends FinishedRecipe {
		private FinishedHeadRecipe(Identifier id, ItemStack output, int mana, Ingredient... inputs) {
			super(id, output, mana, inputs);
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.RUNE_HEAD_SERIALIZER;
		}
	}
}
