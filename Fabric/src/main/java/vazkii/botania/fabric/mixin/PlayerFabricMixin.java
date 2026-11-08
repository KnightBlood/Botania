/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.PlayerAccess;
import vazkii.botania.common.block.block_entity.EnderOverseerBlockEntity;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.item.equipment.bauble.RingOfMagnetizationItem;
import vazkii.botania.common.item.equipment.bauble.SojournersSashItem;

@Mixin(Player.class)
public abstract class PlayerFabricMixin extends LivingEntity {

	protected PlayerFabricMixin(EntityType<? extends LivingEntity> entityType, Level world) {
		super(entityType, world);
	}

	@Shadow
	@Final
	private Inventory inventory;

	/**
	 * Registers the pixie spawn chance attribute on players.
	 * <p>
	 * {@code DefaultAttributes} initializes during bootstrap, before mod initializers run,
	 * so {@link PixieHandler#init()} is invoked here first: this is the earliest point the
	 * attribute holder is needed and the built-in registry is still writable on Fabric
	 * (later call sites hit the idempotent no-op).
	 */
	@Inject(at = @At("RETURN"), method = "createAttributes")
	private static void addPixieAttribute(CallbackInfoReturnable<AttributeSupplier.Builder> cir) {
		PixieHandler.init();
		cir.getReturnValue().add(PixieHandler.PIXIE_SPAWN_CHANCE);
	}

	/**
	 * Tells the magnet ring about item drops
	 */
	@Inject(at = @At("HEAD"), method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;")
	public void onDrop(ItemStack stack, boolean throwRandomly, CallbackInfoReturnable<ItemEntity> cir) {
		Level world = this.level();
		if (!stack.isEmpty() && !world.isClientSide()) {
			RingOfMagnetizationItem.onTossItem((Player) (Object) this);
		}
	}

	@Inject(at = @At("RETURN"), method = "tick")
	private void tickBeltTiara(CallbackInfo ci) {
		Player player = (Player) (Object) this;
		FlugelTiaraItem.updatePlayerFlyStatus(player);
		SojournersSashItem.tickBelt(player);
		if (!player.level().isClientSide()) {
			EnderOverseerBlockEntity.checkLookingAtEnderOverseer(player);
		}
	}

	// 26.1: the super call now targets Avatar (Player's new parent class); @ModifyArg failed
	// injection here for unknown reasons, so wrap the call with MixinExtras instead. The
	// fall distance passed to vanilla's mayFly check and FALL_ONE_CM stat keeps its
	// original value, matching the 1.20.1 @ModifyArg timing (modification only affects
	// the actual damage calculation).
	// TEMP-DISABLED for bytecode probe: @At INVOKE Avatar.causeFallDamage scans 0 targets at runtime
	// @WrapOperation(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Avatar;causeFallDamage(DLnet/minecraft/world/damagesource/DamageSource;)Z"))
	// private boolean cushionFall(double dist, float dmg, DamageSource src, Operation<Boolean> original) {
	// 	return original.call((double) SojournersSashItem.onPlayerFall((Player) (Object) this, (float) dist), dmg, src);
	// }
	//
	// @Inject(method = "causeFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Avatar;causeFallDamage(DLnet/minecraft/world/damagesource/DamageSource;)Z"))
	// private void cushionFallProbe(double dist, float dmg, DamageSource src, CallbackInfoReturnable<Boolean> cir) {
	// }

	// Multiply the damage on crit. Targets the first float LOAD after the crit check for the crit.
	// Stores the entity for further handling in the common Player mixin.
	@ModifyVariable(
		at = @At(value = "LOAD", ordinal = 0),
		slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;canCriticalAttack(Lnet/minecraft/world/entity/Entity;)Z")),
		method = "attack", ordinal = 0
	)
	private float onCritMul(float f, Entity target) {
		if (target instanceof LivingEntity living) {
			((PlayerAccess) this).botania$setCritTarget(living);
			return f * TerrasteelHelmItem.getCritDamageMult((Player) (Object) this);
		}
		return f;
	}
}
