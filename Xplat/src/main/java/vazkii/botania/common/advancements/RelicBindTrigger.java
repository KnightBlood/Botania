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
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class RelicBindTrigger extends SimpleCriterionTrigger<RelicBindTrigger.Instance> {
	public static final Identifier ID = prefix("relic_bind");
	public static final RelicBindTrigger INSTANCE = new RelicBindTrigger();

	private RelicBindTrigger() {}

	@Override
	public Codec<RelicBindTrigger.Instance> codec() {
		return RelicBindTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack relic) {
		trigger(player, instance -> instance.test(relic));
	}

	public static Criterion<RelicBindTrigger.Instance> bindRelic(ItemPredicate relic) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(relic)));
	}

	public record Instance(Optional<ContextAwarePredicate> player,
			Optional<ItemPredicate> relic) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						ItemPredicate.CODEC.optionalFieldOf("relic").forGetter(Instance::relic)
				).apply(i, Instance::new)
		);

		public boolean test(ItemStack stack) {
			return this.relic.isEmpty() || this.relic.get().test(stack);
		}
	}
}
