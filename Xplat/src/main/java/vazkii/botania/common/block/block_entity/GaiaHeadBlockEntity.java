/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.GaiaHeadBlock;

public class GaiaHeadBlockEntity extends SkullBlockEntity {
	public GaiaHeadBlockEntity(BlockPos pos, BlockState state) {
		super(pos, state);
	}

	@Override
	public boolean isValidBlockState(BlockState state) {
		// 26.1: BlockEntity's constructor validates via the `type` field, which SkullBlockEntity
		// hardcodes to vanilla BlockEntityType.SKULL. Our own GAIA_HEAD type would only be visible
		// through getType(), which is not consulted during construction, so accept the gaia head
		// blocks here instead.
		return state.getBlock() instanceof GaiaHeadBlock || super.isValidBlockState(state);
	}

	@NotNull
	@Override
	public BlockEntityType<GaiaHeadBlockEntity> getType() {
		return BotaniaBlockEntities.GAIA_HEAD;
	}

}
