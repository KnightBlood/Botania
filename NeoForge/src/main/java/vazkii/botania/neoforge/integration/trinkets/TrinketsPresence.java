/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.integration.trinkets;

import vazkii.botania.xplat.XplatAbstractions;

/**
 * Trinkets presence probe. MUST NOT reference any Trinkets type: Trinkets is an optional
 * dependency, and loading {@link NeoTrinketsIntegration} (whose static callback wrapper needs
 * {@code eu.pb4.trinkets.api.callback.TrinketCallback}) while the mod is absent crashes mod
 * construction with a NoClassDefFoundError. All guards go through this class; the integration
 * class itself is only touched after this probe returns true.
 */
public final class TrinketsPresence {
	// The universal 4.x mod id is "trinkets_updated". On Fabric the jar also declares a
	// "trinkets" alias through fabric.mod.json 'provides'; keep checking both so a future
	// upload that restores the legacy id keeps working.
	private static final String MOD_ID = "trinkets_updated";
	private static final String LEGACY_MOD_ID = "trinkets";

	private TrinketsPresence() {}

	public static boolean isLoaded() {
		return XplatAbstractions.INSTANCE.isModLoaded(MOD_ID)
				|| XplatAbstractions.INSTANCE.isModLoaded(LEGACY_MOD_ID);
	}
}
