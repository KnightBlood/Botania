/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.XplatAbstractions;

import java.io.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.List;

import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonObject;

/**
 * Cloth Config screen for Botania, kept separate from {@link ClothBotaniaConfig} so that a
 * dedicated server can load the JSON5 storage class without resolving {@code Screen} (which
 * implements the client-only {@code ContainerEventHandler}).
 */
public final class ClothBotaniaConfigScreen {
	private ClothBotaniaConfigScreen() {}

	public static Screen create(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Botania"));

		ConfigEntryBuilder eb = builder.entryBuilder();

		// === Common Config ===

		ConfigCategory blockBreaking = builder.getOrCreateCategory(Component.literal("Block Breaking Particles"));
		blockBreaking.addEntry(eb.startBooleanToggle(Component.literal("Enabled"), ClothBotaniaConfig.COMMON.blockBreakParticles)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Remove block breaking particles from flowers and other items."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.blockBreakParticles = v)
				.build());
		blockBreaking.addEntry(eb.startBooleanToggle(Component.literal("Tool Enabled"), ClothBotaniaConfig.COMMON.blockBreakParticlesTool)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Remove block breaking particles from the Terra Shatterer."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.blockBreakParticlesTool = v)
				.build());

		ConfigCategory spreaders = builder.getOrCreateCategory(Component.literal("Mana Spreaders"));
		spreaders.addEntry(eb.startBooleanToggle(Component.literal("Silent"), ClothBotaniaConfig.COMMON.silentSpreaders)
				.setDefaultValue(false)
				.setTooltip(Component.literal("Disable the mana spreader shooting sound."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.silentSpreaders = v)
				.build());
		spreaders.addEntry(eb.startIntField(Component.literal("Trace Time"), ClothBotaniaConfig.COMMON.spreaderTraceTime)
				.setDefaultValue(400)
				.setTooltip(Component.literal("Ticks into the future mana spreaders predict burst positions."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.spreaderTraceTime = v)
				.build());

		ConfigCategory harvestLevels = builder.getOrCreateCategory(Component.literal("Harvest Levels"));
		harvestLevels.addEntry(eb.startIntField(Component.literal("Weight Lens"), ClothBotaniaConfig.COMMON.harvestLevelWeight)
				.setDefaultValue(2).setMin(0).setMax(4)
				.setTooltip(Component.literal("Harvest level of the Mana Lens: Weighted. 3=diamond, 2=iron."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.harvestLevelWeight = v)
				.build());
		harvestLevels.addEntry(eb.startIntField(Component.literal("Bore Lens"), ClothBotaniaConfig.COMMON.harvestLevelBore)
				.setDefaultValue(3).setMin(0).setMax(4)
				.setTooltip(Component.literal("Harvest level of the Mana Lens: Bore. 3=diamond."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.harvestLevelBore = v)
				.build());

		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(eb.startBooleanToggle(Component.literal("Charge Animation"), ClothBotaniaConfig.COMMON.chargingAnimationEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Animation when an item charges on a mana pool."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.chargingAnimationEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Ender Pickpocket"), ClothBotaniaConfig.COMMON.enderPickpocketEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Hand of Ender can pickpocket other players' ender chests."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.enderPickpocketEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Mana Enchanter"), ClothBotaniaConfig.COMMON.enchanterEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Enable the Mana Enchanter."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.enchanterEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Relics"), ClothBotaniaConfig.COMMON.relicsEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Enable the Relic System."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.relicsEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Invert Magnet Ring"), ClothBotaniaConfig.COMMON.invertMagnetRing)
				.setDefaultValue(false).setTooltip(Component.literal("Invert Ring of Magnetization controls."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.invertMagnetRing = v).build());

		ConfigCategory gog = builder.getOrCreateCategory(Component.literal("Garden of Glass"));
		gog.addEntry(eb.startBooleanToggle(Component.literal("Spawn With Lexicon"), ClothBotaniaConfig.COMMON.gogSpawnWithLexicon)
				.setDefaultValue(true).setTooltip(Component.literal("Spawn with Lexica Botania in Garden of Glass worlds."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.gogSpawnWithLexicon = v).build());
		gog.addEntry(eb.startIntField(Component.literal("Island Scale Multiplier"), ClothBotaniaConfig.COMMON.gogIslandScaleMultiplier)
				.setDefaultValue(8).setMin(1).setMax(512)
				.setTooltip(Component.literal("Multiplier for island distances in multiplayer Garden of Glass."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.gogIslandScaleMultiplier = v).build());

		ConfigCategory rannuncarpus = builder.getOrCreateCategory(Component.literal("Rannuncarpus"));
		rannuncarpus.addEntry(eb.startStrList(Component.literal("Item Blacklist"), ClothBotaniaConfig.COMMON.rannuncarpusItemBlacklist)
				.setDefaultValue(Collections.emptyList())
				.setTooltip(Component.literal("Item registry names ignored by rannuncarpuses."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.rannuncarpusItemBlacklist = v).build());
		rannuncarpus.addEntry(eb.startStrList(Component.literal("Mod Blacklist"), ClothBotaniaConfig.COMMON.rannuncarpusModBlacklist)
				.setDefaultValue(List.of("storagedrawers"))
				.setTooltip(Component.literal("Mod names ignored by rannuncarpuses."))
				.setSaveConsumer(v -> ClothBotaniaConfig.COMMON.rannuncarpusModBlacklist = v).build());

		// === Client Config ===

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			ConfigCategory rendering = builder.getOrCreateCategory(Component.literal("Rendering"));
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Shaders"), ClothBotaniaConfig.CLIENT.useShaders)
					.setDefaultValue(true).setTooltip(Component.literal("Use shaders for some renders. (Requires restart)"))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.useShaders = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Bound Block Wireframe"), ClothBotaniaConfig.CLIENT.boundBlockWireframe)
					.setDefaultValue(true).setTooltip(Component.literal("Wireframe when looking at bound blocks."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.boundBlockWireframe = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Accessories"), ClothBotaniaConfig.CLIENT.renderAccessories)
					.setDefaultValue(true).setTooltip(Component.literal("Render accessories on the player."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.renderAccessories = v).build());
			rendering.addEntry(eb.startIntField(Component.literal("Mana Bar Height"), ClothBotaniaConfig.CLIENT.manaBarHeight)
					.setDefaultValue(29).setTooltip(Component.literal("Height of the mana bar above XP bar."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.manaBarHeight = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Static Floaters"), ClothBotaniaConfig.CLIENT.staticFloaters)
					.setDefaultValue(false).setTooltip(Component.literal("Disable floating flower animations for performance."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.staticFloaters = v).build());

			ConfigCategory skybox = builder.getOrCreateCategory(Component.literal("Fancy Skybox"));
			skybox.addEntry(eb.startBooleanToggle(Component.literal("Enabled"), ClothBotaniaConfig.CLIENT.enableFancySkybox)
					.setDefaultValue(true).setTooltip(Component.literal("Fancy skybox in Garden of Glass."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.enableFancySkybox = v).build());
			skybox.addEntry(eb.startBooleanToggle(Component.literal("Normal Worlds"), ClothBotaniaConfig.CLIENT.enableFancySkyboxInNormalWorlds)
					.setDefaultValue(false).setTooltip(Component.literal("Fancy skybox in non-Garden of Glass worlds."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.enableFancySkyboxInNormalWorlds = v).build());

			ConfigCategory lexicon = builder.getOrCreateCategory(Component.literal("Lexicon"));
			lexicon.addEntry(eb.startBooleanToggle(Component.literal("Rotating Items"), ClothBotaniaConfig.CLIENT.lexiconRotatingItems)
					.setDefaultValue(true).setTooltip(Component.literal("Rotating items in petal/rune entries."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.lexiconRotatingItems = v).build());
			lexicon.addEntry(eb.startBooleanToggle(Component.literal("3D Model"), ClothBotaniaConfig.CLIENT.lexicon3dModel)
					.setDefaultValue(true).setTooltip(Component.literal("Animated 3D render for the Lexica Botania."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.lexicon3dModel = v).build());

			ConfigCategory particles = builder.getOrCreateCategory(Component.literal("Particles"));
			particles.addEntry(eb.startBooleanToggle(Component.literal("Subtle Power System"), ClothBotaniaConfig.CLIENT.subtlePowerSystem)
					.setDefaultValue(false).setTooltip(Component.literal("Subtler power system particles."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.subtlePowerSystem = v).build());
			particles.addEntry(eb.startBooleanToggle(Component.literal("Static Wand Beam"), ClothBotaniaConfig.CLIENT.staticWandBeam)
					.setDefaultValue(false).setTooltip(Component.literal("Static wand beam showing all burst positions."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.staticWandBeam = v).build());
			particles.addEntry(eb.startDoubleField(Component.literal("Flower Frequency"), ClothBotaniaConfig.CLIENT.flowerParticleFrequency)
					.setDefaultValue(0.75).setTooltip(Component.literal("Particle frequency from worldgen mystical flowers."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.flowerParticleFrequency = v).build());
			particles.addEntry(eb.startBooleanToggle(Component.literal("Elf Portal"), ClothBotaniaConfig.CLIENT.elfPortalParticlesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("Particles in the elven portal."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.elfPortalParticlesEnabled = v).build());

			ConfigCategory misc = builder.getOrCreateCategory(Component.literal("Miscellaneous"));
			misc.addEntry(eb.startBooleanToggle(Component.literal("Seasonal Features"), ClothBotaniaConfig.CLIENT.enableSeasonalFeatures)
					.setDefaultValue(true).setTooltip(Component.literal("Seasonal features (halloween, christmas)."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.enableSeasonalFeatures = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("Debug Info"), ClothBotaniaConfig.CLIENT.debugInfo)
					.setDefaultValue(true).setTooltip(Component.literal("Botania messages in F3 debug screen."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.debugInfo = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("References"), ClothBotaniaConfig.CLIENT.referencesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("References in flower tooltips."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.referencesEnabled = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("Splashes"), ClothBotaniaConfig.CLIENT.splashesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("Botania splashes in the main menu."))
					.setSaveConsumer(v -> ClothBotaniaConfig.CLIENT.splashesEnabled = v).build());
		}

		builder.setSavingRunnable(() -> {
			try {
				writeConfigFile(ClothBotaniaConfig.CONFIG_DIR.resolve(LibMisc.MOD_ID + "-common.json5"), ClothBotaniaConfig.toJsonObject(ClothBotaniaConfig.COMMON));
				if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
					writeConfigFile(ClothBotaniaConfig.CONFIG_DIR.resolve(LibMisc.MOD_ID + "-client.json5"), ClothBotaniaConfig.toJsonObject(ClothBotaniaConfig.CLIENT));
				}
			} catch (Exception e) {
				BotaniaAPI.LOGGER.error("Error saving config", e);
			}
		});

		return builder.build();
	}

	private static void writeConfigFile(Path path, JsonObject obj) {
		try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
			s.write(obj.toJson(JsonGrammar.JANKSON).getBytes());
		} catch (IOException e) {
			BotaniaAPI.LOGGER.error("Error writing config to {}", path, e);
		}
	}
}
