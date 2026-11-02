/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import com.mojang.serialization.MapCodec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Bridge helpers between the old {@link CompoundTag} based serialization and the new
 * {@link ValueInput} / {@link ValueOutput} based one.
 */
public final class ValueIOHelper {
	private ValueIOHelper() {}

	/**
	 * Reads the whole level backing this input as a single compound tag.
	 */
	public static CompoundTag readAll(ValueInput input) {
		//noinspection deprecation
		return input.read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseGet(CompoundTag::new);
	}

	/**
	 * Merges all entries of the given tag into this output.
	 */
	public static void storeAll(ValueOutput output, CompoundTag tag) {
		// Mirror readAll(): the whole compound goes through an unsafe MapCodec view (NeoForge's
		// {@code ValueOutput#store(CompoundTag)} extension does not exist on vanilla).
		output.store(MapCodec.assumeMapUnsafe(CompoundTag.CODEC), tag);
	}
}
