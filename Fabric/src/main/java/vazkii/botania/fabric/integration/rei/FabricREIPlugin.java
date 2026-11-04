/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.rei;

import net.minecraft.world.level.block.Block;

import vazkii.botania.client.integration.rei.BotaniaREIPlugin;
import vazkii.botania.fabric.xplat.FabricXplatImpl;

import java.util.Map;

/** Fabric's {@code rei_client} entrypoint; the REI implementation itself is shared in Xplat. */
public final class FabricREIPlugin extends BotaniaREIPlugin {
	@Override
	protected Map<Block, Block> customStripping() {
		return FabricXplatImpl.CUSTOM_STRIPPING;
	}
}
