package vazkii.botania.neoforge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.common.lib.LibMisc;

import java.util.function.Supplier;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class NeoForgeRegistryHandler {
	private static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister
			.createDataComponents(Registries.DATA_COMPONENT_TYPE, LibMisc.MOD_ID);

	// Fluid content storage for item fluid handlers (water bowl, etc.)
	public static final Supplier<DataComponentType<SimpleFluidContent>> SIMPLE_FLUID_CONTENT = DATA_COMPONENTS
			.register("simple_fluid_content",
					() -> DataComponentType.<SimpleFluidContent>builder()
							.persistent(SimpleFluidContent.CODEC)
							.networkSynchronized(SimpleFluidContent.STREAM_CODEC)
							.build());

	public static void init(IEventBus modBus) {
		modBus.addListener(NeoForgeRegistryHandler::registerRegistry);
		DATA_COMPONENTS.register(modBus);
	}

	private static void registerRegistry(NewRegistryEvent evt) {
		evt.create(new RegistryBuilder<>(BotaniaRegistries.BREWS)
				.defaultKey(prefix("fallback"))
				.sync(false));
	}
}
