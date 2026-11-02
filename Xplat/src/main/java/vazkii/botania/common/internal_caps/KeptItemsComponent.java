/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.internal_caps;

import com.mojang.serialization.MapCodec;

import vazkii.botania.common.helper.ItemNBTHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class KeptItemsComponent extends SerializableComponent {
	public static final MapCodec<KeptItemsComponent> CODEC = ItemStack.CODEC.listOf().fieldOf("stacks")
			.xmap(KeptItemsComponent::new, KeptItemsComponent::getStacks);
	private final List<ItemStack> stacks = new ArrayList<>();

	public KeptItemsComponent() {}

	private KeptItemsComponent(Collection<ItemStack> stacks) {
		this.stacks.addAll(stacks);
	}

	public void addAll(Collection<ItemStack> stack) {
		stacks.addAll(stack);
	}

	public List<ItemStack> getStacks() {
		return stacks;
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		stacks.clear();
		ListTag list = tag.getListOrEmpty("stacks");
		for (Tag t : list) {
			stacks.add(ItemNBTHelper.deserializeStackNbt((CompoundTag) t, registries));
		}
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		ListTag list = new ListTag();
		for (ItemStack stack : stacks) {
			list.add(ItemNBTHelper.serializeStackNbt(stack, registries));
		}
		tag.put("stacks", list);
	}
}
