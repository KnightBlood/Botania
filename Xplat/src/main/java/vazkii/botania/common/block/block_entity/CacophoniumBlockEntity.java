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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.CacophoniumItem;

public class CacophoniumBlockEntity extends BotaniaBlockEntity {
	private static final String TAG_STACK = "stack";

	public ItemStack stack = ItemStack.EMPTY;

	public CacophoniumBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CACOPHONIUM, pos, state);
	}

	public void annoyDirewolf() {
		CacophoniumItem.playSound(level, stack, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), SoundSource.BLOCKS, 1F);
		if (!level.isClientSide()) {
			float noteColor = level.getRandom().nextInt(25) / 24.0F;
			((ServerLevel) level).sendParticles(ParticleTypes.NOTE, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 0, noteColor, 0, 0, 1);
			level.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, getBlockPos());
		}
	}

	@Override
	public void writePacketNBT(CompoundTag cmp) {
		super.writePacketNBT(cmp);

		if (!stack.isEmpty()) {
			cmp.put(TAG_STACK, ItemNBTHelper.serializeStackNbt(stack, level.registryAccess()));
		}
	}

	@Override
	public void readPacketNBT(CompoundTag cmp, HolderLookup.Provider lookup) {
		super.readPacketNBT(cmp, lookup);

		CompoundTag cmp1 = cmp.getCompoundOrEmpty(TAG_STACK);
		stack = ItemNBTHelper.deserializeStackNbt(cmp1, lookup);
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		// 1.20.1 onRemove semantics: drop the cacophonium item; the vanilla Container default
		// does not apply (this BE is not a Container). The old block-side drop lived in
		// affectNeighborsAfterRemoval, which runs after the BE is gone -- dead code.
		if (level != null && !stack.isEmpty()) {
			Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
		}
	}

}
