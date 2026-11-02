package vazkii.botania.common.block;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.function.Consumer;

/**
 * Implemented by blocks that want to contribute lines to the tooltip of their {@code BlockItem}.
 * <p>
 * See {@link vazkii.botania.common.item.block.TooltipBlockItem}.
 */
public interface TooltipBlock {
	void appendHoverText(ItemStack stack, Item.TooltipContext context, Consumer<Component> tooltip, TooltipFlag flags);
}
