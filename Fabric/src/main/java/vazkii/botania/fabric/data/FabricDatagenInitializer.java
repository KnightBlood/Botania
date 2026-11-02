/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageType;

import vazkii.botania.data.*;
import vazkii.botania.data.recipes.*;

import static vazkii.botania.common.BotaniaDamageTypes.*;

public class FabricDatagenInitializer implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator generator) {
		bindItemComponents();
		if (System.getProperty("botania.xplat_datagen") != null) {
			configureXplatDatagen(generator.createPack());
		} else {
			configureFabricDatagen(generator.createPack());
		}
	}

	/**
	 * 26.1 binds item data components only during a resource reload
	 * ({@code ReloadableServerResources} / {@code RegistryDataCollector}); datagen never runs one, so
	 * any {@code new ItemStack(...)} in a provider dies with "Components not bound yet". Bind the
	 * registered initializers once, exactly like those reload paths do, before providers run.
	 */
	private static void bindItemComponents() {
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(VanillaRegistries.createLookup())
				.forEach(DataComponentInitializers.PendingComponents::apply);
	}

	private static void configureFabricDatagen(FabricDataGenerator.Pack pack) {
		pack.addProvider((PackOutput output) -> new FabricBlockLootProvider(output));
		// 26.1: fabric's tag alias mixin casts every registered TagsProvider to FabricTagsProvider,
		// so the Fabric-specific tag providers need the same bridges as the Xplat ones.
		var blockTagBridge = pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(
				output, Registries.BLOCK, registriesFuture,
				new FabricBlockTagProvider(output, registriesFuture)));
		var blockTagProvider = (FabricBlockTagProvider) blockTagBridge.getDelegate();
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.ITEM, registriesFuture,
				new FabricItemTagProvider(output, registriesFuture, blockTagProvider)));
		pack.addProvider((PackOutput output) -> new FabricRecipeProvider(output));
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.BIOME, registriesFuture, new FabricBiomeTagProvider(output, registriesFuture)));
	}

	private static void configureXplatDatagen(FabricDataGenerator.Pack pack) {
		pack.addProvider((output, registriesFuture) -> new BlockLootProvider(output, registriesFuture));
		pack.addProvider((PackOutput output) -> new LooniumStructureLootProvider(output));
		pack.addProvider((PackOutput output) -> new LooniumStructureConfigurationProvider(output));
		pack.addProvider(LooniumEquipmentLootProvider::new);
		// 26.1: fabric's tag alias mixin casts every registered TagsProvider to FabricTagsProvider,
		// so Xplat tag providers are registered through FabricXplatTagBridge bridges.
		var blockTagBridge = pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(
				output, Registries.BLOCK, registriesFuture,
				new BlockTagProvider(output, registriesFuture)));
		var blockTagProvider = (BlockTagProvider) blockTagBridge.getDelegate();
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.ITEM, registriesFuture,
				new ItemTagProvider(output, registriesFuture, blockTagProvider)));
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.ENTITY_TYPE, registriesFuture, new EntityTagProvider(output, registriesFuture)));
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.BANNER_PATTERN, registriesFuture, new BannerPatternTagsProvider(output, registriesFuture)));
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.BIOME, registriesFuture, new BiomeTagProvider(output, registriesFuture)));
		pack.addProvider((output, registriesFuture) -> new FabricXplatTagBridge<>(output,
				Registries.DAMAGE_TYPE, registriesFuture, new DamageTypeTagProvider(output, registriesFuture)));
		pack.addProvider(BotaniaDynamicRegistryProvider::new);
		pack.addProvider((PackOutput output) -> new StonecuttingProvider(output));
		pack.addProvider((PackOutput output) -> new CraftingRecipeProvider(output));
		pack.addProvider((PackOutput output) -> new SmeltingProvider(output));
		pack.addProvider((PackOutput output) -> new ElvenTradeProvider(output));
		pack.addProvider((PackOutput output) -> new ManaInfusionProvider(output));
		pack.addProvider((PackOutput output) -> new PureDaisyProvider(output));
		pack.addProvider((PackOutput output) -> new BrewProvider(output));
		pack.addProvider((PackOutput output) -> new PetalApothecaryProvider(output));
		pack.addProvider((PackOutput output) -> new RunicAltarProvider(output));
		pack.addProvider((PackOutput output) -> new TerrestrialAgglomerationProvider(output));
		pack.addProvider((PackOutput output) -> new OrechidProvider(output));
		pack.addProvider((PackOutput output) -> new BlockstateProvider(output));
		pack.addProvider((PackOutput output) -> new FloatingFlowerModelProvider(output));
		pack.addProvider((PackOutput output) -> new ItemModelProvider(output));
		pack.addProvider((PackOutput output) -> new PottedPlantModelProvider(output));
		pack.addProvider(AdvancementProvider::create);
	}

	@Override
	public void buildRegistry(RegistrySetBuilder builder) {
		builder.add(Registries.DAMAGE_TYPE, FabricDatagenInitializer::damageTypeBC);
		builder.add(Registries.BANNER_PATTERN, vazkii.botania.common.block.BotaniaBannerPatterns::bootstrap);
	}

	protected static void damageTypeBC(BootstrapContext<DamageType> context) {
		context.register(RELIC_DAMAGE, RELIC);
		context.register(PLAYER_ATTACK_ARMOR_PIERCING, PLAYER_AP);
		context.register(KEY_EXPLOSION, KEY);
	}
}
