/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.StateIngredientHelper;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class PureDaisyProvider extends BotaniaRecipeProvider {
	public PureDaisyProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public void buildRecipes(RecipeOutput output) {

		saveFinishedRecipe(output, new FinishedRecipe(id("livingrock"), StateIngredientHelper.of(Blocks.STONE), BotaniaBlocks.livingrock.defaultBlockState()));
		saveFinishedRecipe(output, new StateCopyingRecipe(id("livingwood"),
				StateIngredientHelper.of(BlockTags.LOGS),
				BotaniaBlocks.livingwoodLog));

		saveFinishedRecipe(output, new FinishedRecipe(id("cobblestone"), StateIngredientHelper.of(Blocks.NETHERRACK), Blocks.COBBLESTONE.defaultBlockState()));
		saveFinishedRecipe(output, new FinishedRecipe(id("end_stone_to_cobbled_deepslate"), StateIngredientHelper.of(Blocks.END_STONE), Blocks.COBBLED_DEEPSLATE.defaultBlockState(), FinishedRecipe.DEFAULT_TIME, prefix("ender_air_release")));
		saveFinishedRecipe(output, new FinishedRecipe(id("sand"), StateIngredientHelper.of(Blocks.SOUL_SAND), Blocks.SAND.defaultBlockState()));
		saveFinishedRecipe(output, new FinishedRecipe(id("packed_ice"), StateIngredientHelper.of(Blocks.ICE), Blocks.PACKED_ICE.defaultBlockState()));
		saveFinishedRecipe(output, new FinishedRecipe(id("blue_ice"), StateIngredientHelper.of(Blocks.PACKED_ICE), Blocks.BLUE_ICE.defaultBlockState()));
		saveFinishedRecipe(output, new FinishedRecipe(id("obsidian"), StateIngredientHelper.of(BotaniaBlocks.blazeBlock), Blocks.OBSIDIAN.defaultBlockState()));
		saveFinishedRecipe(output, new FinishedRecipe(id("snow_block"), StateIngredientHelper.of(Blocks.WATER), Blocks.SNOW_BLOCK.defaultBlockState()));
	}

	@Override
	public String getName() {
		return "Botania Pure Daisy recipes";
	}

	private static Identifier id(String path) {
		return prefix("pure_daisy/" + path);
	}

	protected static class FinishedRecipe implements BotaniaFinishedRecipe {
		public static final int DEFAULT_TIME = 150;

		protected final Identifier id;
		protected final StateIngredient input;
		protected final BlockState outputState;
		protected final int time;
		@Nullable
		private final Identifier function;

		public FinishedRecipe(Identifier id, StateIngredient input, BlockState state) {
			this(id, input, state, DEFAULT_TIME);
		}

		public FinishedRecipe(Identifier id, StateIngredient input, BlockState state, int time) {
			this(id, input, state, time, null);
		}

		public FinishedRecipe(Identifier id, StateIngredient input, BlockState state, int time, @Nullable Identifier function) {
			Preconditions.checkArgument(time >= 0, "Time must be nonnegative");
			this.id = id;
			this.input = input;
			this.outputState = state;
			this.time = time;
			this.function = function;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("input", input.serialize());
			json.add("output", StateIngredientHelper.serializeBlockState(outputState));
			if (time != DEFAULT_TIME) {
				json.addProperty("time", time);
			}
			if (function != null) {
				json.addProperty("success_function", function.toString());
			}
		}

		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.PURE_DAISY_SERIALIZER;
		}

		@Nullable
		@Override
		public JsonObject serializeAdvancement() {
			return null;
		}

		@Nullable
		@Override
		public Identifier getAdvancementId() {
			return null;
		}
	}

	protected static class StateCopyingRecipe extends FinishedRecipe {
		public StateCopyingRecipe(Identifier id, StateIngredient input, Block block) {
			super(id, input, block.defaultBlockState());
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("input", input.serialize());
			json.addProperty("output", BuiltInRegistries.BLOCK.getKey(outputState.getBlock()).toString());
			if (time != DEFAULT_TIME) {
				json.addProperty("time", time);
			}
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.COPYING_PURE_DAISY_SERIALIZER;
		}
	}
}
