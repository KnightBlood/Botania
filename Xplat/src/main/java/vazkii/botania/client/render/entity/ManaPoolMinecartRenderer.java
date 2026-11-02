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

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.client.render.block_entity.ManaPoolBlockEntityRenderer;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.entity.ManaPoolMinecartEntity;

public class ManaPoolMinecartRenderer extends AbstractMinecartRenderer<ManaPoolMinecartEntity, MinecartRenderState> {
	private static final ManaPoolBlockEntity DUMMY = new ManaPoolBlockEntity(ManaBurst.NO_SOURCE, BotaniaBlocks.manaPool.defaultBlockState());

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
		// TODO 26.1: dummy mana pool block entity rendering inside the cart (BER submit API pending migration), DUMMY kept for reference
		if (DUMMY == null) {
			throw new IllegalStateException();
		}
	}

}
