/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;

public interface OrechidRecipe extends BotaniaRecipe<RecipeInput> {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "orechid");
	Identifier IGNEM_TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "orechid_ignem");
	Identifier MARIMORPHOSIS_TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "marimorphosis");

	/** Valid inputs for the recipe */
	StateIngredient getInput();

	/** Output to display in recipes and to be used by default. */
	StateIngredient getOutput();

	@NotNull
	@Override
	RecipeType<? extends OrechidRecipe> getType();

	/** Location-sensitive output, called with the position of the block to convert. */
	default StateIngredient getOutput(@NotNull Level level, @NotNull BlockPos pos) {
		return getOutput();
	}

	/**
	 * Default Weighted, used if no special Weighted logic is provided, and to display
	 * in recipes (the JEI/REI displayed output per 64 input depends on the sum of default weights).
	 */
	int getWeight();

	/** Location-sensitive Weighted, called with the position of the block to convert. */
	default int getWeight(@NotNull Level level, @NotNull BlockPos pos) {
		return getWeight();
	}

	@Nullable
	CacheableFunction getSuccessFunction();

	@Override
	default boolean matches(RecipeInput c, Level l) {
		return false;
	}

	@Override
	default ItemStack assemble(RecipeInput c) {
		return ItemStack.EMPTY;
	}


		default ItemStack getResultItem() {
		return ItemStack.EMPTY;
	}

	@Override
	default boolean isSpecial() {
		return true;
	}
}
