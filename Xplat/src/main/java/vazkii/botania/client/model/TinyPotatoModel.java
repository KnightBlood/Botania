/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

import vazkii.botania.client.render.block_entity.TinyPotatoBlockEntityRenderer;

/**
 * Item model for the Tiny Potato. Named potatoes render as their own model, which used to be
 * expressed through {@code ItemOverrides}; in 26.1 the switch has to happen while updating the
 * item render state instead.
 */
public class TinyPotatoModel implements ItemModel {
	public static final TinyPotatoModel INSTANCE = new TinyPotatoModel();

	@Override
	public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
			ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
		output.appendModelIdentityElement(this);
		TinyPotatoBlockEntityRenderer.updateTinyPotatoRenderState(output, stack, resolver, displayContext, level, owner, seed);
	}

	public static class Unbaked implements ItemModel.Unbaked {
		public static final Unbaked INSTANCE = new Unbaked();

		@Override
		public ItemModel bake(net.minecraft.client.renderer.item.ItemModel.BakingContext context, Matrix4fc transformation) {
			return TinyPotatoModel.INSTANCE;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver) {}

		@Override
		public com.mojang.serialization.MapCodec<? extends ItemModel.Unbaked> type() {
			return MAP_CODEC;
		}
	}

	public static final com.mojang.serialization.MapCodec<Unbaked> MAP_CODEC = com.mojang.serialization.MapCodec.unit(Unbaked.INSTANCE);
}
