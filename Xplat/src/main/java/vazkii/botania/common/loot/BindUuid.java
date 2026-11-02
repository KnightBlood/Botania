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
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.xplat.XplatAbstractions;

import java.util.List;

public class BindUuid extends LootItemConditionalFunction {
	public static final MapCodec<BindUuid> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(f -> f.predicates)
	).apply(i, BindUuid::new));

	protected BindUuid(List<LootItemCondition> conditionsIn) {
		super(conditionsIn);
	}

	@NotNull
	@Override
	protected ItemStack run(@NotNull ItemStack stack, @NotNull LootContext context) {
		if (context.getOptionalParameter(LootContextParams.ATTACKING_ENTITY) instanceof net.minecraft.world.entity.player.Player player) {
			var relic = XplatAbstractions.INSTANCE.findRelic(stack);
			if (relic != null) {
				relic.bindToUUID(player.getUUID());
			}
		}

		return stack;
	}

	@Override
	public MapCodec<? extends LootItemConditionalFunction> codec() {
		return CODEC;
	}
}
