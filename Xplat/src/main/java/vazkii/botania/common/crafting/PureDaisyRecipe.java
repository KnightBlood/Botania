/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.crafting;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CacheableFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.api.recipe.BotaniaRecipe;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.common.crafting.recipe.RecipeSerializers;

import java.util.Optional;

public class PureDaisyRecipe implements vazkii.botania.api.recipe.PureDaisyRecipe {

	public static final int DEFAULT_TIME = 150;

	private Identifier id;
	protected final StateIngredient input;
	protected final BlockState outputState;
	private final int time;
	private final @Nullable CacheableFunction function;

	/**
	 * @param id       The ID for this recipe.
	 * @param input    The input for the recipe. Can be a Block, BlockState, or Tag&lt;Block&gt;.
	 * @param state    The blockstate to be placed upon recipe completion.
	 * @param time     The amount of time in ticks to complete this recipe. Note that this is ticks on your block, not
	 *                 total time.
	 *                 The Pure Daisy only ticks one block at a time in a round robin fashion.
	 * @param function An mcfunction to run at the converted block after finish. If you don't want one, pass
	 *                 null
	 */
	public PureDaisyRecipe(Identifier id, StateIngredient input, BlockState state, int time, @Nullable CacheableFunction function) {
		Preconditions.checkArgument(time >= 0, "Time must be nonnegative");
		this.id = id;
		this.input = input;
		this.outputState = state;
		this.time = time;
		this.function = function;
	}

	@Override
	public boolean matches(Level world, BlockPos pos, SpecialFlowerBlockEntity pureDaisy, BlockState state) {
		return input.test(state) && outputState != state;
	}

	@Override
	public boolean set(Level world, BlockPos pos, SpecialFlowerBlockEntity pureDaisy) {
		if (!world.isClientSide()) {
			boolean success = world.setBlockAndUpdate(pos, outputState);
			if (success) {
				var serverLevel = (ServerLevel) world;
				var server = serverLevel.getServer();
				if (this.function != null) {
					this.function.get(server.getFunctions()).ifPresent(command -> {
						var context = server.getFunctions().getGameLoopSender()
								.withLevel((ServerLevel) world)
								.withPosition(Vec3.atBottomCenterOf(pos));
						server.getFunctions().execute(command, context);
					});
				}
			}
			return success;
		}
		return true;
	}

	@Override
	public StateIngredient getInput() {
		return input;
	}

	@Override
	public BlockState getOutputState() {
		return outputState;
	}

	@Override
	public @Nullable CacheableFunction getSuccessFunction() {
		return this.function;
	}

	@Override
	public int getTime() {
		return time;
	}

	public Identifier getId() {
		return id;
	}

	@Override
	public RecipeSerializer<? extends BotaniaRecipe<RecipeInput>> getSerializer() {
		return BotaniaRecipeTypes.PURE_DAISY_SERIALIZER;
	}

	public static final MapCodec<PureDaisyRecipe> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StateIngredientHelper.CODEC.fieldOf("input").forGetter(r -> r.input),
			StateIngredientHelper.BLOCK_STATE_CODEC.fieldOf("output").forGetter(r -> r.outputState),
			Codec.INT.optionalFieldOf("time", DEFAULT_TIME).forGetter(r -> r.time),
			CacheableFunction.CODEC.optionalFieldOf("success_function").forGetter(r -> Optional.ofNullable(r.function))
	).apply(i, (input, output, time, function) -> new PureDaisyRecipe(RecipeSerializers.UNKNOWN_ID,
			input, output, time, function.orElse(null))));

	public static final StreamCodec<RegistryFriendlyByteBuf, PureDaisyRecipe> STREAM_CODEC = StreamCodec.composite(
			StateIngredientHelper.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.VAR_INT, r -> Block.getId(r.outputState),
			ByteBufCodecs.VAR_INT, r -> r.time,
			(input, output, time) -> new PureDaisyRecipe(RecipeSerializers.UNKNOWN_ID,
					input, Block.stateById(output), time, null)
	);

	public static final RecipeSerializer<PureDaisyRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

}
