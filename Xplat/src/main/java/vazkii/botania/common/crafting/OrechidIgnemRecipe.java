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

import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

public class OrechidIgnemRecipe extends OrechidRecipe {
	public OrechidIgnemRecipe(Identifier id, StateIngredient input, StateIngredient output, int Weighted, CacheableFunction successFunction) {
		super(id, input, output, Weighted, successFunction);
	}

	private OrechidIgnemRecipe(OrechidRecipe recipe) {
		this(recipe.getId(), recipe.getInput(), recipe.getOutput(), recipe.getWeight(), recipe.getSuccessFunction());
	}

	@NotNull
	@Override
	public RecipeType<? extends OrechidIgnemRecipe> getType() {
		return BotaniaRecipeTypes.ORECHID_IGNEM_TYPE;
	}

	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.ORECHID_IGNEM_SERIALIZER;
	}

	public static final MapCodec<OrechidIgnemRecipe> CODEC = OrechidRecipe.CODEC.xmap(
			recipe -> new OrechidIgnemRecipe(RecipeSerializers.UNKNOWN_ID, recipe.getInput(), recipe.getOutput(),
					recipe.getWeight(), recipe.getSuccessFunction()),
			recipe -> new OrechidRecipe(RecipeSerializers.UNKNOWN_ID, recipe.getInput(), recipe.getOutput(),
					recipe.getWeight(), recipe.getSuccessFunction()));

	public static final StreamCodec<RegistryFriendlyByteBuf, OrechidIgnemRecipe> STREAM_CODEC = OrechidRecipe.STREAM_CODEC.map(
			recipe -> new OrechidIgnemRecipe(RecipeSerializers.UNKNOWN_ID, recipe.getInput(), recipe.getOutput(),
					recipe.getWeight(), recipe.getSuccessFunction()),
			recipe -> new OrechidRecipe(RecipeSerializers.UNKNOWN_ID, recipe.getInput(), recipe.getOutput(),
					recipe.getWeight(), recipe.getSuccessFunction()));

	public static final RecipeSerializer<OrechidIgnemRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
