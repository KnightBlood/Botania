package vazkii.botania.fabric.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import vazkii.botania.common.lib.LibMisc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FabricBlockLootProvider implements DataProvider {
	private final PackOutput.PathProvider pathProvider;

	public FabricBlockLootProvider(PackOutput packOutput) {
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, Registries.LOOT_TABLE.identifier().getPath() + "/blocks");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		var tables = new HashMap<Identifier, LootTable.Builder>();
		for (var b : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(b);
			if (!LibMisc.MOD_ID.equals(id.getNamespace())) {
				continue;
			}

			// Nothing for now
		}

		List<CompletableFuture<?>> output = new ArrayList<>();

		for (var e : tables.entrySet()) {
			Path path = pathProvider.json(e.getKey());
			// 26.1: Deserializers was removed; save via the LootTable DIRECT_CODEC
			// (mirrors Xplat BlockLootProvider).
			output.add(DataProvider.saveStable(cache, LootTable.DIRECT_CODEC, e.getValue().setParamSet(LootContextParamSets.BLOCK).build(), path));
		}
		return CompletableFuture.allOf(output.toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "Botania block loot (Fabric-specific)";
	}
}
