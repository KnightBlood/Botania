/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;

import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.gui.ManaBarTooltipComponent;

import java.util.List;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsFabricMixin {
	// MC 26.1: GuiGraphics is gone; tooltips are laid out by GuiGraphicsExtractor.tooltip.
	// The box position comes from the same positioner call vanilla makes, which we replicate
	// at HEAD (mirrors the Forge RenderTooltipEvent.Pre handling).
	@Inject(method = "tooltip", at = @At("HEAD"))
	private void renderManaBar(Font font, List<ClientTooltipComponent> components, int mouseX, int mouseY, ClientTooltipPositioner positioner, Identifier background, CallbackInfo ci) {
		int width = 0;
		int height = components.size() == 1 ? -2 : 0; // EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE
		ManaBarTooltipComponent manaBar = null;
		for (ClientTooltipComponent component : components) {
			width = Math.max(width, component.getWidth(font));
			height += component.getHeight(font);
			if (component instanceof ManaBarTooltipComponent c) {
				manaBar = c;
			}
		}
		if (manaBar != null) {
			GuiGraphicsExtractor self = (GuiGraphicsExtractor) (Object) this;
			Vector2ic pos = positioner.positionTooltip(self.guiWidth(), self.guiHeight(), mouseX, mouseY, width, height);
			manaBar.setContext(pos.x(), pos.y(), width);
		}
	}
}
