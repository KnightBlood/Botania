/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.PoolOverlayProvider;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.mana.ManaPoolBlock;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.VecHelper;

import java.util.Objects;
import java.util.Random;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaPoolBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaPoolBlockEntity> {

	// Overrides for when we call this renderer from a cart
	public static int cartMana = -1;
	private final TextureAtlasSprite waterSprite;

	public ManaPoolBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
		this.waterSprite = Objects.requireNonNull(
				Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
						.getSprite(prefix("block/mana_water"))
		);
	}

	@Override
	public void render(@Nullable ManaPoolBlockEntity pool, float f, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		ms.pushPose();

		boolean fab = pool != null && ((ManaPoolBlock) pool.getBlockState().getBlock()).variant == ManaPoolBlock.Variant.FABULOUS;
		boolean diluted = pool != null && ((ManaPoolBlock) pool.getBlockState().getBlock()).variant == ManaPoolBlock.Variant.DILUTED;
		boolean creative = pool != null && ((ManaPoolBlock) pool.getBlockState().getBlock()).variant == ManaPoolBlock.Variant.CREATIVE;

		int insideUVStart = diluted ? 1 : 2;
		int insideUVEnd = 16 - insideUVStart;
		float poolBottom = insideUVStart / 16F + 0.001F;
		float poolTop = (diluted ? 5 : creative ? 9 : 7) / 16F;

		if (fab) {
			float time = ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks;
			time += new Random(pool.getBlockPos().getX() ^ pool.getBlockPos().getY() ^ pool.getBlockPos().getZ()).nextInt(100000);
			time *= 0.005F;
			int poolColor = pool.getColor().map(ColorHelper::getColorValue).orElse(-1);
			int color = vazkii.botania.common.helper.MathHelper.multiplyColor(Mth.hsvToRgb(Mth.frac(time), 0.6F, 1F), poolColor);

			int red = (color & 0xFF0000) >> 16;
			int green = (color & 0xFF00) >> 8;
			int blue = color & 0xFF;
			BlockState state = pool.getBlockState();
			BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
			RenderHelper.renderBlockModel(ms, buffers, state, model, red / 255F, green / 255F, blue / 255F, light, overlay);
		}

		if (pool != null) {
			Block below = pool.getLevel().getBlockState(pool.getBlockPos().below()).getBlock();
			if (below instanceof PoolOverlayProvider overlayProvider) {
				var overlaySpriteId = overlayProvider.getIcon(pool.getLevel(), pool.getBlockPos());
				var overlayIcon = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS).getSprite(overlaySpriteId);
				ms.pushPose();

				float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + f) / 20.0) + 1) * 0.3 + 0.2);

				ms.translate(0, poolBottom, 0);
				ms.mulPose(VecHelper.rotateX(90F));

				VertexConsumer buffer = buffers.getBuffer(RenderHelper.ICON_OVERLAY);
				RenderHelper.renderIconCropped(
						ms, buffer,
						insideUVStart, insideUVStart, insideUVEnd, insideUVEnd,
						overlayIcon, 0xFFFFFF, alpha, light
				);

				ms.popPose();
			}
		}

		int mana = pool == null ? cartMana : pool.getCurrentMana();
		int maxMana = pool == null ? -1 : pool.getMaxMana();
		if (maxMana == -1) {
			maxMana = ManaPoolBlockEntity.MAX_MANA;
		}

		float manaLevel = (float) mana / (float) maxMana;
		if (manaLevel > 0) {
			ms.pushPose();
			ms.translate(0, Mth.clampedMap(manaLevel, 0, 1, poolBottom, poolTop), 0);
			ms.mulPose(VecHelper.rotateX(90F));

			VertexConsumer buffer = buffers.getBuffer(RenderHelper.MANA_POOL_WATER);
			RenderHelper.renderIconCropped(
					ms, buffer,
					insideUVStart, insideUVStart, insideUVEnd, insideUVEnd,
					this.waterSprite, 0xFFFFFF, 1, light);

			ms.popPose();
		}
		ms.popPose();

		cartMana = -1;
	}

}
