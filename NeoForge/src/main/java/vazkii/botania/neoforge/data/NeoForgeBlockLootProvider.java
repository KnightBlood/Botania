package vazkii.botania.neoforge.data;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;

import java.util.concurrent.CompletableFuture;

public class NeoForgeBlockLootProvider implements DataProvider {
	private final DataGenerator generator;

	public NeoForgeBlockLootProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public String getName() {
		return "Botania block loot (NeoForge-specific)";
	}
}
