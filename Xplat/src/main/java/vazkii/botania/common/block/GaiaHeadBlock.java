/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.block_entity.GaiaHeadBlockEntity;

public class GaiaHeadBlock extends SkullBlock {
	public static final SkullBlock.Type GAIA_TYPE = new SkullBlock.Type() {
		@Override
		public String getSerializedName() {
			return "gaia";
		}
	};

	static {
		// 26.1 resolves skull types by serialized name through this map (SkullBlock.Type.CODEC,
		// see SkullBlock.Type.TYPES); vanilla enum constants self-register in their constructor.
		// Registering here unblocks the block codec ("kind": "gaia") and the item
		// "minecraft:head" special model, restoring the full 3D gaia skull form.
		SkullBlock.Type.TYPES.put("gaia", GAIA_TYPE);
	}

	public GaiaHeadBlock(Properties builder) {
		super(GAIA_TYPE, builder);
	}

	@NotNull
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new GaiaHeadBlockEntity(pos, state);
	}
}
