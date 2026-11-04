/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.integration.rei;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import me.shedaniel.math.Point;
import me.shedaniel.math.impl.PointHelper;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.config.addon.ConfigAddon;
import me.shedaniel.rei.api.client.config.addon.ConfigAddonRegistry;
import me.shedaniel.rei.api.client.gui.screen.DisplayScreen;
import me.shedaniel.rei.api.client.gui.widgets.Slot;
import me.shedaniel.rei.api.client.overlay.OverlayListWidget;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import me.shedaniel.rei.plugin.common.displays.DefaultStrippingDisplay;
import me.shedaniel.rei.plugin.common.displays.crafting.DefaultCustomDisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.item.AncientWillContainer;
import vazkii.botania.api.recipe.BotanicalBreweryRecipe;
import vazkii.botania.api.recipe.ElvenTradeRecipe;
import vazkii.botania.api.recipe.ManaInfusionRecipe;
import vazkii.botania.api.recipe.PetalApothecaryRecipe;
import vazkii.botania.api.recipe.PureDaisyRecipe;
import vazkii.botania.api.recipe.RunicAltarRecipe;
import vazkii.botania.api.recipe.TerrestrialAgglomerationRecipe;
import vazkii.botania.client.ClothBotaniaConfig;
import vazkii.botania.client.core.handler.CorporeaInputHandler;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.crafting.LexiconElvenTradeRecipe;
import vazkii.botania.common.crafting.MarimorphosisRecipe;
import vazkii.botania.common.crafting.OrechidIgnemRecipe;
import vazkii.botania.common.helper.RecipeHelper;
import vazkii.botania.common.item.AncientWillItem;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.lens.LensItem;
import vazkii.botania.common.lib.BotaniaTags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

/**
 * Shared REI client plugin, compiled into both loader modules.
 *
 * <p>MC 26.1 recipes are only exposed as vanilla {@code RecipeDisplay}s for crafting-grid recipes;
 * Botania's special recipes have no such display, so REI's automatic recipe fillers never see them.
 * Like the EMI integration, the displays are built explicitly in
 * {@link #registerDisplays(DisplayRegistry)} from a real {@code RecipeManager}.
 *
 * <p><b>Known limitation (explicit, not a silent downgrade):</b> MC 26.1 no longer syncs full
 * {@code RecipeHolder}s to clients - a {@code ClientLevel} only exposes {@code RecipeAccess}
 * (property sets + stonecutter recipes). In singleplayer, including dev runs, the integrated
 * server is already running when REI reloads its plugins, so its {@code RecipeManager} is used.
 * On a dedicated server the client has no such manager, so these ten custom categories are empty
 * until a server-side {@code REICommonPlugin} registers them with REI's
 * {@code DisplaySerializerRegistry} (not implemented yet). See {@link #recipeManager()}.
 *
 * <p>Platform-specific parts are supplied by thin subclasses: Fabric registers this as a
 * {@code rei_client} entrypoint, NeoForge uses {@code @REIPluginClient}; both override
 * {@link #customStripping()} to expose their own axe-stripping map.
 */
public class BotaniaREIPlugin implements REIClientPlugin {
	private static final Supplier<ItemStack> HOVERED_STACK_GETTER = BotaniaREIPlugin::getHoveredREIStack;
	private static boolean warnedAboutMissingRecipes = false;

	public BotaniaREIPlugin() {
		if (!CorporeaInputHandler.hoveredStackGetters.contains(HOVERED_STACK_GETTER)) {
			CorporeaInputHandler.hoveredStackGetters.add(HOVERED_STACK_GETTER);
		}
		CorporeaInputHandler.supportedGuiFilter = CorporeaInputHandler.supportedGuiFilter.or(s -> s instanceof DisplayScreen);
	}

