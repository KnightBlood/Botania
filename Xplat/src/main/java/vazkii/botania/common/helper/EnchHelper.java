/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import org.jetbrains.annotations.Nullable;

/**
 * Enchantments are data-driven in MC 26.1, so their {@link Holder}s must be resolved through a
 * {@link RegistryAccess} instead of being statically available.
 */
public final class EnchHelper {

	@Nullable
	public static Holder<Enchantment> tryGet(ResourceKey<Enchantment> key, @Nullable HolderLookup.Provider provider) {
		if (provider == null) {
			return null;
		}
		return provider.lookupOrThrow(Registries.ENCHANTMENT).get(key).orElse(null);
	}

	public static Holder<Enchantment> get(ResourceKey<Enchantment> key, HolderLookup.Provider provider) {
		return provider.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
	}

	/**
	 * Registry-free equivalent of {@code EnchantmentHelper.getItemEnchantmentLevel(Holder, ItemStack)},
	 * resolving the enchantment by resource key against the stack's ENCHANTMENTS component.
	 */
	public static int getItemEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key) {
		ItemEnchantments enchantments = stack.get(DataComponents.ENCHANTMENTS);
		if (enchantments == null) {
			return 0;
		}
		for (var entry : enchantments.entrySet()) {
			if (entry.getKey().is(key)) {
				return entry.getIntValue();
			}
		}
		return 0;
	}

	private EnchHelper() {}
}
