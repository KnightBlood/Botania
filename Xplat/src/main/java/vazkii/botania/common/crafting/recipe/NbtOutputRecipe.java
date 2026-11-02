/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import com.mojang.brigadier.StringReader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A crafting recipe that adds custom data to the result of another recipe.
 */
public class NbtOutputRecipe implements CraftingRecipe {
	public static final MapCodec<NbtOutputRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MapCodec.assumeMapUnsafe(Recipe.CODEC).fieldOf("recipe").forGetter(r -> r.delegate),
			Codec.STRING.fieldOf("nbt").forGetter(r -> r.tag.toString())
	).apply(i, (recipe, nbt) -> new NbtOutputRecipe(recipe, parseTag(nbt))));

	public static final StreamCodec<RegistryFriendlyByteBuf, NbtOutputRecipe> STREAM_CODEC = StreamCodec.composite(
			Recipe.STREAM_CODEC, r -> r.delegate,
			ByteBufCodecs.COMPOUND_TAG, r -> r.tag,
			NbtOutputRecipe::new
	);

	public static final RecipeSerializer<NbtOutputRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final Recipe<?> delegate;
	private final CompoundTag tag;

	public NbtOutputRecipe(Recipe<?> delegate, CompoundTag tag) {
		this.delegate = delegate;
		this.tag = tag;
	}

	public static NbtOutputRecipe wrap(Recipe<?> recipe, CompoundTag tag) {
		return new NbtOutputRecipe(recipe, tag);
	}

	private static CompoundTag parseTag(String nbt) {
		try {
			return TagParser.parseCompoundAsArgument(new StringReader(nbt));
		} catch (CommandSyntaxException e) {
			throw new IllegalArgumentException("Invalid nbt tag: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return delegate instanceof CraftingRecipe craftingRecipe && craftingRecipe.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack result = delegate instanceof CraftingRecipe craftingRecipe
				? craftingRecipe.assemble(input)
				: ItemStack.EMPTY;
		result.set(DataComponents.CUSTOM_DATA, CustomData.of(tag.copy()));
		return result;
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return delegate instanceof CraftingRecipe craftingRecipe ? craftingRecipe.category() : CraftingBookCategory.MISC;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return delegate instanceof CraftingRecipe craftingRecipe
				? craftingRecipe.getRemainingItems(input)
				: NonNullList.withSize(input.size(), ItemStack.EMPTY);
	}

	@Override
	public String group() {
		return delegate instanceof CraftingRecipe craftingRecipe ? craftingRecipe.group() : "";
	}

	@Override
	public boolean showNotification() {
		return delegate instanceof CraftingRecipe craftingRecipe && craftingRecipe.showNotification();
	}

	@Override
	public PlacementInfo placementInfo() {
		return delegate instanceof CraftingRecipe craftingRecipe
				? craftingRecipe.placementInfo()
				: PlacementInfo.NOT_PLACEABLE;
	}
}
