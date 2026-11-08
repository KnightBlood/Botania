/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.integration.rei;

import com.google.common.collect.ImmutableList;

import dev.architectury.utils.GameInstance;

import me.shedaniel.rei.api.common.display.DisplaySerializerRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.api.common.registry.display.ServerDisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.recipe.BotanicalBreweryRecipe;
import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.OrechidRecipe;
import vazkii.botania.api.recipe.PetalApothecaryRecipe;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.api.recipe.StateIngredient;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.common.block.block_entity.AlfheimPortalBlockEntity;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.LexiconElvenTradeRecipe;
import vazkii.botania.common.handler.OrechidManager;
import vazkii.botania.common.helper.RecipeHelper;
import vazkii.botania.common.item.BotaniaItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Server-side REI plugin: builds Botania's ten custom categories from the real
 * {@code RecipeManager} and registers a {@code DisplaySerializer} so REI can sync them with
 * {@code DisplaySyncPacket}.
 *
 * <p>
 * MC 26.1 only syncs vanilla {@code RecipeDisplay}s to clients; Botania's special recipes
 * have none, so without this plugin the ten categories are empty on a dedicated server. REI
 * itself skips its own recipe fill when {@code GameInstance.getServer()} is null
 * ({@code ServerDisplayRegistryImpl#fillRecipes}), and this plugin follows the same convention:
 * a client connected to a remote server registers nothing locally and simply receives the
 * server's displays.
 *
 * <p>
 * The client plugin still builds the same displays locally in singleplayer (where the
 * integrated server's {@code RecipeManager} is available) so the biome-sensitive Orechid
 * tooltip keeps its 1.20.1 behaviour; {@code BotaniaREIPlugin} hides synced duplicates by
 * recipe id. On a dedicated server only the synced displays remain, carrying the generic
 * chance computed here - see {@link BotaniaSyncedDisplay} for the explicit limitation.
 *
 * <p>
 * <b>Explicit requirement:</b> REI must also be installed on the dedicated server. Without it
 * there is no {@code REICommonPlugin} and no {@code DisplaySyncPacket}, so the ten categories stay
 * empty on a remote client; Botania deliberately does not invent a second, private sync channel.
 *
 * <p>
 * <b>Known dev-only limitation:</b> a NeoForge {@code runServer} cannot be used to verify this
 * plugin. REI 26.1.819's own {@code DefaultRuntimePlugin} instantiates
 * {@code ItemEntryDefinition}, whose only {@code LocalPlayer} reference sits in a method annotated
 * {@code @OnlyIn(Dist.CLIENT)}. Production FML dist cleaning strips that method; the dev-only
 * {@code NeoForgeDevDistCleaner} instead rejects the whole class, so REI never registers the
 * {@code minecraft:item} entry definition and every {@code EntryIngredients} call fails. The guard
 * in {@link #registerDisplays} detects that broken state and logs a warning rather than crashing.
 */
public class BotaniaREICommonPlugin implements REICommonPlugin {
	@Override
	public void registerDisplaySerializer(DisplaySerializerRegistry registry) {
		registry.register(BotaniaSyncedDisplay.SERIALIZER_ID, BotaniaSyncedDisplay.SERIALIZER);
		BotaniaAPI.LOGGER.info("Botania REI: server display serializer registered");
	}

	@Override
	public void registerDisplays(ServerDisplayRegistry registry) {
		MinecraftServer server = GameInstance.getServer();
		if (server == null) {
			// No local server: displays arrive over the network from the remote server.
			return;
		}
		if (EntryTypeRegistry.getInstance().get(VanillaEntryTypes.ITEM.getId()) == null) {
			// REI's DefaultRuntimePlugin failed to register the vanilla item definition (NeoForge dev
			// runServer, see the class Javadoc). Building EntryIngredients would throw for every recipe.
			// This is an explicit, loudly logged degradation: the ten categories stay empty on clients
			// connected to such a server until REI itself is fixed / run in production.
			BotaniaAPI.LOGGER.warn("Botania REI: REI has not registered the minecraft:item entry definition "
					+ "on this server, so server-side display registration is skipped. This is expected in a "
					+ "NeoForge dev runServer (FML dev dist cleaner vs REI's client-annotated "
					+ "ItemEntryDefinition); production dedicated servers are unaffected.");
			return;
		}
		Level level = server.overworld();
		int count = 0;
		for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
			BotaniaSyncedDisplay display = create(holder, level);
			if (display != null) {
				registry.add(display);
				count++;
			}
		}
		BotaniaAPI.LOGGER.info("Botania REI: registered {} server-side displays for synchronisation", count);
	}

	@Nullable
	private static BotaniaSyncedDisplay create(RecipeHolder<?> holder, Level level) {
		Recipe<?> recipe = holder.value();
		RecipeType<?> type = recipe.getType();
		if (!isBotaniaRecipe(type)) {
			// A dedicated server's RecipeManager holds every vanilla recipe too. Filter first so
			// reloads do not build EntryIngredients (and resolve recipe displays) for ~3000 recipes
			// that this plugin would discard anyway.
			return null;
		}
		Optional<Identifier> location = Optional.of(holder.id().identifier());

		List<EntryIngredient> inputs = EntryIngredients.ofIngredients(RecipeHelper.getIngredients(recipe));
		List<EntryIngredient> outputs = List.of(EntryIngredients.of(RecipeHelper.getResultItem(recipe, level)));
		int mana = 0;
		Optional<EntryIngredient> containers = Optional.empty();
		Optional<EntryIngredient> reagent = Optional.empty();
		Optional<EntryIngredient> catalyst = Optional.empty();
		Optional<Double> chance = Optional.empty();
		Identifier category;

		if (type == BotaniaRecipeTypes.PETAL_TYPE) {
			category = PetalApothecaryRecipe.TYPE_ID;
			reagent = Optional.of(EntryIngredients.ofIngredient(((PetalApothecaryRecipe) recipe).getReagent()));
		} else if (type == BotaniaRecipeTypes.BREW_TYPE) {
			category = BotanicalBreweryRecipe.TYPE_ID;
			BotanicalBreweryRecipe brew = (BotanicalBreweryRecipe) recipe;
			List<ItemStack> vessels = List.of(new ItemStack(BotaniaItems.vial), new ItemStack(BotaniaItems.flask),
					new ItemStack(BotaniaItems.incenseStick), new ItemStack(BotaniaItems.bloodPendant));
			containers = Optional.of(EntryIngredients.ofItemStacks(vessels));
			List<ItemStack> brewed = new ArrayList<>();
			for (ItemStack vessel : vessels) {
				ItemStack result = brew.getOutput(vessel);
				if (!result.isEmpty()) {
					brewed.add(result);
				}
			}
			outputs = List.of(EntryIngredients.ofItemStacks(brewed));
			mana = brew.getManaUsage();
		} else if (type == BotaniaRecipeTypes.ELVEN_TRADE_TYPE) {
			ElvenTradeRecipe trade = (ElvenTradeRecipe) recipe;
			if (!(recipe instanceof LexiconElvenTradeRecipe) && trade.isReturnRecipe()) {
				return null;
			}
			category = ElvenTradeRecipe.TYPE_ID;
			outputs = List.of(EntryIngredients.ofItemStacks(trade.getOutputs()));
			mana = AlfheimPortalBlockEntity.MANA_COST;
		} else if (type == BotaniaRecipeTypes.MANA_INFUSION_TYPE) {
			category = ManaInfusionRecipe.TYPE_ID;
			ManaInfusionRecipe infusion = (ManaInfusionRecipe) recipe;
			StateIngredient stateIngredient = infusion.getRecipeCatalyst();
			if (stateIngredient != null) {
				List<EntryStack<?>> entries = new ArrayList<>();
				for (BlockState state : stateIngredient.getDisplayed()) {
					entries.add(EntryStacks.of(state.getBlock()));
				}
				catalyst = Optional.of(EntryIngredient.of(entries));
			}
			mana = infusion.getManaToConsume();
		} else if (type == BotaniaRecipeTypes.PURE_DAISY_TYPE) {
			category = PureDaisyRecipe.TYPE_ID;
			PureDaisyRecipe pureDaisy = (PureDaisyRecipe) recipe;
			ImmutableList.Builder<EntryStack<?>> builder = ImmutableList.builder();
			for (BlockState state : pureDaisy.getInput().getDisplayed()) {
				if (!state.getFluidState().isEmpty()) {
					builder.add(EntryStacks.of(state.getFluidState().getType()));
				} else {
					builder.add(EntryStacks.of(state.getBlock()));
				}
			}
			inputs = List.of(EntryIngredient.of(builder.build()));
			outputs = List.of(EntryIngredients.of(pureDaisy.getOutputState().getBlock()));
		} else if (type == BotaniaRecipeTypes.RUNE_TYPE) {
			category = RunicAltarRecipe.TYPE_ID;
			mana = ((RunicAltarRecipe) recipe).getManaUsage();
		} else if (type == BotaniaRecipeTypes.TERRA_PLATE_TYPE) {
			category = TerrestrialAgglomerationRecipe.TYPE_ID;
			mana = ((TerrestrialAgglomerationRecipe) recipe).getMana();
		} else if (type == BotaniaRecipeTypes.ORECHID_TYPE
				|| type == BotaniaRecipeTypes.ORECHID_IGNEM_TYPE
				|| type == BotaniaRecipeTypes.MARIMORPHOSIS_TYPE) {
			OrechidRecipe orechid = (OrechidRecipe) recipe;
			if (type == BotaniaRecipeTypes.ORECHID_IGNEM_TYPE) {
				category = OrechidRecipe.IGNEM_TYPE_ID;
			} else if (type == BotaniaRecipeTypes.MARIMORPHOSIS_TYPE) {
				category = OrechidRecipe.MARIMORPHOSIS_TYPE_ID;
			} else {
				category = OrechidRecipe.TYPE_ID;
			}
			inputs = List.of(EntryIngredients.ofItemStacks(orechid.getInput().getDisplayedStacks()));
			outputs = List.of(EntryIngredients.ofItemStacks(orechid.getOutput().getDisplayedStacks()));
			chance = Optional.ofNullable(genericChance(orechid, level));
		} else {
			return null;
		}

		return new BotaniaSyncedDisplay(category, inputs, outputs, location, mana,
				containers, reagent, catalyst, chance);
	}

	private static boolean isBotaniaRecipe(RecipeType<?> type) {
		return type == BotaniaRecipeTypes.PETAL_TYPE
				|| type == BotaniaRecipeTypes.BREW_TYPE
				|| type == BotaniaRecipeTypes.ELVEN_TRADE_TYPE
				|| type == BotaniaRecipeTypes.MANA_INFUSION_TYPE
				|| type == BotaniaRecipeTypes.PURE_DAISY_TYPE
				|| type == BotaniaRecipeTypes.RUNE_TYPE
				|| type == BotaniaRecipeTypes.TERRA_PLATE_TYPE
				|| type == BotaniaRecipeTypes.ORECHID_TYPE
				|| type == BotaniaRecipeTypes.ORECHID_IGNEM_TYPE
				|| type == BotaniaRecipeTypes.MARIMORPHOSIS_TYPE;
	}

	/**
	 * Position-independent Orechid chance, mirroring {@code OrechidUIHelper.getChance(recipe, null)}
	 * but with the server's overworld instead of {@code Minecraft.getInstance().level}.
	 */
	@Nullable
	private static Double genericChance(OrechidRecipe recipe, Level level) {
		try {
			List<BlockState> displayed = recipe.getInput().getDisplayed();
			if (displayed.isEmpty()) {
				return null;
			}
			int total = OrechidManager.getTotalDisplayWeightAt(level, recipe.getType(), displayed.get(0), null);
			return total > 0 ? (double) recipe.getWeight() / total : null;
		} catch (RuntimeException e) {
			BotaniaAPI.LOGGER.warn("Botania REI: could not compute a server-side Orechid chance", e);
			return null;
		}
	}
}
