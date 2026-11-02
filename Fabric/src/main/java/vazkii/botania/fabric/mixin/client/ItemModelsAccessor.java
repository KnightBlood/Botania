/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin.client;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.1 keeps the id → codec map of item model types private and only fills it in
 * {@code bootstrap()} (plus NeoForge's {@code RegisterItemModelsEvent}). Fabric has no equivalent
 * event, so this accessor lets Botania register the mana blaster's item model type
 * ({@code botania:mana_gun}) into the same map.
 */
@Mixin(ItemModels.class)
public interface ItemModelsAccessor {
	@Accessor("ID_MAPPER")
	static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemModel.Unbaked>> botania$getIdMapper() {
		throw new AssertionError("mixin not applied");
	}
}
