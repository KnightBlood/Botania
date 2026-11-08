/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.BotaniaConfig;
import vazkii.botania.xplat.XplatAbstractions;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import blue.endless.jankson.Jankson;
import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonGrammar;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;
import blue.endless.jankson.api.SyntaxError;

public final class ClothBotaniaConfig {
	private static final Jankson JANKSON = Jankson.builder().build();
	static final Path CONFIG_DIR = Paths.get("config");

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

		@Override
		public boolean blockBreakParticles() {
			return blockBreakParticles;
		}

		@Override
		public boolean blockBreakParticlesTool() {
			return blockBreakParticlesTool;
		}

		@Override
		public boolean chargingAnimationEnabled() {
			return chargingAnimationEnabled;
		}

		@Override
		public boolean silentSpreaders() {
			return silentSpreaders;
		}

		@Override
		public int spreaderTraceTime() {
			return spreaderTraceTime;
		}

		@Override
		public boolean enderPickpocketEnabled() {
			return enderPickpocketEnabled;
		}

		@Override
		public boolean enchanterEnabled() {
			return enchanterEnabled;
		}

		@Override
		public boolean relicsEnabled() {
			return relicsEnabled;
		}

		@Override
		public boolean invertMagnetRing() {
			return invertMagnetRing;
		}

		@Override
		public int harvestLevelWeight() {
			return harvestLevelWeight;
		}

		@Override
		public int harvestLevelBore() {
			return harvestLevelBore;
		}

		@Override
		public boolean gogSpawnWithLexicon() {
			return gogSpawnWithLexicon;
		}

		@Override
		public int gogIslandScaleMultiplier() {
			return gogIslandScaleMultiplier;
		}

		@Override
		public List<String> rannuncarpusItemBlacklist() {
			return rannuncarpusItemBlacklist;
		}

		@Override
		public List<String> rannuncarpusModBlacklist() {
			return rannuncarpusModBlacklist;
		}
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

		@Override
		public boolean lexiconRotatingItems() {
			return lexiconRotatingItems;
		}

		@Override
		public boolean subtlePowerSystem() {
			return subtlePowerSystem;
		}

		@Override
		public boolean staticWandBeam() {
			return staticWandBeam;
		}

		@Override
		public boolean boundBlockWireframe() {
			return boundBlockWireframe;
		}

		@Override
		public boolean lexicon3dModel() {
			return lexicon3dModel;
		}

		@Override
		public double flowerParticleFrequency() {
			return flowerParticleFrequency;
		}

		@Override
		public boolean elfPortalParticlesEnabled() {
			return elfPortalParticlesEnabled;
		}

		@Override
		public boolean renderAccessories() {
			return renderAccessories;
		}

		@Override
		public boolean enableSeasonalFeatures() {
			return enableSeasonalFeatures;
		}

		@Override
		public boolean enableFancySkybox() {
			return enableFancySkybox;
		}

		@Override
		public boolean enableFancySkyboxInNormalWorlds() {
			return enableFancySkyboxInNormalWorlds;
		}

		@Override
		public int manaBarHeight() {
			return manaBarHeight;
		}

		@Override
		public boolean staticFloaters() {
			return staticFloaters;
		}

		@Override
		public boolean debugInfo() {
			return debugInfo;
		}

		@Override
		public boolean referencesEnabled() {
			return referencesEnabled;
		}

		@Override
		public boolean splashesEnabled() {
			return splashesEnabled;
		}

		@Override
		public boolean useShaders() {
			return useShaders;
		}
	}

	static final CommonConfig COMMON = new CommonConfig();
	static final ClientConfig CLIENT = new ClientConfig();

	// --- Jankson JSON I/O ---

	private static void writeDefaultConfig(Path path, JsonObject obj) {
		try (OutputStream s = new BufferedOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW))) {
			s.write(obj.toJson(JsonGrammar.JANKSON).getBytes());
		} catch (FileAlreadyExistsException ignored) {} catch (IOException e) {
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

	static JsonObject toJsonObject(Object obj) {
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
			if (elem == null || elem instanceof JsonNull)
				continue;
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
		} catch (FileAlreadyExistsException ignored) {} catch (IOException e) {
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
}
