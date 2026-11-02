package vazkii.botania.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.BotaniaStats;
import vazkii.botania.common.PlayerAccess;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.ResoluteIvyItem;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerAccess {
	protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
		super(type, level);
	}

	@Unique
	private LivingEntity terraWillCritTarget;

	@Shadow
	public abstract void awardStat(Identifier stat, int i);

	@Unique
	private double botania$lastRideX, botania$lastRideY, botania$lastRideZ;

	/**
	 * Updates the distance by luminizer stat
	 * <p>
	 * 26.1: vanilla's {@code checkRidingStatistics} no longer exists; track ridden distance
	 * ourselves in {@code rideTick} (called every tick while riding any vehicle) and credit the
	 * stat only when the vehicle is the Player Mover, matching the 1.20.1 behavior.
	 */
	@Inject(method = "rideTick", at = @At("TAIL"))
	private void trackLuminizerTravel(CallbackInfo ci) {
		double dx = this.getX() - botania$lastRideX;
		double dy = this.getY() - botania$lastRideY;
		double dz = this.getZ() - botania$lastRideZ;
		botania$lastRideX = this.getX();
		botania$lastRideY = this.getY();
		botania$lastRideZ = this.getZ();
		Entity mount = this.getVehicle();
		if (mount != null && mount.getType() == BotaniaEntities.PLAYER_MOVER) {
			int cm = Mth.floor(Math.sqrt(dx * dx + dy * dy + dz * dz) * 100.0);
			if (cm > 0) {
				awardStat(BotaniaStats.LUMINIZER_ONE_CM, cm);
			}
		}
	}

	@Override
	public void botania$setCritTarget(LivingEntity entity) {
		this.terraWillCritTarget = entity;
	}

	// Perform damage source modifications and apply the potion effects.
	@ModifyArg(
		method = "attack",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z")
	)
	private DamageSource onDamageTarget(DamageSource source, float amount) {
		if (this.terraWillCritTarget != null) {
			DamageSource newSource = TerrasteelHelmItem.onEntityAttacked(source, amount, (Player) (Object) this, terraWillCritTarget);
			this.terraWillCritTarget = null;
			return newSource;
		}
		return source;
	}

	// Clear the entity on any return after the capture.
	@Inject(
		at = @At(value = "RETURN"), method = "attack",
		slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getKnockback(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;)F"))
	)
	private void clearCritTarget(CallbackInfo ci) {
		this.terraWillCritTarget = null;
	}

	@Inject(
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"),
		method = "dropEquipment"
	)
	private void captureIvyDrops(CallbackInfo ci) {
		ResoluteIvyItem.keepDropsOnDeath((Player) (Object) this);
	}
}
