/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import net.minecraft.client.resources.SplashManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.xplat.BotaniaConfig;

import java.util.ArrayList;
import java.util.List;

@Mixin(SplashManager.class)
public class SplashManagerMixin {
	@Shadow
	private List<Component> splashes;

	/**
	 * Adds splash texts if enabled
	 */
	// MC 26.1: splashes are List<Component> and the reload result is immutable,
	// so we swap in a mutable copy before adding.
	@Inject(at = @At("RETURN"), method = "apply(Ljava/util/List;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
	public void addSplashes(List<Component> splashes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
		if (BotaniaConfig.client() != null && BotaniaConfig.client().splashesEnabled()) {
			this.splashes = new ArrayList<>(this.splashes);
			this.splashes.add(Component.literal("Do not feed bread to elves!"));
		}
	}
}
