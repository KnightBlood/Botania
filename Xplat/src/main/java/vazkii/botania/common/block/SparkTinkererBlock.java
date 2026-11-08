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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.block.block_entity.SparkTinkererBlockEntity;
import vazkii.botania.common.item.SparkAugmentItem;

public class SparkTinkererBlock extends BotaniaWaterloggedBlock implements EntityBlock {

	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

	@SuppressWarnings("this-escape") // standard vanilla pattern: registerDefaultState in ctor
	public SparkTinkererBlock(Properties builder) {
		super(builder);
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.POWERED, true));
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.POWERED);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, @Nullable Orientation orientation, boolean isMoving) {
		boolean power = world.getBestNeighborSignal(pos) > 0;
		boolean powered = state.getValue(BlockStateProperties.POWERED);

		if (power && !powered) {
			if (world.getBlockEntity(pos) instanceof SparkTinkererBlockEntity tinkerer) {
				tinkerer.doSwap();
			}
			world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, true), Block.UPDATE_INVISIBLE);
		} else if (!power && powered) {
			world.setBlock(pos, state.setValue(BlockStateProperties.POWERED, false), Block.UPDATE_INVISIBLE);
		}
	}

	@Override
	protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level world, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hit) {
		return useItemOn(itemStack, world, player, hand, hit);
	}

	private InteractionResult useItemOn(ItemStack itemStack, Level world, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockPos pos = hit.getBlockPos();

		if (!(world.getBlockEntity(pos) instanceof SparkTinkererBlockEntity changer)) {
			return InteractionResult.FAIL;
		}
		ItemStack pstack = player.getItemInHand(hand);
		ItemStack cstack = changer.getItemHandler().getItem(0);
		if (!cstack.isEmpty()) {
			changer.getItemHandler().setItem(0, ItemStack.EMPTY);
			player.getInventory().placeItemBackInInventory(cstack);
			return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
		} else if (!pstack.isEmpty() && pstack.getItem() instanceof SparkAugmentItem) {
			changer.getItemHandler().setItem(0, pstack.split(1));
			changer.setChanged();

			return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
		}

		return InteractionResult.PASS;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
		if (world.getBlockEntity(pos) instanceof SparkTinkererBlockEntity changer) {
			ItemStack stack = changer.getItemHandler().getItem(0);
			if (!stack.isEmpty() && stack.getItem() instanceof SparkAugmentItem upgrade) {
				return upgrade.type.ordinal() + 1;
			}
		}
		return 0;
	}

	@NotNull
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new SparkTinkererBlockEntity(pos, state);
	}

}
