/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.loot;

import com.mojang.serialization.MapCodec;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.entity.GaiaGuardianEntity;

public class TrueGuardianKiller implements LootItemCondition {
	public static final MapCodec<TrueGuardianKiller> CODEC = MapCodec.unit(TrueGuardianKiller::new);

	@Override
	public boolean test(@NotNull LootContext context) {
		Entity victim = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
		return victim instanceof GaiaGuardianEntity gg
				&& context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY) == gg.trueKiller;
	}

	@Override
	public MapCodec<? extends LootItemCondition> codec() {
		return CODEC;
	}
}
