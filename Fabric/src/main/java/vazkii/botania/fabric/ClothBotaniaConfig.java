/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.BotaniaConfig;
import vazkii.botania.xplat.XplatAbstractions;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClothBotaniaConfig {
	private static final Jankson JANKSON = Jankson.builder().build();
	private static final Path CONFIG_DIR = Paths.get("config");

	// --- Serialization data classes ---

	public static class CommonConfig implements BotaniaConfig.ConfigAccess {
		public boolean blockBreakParticles = true;
		public boolean blockBreakParticlesTool = true;
		public boolean chargingAnimationEnabled = true;
		public boolean silentSpreaders = false;
		public int spreaderTraceTime = 400;
		public boolean enderPickpocketEnabled = true;
		public boolean enchanterEnabled = true;
		public boolean relicsEnabled = true;
		public boolean invertMagnetRing = false;
		public int harvestLevelWeight = 2;
		public int harvestLevelBore = 3;
		public boolean gogSpawnWithLexicon = true;
		public int gogIslandScaleMultiplier = 8;
		public List<String> rannuncarpusItemBlacklist = new ArrayList<>();
		public List<String> rannuncarpusModBlacklist = new ArrayList<>(List.of("storagedrawers"));

		@Override public boolean blockBreakParticles() { return blockBreakParticles; }
		@Override public boolean blockBreakParticlesTool() { return blockBreakParticlesTool; }
		@Override public boolean chargingAnimationEnabled() { return chargingAnimationEnabled; }
		@Override public boolean silentSpreaders() { return silentSpreaders; }
		@Override public int spreaderTraceTime() { return spreaderTraceTime; }
		@Override public boolean enderPickpocketEnabled() { return enderPickpocketEnabled; }
		@Override public boolean enchanterEnabled() { return enchanterEnabled; }
		@Override public boolean relicsEnabled() { return relicsEnabled; }
		@Override public boolean invertMagnetRing() { return invertMagnetRing; }
		@Override public int harvestLevelWeight() { return harvestLevelWeight; }
		@Override public int harvestLevelBore() { return harvestLevelBore; }
		@Override public boolean gogSpawnWithLexicon() { return gogSpawnWithLexicon; }
		@Override public int gogIslandScaleMultiplier() { return gogIslandScaleMultiplier; }
		@Override public List<String> rannuncarpusItemBlacklist() { return rannuncarpusItemBlacklist; }
		@Override public List<String> rannuncarpusModBlacklist() { return rannuncarpusModBlacklist; }
	}

	public static class ClientConfig implements BotaniaConfig.ClientConfigAccess {
		public boolean lexiconRotatingItems = true;
		public boolean subtlePowerSystem = false;
		public boolean staticWandBeam = false;
		public boolean boundBlockWireframe = true;
		public boolean lexicon3dModel = true;
		public double flowerParticleFrequency = 0.75;
		public boolean elfPortalParticlesEnabled = true;
		public boolean renderAccessories = true;
		public boolean enableSeasonalFeatures = true;
		public boolean enableFancySkybox = true;
		public boolean enableFancySkyboxInNormalWorlds = false;
		public int manaBarHeight = 29;
		public boolean staticFloaters = false;
		public boolean debugInfo = true;
		public boolean referencesEnabled = true;
		public boolean splashesEnabled = true;
		public boolean useShaders = true;

		@Override public boolean lexiconRotatingItems() { return lexiconRotatingItems; }
		@Override public boolean subtlePowerSystem() { return subtlePowerSystem; }
		@Override public boolean staticWandBeam() { return staticWandBeam; }
		@Override public boolean boundBlockWireframe() { return boundBlockWireframe; }
		@Override public boolean lexicon3dModel() { return lexicon3dModel; }
		@Override public double flowerParticleFrequency() { return flowerParticleFrequency; }
		@Override public boolean elfPortalParticlesEnabled() { return elfPortalParticlesEnabled; }
		@Override public boolean renderAccessories() { return renderAccessories; }
		@Override public boolean enableSeasonalFeatures() { return enableSeasonalFeatures; }
		@Override public boolean enableFancySkybox() { return enableFancySkybox; }
		@Override public boolean enableFancySkyboxInNormalWorlds() { return enableFancySkyboxInNormalWorlds; }
		@Override public int manaBarHeight() { return manaBarHeight; }
		@Override public boolean staticFloaters() { return staticFloaters; }
		@Override public boolean debugInfo() { return debugInfo; }
		@Override public boolean referencesEnabled() { return referencesEnabled; }
		@Override public boolean splashesEnabled() { return splashesEnabled; }
		@Override public boolean useShaders() { return useShaders; }
	}

	private static final CommonConfig COMMON = new CommonConfig();
	private static final ClientConfig CLIENT = new ClientConfig();

	// --- Jankson JSON I/O ---

	private static void writeDefaultConfig(Path path, JsonObject obj) {
		try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
			s.write(obj.toJson(JsonGrammar.JANKSON).getBytes());
		} catch (FileAlreadyExistsException ignored) {
		} catch (IOException e) {
			BotaniaAPI.LOGGER.error("Error writing default config", e);
		}
	}

	private static void loadConfig(Path path, Object target) {
		JsonObject defaultObj = toJsonObject(target);
		writeDefaultConfig(path, defaultObj);

		try (InputStream s = new BufferedInputStream(Files.newInputStream(path, StandardOpenOption.READ, StandardOpenOption.CREATE))) {
			JsonObject obj = JANKSON.load(s);
			fromJsonObject(obj, target);
		} catch (IOException | SyntaxError e) {
			BotaniaAPI.LOGGER.error("Error loading config from {}", path, e);
		}
	}

	private static JsonObject toJsonObject(Object obj) {
		JsonObject json = new JsonObject();
		for (var field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				Object val = field.get(obj);
				if (val instanceof List<?> list) {
					JsonArray arr = new JsonArray();
					for (Object item : list) {
						arr.add(new JsonPrimitive(item));
					}
					json.put(field.getName(), arr);
				} else {
					json.put(field.getName(), new JsonPrimitive(val));
				}
			} catch (IllegalAccessException e) {
				BotaniaAPI.LOGGER.error("Failed to serialize config field {}", field.getName(), e);
			}
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	private static void fromJsonObject(JsonObject json, Object obj) {
		for (var field : obj.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			JsonElement elem = json.get(field.getName());
			if (elem == null || elem instanceof JsonNull) continue;
			try {
				if (elem instanceof JsonPrimitive prim) {
					if (field.getType() == boolean.class) {
						field.setBoolean(obj, prim.asBoolean(field.getBoolean(obj)));
					} else if (field.getType() == int.class) {
						field.setInt(obj, prim.asInt((int) field.get(obj)));
					} else if (field.getType() == double.class) {
						field.setDouble(obj, prim.asDouble((double) field.get(obj)));
					}
				} else if (elem instanceof JsonArray arr && field.getType() == List.class) {
					List<String> list = new ArrayList<>();
					for (JsonElement item : arr) {
						if (item instanceof JsonPrimitive p) {
							list.add(p.getValue().toString());
						}
					}
					field.set(obj, list);
				}
			} catch (Exception e) {
				BotaniaAPI.LOGGER.error("Failed to deserialize config field {}", field.getName(), e);
			}
		}
	}

	// --- Setup ---

	public static void setup() {
		try {
			Files.createDirectory(CONFIG_DIR);
		} catch (FileAlreadyExistsException ignored) {
		} catch (IOException e) {
			BotaniaAPI.LOGGER.warn("Failed to make config dir", e);
		}

		loadConfig(CONFIG_DIR.resolve(LibMisc.MOD_ID + "-common.json5"), COMMON);
		BotaniaConfig.setCommon(COMMON);

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			loadConfig(CONFIG_DIR.resolve(LibMisc.MOD_ID + "-client.json5"), CLIENT);
			BotaniaConfig.setClient(CLIENT);
		}
		BotaniaConfig.resetPatchouliFlags();
	}

	// --- Config Screen (Cloth Config API) ---

	public static Screen createConfigScreen(Screen parent) {
		ConfigBuilder builder = ConfigBuilder.create()
				.setParentScreen(parent)
				.setTitle(Component.literal("Botania"));

		ConfigEntryBuilder eb = builder.entryBuilder();

		// === Common Config ===

		ConfigCategory blockBreaking = builder.getOrCreateCategory(Component.literal("Block Breaking Particles"));
		blockBreaking.addEntry(eb.startBooleanToggle(Component.literal("Enabled"), COMMON.blockBreakParticles)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Remove block breaking particles from flowers and other items."))
				.setSaveConsumer(v -> COMMON.blockBreakParticles = v)
				.build());
		blockBreaking.addEntry(eb.startBooleanToggle(Component.literal("Tool Enabled"), COMMON.blockBreakParticlesTool)
				.setDefaultValue(true)
				.setTooltip(Component.literal("Remove block breaking particles from the Terra Shatterer."))
				.setSaveConsumer(v -> COMMON.blockBreakParticlesTool = v)
				.build());

		ConfigCategory spreaders = builder.getOrCreateCategory(Component.literal("Mana Spreaders"));
		spreaders.addEntry(eb.startBooleanToggle(Component.literal("Silent"), COMMON.silentSpreaders)
				.setDefaultValue(false)
				.setTooltip(Component.literal("Disable the mana spreader shooting sound."))
				.setSaveConsumer(v -> COMMON.silentSpreaders = v)
				.build());
		spreaders.addEntry(eb.startIntField(Component.literal("Trace Time"), COMMON.spreaderTraceTime)
				.setDefaultValue(400)
				.setTooltip(Component.literal("Ticks into the future mana spreaders predict burst positions."))
				.setSaveConsumer(v -> COMMON.spreaderTraceTime = v)
				.build());

		ConfigCategory harvestLevels = builder.getOrCreateCategory(Component.literal("Harvest Levels"));
		harvestLevels.addEntry(eb.startIntField(Component.literal("Weight Lens"), COMMON.harvestLevelWeight)
				.setDefaultValue(2).setMin(0).setMax(4)
				.setTooltip(Component.literal("Harvest level of the Mana Lens: Weighted. 3=diamond, 2=iron."))
				.setSaveConsumer(v -> COMMON.harvestLevelWeight = v)
				.build());
		harvestLevels.addEntry(eb.startIntField(Component.literal("Bore Lens"), COMMON.harvestLevelBore)
				.setDefaultValue(3).setMin(0).setMax(4)
				.setTooltip(Component.literal("Harvest level of the Mana Lens: Bore. 3=diamond."))
				.setSaveConsumer(v -> COMMON.harvestLevelBore = v)
				.build());

		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(eb.startBooleanToggle(Component.literal("Charge Animation"), COMMON.chargingAnimationEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Animation when an item charges on a mana pool."))
				.setSaveConsumer(v -> COMMON.chargingAnimationEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Ender Pickpocket"), COMMON.enderPickpocketEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Hand of Ender can pickpocket other players' ender chests."))
				.setSaveConsumer(v -> COMMON.enderPickpocketEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Mana Enchanter"), COMMON.enchanterEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Enable the Mana Enchanter."))
				.setSaveConsumer(v -> COMMON.enchanterEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Relics"), COMMON.relicsEnabled)
				.setDefaultValue(true).setTooltip(Component.literal("Enable the Relic System."))
				.setSaveConsumer(v -> COMMON.relicsEnabled = v).build());
		general.addEntry(eb.startBooleanToggle(Component.literal("Invert Magnet Ring"), COMMON.invertMagnetRing)
				.setDefaultValue(false).setTooltip(Component.literal("Invert Ring of Magnetization controls."))
				.setSaveConsumer(v -> COMMON.invertMagnetRing = v).build());

		ConfigCategory gog = builder.getOrCreateCategory(Component.literal("Garden of Glass"));
		gog.addEntry(eb.startBooleanToggle(Component.literal("Spawn With Lexicon"), COMMON.gogSpawnWithLexicon)
				.setDefaultValue(true).setTooltip(Component.literal("Spawn with Lexica Botania in Garden of Glass worlds."))
				.setSaveConsumer(v -> COMMON.gogSpawnWithLexicon = v).build());
		gog.addEntry(eb.startIntField(Component.literal("Island Scale Multiplier"), COMMON.gogIslandScaleMultiplier)
				.setDefaultValue(8).setMin(1).setMax(512)
				.setTooltip(Component.literal("Multiplier for island distances in multiplayer Garden of Glass."))
				.setSaveConsumer(v -> COMMON.gogIslandScaleMultiplier = v).build());

		ConfigCategory rannuncarpus = builder.getOrCreateCategory(Component.literal("Rannuncarpus"));
		rannuncarpus.addEntry(eb.startStrList(Component.literal("Item Blacklist"), COMMON.rannuncarpusItemBlacklist)
				.setDefaultValue(Collections.emptyList())
				.setTooltip(Component.literal("Item registry names ignored by rannuncarpuses."))
				.setSaveConsumer(v -> COMMON.rannuncarpusItemBlacklist = v).build());
		rannuncarpus.addEntry(eb.startStrList(Component.literal("Mod Blacklist"), COMMON.rannuncarpusModBlacklist)
				.setDefaultValue(List.of("storagedrawers"))
				.setTooltip(Component.literal("Mod names ignored by rannuncarpuses."))
				.setSaveConsumer(v -> COMMON.rannuncarpusModBlacklist = v).build());

		// === Client Config ===

		if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
			ConfigCategory rendering = builder.getOrCreateCategory(Component.literal("Rendering"));
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Shaders"), CLIENT.useShaders)
					.setDefaultValue(true).setTooltip(Component.literal("Use shaders for some renders. (Requires restart)"))
					.setSaveConsumer(v -> CLIENT.useShaders = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Bound Block Wireframe"), CLIENT.boundBlockWireframe)
					.setDefaultValue(true).setTooltip(Component.literal("Wireframe when looking at bound blocks."))
					.setSaveConsumer(v -> CLIENT.boundBlockWireframe = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Accessories"), CLIENT.renderAccessories)
					.setDefaultValue(true).setTooltip(Component.literal("Render accessories on the player."))
					.setSaveConsumer(v -> CLIENT.renderAccessories = v).build());
			rendering.addEntry(eb.startIntField(Component.literal("Mana Bar Height"), CLIENT.manaBarHeight)
					.setDefaultValue(29).setTooltip(Component.literal("Height of the mana bar above XP bar."))
					.setSaveConsumer(v -> CLIENT.manaBarHeight = v).build());
			rendering.addEntry(eb.startBooleanToggle(Component.literal("Static Floaters"), CLIENT.staticFloaters)
					.setDefaultValue(false).setTooltip(Component.literal("Disable floating flower animations for performance."))
					.setSaveConsumer(v -> CLIENT.staticFloaters = v).build());

			ConfigCategory skybox = builder.getOrCreateCategory(Component.literal("Fancy Skybox"));
			skybox.addEntry(eb.startBooleanToggle(Component.literal("Enabled"), CLIENT.enableFancySkybox)
					.setDefaultValue(true).setTooltip(Component.literal("Fancy skybox in Garden of Glass."))
					.setSaveConsumer(v -> CLIENT.enableFancySkybox = v).build());
			skybox.addEntry(eb.startBooleanToggle(Component.literal("Normal Worlds"), CLIENT.enableFancySkyboxInNormalWorlds)
					.setDefaultValue(false).setTooltip(Component.literal("Fancy skybox in non-Garden of Glass worlds."))
					.setSaveConsumer(v -> CLIENT.enableFancySkyboxInNormalWorlds = v).build());

			ConfigCategory lexicon = builder.getOrCreateCategory(Component.literal("Lexicon"));
			lexicon.addEntry(eb.startBooleanToggle(Component.literal("Rotating Items"), CLIENT.lexiconRotatingItems)
					.setDefaultValue(true).setTooltip(Component.literal("Rotating items in petal/rune entries."))
					.setSaveConsumer(v -> CLIENT.lexiconRotatingItems = v).build());
			lexicon.addEntry(eb.startBooleanToggle(Component.literal("3D Model"), CLIENT.lexicon3dModel)
					.setDefaultValue(true).setTooltip(Component.literal("Animated 3D render for the Lexica Botania."))
					.setSaveConsumer(v -> CLIENT.lexicon3dModel = v).build());

			ConfigCategory particles = builder.getOrCreateCategory(Component.literal("Particles"));
			particles.addEntry(eb.startBooleanToggle(Component.literal("Subtle Power System"), CLIENT.subtlePowerSystem)
					.setDefaultValue(false).setTooltip(Component.literal("Subtler power system particles."))
					.setSaveConsumer(v -> CLIENT.subtlePowerSystem = v).build());
			particles.addEntry(eb.startBooleanToggle(Component.literal("Static Wand Beam"), CLIENT.staticWandBeam)
					.setDefaultValue(false).setTooltip(Component.literal("Static wand beam showing all burst positions."))
					.setSaveConsumer(v -> CLIENT.staticWandBeam = v).build());
			particles.addEntry(eb.startDoubleField(Component.literal("Flower Frequency"), CLIENT.flowerParticleFrequency)
					.setDefaultValue(0.75).setTooltip(Component.literal("Particle frequency from worldgen mystical flowers."))
					.setSaveConsumer(v -> CLIENT.flowerParticleFrequency = v).build());
			particles.addEntry(eb.startBooleanToggle(Component.literal("Elf Portal"), CLIENT.elfPortalParticlesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("Particles in the elven portal."))
					.setSaveConsumer(v -> CLIENT.elfPortalParticlesEnabled = v).build());

			ConfigCategory misc = builder.getOrCreateCategory(Component.literal("Miscellaneous"));
			misc.addEntry(eb.startBooleanToggle(Component.literal("Seasonal Features"), CLIENT.enableSeasonalFeatures)
					.setDefaultValue(true).setTooltip(Component.literal("Seasonal features (halloween, christmas)."))
					.setSaveConsumer(v -> CLIENT.enableSeasonalFeatures = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("Debug Info"), CLIENT.debugInfo)
					.setDefaultValue(true).setTooltip(Component.literal("Botania messages in F3 debug screen."))
					.setSaveConsumer(v -> CLIENT.debugInfo = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("References"), CLIENT.referencesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("References in flower tooltips."))
					.setSaveConsumer(v -> CLIENT.referencesEnabled = v).build());
			misc.addEntry(eb.startBooleanToggle(Component.literal("Splashes"), CLIENT.splashesEnabled)
					.setDefaultValue(true).setTooltip(Component.literal("Botania splashes in the main menu."))
					.setSaveConsumer(v -> CLIENT.splashesEnabled = v).build());
		}

		builder.setSavingRunnable(() -> {
			try {
				writeConfigFile(CONFIG_DIR.resolve(LibMisc.MOD_ID + "-common.json5"), toJsonObject(COMMON));
				if (XplatAbstractions.INSTANCE.isPhysicalClient()) {
					writeConfigFile(CONFIG_DIR.resolve(LibMisc.MOD_ID + "-client.json5"), toJsonObject(CLIENT));
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
