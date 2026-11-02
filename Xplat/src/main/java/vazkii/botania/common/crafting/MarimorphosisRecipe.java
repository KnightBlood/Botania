/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeInput;

import vazkii.botania.api.recipe.BotaniaRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.biome.Biome;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.StateIngredient;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

public class MarimorphosisRecipe extends OrechidRecipe {
	private final int weightBonus;
	private final TagKey<Biome> biomes;

	public MarimorphosisRecipe(Identifier id, StateIngredient input, StateIngredient output, int Weighted,
			@Nullable CacheableFunction successFunction,
			int weightBonus, TagKey<Biome> biomes) {
		super(id, input, output, Weighted, successFunction);
		this.weightBonus = weightBonus;
		this.biomes = biomes;
	}

	@Override
	public int getWeight(@NotNull Level level, @NotNull BlockPos pos) {
		if (level.getBiome(pos).is(this.biomes)) {
			return getWeight() + weightBonus;
		}
		return getWeight();
	}

	@NotNull
	@Override
	public RecipeType<? extends MarimorphosisRecipe> getType() {
		return BotaniaRecipeTypes.MARIMORPHOSIS_TYPE;
	}

	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.MARIMORPHOSIS_SERIALIZER;
	}

	public static final MapCodec<MarimorphosisRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StateIngredientHelper.CODEC.fieldOf("input").forGetter(r -> r.input),
			StateIngredientHelper.CODEC.fieldOf("output").forGetter(r -> r.output),
			Codec.INT.fieldOf("weight").forGetter(r -> r.getWeight()),
			CacheableFunction.CODEC.optionalFieldOf("success_function")
					.forGetter(r -> Optional.ofNullable(r.successFunction)),
			Codec.INT.optionalFieldOf("biome_bonus", 0).forGetter(r -> r.weightBonus),
			Codec.STRING.fieldOf("biome_bonus_tag").forGetter(r -> r.biomes.location().toString())
	).apply(i, (input, output, weight, function, bonus, biomeTag) -> new MarimorphosisRecipe(RecipeSerializers.UNKNOWN_ID,
			input, output, weight, function.orElse(null), bonus,
			// 26.1: the 1.20.1 source round-trips the tag id through `new ResourceLocation(String)`,
			// which splits on ':'; `withDefaultNamespace` instead prepends minecraft: blindly and
			// crashes on already-namespaced ids like "botania:marimorphosis_desert_bonus".
			TagKey.create(Registries.BIOME, Identifier.parse(biomeTag)))));

	public static final StreamCodec<RegistryFriendlyByteBuf, MarimorphosisRecipe> STREAM_CODEC = StreamCodec.composite(
			StateIngredientHelper.STREAM_CODEC, r -> r.input,
			StateIngredientHelper.STREAM_CODEC, r -> r.output,
			ByteBufCodecs.VAR_INT, r -> r.getWeight(),
			ByteBufCodecs.VAR_INT, r -> r.weightBonus,
			TagKey.streamCodec(Registries.BIOME), r -> r.biomes,
			(input, output, weight, bonus, biomes) -> new MarimorphosisRecipe(RecipeSerializers.UNKNOWN_ID,
					input, output, weight, null, bonus, biomes)
	);

	public static final RecipeSerializer<MarimorphosisRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);


	public int getWeightBonus() {
		return weightBonus;
	}

	public TagKey<Biome> getBiomes() {
		return biomes;
	}
}
