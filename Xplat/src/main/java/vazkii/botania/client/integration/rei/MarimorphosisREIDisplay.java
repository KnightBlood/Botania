/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;

import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.crafting.MarimorphosisRecipe;

public class MarimorphosisREIDisplay extends OrechidBaseREIDisplay<MarimorphosisRecipe> {
	public MarimorphosisREIDisplay(RecipeHolder<MarimorphosisRecipe> holder) {
		super(holder);
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.MARIMORPHOSIS;
	}
}
