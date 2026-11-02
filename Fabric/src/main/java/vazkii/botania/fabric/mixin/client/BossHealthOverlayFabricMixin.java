package vazkii.botania.fabric.mixin.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.world.BossEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.client.core.handler.BossBarHandler;

@Mixin(BossHealthOverlay.class)
public class BossHealthOverlayFabricMixin {
	// 26.1: drawBar was renamed to extractBar and now uses the GuiGraphicsExtractor render state pipeline.
	@Inject(at = @At("HEAD"), method = "extractBar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IILnet/minecraft/world/BossEvent;)V", cancellable = true)
	private void onDrawBar(GuiGraphicsExtractor gui, int x, int y, BossEvent bossEvent, CallbackInfo ci) {
		// todo fabric hack: pass drawName=false because it's not easy to disable vanilla's name rendering via mixin
		var increment = BossBarHandler.onBarRender(gui, x, y, bossEvent, false);
		if (increment.isPresent()) {
			// todo fabric no clean way of communicating increment to outer code
			ci.cancel();
		}

	}
}
