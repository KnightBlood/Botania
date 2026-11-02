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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import vazkii.botania.api.recipe.BotaniaRecipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.PetalApothecaryRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.IngredientHelper;

import java.util.ArrayList;
import java.util.List;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

public class PetalsRecipe implements PetalApothecaryRecipe {
	private Identifier id;
	private final ItemStackTemplate output;
	private final Ingredient reagent;
	private final NonNullList<Ingredient> inputs;

	public PetalsRecipe(Identifier id, ItemStackTemplate output, Ingredient reagent, Ingredient... inputs) {
		Preconditions.checkArgument(inputs.length <= 16, "Cannot have more than 16 ingredients");
		this.id = id;
		this.output = output;
		this.reagent = reagent;
		this.inputs = NonNullList.of(IngredientHelper.EMPTY, inputs);
	}

	@Override
	public Ingredient getReagent() {
		return reagent;
	}

	@Override
	public boolean matches(RecipeInput inv, @NotNull Level world) {
		List<Ingredient> ingredientsMissing = new ArrayList<>(inputs);

		for (int i = 0; i < inv.size(); i++) {
			ItemStack input = inv.getItem(i);
			if (input.isEmpty()) {
				break;
			}

			int stackIndex = -1;

			for (int j = 0; j < ingredientsMissing.size(); j++) {
				Ingredient ingr = ingredientsMissing.get(j);
				if (ingr.test(input)) {
					stackIndex = j;
					break;
				}
			}

			if (stackIndex != -1) {
				ingredientsMissing.remove(stackIndex);
			} else {
				return false;
			}
		}

		return ingredientsMissing.isEmpty();
	}

	@NotNull
	@Override
	public final ItemStack getResultItem() {
		// 26.1: the output is parsed as a template, so it is only materialized into a real
		// stack once the item components have been bound.
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
		return new ItemStack(BotaniaBlocks.defaultAltar);
	}

	@NotNull
	@Override
	public Identifier getId() {
		return id;
	}

	@NotNull
	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.PETAL_SERIALIZER;
	}

	public static final MapCodec<PetalsRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ItemStackTemplate.CODEC.fieldOf("output").forGetter(r -> r.output),
			IngredientHelper.CODEC.fieldOf("reagent").forGetter(r -> r.reagent),
			IngredientHelper.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.inputs)
	).apply(i, (output, reagent, inputs) -> new PetalsRecipe(RecipeSerializers.UNKNOWN_ID,
			output, reagent, inputs.toArray(new Ingredient[0]))));

	public static final StreamCodec<RegistryFriendlyByteBuf, PetalsRecipe> STREAM_CODEC = StreamCodec.composite(
			ItemStackTemplate.STREAM_CODEC, r -> r.output,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.reagent,
			RecipeSerializers.ingredientList(), r -> r.inputs,
			(output, reagent, inputs) -> new PetalsRecipe(RecipeSerializers.UNKNOWN_ID,
					output, reagent, inputs.toArray(new Ingredient[0]))
	);

	public static final RecipeSerializer<PetalsRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);


}
