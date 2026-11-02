/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;

import vazkii.botania.api.BotaniaAPI;

import java.util.List;

/**
 * Shared helpers for Botania's recipe serializers.
 */
public final class RecipeSerializers {
	/**
	 * Placeholder id used for recipes that are created by a codec. In MC 26.1 the id of a recipe is
	 * owned by the recipe holder, so a recipe created by a codec cannot know its own id - it gets
	 * assigned once the recipe manager has loaded it.
	 */
	public static final Identifier UNKNOWN_ID = Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "unknown");

	private RecipeSerializers() {
	}

	public static StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> ingredientList() {
		return Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list());
	}

	public static StreamCodec<RegistryFriendlyByteBuf, List<ItemStack>> itemStackList() {
		return ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list());
	}

	/**
	 * Stream codec for a list of item stack templates.
	 *
	 * <p>26.1: recipes that are sent over the network have to carry their result as an
	 * {@link net.minecraft.world.item.ItemStackTemplate} rather than a materialized
	 * {@link ItemStack}, because the client may receive the packet before item components have been
	 * bound.
	 */
	public static StreamCodec<RegistryFriendlyByteBuf, List<ItemStackTemplate>> itemStackTemplateList() {
		return ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list());
	}
}
