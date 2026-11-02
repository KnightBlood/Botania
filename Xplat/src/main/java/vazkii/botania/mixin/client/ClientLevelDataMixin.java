/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.LevelHeightAccessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.client.core.SkyblockWorldInfo;

@Mixin(ClientLevel.ClientLevelData.class)
public abstract class ClientLevelDataMixin implements SkyblockWorldInfo {
	private boolean gardenOfGlass;

	@Override
	public boolean isGardenOfGlass() {
		return gardenOfGlass;
	}

	@Override
	public void markGardenOfGlass() {
		gardenOfGlass = true;
	}

	// 26.1: getHorizonHeight takes a LevelHeightAccessor parameter; used by SkyRenderer to decide whether to render
	// the void plane below the horizon. Forcing 0 keeps the Garden of Glass skybox free of the void plane.
	@Inject(at = @At("HEAD"), method = "getHorizonHeight", cancellable = true)
	private void gogHorizon(LevelHeightAccessor level, CallbackInfoReturnable<Double> cir) {
		if (gardenOfGlass) {
			cir.setReturnValue(0.0);
		}
	}

	// 26.1: replaced the old getClearColorScale (forced to 1.0). FogRenderer computes
	// void darkness as clamp(((minY + range) - cameraY) / range, 0, 1), so a range of 0
	// yields no darkening for any camera position above the world bottom.
	@Inject(at = @At("HEAD"), method = "voidDarknessOnsetRange", cancellable = true)
	private void gogFog(CallbackInfoReturnable<Float> cir) {
		if (gardenOfGlass) {
			cir.setReturnValue(0.0F);
		}
	}

}
