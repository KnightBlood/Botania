package vazkii.botania.common.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.PreparableReloadListener.SharedState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.configdata.ConfigDataManager;
import vazkii.botania.api.configdata.LooniumStructureConfiguration;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ConfigDataManagerImpl implements ConfigDataManager {
	public static void registerListener() {
		XplatAbstractions.INSTANCE.registerReloadListener(PackType.SERVER_DATA, prefix("configdata"), new ConfigDataManagerImpl());
	}

	private final Map<Identifier, LooniumStructureConfiguration> looniumConfigs = new HashMap<>();

	@Override
	public @Nullable LooniumStructureConfiguration getEffectiveLooniumStructureConfiguration(Identifier id) {
		LooniumStructureConfiguration configuration = this.looniumConfigs.get(id);
		return configuration != null ? configuration.getEffectiveConfig(looniumConfigs::get) : null;
	}

	private static void validateLooniumConfig(Map<Identifier, LooniumStructureConfiguration> map) {
		Set<Identifier> errorEntries = new HashSet<>();
		Set<Identifier> visitedEntries = new LinkedHashSet<>();
		do {
			errorEntries.clear();
			for (Map.Entry<Identifier, LooniumStructureConfiguration> entry : map.entrySet()) {
				Identifier id = entry.getKey();
				Identifier parent = entry.getValue().parent;
				if (id.equals(parent)) {
					BotaniaAPI.LOGGER.warn("Ignoring Loonium structure configuration, because it specified itself as parent: {}", id);
					errorEntries.add(id);
				} else {
					visitedEntries.clear();
					if (!findTopmostParent(map, id, parent, visitedEntries)) {
						BotaniaAPI.LOGGER.warn("Ignoring Loonium structure configuration(s) without top-most parent: {}", visitedEntries);
						errorEntries.addAll(visitedEntries);
						break;
					}
				}
			}
			errorEntries.forEach(map::remove);
		} while (!errorEntries.isEmpty() && !map.isEmpty());

		if (!map.containsKey(LooniumStructureConfiguration.DEFAULT_CONFIG_ID)) {
			BotaniaAPI.LOGGER.error("Default Loonium configuration not found!");
		}
	}

	private static boolean findTopmostParent(Map<Identifier, LooniumStructureConfiguration> map,
			Identifier id, Identifier parent, Set<Identifier> visitedEntries) {
		if (!visitedEntries.add(id)) {
			BotaniaAPI.LOGGER.warn("Cyclic dependency between Loonium structure configurations detected: {}", visitedEntries);
			return false;
		}
		if (parent == null) {
			return true;
		}
		var parentConfig = map.get(parent);
		return parentConfig != null && findTopmostParent(map, parent, parentConfig.parent, visitedEntries);
	}

	private void applyLooniumConfig(Map<Identifier, LooniumStructureConfiguration> looniumConfigs) {
		BotaniaAPI.LOGGER.info("Loaded {} Loonium configurations", looniumConfigs.size());
		this.looniumConfigs.putAll(looniumConfigs);
	}

	@NotNull
	@Override
	public CompletableFuture<Void> reload(@NotNull SharedState state, @NotNull Executor backgroundExecutor,
			@NotNull PreparationBarrier barrier, @NotNull Executor gameExecutor) {
		var looniumTask = scheduleConfigParse(barrier, state.resourceManager(), backgroundExecutor, gameExecutor, ConfigDataType.LOONUIM);

		return CompletableFuture.allOf(looniumTask).thenRun(() -> BotaniaAPI.instance().setConfigData(this));
	}

	private <T> CompletableFuture<Void> scheduleConfigParse(PreparationBarrier barrier, ResourceManager manager,
			Executor backgroundExecutor, Executor gameExecutor, ConfigDataType<T> type) {
		return CompletableFuture.supplyAsync(() -> {
			Map<Identifier, T> configs = new HashMap<>();
			SimpleJsonResourceReloadListener.scanDirectory(manager, FileToIdConverter.json(type.directory), JsonOps.INSTANCE, type.codec, configs);
			type.validateFunction.accept(configs);
			return configs;
		}, backgroundExecutor)
				.thenCompose(barrier::wait)
				.thenAcceptAsync(c -> type.applyFunction.accept(this, c), gameExecutor);
	}

	private record ConfigDataType<T> (Codec<T> codec, String directory,
			Consumer<Map<Identifier, T>> validateFunction,
			BiConsumer<ConfigDataManagerImpl, Map<Identifier, T>> applyFunction) {
		private static final ConfigDataType<LooniumStructureConfiguration> LOONUIM =
				new ConfigDataType<>(LooniumStructureConfiguration.CODEC, "loonium_config",
						ConfigDataManagerImpl::validateLooniumConfig, ConfigDataManagerImpl::applyLooniumConfig);

	}
}
