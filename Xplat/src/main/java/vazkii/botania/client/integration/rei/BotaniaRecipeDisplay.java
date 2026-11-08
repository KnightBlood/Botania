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

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.common.helper.RecipeHelper;
import vazkii.botania.common.integration.rei.BotaniaREIView;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Shared base for Botania's REI displays. In MC 26.1 recipes are owned by a {@link RecipeHolder},
 * so both the ingredients and the id are taken from that holder rather than from the recipe itself.
 */
public abstract class BotaniaRecipeDisplay<T extends Recipe<?>> implements Display, BotaniaREIView {
	protected final RecipeHolder<T> holder;
	protected final T recipe;
	protected List<EntryIngredient> inputs;
	protected EntryIngredient outputs;

	protected BotaniaRecipeDisplay(RecipeHolder<T> holder) {
		this.holder = holder;
		this.recipe = holder.value();
		this.inputs = EntryIngredients.ofIngredients(RecipeHelper.getIngredients(recipe));
		// RecipeHelper needs a Level for vanilla slot displays; Botania recipes expose their
		// preview result directly, which also works before a world has been loaded.
		Level level = Minecraft.getInstance().level;
		ItemStack result = level == null
				? (recipe instanceof BotaniaRecipe<?> botaniaRecipe ? botaniaRecipe.getResultItem() : ItemStack.EMPTY)
				: RecipeHelper.getResultItem(recipe, level);
		this.outputs = EntryIngredients.of(result);
	}

	@Override
	public @NotNull List<EntryIngredient> getInputEntries() {
		return inputs;
	}

	public abstract int getManaCost();

	@Override
	public @NotNull List<EntryIngredient> getOutputEntries() {
		return Collections.singletonList(outputs);
	}

	@Override
	public @NotNull Optional<Identifier> getDisplayLocation() {
		return Optional.of(holder.id().identifier());
	}

	/**
	 * These displays are built on the client from the local recipe manager and are never
	 * sent over the network, so REI explicitly permits a null serializer here.
	 */
	@Override
	public @Nullable DisplaySerializer<? extends Display> getSerializer() {
		return null;
	}
}
