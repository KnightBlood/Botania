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

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class ManaPumpBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaPumpBlockEntity> {
	public static BlockStateModel headModel = null;

	public ManaPumpBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(ManaPumpBlockEntity pump, float partialTicks, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		ms.pushPose();
		ms.translate(0.5, 0, 0.5);
		float angle = 0;
		switch (pump.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
			default:
			case NORTH:
				break;
			case SOUTH:
				angle = 180;
				break;
			case EAST:
				angle = -90;
				break;
			case WEST:
				angle = 90;
				break;
		}
		ms.mulPose(VecHelper.rotateY(angle));
		ms.translate(-0.5, 0, -0.5);
		double diff = Math.max(0F, Math.min(8F, pump.innerRingPos + pump.moving * partialTicks));
		ms.translate(0, 0, diff / 14);
		RenderHelper.renderBlockModel(ms, buffers, null, headModel, 1, 1, 1, light, overlay);
		ms.popPose();
	}
}
