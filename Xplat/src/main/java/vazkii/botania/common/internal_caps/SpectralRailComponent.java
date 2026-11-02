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

import vazkii.botania.common.block.SpectralRailBlock;

public class SpectralRailComponent extends SerializableComponent {
	public static final MapCodec<SpectralRailComponent> CODEC = Codec.INT.fieldOf("float_ticks")
			.xmap(floatTicks -> {
				var component = new SpectralRailComponent();
				component.floatTicks = floatTicks;
				return component;
			}, component -> component.floatTicks);

	public int floatTicks = 0;

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		floatTicks = tag.getIntOr(SpectralRailBlock.TAG_FLOAT_TICKS, 0);
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putInt(SpectralRailBlock.TAG_FLOAT_TICKS, floatTicks);
	}
}
