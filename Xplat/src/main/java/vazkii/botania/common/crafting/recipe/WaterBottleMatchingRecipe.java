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

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

/**
 * A shaped crafting recipe that only matches when all the potions in the crafting grid are water
 * potions.
 */
public class WaterBottleMatchingRecipe implements CraftingRecipe {
	public static final MapCodec<WaterBottleMatchingRecipe> CODEC = ShapedRecipe.MAP_CODEC.xmap(
			WaterBottleMatchingRecipe::new, recipe -> recipe.delegate);

	public static final StreamCodec<RegistryFriendlyByteBuf, WaterBottleMatchingRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC.map(
			WaterBottleMatchingRecipe::new, recipe -> recipe.delegate);

	public static final RecipeSerializer<WaterBottleMatchingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public WaterBottleMatchingRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	public static WaterBottleMatchingRecipe wrap(Recipe<?> recipe) {
		return new WaterBottleMatchingRecipe((ShapedRecipe) recipe);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		if (!delegate.matches(input, level)) {
			return false;
		}
		for (int i = 0; i < input.size(); i++) {
			var item = input.getItem(i);
			if (item.is(Items.POTION) && !item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return delegate.assemble(input);
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return delegate.category();
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return delegate.getRemainingItems(input);
	}

	@Override
	public String group() {
		return delegate.group();
	}

	@Override
	public boolean showNotification() {
		return delegate.showNotification();
	}

	@Override
	public PlacementInfo placementInfo() {
		return delegate.placementInfo();
	}
}
