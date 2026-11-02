/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockQuadOutput;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.block_entity.PylonBlockEntityRenderer;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.mixin.client.ItemStackRenderStateAccessor;
import vazkii.botania.mixin.client.ItemStackRenderStateLayerAccessor;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;

public final class RenderHelper {
	public static final RenderType STAR;
	public static final RenderType RECTANGLE;
	public static final RenderType CIRCLE;
	public static final RenderType RED_STRING;
	public static final RenderType LINE_1_NO_DEPTH;
	public static final RenderType LINE_4_NO_DEPTH;
	public static final RenderType LINE_5_NO_DEPTH;
	public static final RenderType LINE_8_NO_DEPTH;
	public static final RenderType SPARK;
	public static final RenderType LIGHT_RELAY;
	public static final RenderType ICON_OVERLAY;
	public static final RenderType BABYLON_ICON;
	public static final RenderType MANA_POOL_WATER;
	public static final RenderType TERRA_PLATE;
	public static final RenderType ENCHANTER;
	public static final RenderType HALO;
	public static final RenderType MANA_PYLON_GLOW = getPylonGlow("mana_pylon_glow", PylonBlockEntityRenderer.MANA_TEXTURE);
	public static final RenderType NATURA_PYLON_GLOW = getPylonGlow("natura_pylon_glow", PylonBlockEntityRenderer.NATURA_TEXTURE);
	public static final RenderType GAIA_PYLON_GLOW = getPylonGlow("gaia_pylon_glow", PylonBlockEntityRenderer.GAIA_TEXTURE);
	public static final RenderType MANA_PYLON_GLOW_DIRECT = getPylonGlowDirect("mana_pylon_glow_direct", PylonBlockEntityRenderer.MANA_TEXTURE);
	public static final RenderType NATURA_PYLON_GLOW_DIRECT = getPylonGlowDirect("natura_pylon_glow_direct", PylonBlockEntityRenderer.NATURA_TEXTURE);
	public static final RenderType GAIA_PYLON_GLOW_DIRECT = getPylonGlowDirect("gaia_pylon_glow_direct", PylonBlockEntityRenderer.GAIA_TEXTURE);

	public static final RenderType ASTROLABE_PREVIEW;
	public static final RenderType STARFIELD;
	public static final RenderType LIGHTNING;
	public static final RenderType TRANSLUCENT;

	private static final int ITEM_AND_PADDING_WIDTH = 20;

	private static final double INITIAL_OFFSET = 0.005;
	private static final double OFFSET_INCREMENT = 0.001;
	private static double offY = INITIAL_OFFSET;

	public static double getOffY() {
		return offY;
	}

	public static void incrementOffY() {
		offY += OFFSET_INCREMENT;
	}

	public static void onWorldRenderLast() {
		offY = INITIAL_OFFSET;
	}

	private static RenderType makeLayer(String name, VertexFormat format, VertexFormat.Mode mode, RenderSetup setup) {
		return RenderType.create(name, setup);
	}

