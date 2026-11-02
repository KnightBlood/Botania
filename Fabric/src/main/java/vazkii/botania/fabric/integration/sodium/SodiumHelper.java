package vazkii.botania.fabric.integration.sodium;

import net.caffeinemc.mods.sodium.client.render.texture.SpriteUtil;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SodiumHelper {

	// 26.1: Sodium's SpriteUtil is deprecated (marked for removal); keep using it until
	// Sodium lands its replacement, but silence the removal warning for -Werror builds.
	@SuppressWarnings("removal")
	public static void markSpriteActive(TextureAtlasSprite sprite) {
		// this should work with both the actual Sodium and the Fabric backport of its Forge port Embeddium
		SpriteUtil.markSpriteActive(sprite);
	}
}
