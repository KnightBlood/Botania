/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.crafting.Recipe;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/**
 * A {@link RecipeOutput} that replaces the recipe that is passed to it with another one. This is used
 * to register Botania's own crafting recipe types, which are variants of the vanilla shaped and
 * shapeless recipes.
 *
 * <p>
 * Implemented with a dynamic proxy because the Xplat sources are compiled against both load
 * classpaths: NeoForge's {@code RecipeOutput} inherits an extra abstract conditional
 * {@code accept} overload (with {@code ICondition} varargs) that does not exist on vanilla.
 * Invoking the {@code accept} method reflectively on the delegate sidesteps the divergent
 * signatures (3 args on vanilla, 4 on NeoForge).
 */
public final class WrappingRecipeOutput {

	private WrappingRecipeOutput() {}

	public static RecipeOutput of(RecipeOutput delegate, Function<Recipe<?>, Recipe<?>> wrapper) {
		return (RecipeOutput) Proxy.newProxyInstance(RecipeOutput.class.getClassLoader(), new Class<?>[] { RecipeOutput.class },
				(proxy, method, args) -> {
					switch (method.getName()) {
						case "accept" -> {
							Object[] callArgs = args.clone();
							callArgs[1] = wrapper.apply((Recipe<?>) callArgs[1]);
							try {
								method.invoke(delegate, callArgs);
							} catch (InvocationTargetException e) {
								throw e.getCause();
							}
							return null;
						}
						case "advancement" -> {
							return delegate.advancement();
						}
						case "includeRootAdvancement" -> {
							delegate.includeRootAdvancement();
							return null;
						}
						default -> {
							return null;
						}
					}
				});
	}
}
