/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.ForcePushHelper;
import vazkii.botania.common.item.WandOfTheForestItem;
import vazkii.botania.common.item.lens.ForceLens;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ForceRelayBlock extends BotaniaBlock {

	public final Map<UUID, GlobalPos> activeBindingAttempts = new HashMap<>();

	public ForceRelayBlock(Properties builder) {
		super(builder.pushReaction(PushReaction.PUSH_ONLY));
	}

	@Override
	protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean isMoving) {
		BlockState newState = world.getBlockState(pos);
		if (!world.isClientSide()) {
			var data = WorldData.get(world);

			Direction movementContextDirection = ForcePushHelper.getMovementContextDirection();
			if (isMoving && (movementContextDirection != null || newState.is(Blocks.MOVING_PISTON))) {
				var pistonDirection = movementContextDirection != null
						? movementContextDirection
						: newState.getValue(MovingPistonBlock.FACING);
				// if being moved as part of a retracting sticky piston's block structure, reverse movement direction
				var moveDirection = ForcePushHelper.isExtendingMovementContext() ? pistonDirection : pistonDirection.getOpposite();

				var destPos = data.mapping.get(pos);
				if (destPos != null) {
					BlockPos newSrcPos = pos.relative(moveDirection);

					{
						// Move source side of our binding along
						data.mapping.remove(pos);
						data.mapping.put(newSrcPos, destPos);
						data.setDirty();
					}

					if (!newState.is(Blocks.MOVING_PISTON) || newState.getValue(MovingPistonBlock.TYPE) == PistonType.DEFAULT) {
						// Move the actual bound blocks
						if (ForceLens.moveBlocks(world, destPos.relative(moveDirection.getOpposite()), moveDirection, pos)) {
							// Move dest side of our binding
							data.mapping.put(newSrcPos, data.mapping.get(newSrcPos).relative(moveDirection));
						}
					}
				}
			} else {
				if (data.mapping.remove(pos) != null) {
					data.setDirty();
				}
			}
		}
	}

	public boolean onUsedByWand(Player player, ItemStack stack, Level world, BlockPos pos) {
		if (world.isClientSide()) {
			return false;
		}

		if (player == null || player.isShiftKeyDown()) {
			world.destroyBlock(pos, true);
		} else {
			GlobalPos clicked = GlobalPos.of(world.dimension(), pos.immutable());
			if (WandOfTheForestItem.getBindMode(stack)) {
				activeBindingAttempts.put(player.getUUID(), clicked);
				world.playSound(null, pos, BotaniaSounds.ding, SoundSource.BLOCKS, 0.5F, 1F);
			} else {
				var data = WorldData.get(world);
				if (XplatAbstractions.INSTANCE.isDevEnvironment()) {
					BotaniaAPI.LOGGER.info("PistonRelay pairs");
					for (var e : data.mapping.entrySet()) {
						BotaniaAPI.LOGGER.info("{} -> {}", e.getKey(), e.getValue());
					}
				}
				BlockPos dest = data.mapping.get(pos);
				if (dest != null) {
					XplatAbstractions.INSTANCE.sendToNear(world, pos, new BotaniaEffectPacket(EffectType.PARTICLE_BEAM,
							pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
							dest.getX(), dest.getY(), dest.getZ()));
				}
			}
		}

		return true;
	}

	public static class WorldData extends SavedData {

		public static final Codec<WorldData> CODEC = BlockPos.CODEC.listOf().xmap(WorldData::fromList, WorldData::toList);
		// 4-arg ctor for both loaders; no datafix applies to mod saved data, so pass null (NeoForge's
		// 3-arg convenience overload does exactly this internally, but vanilla only has the 4-arg one).
		private static final SavedDataType<WorldData> TYPE = new SavedDataType<>(
				prefix("piston_relay_pairs"), WorldData::new, CODEC, null);
		public final Map<BlockPos, BlockPos> mapping = new HashMap<>();

		public WorldData() {}

		private static WorldData fromList(List<BlockPos> list) {
			WorldData data = new WorldData();
			for (int i = 0; i + 1 < list.size(); i += 2) {
				data.mapping.put(list.get(i), list.get(i + 1));
			}
			return data;
		}

		private static List<BlockPos> toList(WorldData data) {
			List<BlockPos> list = new ArrayList<>(data.mapping.size() * 2);
			for (Map.Entry<BlockPos, BlockPos> e : data.mapping.entrySet()) {
				list.add(e.getKey());
				list.add(e.getValue());
			}
			return list;
		}

		public static WorldData get(Level world) {
			return ((ServerLevel) world).getDataStorage().computeIfAbsent(TYPE);
		}
	}
}
