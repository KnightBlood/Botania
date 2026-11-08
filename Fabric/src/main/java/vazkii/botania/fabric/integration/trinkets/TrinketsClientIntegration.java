/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.render.AccessoryRenderBridge;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;

/**
 * Client-only Trinkets renderer registration.
 *
 * <p>
 * This must stay out of {@link TrinketsIntegration}: that class implements the server-side
 * {@code EquipmentHandler} and is therefore loaded on a dedicated server, where Fabric's
 * environment transformer rejects {@code eu.pb4.trinkets.api.client.*} and
 * {@code net.minecraft.client.*}. Deciding not to run a lambda at runtime is not enough - the
 * class verifier still resolves the referenced client classes when the outer class is linked.
 * This class is only reachable from the Fabric {@code client} entrypoint.
 */
public final class TrinketsClientIntegration {
	private TrinketsClientIntegration() {}

	/** Registers the Botania accessory renderer for every bauble. */
	public static void registerRenderers() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BaubleItem) {
				TrinketRendererRegistry.registerRenderer(item, new RenderWrapper());
			}
		}
	}

	private static final class RenderWrapper implements TrinketRenderer {
		@Override
		public void submit(ItemStack stack, TrinketSlotAccess slot, EntityModel<? extends LivingEntityRenderState> contextModel,
				PoseStack matrices, SubmitNodeCollector collector, int light, LivingEntityRenderState state,
				float yRot, float xRot) {
			// TrinketSlotAccess has no entity field, but its inventory's attachment does. The
			// 1.20.1 renderers need the LivingEntity for sneak pose, equipped armour, tick count
			// and the item-model seed, so keep passing the real one rather than faking a state.
			LivingEntity living = slot.inventory().getAttachment().getEntity();
			if (!AccessoryRenderBridge.shouldRender(stack, living)) {
				return;
			}
			if (!(contextModel instanceof HumanoidModel<?> humanoid)) {
				return;
			}
			AccessoryRenderBridge.render(stack, living, humanoid, matrices, collector, light, state, yRot, xRot);
		}
	}
}
