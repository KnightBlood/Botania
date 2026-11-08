/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.internal_caps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.helper.ItemNBTHelper;

import java.util.Objects;

public class LooniumComponent extends SerializableComponent {
	protected static final String TAG_TO_DROP = "toDrop";
	protected static final String TAG_OVERRIDE_DROP = "overrideDrop";
	protected static final String TAG_SLOW_DESPAWN = "slowDespawn";
	public static final MapCodec<LooniumComponent> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ItemStack.CODEC.optionalFieldOf(TAG_TO_DROP, ItemStack.EMPTY).forGetter(LooniumComponent::getDrop),
			Codec.BOOL.optionalFieldOf(TAG_OVERRIDE_DROP, false).forGetter(LooniumComponent::isOverrideDrop),
			Codec.BOOL.optionalFieldOf(TAG_SLOW_DESPAWN, false).forGetter(LooniumComponent::isSlowDespawn)
	).apply(i, LooniumComponent::new));
	private ItemStack toDrop = ItemStack.EMPTY;
	private boolean overrideDrop;
	private boolean slowDespawn;

	public LooniumComponent() {}

	private LooniumComponent(ItemStack toDrop, boolean overrideDrop, boolean slowDespawn) {
		this.toDrop = toDrop;
		this.overrideDrop = overrideDrop;
		this.slowDespawn = slowDespawn;
	}

	public ItemStack getDrop() {
		return toDrop;
	}

	public void setDrop(ItemStack stack) {
		this.toDrop = stack;
	}

	public boolean isOverrideDrop() {
		return overrideDrop;
	}

	public void setOverrideDrop(boolean overrideDrop) {
		this.overrideDrop = overrideDrop;
	}

	public boolean isSlowDespawn() {
		return slowDespawn;
	}

	public void setSlowDespawn(boolean slowDespawn) {
		this.slowDespawn = slowDespawn;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		return obj instanceof LooniumComponent component && ItemStack.matches(component.toDrop, toDrop)
				&& component.overrideDrop == overrideDrop && component.slowDespawn == slowDespawn;
	}

	@Override
	public int hashCode() {
		return Objects.hash(toDrop.hashCode(), overrideDrop, slowDespawn);
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		if (tag.contains(TAG_TO_DROP)) {
			setDrop(ItemNBTHelper.deserializeStackNbt(tag.getCompoundOrEmpty(TAG_TO_DROP), registries));
		} else {
			setDrop(ItemStack.EMPTY);
		}
		if (tag.contains(TAG_OVERRIDE_DROP)) {
			setOverrideDrop(tag.getBooleanOr(TAG_OVERRIDE_DROP, false));
		}
		if (tag.contains(TAG_SLOW_DESPAWN)) {
			setSlowDespawn(tag.getBooleanOr(TAG_SLOW_DESPAWN, false));
		}
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		if (isOverrideDrop()) {
			if (!getDrop().isEmpty()) {
				tag.put(TAG_TO_DROP, ItemNBTHelper.serializeStackNbt(getDrop(), registries));
			}
			tag.putBoolean(TAG_OVERRIDE_DROP, true);
		}
		if (isSlowDespawn()) {
			tag.putBoolean(TAG_SLOW_DESPAWN, true);
		}
	}
}
