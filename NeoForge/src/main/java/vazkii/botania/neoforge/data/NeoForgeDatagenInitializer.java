package vazkii.botania.neoforge.data;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class NeoForgeDatagenInitializer {
	public static void init(IEventBus modBus) {
		// GatherDataEvent became abstract in NeoForge 26.1: listeners must target the
		// Client/Server subclasses. Both carry an independent DataGenerator, so each
		// side gets its own full provider set.
		modBus.addListener((GatherDataEvent.Client evt) -> configureNeoForgeDatagen(evt));
		modBus.addListener((GatherDataEvent.Server evt) -> configureNeoForgeDatagen(evt));
	}

	public static void configureNeoForgeDatagen(GatherDataEvent evt) {
		var generator = evt.getGenerator();
		var output = generator.getPackOutput();
		// 26.1: vanilla datagen validates tag references against this run's builders and the
		// parent lookup only (ExistingFileHelper is gone). The NeoForge providers therefore extend
		// their Xplat counterparts: one shared builders map keeps the botania:* tags and the
		// cross-namespace forge:/c: compat tags referencing them resolvable in the same run.
		var blockTagProvider = new NeoForgeBlockTagProvider(output, evt.getLookupProvider());
		generator.addProvider(true, blockTagProvider);
		generator.addProvider(true, new NeoForgeItemTagProvider(output, evt.getLookupProvider(), blockTagProvider));
		generator.addProvider(true, new NeoForgeRecipeProvider(output));
		generator.addProvider(true, new NeoForgeBlockLootProvider(generator));
		generator.addProvider(true, new NeoForgeBiomeTagProvider(output, evt.getLookupProvider()));
		generator.addProvider(true, new NeoForgeEntityTagProvider(output, evt.getLookupProvider()));
	}
}
