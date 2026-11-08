package vazkii.botania.xplat;

import vazkii.botania.api.BotaniaAPI;

/**
 * Optional-dependency probe for Sodium's animated-sprite API, shared by both loaders.
 *
 * <p>
 * Sodium 0.8.9 for MC 26.1 ships an official build for BOTH Fabric and NeoForge under the
 * same mod id {@code sodium} and the same {@code net.caffeinemc.mods.sodium} package. The old
 * Forge-side ports (Rubidium / Embeddium) used {@code me.jellysquid.mods.sodium} and have no
 * 26.1 release at all. Their mod ids are still probed so a third-party 26.1 port that happens
 * to expose the official API keeps working, but the API class is checked before anything
 * references a Sodium class: if only a legacy port is installed, Botania logs a warning and
 * leaves animated sprites alone instead of throwing {@link NoClassDefFoundError}.
 *
 * <p>
 * This class deliberately contains no hard reference to any Sodium type, mirroring the
 * optional-dependency isolation used for Trinkets on NeoForge.
 */
public final class SodiumPresence {
	private static final String OFFICIAL_MOD_ID = "sodium";
	private static final String[] LEGACY_MOD_IDS = { "embeddium", "rubidium" };
	private static final String API_CLASS = "net.caffeinemc.mods.sodium.api.texture.SpriteUtil";

	private SodiumPresence() {}

	public static boolean isActive() {
		boolean official = XplatAbstractions.INSTANCE.isModLoaded(OFFICIAL_MOD_ID);
		boolean legacy = false;
		for (String modId : LEGACY_MOD_IDS) {
			if (XplatAbstractions.INSTANCE.isModLoaded(modId)) {
				legacy = true;
				break;
			}
		}
		if (!official && !legacy) {
			return false;
		}
		try {
			// Do not initialize: INSTANCE resolves its implementation through Sodium's
			// DependencyInjection loader, which is only safe once Sodium is running.
			Class.forName(API_CLASS, false, SodiumPresence.class.getClassLoader());
		} catch (ClassNotFoundException | LinkageError e) {
			BotaniaAPI.LOGGER.warn(
					"A Sodium port is present (official mod={}, legacy port present={}) but {} is unavailable; "
							+ "Botania cannot mark animated sprites active for it.",
					official, legacy, API_CLASS);
			return false;
		}
		return true;
	}
}
