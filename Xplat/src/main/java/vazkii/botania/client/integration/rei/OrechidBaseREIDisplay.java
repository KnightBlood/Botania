/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.OrechidRecipe;

import java.util.List;
import java.util.Optional;

/** Orechid / Orechid Ignem / Marimorphosis all show a state ingredient -> state ingredient exchange. */
public abstract class OrechidBaseREIDisplay<T extends OrechidRecipe> implements Display {
	private final RecipeHolder<T> holder;
	private final T recipe;
	private final List<EntryIngredient> stone;
	private final List<EntryIngredient> ores;

	protected OrechidBaseREIDisplay(RecipeHolder<T> holder) {
		this.holder = holder;
		this.recipe = holder.value();
		this.stone = List.of(EntryIngredients.ofItemStacks(recipe.getInput().getDisplayedStacks()));
		this.ores = List.of(EntryIngredients.ofItemStacks(recipe.getOutput().getDisplayedStacks()));
	}

	@Override
	public @NotNull List<EntryIngredient> getInputEntries() {
		return stone;
	}

	@Override
	public @NotNull List<EntryIngredient> getOutputEntries() {
		return ores;
	}

	public T getRecipe() {
		return recipe;
	}

	@Override
	public @NotNull Optional<Identifier> getDisplayLocation() {
		return Optional.of(holder.id().identifier());
	}

	@Override
	public @Nullable DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}
}
