/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.integration.rei;

import me.shedaniel.rei.api.common.entry.EntryIngredient;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.recipe.OrechidRecipe;

/**
 * The read side shared by Botania's REI categories.
 *
 * <p>
 * Two kinds of display implement this view: the original client-side displays built from a
 * real {@code RecipeHolder}, and {@link BotaniaSyncedDisplay} deserialised from REI's
 * {@code DisplaySyncPacket}. Categories only ever talk to this interface, so both sources can
 * coexist in the same category without the client-side classes leaking into the dedicated
 * server classpath.
 */
public interface BotaniaREIView {
	default int getManaCost() {
		return 0;
	}

	@Nullable
	default EntryIngredient getContainers() {
		return null;
	}

	@Nullable
	default EntryIngredient getReagent() {
		return null;
	}

	@Nullable
	default EntryIngredient getCatalystEntries() {
		return null;
	}

	/**
	 * The real recipe object, available only for displays built locally on the client.
	 * Synced displays return {@code null} because a dedicated-server client has no
	 * {@code RecipeHolder} list; see {@link #getSyncedChance()}.
	 */
	@Nullable
	default OrechidRecipe getRecipe() {
		return null;
	}

	/**
	 * Generic (position-independent) Orechid chance calculated on the server. Only synced
	 * displays carry it; local displays compute the chance from {@link #getRecipe()} so the
	 * biome-sensitive tooltip stays exactly as it was in 1.20.1.
	 */
	@Nullable
	default Double getSyncedChance() {
		return null;
	}
}
