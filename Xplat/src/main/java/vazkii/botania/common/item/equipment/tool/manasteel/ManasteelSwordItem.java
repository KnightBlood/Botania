/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.tool.manasteel;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.item.Item.Properties;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.CustomDamageItem;
import vazkii.botania.common.item.equipment.tool.ToolCommons;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ManasteelSwordItem extends Item implements CustomDamageItem {

	public static final int MANA_PER_DAMAGE = 60;

	public ManasteelSwordItem(Properties props) {
		this(BotaniaAPI.instance().getManasteelItemTier(), props);
	}

	public ManasteelSwordItem(ToolMaterial mat, Properties props) {
		this(mat, 3, -2.4F, props);
	}

	public ManasteelSwordItem(ToolMaterial mat, int attackDamage, float attackSpeed, Properties props) {
		this(mat, attackDamage, attackSpeed, UnaryOperator.identity(), props);
	}

	public ManasteelSwordItem(ToolMaterial mat, int attackDamage, float attackSpeed,
			UnaryOperator<ItemAttributeModifiers> attributeModifier, Properties props) {
		super(props.sword(mat, attackDamage, attackSpeed)
				.attributes(attributeModifier.apply(createSwordAttributes(mat, attackDamage, attackSpeed))));
	}

	/**
	 * Recreates vanilla's private {@code ToolMaterial#createSwordAttributes} so subclasses can merge in extra
	 * modifiers (26.1 removed the overridable {@code getDefaultAttributeModifiers(ItemStack)}).
	 */
	private static ItemAttributeModifiers createSwordAttributes(ToolMaterial mat, float attackDamage, float attackSpeed) {
		return ItemAttributeModifiers.builder()
				.add(Attributes.ATTACK_DAMAGE,
						new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage + mat.attackDamageBonus(),
								AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.add(Attributes.ATTACK_SPEED,
						new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed,
								AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND)
				.build();
	}

	@Override
	public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<Item> onBroken) {
		int manaPerDamage = ((ManasteelSwordItem) stack.getItem()).getManaPerDamage();
		return ToolCommons.damageItemIfPossible(stack, amount, entity, manaPerDamage);
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		if (!world.isClientSide() && entity instanceof Player player && stack.getDamageValue() > 0 && ManaItemHandler.instance().requestManaExactForTool(stack, player, getManaPerDamage() * 2, true)) {
			stack.setDamageValue(stack.getDamageValue() - 1);
		}
	}

	public int getManaPerDamage() {
		return MANA_PER_DAMAGE;
	}
}
