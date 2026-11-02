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

import net.minecraft.data.AtlasIds;
import net.minecraft.client.Minecraft;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.inventory.InventoryMenu;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.TerrestrialAgglomerationPlateBlockEntity;
import vazkii.botania.common.helper.VecHelper;

import java.util.Objects;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class TerrestrialAgglomerationPlateBlockEntityRenderer extends BotaniaBlockEntityRenderer<TerrestrialAgglomerationPlateBlockEntity> {
	private final TextureAtlasSprite overlaySprite;

	public TerrestrialAgglomerationPlateBlockEntityRenderer(BlockEntityRendererProvider.Context manager) {
		this.overlaySprite = Objects.requireNonNull(
				Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS)
						.getSprite(prefix("block/terra_plate_overlay"))
		);
	}

	@Override
	public void render(@NotNull TerrestrialAgglomerationPlateBlockEntity plate, float f, PoseStack ms, BotaniaRenderContext buffers, int light, int overlay) {
		float alphaMod = Math.min(1.0F, plate.getCompletion() / 0.1F);

		ms.pushPose();
		ms.translate(0F, 3F / 16F + 0.001F, 0F);
		ms.mulPose(VecHelper.rotateX(90F));

		float alpha = (float) ((Math.sin((ClientTickHandler.ticksInGame + f) / 8D) + 1D) / 5D + 0.6D) * alphaMod;

		VertexConsumer buffer = buffers.getBuffer(RenderHelper.TERRA_PLATE);
		RenderHelper.renderIconFullBright(ms, buffer, this.overlaySprite, alpha);

		ms.popPose();
	}

}
