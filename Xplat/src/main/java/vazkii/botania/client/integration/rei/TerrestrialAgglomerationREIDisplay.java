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

import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;

public class TerrestrialAgglomerationREIDisplay extends BotaniaRecipeDisplay<TerrestrialAgglomerationRecipe> {
	public TerrestrialAgglomerationREIDisplay(RecipeHolder<TerrestrialAgglomerationRecipe> holder) {
		super(holder);
	}

	@Override
	public int getManaCost() {
		return recipe.getMana();
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.TERRA_PLATE;
	}
}
