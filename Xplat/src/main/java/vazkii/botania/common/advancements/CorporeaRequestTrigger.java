/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class CorporeaRequestTrigger extends SimpleCriterionTrigger<CorporeaRequestTrigger.Instance> {
	public static final Identifier ID = prefix("corporea_index_request");
	public static final CorporeaRequestTrigger INSTANCE = new CorporeaRequestTrigger();

	private CorporeaRequestTrigger() {}

	@Override
	public Codec<CorporeaRequestTrigger.Instance> codec() {
		return CorporeaRequestTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ServerLevel world, BlockPos pos, int count) {
		trigger(player, instance -> instance.test(world, pos, count));
	}

	public static Criterion<CorporeaRequestTrigger.Instance> request(MinMaxBounds.Ints count, LocationPredicate location) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(count), Optional.ofNullable(location)));
	}

	public record Instance(Optional<ContextAwarePredicate> player, Optional<MinMaxBounds.Ints> count,
			Optional<LocationPredicate> location) implements SimpleCriterionTrigger.SimpleInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("extracted").forGetter(Instance::count),
						LocationPredicate.CODEC.optionalFieldOf("location").forGetter(Instance::location)
				).apply(i, Instance::new)
		);

		public boolean test(ServerLevel world, BlockPos pos, int count) {
			return (this.count.isEmpty() || this.count.get().matches(count))
					&& (this.location.isEmpty() || this.location.get().matches(world, pos.getX(), pos.getY(), pos.getZ()));
		}
	}
}
