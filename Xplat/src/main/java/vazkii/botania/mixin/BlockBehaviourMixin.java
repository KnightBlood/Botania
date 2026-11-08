/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import vazkii.botania.common.lib.RegistrationBackfill;

import java.util.Optional;

// 26.1: BlockBehaviour construction requires Properties.id (for the default loot table key and
// description id). Botania constructs its blocks as static fields before the ids are known, so
// during construction we substitute placeholders; RegistrationBackfill backfills the real values
// when the registration id becomes available.
@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;effectiveDrops()Ljava/util/Optional;"))
	private Optional<ResourceKey<LootTable>> botania$deferDrops(BlockBehaviour.Properties props) {
		if (RegistrationBackfill.isDeferredBlock(props)) {
			return Optional.empty();
		}
		return ((BlockPropertiesAccessor) (Object) props).botania$effectiveDrops();
	}

	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockBehaviour$Properties;effectiveDescriptionId()Ljava/lang/String;"))
	private String botania$deferDescriptionId(BlockBehaviour.Properties props) {
		if (RegistrationBackfill.isDeferredBlock(props)) {
			return "block.pending";
		}
		return ((BlockPropertiesAccessor) (Object) props).botania$effectiveDescriptionId();
	}
}
