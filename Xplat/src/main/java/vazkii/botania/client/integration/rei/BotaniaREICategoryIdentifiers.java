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
import me.shedaniel.rei.api.common.display.Display;

import vazkii.botania.api.recipe.BotanicalBreweryRecipe;
import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.OrechidRecipe;
import vazkii.botania.api.recipe.PetalApothecaryRecipe;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;

public final class BotaniaREICategoryIdentifiers {
	public static final CategoryIdentifier<Display> BREWERY = CategoryIdentifier.of(BotanicalBreweryRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> ELVEN_TRADE = CategoryIdentifier.of(ElvenTradeRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> MANA_INFUSION = CategoryIdentifier.of(ManaInfusionRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> MARIMORPHOSIS = CategoryIdentifier.of(OrechidRecipe.MARIMORPHOSIS_TYPE_ID);
	public static final CategoryIdentifier<Display> ORECHID = CategoryIdentifier.of(OrechidRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> ORECHID_IGNEM = CategoryIdentifier.of(OrechidRecipe.IGNEM_TYPE_ID);
	public static final CategoryIdentifier<Display> PETAL_APOTHECARY = CategoryIdentifier.of(PetalApothecaryRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> PURE_DAISY = CategoryIdentifier.of(PureDaisyRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> RUNE_ALTAR = CategoryIdentifier.of(RunicAltarRecipe.TYPE_ID);
	public static final CategoryIdentifier<Display> TERRA_PLATE = CategoryIdentifier.of(TerrestrialAgglomerationRecipe.TYPE_ID);

	private BotaniaREICategoryIdentifiers() {}
}
