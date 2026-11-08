/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.integration.rei;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A single serialisable display covering all ten Botania REI categories.
 *
 * <p>
 * REI 26.1's {@code ServerDisplayRegistryImpl.add()} rejects any display whose
 * {@code getSerializer()} is not registered with {@code DisplaySerializerRegistry}, and the
 * {@code DisplaySyncPacket} uses the generic {@code Display.streamCodec()} to send displays to
 * clients. One shared data class with one {@code MapCodec} + {@code StreamCodec} pair is enough
 * because every category reads only through {@link BotaniaREIView}; the category id selects the
 * layout on the client.
 *
 * <p>
 * <b>Explicit limitation (not a silent downgrade):</b> a synced display cannot carry an
 * {@code OrechidRecipe} object. Orechid/Marimorphosis therefore show the generic chance computed
 * on the server with the overworld and no player position, and Marimorphosis' biome-bonus tooltip
 * (which needs the player's coordinates and the full recipe list) is omitted for synced displays.
 * In singleplayer the client-built displays are still registered and take precedence, so that
 * tooltip is unchanged there.
 *
 * <p>
 * This class lives in the common package and must never reference a
 * {@code net.minecraft.client} type: dedicated servers load it to encode displays.
 */
public final class BotaniaSyncedDisplay implements Display, BotaniaREIView {
	public static final Identifier SERIALIZER_ID = Identifier.fromNamespaceAndPath("botania", "synced_display");

	private static final MapCodec<BotaniaSyncedDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Identifier.CODEC.fieldOf("category").forGetter(d -> d.category),
			EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(Display::getInputEntries),
			EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(Display::getOutputEntries),
			Identifier.CODEC.optionalFieldOf("location").forGetter(Display::getDisplayLocation),
			Codec.INT.fieldOf("mana").forGetter(BotaniaSyncedDisplay::getManaCost),
			EntryIngredient.codec().optionalFieldOf("containers").forGetter(d -> d.containers),
			EntryIngredient.codec().optionalFieldOf("reagent").forGetter(d -> d.reagent),
			EntryIngredient.codec().optionalFieldOf("catalyst").forGetter(d -> d.catalyst),
			Codec.DOUBLE.optionalFieldOf("chance").forGetter(d -> d.chance)
	).apply(instance, BotaniaSyncedDisplay::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, BotaniaSyncedDisplay> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, d -> d.category,
			EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), Display::getInputEntries,
			EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), Display::getOutputEntries,
			ByteBufCodecs.optional(Identifier.STREAM_CODEC), Display::getDisplayLocation,
			ByteBufCodecs.VAR_INT, BotaniaSyncedDisplay::getManaCost,
			ByteBufCodecs.optional(EntryIngredient.streamCodec()), d -> d.containers,
			ByteBufCodecs.optional(EntryIngredient.streamCodec()), d -> d.reagent,
			ByteBufCodecs.optional(EntryIngredient.streamCodec()), d -> d.catalyst,
			ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), d -> d.chance,
			BotaniaSyncedDisplay::new);

	public static final DisplaySerializer<BotaniaSyncedDisplay> SERIALIZER = DisplaySerializer.of(CODEC, STREAM_CODEC);

	private final Identifier category;
	private final List<EntryIngredient> inputs;
	private final List<EntryIngredient> outputs;
	private final Optional<Identifier> location;
	private final int manaCost;
	private final Optional<EntryIngredient> containers;
	private final Optional<EntryIngredient> reagent;
	private final Optional<EntryIngredient> catalyst;
	private final Optional<Double> chance;

	public BotaniaSyncedDisplay(Identifier category, List<EntryIngredient> inputs, List<EntryIngredient> outputs,
			Optional<Identifier> location, int manaCost, Optional<EntryIngredient> containers,
			Optional<EntryIngredient> reagent, Optional<EntryIngredient> catalyst, Optional<Double> chance) {
		this.category = category;
		this.inputs = List.copyOf(inputs);
		this.outputs = List.copyOf(outputs);
		this.location = location;
		this.manaCost = manaCost;
		this.containers = containers;
		this.reagent = reagent;
		this.catalyst = catalyst;
		this.chance = chance;
	}

	@Override
	public @NotNull List<EntryIngredient> getInputEntries() {
		return inputs;
	}

	@Override
	public @NotNull List<EntryIngredient> getOutputEntries() {
		return outputs;
	}

	@Override
	public @NotNull CategoryIdentifier<?> getCategoryIdentifier() {
		return CategoryIdentifier.of(category);
	}

	@Override
	public @NotNull Optional<Identifier> getDisplayLocation() {
		return location;
	}

	@Override
	public @Nullable DisplaySerializer<? extends Display> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public int getManaCost() {
		return manaCost;
	}

	@Override
	public @Nullable EntryIngredient getContainers() {
		return containers.orElse(null);
	}

	@Override
	public @Nullable EntryIngredient getReagent() {
		return reagent.orElse(null);
	}

	@Override
	public @Nullable EntryIngredient getCatalystEntries() {
		return catalyst.orElse(null);
	}

	@Override
	public @Nullable Double getSyncedChance() {
		return chance.orElse(null);
	}
}
