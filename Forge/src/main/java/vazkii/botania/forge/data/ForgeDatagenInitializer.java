package vazkii.botania.forge.data;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import vazkii.botania.common.lib.LibMisc;

public class ForgeDatagenInitializer {
	public static void init(IEventBus modBus) {
		// GatherDataEvent became abstract in NeoForge 26.1: listeners must target the
		// Client/Server subclasses. Both carry an independent DataGenerator, so each
		// side gets its own full provider set.
		modBus.addListener((GatherDataEvent.Client evt) -> configureForgeDatagen(evt));
		modBus.addListener((GatherDataEvent.Server evt) -> configureForgeDatagen(evt));
	}

	public static void configureForgeDatagen(GatherDataEvent evt) {
				var generator = evt.getGenerator();
		var output = generator.getPackOutput();
		// 26.1: vanilla datagen validates tag references against this run's builders and the
		// parent lookup only (ExistingFileHelper is gone). The Forge providers therefore extend
		// their Xplat counterparts: one shared builders map keeps the botania:* tags and the
		// cross-namespace forge:/c: compat tags referencing them resolvable in the same run.
		var blockTagProvider = new ForgeBlockTagProvider(output, evt.getLookupProvider());
		generator.addProvider(true, blockTagProvider);
		generator.addProvider(true, new ForgeItemTagProvider(output, evt.getLookupProvider(), blockTagProvider));
		generator.addProvider(true, new ForgeRecipeProvider(output));
		generator.addProvider(true, new ForgeBlockLootProvider(generator));
		generator.addProvider(true, new ForgeBiomeTagProvider(output, evt.getLookupProvider()));
		generator.addProvider(true, new ForgeEntityTagProvider(output, evt.getLookupProvider()));
	}
}
