/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.patchouli;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.patchouli.client.base.ClientRecipes;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Client-only bridge to Patchouli 26.1's synced recipe cache.
 * <p>
 * MC 26.1 clients no longer own a {@code RecipeManager}: {@code Level#recipeAccess()} exposes only
 * property sets and stonecutter recipes, so Botania's old {@code Level -> RecipeManager} lookup
 * returns nothing on the client. Patchouli 26.1 keeps the {@link RecipeHolder}s it receives from
 * the server in {@link ClientRecipes}; book templates must read those instead.
 * <p>
 * This lives in its own class so dedicated servers, which only load {@link PatchouliUtils} for
 * {@code isInVisualizer()}, never have to resolve Patchouli's client-only classes.
 */
public final class ClientRecipeLookup {
	private ClientRecipeLookup() {}

	/**
	 * @return the client-side recipes of {@code type}, keyed by id, or an empty map when the
	 *         server has not sent its recipes yet (e.g. very early during login).
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> Map<Identifier, T> byType(RecipeType<T> type) {
		Map<Identifier, T> result = new LinkedHashMap<>();
		for (RecipeHolder<? extends T> holder : ClientRecipes.INSTANCE.getRecipesByType(type)) {
			T recipe = (T) holder.value();
			if (recipe instanceof BotaniaRecipe<?> botaniaRecipe) {
				// 26.1 recipes get their id from the holder; the book templates still expect it
				// to be set on the recipe itself.
				botaniaRecipe.setId(holder.id().identifier());
			}
			result.put(holder.id().identifier(), recipe);
		}
		return result;
	}
}