	static {
		RenderSetup starSetup = RenderSetup.builder(RenderPipelines.SOLID_BLOCK)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup();
		STAR = makeLayer(ResourcesLib.PREFIX_MOD + "star", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, starSetup);

		RenderSetup rectangleSetup = RenderSetup.builder(RenderPipelines.SOLID_BLOCK)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.createRenderSetup();
		RECTANGLE = makeLayer(ResourcesLib.PREFIX_MOD + "rectangle_highlight", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, rectangleSetup);
		CIRCLE = makeLayer(ResourcesLib.PREFIX_MOD + "circle_highlight", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLES, rectangleSetup);

		RED_STRING = makeLayer(ResourcesLib.PREFIX_MOD + "red_string", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, RenderSetup.builder(RenderPipelines.LINES).createRenderSetup());
		LINE_1_NO_DEPTH = makeLayer(ResourcesLib.PREFIX_MOD + "line_1_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, RenderSetup.builder(RenderPipelines.LINES).createRenderSetup());
		LINE_4_NO_DEPTH = makeLayer(ResourcesLib.PREFIX_MOD + "line_4_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, RenderSetup.builder(RenderPipelines.LINES).createRenderSetup());
		LINE_5_NO_DEPTH = makeLayer(ResourcesLib.PREFIX_MOD + "line_5_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, RenderSetup.builder(RenderPipelines.LINES).createRenderSetup());
		LINE_8_NO_DEPTH = makeLayer(ResourcesLib.PREFIX_MOD + "line_8_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, RenderSetup.builder(RenderPipelines.LINES).createRenderSetup());

		RenderSetup sparkSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		SPARK = makeLayer(ResourcesLib.PREFIX_MOD + "spark", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, sparkSetup);

		RenderSetup lightRelaySetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			// ENTITY_SOLID's program requires both overlay (Sampler1) and lightmap (Sampler2)
			// bindings even when the vertex format carries no UV1/UV2.
			.useLightmap()
			.useOverlay()
			.createRenderSetup();
		LIGHT_RELAY = makeLayer(ResourcesLib.PREFIX_MOD + "light_relay", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, lightRelaySetup);

		RenderSetup iconOverlaySetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		ICON_OVERLAY = makeLayer(ResourcesLib.PREFIX_MOD + "icon_overlay", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, iconOverlaySetup);

		RenderSetup manaPoolWaterSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		MANA_POOL_WATER = makeLayer(ResourcesLib.PREFIX_MOD + "mana_pool_water", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, manaPoolWaterSetup);

		RenderSetup terraPlateSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		TERRA_PLATE = makeLayer(ResourcesLib.PREFIX_MOD + "terra_plate_rune", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, terraPlateSetup);

		RenderSetup enchanterSetup = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		ENCHANTER = makeLayer(ResourcesLib.PREFIX_MOD + "enchanter_rune", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, enchanterSetup);

		RenderSetup babylonSetup = RenderSetup.builder(RenderPipelines.ITEM_CUTOUT)
			.withTexture("Sampler0", Identifier.parse(ResourcesLib.MISC_BABYLON))
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			// item.vsh samples Sampler2 (lightmap) - ITEM_CUTOUT setups must bind it.
			.useLightmap()
			.createRenderSetup();
		BABYLON_ICON = makeLayer(ResourcesLib.PREFIX_MOD + "babylon", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, babylonSetup);

		RenderSetup haloSetup = RenderSetup.builder(RenderPipelines.ITEM_CUTOUT)
			.withTexture("Sampler0", FlugelTiaraItem.textureHalo)
			.useLightmap()
			.createRenderSetup();
		HALO = makeLayer(ResourcesLib.PREFIX_MOD + "halo", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, haloSetup);

		STARFIELD = makeLayer(ResourcesLib.PREFIX_MOD + "starfield", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, RenderSetup.builder(RenderPipelines.END_SKY).createRenderSetup());

		RenderSetup lightningSetup = RenderSetup.builder(RenderPipelines.LIGHTNING)
			.createRenderSetup();
		LIGHTNING = makeLayer(ResourcesLib.PREFIX_MOD + "lightning", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, lightningSetup);
		TRANSLUCENT = RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS);

		RenderSetup astrolabePreviewSetup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT_CULL)
			.withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		ASTROLABE_PREVIEW = makeLayer(ResourcesLib.PREFIX_MOD + "astrolabe_preview", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS, astrolabePreviewSetup);
	}

	private static RenderType getPylonGlowDirect(String name, Identifier texture) {
		return getPylonGlow(name, texture, true);
	}

	private static RenderType getPylonGlow(String name, Identifier texture) {
		return getPylonGlow(name, texture, false);
	}

	private static RenderType getPylonGlow(String name, Identifier texture, boolean direct) {
		RenderSetup setup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
			.withTexture("Sampler0", texture)
			.setOutputTarget(direct ? OutputTarget.MAIN_TARGET : OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		return makeLayer(ResourcesLib.PREFIX_MOD + name, DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS, setup);
	}

	public static RenderType getHaloLayer(Identifier texture) {
		RenderSetup setup = RenderSetup.builder(RenderPipelines.ITEM_CUTOUT)
			.withTexture("Sampler0", texture)
			.useLightmap()
			.createRenderSetup();
		return makeLayer(ResourcesLib.PREFIX_MOD + "crafting_halo", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, setup);
	}

	private static final Function<Identifier, RenderType> DOPPLEGANGER = Util.memoize(texture -> {
		RenderSetup setup = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
			.withTexture("Sampler0", texture)
			.setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET)
			.useLightmap()
			// entity.vsh texelFetches Sampler1 (overlay) - every ENTITY_* pipeline setup must
			// bind it or GlCommandEncoder throws "Missing sampler Sampler1" (and on Fabric,
			// where pass validation is off, the stale previous binding leaks in and corrupts
			// the rendering - the mana pool water / lexicon visual pollution).
			.useOverlay()
			.createRenderSetup();
		return makeLayer(ResourcesLib.PREFIX_MOD + "doppleganger", DefaultVertexFormat.ENTITY, VertexFormat.Mode.QUADS, setup);
	});

	public static RenderType getDopplegangerLayer(Identifier texture) {
		return DOPPLEGANGER.apply(texture);
	}

	public static void drawTexturedModalRect(GuiGraphicsExtractor gui, Identifier textureId, int x, int y, int u, int v, int width, int height) {
		drawTexturedModalRect(gui, textureId, x, y, u, v, width, height, -1);
	}

	public static void drawTexturedModalRect(GuiGraphicsExtractor gui, Identifier textureId, int x, int y, int u, int v, int width, int height, int color) {
		gui.blit(RenderPipelines.GUI_TEXTURED, textureId, x, y, u, v, width, height, 256, 256, color);
	}

	public static void renderStar(PoseStack ms, MultiBufferSource buffers, int color, float xScale, float yScale, float zScale, long seed) {
		VertexConsumer buffer = buffers.getBuffer(STAR);

		float ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
		float semiPeriodTicks = 200;
		float f1 = Mth.abs(Mth.sin((float) Math.PI / semiPeriodTicks * ticks))
				* 0.9F + 0.1F;

		float f2 = f1 > 0.F ? (f1 - 0.7F) / 0.2F : 0;
		Random random = new Random(seed);

		ms.pushPose();
		ms.scale(xScale, yScale, zScale);

		for (int i = 0; i < (f1 + f1 * f1) / 2F * 90F + 30F; i++) {
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F + f1 * 90F));
			float f3 = random.nextFloat() * 20F + 5F + f2 * 10F;
			float f4 = random.nextFloat() * 2F + 1F + f2 * 2F;
			float r = ((color & 0xFF0000) >> 16) / 255F;
			float g = ((color & 0xFF00) >> 8) / 255F;
			float b = (color & 0xFF) / 255F;
			Matrix4f mat = ms.last().pose();
			Runnable center = () -> buffer.addVertex(mat, 0, 0, 0).setColor(r, g, b, f1);
			Runnable[] vertices = {
					() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, 0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, 0, f3, 1F * f4).setColor(0, 0, 0, 0),
					() -> buffer.addVertex(mat, -0.866F * f4, f3, -0.5F * f4).setColor(0, 0, 0, 0)
			};
			triangleFan(center, vertices);
		}

