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
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.StateIngredient;

import java.util.ArrayList;
import java.util.List;

public class ManaPoolREIDisplay extends BotaniaRecipeDisplay<ManaInfusionRecipe> {
	@Nullable
	private final EntryIngredient catalystEntries;

	public ManaPoolREIDisplay(RecipeHolder<ManaInfusionRecipe> holder) {
		super(holder);
		StateIngredient catalyst = recipe.getRecipeCatalyst();
		if (catalyst == null) {
			this.catalystEntries = null;
		} else {
			List<EntryStack<?>> entries = new ArrayList<>();
			for (BlockState state : catalyst.getDisplayed()) {
				entries.add(EntryStacks.of(state.getBlock()));
			}
			this.catalystEntries = EntryIngredient.of(entries);
		}
	}

	@Override
	public @Nullable EntryIngredient getCatalystEntries() {
		return catalystEntries;
	}

	@Override
	public int getManaCost() {
		return recipe.getManaToConsume();
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.MANA_INFUSION;
	}
}
