/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.advancements;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class BotaniaCriteriaTriggers {
	public static void init() {
		register(AlfheimPortalTrigger.ID, AlfheimPortalTrigger.INSTANCE);
		register(CorporeaRequestTrigger.ID, CorporeaRequestTrigger.INSTANCE);
		register(GaiaGuardianNoArmorTrigger.ID, GaiaGuardianNoArmorTrigger.INSTANCE);
		register(RelicBindTrigger.ID, RelicBindTrigger.INSTANCE);
		register(UseItemSuccessTrigger.ID, UseItemSuccessTrigger.INSTANCE);
		register(ManaBlasterTrigger.ID, ManaBlasterTrigger.INSTANCE);
		register(LokiPlaceTrigger.ID, LokiPlaceTrigger.INSTANCE);
		register(AlfheimPortalBreadTrigger.ID, AlfheimPortalBreadTrigger.INSTANCE);
	}

	private static <T extends CriterionTrigger<?>> void register(Identifier id, T trigger) {
		Registry.register(BuiltInRegistries.TRIGGER_TYPES, id, trigger);
	}
}
