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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import vazkii.botania.xplat.XplatAbstractions;

/**
 * A shaped crafting recipe that adds up the mana of all the mana items in the crafting grid.
 */
public class ManaUpgradeRecipe implements CraftingRecipe {
	public static final MapCodec<ManaUpgradeRecipe> CODEC = ShapedRecipe.MAP_CODEC.xmap(
			ManaUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final StreamCodec<RegistryFriendlyByteBuf, ManaUpgradeRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC.map(
			ManaUpgradeRecipe::new, recipe -> recipe.delegate);

	public static final RecipeSerializer<ManaUpgradeRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public ManaUpgradeRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	public static ManaUpgradeRecipe wrap(Recipe<?> recipe) {
		return new ManaUpgradeRecipe((ShapedRecipe) recipe);
	}

	public static ItemStack output(ItemStack output, RecipeInput inv) {
		ItemStack out = output.copy();
		var outItem = XplatAbstractions.INSTANCE.findManaItem(out);
		if (outItem == null) {
			return out;
		}
		for (int i = 0; i < inv.size(); i++) {
			ItemStack stack = inv.getItem(i);
			var item = XplatAbstractions.INSTANCE.findManaItem(stack);
			if (!stack.isEmpty() && item != null) {
				outItem.addMana(item.getMana());
			}
		}
		return out;
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		return output(delegate.assemble(input), input);
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
