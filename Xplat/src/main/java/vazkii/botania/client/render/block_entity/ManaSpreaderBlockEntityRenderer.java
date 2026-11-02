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
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

import vazkii.botania.api.state.BotaniaStateProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.mana.ManaSpreaderBlockEntity;
import vazkii.botania.common.block.mana.ManaSpreaderBlock;
import vazkii.botania.common.helper.VecHelper;

import java.util.Random;

public class ManaSpreaderBlockEntityRenderer extends BotaniaBlockEntityRenderer<ManaSpreaderBlockEntity> {

	public ManaSpreaderBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {}

	@Override
	public void render(@NotNull ManaSpreaderBlockEntity spreader, float partialTicks, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		ms.pushPose();

		ms.translate(0.5F, 0.5, 0.5F);

		Quaternionf transform = VecHelper.rotateY(spreader.rotationX + 90F);
		transform.mul(VecHelper.rotateX(spreader.rotationY));
		ms.mulPose(transform);

		ms.translate(-0.5F, -0.5F, -0.5F);

		double time = ClientTickHandler.ticksInGame + partialTicks;

		float r = 1, g = 1, b = 1;
		if (spreader.getVariant() == ManaSpreaderBlock.Variant.GAIA) {
			int color = Mth.hsvToRgb((float) ((time * 2 + new Random(spreader.getBlockPos().hashCode()).nextInt(10000)) % 360) / 360F, 0.4F, 0.9F);
			r = (color >> 16 & 0xFF) / 255F;
			g = (color >> 8 & 0xFF) / 255F;
			b = (color & 0xFF) / 255F;
		}

		BlockStateModel spreaderModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(spreader.getBlockState());
		RenderHelper.renderBlockModel(ms, buffers, spreader.getBlockState(),
				spreaderModel, r, g, b, light, overlay);

		ms.pushPose();
		ms.translate(0.5, 0.5, 0.5);
		ms.mulPose(VecHelper.rotateY((float) time % 360));
		ms.translate(-0.5, -0.5, -0.5);
		ms.translate(0F, (float) Math.sin(time / 20.0) * 0.05F, 0F);
		BlockStateModel core = getCoreModel(spreader);
		RenderHelper.renderBlockModel(ms, buffers, spreader.getBlockState(),
				core, 1, 1, 1, light, overlay);
		ms.popPose();

		ItemStack stack = spreader.getItemHandler().getItem(0);
		if (!stack.isEmpty()) {
			ms.pushPose();
			ms.translate(0.5F, 0.5F, 0.094F);
			ms.mulPose(VecHelper.rotateZ(180));
			ms.mulPose(VecHelper.rotateX(180));
			// Prevents z-fighting. Otherwise not noticeable.
			ms.scale(0.997F, 0.997F, 1F);
			RenderHelper.renderItemStatic(stack, ItemDisplayContext.NONE, false, ms, buffers, light, overlay, spreader.getLevel());
			ms.popPose();
		}

		if (spreader.paddingColor != null) {
			ms.pushPose();
			// The padding model is facing up so that the textures are rotated the correct way
			// It's simpler to do this than mess with rotation and UV in the json model
			ms.translate(0.5F, 0.5F, 0.5F);
			ms.mulPose(VecHelper.rotateX(-90));
			ms.mulPose(VecHelper.rotateY(180));
			ms.translate(-0.5F, -0.5F, -0.5F);
			BlockStateModel paddingModel = getPaddingModel(spreader.paddingColor);
			RenderHelper.renderBlockModel(ms, buffers, spreader.getBlockState(),
					paddingModel, r, g, b, light, overlay);
			ms.popPose();
		}

		ms.popPose();

		if (spreader.getBlockState().getValue(BotaniaStateProperties.HAS_SCAFFOLDING)) {
			BlockStateModel scaffolding = getScaffoldingModel(spreader);
			RenderHelper.renderBlockModel(ms, buffers, spreader.getBlockState(),
					scaffolding, r, g, b, light, overlay);
		}

	}

	private BlockStateModel getCoreModel(ManaSpreaderBlockEntity tile) {
		return switch (tile.getVariant()) {
			case GAIA -> MiscellaneousModels.INSTANCE.gaiaSpreaderCore;
			case REDSTONE -> MiscellaneousModels.INSTANCE.redstoneSpreaderCore;
			case ELVEN -> MiscellaneousModels.INSTANCE.elvenSpreaderCore;
			case MANA -> MiscellaneousModels.INSTANCE.manaSpreaderCore;
		};
	}

	private BlockStateModel getPaddingModel(DyeColor color) {
		return MiscellaneousModels.INSTANCE.spreaderPaddings.get(color);
	}

	private BlockStateModel getScaffoldingModel(ManaSpreaderBlockEntity tile) {
		return switch (tile.getVariant()) {
			case MANA, REDSTONE -> MiscellaneousModels.INSTANCE.manaSpreaderScaffolding;
			case ELVEN -> MiscellaneousModels.INSTANCE.elvenSpreaderScaffolding;
			case GAIA -> MiscellaneousModels.INSTANCE.gaiaSpreaderScaffolding;
		};
	}
}
