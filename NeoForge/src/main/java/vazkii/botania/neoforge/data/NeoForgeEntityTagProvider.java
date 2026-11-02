package vazkii.botania.neoforge.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.entity.BotaniaEntities;

import java.util.concurrent.CompletableFuture;

public class NeoForgeEntityTagProvider extends IntrinsicHolderTagsProvider<EntityType<?>> {
	public NeoForgeEntityTagProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
		super(packOutput, Registries.ENTITY_TYPE, lookupProvider,
				(entityType) -> entityType.builtInRegistryHolder().key(), BotaniaAPI.MODID);
	}

	@Override
	protected void addTags(@NotNull HolderLookup.Provider provider) {
		tag(forge("bosses")).add(BotaniaEntities.DOPPLEGANGER);
	}

	private static TagKey<EntityType<?>> forge(String name) {
		return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("forge", name));
	}
}
