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
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A shaped crafting recipe that keeps the custom data (such as the phantom ink) of the armor that
 * is used in the crafting grid.
 */
public class ArmorUpgradeRecipe implements CraftingRecipe {
	public static final MapCodec<ArmorUpgradeRecipe> CODEC = ShapedRecipe.MAP_CODEC.xmap(
			ArmorUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final StreamCodec<RegistryFriendlyByteBuf, ArmorUpgradeRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC.map(
			ArmorUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final RecipeSerializer<ArmorUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public ArmorUpgradeRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	public static ArmorUpgradeRecipe wrap(Recipe<?> recipe) {
		return new ArmorUpgradeRecipe((ShapedRecipe) recipe);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack out = delegate.assemble(input);
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			if (stack.has(DataComponents.EQUIPPABLE) && stack.has(DataComponents.CUSTOM_DATA)) {
				out.set(DataComponents.CUSTOM_DATA, stack.get(DataComponents.CUSTOM_DATA));
				break;
			}
		}
		return out;
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
