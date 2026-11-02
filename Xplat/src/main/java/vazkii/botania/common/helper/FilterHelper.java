package vazkii.botania.common.helper;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.ai.behavior.ShufflingList.WeightedEntry;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FilterHelper {

	public static final String ITEMS_TAG = "Items";

	public static List<ItemStack> getFilterItems(ItemFrame filterFrame) {
		ItemStack filterStack = filterFrame.getItem();
		if (filterStack.isEmpty()) {
			return List.of();
		}
		return filterFrame instanceof GlowItemFrame ? getFilterStacks(filterStack) : List.of(filterStack);
	}

	/**
	 * Expands the given filter item into the list of filter items it represents. This is NOT recursive.
	 * Non-empty container items stand for their contents, not for themselves.
	 */
	public static List<ItemStack> getFilterStacks(ItemStack filterStack) {
		if (filterStack.is(Items.BUNDLE)) {
			// get bundle content
			// 26.1: bundle contents live in the BUNDLE_CONTENTS data component now
			var contents = filterStack.get(net.minecraft.core.component.DataComponents.BUNDLE_CONTENTS);
			if (contents != null) {
				List<ItemStack> bundledItems = contents.itemCopyStream().toList();
				if (!bundledItems.isEmpty()) {
					return bundledItems;
				}
			}
		} else {
			// BlockItems (especially shulker boxes) can contain BlockEntity data, which may include an inventory.
			// Otherwise, items may represent an inventory themselves (e.g. Flower Pouch or Bauble Box)
			var blockEntityData = filterStack.getItem() instanceof BlockItem
					? filterStack.get(net.minecraft.core.component.DataComponents.BLOCK_ENTITY_DATA) : null;
			var customData = filterStack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
			CompoundTag tag = blockEntityData != null ? blockEntityData.copyTagWithoutId()
					: (customData != null ? customData.copyTag() : null);
			if (tag != null && tag.contains(ITEMS_TAG)) {
				// item might contain an inventory
				List<ItemStack> items = getItemStacks(tag);
				if (items != null) {
					return items;
				}
			}
		}
		return List.of(filterStack);
	}

	@Nullable
	private static List<ItemStack> getItemStacks(CompoundTag tag) {
		try {
			ListTag contents = tag.getListOrEmpty(ITEMS_TAG);
			List<ItemStack> items = new ArrayList<>(contents.size());
			for (int i = 0; i < contents.size(); i++) {
				CompoundTag entry = contents.getCompoundOrEmpty(i);
				ItemStack stack = ItemNBTHelper.deserializeStackNbt(entry);
				if (!stack.isEmpty()) {
					items.add(stack);
				}
			}
			if (!items.isEmpty()) {
				return items;
			}
		} catch (ClassCastException ce) {
			// apparently not a typical container inventory
		}
		return null;
	}

	public record WeightedItemStack(ItemStack stack, int weight) {
		public static WeightedItemStack of(ItemStack stack, int weight) {
			return new WeightedItemStack(stack, weight);
		}

		public net.minecraft.util.random.Weighted<WeightedItemStack> weighted() {
			return new net.minecraft.util.random.Weighted<>(this, weight);
		}
	}
}
