/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.patchouli.processor;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.BotanicalBreweryRecipe;
import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.List;
import java.util.stream.Collectors;

public class BrewRecipeProcessor implements IComponentProcessor {
	private BotanicalBreweryRecipe recipe;

	@Override
	public void setup(Level level, IVariableProvider variables) {
		Identifier id = Identifier.parse(variables.get("recipe", level.registryAccess()).asString());
		this.recipe = PatchouliUtils.getRecipe(level, BotaniaRecipeTypes.BREW_TYPE, id);
	}

	@Override
	public IVariable process(Level level, String key) {
		if (recipe == null) {
			if (key.equals("is_offset")) {
				return IVariable.wrap(false, level.registryAccess());
			}
			return null;
		} else if (key.equals("heading")) {
			return IVariable.from(Component.translatable("botaniamisc.brewOf", Component.translatable(recipe.getBrew().getTranslationKey())), level.registryAccess());
		} else if (key.equals("vial")) {
			return IVariable.from(recipe.getOutput(new ItemStack(BotaniaItems.vial)), level.registryAccess());
		} else if (key.equals("flask")) {
			return IVariable.from(recipe.getOutput(new ItemStack(BotaniaItems.flask)), level.registryAccess());
		} else if (key.startsWith("input")) {
			List<Ingredient> ingredients = recipe.getIngredients();
			int requestedIndex = Integer.parseInt(key.substring(5)) - 1;
			int indexOffset = (6 - ingredients.size()) / 2; //Center the brew ingredients
			int index = requestedIndex - indexOffset;

			if (index < ingredients.size() && index >= 0) {
				return IVariable.wrapList(PatchouliUtils.getStacks(ingredients.get(index)).stream()
						.map(stack -> IVariable.from(stack, level.registryAccess()))
						.collect(Collectors.toList()), level.registryAccess());
			} else {
				return null;
			}
		}
		if (key.equals("is_offset")) {
			return IVariable.wrap(recipe.getIngredients().size() % 2 == 0, level.registryAccess());
		}
		return null;
	}
}
