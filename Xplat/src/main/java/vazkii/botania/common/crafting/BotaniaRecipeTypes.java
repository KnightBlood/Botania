/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import vazkii.botania.api.recipe.*;
import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.common.crafting.recipe.HeadRecipe;
import vazkii.botania.common.crafting.recipe.NoOpRecipeSerializer;
import vazkii.botania.common.crafting.recipe.StateCopyingPureDaisyRecipe;
import vazkii.botania.common.helper.RecipeHelper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BotaniaRecipeTypes {
	public static final RecipeType<vazkii.botania.api.recipe.ManaInfusionRecipe> MANA_INFUSION_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<ManaInfusionRecipe> MANA_INFUSION_SERIALIZER = ManaInfusionRecipe.SERIALIZER;

	public static final RecipeType<vazkii.botania.api.recipe.ElvenTradeRecipe> ELVEN_TRADE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<ElvenTradeRecipe> ELVEN_TRADE_SERIALIZER = ElvenTradeRecipe.SERIALIZER;
	public static final RecipeSerializer<LexiconElvenTradeRecipe> LEXICON_ELVEN_TRADE_SERIALIZER = NoOpRecipeSerializer.create(LexiconElvenTradeRecipe::new);

	public static final RecipeType<vazkii.botania.api.recipe.PureDaisyRecipe> PURE_DAISY_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<PureDaisyRecipe> PURE_DAISY_SERIALIZER = PureDaisyRecipe.SERIALIZER;
	public static final RecipeSerializer<StateCopyingPureDaisyRecipe> COPYING_PURE_DAISY_SERIALIZER = StateCopyingPureDaisyRecipe.SERIALIZER;

	public static final RecipeType<vazkii.botania.api.recipe.BotanicalBreweryRecipe> BREW_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<BotanicalBreweryRecipe> BREW_SERIALIZER = BotanicalBreweryRecipe.SERIALIZER;

	public static final RecipeType<PetalApothecaryRecipe> PETAL_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<PetalsRecipe> PETAL_SERIALIZER = PetalsRecipe.SERIALIZER;

	public static final RecipeType<vazkii.botania.api.recipe.RunicAltarRecipe> RUNE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<RunicAltarRecipe> RUNE_SERIALIZER = RunicAltarRecipe.SERIALIZER;
	public static final RecipeSerializer<HeadRecipe> RUNE_HEAD_SERIALIZER = HeadRecipe.SERIALIZER;

	public static final RecipeType<TerrestrialAgglomerationRecipe> TERRA_PLATE_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<RecipeTerraPlate> TERRA_PLATE_SERIALIZER = RecipeTerraPlate.SERIALIZER;

	public static final RecipeType<OrechidRecipe> ORECHID_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<OrechidRecipe> ORECHID_SERIALIZER = OrechidRecipe.SERIALIZER;

	public static final RecipeType<OrechidIgnemRecipe> ORECHID_IGNEM_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<OrechidIgnemRecipe> ORECHID_IGNEM_SERIALIZER = OrechidIgnemRecipe.SERIALIZER;

	public static final RecipeType<MarimorphosisRecipe> MARIMORPHOSIS_TYPE = new ModRecipeType<>();
	public static final RecipeSerializer<MarimorphosisRecipe> MARIMORPHOSIS_SERIALIZER = MarimorphosisRecipe.SERIALIZER;

	public static void submitRecipeTypes(BiConsumer<RecipeType<?>, Identifier> r) {
		r.accept(ELVEN_TRADE_TYPE, vazkii.botania.api.recipe.ElvenTradeRecipe.TYPE_ID);
		r.accept(MANA_INFUSION_TYPE, vazkii.botania.api.recipe.ManaInfusionRecipe.TYPE_ID);
		r.accept(PURE_DAISY_TYPE, vazkii.botania.api.recipe.PureDaisyRecipe.TYPE_ID);
		r.accept(BREW_TYPE, vazkii.botania.api.recipe.BotanicalBreweryRecipe.TYPE_ID);
		r.accept(PETAL_TYPE, PetalApothecaryRecipe.TYPE_ID);
		r.accept(RUNE_TYPE, vazkii.botania.api.recipe.RunicAltarRecipe.TYPE_ID);
		r.accept(TERRA_PLATE_TYPE, TerrestrialAgglomerationRecipe.TYPE_ID);
		r.accept(ORECHID_TYPE, vazkii.botania.api.recipe.OrechidRecipe.TYPE_ID);
		r.accept(ORECHID_IGNEM_TYPE, vazkii.botania.api.recipe.OrechidRecipe.IGNEM_TYPE_ID);
		r.accept(MARIMORPHOSIS_TYPE, vazkii.botania.api.recipe.OrechidRecipe.MARIMORPHOSIS_TYPE_ID);
	}

	public static void submitRecipeSerializers(BiConsumer<RecipeSerializer<?>, Identifier> r) {
		r.accept(ELVEN_TRADE_SERIALIZER, vazkii.botania.api.recipe.ElvenTradeRecipe.TYPE_ID);
		r.accept(LEXICON_ELVEN_TRADE_SERIALIZER, prefix("elven_trade_lexicon"));
		r.accept(MANA_INFUSION_SERIALIZER, vazkii.botania.api.recipe.ManaInfusionRecipe.TYPE_ID);
		r.accept(PURE_DAISY_SERIALIZER, vazkii.botania.api.recipe.PureDaisyRecipe.TYPE_ID);
		r.accept(COPYING_PURE_DAISY_SERIALIZER, prefix("state_copying_pure_daisy"));
		r.accept(BREW_SERIALIZER, vazkii.botania.api.recipe.BotanicalBreweryRecipe.TYPE_ID);
		r.accept(PETAL_SERIALIZER, PetalApothecaryRecipe.TYPE_ID);
		r.accept(RUNE_SERIALIZER, vazkii.botania.api.recipe.RunicAltarRecipe.TYPE_ID);
		r.accept(RUNE_HEAD_SERIALIZER, prefix("runic_altar_head"));
		r.accept(TERRA_PLATE_SERIALIZER, TerrestrialAgglomerationRecipe.TYPE_ID);
		r.accept(ORECHID_SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.TYPE_ID);
		r.accept(ORECHID_IGNEM_SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.IGNEM_TYPE_ID);
		r.accept(MARIMORPHOSIS_SERIALIZER, vazkii.botania.api.recipe.OrechidRecipe.MARIMORPHOSIS_TYPE_ID);
	}

	private static class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
		@Override
		public String toString() {
			return BuiltInRegistries.RECIPE_TYPE.getKey(this).toString();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Recipe<?>> Map<Identifier, T> getRecipes(Level world, RecipeType<T> type) {
		Map<Identifier, T> result = new LinkedHashMap<>();
		RecipeManager manager = RecipeHelper.getRecipeManager(world);
		if (manager == null) {
			return result;
		}
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (holder.value().getType().equals(type)) {
				if (holder.value() instanceof BotaniaRecipe<?> botaniaRecipe) {
					botaniaRecipe.setId(holder.id().identifier());
				}
				result.put(holder.id().identifier(), (T) holder.value());
			}
		}
		return result;
	}

	/**
	 * Looks up the {@link RecipeHolder} backing a recipe instance obtained through {@link #getRecipes},
	 * needed for APIs that take a holder (e.g. {@code Player.triggerRecipeCrafted}).
	 */
	public static <T extends Recipe<?>> Optional<RecipeHolder<T>> getHolder(Level world, RecipeType<T> type, T recipe) {
		RecipeManager manager = RecipeHelper.getRecipeManager(world);
		if (manager == null) {
			return Optional.empty();
		}
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (holder.value() == recipe) {
				@SuppressWarnings("unchecked")
				RecipeHolder<T> typed = (RecipeHolder<T>) holder;
				return Optional.of(typed);
			}
		}
		return Optional.empty();
	}
}
