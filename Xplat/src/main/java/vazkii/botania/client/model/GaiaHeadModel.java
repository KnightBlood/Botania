/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;

import vazkii.botania.client.render.block_entity.GaiaHeadBlockEntityRenderer;

/**
 * A skull model that mirrors the head of whichever mob is currently looking at it, so that the Gaia
 * head appears to be made of the viewer's face. The delegate model is picked per frame; its pose is
 * copied onto our own head geometry.
 */
public class GaiaHeadModel extends SkullModelBase {
	private final ModelPart head;

	public GaiaHeadModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
	}

	@Override
	public void setupAnim(SkullModelBase.State state) {
		super.setupAnim(state);

		SkullModelBase delegate = GaiaHeadBlockEntityRenderer.models.get(GaiaHeadBlockEntityRenderer.getViewType());
		if (delegate != null) {
			delegate.setupAnim(state);
			copyPose(delegate.root(), this.head);
		} else {
			this.head.yRot = state.yRot * (float) (Math.PI / 180.0);
			this.head.xRot = state.xRot * (float) (Math.PI / 180.0);
		}
	}

	/**
	 * Copies the pose of the delegate's head part, then recurses into same-named children so that
	 * things like the dragon's jaw still animate.
	 */
	private static void copyPose(ModelPart from, ModelPart to) {
		to.xRot = from.xRot;
		to.yRot = from.yRot;
		to.zRot = from.zRot;
		to.x = from.x;
		to.y = from.y;
		to.z = from.z;
		for (String name : new String[] { "jaw", "hat" }) {
			if (from.hasChild(name) && to.hasChild(name)) {
				copyPose(from.getChild(name), to.getChild(name));
			}
		}
	}
}
