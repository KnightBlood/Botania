/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.flower.generating.NarslimmusBlockEntity;
import vazkii.botania.common.helper.ItemNBTHelper;

public class SlimeInABottleItem extends Item {
	public static final String TAG_ACTIVE = "active";

	public SlimeInABottleItem(Properties builder) {
		super(builder);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		if (!world.isClientSide()) {
			boolean slime = NarslimmusBlockEntity.isSlimeChunk(world, entity.blockPosition());
			ItemNBTHelper.setBoolean(stack, TAG_ACTIVE, slime);
		}
	}
}
