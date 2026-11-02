/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin.client;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * 26.1 keeps the id → codec map of range-select item model properties private and only fills it in
 * {@code bootstrap()} (plus NeoForge's {@code RegisterRangeSelectItemModelPropertyEvent}). Fabric has
 * no equivalent event, so this accessor lets Botania add its own properties to the same map.
 */
@Mixin(RangeSelectItemModelProperties.class)
public interface RangeSelectItemModelPropertiesAccessor {
	@Accessor("ID_MAPPER")
	static ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends RangeSelectItemModelProperty>> botania$getIdMapper() {
		throw new AssertionError("mixin not applied");
	}
}
