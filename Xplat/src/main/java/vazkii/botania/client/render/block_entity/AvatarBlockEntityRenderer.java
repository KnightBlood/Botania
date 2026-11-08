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

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.AvatarWieldable;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.AvatarModel;
import vazkii.botania.client.model.BotaniaModelLayers;
import vazkii.botania.common.block.block_entity.AvatarBlockEntity;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.XplatAbstractions;

public class AvatarBlockEntityRenderer extends BotaniaBlockEntityRenderer<AvatarBlockEntity> {

	private static final float[] ROTATIONS = new float[] {
			180F, 0F, 90F, 270F
	};

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_AVATAR);
	private final AvatarModel model;

	public AvatarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
		model = new AvatarModel(context.bakeLayer(BotaniaModelLayers.AVATAR));
	}

	@Override
	public void render(@Nullable AvatarBlockEntity avatar, float pticks, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		ms.pushPose();
		Direction facing = avatar != null && avatar.getLevel() != null ? avatar.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING) : Direction.SOUTH;

		ms.translate(0.5F, 1.6F, 0.5F);
		ms.scale(1F, -1F, -1F);
		ms.mulPose(VecHelper.rotateY(ROTATIONS[Math.max(Math.min(ROTATIONS.length - 1, facing.get3DDataValue() - 2), 0)]));
		VertexConsumer buffer = buffers.getBuffer(model.renderType(texture));
		model.renderToBuffer(ms, buffer, light, overlay, -1);

		if (avatar != null) {
			ItemStack stack = avatar.getItemHandler().getItem(0);
			if (!stack.isEmpty()) {
				ms.pushPose();
				float s = 0.6F;
				ms.scale(s, s, s);
				ms.translate(-0.5F, 2F, -0.25F);
				ms.mulPose(VecHelper.rotateX(-70));
				RenderHelper.renderItemStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, ms, buffers, light, overlay, avatar.getLevel(), 0);
				ms.popPose();

				AvatarWieldable wieldable = XplatAbstractions.INSTANCE.findAvatarWieldable(stack);
				if (wieldable != null) {
					buffer = buffers.getBuffer(RenderTypes.entityTranslucent(wieldable.getOverlayResource(avatar)));
					s = 1.01F;

					ms.pushPose();
					ms.scale(s, s, s);
					ms.translate(0F, -0.01F, 0F);
					float alpha = (float) Math.sin(ClientTickHandler.ticksInGame / 20D) / 2F + 0.5F;
					model.renderToBuffer(ms, buffer, 0xF000F0, overlay, ARGB.white(alpha));
					ms.popPose();
				}
			}
		}
		ms.popPose();
	}

}
