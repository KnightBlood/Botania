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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;
import vazkii.botania.common.crafting.recipe.RecipeUtils;
import vazkii.botania.common.helper.IngredientHelper;

public class RunicAltarRecipe implements vazkii.botania.api.recipe.RunicAltarRecipe {
	protected Identifier id;
	protected final ItemStackTemplate output;
	protected final NonNullList<Ingredient> inputs;
	protected final int mana;

	public RunicAltarRecipe(Identifier id, ItemStackTemplate output, int mana, Ingredient... inputs) {
		Preconditions.checkArgument(inputs.length <= 16, "Cannot have more than 16 ingredients");
		this.id = id;
		this.output = output;
		this.inputs = NonNullList.of(IngredientHelper.EMPTY, inputs);
		this.mana = mana;
	}

	@Override
	public boolean matches(RecipeInput inv, @NotNull Level world) {
		return RecipeUtils.matches(inputs, inv, null);
	}

	@NotNull
	@Override
	public ItemStack getResultItem() {
		return output.create();
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull RecipeInput inv) {
		return getResultItem().copy();
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		return inputs;
	}

	@NotNull
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.runeAltar);
	}

	@NotNull
	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public void setId(Identifier id) {
		this.id = id;
	}

	@NotNull
	@Override
	public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.RUNE_SERIALIZER;
	}

	@Override
	public int getManaUsage() {
		return mana;
	}

	public static final MapCodec<RunicAltarRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.output),
			Codec.INT.fieldOf("mana").forGetter(r -> r.mana),
			IngredientHelper.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.inputs)
	).apply(i, (output, mana, inputs) -> new RunicAltarRecipe(RecipeSerializers.UNKNOWN_ID,
			output, mana, inputs.toArray(new Ingredient[0]))));

	public static final StreamCodec<RegistryFriendlyByteBuf, RunicAltarRecipe> STREAM_CODEC = StreamCodec.composite(
			ItemStackTemplate.STREAM_CODEC, r -> r.output,
			ByteBufCodecs.VAR_INT, r -> r.mana,
			RecipeSerializers.ingredientList(), r -> r.inputs,
			(output, mana, inputs) -> new RunicAltarRecipe(RecipeSerializers.UNKNOWN_ID,
					output, mana, inputs.toArray(new Ingredient[0]))
	);

	public static final RecipeSerializer<RunicAltarRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
}
