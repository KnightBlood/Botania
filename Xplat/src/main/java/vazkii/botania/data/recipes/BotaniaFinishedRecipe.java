/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Copyright (c) 2014 - 2024 Vazkii, Violet
 *
 * Botania Mod is licensed under the Botania License:
 * http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

/**
 * Bridge interface for Botania's custom recipe datagen.
 * Replaces the old MC 1.20.1 RecipeOutput (FinishedRecipe) pattern.
 */
public interface BotaniaFinishedRecipe {
	void serializeRecipeData(JsonObject json);

	/**
	 * Serializes the whole recipe, including the "type" discriminator.
	 */
	default JsonObject serializeRecipe() {
		JsonObject json = new JsonObject();
		Identifier type = BuiltInRegistries.RECIPE_SERIALIZER.getKey(getType());
		json.addProperty("type", type != null ? type.toString() : "");
		serializeRecipeData(json);
		return json;
	}

	Identifier getId();

	RecipeSerializer<?> getType();

	@Nullable
	default JsonObject serializeAdvancement() {
		return null;
	}

	@Nullable
	default Identifier getAdvancementId() {
		return null;
	}
}
