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

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterClipRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterClipRecipe> SERIALIZER = NoOpRecipeSerializer.create(ManaBlasterClipRecipe::new);

	public ManaBlasterClipRecipe(Identifier id) {
		// MC 26.1: CustomRecipe no-arg constructor
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world) {
		boolean foundGun = false;
		boolean foundClip = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem
						&& !ManaBlasterItem.hasClip(stack) && !foundGun) {
					foundGun = true;
				} else if (stack.is(BotaniaItems.clip) && !foundClip) {
					foundClip = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundGun && foundClip;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv) {
		ItemStack gun = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty() && stack.getItem() instanceof ManaBlasterItem) {
				gun = stack;
			}
		}

		if (gun.isEmpty()) {
			return ItemStack.EMPTY;
		}

		ItemStack lens = ManaBlasterItem.getLens(gun);
		ItemStack gunCopy = gun.copy();
		ManaBlasterItem.setLens(gunCopy, ItemStack.EMPTY);
		ManaBlasterItem.setClip(gunCopy);
		ManaBlasterItem.setLensAtPos(gunCopy, lens, 0);
		return gunCopy;
	}

	@NotNull
	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SERIALIZER;
	}
}
