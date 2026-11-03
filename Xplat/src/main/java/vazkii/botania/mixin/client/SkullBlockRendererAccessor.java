/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import java.util.Map;

import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.SkullBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SkullBlockRenderer.class)
public interface SkullBlockRendererAccessor {
	@Accessor("SKIN_BY_TYPE")
	static Map<SkullBlock.Type, Identifier> botania$skinByType() {
		// Interface mixins with static @Accessor methods have their body injected by the
		// Mixin processor; this stub is never called.
		throw new AssertionError();
	}
}
