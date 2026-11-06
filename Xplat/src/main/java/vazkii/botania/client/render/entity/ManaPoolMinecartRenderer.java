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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.BotaniaRenderContext;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.entity.ManaPoolMinecartEntity;

public class ManaPoolMinecartRenderer extends AbstractMinecartRenderer<ManaPoolMinecartEntity, MinecartRenderState> {
	private static final ManaPoolBlockEntity DUMMY = new ManaPoolBlockEntity(ManaBurst.NO_SOURCE, BotaniaBlocks.manaPool.defaultBlockState());
	private static boolean warnedMissingPoolRenderer = false;

	public ManaPoolMinecartRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, ModelLayers.MINECART);
	}

	@Override
	public MinecartRenderState createRenderState() {
		return new MinecartRenderState();
	}

	@Override
	public void extractRenderState(ManaPoolMinecartEntity poolCart, MinecartRenderState state, float partialTicks) {
		super.extractRenderState(poolCart, state, partialTicks);
		ManaPoolBlockEntityRenderer.cartMana = poolCart.getMana();
	}

	@Override
	protected void submitMinecartContents(MinecartRenderState state, BlockModelRenderState blockState, PoseStack ms, SubmitNodeCollector collector, int light) {
		super.submitMinecartContents(state, blockState, ms, collector, light);

		// 1.20.1 drove the mana pool BER with a dummy block entity whose render() was passed
		// null, making it read ManaPoolBlockEntityRenderer.cartMana. 26.1 keeps getRenderer(BE)
		// but rendering is deferred, so record into a BotaniaRenderContext and replay it.
		BlockEntityRenderer<?, ?> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(DUMMY);
		if (!(renderer instanceof ManaPoolBlockEntityRenderer poolRenderer)) {
			if (!warnedMissingPoolRenderer) {
				warnedMissingPoolRenderer = true;
				BotaniaAPI.LOGGER.warn("Mana pool block entity renderer is unavailable; mana inside mana pool minecarts will not be drawn");
			}
			return;
		}

		BotaniaRenderContext buffers = new BotaniaRenderContext(null);
		poolRenderer.render(null, ClientTickHandler.partialTicks, ms, buffers, light, OverlayTexture.NO_OVERLAY);
		buffers.flush(collector);
	}

}
