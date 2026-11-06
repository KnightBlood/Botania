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
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.PhantomInkable;
import vazkii.botania.client.model.armor.ArmorModel;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;

/**
 * Restores Botania's custom 3D armour and phantom ink on MC 26.1.
 * <p>
 * 26.1 draws armour exclusively through {@code Equippable.assetId()} and
 * {@code EquipmentLayerRenderer} using the vanilla humanoid silhouette. P9 wired up the
 * equipment textures, but that produced flat armour: Botania's 1.20.1 {@link ArmorModels}
 * (raised pauldrons, helm crests, crystal pieces) were never invoked and the 1.20.1
 * {@code getArmorTexture} hook was never called either. This layer re-routes Botania armour
 * to those models and their original 64x128 atlases, and hides phantom-inked pieces.
 * <p>
 * Known differences from 1.20.1, deliberately not hidden: armour trims are not drawn on the
 * custom geometry (the old custom ArmorRenderer did not draw them either), and baby players
 * keep the vanilla silhouette because the 1.20.1 model set has no baby variant.
 */
@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin {
	private static boolean botania$warnedMissingModel = false;

	@Inject(method = "renderArmorPiece", at = @At("HEAD"), cancellable = true)
	private void botania$renderCustomArmor(PoseStack pose, SubmitNodeCollector collector, ItemStack stack,
			EquipmentSlot slot, int light, HumanoidRenderState state, CallbackInfo ci) {
		if (!(stack.getItem() instanceof ManasteelArmorItem armorItem)) {
			return;
		}

		// 1.20.1 hid inked armour by swapping in a transparent texture; hide the piece here.
		if (stack.getItem() instanceof PhantomInkable inkable && inkable.hasPhantomInk(stack)) {
			ci.cancel();
			return;
		}

		// No baby variant of the 1.20.1 geometry exists. Fall through to vanilla equipment,
		// which still has the 64x64 baby silhouette textures registered by P9.
		if (state.isBaby && state.entityType != EntityType.ARMOR_STAND) {
			return;
		}

		ArmorModels.ensureInitialized();
		ArmorModel model = ArmorModels.get(stack);
		if (model == null) {
			if (!botania$warnedMissingModel) {
				botania$warnedMissingModel = true;
				BotaniaAPI.LOGGER.warn("No custom armour model for {}; falling back to the vanilla silhouette", stack.getItem());
			}
			return;
		}

		model.setupAnim(state);
		Identifier texture = Identifier.parse(armorItem.getArmorTextureAfterInk(stack, slot));
		collector.order(1).submitModel(model, state, pose, RenderTypes.armorCutoutNoCull(texture),
				light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		if (stack.hasFoil()) {
			collector.order(2).submitModel(model, state, pose, RenderTypes.armorEntityGlint(),
					light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
		}
		ci.cancel();
	}
}
