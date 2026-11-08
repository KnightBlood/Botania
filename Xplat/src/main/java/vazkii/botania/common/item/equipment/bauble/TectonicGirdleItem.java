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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.helper.AccessoryRenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.render.AccessoryRenderRegistry;
import vazkii.botania.client.render.AccessoryRenderer;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.proxy.Proxy;

public class TectonicGirdleItem extends BaubleItem {

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_KNOCKBACK_BELT);

	@SuppressWarnings("this-escape") // 1.20.1 pattern preserved: this escapes via lambda/setter in ctor
	public TectonicGirdleItem(Properties props) {
		super(props);
		Proxy.INSTANCE.runOnClient(() -> () -> AccessoryRenderRegistry.register(this, new Renderer()));
	}

	@Override
	public Multimap<Holder<Attribute>, AttributeModifier> getEquippedAttributeModifiers(ItemStack stack) {
		Multimap<Holder<Attribute>, AttributeModifier> attributes = HashMultimap.create();
		attributes.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(getBaubleModifierId(stack), 1, AttributeModifier.Operation.ADD_VALUE));
		return attributes;
	}

	public static boolean negateExplosionKnockback(LivingEntity living) {
		// TODO 1.21: replace with explosion knockback resistance attribute
		return !EquipmentHandler.findOrEmpty(BotaniaItems.knockbackBelt, living).isEmpty();
	}

	public static class Renderer implements AccessoryRenderer {
		private static HumanoidModel<HumanoidRenderState> model = null;

		@Override
		public void doRender(HumanoidModel<?> bipedModel, ItemStack stack, LivingEntity living, PoseStack ms, MultiBufferSource buffers, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
			AccessoryRenderHelper.rotateIfSneaking(ms, living);

			float s = 1.15F;
			ms.scale(s, s, s);
			if (model == null) {
				model = new HumanoidModel<>(Minecraft.getInstance()
						.getEntityModels().bakeLayer(ModelLayers.PLAYER));
			}

			VertexConsumer buffer = buffers.getBuffer(model.renderType(texture));
			model.body.render(ms, buffer, light, OverlayTexture.NO_OVERLAY);
		}
	}
}
