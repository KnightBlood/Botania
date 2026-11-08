package vazkii.botania.common.crafting.recipe;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.function.Function;

/**
 * Serializer for dynamic recipes that don't have a json/network representation.
 * The recipe type fully identifies the recipe.
 * <p>
 * In MC 26.1 {@link RecipeSerializer} is a record, so this cannot be a serializer itself anymore.
 * Use {@link #create(Function)} to obtain one instead.
 */
public final class NoOpRecipeSerializer {
	private static final Identifier PLACEHOLDER_ID = Identifier.parse("botania:dynamic_recipe");

	private NoOpRecipeSerializer() {}

	public static <T extends Recipe<?>> RecipeSerializer<T> create(Function<Identifier, T> constructor) {
		MapCodec<T> codec = MapCodec.unit(() -> constructor.apply(PLACEHOLDER_ID));
		StreamCodec<RegistryFriendlyByteBuf, T> streamCodec = new StreamCodec<>() {
			@Override
			public T decode(RegistryFriendlyByteBuf buffer) {
				return constructor.apply(PLACEHOLDER_ID);
			}

			@Override
			public void encode(RegistryFriendlyByteBuf buffer, T recipe) {}
		};
		return new RecipeSerializer<>(codec, streamCodec);
	}
}
