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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeInput;

import vazkii.botania.api.recipe.BotaniaRecipe;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.StateIngredient;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

public class OrechidRecipe implements vazkii.botania.api.recipe.OrechidRecipe {
	protected Identifier id;
	protected final StateIngredient input;
	protected final StateIngredient output;
	protected final int Weighted;
	protected final @Nullable CacheableFunction successFunction;

	public OrechidRecipe(Identifier id, StateIngredient input, StateIngredient output, int Weighted, @Nullable CacheableFunction successFunction) {
		this.id = id;
		this.input = input;
		this.output = output;
		this.Weighted = Weighted;
		this.successFunction = successFunction;
	}

	@Override
	public StateIngredient getInput() {
		return input;
	}

	@Override
	public StateIngredient getOutput() {
		return output;
	}

	@Override
	public int getWeight() {
		return Weighted;
	}

	@Override
	public @Nullable CacheableFunction getSuccessFunction() {
		return this.successFunction;
	}

		public Identifier getId() {
		return id;
	}

	@NotNull
	@Override
	public RecipeType<? extends OrechidRecipe> getType() {
		return BotaniaRecipeTypes.ORECHID_TYPE;
	}

	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.ORECHID_SERIALIZER;
	}

	public static final MapCodec<OrechidRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StateIngredientHelper.CODEC.fieldOf("input").forGetter(r -> r.input),
			StateIngredientHelper.CODEC.fieldOf("output").forGetter(r -> r.output),
			Codec.INT.fieldOf("weight").forGetter(r -> r.Weighted),
			CacheableFunction.CODEC.optionalFieldOf("success_function")
					.forGetter(r -> Optional.ofNullable(r.successFunction))
	).apply(i, (input, output, weight, function) -> new OrechidRecipe(RecipeSerializers.UNKNOWN_ID,
			input, output, weight, function.orElse(null))));

	public static final StreamCodec<RegistryFriendlyByteBuf, OrechidRecipe> STREAM_CODEC = StreamCodec.composite(
			StateIngredientHelper.STREAM_CODEC, r -> r.input,
			StateIngredientHelper.STREAM_CODEC, r -> r.output,
			ByteBufCodecs.VAR_INT, r -> r.Weighted,
			(input, output, weight) -> new OrechidRecipe(RecipeSerializers.UNKNOWN_ID,
					input, output, weight, null)
	);

	public static final RecipeSerializer<OrechidRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
