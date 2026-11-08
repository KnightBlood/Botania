/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.equipment.tool.manasteel.ManasteelSwordItem;

import java.util.function.Consumer;

public class SoulscribeItem extends ManasteelSwordItem {

	public SoulscribeItem(Properties props) {
		super(BotaniaAPI.instance().getManasteelItemTier(), 3, -1.25F, props);
	}

	@Override
	public void hurtEnemy(ItemStack stack, LivingEntity target, @NotNull LivingEntity attacker) {
		if (!target.level().isClientSide()
				&& target instanceof EnderMan
				&& attacker instanceof Player player) {
			target.hurt(player.damageSources().playerAttack(player), 20);
		}

		stack.hurtAndBreak(1, attacker, InteractionHand.MAIN_HAND);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity player, @Nullable EquipmentSlot slot) {}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		return amount;
	}
}
