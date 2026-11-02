/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.block.flower.functional.LooniumBlockEntity;
import vazkii.botania.common.brew.effect.SoulCrossMobEffect;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.AssemblyHaloItem;
import vazkii.botania.common.item.equipment.bauble.CharmOfTheDivaItem;
import vazkii.botania.common.item.equipment.bauble.CloakOfVirtueItem;
import vazkii.botania.common.item.equipment.bauble.SojournersSashItem;
import vazkii.botania.common.item.relic.RingOfOdinItem;
import vazkii.botania.common.item.rod.ShadedMesaRodItem;

/**
 * 26.1: vanilla removed {@code Player.isInvulnerableTo(DamageSource)} and
 * {@code Player.hurt(DamageSource, float)}; both entry points now live on
 * {@link LivingEntity} ({@code isInvulnerableTo(ServerLevel, DamageSource)} and
 * {@code hurtServer(ServerLevel, DamageSource, float)}), so the Odin Ring and
 * damage-taken hooks moved here from the Player mixin. Player-only behavior is
 * guarded with {@code instanceof Player}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFabricMixin extends Entity {

	public LivingEntityFabricMixin(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Shadow
	public abstract ItemStack getItemInHand(InteractionHand hand);

	// 26.1: dropFromLootTable now carries the ServerLevel, and spawnAtLocation takes it too.
	@Inject(at = @At("HEAD"), cancellable = true, method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;Z)V")
	private void dropLoonium(ServerLevel level, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
		var self = (LivingEntity) (Object) this;
		LooniumBlockEntity.dropLooniumItems(self, stack -> {
			if (!stack.isEmpty()) {
				self.spawnAtLocation(level, stack);
			}
			ci.cancel();
		});
	}

	/**
	 * Applies soul cross effect when being killed
	 */
	@Inject(at = @At("RETURN"), method = "createWitherRose")
	private void healKiller(@Nullable LivingEntity adversary, CallbackInfo ci) {
		if (!level().isClientSide() && adversary != null) {
			SoulCrossMobEffect.onEntityKill((LivingEntity) (Object) this, adversary);
		}

	}

	@Inject(at = @At("HEAD"), method = "swing(Lnet/minecraft/world/InteractionHand;Z)V", cancellable = true)
	private void onSwing(InteractionHand hand, boolean bl, CallbackInfo ci) {
		ItemStack stack = getItemInHand(hand);
		LivingEntity self = (LivingEntity) (Object) this;
		if (!level().isClientSide()) {
			if (stack.getItem() instanceof AssemblyHaloItem halo && halo.onEntitySwing(stack, self)) {
				ci.cancel();
			} else if (stack.getItem() instanceof ShadedMesaRodItem rod) {
				rod.onEntitySwing(stack, self);
			}
		}
	}

	@Inject(at = @At("RETURN"), method = "jumpFromGround")
	private void onJump(CallbackInfo ci) {
		SojournersSashItem.onPlayerJump((LivingEntity) (Object) this);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F", ordinal = 0), method = "actuallyHurt")
	private void onActuallyHurt(ServerLevel level, DamageSource damageSource, float damageAmount, CallbackInfo ci) {
		if (damageSource.getDirectEntity() instanceof Player player) {
			CharmOfTheDivaItem.onEntityDamaged(player, (LivingEntity) (Object) this);
		}
	}

	/**
	 * Makes the player invulnerable to certain damage when wearing an Odin Ring
	 */
	@Inject(at = @At("HEAD"), method = "isInvulnerableTo", cancellable = true)
	private void botania$odinRing(ServerLevel level, DamageSource src, CallbackInfoReturnable<Boolean> cir) {
		if ((Object) this instanceof Player player && RingOfOdinItem.onPlayerAttacked(player, src)) {
			cir.setReturnValue(true);
		}
	}

	/**
	 * Performs many reactions when being hit. Runs before all of
	 * {@code hurtServer}'s own processing, matching the 1.20.1 injection point
	 * (the {@code super.hurt} call at the end of {@code Player.hurt}).
	 */
	@ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private float botania$onHurt(float amount, ServerLevel level, DamageSource src) {
		if ((Object) this instanceof Player self) {
			Container worn = EquipmentHandler.getAllWorn(self);
			for (int i = 0; i < worn.getContainerSize(); i++) {
				ItemStack stack = worn.getItem(i);
				if (stack.getItem() instanceof CloakOfVirtueItem cloak) {
					amount = cloak.onPlayerDamage(self, src, amount);
				}
			}

			// Should really make a separate inject for this, but putting it here works too
			PixieHandler.onDamageTaken(self, src);
		}
		return amount;
	}
}
