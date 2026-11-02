/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import vazkii.botania.common.item.BotaniaItems;

@Mixin(ItemPredicate.class)
public abstract class ItemPredicateFabricMixin {
	// 26.1: the item set constructor parameter became Optional<HolderSet<Item>>
	@ModifyVariable(at = @At("HEAD"), method = "<init>(Ljava/util/Optional;Lnet/minecraft/advancements/criterion/MinMaxBounds$Ints;Lnet/minecraft/advancements/criterion/DataComponentMatchers;)V", argsOnly = true)
	private static Optional<HolderSet<Item>> addBotaniaShears(Optional<HolderSet<Item>> optional) {
		if (optional.isEmpty()) {
			return optional;
		}
		HolderSet<Item> holders = optional.get();
		// Only expand explicit item lists; a named tag's contents aren't resolvable here
		return holders.unwrap().map(
				(TagKey<Item> tag) -> optional,
				(List<Holder<Item>> list) -> {
					if (list.stream().anyMatch(h -> h.value() == Items.SHEARS)) {
						Set<Item> set = list.stream().map(Holder::value).collect(Collectors.toCollection(HashSet::new));
						set.add(BotaniaItems.manasteelShears);
						set.add(BotaniaItems.elementiumShears);
						return Optional.<HolderSet<Item>>of(HolderSet.direct(
								ImmutableSet.copyOf(set).stream().map(Item::builtInRegistryHolder).collect(Collectors.toList())));
					}
					return optional;
				});
	}
}
