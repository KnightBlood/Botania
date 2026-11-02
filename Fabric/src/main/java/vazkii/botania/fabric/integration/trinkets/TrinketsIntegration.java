/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.trinkets;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.yumi.commons.TriState;

import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import eu.pb4.trinkets.api.event.TrinketDropCallback;
import eu.pb4.trinkets.api.event.TrinketSlotCompatibilityCallback;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.ResoluteIvyItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.proxy.Proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Integration with trinkets 4.x (eu.pb4). The old dev.emi 3.x Trinket interface is gone; the
 * per-item hooks now live in {@link TrinketCallback} and slot compatibility is provided through
 * {@link TrinketSlotCompatibilityCallback}. Right-click-to-equip is handled by trinkets itself
 * (its Item mixin consults {@link TrinketCallback#canEquipFromUse}), so no UseItemCallback is
 * registered anymore.
 */
public class TrinketsIntegration extends EquipmentHandler {
	public static void init() {
		TrinketDropCallback.EVENT.register(TrinketsIntegration::keepAccessoryDrops);
		TrinketSlotCompatibilityCallback.EVENT.register(TrinketsIntegration::allowBaubleSlots);
	}

	private static TrinketDropRule keepAccessoryDrops(TrinketDropRule oldRule,
			ItemStack stack, TrinketSlotAccess ref, LivingEntity livingEntity) {
		//TODO make this less hacky
		if (ResoluteIvyItem.hasIvy(stack)) {
			ItemNBTHelper.removeEntry(stack, ResoluteIvyItem.TAG_KEEP);
			return TrinketDropRule.KEEP;
		}
		return oldRule;
	}

	private static TriState allowBaubleSlots(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity, boolean defaultValue) {
		// Botania baubles were registered as trinkets for every slot in 1.20.1; keep that behaviour
		return stack.getItem() instanceof BaubleItem ? TriState.TRUE : TriState.DEFAULT;
	}

	@Override
	@SuppressWarnings("removal") // deprecated TrinketAttachment accessors, replacement requires API rework
	protected Container getAllWornItems(LivingEntity living) {
		var attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return new SimpleContainer(0);
		}
		List<ItemStack> list = new ArrayList<>();
		for (var tuple : attachment.getAllEquipped()) {
			list.add(tuple.getB());
		}
		return new SimpleContainer(list.toArray(new ItemStack[0]));
	}

	@Override
	@SuppressWarnings("removal") // deprecated TrinketAttachment accessors, replacement requires API rework
	protected ItemStack findItem(Item item, LivingEntity living) {
		var attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return ItemStack.EMPTY;
		}
		var results = attachment.getEquipped(item);
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).getB();
	}

	@Override
	@SuppressWarnings("removal") // deprecated TrinketAttachment accessors, replacement requires API rework
	protected ItemStack findItem(java.util.function.Predicate<ItemStack> pred, LivingEntity living) {
		var attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return ItemStack.EMPTY;
		}
		var results = attachment.getEquipped(pred, false);
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).getB();
	}

	@Override
	public void onInit(Item item) {
		TrinketCallback.setCallback(item, WRAPPER);
		Proxy.INSTANCE.runOnClient(() -> () -> TrinketRendererRegistry.registerRenderer(item, new RenderWrapper()));
	}

	@Override
	public boolean isAccessory(ItemStack stack) {
		return super.isAccessory(stack)
				|| TrinketCallback.getCallback(stack) != TrinketCallback.DEFAULT;
	}

	public static final TrinketCallback WRAPPER = new TrinketCallback() {
		private BaubleItem getItem(ItemStack stack) {
			return (BaubleItem) stack.getItem();
		}

		@Override
		public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onWornTick(stack, entity);
			}
		}

		@Override
		public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onEquipped(stack, entity);
			}
		}

		@Override
		public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				getItem(stack).onUnequipped(stack, entity);
			}
		}

		@Override
		public boolean canEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
			if (!stack.isEmpty()) {
				return getItem(stack).canEquip(stack, entity);
			}
			return false;
		}

		@Override
		public boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
			return canEquip(stack, null, entity);
		}

		@Override
		public void forEachTrinketModifier(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity,
				Identifier id, BiConsumer<Holder<Attribute>, AttributeModifier> consumer) {
			if (!stack.isEmpty()) {
				Multimap<Holder<Attribute>, AttributeModifier> modifiers = getItem(stack).getEquippedAttributeModifiers(stack);
				modifiers.forEach(consumer);
			}
		}
	};

	private static class RenderWrapper implements TrinketRenderer {
		// 26.1: TrinketRenderer.submit uses the new render-state pipeline
		// (LivingEntityRenderState + SubmitNodeCollector), which the Xplat accessory
		// renderers are not ported to yet. Registration stays in place so slot
		// rendering does not crash; visuals follow in a dedicated render pass.
		@Override
		public void submit(ItemStack stack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> contextModel,
				PoseStack matrices, SubmitNodeCollector collector, int light, LivingEntityRenderState state,
				float tickDelta, float headYaw) {}
	}
}
