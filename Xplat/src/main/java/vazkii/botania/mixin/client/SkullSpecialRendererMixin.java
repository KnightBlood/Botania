/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.renderer.special.SkullSpecialRenderer;

import com.mojang.blaze3d.vertex.PoseStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO(botania-gaia-debug): temporary diagnostics, remove after gaia head verified.
@Mixin(SkullSpecialRenderer.class)
public class SkullSpecialRendererMixin {
	private static final org.slf4j.Logger LOG = com.mojang.logging.LogUtils.getLogger();
	private static int calls;

	@Inject(at = @At("HEAD"), method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;IIZI)V")
	private void botania$hookSubmit(PoseStack poseStack, net.minecraft.client.renderer.SubmitNodeCollector collector, int light, int overlay, boolean usesBlockLight, int seed, CallbackInfo ci) {
		if (calls++ < 5) {
			LOG.warn("[botania-gaia-debug] SkullSpecialRenderer.submit (item form) call#{}", calls);
		}
	}
}
