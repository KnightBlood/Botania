/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.integration.inventorysorter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import net.kyrptonaught.inventorysorter.InventorySorterMod;
import net.kyrptonaught.inventorysorter.compat.sources.CompatibilityLoader;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.BotaniaItems;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Keeps the flower bag out of Inventory Sorting 3.x's sortable screens, on both loaders.
 *
 * <p>Botania issue 4068: the flower pouch has one slot per mystical flower colour, so sorting
 * it destroys the colour→slot mapping. The 1.20.1 port blacklisted the menu through the cpw
 * build's {@code containerblacklist} InterModComms message (still handled by the NeoForge-only
 * {@code InventorySorterIntegration}). Meza / KyrptonaughtMC's 3.x rewrite dropped IMC and
 * instead builds two {@code Set<Identifier>}s from {@link CompatibilityLoader}s, keyed by
 * {@code BuiltInRegistries.MENU.getKey(menu.getType())}. That is exactly the id the old IMC
 * message carried, so this is a like-for-like replacement rather than a new behaviour.
 *
 * <p>Both sets are populated: {@code getPreventSort()} is the functional blacklist, while
 * {@code getShouldHideSortButtons()} also removes the buttons so the player is not offered an
 * action that would be refused.
 *
 * <p>3.0.0 and 3.0.1 expose the same {@code Compatibility} / {@code CompatibilityLoader}
 * signatures on Fabric and NeoForge (checked with javap on the 3.0.0-fabric, 3.0.1-fabric
 * and 3.0.1-neoforge jars), so one shared implementation covers the 3.0.x line.
 * This class hard-references Inventory Sorting types and must only be loaded after
 * {@code InventorySorterPresence} has confirmed they exist.
 */
public final class InventorySorterCompat {
	private InventorySorterCompat() {}

	public static void register() {
		Identifier flowerBag = BuiltInRegistries.MENU.getKey(BotaniaItems.FLOWER_BAG_CONTAINER);
		if (flowerBag == null) {
			// Explicit, not a silent downgrade: this also catches calling register() before
			// Botania's menu types have been registered.
			BotaniaAPI.LOGGER.warn("Inventory Sorting is present but the flower bag menu type has no registry key; "
					+ "Botania cannot put it on the do-not-sort list.");
			return;
		}

		// InventorySorterMod's static initializer builds Compatibility and immediately calls
		// load(), so a loader appended afterwards is never read until a reload happens.
		// reload() is the same public path InventorySorterMod.reloadConfig() uses; it re-runs
		// every loader (including the remote list) asynchronously and keeps our loader in the
		// list for any later reload.
		InventorySorterMod.compatibility.addLoader(new FlowerBagLoader(flowerBag));
		InventorySorterMod.compatibility.reload();
		BotaniaAPI.LOGGER.info("Inventory Sorting: registered do-not-sort loader for {}", flowerBag);
	}

	private static final class FlowerBagLoader implements CompatibilityLoader {
		private final Set<Identifier> ids;
		private final AtomicBoolean consumed = new AtomicBoolean();

		private FlowerBagLoader(Identifier id) {
			this.ids = Set.of(id);
		}

		@Override
		public Set<Identifier> getPreventSort() {
			logConsumed();
			return ids;
		}

		@Override
		public Set<Identifier> getShouldHideSortButtons() {
			return ids;
		}

		private void logConsumed() {
			// Compatibility.load() calls each loader on its own thread and swallows exceptions,
			// so this is the positive runtime confirmation that the blacklist was picked up.
			if (consumed.compareAndSet(false, true)) {
				BotaniaAPI.LOGGER.info("Inventory Sorting: Botania compatibility loader consumed {}", ids);
			}
		}
	}
}
