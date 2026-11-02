/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.google.gson.JsonObject;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.crafting.recipe.NbtOutputRecipe;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Helpers to register Botania's variants of the vanilla crafting recipes.
 */
public class WrapperResult {
	private WrapperResult() {
	}

	/**
	 * Wraps recipe consumers with one that swaps the recipe type to a different one.
	 */
	public static RecipeOutput ofType(Function<Recipe<?>, Recipe<?>> wrapper, RecipeOutput parent) {
		return WrappingRecipeOutput.of(parent, wrapper);
	}

	/**
	 * Wraps recipe consumers with one that adds custom data to the result of the recipe.
	 */
	public static RecipeOutput with(RecipeOutput parent, Consumer<CompoundTag> tagSetup) {
		CompoundTag tag = new CompoundTag();
		tagSetup.accept(tag);
		return WrappingRecipeOutput.of(parent, recipe -> NbtOutputRecipe.wrap(recipe, tag));
	}
}
