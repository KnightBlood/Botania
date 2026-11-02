/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;

import vazkii.botania.common.lib.LibMisc;

public class BotaniaStats {
	// MC 26.1: Stats.makeCustomStat(String, ...) prepends the minecraft namespace itself,
	// so we register the Identifier-keyed custom stat directly to keep the botania namespace.
	private static Identifier registerCustom(String path, StatFormatter formatter) {
		Identifier id = Identifier.fromNamespaceAndPath(LibMisc.MOD_ID, path);
		Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
		return Stats.CUSTOM.get(id, formatter).getValue();
	}

	public static final Identifier CORPOREA_ITEMS_REQUESTED =
			registerCustom("corporea_items_requested", StatFormatter.DEFAULT);

	public static final Identifier LUMINIZER_ONE_CM =
			registerCustom("luminizer_one_cm", StatFormatter.DISTANCE);

	public static final Identifier TINY_POTATOES_PETTED =
			registerCustom("tiny_potatoes_petted", StatFormatter.DEFAULT);

	public static void init() {

	}
}
