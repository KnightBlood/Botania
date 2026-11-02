package vazkii.botania.common.item.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import vazkii.botania.common.block.TooltipBlock;

import java.util.function.Consumer;

/**
 * A {@link BlockItem} that forwards item tooltip requests to its block, if the block implements {@link TooltipBlock}.
 * <p>
 * 26.1 removed {@code Block#appendHoverText}, so block-provided tooltips have to come from the item side.
 */
public class TooltipBlockItem extends BlockItem {
	public TooltipBlockItem(Block block, Item.Properties properties) {
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, context, display, tooltip, flags);
		if (getBlock() instanceof TooltipBlock tooltipBlock) {
			tooltipBlock.appendHoverText(stack, context, tooltip, flags);
		}
	}
}
