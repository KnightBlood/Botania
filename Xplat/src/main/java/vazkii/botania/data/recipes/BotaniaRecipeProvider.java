/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.CachedOutput;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class BotaniaRecipeProvider implements DataProvider {
	static final RegistryAccess.Frozen BOTANIA_REGISTRY_ACCESS =
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

	private final PackOutput.PathProvider recipePathProvider;
	private final PackOutput.PathProvider advancementPathProvider;

	// The NeoForge and Fabric RecipeOutput interfaces differ (NeoForge adds a
	// conditional accept overload); Xplat sources are compiled against both, so the
	// dynamic RecipeOutput implementations here use proxies to stay platform-neutral.
	private static final ThreadLocal<BotaniaRecipeCollector> CURRENT_COLLECTOR = new ThreadLocal<>();

	public BotaniaRecipeProvider(PackOutput packOutput) {
		this.recipePathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, Registries.RECIPE.identifier().getPath());
		this.advancementPathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, Registries.ADVANCEMENT.identifier().getPath());
	}

	// [VanillaCopy] RecipeProvider
	@Override
	public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
		Set<Identifier> checkDuplicates = Sets.newHashSet();
		List<CompletableFuture<?>> futures = new ArrayList<>();
		BotaniaRecipeCollector collector = new BotaniaRecipeCollector(checkDuplicates, futures, cache, recipePathProvider, advancementPathProvider);
		CURRENT_COLLECTOR.set(collector);
		try {
			buildRecipes(collector.asOutput());
		} finally {
			CURRENT_COLLECTOR.remove();
		}
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	protected abstract void buildRecipes(RecipeOutput output);

	/**
	 * Save a Botania custom finished recipe through the RecipeOutput.
	 * This bridges the old FinishedRecipe pattern to the MC 26.1 RecipeOutput.
	 */
	protected static void saveFinishedRecipe(RecipeOutput output, BotaniaFinishedRecipe finished) {
		JsonObject recipeJson = finished.serializeRecipe();

		Identifier id = finished.getId();
		var collector = CURRENT_COLLECTOR.get();
		if (collector != null) {
			collector.acceptFinished(id, recipeJson, finished.serializeAdvancement());
		}
	}

	/**
	 * A RecipeOutput that captures built vanilla recipes into a callback, wrapped as
	 * {@link BotaniaFinishedRecipe} — the 26.1 replacement for the old 1.20.1
	 * {@code builder.save(Consumer<FinishedRecipe>, id)} MutableObject pattern
	 * (used for GOG alternation sub-recipes).
	 */
	@SuppressWarnings("unchecked") // Proxy.invocation handler args are typed Object
	protected static RecipeOutput capturing(Consumer<BotaniaFinishedRecipe> sink) {
		return (RecipeOutput) Proxy.newProxyInstance(RecipeOutput.class.getClassLoader(), new Class<?>[] {RecipeOutput.class},
				(proxy, method, args) -> switch (method.getName()) {
					case "accept" -> {
						sink.accept(new VanillaRecipeWrapper(((ResourceKey<Recipe<?>>) args[0]).identifier(), (Recipe<?>) args[1]));
						yield null;
					}
					case "advancement" -> Advancement.Builder.advancement();
					// Fabric's AllCraftingRecipeJsonBuildersMixin rewrites recipe keys through this hook;
					// returning null here produced ResourceKey[minecraft:recipe / null] and a datagen NPE.
					case "getRecipeIdentifier" -> args[0];
					default -> null;
				});
	}

	/** Wraps a vanilla {@link Recipe} for GOG alternation serialization. */
	private record VanillaRecipeWrapper(Identifier id, Recipe<?> recipe) implements BotaniaFinishedRecipe {
		@Override
		public void serializeRecipeData(JsonObject json) {}

		@Override
		public net.minecraft.world.item.crafting.RecipeSerializer<?> getType() {
			return recipe.getSerializer();
		}

		@Override
		public Identifier getId() {
			return id;
		}

		@Override
		public JsonObject serializeRecipe() {
			// Registry-aware ops so tag-based ingredients encode as {"tag": ...} refs.
			var ops = net.minecraft.resources.RegistryOps.create(JsonOps.INSTANCE, BOTANIA_REGISTRY_ACCESS);
			return Recipe.CODEC.encodeStart(ops, recipe).getOrThrow().getAsJsonObject();
		}
	}

	/**
	 * Internal recipe collector that saves JSON directly. Does not implement
	 * {@link RecipeOutput} directly (see the platform note above); expose a
	 * dynamic proxy through {@link #asOutput()}.
	 */
	private static class BotaniaRecipeCollector {
		private final Set<Identifier> duplicates;
		private final List<CompletableFuture<?>> futures;
		private final CachedOutput cache;
		private final PackOutput.PathProvider recipePathProvider;
		private final PackOutput.PathProvider advancementPathProvider;

		BotaniaRecipeCollector(Set<Identifier> duplicates, List<CompletableFuture<?>> futures, CachedOutput cache,
				PackOutput.PathProvider recipePathProvider, PackOutput.PathProvider advancementPathProvider) {
			this.duplicates = duplicates;
			this.futures = futures;
			this.cache = cache;
			this.recipePathProvider = recipePathProvider;
			this.advancementPathProvider = advancementPathProvider;
		}

		@SuppressWarnings("unchecked") // Proxy.invocation handler args are typed Object
		RecipeOutput asOutput() {
			return (RecipeOutput) Proxy.newProxyInstance(RecipeOutput.class.getClassLoader(), new Class<?>[] {RecipeOutput.class},
					(proxy, method, args) -> switch (method.getName()) {
						case "accept" -> {
							accept((ResourceKey<Recipe<?>>) args[0], (Recipe<?>) args[1], (AdvancementHolder) args[2]);
							yield null;
						}
						case "advancement" -> Advancement.Builder.advancement();
						// See the note in capturing(): fabric datagen rewrites keys through this hook.
						case "getRecipeIdentifier" -> args[0];
						default -> null;
					});
		}

		private void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @org.jspecify.annotations.Nullable AdvancementHolder advancement) {
			if (!duplicates.add(id.identifier())) {
				throw new IllegalStateException("Duplicate recipe " + id.identifier());
			}
			// Registry-aware ops are required so tag-based ingredients encode as
			// {"tag": ...} refs instead of dereferencing their contents.
			futures.add(DataProvider.saveStable(cache, BOTANIA_REGISTRY_ACCESS, Recipe.CODEC, recipe,
					recipePathProvider.json(id.identifier())));
			if (advancement != null) {
				// Recipe-unlock advancements are normally written by vanilla's RecipeProvider output;
				// this collector replaced it, so it has to persist them as well.
				futures.add(DataProvider.saveStable(cache, BOTANIA_REGISTRY_ACCESS, Advancement.CODEC,
						advancement.value(), advancementPathProvider.json(advancement.id())));
			}
		}

		void acceptFinished(Identifier id, JsonObject recipeJson, @org.jspecify.annotations.Nullable JsonObject advancementJson) {
			if (!duplicates.add(id)) {
				throw new IllegalStateException("Duplicate recipe " + id);
			}
			futures.add(DataProvider.saveStable(cache, recipeJson, recipePathProvider.json(id)));
			if (advancementJson != null) {
				futures.add(DataProvider.saveStable(cache, advancementJson, advancementPathProvider.json(id)));
			}
		}
	}
}
