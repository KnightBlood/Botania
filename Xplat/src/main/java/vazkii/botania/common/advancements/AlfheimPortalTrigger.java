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
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class AlfheimPortalTrigger extends SimpleCriterionTrigger<AlfheimPortalTrigger.Instance> {
	public static final Identifier ID = prefix("open_elf_portal");
	public static final AlfheimPortalTrigger INSTANCE = new AlfheimPortalTrigger();

	private AlfheimPortalTrigger() {}

	@Override
	public Codec<AlfheimPortalTrigger.Instance> codec() {
		return AlfheimPortalTrigger.Instance.CODEC;
	}

	public void trigger(ServerPlayer player, ServerLevel world, BlockPos pos, ItemStack wand) {
		trigger(player, instance -> instance.test(world, pos, wand));
	}

	public static Criterion<AlfheimPortalTrigger.Instance> openPortal(ItemPredicate wand, LocationPredicate location) {
		return INSTANCE.createCriterion(new Instance(Optional.empty(), Optional.ofNullable(wand), Optional.ofNullable(location)));
	}

	public record Instance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> wand,
			Optional<LocationPredicate> location) implements SimpleCriterionTrigger.SimpleInstance {

		public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
				i -> i.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
						ItemPredicate.CODEC.optionalFieldOf("wand").forGetter(Instance::wand),
						LocationPredicate.CODEC.optionalFieldOf("location").forGetter(Instance::location)
				).apply(i, Instance::new)
		);

		public boolean test(ServerLevel world, BlockPos pos, ItemStack wand) {
			return (this.wand.isEmpty() || this.wand.get().test(wand))
					&& (this.location.isEmpty() || this.location.get().matches(world, pos.getX(), pos.getY(), pos.getZ()));
		}
	}
}
