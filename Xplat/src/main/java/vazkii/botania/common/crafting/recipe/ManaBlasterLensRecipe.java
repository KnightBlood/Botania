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

import vazkii.botania.api.mana.BasicLensItem;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterLensRecipe> SERIALIZER = NoOpRecipeSerializer.create(ManaBlasterLensRecipe::new);

	public ManaBlasterLensRecipe(Identifier id) {
		// MC 26.1: CustomRecipe no-arg constructor
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world) {
		int foundLens = 0;
		int foundGun = 0;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem && ManaBlasterItem.getLens(stack).isEmpty()) {
					foundGun++;
				} else if (ManaBlasterItem.isValidLens(stack)) {
					foundLens++;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundLens == 1 && foundGun == 1;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv) {
		ItemStack lens = ItemStack.EMPTY;
		ItemStack gun = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem) {
					gun = stack;
				} else if (stack.getItem() instanceof BasicLensItem) {
					lens = stack.copyWithCount(1);
				}
			}
		}

		if (lens.isEmpty() || gun.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack gunCopy = gun.copy();
		ManaBlasterItem.setLens(gunCopy, lens);

		return gunCopy;
	}


	@NotNull
	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SERIALIZER;
	}
}
