/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ManaBlasterItem;

public class ManaBlasterRemoveLensRecipe extends CustomRecipe {
	public static final RecipeSerializer<ManaBlasterRemoveLensRecipe> SERIALIZER = NoOpRecipeSerializer.create(ManaBlasterRemoveLensRecipe::new);

	public ManaBlasterRemoveLensRecipe(Identifier id) {
		// MC 26.1: CustomRecipe no-arg constructor
	}

	@Override
	public boolean matches(@NotNull CraftingInput inv, @NotNull Level world) {
		boolean foundGun = false;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem
						&& !ManaBlasterItem.getLens(stack).isEmpty() && !foundGun) {
					foundGun = true;
				} else {
					return false; // Found an invalid item, breaking the recipe
				}
			}
		}

		return foundGun;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull CraftingInput inv) {
		ItemStack gun = ItemStack.EMPTY;

		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			if (!stack.isEmpty()) {
				if (stack.getItem() instanceof ManaBlasterItem) {
					gun = stack;
				}
			}
		}

		ItemStack gunCopy = gun.copyWithCount(1);
		ManaBlasterItem.setLens(gunCopy, ItemStack.EMPTY);

		return gunCopy;
	}


	@NotNull
	@Override
	public RecipeSerializer<? extends CustomRecipe> getSerializer() {
		return SERIALIZER;
	}

	@NotNull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
		return RecipeUtils.getRemainingItemsSub(inv, s -> {
			if (s.is(BotaniaItems.manaGun)) {
				ItemStack stack = ManaBlasterItem.getLens(s);
				stack.setCount(1);
				return stack;
			}
			return null;
		});
	}
}
