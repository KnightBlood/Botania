/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.api.item.PhantomInkable;

/**
 * Restores phantom ink on 26.1: 1.20.1 hid inked armour by returning the transparent
 * texture from {@code getArmorTexture}, but 26.1 draws armour exclusively through
 * {@code Equippable.assetId()} / {@code EquipmentLayerRenderer} and never calls that hook.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void botania$skipPhantomInk(PoseStack pose, SubmitNodeCollector collector, ItemStack stack,
			EquipmentSlot slot, int light, HumanoidRenderState state, CallbackInfo ci) {
		if (stack.getItem() instanceof PhantomInkable inkable && inkable.hasPhantomInk(stack)) {
			ci.cancel();
		}
	}
}
