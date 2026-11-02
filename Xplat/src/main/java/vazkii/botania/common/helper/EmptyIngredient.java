/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.helper;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * A placeholder ingredient that matches nothing at all.
 * <p>
 * MC 26.1 removed {@code Ingredient.EMPTY}. The {@link Ingredient} constructor only validates
 * <em>direct</em> holder sets, so an <em>indirect</em> set passes through. We use an unbound
 * named tag (one that never exists and thus always resolves to an empty item list), which makes
 * this ingredient never match any stack. This works identically on NeoForge and Fabric, avoiding
 * the platform-specific custom ingredient SPIs entirely.
 */
public final class EmptyIngredient {
	private static final TagKey<Item> EMPTY_TAG = TagKey.create(Registries.ITEM,
			Identifier.withDefaultNamespace("botania_empty"));

	/**
	 * An ingredient that never matches any stack.
	 */
	public static final Ingredient EMPTY = Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, EMPTY_TAG));

	private EmptyIngredient() {}
}
