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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BifrostBlockEntity extends BotaniaBlockEntity {
	private static final String TAG_TICKS = "ticks";

	public int ticks = 0;

	public BifrostBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.BIFROST, pos, state);
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, BifrostBlockEntity self) {
		if (self.ticks <= 0) {
			level.removeBlock(worldPosition, false);
		} else {
			self.ticks--;
		}
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt(TAG_TICKS, ticks);
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		ticks = input.getIntOr(TAG_TICKS, 0);
	}

}
