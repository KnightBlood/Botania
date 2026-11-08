/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;

import vazkii.botania.data.PublicAddTags;
import vazkii.botania.fabric.mixin.TagsProviderFabricAccessor;

import java.util.concurrent.CompletableFuture;

/**
 * 26.1: fabric's tag alias mixin wraps TagsProvider.run and unconditionally casts the provider
 * to FabricTagsProvider, so Xplat tag providers (vanilla subclasses) can no longer be registered
 * directly. This bridge extends FabricTagsProvider, runs the Xplat provider's content building,
 * and copies the resulting builders over before the Fabric provider writes them out.
 */
public class FabricXplatTagBridge<T> extends FabricTagsProvider<T> {
	private final TagsProvider<T> delegate;

	public FabricXplatTagBridge(FabricPackOutput output, ResourceKey<? extends Registry<T>> registryKey,
			CompletableFuture<HolderLookup.Provider> lookupProvider, TagsProvider<T> delegate) {
		super(output, registryKey, lookupProvider);
		this.delegate = delegate;
	}

	public TagsProvider<T> getDelegate() {
		return this.delegate;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void addTags(HolderLookup.Provider provider) {
		// The Xplat providers widen addTags to public and implement PublicAddTags,
		// so a cast to that interface lets us invoke it across packages.
		((PublicAddTags<T>) (Object) this.delegate).addTags(provider);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
		// vanilla clears builders and calls addTags inside createContentsProvider; copy the
		// delegate's builders over once both that and our addTags delegation are done.
		return super.createContentsProvider().thenApply(lookup -> {
			((TagsProviderFabricAccessor<T>) (Object) this).botania$getBuilders()
					.putAll(((TagsProviderFabricAccessor<T>) (Object) this.delegate).botania$getBuilders());
			return lookup;
		});
	}
}
