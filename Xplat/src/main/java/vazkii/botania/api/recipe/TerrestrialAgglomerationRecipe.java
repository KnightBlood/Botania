/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;

public interface TerrestrialAgglomerationRecipe extends BotaniaRecipe<RecipeInput> {
	Identifier TERRA_PLATE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "terra_plate");
	Identifier TYPE_ID = TERRA_PLATE_ID;

	int getMana();

	/**
	 * @return the preview of the result of this recipe.
	 */
	default ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@SuppressWarnings("unchecked")
	@Override
	default RecipeType<? extends Recipe<RecipeInput>> getType() {
		return (RecipeType<? extends Recipe<RecipeInput>>) BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID);
	}


	@NotNull
		default ItemStack getToastSymbol() {
		return BuiltInRegistries.ITEM.getOptional(TERRA_PLATE_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
	}

	@Override
	default boolean isSpecial() {
		return true;
	}
}
