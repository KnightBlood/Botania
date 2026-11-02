/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import vazkii.botania.common.item.equipment.bauble.BenevolentGoddessCharmItem;

/**
 * 26.1: {@code Explosion} became an interface and the old {@code toBlow}/{@code clearToBlow}
 * machinery is gone; the concrete implementation is {@link ServerExplosion}. Mirrors the
 * NeoForge-side {@code ExplosionEvent.Detonate} handling in ForgeCommonInitializer: right after
 * entity damage is dealt, clear the affected-blocks list so neither block destruction nor fire
 * is applied (matches the 1.20.1 {@code clearToBlow} semantics, and keeps the mana cost at one
 * check per explosion).
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionFabricMixin {
	@Shadow
	@Final
	private ServerLevel level;

	@Shadow
	@Final
	private Vec3 center;

	@Inject(method = "explode", at = @At(value = "INVOKE", shift = At.Shift.AFTER,
			target = "Lnet/minecraft/world/level/ServerExplosion;hurtEntities()V"))
	private void botania$protectBlocks(CallbackInfoReturnable<Integer> cir, @Local List<BlockPos> positions) {
		if (BenevolentGoddessCharmItem.shouldProtectExplosion(level, center)) {
			positions.clear();
		}
	}
}
