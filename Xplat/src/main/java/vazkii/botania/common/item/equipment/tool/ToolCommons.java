/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.helper.EnchHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumAxeItem;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumHoeItem;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumPickaxeItem;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumShovelItem;
import vazkii.botania.common.item.equipment.tool.elementium.ElementiumSwordItem;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelAxeItem;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelHoeItem;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelPickaxeItem;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelShovelItem;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelSwordItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;

import java.util.function.Predicate;

public final class ToolCommons {

	private static boolean recCall = false;

	/**
	 * Consumes as much mana as possible, returning the amount of damage that couldn't be paid with mana
	 */
	public static int damageItemIfPossible(ItemStack stack, int amount, LivingEntity entity, int manaPerDamage) {
		if (!(entity instanceof Player player) || amount == 0) {
			return amount;
		}

		final int unbreaking = EnchHelper.getItemEnchantmentLevel(stack, Enchantments.UNBREAKING);

		while (amount > 0) {
			if (ManaItemHandler.instance().requestManaExactForTool(stack, player, manaPerDamage, false)) {
				if (entity.level().getRandom().nextInt(unbreaking + 1) == 0) {
					ManaItemHandler.instance().requestManaExactForTool(stack, player, manaPerDamage, true);
				}
				amount--;
			} else {
				break;
			}
		}

		return amount;
	}

	public static void removeBlocksInIteration(Player player, ItemStack stack, Level world, BlockPos centerPos,
			Vec3i startDelta, Vec3i endDelta, Predicate<BlockState> filter) {
		if (recCall) {
			return;
		}

		recCall = true;
		try {
			for (BlockPos iterPos : BlockPos.betweenClosed(centerPos.offset(startDelta),
					centerPos.offset(endDelta))) {
				// skip original block space, vanilla code will handle it
				if (iterPos.equals(centerPos)) {
					continue;
				}
				removeBlockWithDrops(player, stack, world, iterPos, filter);
			}
		} finally {
			recCall = false;
		}
	}

	/**
	 * NB: Cannot be called in a call chain leading from PlayerInteractionManager.tryHarvestBlock
	 * without additional protection like {@link #recCall} in {@link #removeBlocksInIteration}
	 * or {@link vazkii.botania.common.item.relic.RingOfLokiItem#breakOnAllCursors},
	 * since this method calls that method also and would lead to an infinite loop.
	 */
	public static void removeBlockWithDrops(Player player, ItemStack stack, Level world, BlockPos pos,
			Predicate<BlockState> filter) {
		if (!world.hasChunkAt(pos)) {
			return;
		}

		BlockState blockstate = world.getBlockState(pos);
		boolean unminable = blockstate.getDestroyProgress(player, world, pos) == 0;

		if (!world.isClientSide() && !unminable && filter.test(blockstate) && !blockstate.isAir()) {
			ItemStack save = player.getMainHandItem();
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			((ServerPlayer) player).connection.send(
					new ClientboundLevelEventPacket(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(blockstate), false));
			((ServerPlayer) player).gameMode.destroyBlock(pos);
			player.setItemInHand(InteractionHand.MAIN_HAND, save);
		}
	}

	public static int getToolPriority(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}

		Item item = stack.getItem();
		// 26.1: pickaxes are plain Items now; identify diggers via the TOOL data component
		if (stack.get(DataComponents.TOOL) == null) {
			return 0;
		}

		int materialLevel = 0;
		if (item instanceof TerraShattererItem || item instanceof TerraTruncatorItem || item instanceof StarcallerItem) {
			materialLevel = 20;
		} else if (item instanceof ElementiumPickaxeItem || item instanceof ElementiumShovelItem
				|| item instanceof ElementiumAxeItem || item instanceof ElementiumHoeItem
				|| item instanceof ElementiumSwordItem) {
			materialLevel = 11;
		} else if (item instanceof ManasteelPickaxeItem || item instanceof ManasteelShovelItem
				|| item instanceof ManasteelAxeItem || item instanceof ManasteelHoeItem
				|| item instanceof ManasteelSwordItem) {
			materialLevel = 10;
		}

		int modifier = 0;
		if (stack.is(BotaniaItems.terraPick)) {
			modifier = TerraShattererItem.getLevel(stack);
		}

		int efficiency = EnchHelper.getItemEnchantmentLevel(stack, Enchantments.EFFICIENCY);
		return materialLevel * 100 + modifier * 10 + efficiency;
	}

	public static BlockHitResult raytraceFromEntity(Entity e, double distance, boolean fluids) {
		return (BlockHitResult) e.pick(distance, 1, fluids);
	}
}
