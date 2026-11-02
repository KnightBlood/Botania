/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.item.FlowerPouchItem;

@Mixin(ItemEntity.class)
public class ItemEntityFabricMixin {
	@Shadow
	private int pickupDelay;

	@Inject(at = @At("HEAD"), method = "playerTouch", cancellable = true)
	private void onPickup(Player player, CallbackInfo ci) {
		ItemEntity self = (ItemEntity) (Object) this;
		// 26.1: the thrower field is now an EntityReference; use the public getOwner() accessor instead
		Entity owner = self.getOwner();
		if (!player.level().isClientSide() && pickupDelay == 0 && (owner == null || owner.getUUID().equals(player.getUUID()))
				&& FlowerPouchItem.onPickupItem(self, player)) {
			ci.cancel();
		}
	}

}
