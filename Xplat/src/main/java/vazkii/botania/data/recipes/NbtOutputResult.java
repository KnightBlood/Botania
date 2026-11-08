/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.data.recipes;

import com.google.gson.JsonObject;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.crafting.recipe.NbtOutputRecipe;

public class NbtOutputResult implements BotaniaFinishedRecipe {
	private final BotaniaFinishedRecipe innerRecipe;
	private final CompoundTag tag;

	public NbtOutputResult(BotaniaFinishedRecipe innerRecipe, CompoundTag tag) {
		this.innerRecipe = innerRecipe;
		this.tag = tag;
	}

	@Override
	public void serializeRecipeData(JsonObject json) {
		json.add("recipe", innerRecipe.serializeRecipe());
		json.addProperty("nbt", tag.toString());
	}

	public Identifier getId() {
		return innerRecipe.getId();
	}

	@Override
	public RecipeSerializer<?> getType() {
		return NbtOutputRecipe.SERIALIZER;
	}

	@Nullable
	@Override
	public JsonObject serializeAdvancement() {
		return innerRecipe.serializeAdvancement();
	}

	@Nullable
	@Override
	public Identifier getAdvancementId() {
		return innerRecipe.getAdvancementId();
	}
}
