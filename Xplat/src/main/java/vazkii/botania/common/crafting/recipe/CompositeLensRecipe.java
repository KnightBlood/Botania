/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.mana.CompositableLensItem;
import vazkii.botania.common.lib.BotaniaTags;

public class CompositeLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<CompositeLensRecipe> SERIALIZER = NoOpRecipeSerializer.create(CompositeLensRecipe::new);

	public CompositeLensRecipe(Identifier id) {
		// MC 26.1: CustomRecipe no-arg constructor
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world) {
		boolean foundLens = false;
		boolean foundSecondLens = false;
		boolean foundGlue = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CompositableLensItem && !foundSecondLens) {
					if (foundLens) {
						foundSecondLens = true;
					} else {
						foundLens = true;
					}
				} else if (stack.is(BotaniaTags.Items.LENS_GLUE) && !foundGlue) {
					foundGlue = true;
				} else {
					return false; // Found an invalid or extra item, breaking the recipe
				}
			}
		}

		return foundSecondLens && foundGlue;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv) {
		ItemStack lens = ItemStack.EMPTY;
		ItemStack secondLens = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof CompositableLensItem) {
					if (lens.isEmpty()) {
						lens = stack;
					} else {
						secondLens = stack;
					}
				}
			}
		}

		if (lens.getItem() instanceof CompositableLensItem lensItem) {
			if (secondLens.isEmpty() || !lensItem.canCombineLenses(lens, secondLens) || !lensItem.getCompositeLens(lens).isEmpty() || !lensItem.getCompositeLens(secondLens).isEmpty()) {
				return ItemStack.EMPTY;
			}

			ItemStack lensCopy = lens.copyWithCount(1);
			ItemStack secondCopy = secondLens.copyWithCount(1);
			lensItem.setCompositeLens(lensCopy, secondCopy);

			return lensCopy;
		}

		return ItemStack.EMPTY;
	}


	@NotNull
	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SERIALIZER;
	}
}
