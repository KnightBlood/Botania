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
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.mana.ManaPoolBlockEntity;
import vazkii.botania.common.block.block_entity.mana.ManaPumpBlockEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.xplat.XplatAbstractions;

public class ManaPoolMinecartEntity extends AbstractMinecart {
	private static final int TRANSFER_RATE = 10000;
	private static final String TAG_MANA = "mana";
	private static final EntityDataAccessor<Integer> MANA = SynchedEntityData.defineId(ManaPoolMinecartEntity.class, EntityDataSerializers.INT);

	public ManaPoolMinecartEntity(EntityType<ManaPoolMinecartEntity> type, Level world) {
		super(type, world);
	}

	public ManaPoolMinecartEntity(Level world, double x, double y, double z) {
		super(BotaniaEntities.POOL_MINECART, world, x, y, z);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder entityData) {
		super.defineSynchedData(entityData);
		entityData.define(MANA, 0);
	}

	@NotNull
	@Override
	public BlockState getDisplayBlockState() {
		return BotaniaBlocks.manaPool.defaultBlockState();
	}

	@Override
	protected boolean canAddPassenger(Entity passenger) {
		return false;
	}

	@Override
	protected Vec3 applyNaturalSlowdown(Vec3 movement) {
		return movement.multiply(0.98F, 0.0, 0.98F);
	}

	@NotNull
	@Override
	public ItemStack getPickResult() {
		return new ItemStack(BotaniaItems.poolMinecart);
	}

	@Override
	public int getDefaultDisplayOffset() {
		return 8;
	}

	@Override
	public void tick() {
		super.tick();

		if (level().isClientSide()) {
			double particleChance = 1F - (double) getMana() / (double) ManaPoolBlockEntity.MAX_MANA * 0.1;
			int color = ManaPoolBlockEntity.PARTICLE_COLOR;
			float red = (color >> 16 & 0xFF) / 255F;
			float green = (color >> 8 & 0xFF) / 255F;
			float blue = (color & 0xFF) / 255F;
			double x = Mth.floor(getX());
			double y = Mth.floor(getY());
			double z = Mth.floor(getZ());
			if (Math.random() > particleChance) {
				WispParticleData data = WispParticleData.wisp((float) Math.random() / 3F, red, green, blue, 2F);
				level().addParticle(data, x + 0.3 + Math.random() * 0.5, y + 0.85 + Math.random() * 0.25, z + Math.random(), 0, (float) Math.random() / 25F, 0);
			}
		}
	}

	@Override
	protected void moveAlongTrack(ServerLevel level) {
		super.moveAlongTrack(level);

		BlockPos pos = this.getCurrentBlockPosOrRailBelow();
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos pumpPos = pos.relative(dir);
			BlockState pumpState = level.getBlockState(pumpPos);
			if (pumpState.is(BotaniaBlocks.pump)
					&& level().getBlockEntity(pumpPos) instanceof ManaPumpBlockEntity pump) {
				BlockPos poolPos = pumpPos.relative(dir);
				var receiver = XplatAbstractions.INSTANCE.findManaReceiver(level, poolPos, dir.getOpposite());

				if (receiver instanceof ManaPool pool) {
					Direction pumpDir = pumpState.getValue(BlockStateProperties.HORIZONTAL_FACING);
					boolean did = false;
					boolean can = false;

					if (pumpDir == dir) { // Pool -> Cart
						can = true;

						if (!pump.hasRedstone) {
							int cartMana = getMana();
							int poolMana = pool.getCurrentMana();
							int transfer = Math.min(TRANSFER_RATE, poolMana);
							int actualTransfer = Math.min(ManaPoolBlockEntity.MAX_MANA - cartMana, transfer);
							if (actualTransfer > 0) {
								pool.receiveMana(-transfer);
								setMana(cartMana + actualTransfer);
								did = true;
							}
						}
					} else if (pumpDir == dir.getOpposite()) { // Cart -> Pool
						can = true;

						if (!pump.hasRedstone && !pool.isFull()) {
							int cartMana = getMana();
							int transfer = Math.min(TRANSFER_RATE, cartMana);
							if (transfer > 0) {
								pool.receiveMana(transfer);
								setMana(cartMana - transfer);
								did = true;
							}
						}
					}

					if (did) {
						pump.hasCart = true;
						pump.setActive(true);
					}

					if (can) {
						pump.hasCartOnTop = true;
						pump.comparator = (int) ((double) getMana() / (double) ManaPoolBlockEntity.MAX_MANA * 15); // different from ManaPoolBlockEntity.calculateComparatorLevel, kept for compatibility
					}

				}
			}
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		output.putInt(TAG_MANA, getMana());
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		setMana(input.getIntOr(TAG_MANA, 0));
	}

	@Override
	public Item getDropItem() {
		return BotaniaItems.poolMinecart;
	}

	@SoftImplement("IForgeMinecart")
	public int getComparatorLevel() {
		return ManaPoolBlockEntity.calculateComparatorLevel(getMana(), ManaPoolBlockEntity.MAX_MANA);
	}

	public int getMana() {
		return entityData.get(MANA);
	}

	public void setMana(int mana) {
		entityData.set(MANA, mana);
	}

}
