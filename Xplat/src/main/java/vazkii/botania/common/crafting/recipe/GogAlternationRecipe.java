/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.xplat.XplatAbstractions;

public class GogAlternationRecipe {

	public static final MapCodec<Recipe<?>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MapCodec.assumeMapUnsafe(Recipe.CODEC).fieldOf("gog").forGetter(r -> null),
			MapCodec.assumeMapUnsafe(Recipe.CODEC).fieldOf("base").forGetter(r -> null)
	).apply(i, (gog, base) -> {
		if (gog.getType() != base.getType()) {
			throw new IllegalArgumentException("Subrecipes must have matching types");
		}
		return XplatAbstractions.INSTANCE.gogLoaded() ? gog : base;
	}));

	public static final StreamCodec<RegistryFriendlyByteBuf, Recipe<?>> STREAM_CODEC = StreamCodec.composite(
			Recipe.STREAM_CODEC, r -> r,
			Recipe.STREAM_CODEC, r -> r,
			(gog, base) -> gog
	);

	public static final RecipeSerializer<Recipe<?>> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
