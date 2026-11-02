package vazkii.botania.client.integration.emi;

import dev.emi.emi.api.widget.TextureWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public class BlendTextureWidget extends TextureWidget {

	public BlendTextureWidget(Identifier texture, int x, int y, int width, int height, int u, int v) {
		super(texture, x, y, width, height, u, v);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
		super.extractRenderState(gui, mouseX, mouseY, delta);
	}
}
