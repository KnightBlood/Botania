/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import vazkii.botania.common.item.BotaniaItems;

@Mixin(ItemStack.class)
public abstract class ItemStackFabricMixin {
	@Shadow
	public abstract Item getItem();

	// 26.1: ItemStack.is(Item) moved to TypedInstance.is(T), a default interface method
	// that lives outside the ItemStack class, so injecting into it is impossible.
	// Instead we add an override of is(Object) (the erased is(T) signature) directly to
	// ItemStack, replicating TypedInstance's default impl plus the Botania shears special case.
	public boolean is(Object obj) {
		if (obj == Items.SHEARS) {
			Item item = getItem();
			if (item == BotaniaItems.manasteelShears || item == BotaniaItems.elementiumShears) {
				return true;
			}
		}
		return getItem() == obj;
	}
}
