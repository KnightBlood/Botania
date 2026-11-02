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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.render.block_entity.BotaniaRenderContext;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.xplat.BotaniaConfig;

public final class ManaTabletRenderHandler extends RenderLayer<AvatarRenderState, PlayerModel> {

	public ManaTabletRenderHandler(RenderLayerParent<AvatarRenderState, PlayerModel> renderer) {
		super(renderer);
	}

	@Override
	public void submit(@NotNull PoseStack ms, @NotNull SubmitNodeCollector collector, int light,
			@NotNull AvatarRenderState state, float yRot, float xRot) {
		if (!BotaniaConfig.client().renderAccessories() || state.isInvisible) {
			return;
		}

		Player player = resolvePlayer(state);
		if (player == null) {
			return;
		}

		BotaniaRenderContext buffers = new BotaniaRenderContext(null);
		boolean renderedOne = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			ItemStack stack = player.getInventory().getItem(i);
			if (!stack.isEmpty() && stack.is(BotaniaItems.manaTablet)) {
				ms.pushPose();
				boolean armor = !state.legsEquipment.isEmpty();

				getParentModel().body.translateAndRotate(ms);
				ms.translate(0, 0.65, 0);
				if (renderedOne) {
					ms.translate(armor ? 0.3 : 0.25, 0, 0);
					ms.mulPose(VecHelper.rotateY(-90F));
				} else {
					ms.translate(armor ? -0.3 : -0.25, 0, 0);
					ms.mulPose(VecHelper.rotateY(90F));
				}

				ms.scale(0.375F, -0.375F, -0.375F);
				RenderHelper.renderItemStatic(stack, ItemDisplayContext.NONE, false,
						ms, buffers, 0xF000F0, OverlayTexture.NO_OVERLAY, player.level(), player.getId());
				ms.popPose();

				if (renderedOne) {
					break;
				}
				renderedOne = true;
			}
		}
		buffers.flush(collector);
	}

	private static Player resolvePlayer(AvatarRenderState state) {
		var level = Minecraft.getInstance().level;
		if (level == null) {
			return null;
		}
		Entity entity = level.getEntity(state.id);
		return entity instanceof Player player ? player : null;
	}
}
