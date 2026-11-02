/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.resources.model.ModelManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.fabric.client.FabricClientInitializer;

/**
 * 26.1: NeoForge fires {@code ModelEvent.ModifyBakingResult} once the models are baked, but Fabric
 * has no equivalent event. Baking Botania's extra models from {@code CLIENT_STARTED} (as the port
 * did) happens before the model manager has baked anything, so the extra models were never
 * available and the whole set was silently dropped.
 * <p>
 * {@link ModelManager#reload} only completes after the baked results have been applied, so chaining
 * onto its future gives the same "models are ready" moment the NeoForge event provides.
 */
@Mixin(ModelManager.class)
public class ModelManagerMixin {
	@Inject(method = "reload", at = @At("RETURN"), cancellable = true)
	private void botania$bakeExtraModels(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.setReturnValue(cir.getReturnValue()
				.thenRun(() -> FabricClientInitializer.onModelsBaked((ModelManager) (Object) this)));
	}
}
