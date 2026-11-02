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

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class NarslimmusComponent extends SerializableComponent {
	public static final String TAG_WORLD_SPAWNED = "botania:world_spawned";
	public static final MapCodec<NarslimmusComponent> CODEC = Codec.BOOL.fieldOf("natural_spawned")
			.xmap(naturalSpawned -> new NarslimmusComponent().setNaturalSpawnRaw(naturalSpawned), NarslimmusComponent::isNaturalSpawned);
	private boolean naturalSpawned = false;

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		naturalSpawned = tag.getBooleanOr(TAG_WORLD_SPAWNED, false);
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putBoolean(TAG_WORLD_SPAWNED, naturalSpawned);
	}

	public boolean isNaturalSpawned() {
		return naturalSpawned;
	}

	public void setNaturalSpawn(boolean naturalSpawned) {
		this.naturalSpawned = naturalSpawned;
	}

	private NarslimmusComponent setNaturalSpawnRaw(boolean naturalSpawned) {
		this.naturalSpawned = naturalSpawned;
		return this;
	}
}
