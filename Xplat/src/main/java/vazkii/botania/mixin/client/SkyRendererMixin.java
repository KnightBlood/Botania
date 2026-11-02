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
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.MoonPhase;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import vazkii.botania.client.core.SkyblockWorldInfo;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.render.world.SkyblockSkyRenderer;
import vazkii.botania.xplat.BotaniaConfig;

/**
 * This Mixin implements the Garden of Glass skybox.
 * It needs to be applied before similar mixins from other mods (e.g. Enhanced Celestials, BedrockIfy) to prevent those
 * from removing our injection points. These changes are compatible with those mods, assuming proper sequencing.
 */
@Mixin(value = SkyRenderer.class, priority = 900)
public class SkyRendererMixin {

	@Unique
	private static boolean isGogSky() {
		Level world = Minecraft.getInstance().level;
		boolean isGog = world.getLevelData() instanceof SkyblockWorldInfo skyblockInfo && skyblockInfo.isGardenOfGlass();
		return BotaniaConfig.client().enableFancySkybox()
				&& world.dimension() == Level.OVERWORLD
				&& (BotaniaConfig.client().enableFancySkyboxInNormalWorlds() || isGog);
	}

	/**
	 * Render planets, rays, the rainbow and lots of extra stars, at the end of the vanilla sun/moon/stars rendering.
	 * The vanilla method restores the pose stack to its entry state, so we re-apply the Y rotation to match the
	 * coordinate system of the old 1.20.1 injection point.
	 */
	@Inject(
		method = "renderSunMoonAndStars",
		at = @At("TAIL"),
		require = 0
	)
	private void renderExtras(PoseStack poseStack, float sunAngle, float moonAngle, float starAngle,
			MoonPhase moonPhase, float rainBrightness, float starBrightness, CallbackInfo ci) {
		if (isGogSky()) {
			Minecraft mc = Minecraft.getInstance();
			float partialTicks = ClientTickHandler.partialTicks;
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
			SkyblockSkyRenderer.renderExtra(poseStack, mc.level, partialTicks, 0);
			SkyblockSkyRenderer.renderStars(poseStack, partialTicks);
			poseStack.popPose();
		}
	}

	/**
	 * Make the sun bigger by scaling it to double size
	 */
	@ModifyArgs(
		method = "renderSun",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;"
		),
		require = 0
	)
	private void makeSunBigger(Args args) {
		if (isGogSky()) {
			args.set(0, (float) args.get(0) * 2F);
			args.set(2, (float) args.get(2) * 2F);
		}
	}

	/**
	 * Make the moon bigger by scaling it to 1.5x size
	 */
	@ModifyArgs(
		method = "renderMoon",
		at = @At(
			value = "INVOKE",
			target = "Lorg/joml/Matrix4f;scale(FFF)Lorg/joml/Matrix4f;"
		),
		require = 0
	)
	private void makeMoonBigger(Args args) {
		if (isGogSky()) {
			args.set(0, (float) args.get(0) * 1.5F);
			args.set(2, (float) args.get(2) * 1.5F);
		}
	}
}
