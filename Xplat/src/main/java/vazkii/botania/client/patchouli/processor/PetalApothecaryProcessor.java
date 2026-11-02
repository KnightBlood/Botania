/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.patchouli.processor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.patchouli.api.IVariableProvider;

public class PetalApothecaryProcessor extends ReagentRecipeProcessor {
	@Override
	public void setup(Level level, IVariableProvider variables) {
		Identifier id = Identifier.parse(variables.get("recipe", level.registryAccess()).asString());
		this.recipe = PatchouliUtils.getRecipe(level, BotaniaRecipeTypes.PETAL_TYPE, id);
	}
}
