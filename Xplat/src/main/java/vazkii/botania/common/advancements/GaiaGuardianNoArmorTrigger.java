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
import net.minecraft.advancements.criterion.DamageSourcePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class GaiaGuardianNoArmorTrigger extends SimpleCriterionTrigger<GaiaGuardianNoArmorTrigger.Instance> {
	public static final Identifier ID = prefix("gaia_guardian_no_armor");
	public static final GaiaGuardianNoArmorTrigger INSTANCE = new GaiaGuardianNoArmorTrigger();

	private GaiaGuardianNoArmorTrigger() {}

	@Override
	public Codec<GaiaGuardianNoArmorTrigger.Instance> codec() {
		return GaiaGuardianNoArmorTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, GaiaGuardianEntity guardian, DamageSource src) {
		trigger(player, instance -> instance.test(player, guardian, src));
	}

	public static Criterion<GaiaGuardianNoArmorTrigger.Instance> noArmor(EntityPredicate guardian, DamageSourcePredicate killingBlow) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(guardian), Optional.ofNullable(killingBlow)));
	}

	public record Instance(Optional<ContextAwarePredicate> player, Optional<EntityPredicate> guardian,
			Optional<DamageSourcePredicate> killingBlow) implements SimpleCriterionTrigger.SimpleInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						EntityPredicate.CODEC.optionalFieldOf("guardian").forGetter(Instance::guardian),
						DamageSourcePredicate.CODEC.optionalFieldOf("killing_blow").forGetter(Instance::killingBlow)
				).apply(i, Instance::new)
		);

		public boolean test(ServerPlayer player, GaiaGuardianEntity guardian, DamageSource src) {
			return (this.guardian.isEmpty() || this.guardian.get().matches(player, guardian))
					&& (this.killingBlow.isEmpty() || this.killingBlow.get().matches(player, src));
		}
	}
}
