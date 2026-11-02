/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;

public class ArmorModel extends HumanoidModel<HumanoidRenderState> {
	protected final EquipmentSlot slot;

	public ArmorModel(ModelPart root, EquipmentSlot slot) {
		super(root);
		this.slot = slot;
	}

	@Override
	public void setupAnim(HumanoidRenderState state) {
		super.setupAnim(state);

		// [VanillaCopy] ArmorStandArmorModel.setupAnim because armor stands are dumb
		// This fixes the armor "breathing" and helmets always facing south on armor stands
		if (state instanceof ArmorStandRenderState stand) {
			this.head.xRot = (float) (Math.PI / 180.0) * stand.headPose.x();
			this.head.yRot = (float) (Math.PI / 180.0) * stand.headPose.y();
			this.head.zRot = (float) (Math.PI / 180.0) * stand.headPose.z();
			this.head.setPos(0.0F, 1.0F, 0.0F);
			this.body.xRot = (float) (Math.PI / 180.0) * stand.bodyPose.x();
			this.body.yRot = (float) (Math.PI / 180.0) * stand.bodyPose.y();
			this.body.zRot = (float) (Math.PI / 180.0) * stand.bodyPose.z();
			this.leftArm.xRot = (float) (Math.PI / 180.0) * stand.leftArmPose.x();
			this.leftArm.yRot = (float) (Math.PI / 180.0) * stand.leftArmPose.y();
			this.leftArm.zRot = (float) (Math.PI / 180.0) * stand.leftArmPose.z();
			this.rightArm.xRot = (float) (Math.PI / 180.0) * stand.rightArmPose.x();
			this.rightArm.yRot = (float) (Math.PI / 180.0) * stand.rightArmPose.y();
			this.rightArm.zRot = (float) (Math.PI / 180.0) * stand.rightArmPose.z();
			this.leftLeg.xRot = (float) (Math.PI / 180.0) * stand.leftLegPose.x();
			this.leftLeg.yRot = (float) (Math.PI / 180.0) * stand.leftLegPose.y();
			this.leftLeg.zRot = (float) (Math.PI / 180.0) * stand.leftLegPose.z();
			this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
			this.rightLeg.xRot = (float) (Math.PI / 180.0) * stand.rightLegPose.x();
			this.rightLeg.yRot = (float) (Math.PI / 180.0) * stand.rightLegPose.y();
			this.rightLeg.zRot = (float) (Math.PI / 180.0) * stand.rightLegPose.z();
			this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
			copyPose(this.head, this.hat);
		}

		setPartVisibility();
	}

	private static void copyPose(ModelPart from, ModelPart to) {
		to.x = from.x;
		to.y = from.y;
		to.z = from.z;
		to.xRot = from.xRot;
		to.yRot = from.yRot;
		to.zRot = from.zRot;
	}

	// [VanillaCopy] HumanoidArmorLayer
	private void setPartVisibility() {
		this.head.visible = false;
		this.hat.visible = false;
		this.body.visible = false;
		this.rightArm.visible = false;
		this.leftArm.visible = false;
		this.rightLeg.visible = false;
		this.leftLeg.visible = false;
		switch (slot) {
			case HEAD -> {
				this.head.visible = true;
				this.hat.visible = true;
			}
			case CHEST -> {
				this.body.visible = true;
				this.rightArm.visible = true;
				this.leftArm.visible = true;
			}
			case LEGS -> {
				this.body.visible = true;
				this.rightLeg.visible = true;
				this.leftLeg.visible = true;
			}
			case FEET -> {
				this.rightLeg.visible = true;
				this.leftLeg.visible = true;
			}
			default -> {}
		}
	}
}
