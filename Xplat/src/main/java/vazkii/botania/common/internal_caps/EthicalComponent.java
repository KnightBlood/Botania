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
import net.minecraft.world.entity.item.PrimedTnt;

import vazkii.botania.common.helper.EthicalTntHelper;

public class EthicalComponent extends SerializableComponent {
	protected static final String TAG_UNETHICAL = "botania:unethical";
	public static final MapCodec<EthicalComponent> CODEC = Codec.BOOL.fieldOf("unethical")
			.xmap(unethical -> {
				var component = new EthicalComponent();
				component.unethical = unethical;
				return component;
			}, component -> component.unethical);
	protected boolean unethical;

	public EthicalComponent() {}

	public EthicalComponent(PrimedTnt entity) {
		if (!entity.level().isClientSide()) {
			EthicalTntHelper.addTrackedTntEntity(entity);
		}
	}

	public final boolean isUnethical() {
		return unethical;
	}

	public final void markUnethical() {
		unethical = true;
	}

	@Override
	public void readFromNbt(CompoundTag tag, HolderLookup.Provider registries) {
		unethical = tag.getBooleanOr(TAG_UNETHICAL, false);
	}

	@Override
	public void writeToNbt(CompoundTag tag, HolderLookup.Provider registries) {
		tag.putBoolean(TAG_UNETHICAL, unethical);
	}
}
