package vazkii.botania.neoforge.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;

import org.apache.commons.lang3.mutable.MutableObject;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.data.recipes.BotaniaFinishedRecipe;
import vazkii.botania.data.recipes.BotaniaRecipeProvider;
import vazkii.botania.data.recipes.GogAlternationResult;

import static vazkii.botania.data.recipes.CraftingRecipeProvider.*;
// NOTE: saveFinishedRecipe is inherited (protected static) from BotaniaRecipeProvider;
// a static import of a protected member from another package is not permitted.

public class NeoForgeRecipeProvider extends BotaniaRecipeProvider {
	public static final TagKey<Item> QUARTZ_STORAGE = TagKey.create(Registries.ITEM,
			Identifier.fromNamespaceAndPath("c", "storage_blocks/quartz"));

	public NeoForgeRecipeProvider(PackOutput output) {
		super(output);
	}

	@Override
	protected void buildRecipes(RecipeOutput output) {
		ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.BUILDING_BLOCKS, BotaniaBlocks.azulejo0)
				.requires(Items.BLUE_DYE)
				.requires(tagIngredient(QUARTZ_STORAGE))
				.unlockedBy("has_item", conditionsFromItem(Items.BLUE_DYE))
				.save(output);

		ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.TOOLS, BotaniaItems.baubleBox)
				.define('C', tagIngredient(Tags.Items.CHESTS_WOODEN))
				.define('G', Items.GOLD_INGOT)
				.define('M', tagIngredient(BotaniaTags.Items.INGOTS_MANASTEEL))
				.pattern(" M ")
				.pattern("MCM")
				.pattern(" G ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);

		ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.TOOLS, BotaniaItems.glassPick)
				.define('T', BotaniaItems.livingwoodTwig)
				.define('G', tagIngredient(Tags.Items.GLASS_BLOCKS_COLORLESS))
				.define('I', tagIngredient(BotaniaTags.Items.INGOTS_MANASTEEL))
				.pattern("GIG")
				.pattern(" T ")
				.pattern(" T ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);

		ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.DECORATIONS, BotaniaBlocks.prism)
				.define('P', tagIngredient(Tags.Items.GEMS_PRISMARINE))
				.define('S', BotaniaBlocks.spectralPlatform)
				.define('G', tagIngredient(Tags.Items.GLASS_BLOCKS_COLORLESS))
				.pattern("GPG")
				.pattern("GSG")
				.pattern("GPG")
				.unlockedBy("has_item", conditionsFromTag(Tags.Items.GEMS_PRISMARINE))
				.unlockedBy("has_alt_item", conditionsFromItem(BotaniaBlocks.spectralPlatform))
				.save(output);

		ShapedRecipeBuilder.shaped(BuiltInRegistries.ITEM, RecipeCategory.MISC, BotaniaItems.lensNormal)
				.define('S', tagIngredient(BotaniaTags.Items.INGOTS_MANASTEEL))
				.define('G', net.neoforged.neoforge.common.crafting.CompoundIngredient.of(
						tagIngredient(Tags.Items.GLASS_BLOCKS_COLORLESS),
						tagIngredient(Tags.Items.GLASS_PANES_COLORLESS)))
				.pattern(" S ")
				.pattern("SGS")
				.pattern(" S ")
				.unlockedBy("has_item", conditionsFromTag(BotaniaTags.Items.INGOTS_MANASTEEL))
				.save(output);

		registerRedStringBlock(output, BotaniaBlocks.redStringContainer, tagIngredient(Tags.Items.CHESTS_WOODEN), conditionsFromTag(Tags.Items.CHESTS_WOODEN));
		ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.REDSTONE, BotaniaBlocks.corporeaRetainer)
				.requires(tagIngredient(Tags.Items.CHESTS_WOODEN))
				.requires(BotaniaItems.corporeaSpark)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.corporeaSpark))
				.save(output);
		ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.MISC, BotaniaItems.phantomInk, 4)
				.requires(BotaniaItems.manaPearl)
				.requires(tagIngredient(Tags.Items.DYES))
				.requires(tagIngredient(Tags.Items.GLASS_BLOCKS))
				.requires(Items.GLASS_BOTTLE, 4)
				.unlockedBy("has_item", conditionsFromItem(BotaniaItems.manaPearl))
				.save(output);

		MutableObject<BotaniaFinishedRecipe> base = new MutableObject<>();
		MutableObject<BotaniaFinishedRecipe> gog = new MutableObject<>();
		ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.MISC, BotaniaItems.fertilizer)
				.requires(Items.BONE_MEAL)
				.requires(tagIngredient(Tags.Items.DYES), 4)
				.unlockedBy("has_item", conditionsFromTag(Tags.Items.DYES))
				.save(capturing(base::setValue), "botania:fertilizer_dye");
		ShapelessRecipeBuilder.shapeless(BuiltInRegistries.ITEM, RecipeCategory.MISC, BotaniaItems.fertilizer, 3)
				.requires(Items.BONE_MEAL)
				.requires(tagIngredient(Tags.Items.DYES), 4)
				.unlockedBy("has_item", conditionsFromTag(Tags.Items.DYES))
				.save(capturing(gog::setValue), "botania:fertilizer_dye");
		saveFinishedRecipe(output, new GogAlternationResult(gog.getValue(), base.getValue()));
	}

	private static Ingredient tagIngredient(TagKey<Item> tag) {
		return vazkii.botania.neoforge.crafting.LazyTagIngredient.of(tag);
	}

	@Override
	public String getName() {
		return "Botania crafting recipes (NeoForge-specific)";
	}
}
