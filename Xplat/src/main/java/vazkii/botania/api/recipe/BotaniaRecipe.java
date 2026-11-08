/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;

import org.jetbrains.annotations.Nullable;

/**
 * Base interface for Botania's own recipes. Botania's recipes are all "special" recipes that are
 * triggered by their own block entities rather than by the vanilla crafting grid, so they share a
 * couple of default implementations.
 */
public interface BotaniaRecipe<T extends RecipeInput> extends Recipe<T> {
	@Override
	default boolean showNotification() {
		return false;
	}

	@Override
	default String group() {
		return "";
	}

	@Override
	default PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	default RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.CRAFTING_MISC;
	}

	/**
	 * @return the ingredients of this recipe, for display purposes (JEI, EMI, Patchouli, ...).
	 */
	default NonNullList<Ingredient> getIngredients() {
		return NonNullList.create();
	}

	/**
	 * @return the preview of the result of this recipe, for display purposes.
	 */
	default ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	/**
	 * @return the id of this recipe, or {@code null} if it is not known (yet).
	 */
	@Nullable
	default Identifier getId() {
		return null;
	}

	/**
	 * Assigns the id of this recipe. In MC 26.1 the id of a recipe is owned by the recipe holder, so
	 * a recipe that was created by a codec cannot know its own id until the recipe manager has
	 * loaded it.
	 */
	default void setId(Identifier id) {}
}
