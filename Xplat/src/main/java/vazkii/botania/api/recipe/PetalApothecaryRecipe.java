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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;

public interface PetalApothecaryRecipe extends RecipeWithReagent {
	Identifier TYPE_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "petal_apothecary");

	@SuppressWarnings("unchecked")
	@NotNull
	@Override
	default RecipeType<? extends Recipe<RecipeInput>> getType() {
		return (RecipeType<? extends Recipe<RecipeInput>>) BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID);
	}
}
