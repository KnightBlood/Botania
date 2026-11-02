/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.world.level.block.SkullBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.client.render.block_entity.GaiaHeadBlockEntityRenderer;
import vazkii.botania.common.block.GaiaHeadBlock;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
	@Inject(at = @At("HEAD"), method = "createModel", cancellable = true)
	private static void hookCreateModel(EntityModelSet entityModelSet, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
		if (type == GaiaHeadBlock.GAIA_TYPE) {
			cir.setReturnValue(GaiaHeadBlockEntityRenderer.createGaiaHeadModel(entityModelSet));
		}
	}
}
