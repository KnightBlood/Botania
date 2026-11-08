/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.integration.trinkets;

import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.yumi.commons.TriState;

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
import java.util.function.Predicate;

import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketDropRule;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;
import eu.pb4.trinkets.api.event.TrinketDropCallback;
import eu.pb4.trinkets.api.event.TrinketSlotCompatibilityCallback;

/**
 * NeoForge-side integration with Trinkets Updated 4.x ({@code eu.pb4:trinkets:4.0.1+26.1}).
 *
 * <p>
 * That artifact is a single jar for both loaders: it carries a {@code fabric.mod.json} and a
 * {@code META-INF/neoforge.mods.toml} with an {@code @Mod("trinkets_updated")} entry point, plus
 * its own NeoForge mixin/interface-injection configs. Curios stays Botania's primary accessory
 * backend on NeoForge; when Trinkets is also present, baubles get a Trinkets callback and the
 * Curios lookups fall back to (or are merged with) the Trinkets slots. If Curios is somehow
 * absent, {@link Handler} can serve as the sole equipment handler.
 *
 * <p>
 * Every call into Trinkets must be guarded by {@link TrinketsPresence#isLoaded()}: the
 * classes are only on the classpath when the optional mod is installed. The probe lives in a
 * separate class so that checking it never initializes this one (whose static callback wrapper
 * needs Trinkets types).
 *
 * <p>
 * Trinkets 4.0.x+26.1's NeoForge side needs {@code TagsUpdatedEvent$ClientPacketReceived}
 * and {@code common.tooltip.ItemTooltipHandler}, which only exist in the NeoForge 26.1.2.x line.
 * The port was originally pinned to 26.1.0.1-beta; {@code gradle.properties} now targets a
 * compatible NeoForge version so this integration is actually exercised at runtime.
 */
public final class NeoTrinketsIntegration {
	private NeoTrinketsIntegration() {}

	public static void init() {
		TrinketDropCallback.EVENT.register(NeoTrinketsIntegration::keepAccessoryDrops);
		TrinketSlotCompatibilityCallback.EVENT.register(NeoTrinketsIntegration::allowBaubleSlots);
	}

	private static TrinketDropRule keepAccessoryDrops(TrinketDropRule oldRule,
			ItemStack stack, TrinketSlotAccess ref, LivingEntity livingEntity) {
		// TODO make this less hacky
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

	public static void onInit(Item item) {
		TrinketCallback.setCallback(item, WRAPPER);
		Proxy.INSTANCE.runOnClient(() -> () -> TrinketRendererRegistry.registerRenderer(item, new RenderWrapper()));
	}

	public static boolean isAccessory(ItemStack stack) {
		return TrinketCallback.getCallback(stack) != TrinketCallback.DEFAULT;
	}

	/** All non-empty trinket slots, using the 4.x non-deprecated attachment API. */
	public static Container getAllWornItems(LivingEntity living) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return new SimpleContainer(0);
		}
		List<ItemStack> list = new ArrayList<>();
		for (TrinketSlotAccess slot : attachment.allEquipped(false)) {
			ItemStack stack = slot.get();
			if (!stack.isEmpty()) {
				list.add(stack);
			}
		}
		return new SimpleContainer(list.toArray(new ItemStack[0]));
	}

	public static ItemStack findItem(Item item, LivingEntity living) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return ItemStack.EMPTY;
		}
		return attachment.findFirst(item).map(TrinketSlotAccess::get).orElse(ItemStack.EMPTY);
	}

	public static ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
		TrinketAttachment attachment = TrinketsApi.getAttachment(living);
		if (attachment == null) {
			return ItemStack.EMPTY;
		}
		return attachment.findFirst(pred).map(TrinketSlotAccess::get).orElse(ItemStack.EMPTY);
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

	/** Used when Trinkets is installed but Curios is not. */
	public static class Handler extends EquipmentHandler {
		@Override
		protected Container getAllWornItems(LivingEntity living) {
			return NeoTrinketsIntegration.getAllWornItems(living);
		}

		@Override
		protected ItemStack findItem(Item item, LivingEntity living) {
			return NeoTrinketsIntegration.findItem(item, living);
		}

		@Override
		protected ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
			return NeoTrinketsIntegration.findItem(pred, living);
		}

		@Override
		public void onInit(Item item) {
			NeoTrinketsIntegration.onInit(item);
		}

		@Override
		public boolean isAccessory(ItemStack stack) {
			return super.isAccessory(stack) || NeoTrinketsIntegration.isAccessory(stack);
		}
	}
}
