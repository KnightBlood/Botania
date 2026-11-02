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

public class TigerseyeComponent extends SerializableComponent {
	private static final String TAG_PACIFIED = "botania:tigerseye_pacified";
	public static final MapCodec<TigerseyeComponent> CODEC = Codec.BOOL.fieldOf("pacified")
			.xmap(pacified -> new TigerseyeComponent().setPacifiedRaw(pacified), TigerseyeComponent::isPacified);
	private boolean pacified = false;

	private TigerseyeComponent setPacifiedRaw(boolean pacified) {
		this.pacified = pacified;
		return this;
	}

	public boolean isPacified() {
		return pacified;
	}

	public void setPacified() {
		this.pacified = true;
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		this.pacified = tag.getBooleanOr(TAG_PACIFIED, false);
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		if (pacified) {
			tag.putBoolean(TAG_PACIFIED, true);
		}
	}
}
