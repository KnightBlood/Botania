/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting.recipe;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.MapCodec;
import com.google.gson.JsonSyntaxException;

import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.PureDaisyRecipe;
import vazkii.botania.common.crafting.StateIngredientHelper;

/**
 * Recipe that copies state properties to the new block on crafting.
 */
public class StateCopyingPureDaisyRecipe extends PureDaisyRecipe {
	public StateCopyingPureDaisyRecipe(Identifier id, StateIngredient input, Block block, int time) {
		super(id, input, block.defaultBlockState(), time, null);
	}

	@Override
	public boolean matches(Level world, BlockPos pos, SpecialFlowerBlockEntity pureDaisy, BlockState state) {
		return input.test(state) && outputState.getBlock().withPropertiesOf(state) != state;
	}

	@Override
	public boolean set(Level world, BlockPos pos, SpecialFlowerBlockEntity pureDaisy) {
		if (!world.isClientSide()) {
			Block block = getOutputState().getBlock();
			world.setBlockAndUpdate(pos, block.withPropertiesOf(world.getBlockState(pos)));
		}
		return true;
	}

	public static final MapCodec<StateCopyingPureDaisyRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StateIngredientHelper.CODEC.fieldOf("input").forGetter(r -> r.input),
			Identifier.CODEC.fieldOf("output").forGetter(r -> BuiltInRegistries.BLOCK.getKey(r.outputState.getBlock())),
			Codec.INT.optionalFieldOf("time", DEFAULT_TIME).forGetter(r -> r.getTime())
	).apply(i, (input, output, time) -> new StateCopyingPureDaisyRecipe(RecipeSerializers.UNKNOWN_ID,
			input, BuiltInRegistries.BLOCK.getValue(output), time)));

	public static final StreamCodec<RegistryFriendlyByteBuf, StateCopyingPureDaisyRecipe> STREAM_CODEC = StreamCodec.composite(
			StateIngredientHelper.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.VAR_INT, r -> BuiltInRegistries.BLOCK.getId(r.outputState.getBlock()),
			ByteBufCodecs.VAR_INT, r -> r.getTime(),
			(input, output, time) -> new StateCopyingPureDaisyRecipe(RecipeSerializers.UNKNOWN_ID,
					input, BuiltInRegistries.BLOCK.byId(output), time)
	);

	public static final RecipeSerializer<StateCopyingPureDaisyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
