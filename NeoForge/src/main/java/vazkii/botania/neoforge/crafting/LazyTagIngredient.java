/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.neoforge.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import java.util.stream.Stream;

/**
 * 26.1 vanilla datagen resolves tag ingredients eagerly via
 * {@code BuiltInRegistries.ITEM.getOrThrow(tag)} at builder construction time, which throws
 * for tags that no datagen-time provider exists for - notably {@code c:} common tags that
 * only third-party mods would populate (e.g. {@code c:storage_blocks/quartz}, which 1.20.1
 * Botania emitted as {@code "#c:storage_blocks/quartz"}).
 * <p>
 * This ingredient keeps the tag reference lazy: it serializes to
 * {@code {"type": "botania:tag", "tag": "&lt;id&gt;"}} and matches stacks directly against
 * the tag, so runtime behavior is identical to a vanilla tag ingredient even if the tag is
 * still empty at recipe-load time.
 */
public final class LazyTagIngredient implements ICustomIngredient {
	public static final MapCodec<LazyTagIngredient> CODEC = RecordCodecBuilder.mapCodec(
			i -> i.group(TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(t -> t.tag))
					.apply(i, LazyTagIngredient::new));

	private final TagKey<Item> tag;

	public LazyTagIngredient(TagKey<Item> tag) {
		this.tag = tag;
	}

	public static Ingredient of(TagKey<Item> tag) {
		return new Ingredient(new LazyTagIngredient(tag));
	}

	public TagKey<Item> getTag() {
		return this.tag;
	}

	@Override
	public boolean test(ItemStack stack) {
		return stack.is(this.tag);
	}

	@Override
	public Stream<Holder<Item>> items() {
		return BuiltInRegistries.ITEM.get(this.tag).stream().flatMap(set -> set.stream());
	}

	@Override
	public boolean isSimple() {
		return true;
	}

	@Override
	public IngredientType<?> getType() {
		return NeoForgeBotaniaIngredients.ITEM_TAG.get();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof LazyTagIngredient other && other.tag == this.tag;
	}

	@Override
	public int hashCode() {
		return this.tag.hashCode();
	}

	@Override
	public String toString() {
		return "LazyTagIngredient[" + this.tag.location() + "]";
	}
}
