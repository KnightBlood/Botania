/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.helper.IngredientHelper;
import vazkii.botania.common.item.BotaniaItems;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BrewProvider extends BotaniaRecipeProvider {
	public BrewProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public String getName() {
		return "Botania Brew recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		saveFinishedRecipe(output, new FinishedRecipe(idFor("speed"), BotaniaBrews.speed, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.SUGAR), Ingredient.of(Items.REDSTONE)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("strength"), BotaniaBrews.strength, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(Items.GLOWSTONE_DUST)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("haste"), BotaniaBrews.haste, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.SUGAR), Ingredient.of(Items.GOLD_NUGGET)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("healing"), BotaniaBrews.healing, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GLISTERING_MELON_SLICE), Ingredient.of(Items.POTATO)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("jump_boost"), BotaniaBrews.jumpBoost, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.FEATHER), Ingredient.of(Items.CARROT)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("regeneration"), BotaniaBrews.regen, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GHAST_TEAR), Ingredient.of(Items.GLOWSTONE_DUST)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("weak_regeneration"), BotaniaBrews.regenWeak, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GHAST_TEAR), Ingredient.of(Items.REDSTONE)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("resistance"), BotaniaBrews.resistance, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.IRON_INGOT), Ingredient.of(Items.LEATHER)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("fire_resistance"), BotaniaBrews.fireResistance, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.MAGMA_CREAM), Ingredient.of(Blocks.NETHERRACK)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("water_breathing"), BotaniaBrews.waterBreathing, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.PRISMARINE_CRYSTALS), Ingredient.of(Items.GLOWSTONE_DUST)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("invisibility"), BotaniaBrews.invisibility, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.SNOWBALL), Ingredient.of(Items.GLOWSTONE_DUST)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("night_vision"), BotaniaBrews.nightVision, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.SPIDER_EYE), Ingredient.of(Items.GOLDEN_CARROT)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("absorption"), BotaniaBrews.absorption, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GOLDEN_APPLE), Ingredient.of(Items.POTATO)));

		saveFinishedRecipe(output, new FinishedRecipe(idFor("overload"), BotaniaBrews.overload, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.BLAZE_POWDER), Ingredient.of(Items.SUGAR), Ingredient.of(Items.GLOWSTONE_DUST), Ingredient.of(BotaniaItems.manaSteel), Ingredient.of(Items.SPIDER_EYE)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("soul_cross"), BotaniaBrews.soulCross, Ingredient.of(Items.NETHER_WART), Ingredient.of(Blocks.SOUL_SAND), Ingredient.of(Items.PAPER), Ingredient.of(Items.APPLE), Ingredient.of(Items.BONE)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("feather_feet"), BotaniaBrews.featherfeet, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.FEATHER), Ingredient.of(Items.LEATHER), IngredientHelper.of(ItemTags.WOOL)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("emptiness"), BotaniaBrews.emptiness, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.GUNPOWDER), Ingredient.of(Items.ROTTEN_FLESH), Ingredient.of(Items.BONE), Ingredient.of(Items.STRING), Ingredient.of(Items.ENDER_PEARL)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("bloodthirst"), BotaniaBrews.bloodthirst, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.FERMENTED_SPIDER_EYE), Ingredient.of(Items.LAPIS_LAZULI), Ingredient.of(Items.FIRE_CHARGE), Ingredient.of(Items.IRON_INGOT)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("allure"), BotaniaBrews.allure, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.COD), Ingredient.of(Items.QUARTZ), Ingredient.of(Items.GOLDEN_CARROT)));
		saveFinishedRecipe(output, new FinishedRecipe(idFor("clear"), BotaniaBrews.clear, Ingredient.of(Items.NETHER_WART), Ingredient.of(Items.QUARTZ), Ingredient.of(Items.EMERALD), Ingredient.of(Items.MELON_SLICE)));
	}

	private static Identifier idFor(String s) {
		return prefix("brew/" + s);
	}

	protected static class FinishedRecipe implements BotaniaFinishedRecipe {
		private final Identifier id;
		private final Brew brew;
		private final Ingredient[] inputs;

		private FinishedRecipe(Identifier id, Brew brew, Ingredient... inputs) {
			this.id = id;
			this.brew = brew;
			this.inputs = inputs;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.addProperty("brew", BotaniaAPI.instance().getBrewRegistry().getKey(brew).toString());
			JsonArray ingredients = new JsonArray();
			for (Ingredient ingr : inputs) {
				ingredients.add(IngredientHelper.serialize(ingr));
			}
			json.add("ingredients", ingredients);
		}

		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.BREW_SERIALIZER;
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
}
