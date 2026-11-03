/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.block.SkullBlock;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.client.render.block_entity.GaiaHeadBlockEntityRenderer;
import vazkii.botania.common.block.GaiaHeadBlock;

@Mixin(SkullBlockRenderer.class)
public abstract class SkullBlockRendererMixin {
	@Inject(at = @At("HEAD"), method = "createModel", cancellable = true)
	private static void hookCreateModel(EntityModelSet entityModelSet, SkullBlock.Type type, CallbackInfoReturnable<SkullModelBase> cir) {
		if (type == GaiaHeadBlock.GAIA_TYPE) {
			cir.setReturnValue(GaiaHeadBlockEntityRenderer.createGaiaHeadModel(entityModelSet));
		}
	}

	/**
	 * The Gaia head mirrors whoever is looking at it, so it must be textured with that viewer's
	 * skin/skull texture, exactly like 1.20.1's {@code hookGetRenderType} (GaiaHeadBlockEntityRenderer).
	 * Restored for 26.1's static {@code getSkullRenderType}, which both the block entity renderer
	 * (per frame) and the item "minecraft:head" special model (at bake time) go through.
	 */
	@Inject(at = @At("HEAD"), method = "getSkullRenderType", cancellable = true)
	private static void hookGetRenderType(SkullBlock.Type type, @Nullable Identifier texture, CallbackInfoReturnable<RenderType> cir) {
		if (type == GaiaHeadBlock.GAIA_TYPE) {
			Identifier viewerTexture = viewerTexture();
			if (viewerTexture != null) {
				cir.setReturnValue(RenderTypes.entityCutoutZOffset(viewerTexture));
			}
		}
	}

	private static @Nullable Identifier viewerTexture() {
		SkullBlock.Type viewer = GaiaHeadBlockEntityRenderer.getViewType();
		if (viewer == SkullBlock.Types.PLAYER) {
			Player player = Minecraft.getInstance().player;
			if (player == null) {
				return DefaultPlayerSkin.getDefaultTexture();
			}
			// 26.1: RenderType texture bindings take the full texture *path* (textures/...png),
			// matching vanilla's SKIN_BY_TYPE entries -- NOT the ClientAsset id.
			return Minecraft.getInstance().getSkinManager()
					.get(player.getGameProfile())
					.getNow(Optional.empty())
					.map(PlayerSkin::body)
					.map(net.minecraft.core.ClientAsset.Texture::texturePath)
					.orElse(DefaultPlayerSkin.getDefaultTexture());
		}
		return SkullBlockRendererAccessor.botania$skinByType().get(viewer);
	}
}
