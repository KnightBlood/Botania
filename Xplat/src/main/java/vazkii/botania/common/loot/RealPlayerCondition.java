/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.MapCodec;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.helper.PlayerHelper;

import java.util.Set;

public class RealPlayerCondition implements LootItemCondition {
	public static final RealPlayerCondition INSTANCE = new RealPlayerCondition();
	public static final MapCodec<RealPlayerCondition> CODEC = MapCodec.unit(() -> INSTANCE);

	private RealPlayerCondition() {
	}

	@Override
	public boolean test(LootContext lootContext) {
		Player player = lootContext.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER);
		return PlayerHelper.isTruePlayer(player);
	}

	@NotNull
	@Override
	public Set<net.minecraft.util.context.ContextKey<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.LAST_DAMAGE_PLAYER);
	}

	@Override
	public MapCodec<? extends LootItemCondition> codec() {
		return CODEC;
	}
}
