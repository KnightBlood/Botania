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
import com.mojang.serialization.JsonOps;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.ResourceLocationHelper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StonecuttingProvider extends BotaniaRecipeProvider {
	public StonecuttingProvider(PackOutput packOutput) {
		super(packOutput);
	}

	@Override
	public void buildRecipes(RecipeOutput output) {
		for (String variant : LibBlockNames.METAMORPHIC_VARIANTS) {
			registerForMetamorphic(variant, output);
		}

		for (String color : LibBlockNames.PAVEMENT_VARIANTS) {
			registerForPavement(color, output);
		}

		for (String variant : LibBlockNames.QUARTZ_VARIANTS) {
			registerForQuartz(variant, output);
		}

		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockPolished));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockPolishedSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockPolishedStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockPolishedWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockBrick));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockBrickSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockBrickStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockBrickWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockBrickChiseled));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrock, BotaniaBlocks.livingrockSlate));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockPolishedSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockPolishedStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockPolishedWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockBrick));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockBrickSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockBrickStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockBrickWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockPolished, BotaniaBlocks.livingrockBrickChiseled));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrick, BotaniaBlocks.livingrockBrickSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrick, BotaniaBlocks.livingrockBrickStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrick, BotaniaBlocks.livingrockBrickWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrick, BotaniaBlocks.livingrockBrickChiseled));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrickMossy, BotaniaBlocks.livingrockBrickMossySlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrickMossy, BotaniaBlocks.livingrockBrickMossyStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.livingrockBrickMossy, BotaniaBlocks.livingrockBrickMossyWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.shimmerrock, BotaniaBlocks.shimmerrockSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.shimmerrock, BotaniaBlocks.shimmerrockStairs));

		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaBrick));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaBrickSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaBrickStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBlock, BotaniaBlocks.corporeaBrickWall));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBrick, BotaniaBlocks.corporeaBrickSlab, 2));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBrick, BotaniaBlocks.corporeaBrickStairs));
		saveFinishedRecipe(output, stonecutting(BotaniaBlocks.corporeaBrick, BotaniaBlocks.corporeaBrickWall));

		List<Item> allAzulejos = IntStream.range(0, 16).mapToObj(i -> "azulejo_" + i)
				.map(ResourceLocationHelper::prefix)
				.map(BuiltInRegistries.ITEM::getValue)
				.collect(Collectors.toList());
		for (Item azulejo : allAzulejos) {
			saveFinishedRecipe(output, anyToAnyStonecutting(allAzulejos, azulejo));
		}
	}

	private void registerForQuartz(String variant, RecipeOutput output) {
		Block base = BuiltInRegistries.BLOCK.getValue(prefix(variant));
		Block slab = BuiltInRegistries.BLOCK.getValue(prefix(variant + LibBlockNames.SLAB_SUFFIX));
		Block stairs = BuiltInRegistries.BLOCK.getValue(prefix(variant + LibBlockNames.STAIR_SUFFIX));
		Block chiseled = BuiltInRegistries.BLOCK.getValue(prefix("chiseled_" + variant));
		Block pillar = BuiltInRegistries.BLOCK.getValue(prefix(variant + "_pillar"));
		saveFinishedRecipe(output, stonecutting(base, slab, 2));
		saveFinishedRecipe(output, stonecutting(base, stairs));
		saveFinishedRecipe(output, stonecutting(base, chiseled));
		saveFinishedRecipe(output, stonecutting(base, pillar));
	}

	private void registerForPavement(String color, RecipeOutput output) {
		Block base = BuiltInRegistries.BLOCK.getValue(prefix(color + LibBlockNames.PAVEMENT_SUFFIX));
		Block slab = BuiltInRegistries.BLOCK.getValue(prefix(color + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.SLAB_SUFFIX));
		Block stair = BuiltInRegistries.BLOCK.getValue(prefix(color + LibBlockNames.PAVEMENT_SUFFIX + LibBlockNames.STAIR_SUFFIX));
		saveFinishedRecipe(output, stonecutting(base, slab, 2));
		saveFinishedRecipe(output, stonecutting(base, stair));
	}

	private void registerForMetamorphic(String variant, RecipeOutput output) {
		Block base = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone"));
		Block slab = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.SLAB_SUFFIX));
		Block stair = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.STAIR_SUFFIX));
		Block wall = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_stone" + LibBlockNames.WALL_SUFFIX));
		Block brick = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks"));
		Block brickSlab = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.SLAB_SUFFIX));
		Block brickStair = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.STAIR_SUFFIX));
		Block brickWall = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks" + LibBlockNames.WALL_SUFFIX));
		Block chiseledBrick = BuiltInRegistries.BLOCK.getValue(prefix("chiseled_" + LibBlockNames.METAMORPHIC_PREFIX + variant + "_bricks"));
		Block cobble = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone"));
		Block cobbleSlab = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.SLAB_SUFFIX));
		Block cobbleStair = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.STAIR_SUFFIX));
		Block cobbleWall = BuiltInRegistries.BLOCK.getValue(prefix(LibBlockNames.METAMORPHIC_PREFIX + variant + "_cobblestone" + LibBlockNames.WALL_SUFFIX));

		saveFinishedRecipe(output, stonecutting(base, slab, 2));
		saveFinishedRecipe(output, stonecutting(base, stair));
		saveFinishedRecipe(output, stonecutting(base, wall));
		saveFinishedRecipe(output, stonecutting(base, brick));
		saveFinishedRecipe(output, stonecutting(base, brickSlab, 2));
		saveFinishedRecipe(output, stonecutting(base, brickStair));
		saveFinishedRecipe(output, stonecutting(base, brickWall));
		saveFinishedRecipe(output, stonecutting(base, chiseledBrick));

		saveFinishedRecipe(output, stonecutting(brick, brickSlab, 2));
		saveFinishedRecipe(output, stonecutting(brick, brickStair));
		saveFinishedRecipe(output, stonecutting(brick, brickWall));
		saveFinishedRecipe(output, stonecutting(brick, chiseledBrick));

		saveFinishedRecipe(output, stonecutting(cobble, cobbleSlab, 2));
		saveFinishedRecipe(output, stonecutting(cobble, cobbleStair));
		saveFinishedRecipe(output, stonecutting(cobble, cobbleWall));
	}

	@NotNull
	@Override
	public String getName() {
		return "Botania stonecutting recipes";
	}

	protected Identifier idFor(ItemLike a, ItemLike b) {
		Identifier aId = BuiltInRegistries.ITEM.getKey(a.asItem());
		Identifier bId = BuiltInRegistries.ITEM.getKey(b.asItem());
		return prefix("stonecutting/" + aId.getPath() + "_to_" + bId.getPath());
	}

	protected BotaniaFinishedRecipe stonecutting(ItemLike input, ItemLike output) {
		return stonecutting(input, output, 1);
	}

	protected BotaniaFinishedRecipe stonecutting(ItemLike input, ItemLike output, int count) {
		return new Result(idFor(input, output), StonecutterRecipe.SERIALIZER, Ingredient.of(input), output.asItem(), count);
	}

	protected BotaniaFinishedRecipe anyToAnyStonecutting(List<? extends ItemLike> inputs, ItemLike output) {
		Ingredient input = Ingredient.of(inputs.stream().filter(obj -> output != obj).toArray(ItemLike[]::new));
		return new Result(prefix("stonecutting/" + BuiltInRegistries.ITEM.getKey(output.asItem()).getPath()), StonecutterRecipe.SERIALIZER, input, output.asItem(), 1);
	}

	protected Identifier prefix(String path) {
		return ResourceLocationHelper.prefix(path);
	}

	// Wrapper without advancements; SingleItemRecipeBuilder.Result was removed in MC 26.1
	public static class Result implements BotaniaFinishedRecipe {
		private final Identifier id;
		private final RecipeSerializer<?> serializer;
		private final Ingredient input;
		private final Item result;
		private final int count;

		public Result(Identifier id, RecipeSerializer<?> serializer, Ingredient input, Item result, int count) {
			this.id = id;
			this.serializer = serializer;
			this.input = input;
			this.result = result;
			this.count = count;
		}

		@Override
		public void serializeRecipeData(JsonObject json) {
			// Registry-aware ops so tag-based ingredients encode as {"tag": ...} refs.
			var ops = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, BOTANIA_REGISTRY_ACCESS);
			json.add("ingredient", Ingredient.CODEC.encodeStart(ops, input).getOrThrow());
			json.add("result", ItemStackTemplate.CODEC.encodeStart(ops, new ItemStackTemplate(result, count)).getOrThrow());
		}

		@Override
		public Identifier getId() {
			return id;
		}

		@Override
		public RecipeSerializer<?> getType() {
			return serializer;
		}
	}
}
