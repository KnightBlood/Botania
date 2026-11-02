/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.client.lib.ResourcesLib;

public class PinkWitherRenderer extends WitherBossRenderer {

	private static final Identifier resource = Identifier.parse(ResourcesLib.MODEL_PINK_WITHER);

	public PinkWitherRenderer(EntityRendererProvider.Context ctx) {
		super(ctx);
	}

	@NotNull
	@Override
	public Identifier getTextureLocation(WitherRenderState state) {
		return resource;
	}

}
