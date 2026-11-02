/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data;

import net.minecraft.core.HolderLookup;

/**
 * Exposes a tag provider addTags as public, so the Fabric-side datagen bridge
 * (FabricXplatTagBridge) can invoke it across packages; vanilla declares it protected.
 */
public interface PublicAddTags<T> {
	void addTags(HolderLookup.Provider provider);
}
