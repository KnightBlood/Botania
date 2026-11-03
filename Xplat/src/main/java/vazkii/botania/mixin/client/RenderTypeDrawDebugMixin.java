/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderPass;

import net.minecraft.client.renderer.rendertype.RenderType;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.botania.api.BotaniaAPI;

/**
 * TEMPORARY diagnostic: vanilla's "Missing sampler" crashes never name the render
 * type. Wrap the draw call to log the offending RenderType's name before rethrowing.
 */
@Mixin(RenderType.class)
public class RenderTypeDrawDebugMixin {
	@Shadow
	@Final
	private String name;

	@WrapOperation(method = "draw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderPass;drawIndexed(IIII)V"))
	private void botania$logDrawFailure(RenderPass instance, int baseVertex, int firstIndex, int indexCount, int instanceCount, Operation<Void> original) {
		try {
			original.call(instance, baseVertex, firstIndex, indexCount, instanceCount);
		} catch (Throwable t) {
			BotaniaAPI.LOGGER.error("[botania-render-debug] RenderType draw failed: {}", this.name, t);
			throw t;
		}
	}
}
