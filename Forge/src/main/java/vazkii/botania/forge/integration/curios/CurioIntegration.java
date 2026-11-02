/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.forge.integration.curios;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import org.jetbrains.annotations.NotNull;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.common.DropRule;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.ResoluteIvyItem;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;
import vazkii.botania.common.proxy.Proxy;

import java.util.UUID;
import java.util.function.Predicate;

public class CurioIntegration extends EquipmentHandler {
	public static void init() {
		NeoForge.EVENT_BUS.addListener(CurioIntegration::keepCurioDrops);
	}

	public static void keepCurioDrops(DropRulesEvent event) { //TODO make this less hacky
		event.addOverride(stack -> {
			if (ResoluteIvyItem.hasIvy(stack)) {
				// 26.1: ItemStack.removeTagKey is gone with raw NBT; use the ItemNBTHelper bridge
				ItemNBTHelper.removeEntry(stack, ResoluteIvyItem.TAG_KEEP);
				return true;
			}
			return false;
		}, DropRule.ALWAYS_KEEP);
	}

	@Override
	protected Container getAllWornItems(LivingEntity living) {
		// 26.1: IItemHandler accessors are deprecated for removal; use the Curios query API instead
		return CuriosApi.getCuriosInventory(living)
				.map(i -> {
					var worn = i.findCurios(stack -> true);
					SimpleContainer container = new SimpleContainer(worn.size());
					for (int j = 0; j < worn.size(); j++) {
						container.setItem(j, worn.get(j).stack());
					}
					return (Container) container;
				})
				.orElseGet(() -> new SimpleContainer(0));
	}

	@Override
	protected ItemStack findItem(Item item, LivingEntity living) {
		return CuriosApi.getCuriosInventory(living)
				.map(i -> i.findFirstCurio(item).map(SlotResult::stack).orElse(ItemStack.EMPTY))
				.orElse(ItemStack.EMPTY);
	}

	@Override
	protected ItemStack findItem(Predicate<ItemStack> pred, LivingEntity living) {
		return CuriosApi.getCuriosInventory(living)
				.map(i -> i.findFirstCurio(pred).map(SlotResult::stack).orElse(ItemStack.EMPTY))
				.orElse(ItemStack.EMPTY);
	}

	public void initCapability(RegisterCapabilitiesEvent event, Item... items) {
		event.registerItem(CuriosCapability.ITEM, (stack, context) -> new Wrapper(stack), items);
	}

	// 26.1: CuriosRendererRegistry and ICurio#getAttributeModifiers are deprecated for
	// removal in Curios 15, but still functional; suppressed until the new API is ported.
	@SuppressWarnings("removal")
	@Override
	public void onInit(Item item) {
		Proxy.INSTANCE.runOnClient(() -> () -> CuriosRendererRegistry.register(item, () -> Renderer.INSTANCE));
	}

	@Override
	public boolean isAccessory(ItemStack stack) {
		return super.isAccessory(stack) || CuriosCapability.ITEM.getCapability(stack, null) != null;
	}

	public static class Wrapper implements ICurio {
		private static final SoundInfo SOUND_INFO = new SoundInfo(BotaniaSounds.equipBauble, 0.1F, 1.3F);

		private final ItemStack stack;

		Wrapper(ItemStack stack) {
			this.stack = stack;
		}

		private BaubleItem getItem() {
			return (BaubleItem) stack.getItem();
		}

		@Override
		public ItemStack getStack() {
			return stack;
		}

		@Override
		public void curioTick(SlotContext slotContext) {
			getItem().onWornTick(stack, slotContext.entity());
		}

		@Override
		public void onEquip(SlotContext slotContext, ItemStack previousStack) {
			getItem().onEquipped(stack, slotContext.entity());
		}

		@Override
		public void onUnequip(SlotContext slotContext, ItemStack newStack) {
			getItem().onUnequipped(stack, slotContext.entity());
		}

		@Override
		public boolean canEquip(SlotContext slotContext) {
			return getItem().canEquip(stack, slotContext.entity());
		}

		@SuppressWarnings("removal")
		@Override
		public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, Identifier uuid) {
			// 26.1 Curios (and Botania's BaubleItem) both key attribute modifiers by Holder<Attribute>
			return getItem().getEquippedAttributeModifiers(stack);
		}

		@Override
		public boolean canSync(SlotContext slotContext) {
			return true;
		}

		@NotNull
		@Override
		public SoundInfo getEquipSound(SlotContext slotContext) {
			return SOUND_INFO;
		}

		@Override
		public boolean canEquipFromUse(SlotContext slotContext) {
			return true;
		}
	}

	private static class Renderer implements ICurioRenderer {
		private static final Renderer INSTANCE = new Renderer();

		@Override
		public <S extends LivingEntityRenderState, M extends EntityModel<? super S>>
				void render(ItemStack stack, SlotContext slotContext, PoseStack matrixStack,
						MultiBufferSource renderTypeBuffer, int light, S renderState,
						RenderLayerParent<S, M> renderLayerParent, EntityRendererProvider.Context context,
						float partialTicks, float distance) {
			// 26.1: Curios renders through entity render states now; the limb-swing params are gone.
			LivingEntity livingEntity = slotContext.entity();
			BaubleItem item = (BaubleItem) stack.getItem();

			if (!item.hasRender(stack, livingEntity)) {
				return;
			}

			ItemStack cosmetic = item.getCosmeticItem(stack);
			if (!cosmetic.isEmpty()) {
				@SuppressWarnings("removal")
				var renderer = CuriosRendererRegistry.getRenderer(item);
				renderer.ifPresent(curio -> curio.render(cosmetic, slotContext, matrixStack, renderTypeBuffer,
								light, renderState, renderLayerParent, context, partialTicks, distance));
			}
			// TODO 26.1: the legacy Botania accessory render path (AccessoryRenderRegistry) is
			// model-based (HumanoidModel + limb swing values) and needs a render-state port.
		}
	}

}
