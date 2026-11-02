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

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.BotaniaRenderContext;
import vazkii.botania.common.handler.ContributorList;

import java.util.Locale;

public final class ContributorFancinessHandler extends RenderLayer<AvatarRenderState, PlayerModel> {

	public ContributorFancinessHandler(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NotNull PoseStack ms, @NotNull SubmitNodeCollector collector, int light,
			@NotNull AvatarRenderState state, float yRot, float xRot) {
		ContributorList.firstStart();

		if (state.isInvisible) {
			return;
		}

		String name = playerName(state);
		if (name == null || name.isEmpty()) {
			return;
		}

		BotaniaRenderContext buffers = new BotaniaRenderContext(null);

		if (name.equals("haighyorkie")) {
			ms.pushPose();
			getParentModel().head.translateAndRotate(ms);
			ms.translate(-0.15F, -0.60F, 0F);
			ms.scale(0.4F, -0.4F, -0.4F);
			RenderHelper.renderBlockModel(ms, buffers, null, MiscellaneousModels.INSTANCE.goldfishModel,
					1, 1, 1, 0xF000F0, OverlayTexture.NO_OVERLAY);
			ms.popPose();
		}

		if (state.showCape) {
			ItemStack flower = ContributorList.getFlower(name.toLowerCase(Locale.ROOT));
			if (!flower.isEmpty()) {
				ms.pushPose();
				getParentModel().head.translateAndRotate(ms);
				ms.translate(0, -0.75, 0);
				ms.scale(0.5F, -0.5F, -0.5F);
				RenderHelper.renderItemStatic(flower, ItemDisplayContext.NONE, false,
						ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, null);
				ms.popPose();
			}
		}

		buffers.flush(collector);
	}

	/**
	 * The render state no longer carries the game profile, so look the entity back up by its id.
	 */
	private static String playerName(AvatarRenderState state) {
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return null;
		}
		Entity entity = level.getEntity(state.id);
		return entity instanceof AbstractClientPlayer player ? player.getGameProfile().name() : null;
	}
}
