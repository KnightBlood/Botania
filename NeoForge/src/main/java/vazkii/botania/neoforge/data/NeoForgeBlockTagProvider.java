package vazkii.botania.neoforge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeoForgeBlockTagProvider extends vazkii.botania.data.BlockTagProvider {
	public static final TagKey<Block> MUSHROOMS = forge("mushrooms");
	public static final TagKey<Block> ELEMENTIUM = forge("storage_blocks/elementium");
	public static final TagKey<Block> MANASTEEL = forge("storage_blocks/manasteel");
	public static final TagKey<Block> TERRASTEEL = forge("storage_blocks/terrasteel");
	public static final TagKey<Block> MANA_DIAMOND = forge("storage_blocks/mana_diamond");
	public static final TagKey<Block> DRAGONSTONE = forge("storage_blocks/dragonstone");
	public static final TagKey<Block> BLAZE_MESH = forge("storage_blocks/blaze_mesh");
	public static final Map<DyeColor, TagKey<Block>> PETAL_BLOCKS = ColorHelper.supportedColors().collect(
			Collectors.toMap(Function.identity(), color -> forge("storage_blocks/"
					+ BuiltInRegistries.BLOCK.getKey(BotaniaBlocks.getPetalBlock(color)).getPath())));

	public static final TagKey<Block> STORAGE_QUARTZ = TagKey.create(Registries.BLOCK,
			Identifier.fromNamespaceAndPath("c", "storage_blocks/quartz"));

	/**
	 * Extends the Xplat provider so that the botania:* block tags and the NeoForge-specific
	 * cross-namespace compat tags live in the same builders map. 26.1 datagen validates
	 * same-modId tag references against this run's builders + parent lookup only (no
	 * ExistingFileHelper), and references from a different namespace than the provider's
	 * modId are skipped entirely - so the shared map keeps everything resolvable.
	 */
	public NeoForgeBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
		super(output, provider);
	}

	@Override
	public String getName() {
		return "Botania block tags (NeoForge-specific)";
	}

	@Override
	public void addTags(HolderLookup.Provider provider) {
		super.addTags(provider);

		tag(STORAGE_QUARTZ).add(
				BotaniaBlocks.darkQuartz, BotaniaBlocks.manaQuartz, BotaniaBlocks.blazeQuartz,
				BotaniaBlocks.lavenderQuartz, BotaniaBlocks.redQuartz, BotaniaBlocks.elfQuartz, BotaniaBlocks.sunnyQuartz
		);

		TagAppender<Block, Block> storageBlocks = tag(Tags.Blocks.STORAGE_BLOCKS);
		ColorHelper.supportedColors().forEach(color -> {
			tag(MUSHROOMS).add(BotaniaBlocks.getMushroom(color));
			TagKey<Block> petalStorageBlockTag = PETAL_BLOCKS.get(color);
			tag(petalStorageBlockTag).add(BotaniaBlocks.getPetalBlock(color));
			storageBlocks.addTag(petalStorageBlockTag);
		});

		// Removed buzzier_bees:flower_blacklist compat tag (MC 26.1): vanilla datagen no longer
		// consults ExistingFileHelper for tag validation, so this cross-namespace reference to
		// #botania:mystical_flowers/#botania:special_flowers can't be resolved in this run.
		// buzzier_bees is not a dependency (MVP: no external mod deps), so the tag is dropped
		// rather than duplicated here.

		tag(ELEMENTIUM).addTag(BotaniaTags.Blocks.BLOCKS_ELEMENTIUM);
		tag(MANASTEEL).addTag(BotaniaTags.Blocks.BLOCKS_MANASTEEL);
		tag(TERRASTEEL).addTag(BotaniaTags.Blocks.BLOCKS_TERRASTEEL);
		tag(MANA_DIAMOND).add(BotaniaBlocks.manaDiamondBlock);
		tag(DRAGONSTONE).add(BotaniaBlocks.dragonstoneBlock);
		tag(BLAZE_MESH).add(BotaniaBlocks.blazeBlock);
		storageBlocks
				.addTag(ELEMENTIUM)
				.addTag(MANASTEEL)
				.addTag(TERRASTEEL)
				.addTag(MANA_DIAMOND)
				.addTag(DRAGONSTONE)
				.addTag(BLAZE_MESH);
		tag(Tags.Blocks.GLASS_BLOCKS).add(BotaniaBlocks.manaGlass, BotaniaBlocks.elfGlass, BotaniaBlocks.bifrostPerm);
		tag(Tags.Blocks.GLASS_PANES).add(BotaniaBlocks.managlassPane, BotaniaBlocks.alfglassPane, BotaniaBlocks.bifrostPane);
		tag(Tags.Blocks.FENCES_WOODEN).add(BotaniaBlocks.livingwoodFence, BotaniaBlocks.dreamwoodFence);
		tag(Tags.Blocks.FENCE_GATES_WOODEN).add(BotaniaBlocks.livingwoodFenceGate, BotaniaBlocks.dreamwoodFenceGate);
	}

	private static TagKey<Block> forge(String name) {
		return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("forge", name));
	}
}