		ms.popPose();
	}

	/**
	 * 26.1 submit-pipeline variant of {@link #renderStar(PoseStack, MultiBufferSource, int, float, float, float, long)}.
	 */
	public static void renderStar(PoseStack ms, OrderedSubmitNodeCollector collector, int color, float xScale, float yScale, float zScale, long seed) {
		float ticks = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
		float semiPeriodTicks = 200;
		float f1 = Mth.abs(Mth.sin((float) Math.PI / semiPeriodTicks * ticks))
				* 0.9F + 0.1F;

		float f2 = f1 > 0.F ? (f1 - 0.7F) / 0.2F : 0;
		Random random = new Random(seed);

		ms.pushPose();
		ms.scale(xScale, yScale, zScale);

		for (int i = 0; i < (f1 + f1 * f1) / 2F * 90F + 30F; i++) {
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateX(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateY(random.nextFloat() * 360F));
			ms.mulPose(VecHelper.rotateZ(random.nextFloat() * 360F + f1 * 90F));
			float f3 = random.nextFloat() * 20F + 5F + f2 * 10F;
			float f4 = random.nextFloat() * 2F + 1F + f2 * 2F;
			final float r = ((color & 0xFF0000) >> 16) / 255F;
			final float g = ((color & 0xFF00) >> 8) / 255F;
			final float b = (color & 0xFF) / 255F;
			final float fa = f1;
			final float fb = f3;
			final float fc = f4;
			collector.submitCustomGeometry(ms, STAR, (pose, buffer) -> {
				Matrix4f mat = pose.pose();
				buffer.addVertex(mat, 0, 0, 0).setColor(r, g, b, fa);
				buffer.addVertex(mat, -0.866F * fc, fb, -0.5F * fc).setColor(0, 0, 0, 0);
				buffer.addVertex(mat, 0.866F * fc, fb, -0.5F * fc).setColor(0, 0, 0, 0);
				buffer.addVertex(mat, 0, 0, 0).setColor(r, g, b, fa);
				buffer.addVertex(mat, 0.866F * fc, fb, -0.5F * fc).setColor(0, 0, 0, 0);
				buffer.addVertex(mat, 0, fb, 1F * fc).setColor(0, 0, 0, 0);
				buffer.addVertex(mat, 0, 0, 0).setColor(r, g, b, fa);
				buffer.addVertex(mat, 0, fb, 1F * fc).setColor(0, 0, 0, 0);
				buffer.addVertex(mat, -0.866F * fc, fb, -0.5F * fc).setColor(0, 0, 0, 0);
			});
		}

		ms.popPose();
	}

	public static void triangleFan(Runnable center, Runnable... vertices) {
		triangleFan(center, Arrays.asList(vertices));
	}

	public static void triangleFan(Runnable center, List<Runnable> vertices) {
		for (int i = 0; i < vertices.size() - 1; i++) {
			center.run();
			vertices.get(i).run();
			vertices.get(i + 1).run();
		}
	}

	public static void flatRectangle(VertexConsumer buffer, Matrix4f mat,
			float xMin, float xMax, float y, float zMin, float zMax,
			int r, int g, int b, int a) {
		buffer.addVertex(mat, xMax, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMin).setColor(r, g, b, a);
		buffer.addVertex(mat, xMin, y, zMax).setColor(r, g, b, a);
		buffer.addVertex(mat, xMax, y, zMax).setColor(r, g, b, a);
	}

	public static void renderProgressPie(GuiGraphicsExtractor gui, int x, int y, float progress, ItemStack stack) {
		gui.item(stack, x, y);

		int degs = (int) (360 * progress);
		if (degs <= 0) {
			return;
		}

		int r = 10;
		int centerX = x + 8;
		int centerY = y + 8;
		float a = 0.5F + 0.2F * (Mth.cos((ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) / 10D) * 0.5F + 0.5F);

		int centerColor = ARGB.colorFromFloat(a, 0F, 0.5F, 0.5F);
		int edgeColor = ARGB.colorFromFloat(a, 0F, 1F, 0.5F);

		// 26.1: submitting custom GUI elements is platform-specific (NeoForge extension method
		// vs vanilla GuiRenderState); peek the extractor's scissor stack so the pie is clipped
		// like vanilla elements.
		var scissor = ClientXplatAbstractions.INSTANCE.peekGuiScissor(gui);
		ClientXplatAbstractions.INSTANCE.submitGuiElementRenderState(gui,
				new ProgressPieRenderState(new org.joml.Matrix3x2f(gui.pose()),
						centerX, centerY, r, degs, centerColor, edgeColor, scissor));
	}

	/**
	 * Appends a block state model to an item render state as a plain quad layer, in the same way as
	 * the old {@code ItemRenderer.render(..., BakedModel)} overload rendered an explicit model.
	 */
	public static void appendBlockModelLayers(ItemStackRenderState output, ItemDisplayContext displayContext,
			BlockStateModel model) {
		((ItemStackRenderStateAccessor) output).botania$setDisplayContext(displayContext);
		output.ensureCapacity(1);
		ItemStackRenderState.LayerRenderState layer = output.newLayer();
		List<BakedQuad> quads = layer.prepareQuadList();

		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(), parts);
		for (BlockStateModelPart part : parts) {
			for (Direction direction : Direction.values()) {
				quads.addAll(part.getQuads(direction));
			}
			quads.addAll(part.getQuads(null));
		}
		layer.setUsesBlockLight(true);
	}

	public static void renderItemCustomColor(LivingEntity entity, ItemStack stack, int color, PoseStack ms, MultiBufferSource buffers, int light, int overlay, @Nullable BlockStateModel model) {
		ItemStackRenderState renderState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, stack, ItemDisplayContext.NONE, entity);
		drawItemLayers(ms, buffers, light, overlay, renderState, false, color);
	}

	/**
	 * Renders an item stack into a world-space {@link MultiBufferSource}, in the same way as
	 * {@code ItemRenderer.renderStatic} did in older versions. The stack's model is resolved from the
	 * stack's {@code item_model} component.
	 */
	public static void renderItemStatic(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded,
			PoseStack ms, MultiBufferSource buffers, int light, int overlay, @Nullable Level level) {
		renderItemStatic(stack, displayContext, leftHanded, ms, buffers, light, overlay, level, 0);
	}

	/**
	 * Renders an item stack into a world-space {@link MultiBufferSource}. The stack's model is resolved
	 * from the stack's {@code item_model} component.
	 */
	public static void renderItemStatic(ItemStack stack, ItemDisplayContext displayContext, boolean leftHanded,
			PoseStack ms, MultiBufferSource buffers, int light, int overlay, @Nullable Level level, int seed) {
		Minecraft mc = Minecraft.getInstance();
		ItemStackRenderState renderState = new ItemStackRenderState();
		mc.getItemModelResolver().updateForTopItem(renderState, stack, displayContext, level, null, seed);
		drawItemLayers(ms, buffers, light, overlay, renderState, stack, leftHanded);
	}

	/**
	 * Renders an item stack into a world-space {@link MultiBufferSource}, resolving the model for a
	 * living holder.
	 */
	public static void renderItemStatic(LivingEntity living, ItemStack stack, ItemDisplayContext displayContext,
			PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		ItemStackRenderState renderState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, stack, displayContext, living);
		drawItemLayers(ms, buffers, light, overlay, renderState, stack, false);
	}

	/**
	 * Renders an item stack with a tint multiplied over the final color.
	 */
	public static void renderItemStaticColor(LivingEntity living, ItemStack stack, int color,
			PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		ItemStackRenderState renderState = new ItemStackRenderState();
		Minecraft.getInstance().getItemModelResolver().updateForLiving(renderState, stack, ItemDisplayContext.NONE, living);
		drawItemLayers(ms, buffers, light, overlay, renderState, false, color);
	}

	private static void drawItemLayers(PoseStack ms, MultiBufferSource buffers, int light, int overlay,
			ItemStackRenderState renderState, ItemStack stack, boolean leftHanded) {
		drawItemLayers(ms, buffers, light, overlay, renderState, leftHanded, -1);
	}

	/**
	 * Submits every quad of every active layer of the given render state, applying the layer's own
	 * transform on top of the incoming pose stack.
	 */
	private static void drawItemLayers(PoseStack ms, MultiBufferSource buffers, int light, int overlay,
			ItemStackRenderState renderState, boolean leftHanded, int color) {
		ItemStackRenderStateAccessor accessor = (ItemStackRenderStateAccessor) renderState;
		ItemStackRenderState.LayerRenderState[] layers = accessor.botania$getLayers();
		int layerCount = accessor.botania$getActiveLayerCount();

		QuadInstance instance = new QuadInstance();
		instance.setLightCoords(light);
		instance.setOverlayCoords(overlay);
		instance.setColor(color);

		for (int i = 0; i < layerCount; i++) {
			ItemStackRenderState.LayerRenderState layer = layers[i];
			List<BakedQuad> quads = layer.prepareQuadList();
			if (quads.isEmpty()) {
				continue;
			}

			VertexConsumer buffer = buffers.getBuffer(getItemRenderType(quads.get(0)));
			ms.pushPose();
			((ItemStackRenderStateLayerAccessor) layer).botania$applyTransform(ms.last());
			for (BakedQuad quad : quads) {
				buffer.putBakedQuad(ms.last(), quad, instance);
			}
			ms.popPose();
		}
	}

	/**
	 * Renders a block state model directly into a buffer, in the same way as
	 * {@code BlockModelRenderer.renderModel} did in older versions.
	 */
	public static void renderBlockModel(PoseStack ms, VertexConsumer buffer, @Nullable BlockState state,
			BlockStateModel model, float red, float green, float blue, int light, int overlay) {
		renderBlockModel(ms, buffer, state, model, red, green, blue, light, overlay, RandomSource.create());
	}

	public static void renderBlockModel(PoseStack ms, VertexConsumer buffer, @Nullable BlockState state,
			BlockStateModel model, float red, float green, float blue, int light, int overlay, RandomSource random) {
		Minecraft mc = Minecraft.getInstance();
		ModelBlockRenderer renderer = new ModelBlockRenderer(mc.options.ambientOcclusion().get(), false, mc.getBlockColors());

		BlockState actualState = state != null ? state : Blocks.STONE.defaultBlockState();
		BlockAndTintGetter level = BlockAndTintGetter.EMPTY;
		BlockPos pos = BlockPos.ZERO;

		BlockQuadOutput output = (x, y, z, quad, quadInstance) -> {
			ms.pushPose();
			ms.translate(x, y, z);
			quadInstance.setColor(ARGB.colorFromFloat(1.0F, red, green, blue));
			quadInstance.setLightCoords(light);
			quadInstance.setOverlayCoords(overlay);
			buffer.putBakedQuad(ms.last(), quad, quadInstance);
			ms.popPose();
		};

		renderer.tesselateBlock(output, 0.0F, 0.0F, 0.0F, level, pos, actualState, model, random.nextLong());
	}

	/**
	 * Renders a block state model into a {@link MultiBufferSource}, using the block sheet appropriate
	 * for the state.
	 */
	public static void renderBlockModel(PoseStack ms, MultiBufferSource buffers, BlockState state,
			BlockStateModel model, float red, float green, float blue, int light, int overlay) {
		RenderType renderType = model.hasMaterialFlag(1)
				? RenderTypes.translucentMovingBlock()
				: RenderTypes.cutoutMovingBlock();
		VertexConsumer buffer = buffers.getBuffer(renderType);
		renderBlockModel(ms, buffer, state, model, red, green, blue, light, overlay);
	}

	/**
	 * 26.1 submit-pipeline variant of {@link #renderBlockModel(PoseStack, MultiBufferSource, BlockState,
	 * BlockStateModel, float, float, float, int, int)}, for use inside {@code EntityRenderer#submit}.
	 */
	public static void renderBlockModel(PoseStack ms, OrderedSubmitNodeCollector collector, @Nullable BlockState state,
			BlockStateModel model, int light, int overlay) {
		List<BlockStateModelPart> parts = new ArrayList<>();
		model.collectParts(RandomSource.create(), parts);
		RenderType renderType = model.hasMaterialFlag(1)
				? RenderTypes.translucentMovingBlock()
				: RenderTypes.cutoutMovingBlock();
		collector.submitBlockModel(ms, renderType, parts, new int[0], light, overlay, 0);
	}

	private static RenderType getItemRenderType(BakedQuad quad) {
		return quad.materialInfo().itemRenderType();
	}

	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			float startX, float startY, float endX, float endY,
			int uvStartX, int uvStartY, int uvEndX, int uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		Matrix4f mat = ms.last().pose();
		float red = ((color >> 16) & 0xFF) / 255F;
		float green = ((color >> 8) & 0xFF) / 255F;
		float blue = (color & 0xFF) / 255F;

		// 26.1: these layers run on entity-family pipelines whose vertex format is DefaultVertexFormat.ENTITY,
		// so every vertex must supply the overlay (UV1) and normal elements as well, or BufferBuilder rejects
		// the vertex with "Missing elements in vertex".
		buffer.addVertex(mat, startX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvEndY))
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0, 0, 1);
		buffer.addVertex(mat, endX, endY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvEndY))
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0, 0, 1);
		buffer.addVertex(mat, endX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvEndX), icon.getV(uvStartY))
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0, 0, 1);
		buffer.addVertex(mat, startX, startY, 0).setColor(red, green, blue, alpha).setUv(icon.getU(uvStartX), icon.getV(uvStartY))
				.setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(ms.last(), 0, 0, 1);
	}

	public static void renderIconCropped(
			PoseStack ms, VertexConsumer buffer,
			int uvStartX, int uvStartY, int uvEndX, int uvEndY,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconFullBright(
				ms, buffer,
				uvStartX / 16F, uvStartY / 16F, uvEndX / 16F, uvEndY / 16F,
				uvStartX, uvStartY, uvEndX, uvEndY,
				icon, color, alpha, light
		);
	}

	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha, int light) {
		renderIconCropped(
				ms, buffer,
				0, 0, 16, 16,
				icon, color, alpha, light
		);
	}

	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, int color, float alpha) {
		int fullbright = 0xF000F0;
		renderIconFullBright(ms, buffer, icon, color, alpha, fullbright);
	}

	public static void renderIconFullBright(
			PoseStack ms, VertexConsumer buffer,
			TextureAtlasSprite icon, float alpha) {
		renderIconFullBright(ms, buffer, icon, 0xFFFFFF, alpha);
	}

	/**
	 * Renders a "ghost" item into a GUI.
	 * <p>
	 * 26.1 renders GUI items through a shared item atlas which always blits with a fully opaque tint,
	 * so a per-item alpha can no longer be applied to the item geometry itself. The ghosting is instead
	 * approximated by veiling the item with the vanilla GUI panel colour.
	 */
	public static void renderGuiItemAlpha(GuiGraphicsExtractor gui, ItemStack stack, int x, int y, int alpha, @Nullable BlockStateModel model) {
		gui.item(stack, x, y);
		int veil = (((255 - (alpha & 0xFF)) & 0xFF) << 24) | 0xC6C6C6;
		gui.fill(x, y, x + 16, y + 16, veil);
	}

	private static MultiBufferSource wrapBuffer(MultiBufferSource buffer, int alpha, boolean forceTranslucent) {
		return renderType -> new GhostVertexConsumer(buffer.getBuffer(forceTranslucent ? TRANSLUCENT : renderType), alpha);
	}

	public static void renderHUDBox(GuiGraphicsExtractor gui, int startX, int startY, int endX, int endY) {
		gui.fill(startX, startY, endX, endY, 0x40000000);
		gui.fill(startX - 2, startY - 2, endX + 2, endY + 2, 0x40000000);
	}

	public static void renderItemWithName(GuiGraphicsExtractor gui, Minecraft mc, ItemStack itemStack, int startX, int startY, int color) {
		if (!itemStack.isEmpty()) {
			gui.text(mc.font, itemStack.getHoverName(), startX + ITEM_AND_PADDING_WIDTH, startY + 4, color);
			gui.item(itemStack, startX, startY);
		}
	}

	public static void renderItemWithNameCentered(GuiGraphicsExtractor gui, Minecraft mc, ItemStack itemStack, int startY, int color) {
		int centerX = mc.getWindow().getGuiScaledWidth() / 2;
		int startX = centerX - (ITEM_AND_PADDING_WIDTH + mc.font.width(itemStack.getHoverName())) / 2;
		renderItemWithName(gui, mc, itemStack, startX, startY, color);
	}

	public static int itemWithNameWidth(Minecraft mc, ItemStack itemStack) {
		return ITEM_AND_PADDING_WIDTH + mc.font.width(itemStack.getHoverName());
	}

	public record GhostVertexConsumer(VertexConsumer wrapped, int alpha) implements VertexConsumer {
		@Override
		public VertexConsumer addVertex(float x, float y, float z) {
			return wrapped.addVertex(x, y, z);
		}

		@Override
		public VertexConsumer setColor(int red, int green, int blue, int alpha) {
			return wrapped.setColor(red, green, blue, (alpha * this.alpha) / 0xFF);
		}

		@Override
		public VertexConsumer setColor(int color) {
			return wrapped.setColor(color);
		}

		@Override
		public VertexConsumer setUv(float u, float v) {
			return wrapped.setUv(u, v);
		}

		@Override
		public VertexConsumer setUv1(int u, int v) {
			return wrapped.setUv1(u, v);
		}

		@Override
		public VertexConsumer setUv2(int u, int v) {
			return wrapped.setUv2(u, v);
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z) {
			return wrapped.setNormal(x, y, z);
		}

		@Override
		public VertexConsumer setLineWidth(float width) {
			return wrapped.setLineWidth(width);
		}
	}
}
