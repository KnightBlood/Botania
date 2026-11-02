/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.patchouli.component;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.crafting.Ingredient;

import org.joml.Matrix3x2fStack;

import vazkii.botania.xplat.BotaniaConfig;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.ICustomComponent;

import java.util.List;

/**
 * Base custom Patchouli component that draws a rotating circle of items.
 * Size is 80x80. For a centered one, set X to -1.
 * For usage with a specified list of items, see {@link RotatingItemListComponent}.
 * For usage with a runic altar or petal apothecary recipes, see {@link RotatingRecipeComponent}.
 */
abstract class RotatingItemListComponentBase implements ICustomComponent {
	protected transient List<Ingredient> ingredients;
	protected transient int x, y;

	@Override
	public void build(int componentX, int componentY, int pageNum) {
		this.x = componentX != -1 ? componentX : 17;
		this.y = componentY;
		this.ingredients = makeIngredients();
	}

	protected abstract List<Ingredient> makeIngredients();

	@Override
	public void extractRenderState(GuiGraphicsExtractor gui, IComponentRenderContext context, float pticks, int mouseX, int mouseY) {
		int degreePerInput = (int) (360F / ingredients.size());
		int ticksElapsed = context.getTicksInBook();

		float currentDegree = BotaniaConfig.client().lexiconRotatingItems()
				? Minecraft.getInstance().hasShiftDown()
						? ticksElapsed
						: ticksElapsed + pticks
				: 0;

		for (Ingredient input : ingredients) {
			extractIngredientAtAngle(gui, context, currentDegree, input, mouseX, mouseY);

			currentDegree += degreePerInput;
		}
	}

	private void extractIngredientAtAngle(GuiGraphicsExtractor gui, IComponentRenderContext context, float angle, Ingredient ingredient, int mouseX, int mouseY) {
		if (ingredient.isEmpty()) {
			return;
		}

		angle -= 90;
		int radius = 32;
		double xPos = x + Math.cos(angle * Math.PI / 180D) * radius + 32;
		double yPos = y + Math.sin(angle * Math.PI / 180D) * radius + 32;

		Matrix3x2fStack ms = gui.pose();
		ms.pushMatrix(); // This translation makes it not stuttery. It does not affect the tooltip as that is drawn separately later.
		ms.translate((float) (xPos - (int) xPos), (float) (yPos - (int) yPos));
		context.renderIngredient(gui, (int) xPos, (int) yPos, mouseX, mouseY, ingredient);
		ms.popMatrix();
	}

}
