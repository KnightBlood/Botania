/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.equipment.bauble;

import net.minecraft.resources.Identifier;

import vazkii.botania.client.lib.ResourcesLib;

public class GlobetrottersSashItem extends SojournersSashItem {

	private static final Identifier texture = Identifier.parse(ResourcesLib.MODEL_SUPER_TRAVEL_BELT);

	public GlobetrottersSashItem(Properties props) {
		super(props, 0.085F, 0.3F, 4F);
	}

	@Override
	public Identifier getRenderTexture() {
		return texture;
	}
}
