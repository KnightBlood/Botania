/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.elementium;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.Item.Properties;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.handler.PixieHandler;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelSwordItem;

public class ElementiumSwordItem extends ManasteelSwordItem {

	public ElementiumSwordItem(Properties props) {
		// 26.1: getDefaultAttributeModifiers(ItemStack) is gone, so the pixie chance modifier is
		// merged into the item's attribute modifiers component at construction time instead.
		super(BotaniaAPI.instance().getElementiumItemTier(), 3, -2.4F,
				attrs -> attrs.withModifierAdded(
						PixieHandler.PIXIE_SPAWN_CHANCE,
						PixieHandler.makeModifier(EquipmentSlot.MAINHAND, "Sword modifier", 0.05),
						EquipmentSlotGroup.MAINHAND),
				props);
	}

}
