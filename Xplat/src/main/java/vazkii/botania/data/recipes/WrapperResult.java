/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.reflect.Proxy;
import java.util.function.UnaryOperator;

/**
 * A {@link RecipeOutput} wrapper that swaps the recipes it receives for Botania's
 * wrapped recipe types. Receives the recipes built by vanilla recipe builders and
 * delegates the transformed recipes to the parent output.
 * <p>
 * Implemented with a dynamic proxy because the NeoForge and Fabric
 * {@code RecipeOutput} interfaces differ (NeoForge adds a conditional accept
 * overload through {@code IRecipeOutputExtension}); the wrapper only ever
 * forwards the vanilla three-argument accept, and Botania's Xplat datagen
 * never emits conditional recipes.
 */
public final class WrapperResult {
	private WrapperResult() {}

	/**
	 * Wraps the output's recipes with the given recipe wrapper.
	 */
	@SuppressWarnings("unchecked") // Proxy.invocation handler args are typed Object
	public static RecipeOutput ofType(UnaryOperator<Recipe<?>> wrapper, RecipeOutput parent) {
		return (RecipeOutput) Proxy.newProxyInstance(RecipeOutput.class.getClassLoader(), new Class<?>[] { RecipeOutput.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "accept" -> {
						parent.accept((ResourceKey<Recipe<?>>) args[0], transform(wrapper, (Recipe<?>) args[1]), (AdvancementHolder) args[2]);
						yield null;
					}
					case "advancement" -> parent.advancement();
					case "includeRootAdvancement" -> {
						parent.includeRootAdvancement();
						yield null;
					}
					// Fabric's AllCraftingRecipeJsonBuildersMixin rewrites recipe keys through this hook
					// before save() builds the unlock advancement; returning null here produces
					// ResourceKey[minecraft:recipe / null] and an NPE in RecipeUnlockAdvancementBuilder.
					case "getRecipeIdentifier" -> args[0];
					default -> null;
				});
	}

	private static Recipe<?> transform(UnaryOperator<Recipe<?>> wrapper, Recipe<?> recipe) {
		return wrapper != null ? wrapper.apply(recipe) : recipe;
	}
}
