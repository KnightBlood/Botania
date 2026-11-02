/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

import vazkii.botania.client.render.entity.PixieRenderState;

public class PixieModel extends EntityModel<PixieRenderState> {
	private final ModelPart body;
	private final ModelPart leftWingT;
	private final ModelPart leftWingB;
	private final ModelPart rightWingT;
	private final ModelPart rightWingB;

	public PixieModel(ModelPart root) {
		super(root, t -> RenderTypes.entityCutout(t));

		body = root.getChild("body");
		leftWingT = root.getChild("leftWingT");
		leftWingB = root.getChild("leftWingB");
		rightWingT = root.getChild("rightWingT");
		rightWingB = root.getChild("rightWingB");
	}

	public static MeshDefinition createMesh() {
		var mesh = new MeshDefinition();
		var root = mesh.getRoot();
		root.addOrReplaceChild("body", CubeListBuilder.create().addBox(-2.5F, 0.0F, -2.5F, 5, 5, 5), PartPose.offset(0.0F, 16.0F, 0.0F));
		root.addOrReplaceChild("leftWingT", CubeListBuilder.create().texOffs(0, 4)
				.addBox(0.0F, -5.0F, 0.0F, 0, 5, 6),
				PartPose.offsetAndRotation(2.5F, 18.0F, 0.5F, 0.2618F, 0.5236F, 0.2618F));
		root.addOrReplaceChild("leftWingB", CubeListBuilder.create().texOffs(0, 11)
				.addBox(0.0F, 0.0F, 0.0F, 0, 3, 4),
				PartPose.offsetAndRotation(2.5F, 18.0F, 0.5F, -0.2618F, 0.2618F, -0.2618F));
		root.addOrReplaceChild("rightWingT", CubeListBuilder.create().texOffs(0, 4)
				.addBox(0.0F, -5.0F, 0.0F, 0, 5, 6),
				PartPose.offsetAndRotation(-2.5F, 18.0F, 0.5F, 0.2618F, -0.5236F, -0.2618F));
		root.addOrReplaceChild("rightWingB", CubeListBuilder.create().texOffs(0, 11)
				.addBox(0.0F, 0.0F, 0.0F, 0, 3, 4),
				PartPose.offsetAndRotation(-2.5F, 18.0F, 0.5F, -0.2618F, -0.2618F, 0.2618F));
		return mesh;
	}

	@Override
	public void setupAnim(PixieRenderState state) {
		super.setupAnim(state);
		float f2 = state.ageInTicks;
		rightWingT.yRot = -(Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.5F);
		leftWingT.yRot = Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.5F;
		rightWingB.yRot = -(Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.25F);
		leftWingB.yRot = Mth.cos(f2 * 1.7F) * (float) Math.PI * 0.25F;
	}

}
