package vazkii.botania.fabric.data;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;

import java.util.concurrent.CompletableFuture;

import static vazkii.botania.common.BotaniaDamageTypes.*;

public class BotaniaDynamicRegistryProvider extends FabricDynamicRegistryProvider {

	public BotaniaDynamicRegistryProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registriesFuture);
	}

	@Override
	protected void configure(HolderLookup.Provider registries, Entries entries) {
		entries.add(RELIC_DAMAGE, RELIC);
		entries.add(PLAYER_ATTACK_ARMOR_PIERCING, PLAYER_AP);
		entries.add(KEY_EXPLOSION, KEY);
		entries.addAll(registries.lookupOrThrow(Registries.BANNER_PATTERN));
	}

	@Override
	public String getName() {
		return "BotaniaDynamicRegistryProvider";
	}
}
