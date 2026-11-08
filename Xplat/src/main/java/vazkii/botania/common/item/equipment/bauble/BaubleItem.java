/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import vazkii.botania.api.item.CosmeticAttachable;
import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public abstract class BaubleItem extends Item implements CosmeticAttachable, PhantomInkable {

	private static final String TAG_BAUBLE_UUID = "baubleUUID";
	private static final String TAG_COSMETIC_ITEM = "cosmeticItem";
	private static final String TAG_PHANTOM_INK = "phantomInk";

	@SuppressWarnings("this-escape") // 1.20.1 pattern preserved: this escapes via lambda/setter in ctor
	public BaubleItem(Properties props) {
		super(props);
		EquipmentHandler.instance.onInit(this);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flags) {
		ItemStack cosmetic = getCosmeticItem(stack);
		if (!cosmetic.isEmpty()) {
			builder.accept(Component.translatable("botaniamisc.hasCosmetic", cosmetic.getHoverName()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		if (hasPhantomInk(stack)) {
			builder.accept(Component.translatable("botaniamisc.hasPhantomInk").withStyle(ChatFormatting.AQUA));
		}
	}

	@Override
	public ItemStack getCosmeticItem(ItemStack stack) {
		CompoundTag cmp = ItemNBTHelper.getCompound(stack, TAG_COSMETIC_ITEM, true);
		if (cmp == null) {
			return ItemStack.EMPTY;
		}
		return ItemNBTHelper.deserializeStackNbt(cmp);
	}

	@Override
	public void setCosmeticItem(ItemStack stack, ItemStack cosmetic) {
		CompoundTag cmp = ItemNBTHelper.serializeStackNbt(cosmetic);
		ItemNBTHelper.setCompound(stack, TAG_COSMETIC_ITEM, cmp);
	}

	public static Identifier getBaubleModifierId(ItemStack stack) {
		// Use the item's registry name as a stable modifier identifier
		return Identifier.parse("botania:bauble_" + stack.getItem().toString().replace(":", "_"));
	}

	@Override
	public boolean hasPhantomInk(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_PHANTOM_INK, false);
	}

	@Override
	public void setPhantomInk(ItemStack stack, boolean ink) {
		ItemNBTHelper.setBoolean(stack, TAG_PHANTOM_INK, ink);
	}

	public void onWornTick(ItemStack stack, LivingEntity entity) {}

	public void onEquipped(ItemStack stack, LivingEntity entity) {
		if (!entity.level().isClientSide() && entity instanceof ServerPlayer player) {
			PlayerHelper.grantCriterion(player, prefix("main/bauble_wear"), "code_triggered");
		}
	}

	public void onUnequipped(ItemStack stack, LivingEntity entity) {}

	public boolean canEquip(ItemStack stack, LivingEntity entity) {
		return true;
	}

	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		return HashMultimap.create();
	}

	public boolean hasRender(ItemStack stack, LivingEntity living) {
		return !hasPhantomInk(stack)
				&& BotaniaConfig.client().renderAccessories()
				&& !living.isInvisible();
	}
}
