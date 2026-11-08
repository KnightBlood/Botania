/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;
import vazkii.botania.common.helper.IngredientHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ElvenTradeRecipe implements vazkii.botania.api.recipe.ElvenTradeRecipe {
	private Identifier id;
	private final ImmutableList<ItemStackTemplate> outputs;
	private final NonNullList<Ingredient> inputs;

	public ElvenTradeRecipe(Identifier id, ItemStackTemplate[] outputs, Ingredient... inputs) {
		this.id = id;
		this.outputs = ImmutableList.copyOf(outputs);
		this.inputs = NonNullList.create();
		this.inputs.addAll(Arrays.asList(inputs));
	}

	@Override
	public Optional<List<ItemStack>> match(List<ItemStack> stacks) {
		List<Ingredient> inputsMissing = new ArrayList<>(inputs);
		List<ItemStack> stacksToRemove = new ArrayList<>();

		for (ItemStack stack : stacks) {
			if (stack.isEmpty()) {
				continue;
			}
			if (inputsMissing.isEmpty()) {
				break;
			}

			int stackIndex = -1;

			for (int i = 0; i < inputsMissing.size(); i++) {
				Ingredient ingr = inputsMissing.get(i);
				if (ingr.test(stack)) {
					if (!stacksToRemove.contains(stack)) {
						stacksToRemove.add(stack);
					}
					stackIndex = i;
					break;
				}
			}

			if (stackIndex != -1) {
				inputsMissing.remove(stackIndex);
			}
		}

		return inputsMissing.isEmpty() ? Optional.of(stacksToRemove) : Optional.empty();
	}

	@Override
	public boolean containsItem(ItemStack stack) {
		for (Ingredient input : inputs) {
			if (input.test(stack)) {
				return true;
			}
		}
		return false;
	}

	@NotNull
	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.ELVEN_TRADE_SERIALIZER;
	}

	@NotNull
	@Override
	public NonNullList<Ingredient> getIngredients() {
		return inputs;
	}

	@NotNull
	public ItemStack getToastSymbol() {
		return new ItemStack(BotaniaBlocks.alfPortal);
	}

	@NotNull
	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public List<ItemStack> getOutputs() {
		// 26.1: the outputs are stored as templates so they can be parsed before item
		// components are bound. Materialize them only when they are actually needed.
		return outputs.stream().map(ItemStackTemplate::create).toList();
	}

	@Override
	public List<ItemStack> getOutputs(List<ItemStack> inputs) {
		return getOutputs();
	}

	private static final Codec<List<ItemStackTemplate>> OUTPUT_CODEC = Codec.withAlternative(
			ItemStackTemplate.CODEC.listOf(),
			ItemStackTemplate.CODEC.xmap(List::of, list -> list.get(0))
	);

	public static final MapCodec<ElvenTradeRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			OUTPUT_CODEC.fieldOf("output").forGetter(r -> r.outputs),
			IngredientHelper.CODEC.listOf().fieldOf("ingredients").forGetter(r -> r.inputs)
	).apply(i, (outputs, inputs) -> new ElvenTradeRecipe(RecipeSerializers.UNKNOWN_ID,
			outputs.toArray(new ItemStackTemplate[0]), inputs.toArray(new Ingredient[0]))));

	public static final StreamCodec<RegistryFriendlyByteBuf, ElvenTradeRecipe> STREAM_CODEC = StreamCodec.composite(
			RecipeSerializers.itemStackTemplateList(), r -> r.outputs,
			RecipeSerializers.ingredientList(), r -> r.inputs,
			(outputs, inputs) -> new ElvenTradeRecipe(RecipeSerializers.UNKNOWN_ID,
					outputs.toArray(new ItemStackTemplate[0]), inputs.toArray(new Ingredient[0]))
	);

	public static final RecipeSerializer<ElvenTradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
