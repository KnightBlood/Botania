/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;
import vazkii.botania.common.crafting.recipe.RecipeUtils;
import vazkii.botania.common.helper.IngredientHelper;

public class RecipeTerraPlate implements TerrestrialAgglomerationRecipe {
	private Identifier id;
	private final int mana;
	private final NonNullList<Ingredient> inputs;
	private final ItemStackTemplate output;

	public RecipeTerraPlate(Identifier id, int mana, NonNullList<Ingredient> inputs, ItemStackTemplate output) {
		this.id = id;
		this.mana = mana;
		this.inputs = inputs;
		this.output = output;
	}

	@Override
	public int getMana() {
		return mana;
	}

	@Override
	public boolean matches(RecipeInput inv, @NotNull Level world) {
		int nonEmptySlots = 0;
		for (int i = 0; i < inv.size(); i++) {
			if (!inv.getItem(i).isEmpty()) {
				if (inv.getItem(i).getCount() > 1) {
					return false;
				}
				nonEmptySlots++;
			}
		}

		IntOpenHashSet usedSlots = new IntOpenHashSet(inv.size());
		return RecipeUtils.matches(inputs, inv, usedSlots) && usedSlots.size() == nonEmptySlots;
	}

	@NotNull
	@Override
	public ItemStack assemble(@NotNull RecipeInput inv) {
		return output.create();
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
		return inputs;
	}

	@NotNull
	@Override
	public Identifier getId() {
		return id;
	}

	@NotNull
	@Override
	public RecipeSerializer<RecipeTerraPlate> getSerializer() {
		return BotaniaRecipeTypes.TERRA_PLATE_SERIALIZER;
	}

	public static final MapCodec<RecipeTerraPlate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("mana").forGetter(r -> r.mana),
			IngredientHelper.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.inputs),
			ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.output)
	).apply(i, (mana, inputs, output) -> new RecipeTerraPlate(RecipeSerializers.UNKNOWN_ID,
			mana, NonNullList.of(IngredientHelper.EMPTY, inputs.toArray(new Ingredient[0])), output)));

	public static final StreamCodec<RegistryFriendlyByteBuf, RecipeTerraPlate> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, r -> r.mana,
			RecipeSerializers.ingredientList(), r -> r.inputs,
			ItemStackTemplate.STREAM_CODEC, r -> r.output,
			(mana, inputs, output) -> new RecipeTerraPlate(RecipeSerializers.UNKNOWN_ID,
					mana, NonNullList.of(IngredientHelper.EMPTY, inputs.toArray(new Ingredient[0])), output)
	);

	public static final RecipeSerializer<RecipeTerraPlate> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
