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

import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.client.TrinketRenderer;
import eu.pb4.trinkets.api.client.TrinketRendererRegistry;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * Client-only Trinkets renderer registration.
 *
 * <p>This must stay out of {@link TrinketsIntegration}: that class implements the server-side
 * {@code EquipmentHandler} and is therefore loaded on a dedicated server, where Fabric's
 * environment transformer rejects {@code eu.pb4.trinkets.api.client.*} and
 * {@code net.minecraft.client.*}. Deciding not to run a lambda at runtime is not enough - the
 * class verifier still resolves the referenced client classes when the outer class is linked.
 * This class is only reachable from the Fabric {@code client} entrypoint.
 */
public final class TrinketsClientIntegration {
	private TrinketsClientIntegration() {}

	/** Registers the (currently empty) renderer for every bauble that received a TrinketCallback. */
	public static void registerRenderers() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (item instanceof BaubleItem) {
				TrinketRendererRegistry.registerRenderer(item, new RenderWrapper());
			}
		}
	}

	private static final class RenderWrapper implements TrinketRenderer {
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
