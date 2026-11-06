/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.IngredientHelper;

import java.util.Objects;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import java.util.Optional;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

public class ManaInfusionRecipe implements vazkii.botania.api.recipe.ManaInfusionRecipe {
	private Identifier id;
	private final ItemStackTemplate output;
	private final Ingredient input;
	private final int mana;
	@Nullable
	private final StateIngredient catalyst;
	private final String group;

	public ManaInfusionRecipe(Identifier id, ItemStackTemplate output, Ingredient input, int mana,
			@Nullable String group, @Nullable StateIngredient catalyst) {
		Preconditions.checkArgument(mana > 0, "Mana cost must be positive");
		Preconditions.checkArgument(mana <= 1_000_001, "Mana cost must be at most a pool"); // Leaving wiggle room for a certain modpack having creative-pool-only recipes
		this.id = id;
		this.output = output;
		this.input = input;
		this.mana = mana;
		this.group = group == null ? "" : group;
		this.catalyst = catalyst;
	}

	@NotNull
	@Override
	public final Identifier getId() {
		return id;
	}

	@NotNull
	@Override
	public RecipeSerializer<ManaInfusionRecipe> getSerializer() {
		return BotaniaRecipeTypes.MANA_INFUSION_SERIALIZER;
	}

	@Override
	public boolean matches(ItemStack stack) {
		return input.test(stack);
	}

	@Override
	public StateIngredient getRecipeCatalyst() {
		return catalyst;
	}

	@Override
	public int getManaToConsume() {
		return mana;
	}

	@NotNull
	@Override
	public ItemStack getResultItem() {
		// 26.1: the output is parsed as a template, so it is only materialized into a real
		// stack once the item components have been bound.
		return output.create();
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		return NonNullList.of(IngredientHelper.EMPTY, input);
	}

	/**
	 * MC 26.1 renamed {@code Recipe#getGroup()} to {@link #group()}. BotaniaRecipe's default
	 * implementation returns an empty string, which made every Lexicon multi-recipe page that
	 * uses a mana infusion group appear empty after the port.
	 */
	@NotNull
	@Override
	public String group() {
		return group;
	}

	/** Kept for addons that still use the 1.20.1 name. */
	@NotNull
	public String getGroup() {
		return group;
	}

	@NotNull
		public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.manaPool);
	}

	public static final MapCodec<ManaInfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			IngredientHelper.CODEC.fieldOf("input").forGetter(r -> r.input),
			ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.output),
			Codec.INT.fieldOf("mana").forGetter(r -> r.mana),
			Codec.STRING.optionalFieldOf("group", "").forGetter(r -> r.group),
			StateIngredientHelper.CODEC.optionalFieldOf("catalyst").forGetter(r -> r.catalyst == null ? Optional.empty() : Optional.of(r.catalyst))
	).apply(i, (input, output, mana, group, catalyst) -> new ManaInfusionRecipe(RecipeSerializers.UNKNOWN_ID,
			output, input, mana, group, catalyst.orElse(null))));

	public static final StreamCodec<RegistryFriendlyByteBuf, ManaInfusionRecipe> STREAM_CODEC = StreamCodec.composite(
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.input,
			ItemStackTemplate.STREAM_CODEC, r -> r.output,
			ByteBufCodecs.VAR_INT, r -> r.mana,
			ByteBufCodecs.STRING_UTF8, r -> r.group,
			ByteBufCodecs.optional(StateIngredientHelper.STREAM_CODEC), r -> java.util.Optional.ofNullable(r.catalyst),
			(input, output, mana, group, catalyst) -> new ManaInfusionRecipe(RecipeSerializers.UNKNOWN_ID,
					output, input, mana, group, catalyst.orElse(null))
	);

	public static final RecipeSerializer<ManaInfusionRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
