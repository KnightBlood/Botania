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

public class ManaBlasterTrigger extends SimpleCriterionTrigger<ManaBlasterTrigger.Instance> {
	public static final Identifier ID = prefix("fire_mana_blaster");
	public static final ManaBlasterTrigger INSTANCE = new ManaBlasterTrigger();

	private ManaBlasterTrigger() {}

	@Override
	public Codec<ManaBlasterTrigger.Instance> codec() {
		return ManaBlasterTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack stack) {
		trigger(player, instance -> instance.test(stack, player));
	}

	public static Criterion<ManaBlasterTrigger.Instance> fire(ItemPredicate item, EntityPredicate user) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(item), Optional.ofNullable(user)));
	}

	public record Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item,
			Optional<EntityPredicate> user) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						ItemPredicate.CODEC.optionalFieldOf("item").forGetter(Instance::item),
						EntityPredicate.CODEC.optionalFieldOf("user").forGetter(Instance::user)
				).apply(i, Instance::new)
		);

		public boolean test(ItemStack stack, ServerPlayer entity) {
			return (this.item.isEmpty() || this.item.get().test(stack))
					&& (this.user.isEmpty() || this.user.get().matches(entity, entity));
		}
	}
}
