/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.helper.ItemNBTHelper;

/**
 * An inventory that writes into the provided stack's NBT on save
 */
public class ItemBackedInventory extends SimpleContainer {
	private static final String TAG_ITEMS = "Items";
	private final ItemStack stack;

	@SuppressWarnings("this-escape") // 1.20.1 pattern preserved: this escapes via lambda/setter in ctor
	public ItemBackedInventory(ItemStack stack, int expectedSize) {
		// 26.1 has SimpleContainer(ItemStack...) alongside SimpleContainer(int). Passing the
		// backing stack here selects the varargs overload, which creates a ONE-slot container
		// holding the bag itself; onPickupItem then writes to slots 16..31 and throws
		// ArrayIndexOutOfBoundsException. Restore the 1.20.1 behaviour: size it explicitly.
		super(expectedSize);
		this.stack = stack;

		ListTag lst = ItemNBTHelper.getList(stack, TAG_ITEMS, Tag.TAG_COMPOUND, false);
		int i = 0;
		for (; i < expectedSize && i < lst.size(); i++) {
			setItem(i, ItemNBTHelper.deserializeStackNbt(lst.getCompoundOrEmpty(i)));
		}
	}

	@Override
	public boolean stillValid(Player player) {
		return !stack.isEmpty();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		ListTag list = new ListTag();
		for (int i = 0; i < getContainerSize(); i++) {
			list.add(ItemNBTHelper.serializeStackNbt(getItem(i)));
		}
		ItemNBTHelper.setList(stack, TAG_ITEMS, list);
	}
}
