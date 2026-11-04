package vazkii.botania.fabric.integration.sodium;

import net.caffeinemc.mods.sodium.api.texture.SpriteUtil;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Bridge to Sodium's official animated-sprite API (0.8.9+). The NeoForge and Fabric builds of
 * Sodium 0.8.9 ship the very same {@code net.caffeinemc.mods.sodium} classes, so both loaders
 * now use the identical integration.
 *
 * <p>Only ever called through {@link vazkii.botania.xplat.SodiumPresence#isActive()}.
 */
public final class SodiumHelper {
	private SodiumHelper() {}

	public static void markSpriteActive(TextureAtlasSprite sprite) {
		SpriteUtil.INSTANCE.markSpriteActive(sprite);
	}
}
