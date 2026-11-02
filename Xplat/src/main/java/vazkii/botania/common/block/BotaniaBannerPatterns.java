/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BannerPattern;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * 26.1: banner patterns are a fully data-driven registry
 * ({@code data/<ns>/banner_pattern/<name>.json}, see vanilla's {@link BannerPattern}).
 * The pattern instances themselves now come from JSON on both platforms; this class only
 * carries the {@link ResourceKey}s used by tags and datagen, and bootstraps the JSON
 * contents during datagen.
 */
public final class BotaniaBannerPatterns {
	public static final ResourceKey<BannerPattern> FLOWER = make("flower");
	public static final ResourceKey<BannerPattern> LEXICON = make("lexicon");
	public static final ResourceKey<BannerPattern> LOGO = make("logo");
	public static final ResourceKey<BannerPattern> SAPLING = make("sapling");
	public static final ResourceKey<BannerPattern> TINY_POTATO = make("tiny_potato");
	public static final ResourceKey<BannerPattern> SPARK_DISPERSIVE = make("spark_dispersive");
	public static final ResourceKey<BannerPattern> SPARK_DOMINANT = make("spark_dominant");
	public static final ResourceKey<BannerPattern> SPARK_RECESSIVE = make("spark_recessive");
	public static final ResourceKey<BannerPattern> SPARK_ISOLATED = make("spark_isolated");

	public static final ResourceKey<BannerPattern> FISH = make("fish");
	public static final ResourceKey<BannerPattern> AXE = make("axe");
	public static final ResourceKey<BannerPattern> HOE = make("hoe");
	public static final ResourceKey<BannerPattern> PICKAXE = make("pickaxe");
	public static final ResourceKey<BannerPattern> SHOVEL = make("shovel");
	public static final ResourceKey<BannerPattern> SWORD = make("sword");

	public static void bootstrap(BootstrapContext<BannerPattern> ctx) {
		register(ctx, FLOWER);
		register(ctx, LEXICON);
		register(ctx, LOGO);
		register(ctx, SAPLING);
		register(ctx, TINY_POTATO);
		register(ctx, SPARK_DISPERSIVE);
		register(ctx, SPARK_DOMINANT);
		register(ctx, SPARK_RECESSIVE);
		register(ctx, SPARK_ISOLATED);
		register(ctx, FISH);
		register(ctx, AXE);
		register(ctx, HOE);
		register(ctx, PICKAXE);
		register(ctx, SHOVEL);
		register(ctx, SWORD);
	}

	private static void register(BootstrapContext<BannerPattern> ctx, ResourceKey<BannerPattern> key) {
		// vanilla-style translation key: the banner name appends "." + dye color at lookup time
		ctx.register(key, new BannerPattern(key.identifier(), "block.minecraft.banner." + key.identifier().getNamespace() + "." + key.identifier().getPath()));
	}

	private static ResourceKey<BannerPattern> make(String hashName) {
		return ResourceKey.create(Registries.BANNER_PATTERN, prefix(hashName));
	}
}
