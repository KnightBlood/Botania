package vazkii.botania.client.patchouli.processor;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.RecipeWithReagent;
import vazkii.botania.client.patchouli.PatchouliUtils;
import vazkii.patchouli.api.IComponentProcessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.IVariableProvider;

import java.util.List;

public abstract class ReagentRecipeProcessor implements IComponentProcessor {
	protected RecipeWithReagent recipe;

	@Override
	public abstract void setup(Level level, IVariableProvider variables);

	@Override
	public IVariable process(Level level, String key) {
		if (recipe == null) {
			return null;
		}
		return switch (key) {
			case "recipe" -> {
				Identifier id = recipe.getId();
				yield id == null ? null : IVariable.wrap(id.toString(), level.registryAccess());
			}
			case "reagent" -> PatchouliUtils.interweaveIngredients(List.of(recipe.getReagent()), level.registryAccess());
			case "output" -> IVariable.from(recipe.getResultItem(), level.registryAccess());
			case "heading" -> IVariable.from(recipe.getResultItem().getHoverName(), level.registryAccess());
			default -> null;
		};
	}
}
