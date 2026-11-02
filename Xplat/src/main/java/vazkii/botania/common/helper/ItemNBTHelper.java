/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.Registry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Optional;

public final class ItemNBTHelper {

	private static final int[] EMPTY_INT_ARRAY = new int[0];
	private static final long[] EMPTY_LONG_ARRAY = new long[0];

	/**
	 * Registry lookup used to (de)serialize item stacks to/from JSON. Only the item registry is really needed for
	 * the components Botania cares about, but anything that resolves through the item registry works as well.
	 */
	private static final RegistryOps.RegistryInfoLookup REGISTRY_INFO_LOOKUP = new RegistryOps.RegistryInfoLookup() {
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
			if (registryKey.equals(Registries.ITEM)) {
				return (Optional) Optional.of(RegistryOps.RegistryInfo.fromRegistryLookup(BuiltInRegistries.ITEM));
			}
			return Optional.empty();
		}
	};

	public static final RegistryOps<JsonElement> JSON_REGISTRY_OPS = RegistryOps.create(JsonOps.INSTANCE, REGISTRY_INFO_LOOKUP);

	// INTERNAL HELPERS /////////////////////////////////////////////////////////

	/**
	 * Gets the custom data CompoundTag from an ItemStack.
	 * In MC 26.1+, item NBT is stored via DataComponents.CUSTOM_DATA.
	 * Returns null if no custom data is present.
	 */
	@Nullable
	private static CompoundTag getCustomTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null ? data.copyTag() : null;
	}

	/**
	 * Gets or creates the custom data CompoundTag on an ItemStack.
	 */
	private static CompoundTag getOrCreateCustomTag(ItemStack stack) {
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		return data != null ? data.copyTag() : new CompoundTag();
	}

	/**
	 * Sets the custom data CompoundTag on an ItemStack.
	 */
	private static void setCustomTag(ItemStack stack, CompoundTag tag) {
		if (tag.isEmpty()) {
			stack.remove(DataComponents.CUSTOM_DATA);
		} else {
			stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	// SETTERS ///////////////////////////////////////////////////////////////////

	public static void set(ItemStack stack, String tag, Tag nbt) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.put(tag, nbt);
		setCustomTag(stack, cmp);
	}

	public static void setBoolean(ItemStack stack, String tag, boolean b) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putBoolean(tag, b);
		setCustomTag(stack, cmp);
	}

	public static void setByte(ItemStack stack, String tag, byte b) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putByte(tag, b);
		setCustomTag(stack, cmp);
	}

	public static void setShort(ItemStack stack, String tag, short s) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putShort(tag, s);
		setCustomTag(stack, cmp);
	}

	public static void setInt(ItemStack stack, String tag, int i) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putInt(tag, i);
		setCustomTag(stack, cmp);
	}

	public static void setIntArray(ItemStack stack, String tag, int[] val) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putIntArray(tag, val);
		setCustomTag(stack, cmp);
	}

	public static void setLong(ItemStack stack, String tag, long l) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putLong(tag, l);
		setCustomTag(stack, cmp);
	}

	public static void setLongArray(ItemStack stack, String tag, long[] val) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putLongArray(tag, val);
		setCustomTag(stack, cmp);
	}

	public static void setFloat(ItemStack stack, String tag, float f) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putFloat(tag, f);
		setCustomTag(stack, cmp);
	}

	public static void setDouble(ItemStack stack, String tag, double d) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putDouble(tag, d);
		setCustomTag(stack, cmp);
	}

	public static void setCompound(ItemStack stack, String tag, CompoundTag cmp) {
		if (!tag.equalsIgnoreCase("ench")) { // not override the enchantments
			CompoundTag existing = getOrCreateCustomTag(stack);
			existing.put(tag, cmp);
			setCustomTag(stack, existing);
		}
	}

	public static void setString(ItemStack stack, String tag, String s) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.putString(tag, s);
		setCustomTag(stack, cmp);
	}

	public static void setList(ItemStack stack, String tag, ListTag list) {
		CompoundTag cmp = getOrCreateCustomTag(stack);
		cmp.put(tag, list);
		setCustomTag(stack, cmp);
	}

	public static void removeEntry(ItemStack stack, String tag) {
		CompoundTag cmp = getCustomTag(stack);
		if (cmp != null) {
			cmp.remove(tag);
			setCustomTag(stack, cmp);
		}
	}

	// GETTERS ///////////////////////////////////////////////////////////////////

	public static boolean verifyExistance(ItemStack stack, String tag) {
		if (stack.isEmpty()) {
			return false;
		}
		CompoundTag cmp = getCustomTag(stack);
		return cmp != null && cmp.contains(tag);
	}

	public static boolean verifyType(ItemStack stack, String tag, Class<? extends Tag> tagClass) {
		if (stack.isEmpty()) {
			return false;
		}
		CompoundTag cmp = getCustomTag(stack);
		return cmp != null && tagClass.isInstance(cmp.get(tag));
	}

	@Nullable
	public static Tag get(ItemStack stack, String tag) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).get(tag) : null;
	}

	public static boolean getBoolean(ItemStack stack, String tag, boolean defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getBooleanOr(tag, false) : defaultExpected;
	}

	public static byte getByte(ItemStack stack, String tag, byte defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getByteOr(tag, (byte)0) : defaultExpected;
	}

	public static short getShort(ItemStack stack, String tag, short defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getShortOr(tag, (short)0) : defaultExpected;
	}

	public static int getInt(ItemStack stack, String tag, int defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getIntOr(tag, 0) : defaultExpected;
	}

	public static int[] getIntArray(ItemStack stack, String tag) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getIntArray(tag).orElse(EMPTY_INT_ARRAY) : EMPTY_INT_ARRAY;
	}

	public static long getLong(ItemStack stack, String tag, long defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getLongOr(tag, 0L) : defaultExpected;
	}

	public static long[] getLongArray(ItemStack stack, String tag) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getLongArray(tag).orElse(EMPTY_LONG_ARRAY) : EMPTY_LONG_ARRAY;
	}

	public static float getFloat(ItemStack stack, String tag, float defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getFloatOr(tag, 0.0F) : defaultExpected;
	}

	public static double getDouble(ItemStack stack, String tag, double defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getDoubleOr(tag, 0.0) : defaultExpected;
	}

	/**
	 * If nullifyOnFail is true it'll return null if it doesn't find any
	 * compounds, otherwise it'll return a new one.
	 **/
	@Nullable
	@Contract("_, _, false -> !null")
	public static CompoundTag getCompound(ItemStack stack, String tag, boolean nullifyOnFail) {
		if (!verifyExistance(stack, tag)) {
			return nullifyOnFail ? null : new CompoundTag();
		}
		return getCustomTag(stack).getCompound(tag).orElse(nullifyOnFail ? null : new CompoundTag());
	}

	@Nullable
	@Contract("_, _, !null -> !null")
	public static String getString(ItemStack stack, String tag, String defaultExpected) {
		return verifyExistance(stack, tag) ? getCustomTag(stack).getStringOr(tag, "") : defaultExpected;
	}

	@Nullable
	@Contract("_, _, _, false -> !null")
	public static ListTag getList(ItemStack stack, String tag, int objtype, boolean nullifyOnFail) {
		if (!verifyExistance(stack, tag)) {
			return nullifyOnFail ? null : new ListTag();
		}
		return getCustomTag(stack).getList(tag).orElse(nullifyOnFail ? null : new ListTag());
	}

	// OTHER ///////////////////////////////////////////////////////////////////

	/**
	 * Returns the fullness of the mana item:
	 * 0 if empty, 1 if partially full, 2 if full.
	 */
	public static int getFullness(ManaItem item) {
		int mana = item.getMana();
		if (mana == 0) {
			return 0;
		} else if (mana == item.getMaxMana()) {
			return 2;
		} else {
			return 1;
		}
	}

	public static ItemStack duplicateAndClearMana(ItemStack stack) {
		ItemStack copy = stack.copy();
		ManaItem manaItem = XplatAbstractions.INSTANCE.findManaItem(copy);
		if (manaItem != null) {
			manaItem.addMana(-manaItem.getMana());
		}
		return copy;
	}

	/**
	 * Checks if two items are the same and have the same components. If they are `IManaItems`, their mana property is matched
	 * on whether they are empty, partially full, or full.
	 */
	public static boolean matchTagAndManaFullness(ItemStack stack1, ItemStack stack2) {
		if (!ItemStack.isSameItem(stack1, stack2)) {
			return false;
		}
		ManaItem manaItem1 = XplatAbstractions.INSTANCE.findManaItem(stack1);
		ManaItem manaItem2 = XplatAbstractions.INSTANCE.findManaItem(stack2);
		if (manaItem1 != null && manaItem2 != null) {
			if (getFullness(manaItem1) != getFullness(manaItem2)) {
				return false;
			} else {
				return ItemStack.matches(duplicateAndClearMana(stack1), duplicateAndClearMana(stack2));
			}
		}
		return ItemStack.isSameItemSameComponents(stack1, stack2);
	}

	/**
	 * Serializes the given stack such that {@link net.minecraft.world.item.ItemStack#CODEC} would be able to read the
	 * result back.
	 */
	public static JsonObject serializeStack(ItemStack stack) {
		return ItemStack.CODEC.encodeStart(JSON_REGISTRY_OPS, stack).getOrThrow().getAsJsonObject();
	}

	/**
	 * Registry ops for (de)serializing item stacks to/from NBT without a full {@link net.minecraft.core.RegistryAccess}.
	 */
	public static final RegistryOps<Tag> NBT_REGISTRY_OPS = RegistryOps.create(NbtOps.INSTANCE, REGISTRY_INFO_LOOKUP);

	/**
	 * Serializes the given stack into a {@link CompoundTag} that {@link #deserializeStackNbt(CompoundTag)} can read back.
	 */
	public static CompoundTag serializeStackNbt(ItemStack stack) {
		if (stack.isEmpty()) {
			return new CompoundTag();
		}
		return (CompoundTag) ItemStack.CODEC.encodeStart(NBT_REGISTRY_OPS, stack).getOrThrow();
	}

	/**
	 * Deserializes a stack written by {@link #serializeStackNbt(ItemStack)}.
	 */
	public static ItemStack deserializeStackNbt(@Nullable CompoundTag tag) {
		if (tag == null || tag.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return ItemStack.CODEC.parse(NBT_REGISTRY_OPS, tag).result().orElse(ItemStack.EMPTY);
	}

	/**
	 * Registry-aware variant of {@link #serializeStackNbt(ItemStack)}.
	 */
	public static CompoundTag serializeStackNbt(ItemStack stack, HolderLookup.Provider registries) {
		if (stack.isEmpty()) {
			return new CompoundTag();
		}
		return (CompoundTag) ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), stack).getOrThrow();
	}

	/**
	 * Registry-aware variant of {@link #deserializeStackNbt(CompoundTag)}.
	 */
	public static ItemStack deserializeStackNbt(@Nullable CompoundTag tag, HolderLookup.Provider registries) {
		if (tag == null || tag.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
	}

	public static void renameTag(CompoundTag nbt, String oldName, String newName) {
		Tag tag = nbt.get(oldName);
		if (tag != null) {
			nbt.remove(oldName);
			nbt.put(newName, tag);
		}
	}
}
