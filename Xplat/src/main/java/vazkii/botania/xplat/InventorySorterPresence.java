/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.xplat;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.integration.inventorysorter.InventorySorterCompat;

/**
 * Optional-dependency probe for Inventory Sorting, shared by both loaders.
 *
 * <p>On MC 26.1 two unrelated mods ship under the SAME mod id {@code inventorysorter}:
 * <ul>
 *     <li>{@code cpw/inventorysorter} (NeoForge only) still consumes the legacy
 *     {@code containerblacklist} InterModComms message; that path is handled by the
 *     NeoForge-side {@code InventorySorterIntegration} and must stay in place.</li>
 *     <li>{@code KyrptonaughtMC/Inventory-Sorter} 3.x ("Inventory Sorting", Meza) ships
 *     for BOTH Fabric and NeoForge, dropped IMC entirely, and instead builds a set of
 *     do-not-sort menu ids from {@code CompatibilityLoader}s.</li>
 * </ul>
 * Only the second one has anything for Botania to register with, and only it exposes
 * the {@code net.kyrptonaught.inventorysorter} package. Probing the classes (not just the
 * mod id) tells the two apart without ever loading a class that may be absent.
 *
 * <p>This class deliberately contains no hard reference to any Inventory Sorter type:
 * {@link InventorySorterCompat} is resolved only after the probe succeeds, mirroring the
 * isolation used for Trinkets / Sodium. Otherwise a cpw-only install could throw
 * {@link NoClassDefFoundError} while Botania is being verified.
 */
public final class InventorySorterPresence {
	private static final String MOD_ID = "inventorysorter";
	private static final String MEZA_MAIN_CLASS = "net.kyrptonaught.inventorysorter.InventorySorterMod";
	private static final String MEZA_LOADER_API = "net.kyrptonaught.inventorysorter.compat.sources.CompatibilityLoader";

	private InventorySorterPresence() {}

	/**
	 * Registers the flower bag blacklist when the Meza 3.x build is installed.
	 *
	 * @return {@code true} when the 3.x compatibility API was found and used. NeoForge uses this
	 *         to decide whether the legacy cpw IMC bridge is still needed; on Fabric the return
	 *         value is simply ignored.
	 */
	public static boolean init() {
		if (!XplatAbstractions.INSTANCE.isModLoaded(MOD_ID)) {
			return false;
		}
		try {
			ClassLoader loader = InventorySorterPresence.class.getClassLoader();
			// false: do not run the upstream static initializer (it writes config files and
			// starts the compatibility loaders) before Botania is done setting up.
			Class.forName(MEZA_MAIN_CLASS, false, loader);
			Class.forName(MEZA_LOADER_API, false, loader);
		} catch (ClassNotFoundException | LinkageError e) {
			// A cpw build (or an older/forked build without the compatibility API) is installed.
			// It has no loader list to register with; on NeoForge the IMC bridge covers it.
			BotaniaAPI.LOGGER.info("Inventory Sorter is installed, but not the Meza 3.x compatibility API; "
					+ "Botania will not register a do-not-sort loader for it.");
			return false;
		}
		InventorySorterCompat.register();
		return true;
	}
}
