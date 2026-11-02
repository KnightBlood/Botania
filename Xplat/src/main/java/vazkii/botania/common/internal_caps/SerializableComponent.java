package vazkii.botania.common.internal_caps;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public abstract class SerializableComponent {
	/**
	 * Fallback provider for the CCA v8 {@code writeData(ValueOutput)} bridge below: {@code ValueOutput} offers no
	 * registry context, so resolve through the item registry, mirroring {@code ItemNBTHelper}'s registry ops.
	 */
	private static final HolderLookup.Provider FALLBACK_PROVIDER = HolderLookup.Provider
			.create(Stream.<HolderLookup.RegistryLookup<?>>of(BuiltInRegistries.ITEM));

	public abstract void readFromNbt(CompoundTag tag, HolderLookup.Provider registries);

	public abstract void writeToNbt(CompoundTag tag, HolderLookup.Provider registries);

	@NotNull
	public final CompoundTag serializeNBT(HolderLookup.Provider registries) {
		var ret = new CompoundTag();
		writeToNbt(ret, registries);
		return ret;
	}

	public final void deserializeNBT(CompoundTag nbt, HolderLookup.Provider registries) {
		readFromNbt(nbt, registries);
	}

	/**
	 * 26.1 bridge for Cardinal Components' v8 {@code CardinalComponent} interface (Fabric side):
	 * the whole NBT payload is stored under a single {@code Data} compound.
	 */
	public void readData(ValueInput in) {
		in.read("Data", CompoundTag.CODEC).ifPresent(tag -> readFromNbt(tag, in.lookup()));
	}

	public void writeData(ValueOutput out) {
		var tag = new CompoundTag();
		writeToNbt(tag, FALLBACK_PROVIDER);
		out.store("Data", CompoundTag.CODEC, tag);
	}
}
