package vazkii.botania.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;

public interface RecipeWithReagent extends BotaniaRecipe<RecipeInput> {
	/**
	 * @return Ingredient matching the final item that needs to be thrown into the apothecary
	 *         to perform a craft after a matching recipe is in.
	 */
	Ingredient getReagent();

	/**
	 * @return the preview of the result of this recipe.
	 */
	default ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}
}
