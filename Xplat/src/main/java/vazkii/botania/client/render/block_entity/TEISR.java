/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.client.core.handler.ClientTickHandler;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Renders a block entity's in-the-world appearance as an item model, by driving that block entity's
 * renderer with a dummy block entity.
 */
public class TEISR implements SpecialModelRenderer<Unit> {
	private final Block block;
	private final Supplier<BlockEntity> dummy;

	public TEISR(Block block) {
		Preconditions.checkArgument(block instanceof EntityBlock);
		this.block = block;
		this.dummy = Suppliers.memoize(() -> ((EntityBlock) block).newBlockEntity(ManaBurst.NO_SOURCE, block.defaultBlockState()));
	}

	/**
	 * Submits this renderer's geometry. Exposed so subclasses that need a different pose or variant
	 * can reuse the block entity renderer directly.
	 */
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords) {
		BlockEntity blockEntity = this.dummy.get();
		if (resolveRenderer(blockEntity) instanceof BotaniaBlockEntityRenderer<?> renderer) {
			BotaniaRenderContext buffers = new BotaniaRenderContext(null);
			renderer.renderUntyped(blockEntity, ClientTickHandler.partialTicks, poseStack, buffers,
					lightCoords, overlayCoords);
			buffers.flush(collector);
		}
	}

	@Override
	public void submit(Unit argument, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords,
			int overlayCoords, boolean hasFoil, int outlineColor) {
		submit(poseStack, collector, lightCoords, overlayCoords);
	}

	@Override
	public void getExtents(Consumer<Vector3fc> output) {}

	protected Block block() {
		return this.block;
	}

	@Override
	public @Nullable Unit extractArgument(ItemStack stack) {
		return stack.is(this.block.asItem()) ? Unit.INSTANCE : null;
	}

	@SuppressWarnings("unchecked")
	private static BlockEntityRenderer<BlockEntity, ?> resolveRenderer(BlockEntity blockEntity) {
		return (BlockEntityRenderer<BlockEntity, ?>) (BlockEntityRenderer<?, ?>)
				Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
	}

	/**
	 * Data-driven descriptor: the block whose block entity should be rendered in item form.
	 */
	public record Unbaked(Identifier block) implements SpecialModelRenderer.Unbaked<Unit> {
		public static final MapCodec<Unbaked> MAP_CODEC = Identifier.CODEC
				.xmap(Unbaked::new, Unbaked::block)
				.fieldOf("block");

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public @Nullable SpecialModelRenderer<Unit> bake(SpecialModelRenderer.BakingContext context) {
			return BuiltInRegistries.BLOCK.get(block)
					.<SpecialModelRenderer<Unit>>map(holder -> new TEISR(holder.value()))
					.orElse(null);
		}
	}
}
