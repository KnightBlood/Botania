/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.BotaniaRecipe;

/**
 * Small helper to get hold of the {@link RecipeManager} from a {@link Level}.
 * <p>
 * In MC 26.1 the recipe manager is only reachable through a {@link ServerLevel}, but a lot of
 * Botania's code only has a plain {@link Level} at hand.
 */
public final class RecipeHelper {
	private RecipeHelper() {
	}

	/**
	 * Adapts a {@link Container} into a {@link RecipeInput}, which is what MC 26.1 recipes consume.
	 */
	public static RecipeInput adapt(Container container) {
		return new RecipeInput() {
			@Override
			public ItemStack getItem(int index) {
				return container.getItem(index);
			}

			@Override
			public int size() {
				return container.getContainerSize();
			}
		};
	}

	/**
	 * @return the recipe manager of the given level, or {@code null} if the level has no server attached.
	 */
	public static RecipeManager getRecipeManager(Level level) {
		if (level == null) {
			return null;
		}
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.recipeAccess();
		}
		MinecraftServer server = level.getServer();
		return server != null ? RecipeHelper.getRecipeManager(server) : null;
	}

	public static RecipeManager getRecipeManager(MinecraftServer server) {
		if (server == null) {
			return null;
		}
		for (ServerLevel serverLevel : server.getAllLevels()) {
			return serverLevel.recipeAccess();
		}
		return null;
	}

	/**
	 * @return the ingredients of the recipe, using Botania's own accessor when available (vanilla
	 *         recipes do not expose their ingredients directly in MC 26.1).
	 */
	public static NonNullList<Ingredient> getIngredients(Recipe<?> recipe) {
		if (recipe instanceof BotaniaRecipe<?> botaniaRecipe) {
			return botaniaRecipe.getIngredients();
		}
		NonNullList<Ingredient> list = NonNullList.create();
		list.addAll(recipe.placementInfo().ingredients());
		return list;
	}

	/**
	 * @return the display ingredients of a crafting recipe, slot-aligned (empty slots are
	 *         {@link IngredientHelper#EMPTY}).
	 */
	public static NonNullList<Ingredient> getCraftingIngredients(CraftingRecipe recipe) {
		if (recipe instanceof ShapedRecipe shaped) {
			NonNullList<Ingredient> list = NonNullList.create();
			for (var maybeIngredient : shaped.getIngredients()) {
				list.add(maybeIngredient.orElse(IngredientHelper.EMPTY));
			}
			return list;
		}
		return getIngredients(recipe);
	}

	/**
	 * @return the item that the recipe displays as its result, or {@link ItemStack#EMPTY} if the
	 *         recipe has no display.
	 */
	public static ItemStack getResultItem(Recipe<?> recipe, Level level) {
		if (recipe instanceof BotaniaRecipe<?> botaniaRecipe) {
			ItemStack stack = botaniaRecipe.getResultItem();
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		for (RecipeDisplay display : recipe.display()) {
			ItemStack stack = display.result().resolveForFirstStack(SlotDisplayContext.fromLevel(level));
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
