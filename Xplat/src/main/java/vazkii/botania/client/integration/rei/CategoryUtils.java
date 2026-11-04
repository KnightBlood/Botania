/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.math.FloatingPoint;
import me.shedaniel.math.Point;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import vazkii.botania.client.core.helper.RenderHelper;

interface CategoryUtils {
	/**
	 * Widgets#createTexturedWidget doesn't allow partial transparency, so this is called through
	 * createDrawableWidget instead. In MC 26.1 the blit goes through the GUI_TEXTured render
	 * pipeline in {@link RenderHelper}; the REI compatibility GuiGraphics extends
	 * {@link GuiGraphicsExtractor}, so the same helper works for both loaders.
	 */
	static void drawOverlay(GuiGraphicsExtractor gui, Identifier texture, int x, int y, int u, int v, int width, int height) {
		RenderHelper.drawTexturedModalRect(gui, texture, x, y, u, v, width, height);
	}

	/** Spin the wheel and laugh at god! Used for the Apothecary / Rune Altar / Terra Plate categories. */
	static FloatingPoint rotatePointAbout(FloatingPoint in, Point about, double degrees) {
		double rad = degrees * Math.PI / 180.0;
		double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
		double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
		return new FloatingPoint(newX, newY);
	}
}
