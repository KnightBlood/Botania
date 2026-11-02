/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.item.SparkEntity;
import vazkii.botania.api.mana.ManaCollisionGhost;

public abstract class SparkBaseEntity extends Entity implements SparkEntity, ManaCollisionGhost {
	private static final String TAG_INVIS = "invis";
	private static final String TAG_NETWORK = "network";
	private static final EntityDataAccessor<Integer> NETWORK = SynchedEntityData.defineId(SparkBaseEntity.class, EntityDataSerializers.INT);

	public SparkBaseEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@NotNull @Override
	public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
		// sparks are invulnerable, matching pre-1.21 Entity#hurt default behaviour
		return false;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		entityData.define(NETWORK, 0);
	}

	@Override
	public BlockPos getAttachPos() {
		int x = Mth.floor(getX());
		int y = Mth.floor(getY() - 1);
		int z = Mth.floor(getZ());
		return new BlockPos(x, y, z);
	}

	@Override
	public DyeColor getNetwork() {
		return DyeColor.byId(entityData.get(NETWORK));
	}

	@Override
	public void setNetwork(DyeColor color) {
		entityData.set(NETWORK, color.getId());
	}

	@Override
	public boolean isPickable() {
		return true;
	}

	@Override
	public boolean canBeHitByProjectile() {
		return false;
	}

	@Override
	public Behaviour getGhostBehaviour() {
		return Behaviour.RUN_ALL;
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		setInvisible(input.getBooleanOr(TAG_INVIS, false));
		setNetwork(DyeColor.byId(input.getIntOr(TAG_NETWORK, 0)));
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putBoolean(TAG_INVIS, isInvisible());
		output.putInt(TAG_NETWORK, getNetwork().getId());
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
		return new ClientboundAddEntityPacket(this, serverEntity);
	}
}
