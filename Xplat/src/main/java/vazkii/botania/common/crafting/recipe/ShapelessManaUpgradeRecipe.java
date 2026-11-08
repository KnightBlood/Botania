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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

/**
 * A shapeless crafting recipe that adds up the mana of all the mana items in the crafting grid.
 */
public class ShapelessManaUpgradeRecipe implements CraftingRecipe {
	public static final MapCodec<ShapelessManaUpgradeRecipe> CODEC = ShapelessRecipe.MAP_CODEC.xmap(
			ShapelessManaUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final StreamCodec<RegistryFriendlyByteBuf, ShapelessManaUpgradeRecipe> STREAM_CODEC = ShapelessRecipe.STREAM_CODEC.map(
			ShapelessManaUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final RecipeSerializer<ShapelessManaUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapelessRecipe delegate;

	public ShapelessManaUpgradeRecipe(ShapelessRecipe delegate) {
		this.delegate = delegate;
	}

	public static ShapelessManaUpgradeRecipe wrap(Recipe<?> recipe) {
		return new ShapelessManaUpgradeRecipe((ShapelessRecipe) recipe);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return ManaUpgradeRecipe.output(delegate.assemble(input), input);
	}

	@Override
	public RecipeSerializer<? extends CraftingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public CraftingBookCategory category() {
		return CraftingBookCategory.EQUIPMENT;
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
