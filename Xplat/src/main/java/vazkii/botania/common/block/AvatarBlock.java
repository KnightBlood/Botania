/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.block_entity.AvatarBlockEntity;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.mana.ManaPrismBlock;
import vazkii.botania.xplat.XplatAbstractions;

public class AvatarBlock extends BotaniaWaterloggedBlock implements EntityBlock {

	private static final VoxelShape X_AABB = box(5, 0, 3.5, 11, 17, 12.5);
	private static final VoxelShape Z_AABB = box(3.5, 0, 5, 12.5, 17, 11);

	@SuppressWarnings("this-escape") // standard vanilla pattern: registerDefaultState in ctor
	protected AvatarBlock(Properties builder) {
		super(builder);
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
		if (state.getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis() == Direction.Axis.X) {
			return X_AABB;
		} else {
			return Z_AABB;
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.HORIZONTAL_FACING);
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
		return useItemOn(itemStack, world, player, hand, hit);
	}

	private InteractionResult useItemOn(ItemStack itemStack, Level world, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockPos pos = hit.getBlockPos();

		if (world.getBlockEntity(pos) instanceof AvatarBlockEntity avatar) {
			ItemStack stackOnAvatar = avatar.getItemHandler().getItem(0);
			ItemStack stackOnPlayer = player.getItemInHand(hand);
			if (!stackOnAvatar.isEmpty()) {
				avatar.getItemHandler().setItem(0, ItemStack.EMPTY);
				player.getInventory().placeItemBackInInventory(stackOnAvatar);
				return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
			} else if (!stackOnPlayer.isEmpty() &&
					XplatAbstractions.INSTANCE.findAvatarWieldable(stackOnPlayer) != null) {
				avatar.getItemHandler().setItem(0, stackOnPlayer.split(1));
				return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
			}
		}

		return InteractionResult.PASS;
	}

	@NotNull
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
	}

	@NotNull
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@NotNull
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new AvatarBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, BotaniaBlockEntities.AVATAR, AvatarBlockEntity::commonTick);
	}

	@NotNull
	@Override
	public BlockState mirror(@NotNull BlockState state, Mirror mirror) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, mirror.mirror(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}

	@NotNull
	@Override
	public BlockState rotate(@NotNull BlockState state, Rotation rot) {
		return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rot.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource rand) {
		if (world.hasNeighborSignal(pos)) {
			ManaPrismBlock.redstoneParticlesInShape(state, world, pos, rand);
		}
	}
}
