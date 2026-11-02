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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
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

import vazkii.botania.common.block.decor.BotaniaMushroomBlock;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.material.MysticalPetalItem;

/**
 * A shaped crafting recipe that dyes the resulting wand with the colors of the petals used.
 */
public class WandOfTheForestRecipe implements CraftingRecipe {
	public static final MapCodec<WandOfTheForestRecipe> CODEC = ShapedRecipe.MAP_CODEC.xmap(
			WandOfTheForestRecipe::new, recipe -> recipe.delegate);

	public static final StreamCodec<RegistryFriendlyByteBuf, WandOfTheForestRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC.map(
			WandOfTheForestRecipe::new, recipe -> recipe.delegate);

	public static final RecipeSerializer<WandOfTheForestRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private final ShapedRecipe delegate;

	public WandOfTheForestRecipe(ShapedRecipe delegate) {
		this.delegate = delegate;
	}

	public static WandOfTheForestRecipe wrap(Recipe<?> recipe) {
		return new WandOfTheForestRecipe((ShapedRecipe) recipe);
	}

	@Override
	public boolean matches(CraftingInput input, Level level) {
		return delegate.matches(input, level);
	}

	@Override
	public ItemStack assemble(CraftingInput input) {
		ItemStack result = delegate.assemble(input);
		int first = -1;
		for (int i = 0; i < input.size(); i++) {
			ItemStack stack = input.getItem(i);
			Item item = stack.getItem();

			int colorId;
			if (item instanceof MysticalPetalItem petal) {
				colorId = petal.color.getId();
			} else if (item instanceof BlockItem block && block.getBlock() instanceof BotaniaMushroomBlock mushroom) {
				colorId = mushroom.color.getId();
			} else {
				continue;
			}
			if (first == -1) {
				first = colorId;
			} else {
				return WandOfTheForestItem.setColors(result.copy(), first, colorId);
			}
		}
		return WandOfTheForestItem.setColors(result.copy(), first != -1 ? first : 0, 0);
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
