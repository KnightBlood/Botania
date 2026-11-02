package vazkii.botania.neoforge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.Tags;

import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.lib.LibMisc;

import java.util.concurrent.CompletableFuture;

public class NeoForgeBiomeTagProvider extends KeyTagProvider<Biome> {
	protected NeoForgeBiomeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> holderProvider) {
		super(output, Registries.BIOME, holderProvider, LibMisc.MOD_ID);
	}

	@Override
	protected void addTags(HolderLookup.Provider provider) {
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_DESERT_BONUS).addTag(Tags.Biomes.IS_DESERT).addTag(BiomeTags.IS_SAVANNA);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_FOREST_BONUS).addTag(BiomeTags.IS_FOREST);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_FUNGAL_BONUS).addTag(Tags.Biomes.IS_MUSHROOM).addTag(Tags.Biomes.IS_UNDERGROUND);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_MESA_BONUS).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_SAVANNA);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_MOUNTAIN_BONUS).addTag(BiomeTags.IS_MOUNTAIN);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_PLAINS_BONUS).addTag(Tags.Biomes.IS_PLAINS).addTag(BiomeTags.IS_BEACH);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_SWAMP_BONUS).addTag(Tags.Biomes.IS_SWAMP).addTag(BiomeTags.IS_JUNGLE);
		tag(BotaniaTags.Biomes.MARIMORPHOSIS_TAIGA_BONUS).addTag(Tags.Biomes.IS_CONIFEROUS_TREE).addTag(Tags.Biomes.IS_COLD).addTag(Tags.Biomes.IS_SNOWY);
	}
}
