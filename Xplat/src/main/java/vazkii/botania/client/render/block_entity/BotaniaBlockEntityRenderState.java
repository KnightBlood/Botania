/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

import org.jetbrains.annotations.Nullable;

/**
 * A render state that keeps a reference to the block entity it was extracted from, so that the
 * renderer can keep using the block entity directly in {@code submit}. Extraction and submission
 * happen within the same frame, so the reference stays valid.
 */
public class BotaniaBlockEntityRenderState<T extends BlockEntity> extends BlockEntityRenderState {
	public @Nullable T blockEntity;
	public float partialTicks;
}
