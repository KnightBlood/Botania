package vazkii.botania.client.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.integration.shared.OrechidUIHelper;
import vazkii.botania.common.crafting.MarimorphosisRecipe;

import java.util.stream.Stream;

public class MarimorphosisEmiRecipe extends OrechidEmiRecipe {
	public MarimorphosisEmiRecipe(
			RecipeHolder<MarimorphosisRecipe> holder,
			EmiIngredient orechid) {
		super(BotaniaEmiPlugin.MARIMORPHOSIS, holder, orechid);
	}

	@NotNull
	@Override
	protected Stream<Component> getChanceTooltipComponents(double chance) {
		Stream<Component> genericChanceTooltipComponents = super.getChanceTooltipComponents(chance);
		Stream<Component> biomeChanceTooltipComponents = OrechidUIHelper.getBiomeChanceAndRatioTooltipComponents(chance, recipe);
		return Stream.concat(genericChanceTooltipComponents, biomeChanceTooltipComponents);
	}
}
