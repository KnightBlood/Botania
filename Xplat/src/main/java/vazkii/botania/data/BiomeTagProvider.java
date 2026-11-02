package vazkii.botania.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;

import vazkii.botania.common.lib.BotaniaTags;

import java.util.concurrent.CompletableFuture;

public class BiomeTagProvider extends KeyTagProvider<Biome> implements PublicAddTags<Biome> {
	public BiomeTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Registries.BIOME, lookupProvider);
	}

	@Override
	public void addTags(HolderLookup.Provider provider) {
		// need to do this so we can use them in addTag. It generates a dummy empty file,
		// but whatever.
		tag(BiomeTags.IS_OVERWORLD);
		tag(BiomeTags.IS_NETHER);

		tag(BotaniaTags.Biomes.MYSTICAL_FLOWER_SPAWNLIST).addTag(BiomeTags.IS_OVERWORLD);
		tag(BotaniaTags.Biomes.MYSTICAL_FLOWER_BLOCKLIST);
		this.getOrCreateRawBuilder(BotaniaTags.Biomes.MYSTICAL_FLOWER_BLOCKLIST)
				.addOptionalTag(Identifier.fromNamespaceAndPath("forge", "is_mushroom"))
				.addOptionalTag(Identifier.fromNamespaceAndPath("c", "mushroom"));

		tag(BotaniaTags.Biomes.MYSTICAL_MUSHROOM_SPAWNLIST).addTag(BiomeTags.IS_OVERWORLD)
				.addTag(BiomeTags.IS_NETHER);
		tag(BotaniaTags.Biomes.MYSTICAL_MUSHROOM_BLOCKLIST);
	}
}
