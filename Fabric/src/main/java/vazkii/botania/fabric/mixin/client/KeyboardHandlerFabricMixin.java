/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.handler.KonamiHandler;
import vazkii.botania.common.item.equipment.bauble.RingOfDexterousMotionItem;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerFabricMixin {
	// MC 26.1: keyPress(long window, int action, KeyEvent) - key/scancode/modifiers moved into KeyEvent
	@Inject(at = @At("HEAD"), method = "keyPress")
	private void keyEvent(long window, int action, KeyEvent event, CallbackInfo ci) {
		if (window == Minecraft.getInstance().getWindow().handle()) {
			RingOfDexterousMotionItem.ClientLogic.onKeyDown();
			KonamiHandler.handleInput(event.key(), action, event.modifiers());
		}
	}
}
