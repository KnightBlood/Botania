/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FloralFertilizerItem extends Item {
	private static final int RANGE = 3;

	public FloralFertilizerItem(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		if (!world.isClientSide()) {
			List<Holder<Block>> flowersTag = tagList(BotaniaTags.Blocks.MYSTICAL_FLOWERS);
			List<Holder<Block>> mushroomsTag = tagList(BotaniaTags.Blocks.SHIMMERING_MUSHROOMS);
			boolean flowersAvailable = !flowersTag.isEmpty();
			boolean mushroomsAvailable = !mushroomsTag.isEmpty();

			List<BlockPos> validCoords = new ArrayList<>();

			for (BlockPos candidatePos : BlockPos.betweenClosed(
					pos.getX() - RANGE, pos.getY() - 2, pos.getZ() - RANGE,
					pos.getX() + RANGE, pos.getY() + 2, pos.getZ() + RANGE)) {
				if (!world.isInWorldBounds(candidatePos) || !world.isEmptyBlock(candidatePos)) {
					continue;
				}
				BlockState belowState = world.getBlockState(candidatePos.below());
				if (flowersAvailable && canPlaceFlower(belowState, world) || mushroomsAvailable && canPlaceMushroom(belowState)) {
					validCoords.add(candidatePos.immutable());
				}
			}

			int petalCount = world.getRandom().nextIntBetweenInclusive(5, 7);
			while (petalCount > 0 && !validCoords.isEmpty()) {
				petalCount--;
				BlockPos coords = validCoords.get(world.getRandom().nextInt(validCoords.size()));
				validCoords.remove(coords);
				BlockState belowState = world.getBlockState(coords.below());
				boolean tryPlaceFlower = flowersAvailable && canPlaceFlower(belowState, world);
				boolean tryPlaceMushroom = mushroomsAvailable && canPlaceMushroom(belowState);

				Optional<Holder<Block>> toPlace;
				if (tryPlaceMushroom && (!tryPlaceFlower || world.getRandom().nextInt(3) == 0)) {
					toPlace = Optional.of(mushroomsTag.get(world.getRandom().nextInt(mushroomsTag.size())));
				} else if (tryPlaceFlower) {
					toPlace = Optional.of(flowersTag.get(world.getRandom().nextInt(flowersTag.size())));
					petalCount--;
				} else {
					continue;
				}

				if (toPlace.isPresent() && !toPlace.get().value().defaultBlockState().isAir()) {
					world.setBlockAndUpdate(coords, toPlace.get().value().defaultBlockState());
				}
			}
			ctx.getItemInHand().shrink(1);
		} else {
			for (int i = 0; i < 15; i++) {
				double x = pos.getX() - RANGE + world.getRandom().nextInt(RANGE * 2 + 1) + Math.random();
				double y = pos.getY() + 1;
				double z = pos.getZ() - RANGE + world.getRandom().nextInt(RANGE * 2 + 1) + Math.random();
				float red = (float) Math.random();
				float green = (float) Math.random();
				float blue = (float) Math.random();
				WispParticleData data = WispParticleData.wisp(0.15F + (float) Math.random() * 0.25F, red, green, blue, 1);
				world.addParticle(data, x, y, z, 0, (float) Math.random() * 0.1F - 0.05F, 0);
			}
		}

		return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER);
	}

	private static List<Holder<Block>> tagList(TagKey<Block> tag) {
		List<Holder<Block>> list = new ArrayList<>();
		BuiltInRegistries.BLOCK.getTagOrEmpty(tag).forEach(list::add);
		return list;
	}

	private static boolean canPlaceMushroom(BlockState belowState) {
		return belowState.is(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT);
	}

	private static boolean canPlaceFlower(BlockState belowState, Level world) {
		return belowState.is(BlockTags.DIRT) && !world.environmentAttributes().getDimensionValue(net.minecraft.world.attribute.EnvironmentAttributes.WATER_EVAPORATES);
	}
}
