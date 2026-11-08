/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.common.item.AncientWillItem;

public class AncientWillRecipe extends CustomRecipe {
	public static final RecipeSerializer<AncientWillRecipe> SERIALIZER = NoOpRecipeSerializer.create(AncientWillRecipe::new);

	public AncientWillRecipe(Identifier id) {
		// MC 26.1: CustomRecipe no-arg constructor
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world) {
		boolean foundWill = false;
		boolean foundItem = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof AncientWillItem) {
					if (foundWill) {
						return false;
					}
					foundWill = true;
				} else if (stack.getItem() instanceof AncientWillContainer) {
					if (foundItem) {
						return false;
					}
					foundItem = true;
				} else {
					return false;
				}
			}
		}

		return foundWill && foundItem;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv) {
		ItemStack item = ItemStack.EMPTY;
		AncientWillContainer.AncientWillType will = null;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof AncientWillContainer && item.isEmpty()) {
					item = stack;
				} else {
					will = ((AncientWillItem) stack.getItem()).type; // we already verified this is a will in matches()
				}
			}
		}

		AncientWillContainer container = (AncientWillContainer) item.getItem();
		if (container.hasAncientWill(item, will)) {
			return ItemStack.EMPTY;
		}

		ItemStack copy = item.copy();
		container.addAncientWill(copy, will);
		return copy;
	}

	@NotNull
	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SERIALIZER;
	}
}
