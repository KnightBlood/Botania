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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.helper.ValueIOHelper;

public class BotaniaBlockEntity extends BlockEntity {
	public BotaniaBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		var tag = new CompoundTag();
		writePacketNBT(tag);
		ValueIOHelper.storeAll(output, tag);
	}

	@NotNull
	@Override
	public final CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		var tag = new CompoundTag();
		writePacketNBT(tag);
		return tag;
	}

	@Override
	public void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		// NB: During the vanilla load path (BlockEntity.loadStatic -> loadWithComponents) the level is
		// not yet assigned, so reading must not go through level.registryAccess(). The ValueInput
		// carries its own HolderLookup.Provider, which we forward to the readPacketNBT chain.
		readPacketNBT(ValueIOHelper.readAll(input), input.lookup());
	}

	public void writePacketNBT(CompoundTag cmp) {}

	public void readPacketNBT(CompoundTag cmp) {}

	/**
	 * Load entry point used by {@link #loadAdditional}. Subclasses needing registry access while
	 * reading should override this instead of the level-less variant, which may run before the
	 * level is assigned.
	 */
	public void readPacketNBT(CompoundTag cmp, HolderLookup.Provider lookup) {
		readPacketNBT(cmp);
	}

	@Nullable
	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
