/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.handler;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ServerLevelAccessor;

import vazkii.botania.common.entity.PixieEntity;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.armor.elementium.ElementiumHelmItem;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public final class PixieHandler {

	private PixieHandler() {}

	public static Holder<Attribute> PIXIE_SPAWN_CHANCE;
	private static final Map<EquipmentSlot, Identifier> DEFAULT_MODIFIER_IDS = Util.make(new EnumMap<>(EquipmentSlot.class), m -> {
		m.put(EquipmentSlot.HEAD, prefix("pixie_mod_head"));
		m.put(EquipmentSlot.CHEST, prefix("pixie_mod_chest"));
		m.put(EquipmentSlot.LEGS, prefix("pixie_mod_legs"));
		m.put(EquipmentSlot.FEET, prefix("pixie_mod_feet"));
		m.put(EquipmentSlot.MAINHAND, prefix("pixie_mod_mainhand"));
		m.put(EquipmentSlot.OFFHAND, prefix("pixie_mod_offhand"));
	});

	private static final List<Supplier<MobEffectInstance>> effectSuppliers = List.of(
			() -> new MobEffectInstance(MobEffects.BLINDNESS, 40, 0),
			() -> new MobEffectInstance(MobEffects.WITHER, 50, 0),
			() -> new MobEffectInstance(MobEffects.SLOWNESS, 40, 0),
			() -> new MobEffectInstance(MobEffects.WEAKNESS, 40, 0)
	);

	/**
	 * Registers {@link #PIXIE_SPAWN_CHANCE} into the built-in ATTRIBUTE registry.
	 * <p>
	 * Must be called while that registry is still writable: on Fabric this is during the
	 * initializer (before the vanilla bootstrap freezes it), on NeoForge during the
	 * {@code RegisterEvent} for {@link Registries#ATTRIBUTE} (NeoForge unfreezes builtin
	 * registries only for the duration of that event). Idempotent for both call orders.
	 */
	public static void init() {
		if (PIXIE_SPAWN_CHANCE == null) {
			PIXIE_SPAWN_CHANCE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE,
					prefix("pixie_spawn_chance"), new RangedAttribute("attribute.name.botania.pixieSpawnChance", 0, 0, 1).setSyncable(true));
		}
	}

	public static AttributeModifier makeModifier(EquipmentSlot slot, String name, double amount) {
		return new AttributeModifier(DEFAULT_MODIFIER_IDS.get(slot), amount, AttributeModifier.Operation.ADD_VALUE);
	}

	public static void onDamageTaken(Player player, DamageSource source) {
		if (!player.level().isClientSide() && source.getEntity() instanceof LivingEntity livingSource) {
			// Sometimes the player doesn't have the attribute, not sure why.
			// Could be badly-written mixins on Fabric.
			double chance = player.getAttributes().hasAttribute(PIXIE_SPAWN_CHANCE)
					? player.getAttributeValue(PIXIE_SPAWN_CHANCE) : 0;
			ItemStack sword = PlayerHelper.getFirstHeldItem(player, s -> s.is(BotaniaItems.elementiumSword));

			if (Math.random() < chance) {
				PixieEntity pixie = new PixieEntity(player.level());
				pixie.setPos(player.getX(), player.getY() + 2, player.getZ());

				if (((ElementiumHelmItem) BotaniaItems.elementiumHelm).hasArmorSet(player)) {
					pixie.setApplyPotionEffect(effectSuppliers.get(player.level().getRandom().nextInt(effectSuppliers.size())).get());
				}

				float dmg = 4;
				if (!sword.isEmpty()) {
					dmg += 2;
				}

				pixie.setProps(livingSource, player, 0, dmg);
				pixie.finalizeSpawn((ServerLevelAccessor) player.level(), ((ServerLevelAccessor) player.level()).getCurrentDifficultyAt(pixie.blockPosition()),
						EntitySpawnReason.EVENT, null);
				player.level().addFreshEntity(pixie);
			}
		}
	}
}
