/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonObject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.StateIngredientHelper;
import vazkii.botania.common.helper.IngredientHelper;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ManaInfusionProvider extends BotaniaRecipeProvider {
	public ManaInfusionProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public String getName() {
		return "Botania mana pool recipes";
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		saveFinishedRecipe(output, new FinishedRecipe(id("manasteel"), new ItemStack(BotaniaItems.manaSteel), Ingredient.of(Items.IRON_INGOT), 3000));
		saveFinishedRecipe(output, new FinishedRecipe(id("manasteel_block"), new ItemStack(BotaniaBlocks.manasteelBlock), ingr(Blocks.IRON_BLOCK), 27000));

		saveFinishedRecipe(output, new FinishedRecipe(id("mana_pearl"), new ItemStack(BotaniaItems.manaPearl), ingr(Items.ENDER_PEARL), 6000));

		saveFinishedRecipe(output, new FinishedRecipe(id("mana_diamond"), new ItemStack(BotaniaItems.manaDiamond), Ingredient.of(Items.DIAMOND), 10000));
		saveFinishedRecipe(output, new FinishedRecipe(id("mana_diamond_block"), new ItemStack(BotaniaBlocks.manaDiamondBlock), ingr(Blocks.DIAMOND_BLOCK), 90000));

		Ingredient dust = Ingredient.of(Items.GUNPOWDER, Items.REDSTONE, Items.GLOWSTONE_DUST, Items.SUGAR);
		saveFinishedRecipe(output, new FinishedRecipe(id("mana_powder_dust"), new ItemStack(BotaniaItems.manaPowder), dust, 500));
		Ingredient dyeIngredient = Ingredient.of(Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
				Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE,
				Items.LIGHT_GRAY_DYE, Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE,
				Items.BROWN_DYE, Items.GREEN_DYE, Items.RED_DYE, Items.BLACK_DYE);
		saveFinishedRecipe(output, new FinishedRecipe(id("mana_powder_dye"), new ItemStack(BotaniaItems.manaPowder), dyeIngredient, 400));

		saveFinishedRecipe(output, new FinishedRecipe(id("piston_relay"), new ItemStack(BotaniaBlocks.pistonRelay), ingr(Blocks.PISTON), 15000));
		saveFinishedRecipe(output, new FinishedRecipe(id("mana_cookie"), new ItemStack(BotaniaItems.manaCookie), ingr(Items.COOKIE), 20000));
		saveFinishedRecipe(output, new FinishedRecipe(id("grass_seeds"), new ItemStack(BotaniaItems.grassSeeds), ingr(Blocks.SHORT_GRASS), 2500));
		saveFinishedRecipe(output, new FinishedRecipe(id("podzol_seeds"), new ItemStack(BotaniaItems.podzolSeeds), ingr(Blocks.DEAD_BUSH), 2500));

		saveFinishedRecipe(output, new FinishedRecipe(id("mycel_seeds"), new ItemStack(BotaniaItems.mycelSeeds), Ingredient.of(Blocks.RED_MUSHROOM, Blocks.BROWN_MUSHROOM), 6500));

		saveFinishedRecipe(output, new FinishedRecipe(id("mana_quartz"), new ItemStack(BotaniaItems.manaQuartz), ingr(Items.QUARTZ), 250));
		saveFinishedRecipe(output, new FinishedRecipe(id("tiny_potato"), new ItemStack(BotaniaBlocks.tinyPotato), ingr(Items.POTATO), 1337));

		saveFinishedRecipe(output, new FinishedRecipe(id("mana_glass"), new ItemStack(BotaniaBlocks.manaGlass), ingr(Blocks.GLASS), 150));
		saveFinishedRecipe(output, new FinishedRecipe(id("mana_string"), new ItemStack(BotaniaItems.manaString), ingr(Items.STRING), 1250));

		saveFinishedRecipe(output, new FinishedRecipe(id("mana_bottle"), new ItemStack(BotaniaItems.manaBottle), ingr(Items.GLASS_BOTTLE), 5000));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("rotten_flesh_to_leather"), new ItemStack(Items.LEATHER), ingr(Items.ROTTEN_FLESH), 600));

		cycle(output, 40, "botania:log_cycle", Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG, Blocks.CHERRY_LOG);
		cycle(output, 40, "botania:froglight_cycle", Blocks.OCHRE_FROGLIGHT, Blocks.VERDANT_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT);
		cycle(output, 120, "botania:sapling_cycle", Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING, Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING, Blocks.MANGROVE_PROPAGULE, Blocks.CHERRY_SAPLING);

		saveFinishedRecipe(output, deconstruct("glowstone_deconstruct", Items.GLOWSTONE_DUST, Blocks.GLOWSTONE));
		saveFinishedRecipe(output, deconstruct("quartz_deconstruct", Items.QUARTZ, Blocks.QUARTZ_BLOCK));
		saveFinishedRecipe(output, deconstruct("dark_quartz_deconstruct", BotaniaItems.darkQuartz, BotaniaBlocks.darkQuartz));
		saveFinishedRecipe(output, deconstruct("mana_quartz_deconstruct", BotaniaItems.manaQuartz, BotaniaBlocks.manaQuartz));
		saveFinishedRecipe(output, deconstruct("blaze_quartz_deconstruct", BotaniaItems.blazeQuartz, BotaniaBlocks.blazeQuartz));
		saveFinishedRecipe(output, deconstruct("lavender_quartz_deconstruct", BotaniaItems.lavenderQuartz, BotaniaBlocks.lavenderQuartz));
		saveFinishedRecipe(output, deconstruct("red_quartz_deconstruct", BotaniaItems.redQuartz, BotaniaBlocks.redQuartz));
		saveFinishedRecipe(output, deconstruct("elf_quartz_deconstruct", BotaniaItems.elfQuartz, BotaniaBlocks.elfQuartz));
		saveFinishedRecipe(output, deconstruct("sunny_quartz_deconstruct", BotaniaItems.sunnyQuartz, BotaniaBlocks.sunnyQuartz));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("chiseled_stone_bricks"), new ItemStack(Blocks.CHISELED_STONE_BRICKS, 1), ingr(Blocks.STONE_BRICKS), 150));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("ice"), new ItemStack(Blocks.ICE), ingr(Blocks.SNOW_BLOCK), 2250));

		final String vineLilypadGroup = "botania:vine_and_lily_pad_cycle";
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("vine_to_lily_pad"), new ItemStack(Blocks.LILY_PAD), ingr(Blocks.VINE), 320, vineLilypadGroup));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("lily_pad_to_vine"), new ItemStack(Blocks.VINE), ingr(Blocks.LILY_PAD), 320, vineLilypadGroup));

		cycle(output, 200, "botania:fish_cycle", Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH);
		cycle(output, 6000, "botania:crop_cycle", Items.COCOA_BEANS, Items.WHEAT_SEEDS, Items.POTATO, Items.CARROT, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("potato_unpoison"), new ItemStack(Items.POTATO), ingr(Items.POISONOUS_POTATO), 1200));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("blaze_rod_to_nether_wart"), new ItemStack(Items.NETHER_WART), ingr(Items.BLAZE_ROD), 4000));

		cycle(output, 200, "", Items.GUNPOWDER, Items.FLINT);

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("book_to_name_tag"), new ItemStack(Items.NAME_TAG), ingr(Items.WRITABLE_BOOK), 6000));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("wool_deconstruct"), new ItemStack(Items.STRING, 3), IngredientHelper.of(ItemTags.WOOL), 100));

		final String cactusSlimeGroup = "botania:cactus_and_slime_cycle";
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("cactus_to_slime"), new ItemStack(Items.SLIME_BALL), ingr(Blocks.CACTUS), 1200, cactusSlimeGroup));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("slime_to_cactus"), new ItemStack(Blocks.CACTUS), ingr(Items.SLIME_BALL), 1200, cactusSlimeGroup));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("ender_pearl_from_ghast_tear"), new ItemStack(Items.ENDER_PEARL), ingr(Items.GHAST_TEAR), 28000));

		cycle(output, 300, "botania:glowstone_and_redstone_cycle", Items.GLOWSTONE_DUST, Items.REDSTONE);

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("cobble_to_sand"), new ItemStack(Blocks.SAND), ingr(Blocks.COBBLESTONE), 50));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("terracotta_to_red_sand"), new ItemStack(Blocks.RED_SAND), ingr(Blocks.TERRACOTTA), 50));

		saveFinishedRecipe(output, deconstruct("clay_deconstruct", Items.CLAY_BALL, Blocks.CLAY));
		saveFinishedRecipe(output, deconstruct("brick_deconstruct", Items.BRICK, Blocks.BRICKS));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("coarse_dirt"), new ItemStack(Blocks.COARSE_DIRT), ingr(Blocks.DIRT), 120));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("soul_soil"), new ItemStack(Blocks.SOUL_SOIL), ingr(Blocks.SOUL_SAND), 120));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("stone_to_andesite"), new ItemStack(Blocks.ANDESITE), ingr(Blocks.STONE), 200));
		cycle(output, 200, "botania:stone_cycle", Blocks.DIORITE, Blocks.GRANITE, Blocks.ANDESITE);

		cycle(output, 200, "botania:117_stone_cycle", Blocks.TUFF, Blocks.CALCITE, Blocks.DEEPSLATE);

		cycle(output, 500, "botania:shrub_cycle", Blocks.FERN, Blocks.DEAD_BUSH, Blocks.SHORT_GRASS);

		// NB: No wither rose is intentional
		cycle(output, 400, "botania:flower_cycle", Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP,
				Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY,
				Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY);

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("dripleaf_shrinking"), new ItemStack(Blocks.SMALL_DRIPLEAF), ingr(Items.BIG_DRIPLEAF), 500));
		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("chorus_fruit_to_flower"), new ItemStack(Blocks.CHORUS_FLOWER), ingr(Items.POPPED_CHORUS_FRUIT), 10000));

		cycle(output, 240, "botania:berry_cycle", Items.APPLE, Items.SWEET_BERRIES, Items.GLOW_BERRIES);

		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.agricarnationChibi, BotaniaFlowerBlocks.agricarnation));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.clayconiaChibi, BotaniaFlowerBlocks.clayconia));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.bellethornChibi, BotaniaFlowerBlocks.bellethorn));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.bubbellChibi, BotaniaFlowerBlocks.bubbell));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.hopperhockChibi, BotaniaFlowerBlocks.hopperhock));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.jiyuuliaChibi, BotaniaFlowerBlocks.jiyuulia));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.tangleberrieChibi, BotaniaFlowerBlocks.tangleberrie));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.marimorphosisChibi, BotaniaFlowerBlocks.marimorphosis));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.rannuncarpusChibi, BotaniaFlowerBlocks.rannuncarpus));
		saveFinishedRecipe(output, mini(BotaniaFlowerBlocks.solegnoliaChibi, BotaniaFlowerBlocks.solegnolia));

		saveFinishedRecipe(output, FinishedRecipe.alchemy(id("hydroangeas_motif"), new ItemStack(BotaniaBlocks.motifHydroangeas), ingr(BotaniaFlowerBlocks.hydroangeas), 2500));

		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("redstone_dupe"), new ItemStack(Items.REDSTONE, 2), ingr(Items.REDSTONE), 5000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("glowstone_dupe"), new ItemStack(Items.GLOWSTONE_DUST, 2), ingr(Items.GLOWSTONE_DUST), 5000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("quartz_dupe"), new ItemStack(Items.QUARTZ, 2), ingr(Items.QUARTZ), 2500));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("coal_dupe"), new ItemStack(Items.COAL, 2), ingr(Items.COAL), 2100));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("snowball_dupe"), new ItemStack(Items.SNOWBALL, 2), ingr(Items.SNOWBALL), 200));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("netherrack_dupe"), new ItemStack(Blocks.NETHERRACK, 2), ingr(Blocks.NETHERRACK), 200));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("soul_sand_dupe"), new ItemStack(Blocks.SOUL_SAND, 2), ingr(Blocks.SOUL_SAND), 1500));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("gravel_dupe"), new ItemStack(Blocks.GRAVEL, 2), ingr(Blocks.GRAVEL), 720));

		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("oak_leaves_dupe"), new ItemStack(Blocks.OAK_LEAVES, 2), ingr(Blocks.OAK_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("birch_leaves_dupe"), new ItemStack(Blocks.BIRCH_LEAVES, 2), ingr(Blocks.BIRCH_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("spruce_leaves_dupe"), new ItemStack(Blocks.SPRUCE_LEAVES, 2), ingr(Blocks.SPRUCE_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("jungle_leaves_dupe"), new ItemStack(Blocks.JUNGLE_LEAVES, 2), ingr(Blocks.JUNGLE_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("acacia_leaves_dupe"), new ItemStack(Blocks.ACACIA_LEAVES, 2), ingr(Blocks.ACACIA_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("dark_oak_leaves_dupe"), new ItemStack(Blocks.DARK_OAK_LEAVES, 2), ingr(Blocks.DARK_OAK_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("azalea_leaves_dupe"), new ItemStack(Blocks.AZALEA_LEAVES, 2), ingr(Blocks.AZALEA_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("flowering_azalea_leaves_dupe"), new ItemStack(Blocks.FLOWERING_AZALEA_LEAVES, 2), ingr(Blocks.FLOWERING_AZALEA_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("mangrove_leaves_dupe"), new ItemStack(Blocks.MANGROVE_LEAVES, 2), ingr(Blocks.MANGROVE_LEAVES), 2000));
		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("cherry_leaves_dupe"), new ItemStack(Blocks.CHERRY_LEAVES, 2), ingr(Blocks.CHERRY_LEAVES), 2000));

		saveFinishedRecipe(output, FinishedRecipe.conjuration(id("grass"), new ItemStack(Blocks.SHORT_GRASS, 2), ingr(Blocks.SHORT_GRASS), 800));
	}

	protected void cycle(RecipeOutput output, int cost, String group, ItemLike... items) {
		for (int i = 0; i < items.length; i++) {
			Ingredient in = ingr(items[i]);
			ItemStack out = new ItemStack(i == items.length - 1 ? items[0] : items[i + 1]);
			String id = String.format("%s_to_%s", BuiltInRegistries.ITEM.getKey(items[i].asItem()).getPath(), BuiltInRegistries.ITEM.getKey(out.getItem()).getPath());
			saveFinishedRecipe(output, FinishedRecipe.alchemy(id(id), out, in, cost, group));
		}
	}

	protected BotaniaFinishedRecipe mini(ItemLike mini, ItemLike full) {
		return FinishedRecipe.alchemy(id(BuiltInRegistries.ITEM.getKey(mini.asItem()).getPath()), new ItemStack(mini), ingr(full), 2500, "botania:flower_shrinking");
	}

	protected BotaniaFinishedRecipe deconstruct(String id, ItemLike items, ItemLike block) {
		return FinishedRecipe.alchemy(id(id), new ItemStack(items, 4), ingr(block), 25, "botania:block_deconstruction");
	}

	protected Identifier id(String s) {
		return prefix("mana_infusion/" + s);
	}

	protected static Ingredient ingr(ItemLike i) {
		return Ingredient.of(i);
	}

	protected static class FinishedRecipe implements BotaniaFinishedRecipe {
		private static final StateIngredient CONJURATION = StateIngredientHelper.of(BotaniaBlocks.conjurationCatalyst);
		private static final StateIngredient ALCHEMY = StateIngredientHelper.of(BotaniaBlocks.alchemyCatalyst);

		private final Identifier id;
		private final Ingredient input;
		private final ItemStack output;
		private final int mana;
		private final String group;
		@Nullable
		private final StateIngredient catalyst;

		public static BotaniaFinishedRecipe conjuration(Identifier id, ItemStack output, Ingredient input, int mana) {
			return new FinishedRecipe(id, output, input, mana, "", CONJURATION);
		}

		public static BotaniaFinishedRecipe alchemy(Identifier id, ItemStack output, Ingredient input, int mana) {
			return alchemy(id, output, input, mana, "");
		}

		public static BotaniaFinishedRecipe alchemy(Identifier id, ItemStack output, Ingredient input, int mana, String group) {
			return new FinishedRecipe(id, output, input, mana, group, ALCHEMY);
		}

		public FinishedRecipe(Identifier id, ItemStack output, Ingredient input, int mana) {
			this(id, output, input, mana, "");
		}

		public FinishedRecipe(Identifier id, ItemStack output, Ingredient input, int mana, String group) {
			this(id, output, input, mana, group, null);
		}

		public FinishedRecipe(Identifier id, ItemStack output, Ingredient input, int mana, String group, @Nullable StateIngredient catalyst) {
			this.id = id;
			this.input = input;
			this.output = output;
			this.mana = mana;
			this.group = group;
			this.catalyst = catalyst;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			json.add("input", IngredientHelper.serialize(input));
			json.add("output", ItemNBTHelper.serializeStack(output));
			json.addProperty("mana", mana);
			if (!group.isEmpty()) {
				json.addProperty("group", group);
			}
			if (catalyst != null) {
				json.add("catalyst", catalyst.serialize());
			}
		}

		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return BotaniaRecipeTypes.MANA_INFUSION_SERIALIZER;
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
