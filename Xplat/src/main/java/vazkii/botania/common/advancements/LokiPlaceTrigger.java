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
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class LokiPlaceTrigger extends SimpleCriterionTrigger<LokiPlaceTrigger.Instance> {
	public static final Identifier ID = prefix("loki_placed_blocks");
	public static final LokiPlaceTrigger INSTANCE = new LokiPlaceTrigger();

	private LokiPlaceTrigger() {}

	@Override
	public Codec<LokiPlaceTrigger.Instance> codec() {
		return LokiPlaceTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack ring, int blocksPlaced) {
		trigger(player, instance -> instance.test(player, ring, blocksPlaced));
	}

	public static Criterion<LokiPlaceTrigger.Instance> placeBlocks(EntityPredicate playerEntity, ItemPredicate ring, MinMaxBounds.Ints blocksPlaced) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(playerEntity),
				Optional.ofNullable(ring), Optional.ofNullable(blocksPlaced)));
	}

	public record Instance(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> playerEntity,
			Optional<ItemPredicate> ring, Optional<MinMaxBounds.Ints> blocksPlaced) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						EntityPredicate.CODEC.optionalFieldOf("player_entity").forGetter(Instance::playerEntity),
						ItemPredicate.CODEC.optionalFieldOf("ring").forGetter(Instance::ring),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("blocks_placed").forGetter(Instance::blocksPlaced)
				).apply(i, Instance::new)
		);

		public boolean test(ServerPlayer player, ItemStack ring, int blocksPlaced) {
			return (this.playerEntity.isEmpty() || this.playerEntity.get().matches(player, null))
					&& (this.ring.isEmpty() || this.ring.get().test(ring))
					&& (this.blocksPlaced.isEmpty() || this.blocksPlaced.get().matches(blocksPlaced));
		}
	}
}
