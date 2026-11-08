package vazkii.botania.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.SimpleUnbakedExtraModel;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaFabricClientCapabilities;
import vazkii.botania.client.BotaniaItemProperties;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.handler.CorporeaInputHandler;
import vazkii.botania.client.core.handler.KonamiHandler;
import vazkii.botania.client.core.handler.MiscellaneousModels;
import vazkii.botania.client.core.helper.CoreShaders;
import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.client.gui.HUDHandler;
import vazkii.botania.client.gui.ManaBarTooltipComponent;
import vazkii.botania.client.gui.TooltipHandler;
import vazkii.botania.client.gui.bag.FlowerPouchGui;
import vazkii.botania.client.gui.box.BaubleBoxGui;
import vazkii.botania.client.integration.ears.EarsIntegration;
import vazkii.botania.client.model.BotaniaLayerDefinitions;
import vazkii.botania.client.render.BlockRenderLayers;
import vazkii.botania.client.render.ColorHandler;
import vazkii.botania.client.render.entity.*;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.fabric.integration.trinkets.TrinketsClientIntegration;
import vazkii.botania.fabric.network.FabricPacketHandler;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.BookDrawScreenCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FabricClientInitializer implements ClientModInitializer {
	// Extra model keys registered through the ModelLoadingPlugin, by their model id.
	private static final Map<Identifier, ExtraModelKey<net.minecraft.client.renderer.block.dispatch.BlockStateModel>> EXTRA_MODEL_KEYS = new HashMap<>();

	@Override
	public void onInitializeClient() {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("Client API instances: {}",
				List.of(BotaniaAPIClient.instance(), ClientXplatAbstractions.instance()));

		// 26.1: Fabric's CoreShaderRegistrationCallback is gone and there is no replacement
		// event. Shader sources are discovered automatically by ShaderManager; only the
		// RenderPipeline objects need to exist. Register them through vanilla's (AW-exposed)
		// private RenderPipelines.register so they are precompiled and validated on every
		// resource reload, exactly like vanilla pipelines. Without this they would still work
		// via GlDevice's lazy compilation, but GLSL errors would only surface on first draw.
		CoreShaders.bootstrap();
		CoreShaders.registerAll(RenderPipelines::register);
		FabricPacketHandler.initClient();

		// Trinkets renderer registration is client-only: TrinketsIntegration itself is loaded on
		// dedicated servers as the EquipmentHandler, so it must not reference client classes.
		if (XplatAbstractions.INSTANCE.isModLoaded("trinkets")) {
			TrinketsClientIntegration.registerRenderers();
		}

		// Guis
		MenuScreens.register(BotaniaItems.FLOWER_BAG_CONTAINER, FlowerPouchGui::new);
		MenuScreens.register(BotaniaItems.BAUBLE_BOX_CONTAINER, BaubleBoxGui::new);

		// Blocks and Items
		// 26.1: model JSON dispatch keys differ per loader - NeoForge parses "loader" via
		// UnbakedModelLoader, Fabric parses "fabric:type" via UnbakedModelDeserializer. The datagen
		// floating flower JSON carries both keys.
		FabricFloatingFlowerModel.init();
		// 26.1: NeoForge registers botania:mana_gun through RegisterItemModelsEvent; Fabric has no
		// equivalent, so the codec goes into the same private ItemModels map through an accessor.
		vazkii.botania.fabric.mixin.client.ItemModelsAccessor.botania$getIdMapper()
				.put(ClientXplatAbstractions.MANA_GUN_MODEL_LOADER_ID,
						vazkii.botania.client.model.ManaBlasterBakedModel.Unbaked.MAP_CODEC);
		ModelLoadingPlugin.register(pluginContext -> {
			// 26.1: addModel now takes ExtraModelKey + UnbakedExtraModel instead of raw ids.
			// Bake without vanilla's atlas check - the icon/* models sample the item atlas,
			// and the render type matching each model's atlas is picked in RenderHelper.
			// (Inlined copy of AtlasUncheckedModels.bakeUnchecked.)
			MiscellaneousModels.INSTANCE.onModelRegister(Minecraft.getInstance().getResourceManager(), id -> {
				var key = ExtraModelKey.<net.minecraft.client.renderer.block.dispatch.BlockStateModel>create(() -> id.toString());
				pluginContext.addModel(key, new SimpleUnbakedExtraModel<>(id, (model, baker) -> {
					var slots = model.getTopTextureSlots();
					var particle = model.resolveParticleMaterial(slots, baker);
					var geometry = model.bakeTopGeometry(slots, baker,
							net.minecraft.client.renderer.block.dispatch.BlockModelRotation.IDENTITY);
					return new net.minecraft.client.renderer.block.dispatch.SingleVariant(
							new net.minecraft.client.resources.model.SimpleModelWrapper(geometry,
									model.getTopAmbientOcclusion(), particle));
				}));
				EXTRA_MODEL_KEYS.put(id, key);
			});
			// Islands only feed the island-type registry inside onModelRegister and never reach the
			// consumer above; NeoForge registers them as standalone models instead. Registering them
			// here is what makes their resolveDependencies mark the island models, so the floating
			// flower loader's geometry can resolve them via baker.getModel (otherwise every floating
			// flower bakes a missing model and crashes the item atlas validation).
			FabricFloatingFlowerModel.islandModels().values().forEach(id -> {
				var key = ExtraModelKey.<net.minecraft.client.renderer.block.dispatch.BlockStateModel>create(() -> id.toString());
				pluginContext.addModel(key, SimpleUnbakedExtraModel.blockStateModel(id));
				EXTRA_MODEL_KEYS.put(id, key);
			});
			// Platform camouflage is per-position: wrap each platform blockstate's baked model.
			pluginContext.modifyBlockModelAfterBake().register((model, ctx) -> {
				if (ctx.state().getBlock() instanceof vazkii.botania.common.block.decor.FloatingFlowerBlock) {
					return FabricFloatingFlowerModel.wrapBlockModel(model, ctx.baker(), ctx.state());
				}
				return ctx.state().getBlock() instanceof vazkii.botania.common.block.PlatformBlock
						? ClientXplatAbstractions.INSTANCE.wrapPlatformModel(model) : model;
			});
		});
		// 26.1 has no per-block chunk layer registration; layers are derived from the
		// baked quads' material info, so the Xplat layer table has no recipient here.
		BlockRenderLayers.init((block, layer) -> {});
		// 26.1: ItemProperties is gone; range-select item model properties are codecs registered in a
		// private id map inside RangeSelectItemModelProperties. NeoForge gets an event for this,
		// Fabric does not, so the map is reached through an accessor.
		BotaniaItemProperties.init((id, codec) -> vazkii.botania.mixin.client.RangeSelectItemModelPropertiesAccessor
				.botania$getIdMapper().put(id, codec));
		// Same story for the special model renderer types: NeoForge fires RegisterSpecialModelRendererEvent,
		// Fabric has no equivalent, so botania:be_item goes into the same private id map through an accessor.
		vazkii.botania.mixin.client.SpecialModelRenderersAccessor.botania$getIdMapper()
				.put(vazkii.botania.common.lib.ResourceLocationHelper.prefix("be_item"),
						vazkii.botania.client.render.block_entity.TEISR.Unbaked.MAP_CODEC);

		// BE/Entity Renderer
		BotaniaLayerDefinitions.init((loc, supplier) -> ModelLayerRegistry.registerModelLayer(loc, supplier::get));
		EntityRenderers.registerBlockEntityRenderers(BlockEntityRendererRegistry::register);
		// 26.1: fabric's BuiltinItemRendererRegistry is gone; BE items use the data-driven special model
		// renderer above instead, which works on both loaders.
		EntityRenderers.registerEntityRenderers(EntityRendererRegistry::register);
		LivingEntityRenderLayerRegistrationCallback.EVENT.register(this::initAuxiliaryRender);

		BotaniaParticles.FactoryHandler.registerFactories(new BotaniaParticles.FactoryHandler.Consumer() {
			@Override
			public <T extends ParticleOptions> void register(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> constructor) {
				ParticleProviderRegistry.getInstance().register(type, constructor::apply);
			}
		});

		// Events
		BookDrawScreenCallback.EVENT.register(KonamiHandler::renderBook);
		ClientLifecycleEvents.CLIENT_STARTED.register(this::loadComplete);
		ClientTickEvents.END_CLIENT_TICK.register(ClientTickHandler::clientTickEnd);
		ClientTickEvents.END_CLIENT_TICK.register(KonamiHandler::clientTick);
		// 26.1: HudRenderCallback was replaced by the composable HudElementRegistry.
		HudElementRegistry.addLast(vazkii.botania.common.lib.ResourceLocationHelper.prefix("hud"),
				(var gui, var delta) -> HUDHandler.onDrawScreenPost(gui, delta.getGameTimeDeltaPartialTick(true)));
		ItemTooltipCallback.EVENT.register((stack, tooltipCtx, flag, lines) -> TooltipHandler.onTooltipEvent(stack, flag, lines));
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> ScreenKeyboardEvents.beforeKeyPress(screen)
				.register((screen2, keyEvent) -> CorporeaInputHandler.buttonPressed(keyEvent.key(), keyEvent.scancode())));
		ClientTooltipComponentCallback.EVENT.register(ManaBarTooltipComponent::tryConvert);

		// Etc
		ClientProxy.initSeasonal();
		ClientProxy.initDebugEntry();
		ClientProxy.initKeybindings(KeyMappingHelper::registerKeyMapping);

		registerCapabilities();

		if (XplatAbstractions.INSTANCE.isModLoaded("ears")) {
			EarsIntegration.register();
		}
	}

	// 26.1: fabric's ArmorRenderer and BuiltinItemRendererRegistry are gone. Player armour is
	// re-routed to Botania's 1.20.1 custom 3D geometry by HumanoidArmorLayerMixin, which also
	// handles phantom ink and the santaweave texture variant via getArmorTextureAfterInk.
	// The vanilla equipment layer system (BotaniaAPIImpl's botania:<material> EquipmentAsset
	// key and assets/botania/equipment/<material>.json) remains as the fallback for baby
	// players and for anything outside Botania's four armour sets.
	// BE items fall back to static models.

	private static void registerCapabilities() {
		BotaniaEntities.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.ENTITY_WAND_HUD.registerForTypes((e, c) -> factory.apply(e), types));
		BotaniaBlockEntities.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities((be, c) -> factory.apply(be), types));
		BotaniaFlowerBlocks.registerWandHudCaps((factory, types) -> BotaniaFabricClientCapabilities.WAND_HUD.registerForBlockEntities((be, c) -> factory.apply(be), types));
	}

	/**
	 * Called once the model manager finished baking, i.e. at the same moment NeoForge fires
	 * {@code ModelEvent.ModifyBakingResult}. The extra models registered through the
	 * {@link net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin} are only available
	 * from that point on; baking them earlier silently produced an empty set.
	 */
	public static void onModelsBaked(ModelManager modelManager) {
		var fabricModels = (FabricModelManager) modelManager;
		Map<Identifier, net.minecraft.client.renderer.block.dispatch.BlockStateModel> bakedExtras = new HashMap<>();
		EXTRA_MODEL_KEYS.forEach((id, key) -> {
			var model = fabricModels.getModel(key);
			// A model whose sprites live in an unsupported atlas is not baked at all; keep it out of
			// the map instead of handing a null model to the consumers.
			if (model != null) {
				bakedExtras.put(id, model);
			}
		});
		MiscellaneousModels.INSTANCE.onModelBake(bakedExtras);
	}

	private void loadComplete(Minecraft mc) {
		ColorHandler.submitBlocks((handler, blocks) -> {
			// 26.1: tints are data-driven BlockTintSources; emit one entry per tint index.
			BlockColorRegistry.register((state, world, pos, out) -> {
				for (int i = 0; i < 4; i++) {
					out.add(handler.getColor(state, world, pos, i));
				}
			}, blocks);
		});
		// 26.1: item tints come from item model JSON and Fabric exposes no tint-source
		// registration yet, so ColorHandler.submitItems has no recipient here (TODO 26.1).
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initAuxiliaryRender(EntityType<? extends LivingEntity> type, net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?, ?> renderer,
			LivingEntityRenderLayerRegistrationCallback.RegistrationHelper helper, EntityRendererProvider.Context ctx) {
		if (type == EntityType.PLAYER && renderer instanceof AvatarRenderer avatarRenderer) {
			EntityRenderers.addAuxiliaryPlayerRenders((AvatarRenderer<net.minecraft.client.player.AbstractClientPlayer>) avatarRenderer, helper::register);
		}
	}
}
