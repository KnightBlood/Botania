/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.forge.crafting;

import net.minecraft.resources.Identifier;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import vazkii.botania.common.lib.LibMisc;

import java.util.function.Supplier;

/**
 * Holds the {@link IngredientType}s for Botania's NeoForge-only custom ingredients.
 * See {@link LazyTagIngredient} for why the tag ingredient has to be custom on 26.1.
 */
public final class ForgeBotaniaIngredients {
	public static final DeferredRegister<IngredientType<?>> INGREDIENT_TYPES = DeferredRegister
			.create(NeoForgeRegistries.Keys.INGREDIENT_TYPES, LibMisc.MOD_ID);

	public static final Supplier<IngredientType<LazyTagIngredient>> ITEM_TAG = INGREDIENT_TYPES
			.register("tag", () -> new IngredientType<>(LazyTagIngredient.CODEC));

	public static void init(IEventBus modBus) {
		INGREDIENT_TYPES.register(modBus);
	}
}
