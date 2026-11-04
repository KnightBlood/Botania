/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.integration.rei;

import me.shedaniel.rei.forge.REIPluginClient;

import net.minecraft.world.level.block.Block;

import vazkii.botania.client.integration.rei.BotaniaREIPlugin;
import vazkii.botania.neoforge.xplat.NeoForgeXplatImpl;

import java.util.Map;

/**
 * NeoForge discovers REI client plugins by scanning {@code @REIPluginClient} annotated classes
 * (Architectury annotation scan), so only this thin shell is platform-specific.
 */
@REIPluginClient
public final class NeoForgeREIPlugin extends BotaniaREIPlugin {
	@Override
	protected Map<Block, Block> customStripping() {
		return NeoForgeXplatImpl.CUSTOM_STRIPPABLES;
	}
}
