/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.CorporeaSparkEntity;
import vazkii.botania.common.helper.EntityHelper;
import vazkii.botania.common.impl.corporea.DummyCorporeaNode;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.List;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

public class CorporeaSparkItem extends Item {

	public CorporeaSparkItem(Properties props) {
		super(props);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		ItemStack otherHandStack = ItemStack.EMPTY;
		if (ctx.getPlayer() != null) {
			otherHandStack = ctx.getPlayer().getItemInHand(EntityHelper.otherHand(ctx.getHand()));
			if (ctx.getPlayer().isCreative()) {
				otherHandStack = otherHandStack.copy();
			}
		}
		return attachSpark(ctx.getLevel(), ctx.getClickedPos(), ctx.getItemInHand(), otherHandStack)
				? (ctx.getLevel().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER)
				: InteractionResult.PASS;
	}

	private static boolean canPlace(Level world, CorporeaSparkEntity spark) {
		return world.getBlockState(spark.getAttachPos()).is(BotaniaTags.Blocks.CORPOREA_SPARK_OVERRIDE)
				|| !(spark.getSparkNode() instanceof DummyCorporeaNode);
	}

	public static boolean attachSpark(Level world, BlockPos pos, ItemStack stack, ItemStack otherHandStack) {
		CorporeaSparkEntity spark = BotaniaEntities.CORPOREA_SPARK.create(world, EntitySpawnReason.TRIGGERED);
		if (stack.is(BotaniaItems.corporeaSparkMaster)) {
			spark.setMaster(true);
		}
		if (stack.is(BotaniaItems.corporeaSparkCreative)) {
			spark.setCreative(true);
		}
		spark.setPos(pos.getX() + 0.5, pos.getY() + 1.25, pos.getZ() + 0.5);
		if (otherHandStack.getItem() instanceof DyeItem dye) {
			otherHandStack.shrink(1);
			spark.setNetwork(otherHandStack.get(net.minecraft.core.component.DataComponents.DYE));
		}

		if (canPlace(world, spark) && !CorporeaHelper.instance().doesBlockHaveSpark(world, pos)) {
			if (!world.isClientSide()) {
				world.addFreshEntity(spark);
				stack.shrink(1);
			}
			return true;
		}
		return false;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
		if (stack.is(BotaniaItems.corporeaSparkCreative)) {
			builder.accept(Component.translatable("botaniamisc.creativeSpark").withStyle(ChatFormatting.GRAY));
		}
	}
}
