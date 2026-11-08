/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.block_entity.BotaniaRenderContext;
import vazkii.botania.common.item.equipment.bauble.BaubleItem;

/**
 * Wires the 1.20.1 {@link AccessoryRenderer} renderers into MC 26.1's extract/submit pipeline.
 *
 * <p>
 * Botania's bauble geometry (17 renderers registered through {@link AccessoryRenderRegistry})
 * still uses the immediate-mode {@code MultiBufferSource} API. MC 26.1 renders entities through
 * render states and a {@link SubmitNodeCollector} instead, so the two loader integrations call
 * this bridge from their respective renderer callbacks and the recorded geometry is replayed with
 * {@link BotaniaRenderContext#flush(SubmitNodeCollector)}.
 *
 * <p>
 * Cosmetic overrides follow the 1.20.1 behaviour: both loaders ultimately resolved the
 * override stack back through {@code AccessoryRenderRegistry} (Trinkets via the cosmetic item's
 * own {@code TrinketRenderer}, Curios via the bauble's own {@code ICurioRenderer}), so this bridge
 * looks up the override stack directly. A bauble with no registered renderer is intentionally not
 * drawn - most baubles have no worn geometry, exactly as in 1.20.1.
 *
 * <p>
 * Parameter mapping for {@link AccessoryRenderer#doRender}: the limb-swing values come from
 * {@link LivingEntityRenderState#walkAnimationPos} / {@code walkAnimationSpeed}, and the head
 * rotation from the two floats passed to the layer's {@code submit}/{@code render} callback
 * (26.1 {@code RenderLayer#submit} names them {@code yRot}/{@code xRot}; Trinkets still calls them
 * {@code limbAngle}/{@code limbDistance} but forwards the same values).
 */
public final class AccessoryRenderBridge {
	private AccessoryRenderBridge() {}

	/**
	 * Mirrors the 1.20.1 guard: phantom ink, the "render accessories" config and invisibility all
	 * suppress the worn geometry. Takes an {@link ItemStack} rather than a {@link BaubleItem} so
	 * callers never have to cast (cosmetic override stacks are not necessarily baubles).
	 */
	public static boolean shouldRender(ItemStack stack, LivingEntity living) {
		return stack.getItem() instanceof BaubleItem bauble && bauble.hasRender(stack, living);
	}

	/**
	 * Records and submits the worn geometry for {@code stack}. The caller is responsible for
	 * checking {@link #shouldRender} and for supplying a {@link HumanoidModel} parent.
	 */
	public static void render(ItemStack stack, LivingEntity living, HumanoidModel<?> bipedModel,
			PoseStack ms, SubmitNodeCollector collector, int light,
			LivingEntityRenderState state, float netHeadYaw, float headPitch) {
		BaubleItem item = (BaubleItem) stack.getItem();
		ItemStack cosmetic = item.getCosmeticItem(stack);
		ItemStack displayed = cosmetic.isEmpty() ? stack : cosmetic;
		AccessoryRenderer renderer = AccessoryRenderRegistry.get(displayed);
		if (renderer == null) {
			return;
		}

		BotaniaRenderContext buffers = new BotaniaRenderContext(null);
		ms.pushPose();
		try {
			// EntityRenderState.partialTick only exists on NeoForge's patched hierarchy, so the
			// cross-loader partial tick source is Botania's own ClientTickHandler field.
			renderer.doRender(bipedModel, displayed, living, ms, buffers, light,
					state.walkAnimationPos, state.walkAnimationSpeed, ClientTickHandler.partialTicks,
					state.ageInTicks, netHeadYaw, headPitch);
		} finally {
			ms.popPose();
		}
		buffers.flush(collector);
	}
}
