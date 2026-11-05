/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.integration.rei.BotaniaREIView;
import vazkii.botania.api.recipe.OrechidRecipe;
import vazkii.botania.client.integration.shared.OrechidUIHelper;
import vazkii.botania.common.block.BotaniaFlowerBlocks;

import java.util.stream.Stream;

public class MarimorphosisREICategory extends OrechidREICategory {
	public MarimorphosisREICategory() {
		super(BotaniaREICategoryIdentifiers.MARIMORPHOSIS, BotaniaFlowerBlocks.marimorphosis);
	}

	@NotNull
	@Override
	protected Stream<Component> getChanceTooltipComponents(double chance, BotaniaREIView view) {
		Stream<Component> generic = super.getChanceTooltipComponents(chance, view);
		OrechidRecipe recipe = view.getRecipe();
		if (recipe == null) {
			// Synced display from a dedicated server: no OrechidRecipe object and no full recipe
			// list on the client, so the player-biome bonus cannot be recomputed. The generic
			// chance is still shown; the omission is documented in BotaniaSyncedDisplay.
			return generic;
		}
		return Stream.concat(generic, OrechidUIHelper.getBiomeChanceAndRatioTooltipComponents(chance, recipe));
	}
}
