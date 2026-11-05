/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.integration.rei.BotaniaREIView;
import vazkii.botania.common.block.BotaniaBlocks;

import java.util.ArrayList;
import java.util.List;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ElvenTradeREICategory implements DisplayCategory<Display> {
	private final EntryStack<ItemStack> gateway = EntryStacks.of(new ItemStack(BotaniaBlocks.alfPortal));
	private final Identifier TRADE_OVERLAY = prefix("textures/gui/elven_trade_overlay.png");
	private final Identifier PORTAL_SPRITE = prefix("block/alfheim_portal_swirl");

	@Override
	public @NotNull CategoryIdentifier<Display> getCategoryIdentifier() {
		return BotaniaREICategoryIdentifiers.ELVEN_TRADE;
	}

	@Override
	public @NotNull Renderer getIcon() {
		return gateway;
	}

	@Override
	public @NotNull Component getTitle() {
		return Component.translatable("botania.nei.elvenTrade");
	}

	@Override
	public @NotNull List<Widget> setupDisplay(Display display, Rectangle bounds) {
		BotaniaREIView view = (BotaniaREIView) display;
		List<Widget> widgets = new ArrayList<>();
		Point center = new Point(bounds.getCenterX() - 8, bounds.getCenterY() - 4);
		widgets.add(Widgets.createRecipeBase(bounds));
		widgets.add(Widgets.createDrawableWidget((gui, mouseX, mouseY, delta) -> {
			CategoryUtils.drawOverlay(gui, TRADE_OVERLAY, center.x - 45, center.y - 34, 20, 19, 71, 75);
			drawPortal(gui, center);
		}));
		int x = center.x - 20;
		for (EntryIngredient o : display.getInputEntries()) {
			widgets.add(Widgets.createSlot(new Point(x, center.y - 40)).entries(o).disableBackground());
			x += 18;
		}
		x = center.x + 28;
		for (EntryIngredient o : display.getOutputEntries()) {
			widgets.add(Widgets.createSlot(new Point(x, center.y)).entries(o).disableBackground());
			x += 18;
		}
		return widgets;
	}

	@Override
	public int getDisplayHeight() {
		return 100;
	}

	/**
	 * MC 26.1 replaced the immediate-mode quad submission with a GUI render state; the atlas
	 * sprite is now drawn through {@link GuiGraphicsExtractor#blitSprite}, matching the EMI
	 * integration. The REI compatibility GuiGraphics extends GuiGraphicsExtractor.
	 */
	void drawPortal(GuiGraphicsExtractor gui, Point point) {
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
				.getSprite(PORTAL_SPRITE);
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, point.x - 43, point.y - 17, 48, 48);
	}
}
