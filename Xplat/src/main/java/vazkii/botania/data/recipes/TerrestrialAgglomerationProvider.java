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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.helper.IngredientHelper;


import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class TerrestrialAgglomerationProvider extends BotaniaRecipeProvider {
	public TerrestrialAgglomerationProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public String getName() {
		return "Botania Terra Plate recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		saveFinishedRecipe(output, new FinishedRecipe(idFor("terrasteel_ingot"), ManaPoolBlockEntity.MAX_MANA / 2,
				new ItemStack(BotaniaItems.terrasteel), Ingredient.of(BotaniaItems.manaSteel),
				Ingredient.of(BotaniaItems.manaPearl), Ingredient.of(BotaniaItems.manaDiamond)));
	}

	private static Identifier idFor(String s) {
		return prefix("terra_plate/" + s);
	}

	protected static class FinishedRecipe implements BotaniaFinishedRecipe {
		private final Identifier id;
		private final int mana;
		private final ItemStack output;
		private final Ingredient[] inputs;

		public FinishedRecipe(Identifier id, int mana, ItemStack output, Ingredient... inputs) {
			this.id = id;
			this.mana = mana;
			this.output = output;
			this.inputs = inputs;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("mana", mana);
			JsonArray ingredients = new JsonArray();
			for (Ingredient ingr : inputs) {
				ingredients.add(IngredientHelper.serialize(ingr));
			}
			json.add("ingredients", ingredients);
			json.add("result", ItemNBTHelper.serializeStack(output));
		}

				public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.TERRA_PLATE_SERIALIZER;
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
}
