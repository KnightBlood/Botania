/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.BotaniaRenderContext;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;

public class TerrasteelHelmetLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
	public TerrasteelHelmetLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NotNull PoseStack ms, @NotNull SubmitNodeCollector collector, int light,
			@NotNull AvatarRenderState state, float yRot, float xRot) {
		ItemStack helm = state.headEquipment;
		if (!helm.isEmpty() && helm.getItem() instanceof TerrasteelHelmItem terraHelm) {
			if (TerrasteelHelmItem.hasAnyWill(helm) && !terraformHasInk(terraHelm, helm)) {
				ms.pushPose();
				getParentModel().head.translateAndRotate(ms);
				ms.translate(-0.2, -0.15, -0.3);
				ms.scale(0.4F, -0.4F, -0.4F);
				BotaniaRenderContext buffers = new BotaniaRenderContext(null);
				BlockStateModel model = MiscellaneousModels.INSTANCE.terrasteelHelmWillModel;
				RenderHelper.renderBlockModel(ms, buffers, null, model,
						1, 1, 1, light, OverlayTexture.NO_OVERLAY);
				buffers.flush(collector);
				ms.popPose();
			}
		}
	}

	@SoftImplement("PhantomInkable")
	private static boolean terraformHasInk(TerrasteelHelmItem helm, ItemStack stack) {
		return helm.hasPhantomInk(stack);
	}
}
