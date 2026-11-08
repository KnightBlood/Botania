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

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.CocoonBlockEntity;
import vazkii.botania.common.helper.VecHelper;

public class CocoonBlockEntityRenderer extends BotaniaBlockEntityRenderer<CocoonBlockEntity> {
	public CocoonBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@NotNull CocoonBlockEntity cocoon, float partialTicks, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		float rot = 0F;
		float modval = 60F - (float) cocoon.timePassed / (float) CocoonBlockEntity.TOTAL_TIME * 30F;
		if (cocoon.timePassed % modval < 10) {
			float mod = (cocoon.timePassed + partialTicks) % modval;
			float v = mod / 5 * (float) Math.PI * 2;
			rot = (float) Math.sin(v) * (float) Math.log(cocoon.timePassed + partialTicks);
		}

		ms.pushPose();
		ms.translate(0.5, 0, 0);
		ms.mulPose(VecHelper.rotateX(rot));
		ms.translate(-0.5, 0, 0);
		BlockState state = cocoon.getBlockState();
		BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
		RenderHelper.renderBlockModel(ms, buffers, state, model, 1, 1, 1, light, overlay);
		ms.popPose();
	}
}
