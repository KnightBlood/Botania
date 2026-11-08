/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import vazkii.botania.api.item.AncientWillContainer;

import java.util.Locale;
import java.util.function.Consumer;

public class AncientWillItem extends Item {
	public final AncientWillContainer.AncientWillType type;

	public AncientWillItem(AncientWillContainer.AncientWillType variant, Properties props) {
		super(props);
		this.type = variant;
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
		builder.accept(Component.translatable("botaniamisc.craftToAddWill").withStyle(ChatFormatting.GREEN));
		builder.accept(Component.translatable("botania.armorset.will_" + type.name().toLowerCase(Locale.ROOT) + ".shortDesc").withStyle(ChatFormatting.GRAY));
	}
}
