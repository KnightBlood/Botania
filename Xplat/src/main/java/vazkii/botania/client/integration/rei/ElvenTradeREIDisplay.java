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
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;

public class ElvenTradeREIDisplay extends BotaniaRecipeDisplay<ElvenTradeRecipe> {
	public ElvenTradeREIDisplay(RecipeHolder<ElvenTradeRecipe> holder) {
		super(holder);
		this.outputs = EntryIngredients.ofItemStacks(recipe.getOutputs());
	}

	@Override
	public int getManaCost() {
		return AlfheimPortalBlockEntity.MANA_COST;
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.ELVEN_TRADE;
	}
}
