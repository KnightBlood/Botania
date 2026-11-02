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
import com.mojang.serialization.Codec;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Helpers around the reworked {@link Ingredient} API of MC 26.1.
 */
public final class IngredientHelper {
	/**
	 * Codec used to (de)serialize ingredients. Only vanilla (holder set based) ingredients are supported.
	 * <p>
	 * Delegates to the vanilla codec, whose encoder reads the ingredient's own {@link HolderSet} instead
	 * of flattening it via {@code items()}. Dereferencing the contents throws
	 * {@code "Tag ... can't be dereferenced during construction"} for tag ingredients built while tags
	 * are unbound (datagen), and a flattened set would also lose the {@code {"tag": ...}} form.
	 */
	public static final Codec<Ingredient> CODEC = Ingredient.CODEC;

	/**
	 * An ingredient that never matches any stack, the 26.1 replacement of the removed {@code Ingredient.EMPTY}.
	 */
	public static final Ingredient EMPTY = EmptyIngredient.EMPTY;

	private IngredientHelper() {
	}

	/**
	 * Creates an ingredient matching everything in the given tag.
	 * <p>
	 * Unlike {@code BuiltInRegistries.ITEM.get(tag)} this also works while the tag is not bound yet, which is the
	 * case during datagen.
	 */
	public static Ingredient of(TagKey<Item> tag) {
		return Ingredient.of(HolderSet.emptyNamed(BuiltInRegistries.ITEM, tag));
	}

	public static JsonElement serialize(Ingredient ingredient) {
		return CODEC.encodeStart(ItemNBTHelper.JSON_REGISTRY_OPS, ingredient).getOrThrow();
	}

	public static Ingredient fromJson(JsonElement json) {
		return CODEC.parse(ItemNBTHelper.JSON_REGISTRY_OPS, json).getOrThrow();
	}

	public static Ingredient fromNetwork(RegistryFriendlyByteBuf buffer) {
		return Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
	}
}
