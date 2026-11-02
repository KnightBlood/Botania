package vazkii.botania.forge.client;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.handler.*;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.gui.ManaBarTooltipComponent;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.gui.bag.FlowerPouchGui;
import vazkii.botania.client.gui.box.BaubleBoxGui;
import vazkii.botania.client.integration.ears.EarsIntegration;
import vazkii.botania.client.model.BotaniaLayerDefinitions;
import vazkii.botania.client.model.ManaBlasterBakedModel;
import vazkii.botania.client.render.BlockRenderLayers;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.client.render.block_entity.TEISR;
import vazkii.botania.client.render.entity.EntityRenderers;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.block.PlatformBlock;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.RingOfDexterousMotionItem;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.BookDrawScreenEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class ForgeClientInitializer {
	/** Keys of every standalone model we register, for lookup after baking. */
	private static final Map<StandaloneModelKey<BlockStateModel>, Identifier> STANDALONE_KEYS = new HashMap<>();

	public static void init(IEventBus modBus) {
		modBus.addListener(ForgeClientInitializer::clientInit);
		modBus.addListener(ForgeClientInitializer::registerGuiLayers);
		modBus.addListener(ForgeClientInitializer::registerTooltipComponent);
		modBus.addListener(ForgeClientInitializer::registerKeys);
		modBus.addListener(ForgeClientInitializer::registerWandHudCaps);
		modBus.addListener(ForgeClientInitializer::registerModelLoaders);
		modBus.addListener(ForgeClientInitializer::registerItemModels);
		modBus.addListener(ForgeClientInitializer::registerSpecialModels);
		modBus.addListener(ForgeClientInitializer::registerStandaloneModels);
		modBus.addListener(ForgeClientInitializer::onModifyBakingResult);
		modBus.addListener(ForgeClientInitializer::registerEntityLayers);
		modBus.addListener(ForgeClientInitializer::registerEntityRenderers);
		modBus.addListener(ForgeClientInitializer::registerParticleFactories);
		modBus.addListener(ForgeClientInitializer::registerBlockColors);
		modBus.addListener(ForgeClientInitializer::registerItemColors);
		modBus.addListener(ForgeClientInitializer::registerItemProperties);
		modBus.addListener(ForgeClientInitializer::registerMenuScreens);
		modBus.addListener(ForgeClientInitializer::initAuxiliaryRender);
	}

	public static void clientInit(FMLClientSetupEvent evt) {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("Client API instances: {}",
				List.of(BotaniaAPIClient.instance(), ClientXplatAbstractions.instance()));

		BlockRenderLayers.skipPlatformBlocks = true; // platforms can use standard rendering on Forge
		// 26.1 has no per-block chunk layer registration; layers are derived from the
		// baked quads' material info, so the Xplat layer table has no recipient here.
		BlockRenderLayers.init((block, layer) -> {});

		// Events
		var bus = NeoForge.EVENT_BUS;
		bus.addListener((BookDrawScreenEvent e) -> KonamiHandler.renderBook(e.getBook(), e.getScreen(), e.getMouseX(), e.getMouseY(), e.getPartialTicks(), e.getGraphics()));
		bus.addListener((ClientTickEvent.Post e) -> {
			ClientTickHandler.clientTickEnd(Minecraft.getInstance());
			KonamiHandler.clientTick(Minecraft.getInstance());
		});
		bus.addListener((ItemTooltipEvent e) -> TooltipHandler.onTooltipEvent(e.getItemStack(), e.getFlags(), e.getToolTip()));
		bus.addListener((ScreenEvent.KeyPressed.Post e) -> CorporeaInputHandler.buttonPressed(e.getKeyCode(), e.getScanCode()));

		// Forge bus events done with Mixins on Fabric
		bus.addListener(EventPriority.HIGH, (ClientChatEvent e) -> {
			var player = Minecraft.getInstance().player;
			if (player != null && CorporeaIndexBlockEntity.ClientHandler.onChat(player, e.getMessage())) {
				e.setCanceled(true);
			}
		});
		bus.addListener((CustomizeGuiOverlayEvent.BossEventProgress e) -> {
			var result = BossBarHandler.onBarRender(e.getGuiGraphics(), e.getX(), e.getY(),
					e.getBossEvent(), true);
			result.ifPresent(increment -> {
				e.setCanceled(true);
				e.setIncrement(increment);
			});
		});
		bus.addListener((InputEvent.Key e) -> {
			RingOfDexterousMotionItem.ClientLogic.onKeyDown();
			KonamiHandler.handleInput(e.getKey(), e.getAction(), e.getModifiers());
		});
		bus.addListener((RenderFrameEvent.Post e) -> {
			ClientTickHandler.renderTick(e.getPartialTick().getGameTimeDeltaPartialTick(true));
		});
		bus.addListener(EventPriority.LOWEST, (RenderTooltipEvent.Pre e) -> {
			var manaItem = XplatAbstractions.INSTANCE.findManaItem(e.getItemStack());
			if (manaItem == null) {
				return;
			}
			// Forge does not pass the tooltip width to any tooltip event.
			// To avoid a mixin here, we just duplicate the width checking part.
			int width = 0;
			ManaBarTooltipComponent manaBar = null;
			for (ClientTooltipComponent component : e.getComponents()) {
				width = Math.max(width, component.getWidth(e.getFont()));
				if (component instanceof ManaBarTooltipComponent c) {
					manaBar = c;
				}
			}
			if (manaBar != null) {
				manaBar.setContext(e.getX(), e.getY(), width);
			}
		});

		// Etc
		ClientProxy.initSeasonal();
		ClientProxy.initDebugEntry();

		if (XplatAbstractions.INSTANCE.isModLoaded("ears")) {
			EarsIntegration.register();
		}
	}

	public static void registerGuiLayers(RegisterGuiLayersEvent e) {
		e.registerAboveAll(prefix("hud"),
				(gui, deltaTracker) -> HUDHandler.onDrawScreenPost(gui, deltaTracker.getGameTimeDeltaPartialTick(true)));
	}

	public static void registerTooltipComponent(RegisterClientTooltipComponentFactoriesEvent e) {
		e.register(ManaBarTooltip.class, ManaBarTooltipComponent::new);
	}

	public static void registerKeys(RegisterKeyMappingsEvent e) {
		ClientProxy.initKeybindings(e::register);
	}

	private static final Supplier<Map<BlockEntityType<?>, Function<BlockEntity, WandHUD>>> WAND_HUD = Suppliers.memoize(() -> {
		var ret = new IdentityHashMap<BlockEntityType<?>, Function<BlockEntity, WandHUD>>();
		BotaniaBlockEntities.registerWandHudCaps((factory, types) -> {
			for (var type : types) {
				ret.put(type, factory);
			}
		});
		BotaniaFlowerBlocks.registerWandHudCaps((factory, types) -> {
			for (var type : types) {
				ret.put(type, factory);
			}
		});
		return Collections.unmodifiableMap(ret);
	});

	private static final Supplier<Map<EntityType<?>, Function<Entity, WandHUD>>> ENTITY_WAND_HUD = Suppliers.memoize(() -> {
		var ret = new IdentityHashMap<EntityType<?>, Function<Entity, WandHUD>>();
		BotaniaEntities.registerWandHudCaps((factory, types) -> {
			for (var type : types) {
				ret.put(type, factory);
			}
		});
		return Collections.unmodifiableMap(ret);
	});

	public static void registerWandHudCaps(RegisterCapabilitiesEvent evt) {
		WAND_HUD.get().forEach((type, factory) ->
				evt.registerBlockEntity(BotaniaForgeClientCapabilities.WAND_HUD, type, (be, side) -> factory.apply(be)));
		ENTITY_WAND_HUD.get().forEach((type, factory) ->
				evt.registerEntity(BotaniaForgeClientCapabilities.ENTITY_WAND_HUD, type, (entity, side) -> factory.apply(entity)));
	}

	public static void registerModelLoaders(ModelEvent.RegisterLoaders evt) {
		evt.register(ClientXplatAbstractions.FLOATING_FLOWER_MODEL_LOADER_ID, ForgeFloatingFlowerModel.Loader.INSTANCE);
	}

	public static void registerItemModels(RegisterItemModelsEvent evt) {
		evt.register(ClientXplatAbstractions.MANA_GUN_MODEL_LOADER_ID, ManaBlasterBakedModel.Unbaked.MAP_CODEC);
	}

	public static void registerSpecialModels(RegisterSpecialModelRendererEvent evt) {
		evt.register(prefix("be_item"), TEISR.Unbaked.MAP_CODEC);
	}

	private record DebugName(Identifier id) implements ModelDebugName {
		@Override
		public String debugName() {
			return this.id.toString();
		}
	}

	private static void registerStandalone(ModelEvent.RegisterStandalone evt, Identifier id,
			SimpleUnbakedStandaloneModel<BlockStateModel> model) {
		var key = new StandaloneModelKey<BlockStateModel>(new DebugName(id));
		STANDALONE_KEYS.put(key, id);
		evt.register(key, model);
	}

	public static void registerStandaloneModels(ModelEvent.RegisterStandalone evt) {
		var resourceManager = Minecraft.getInstance().getResourceManager();
		MiscellaneousModels.INSTANCE.onModelRegister(resourceManager,
				id -> registerStandalone(evt, id, SimpleUnbakedStandaloneModel.blockStateModel(id)));
		ForgeFloatingFlowerModel.forEachStandaloneModel((id, model) -> registerStandalone(evt, id, model));
	}

	public static void onModifyBakingResult(ModelEvent.ModifyBakingResult evt) {
		var result = evt.getBakingResult();
		Map<Identifier, BlockStateModel> standaloneById = new HashMap<>();
		STANDALONE_KEYS.forEach((key, id) -> {
			var model = result.standaloneModels().get(key);
			if (model != null) {
				standaloneById.put(id, model);
			}
		});
		STANDALONE_KEYS.clear();

		var blockStateModels = result.blockStateModels();
		// Platforms disguise as whatever they are covering.
		for (var entry : blockStateModels.entrySet()) {
			if (entry.getKey().getBlock() instanceof PlatformBlock) {
				entry.setValue(ClientXplatAbstractions.INSTANCE.wrapPlatformModel(entry.getValue()));
			}
		}
		// Floating flowers pick their island per position instead of through ModelData.
		ForgeFloatingFlowerModel.replaceFloatingFlowerModels(blockStateModels, standaloneById);
		// Spreader cores, potato pendants, etc. resolve through MiscellaneousModels.
		MiscellaneousModels.INSTANCE.onModelBake(standaloneById);
	}

	public static void registerEntityLayers(EntityRenderersEvent.RegisterLayerDefinitions evt) {
		BotaniaLayerDefinitions.init(evt::registerLayerDefinition);
	}

	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers evt) {
		EntityRenderers.registerBlockEntityRenderers(evt::registerBlockEntityRenderer);
		EntityRenderers.registerEntityRenderers(evt::registerEntityRenderer);
	}

	public static void registerParticleFactories(RegisterParticleProvidersEvent evt) {
		BotaniaParticles.FactoryHandler.registerFactories(new BotaniaParticles.FactoryHandler.Consumer() {
			@Override
			public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> constructor) {
				evt.registerSpriteSet(type, constructor::apply);
			}
		});
	}

	public static void registerBlockColors(RegisterColorHandlersEvent.BlockTintSources evt) {
		ColorHandler.submitBlocks((handler, blocks) -> {
			// Vanilla looks up tint sources by the model's tint index, so provide one
			// delegating source per potential index to preserve the old per-index semantics.
			List<BlockTintSource> sources = new ArrayList<>();
			for (int i = 0; i < 4; i++) {
				int tintIndex = i;
				sources.add(new BlockTintSource() {
					@Override
					public int color(BlockState state) {
						return handler.getColor(state, null, null, tintIndex);
					}

					@Override
					public int colorInWorld(BlockState state, net.minecraft.client.renderer.block.BlockAndTintGetter level,
							net.minecraft.core.BlockPos pos) {
						return handler.getColor(state, level, pos, tintIndex);
					}
				});
			}
			evt.register(sources, blocks);
		});
	}

	public static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources evt) {
		// 26.1 item tints are referenced from item model JSON instead of registered per item.
		// One codec is registered per (item, tint index) pair, so datagen can reference them.
		ColorHandler.submitItems((handler, items) -> {
			for (ItemLike item : items) {
				Identifier itemId = BuiltInRegistries.ITEM.getKey(item.asItem());
				for (int i = 0; i < 3; i++) {
					int tintIndex = i;
					evt.register(prefix("item_tint_" + itemId.getPath() + "_" + i),
							MapCodec.unit(new ItemTintSource() {
								@Override
								public int calculate(ItemStack stack, @Nullable net.minecraft.client.multiplayer.ClientLevel level,
										@Nullable net.minecraft.world.entity.LivingEntity entity) {
									return handler.getColor(stack, tintIndex);
								}

								@Override
								public MapCodec<? extends ItemTintSource> type() {
									return null; // unused; registered codec captures this instance directly
								}
							}));
				}
			}
		});
	}

	public static void registerItemProperties(RegisterRangeSelectItemModelPropertyEvent evt) {
		BotaniaItemProperties.init(evt::register);
	}

	public static void registerMenuScreens(RegisterMenuScreensEvent evt) {
		evt.register(BotaniaItems.FLOWER_BAG_CONTAINER, FlowerPouchGui::new);
		evt.register(BotaniaItems.BAUBLE_BOX_CONTAINER, BaubleBoxGui::new);
	}

	public static void initAuxiliaryRender(EntityRenderersEvent.AddLayers evt) {
		for (var playerModelType : evt.getSkins()) {
			var renderer = evt.getPlayerRenderer(playerModelType);
			EntityRenderers.addAuxiliaryPlayerRenders(renderer, renderer::addLayer);
		}
	}

	private ForgeClientInitializer() {}
}