	@Override
	public void registerCategories(CategoryRegistry helper) {
		helper.add(List.of(
				new PetalApothecaryREICategory(),
				new PureDaisyREICategory(),
				new ManaPoolREICategory(),
				new RunicAltarREICategory(),
				new ElvenTradeREICategory(),
				new BreweryREICategory(),
				new TerrestrialAgglomerationREICategory(),
				new OrechidREICategory(BotaniaREICategoryIdentifiers.ORECHID, BotaniaFlowerBlocks.orechid),
				new OrechidREICategory(BotaniaREICategoryIdentifiers.ORECHID_IGNEM, BotaniaFlowerBlocks.orechidIgnem),
				new MarimorphosisREICategory()
		));

		helper.addWorkstations(BuiltinPlugin.CRAFTING, EntryStacks.of(BotaniaItems.craftingHalo), EntryStacks.of(BotaniaItems.autocraftingHalo));
		for (Block apothecary : BotaniaBlocks.ALL_APOTHECARIES) {
			helper.addWorkstations(BotaniaREICategoryIdentifiers.PETAL_APOTHECARY, EntryStacks.of(apothecary));
		}
		helper.addWorkstations(BotaniaREICategoryIdentifiers.BREWERY, EntryStacks.of(BotaniaBlocks.brewery));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.ELVEN_TRADE, EntryStacks.of(BotaniaBlocks.alfPortal));
		Set<Block> manaPools = ImmutableSet.of(BotaniaBlocks.manaPool, BotaniaBlocks.dilutedPool, BotaniaBlocks.fabulousPool);
		for (Block pool : manaPools) {
			helper.addWorkstations(BotaniaREICategoryIdentifiers.MANA_INFUSION, EntryStacks.of(pool));
		}
		helper.addWorkstations(BotaniaREICategoryIdentifiers.ORECHID, EntryStacks.of(BotaniaFlowerBlocks.orechid), EntryStacks.of(BotaniaFlowerBlocks.orechidFloating));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.ORECHID_IGNEM, EntryStacks.of(BotaniaFlowerBlocks.orechidIgnem), EntryStacks.of(BotaniaFlowerBlocks.orechidIgnemFloating));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.MARIMORPHOSIS, EntryStacks.of(BotaniaFlowerBlocks.marimorphosis), EntryStacks.of(BotaniaFlowerBlocks.marimorphosisFloating),
				EntryStacks.of(BotaniaFlowerBlocks.marimorphosisChibi), EntryStacks.of(BotaniaFlowerBlocks.marimorphosisChibiFloating));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.PURE_DAISY, EntryStacks.of(BotaniaFlowerBlocks.pureDaisy), EntryStacks.of(BotaniaFlowerBlocks.pureDaisyFloating));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.RUNE_ALTAR, EntryStacks.of(BotaniaBlocks.runeAltar));
		helper.addWorkstations(BotaniaREICategoryIdentifiers.TERRA_PLATE, EntryStacks.of(BotaniaBlocks.terraPlate));

		// None of Botania's recipes can be quick-crafted from the crafting grid.
		// A ButtonArea returning null hides the + button (same as removePlusButton(), without
		// depending on REI's deprecated-for-removal helper).
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.PETAL_APOTHECARY, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.BREWERY, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.ELVEN_TRADE, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.MANA_INFUSION, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.ORECHID, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.ORECHID_IGNEM, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.MARIMORPHOSIS, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.PURE_DAISY, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.RUNE_ALTAR, bounds -> null);
		helper.setPlusButtonArea(BotaniaREICategoryIdentifiers.TERRA_PLATE, bounds -> null);
	}

	/** Axe stripping maps live in each loader's Xplat implementation. */
	protected Map<Block, Block> customStripping() {
		return Map.of();
	}

	/**
	 * Makes REI's config screen the preferred entry point for Botania's settings. REI only hosts
	 * the addon list - it explicitly does not serialize addon config - so the screen and all
	 * persistence stay in {@link ClothBotaniaConfig}, shared with the ModMenu / NeoForge entries.
	 * REI remains an optional dependency; without it those entries still work unchanged.
	 */
	@Override
	public void registerConfigAddons(ConfigAddonRegistry registry) {
		registry.register(new BotaniaConfigAddon());
		BotaniaAPI.LOGGER.info("Botania REI: config addon registered (REI config screen entry point)");
	}

	private static final class BotaniaConfigAddon implements ConfigAddon {
		@Override
		public Component getName() {
			return Component.literal("Botania");
		}

		@Override
		public Component getDescription() {
			return Component.translatable("botania.rei.config_addon.description");
		}

		@Override
		public Screen createScreen(Screen parent) {
			return ClothBotaniaConfig.createConfigScreen(parent);
		}
	}

	@Override
	public void registerDisplays(DisplayRegistry helper) {
		int displaysBefore = helper.size();
		registerAncientWillRecipeWrapper(helper);
		registerCompositeLensRecipeWrapper(helper);
		registerTerraPickTippingRecipeWrapper(helper);

		for (var holder : recipesOfType(BotaniaRecipeTypes.PETAL_TYPE)) {
			helper.add(new PetalApothecaryREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.BREW_TYPE)) {
			helper.add(new BreweryREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.ELVEN_TRADE_TYPE)) {
			ElvenTradeRecipe recipe = holder.value();
			if (recipe instanceof LexiconElvenTradeRecipe || !recipe.isReturnRecipe()) {
				helper.add(new ElvenTradeREIDisplay(holder));
			}
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.MANA_INFUSION_TYPE)) {
			helper.add(new ManaPoolREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.PURE_DAISY_TYPE)) {
			helper.add(new PureDaisyREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.RUNE_TYPE)) {
			helper.add(new RunicAltarREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.TERRA_PLATE_TYPE)) {
			helper.add(new TerrestrialAgglomerationREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.ORECHID_TYPE)) {
			helper.add(new OrechidREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.ORECHID_IGNEM_TYPE)) {
			helper.add(new OrechidIgnemREIDisplay(holder));
		}
		for (var holder : recipesOfType(BotaniaRecipeTypes.MARIMORPHOSIS_TYPE)) {
			helper.add(new MarimorphosisREIDisplay(holder));
		}

		try {
			for (var entry : customStripping().entrySet()) {
				helper.add(new DefaultStrippingDisplay(EntryStacks.of(entry.getKey()), EntryStacks.of(entry.getValue())));
			}
		} catch (Exception e) {
			BotaniaAPI.LOGGER.error("Error adding strippable entry to REI", e);
		}
		BotaniaAPI.LOGGER.info("Botania REI: registered {} displays", helper.size() - displaysBefore);
	}

	/**
	 * Returns a {@code RecipeManager} that still holds the full {@code RecipeHolder} list.
	 *
	 * <p>REI reloads its client plugins while joining a world, before {@code Minecraft#level} is
	 * populated, and a {@code ClientLevel} only exposes a {@code RecipeAccess} anyway. The
	 * integrated server, however, is already up at that point (it logged the player in), so its
	 * {@code RecipeManager} is the reliable source in singleplayer / dev runs. A dedicated server
	 * supplies neither, which is reported by {@link #recipesOfType(RecipeType)}.
	 */
	@Nullable
	private static RecipeManager recipeManager() {
		Minecraft mc = Minecraft.getInstance();
		RecipeManager manager = RecipeHelper.getRecipeManager(mc.getSingleplayerServer());
		return manager != null ? manager : RecipeHelper.getRecipeManager(mc.level);
	}

	/**
	 * MC 26.1 removed {@code RecipeManager#recipeMap()}; filter the flat recipe collection by type,
	 * exactly like the EMI integration.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Recipe<?>> List<RecipeHolder<T>> recipesOfType(RecipeType<T> type) {
		RecipeManager manager = recipeManager();
		if (manager == null) {
			if (!warnedAboutMissingRecipes) {
				warnedAboutMissingRecipes = true;
				BotaniaAPI.LOGGER.warn("Botania's custom REI recipe categories cannot be filled: this client has no "
						+ "RecipeManager with full RecipeHolders. MC 26.1 only syncs RecipeDisplays to clients, and "
						+ "Botania's special recipes have no vanilla RecipeDisplay. Dedicated-server play requires a "
						+ "server-side REICommonPlugin with registered DisplaySerializers (not implemented yet).");
			}
			return List.of();
		}
		return manager.getRecipes().stream()
				.filter(h -> h.value().getType() == type)
				.map(h -> (RecipeHolder<T>) h)
				.toList();
	}

	void registerAncientWillRecipeWrapper(DisplayRegistry helper) {
		ImmutableList.Builder<EntryIngredient> input = ImmutableList.builder();
		ImmutableList.Builder<EntryStack<ItemStack>> output = ImmutableList.builder();
		Set<ItemStack> wills = ImmutableSet.of(new ItemStack(BotaniaItems.ancientWillAhrim), new ItemStack(BotaniaItems.ancientWillDharok),
				new ItemStack(BotaniaItems.ancientWillGuthan), new ItemStack(BotaniaItems.ancientWillKaril),
				new ItemStack(BotaniaItems.ancientWillTorag), new ItemStack(BotaniaItems.ancientWillVerac));
		AncientWillContainer container = (AncientWillContainer) BotaniaItems.terrasteelHelm;

		ItemStack helmet = new ItemStack(BotaniaItems.terrasteelHelm);
		input.add(EntryIngredients.of(helmet));
		input.add(EntryIngredients.ofItemStacks(wills));
		for (ItemStack will : wills) {
			ItemStack copy = helmet.copy();
			container.addAncientWill(copy, ((AncientWillItem) will.getItem()).type);
			output.add(EntryStacks.of(copy));
		}
		helper.add(new DefaultCustomDisplay(input.build(), Collections.singletonList(EntryIngredient.of(output.build())), Optional.empty()));
	}

	void registerCompositeLensRecipeWrapper(DisplayRegistry helper) {
		List<ItemStack> lensStacks = StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(BotaniaTags.Items.LENS).spliterator(), false)
				.map(ItemStack::new)
				.filter(s -> s.getItem() instanceof LensItem lens && !lens.isControlLens(s))
				.filter(s -> ((LensItem) s.getItem()).isCombinable(s))
				.toList();
		List<Item> lenses = lensStacks.stream().map(ItemStack::getItem).toList();
		List<EntryIngredient> inputs = new ArrayList<>(Arrays.asList(EntryIngredients.ofItemStacks(lensStacks),
				EntryIngredients.of(new ItemStack(Items.SLIME_BALL)), EntryIngredients.ofItemStacks(lensStacks)));
		int end = lenses.size() - 1;

		List<ItemStack> firstInput = new ArrayList<>();
		List<ItemStack> secondInput = new ArrayList<>();
		List<ItemStack> outputs = new ArrayList<>();
		for (int i = 1; i <= end; i++) {
			ItemStack firstLens = new ItemStack(lenses.get(i));
			for (Item secondLens : lenses) {
				if (secondLens == firstLens.getItem()) {
					continue;
				}
				ItemStack secondLensStack = new ItemStack(secondLens);
				if (((LensItem) firstLens.getItem()).canCombineLenses(firstLens, secondLensStack)) {
					firstInput.add(firstLens);
					secondInput.add(secondLensStack);
					outputs.add(((LensItem) firstLens.getItem()).setCompositeLens(firstLens.copy(), secondLensStack));
				}
			}
		}
		inputs.set(0, EntryIngredients.ofItemStacks(firstInput));
		inputs.set(2, EntryIngredients.ofItemStacks(secondInput));

		helper.add(new DefaultCustomDisplay(inputs, Collections.singletonList(EntryIngredients.ofItemStacks(outputs)), Optional.empty()));
	}

	void registerTerraPickTippingRecipeWrapper(DisplayRegistry helper) {
		List<EntryIngredient> inputs = ImmutableList.of(EntryIngredients.of(BotaniaItems.terraPick), EntryIngredients.of(BotaniaItems.elementiumPick));
		ItemStack output = new ItemStack(BotaniaItems.terraPick);
		TerraShattererItem.setTipped(output);

		helper.add(new DefaultCustomDisplay(inputs, Collections.singletonList(EntryIngredients.of(output)), Optional.empty()));
	}

	private static ItemStack getHoveredREIStack() {
		return REIRuntime.getInstance().getOverlay().map(o -> {
			ItemStack stack;
			if (REIRuntime.getInstance().isOverlayVisible()) {
				stack = unwrapEntry(o.getEntryList().getFocusedStack());
				if (!stack.isEmpty()) {
					return stack;
				}
				stack = o.getFavoritesList()
						.map(OverlayListWidget::getFocusedStack)
						.map(BotaniaREIPlugin::unwrapEntry)
						.orElse(ItemStack.EMPTY);
				if (!stack.isEmpty()) {
					return stack;
				}
			}
			if (Minecraft.getInstance().screen instanceof DisplayScreen) {
				Point point = PointHelper.ofMouse();
				for (var child : Minecraft.getInstance().screen.children()) {
					if (child instanceof Slot slot && slot.containsMouse(point)) {
						stack = unwrapEntry(slot.getCurrentEntry());
						if (!stack.isEmpty()) {
							return stack;
						}
					}
				}
			}
			return ItemStack.EMPTY;
		}).orElse(ItemStack.EMPTY);
	}

	private static ItemStack unwrapEntry(@Nullable EntryStack<?> stack) {
		if (stack != null && !stack.isEmpty() && stack.getType() == VanillaEntryTypes.ITEM) {
			return (ItemStack) stack.getValue();
		}
		return ItemStack.EMPTY;
	}
}
