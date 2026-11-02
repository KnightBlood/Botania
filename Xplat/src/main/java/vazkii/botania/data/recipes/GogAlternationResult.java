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

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.crafting.recipe.GogAlternationRecipe;

public class GogAlternationResult implements BotaniaFinishedRecipe {
	private final BotaniaFinishedRecipe gogRecipe;
	private final BotaniaFinishedRecipe baseRecipe;

	public GogAlternationResult(BotaniaFinishedRecipe gogRecipe, BotaniaFinishedRecipe baseRecipe) {
		this.gogRecipe = gogRecipe;
		this.baseRecipe = baseRecipe;
	}

	@Override
	public void serializeRecipeData(JsonObject json) {
		json.add("gog", gogRecipe.serializeRecipe());
		json.add("base", baseRecipe.serializeRecipe());
	}

	@Override
	public RecipeSerializer<?> getType() {
		return GogAlternationRecipe.SERIALIZER;
	}

	// Take these from the base recipe
		public Identifier getId() {
		return baseRecipe.getId();
	}

	@Nullable
	@Override
	public JsonObject serializeAdvancement() {
		return baseRecipe.serializeAdvancement();
	}

	@Nullable
	@Override
	public Identifier getAdvancementId() {
		return baseRecipe.getAdvancementId();
	}
}
