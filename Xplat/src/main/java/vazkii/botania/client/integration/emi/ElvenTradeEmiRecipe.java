package vazkii.botania.client.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;

import net.minecraft.data.AtlasIds;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.crafting.RecipeHolder;

import vazkii.botania.api.recipe.ElvenTradeRecipe;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ElvenTradeEmiRecipe extends BotaniaEmiRecipe {
	private static final Identifier TEXTURE = prefix("textures/gui/elven_trade_overlay.png");

	public ElvenTradeEmiRecipe(RecipeHolder<ElvenTradeRecipe> holder) {
		super(BotaniaEmiPlugin.ELVEN_TRADE, holder);
		ElvenTradeRecipe recipe = holder.value();
		this.input = recipe.getIngredients().stream().map(EmiIngredient::of).toList();
		this.output = recipe.getOutputs().stream().map(EmiStack::of).toList();
	}

	@Override
	public int getDisplayHeight() {
		return 90;
	}

	@Override
	public int getDisplayWidth() {
		return 120;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.add(new BlendTextureWidget(TEXTURE, 10, 5, 71, 75, 20, 19));
		widgets.add(new ElevenTradeWidget(12, 22));
		int sx = 35;
		for (EmiIngredient ing : input) {
			widgets.addSlot(ing, sx, 0).drawBack(false);
			sx += 18;
		}
		sx = 83;
		for (EmiStack stack : output) {
			widgets.addSlot(stack, sx, 40).drawBack(false).recipeContext(this);
			sx += 18;
		}
	}

	private static class ElevenTradeWidget extends Widget {
		private final int x, y;

		public ElevenTradeWidget(int x, int y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public Bounds getBounds() {
			return new Bounds(x, y, 0, 0);
		}

		@Override
		public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
			TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
					.getSprite(prefix("block/alfheim_portal_swirl"));
			gui.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, 48, 48);
		}

	}
}
