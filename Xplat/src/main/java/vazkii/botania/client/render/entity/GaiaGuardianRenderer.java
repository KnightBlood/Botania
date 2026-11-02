/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.model.armor.ArmorModels;
import vazkii.botania.common.entity.GaiaGuardianEntity;

public class GaiaGuardianRenderer extends HumanoidMobRenderer<GaiaGuardianEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {

	public static final float DEFAULT_GRAIN_INTENSITY = 0.05F;
	public static final float DEFAULT_DISFIGURATION = 0.025F;

	private final Model normalModel;
	private final Model slimModel;

	@SuppressWarnings("this-escape") // getModel() is final on EntityRenderer in practice; same pattern as 1.20.1
	public GaiaGuardianRenderer(EntityRendererProvider.Context ctx) {
		super(ctx, new Model(ctx.bakeLayer(ModelLayers.PLAYER)), 0F);
		this.normalModel = (Model) this.getModel();
		this.slimModel = new Model(ctx.bakeLayer(ModelLayers.PLAYER_SLIM));
		// Call this here bc no other place with access to Context
		ArmorModels.init(ctx);
	}

	@NotNull
	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public void submit(@NotNull HumanoidRenderState state, @NotNull com.mojang.blaze3d.vertex.PoseStack poseStack,
			@NotNull net.minecraft.client.renderer.SubmitNodeCollector collector,
			@NotNull net.minecraft.client.renderer.state.level.CameraRenderState camera) {
		var view = Minecraft.getInstance().getCameraEntity();
		if (view instanceof AbstractClientPlayer && DefaultPlayerSkin.get(view.getUUID()).model() == PlayerModelType.SLIM) {
			this.model = slimModel;
		} else {
			this.model = normalModel;
		}

		super.submit(state, poseStack, collector, camera);
	}

	@NotNull
	@Override
	public Identifier getTextureLocation(@NotNull HumanoidRenderState state) {
		Minecraft mc = Minecraft.getInstance();

		if (!(mc.getCameraEntity() instanceof AbstractClientPlayer clientPlayer)) {
			return DefaultPlayerSkin.getDefaultSkin().body().texturePath();
		}

		return clientPlayer.getSkin().body().texturePath();
	}

	@Override
	protected boolean isBodyVisible(@NotNull HumanoidRenderState state) {
		return true;
	}

	private static class Model extends HumanoidModel<HumanoidRenderState> {
		Model(ModelPart root) {
			super(root, RenderHelper::getDopplegangerLayer);
		}
	}

}
