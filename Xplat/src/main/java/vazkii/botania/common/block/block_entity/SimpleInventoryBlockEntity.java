/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import com.google.common.base.Preconditions;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

public abstract class SimpleInventoryBlockEntity extends BotaniaBlockEntity implements Clearable {

	private final SimpleContainer itemHandler;

	@SuppressWarnings("this-escape") // same as 1.20.1: subclasses are expected to override createItemHandler
	protected SimpleInventoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		SimpleContainer inner = createItemHandler();
		this.itemHandler = new SimpleContainer(inner.getContainerSize()) {
			@Override
			public void setChanged() {
				// SimpleContainer#addListener was removed in 26.1; propagate changes via setChanged
				SimpleInventoryBlockEntity.this.setChanged();
			}
		};
	}

	private static void copyToInv(NonNullList<ItemStack> src, Container dest) {
		Preconditions.checkArgument(src.size() == dest.getContainerSize());
		for (int i = 0; i < src.size(); i++) {
			dest.setItem(i, src.get(i));
		}
	}

	private static NonNullList<ItemStack> copyFromInv(Container inv) {
		NonNullList<ItemStack> ret = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ret.set(i, inv.getItem(i));
		}
		return ret;
	}

	@Override
	public void readPacketNBT(CompoundTag tag, HolderLookup.Provider lookup) {
		// NB: The vanilla load path runs before setLevel, so registries must come from the caller
		// (ValueInput#lookup) rather than level.registryAccess().
		ValueInput input = TagValueInput.create(ProblemReporter.DISCARDING, lookup, tag);
		NonNullList<ItemStack> tmp = NonNullList.withSize(inventorySize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, tmp);
		copyToInv(tmp, itemHandler);
	}

	@Override
	public void writePacketNBT(CompoundTag tag) {
		TagValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		ContainerHelper.saveAllItems(output, copyFromInv(itemHandler));
		tag.merge(output.buildResult());
	}

	// NB: Cannot be named the same as the corresponding method in vanilla's interface -- causes obf issues with MCP
	public final int inventorySize() {
		return getItemHandler().getContainerSize();
	}

	protected abstract SimpleContainer createItemHandler();

	@Override
	public void clearContent() {
		getItemHandler().clearContent();
	}

	public final Container getItemHandler() {
		return itemHandler;
	}

	@Override
	public void preRemoveSideEffects(BlockPos pos, BlockState state) {
		// NB: deliberately not calling super: the vanilla default drops only when the BE
		// implements Container, which would double-drop for the WorldlyContainer subclasses
		// (they override this method with their own conditions). Botania inventory BEs are
		// not vanilla Containers, and the 1.20.1 onRemove container drop was (incorrectly)
		// ported into the blocks' affectNeighborsAfterRemoval, which runs after the BE is
		// already removed -- breaking these blocks silently deleted their contents. 26.1
		// drops container contents here, BEFORE the block entity is removed.
		if (level != null) {
			Containers.dropContents(level, pos, getItemHandler());
		}
	}
}
