/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.PetalApothecaryRecipe;

public class PetalApothecaryREIDisplay extends BotaniaRecipeDisplay<PetalApothecaryRecipe> {
	private final EntryIngredient reagent;

	public PetalApothecaryREIDisplay(RecipeHolder<PetalApothecaryRecipe> holder) {
		super(holder);
		this.reagent = EntryIngredients.ofIngredient(recipe.getReagent());
	}

	public EntryIngredient getReagent() {
		return reagent;
	}

	@Override
	public int getManaCost() {
		return 0;
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.PETAL_APOTHECARY;
	}
}
