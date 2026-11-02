/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import net.minecraft.data.AtlasIds;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.lib.ResourceLocationHelper;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ElvenTradeRecipeCategory implements IRecipeCategory<ElvenTradeRecipe> {

	public static final RecipeType<ElvenTradeRecipe> TYPE = RecipeType.create(LibMisc.MOD_ID, "elven_trade", ElvenTradeRecipe.class);
	private final Component localizedName;
	private final IDrawable background;
	private final IDrawable overlay;
	private final IDrawable icon;

	public ElvenTradeRecipeCategory(IGuiHelper guiHelper) {
		localizedName = Component.translatable("botania.nei.elvenTrade");
		background = guiHelper.createBlankDrawable(145, 95);
		overlay = guiHelper.createDrawable(prefix("textures/gui/elven_trade_overlay.png"), 0, 15, 140, 90);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BotaniaBlocks.alfPortal));
	}

	@NotNull
	@Override
	public RecipeType<ElvenTradeRecipe> getRecipeType() {
		return TYPE;
	}

	@NotNull
	@Override
	public Component getTitle() {
		return localizedName;
	}

	@NotNull
	@Override
	public IDrawable getBackground() {
		return background;
	}

	@NotNull
	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void draw(@NotNull ElvenTradeRecipe recipe, @NotNull IRecipeSlotsView slotsView, @NotNull GuiGraphicsExtractor gui, double mouseX, double mouseY) {
		PoseStack matrices = gui.pose();
		RenderSystem.enableBlend();
		overlay.draw(gui, 0, 4);
		RenderSystem.disableBlend();

		RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
		TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(ResourceLocationHelper.prefix("block/alfheim_portal_swirl"));
		MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
		VertexConsumer v = immediate.getBuffer(RenderType.solid());
		int startX = 22;
		int startY = 25;
		int stopX = 70;
		int stopY = 73;
		Matrix4f mat = matrices.last().pose();
		v.addVertex(mat, startX, startY, 0).setColor(1f, 1f, 1f, 1f).setUv(sprite.getU0(), sprite.getV0()).setLight(0xF000F0).setNormal(matrices.last(), 1, 0, 0);
		v.addVertex(mat, startX, stopY, 0).setColor(1f, 1f, 1f, 1f).setUv(sprite.getU0(), sprite.getV1()).setLight(0xF000F0).setNormal(matrices.last(), 1, 0, 0);
		v.addVertex(mat, stopX, stopY, 0).setColor(1f, 1f, 1f, 1f).setUv(sprite.getU1(), sprite.getV1()).setLight(0xF000F0).setNormal(matrices.last(), 1, 0, 0);
		v.addVertex(mat, stopX, startY, 0).setColor(1f, 1f, 1f, 1f).setUv(sprite.getU1(), sprite.getV0()).setLight(0xF000F0).setNormal(matrices.last(), 1, 0, 0);
		immediate.endBatch();

	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull ElvenTradeRecipe recipe, @NotNull IFocusGroup focusGroup) {
		int posX = 42;
		for (var ingr : recipe.getIngredients()) {
			builder.addSlot(RecipeIngredientRole.INPUT, posX, 0)
					.addIngredients(ingr);
			posX += 18;
		}

		int outIdx = 0;
		for (var stack : recipe.getOutputs()) {
			builder.addSlot(RecipeIngredientRole.OUTPUT, 93 + outIdx % 2 * 20, 41 + outIdx / 2 * 20)
					.addItemStack(stack);
			outIdx++;
		}
	}
}
