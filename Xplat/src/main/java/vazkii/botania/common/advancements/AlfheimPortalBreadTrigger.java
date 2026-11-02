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
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AlfheimPortalBreadTrigger extends SimpleCriterionTrigger<AlfheimPortalBreadTrigger.Instance> {
	public static final Identifier ID = prefix("alf_portal_bread");
	public static final AlfheimPortalBreadTrigger INSTANCE = new AlfheimPortalBreadTrigger();

	private AlfheimPortalBreadTrigger() {}

	@Override
	public Codec<AlfheimPortalBreadTrigger.Instance> codec() {
		return AlfheimPortalBreadTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, BlockPos portal) {
		this.trigger(player, instance -> instance.test(player.level(), portal));
	}

	public static Criterion<AlfheimPortalBreadTrigger.Instance> eatBread(LocationPredicate portal) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(portal)));
	}

	public record Instance(Optional<ContextAwarePredicate> player,
			Optional<LocationPredicate> portal) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						LocationPredicate.CODEC.optionalFieldOf("portal_location").forGetter(Instance::portal)
				).apply(i, Instance::new)
		);

		public boolean test(ServerLevel world, BlockPos portal) {
			return this.portal.isEmpty() || this.portal.get().matches(world, portal.getX(), portal.getY(), portal.getZ());
		}
	}
}
