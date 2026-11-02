package vazkii.botania.forge;

import com.mojang.datafixers.util.Pair;
import com.google.common.base.Suppliers;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.block.HornHarvestable;
import vazkii.botania.api.block.PhantomInkableBlock;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.item.AvatarWieldable;
import vazkii.botania.api.item.BlockProvider;
import vazkii.botania.api.item.CoordBoundItem;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.*;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.client.fx.BotaniaParticles;
import vazkii.botania.common.BotaniaStats;
import vazkii.botania.common.PlayerAccess;
import vazkii.botania.common.advancements.BotaniaCriteriaTriggers;
import vazkii.botania.common.block.*;
import vazkii.botania.common.block.block_entity.*;
import vazkii.botania.common.block.block_entity.BlockEntityConstants;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.block.block_entity.mana.PowerGeneratorBlockEntity;
import vazkii.botania.common.block.block_entity.red_string.RedStringContainerBlockEntity;
import vazkii.botania.common.block.flower.functional.DaffomillBlockEntity;
import vazkii.botania.common.block.flower.functional.LooniumBlockEntity;
import vazkii.botania.common.block.flower.functional.TigerseyeBlockEntity;
import vazkii.botania.common.block.flower.functional.VinculotusBlockEntity;
import vazkii.botania.common.block.mana.DrumBlock;
import vazkii.botania.common.block.mana.ManaDetectorBlock;
import vazkii.botania.common.block.mana.ManaVoidBlock;
import vazkii.botania.common.block.red_string.RedStringInterceptorBlock;
import vazkii.botania.common.brew.BotaniaBrews;
import vazkii.botania.common.brew.BotaniaMobEffects;
import vazkii.botania.common.brew.effect.SoulCrossMobEffect;
import vazkii.botania.common.command.SkyblockCommand;
import vazkii.botania.common.config.ConfigDataManagerImpl;
import vazkii.botania.common.crafting.BotaniaRecipeTypes;
import vazkii.botania.common.entity.BotaniaEntities;
import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.common.handler.*;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.impl.BotaniaAPIImpl;
import vazkii.botania.common.impl.DefaultHornHarvestable;
import vazkii.botania.common.impl.corporea.DefaultCorporeaMatchers;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;
import vazkii.botania.common.item.*;
import vazkii.botania.common.item.equipment.armor.terrasteel.TerrasteelHelmItem;
import vazkii.botania.common.item.equipment.bauble.*;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraBladeItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraShattererItem;
import vazkii.botania.common.item.equipment.tool.terrasteel.TerraTruncatorItem;
import vazkii.botania.common.item.material.EnderAirItem;
import vazkii.botania.common.item.relic.*;
import vazkii.botania.common.item.rod.*;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.common.lib.RegistrationBackfill;
import vazkii.botania.common.loot.BotaniaLootModifiers;
import vazkii.botania.common.loot.LootHandler;
import vazkii.botania.common.world.BotaniaFeatures;
import vazkii.botania.common.world.SkyblockChunkGenerator;
import vazkii.botania.common.world.SkyblockWorldEvents;
import vazkii.botania.forge.client.ForgeClientInitializer;
import vazkii.botania.forge.data.ForgeDatagenInitializer;
import vazkii.botania.forge.integration.InventorySorterIntegration;
import vazkii.botania.forge.integration.corporea.ForgeCapCorporeaNodeDetector;
import vazkii.botania.forge.mixin.HoeItemForgeAccessor;
import vazkii.botania.forge.mixin.ShovelItemForgeAccessor;
import vazkii.botania.forge.integration.curios.CurioIntegration;
import vazkii.botania.forge.internal_caps.ForgeInternalEntityCapabilities;
import vazkii.botania.forge.internal_caps.RedStringContainerCapProvider;
import vazkii.botania.forge.network.ForgePacketHandler;
import vazkii.botania.forge.xplat.ForgeXplatImpl;
import vazkii.botania.xplat.XplatAbstractions;
import vazkii.patchouli.api.PatchouliAPI;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

@Mod(LibMisc.MOD_ID)
public class ForgeCommonInitializer {
	private final IEventBus modBus;
	private final ModContainer container;

	// 26.1: registering instance-method listeners from the @Mod constructor is the
	// standard NeoForge pattern and triggers javac's this-escape lint.
	@SuppressWarnings("this-escape")
	public ForgeCommonInitializer(IEventBus modBus, ModContainer container) {
		this.modBus = modBus;
		this.container = container;
		coreInit();
		registryInit(modBus);
		modBus.addListener(this::commonSetup);
		modBus.addListener(this::registerCapabilities);
		modBus.addListener(ForgePacketHandler::register);
		ForgeInternalEntityCapabilities.register(modBus);
		ForgeRegistryHandler.init(modBus);
		vazkii.botania.forge.crafting.ForgeBotaniaIngredients.init(modBus);
		ForgeDatagenInitializer.init(modBus);
		if (FMLEnvironment.getDist() == Dist.CLIENT) {
			ForgeClientInitializer.init(modBus);
		}
	}

	public void commonSetup(FMLCommonSetupEvent evt) {
		registerEvents();

		evt.enqueueWork(BotaniaBlocks::addDispenserBehaviours);
		evt.enqueueWork(() -> {
			BiConsumer<Identifier, Supplier<? extends Block>> consumer = (Identifier, blockSupplier) -> ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(Identifier, blockSupplier);
			BotaniaBlocks.registerFlowerPotPlants(consumer);
			BotaniaFlowerBlocks.registerFlowerPotPlants(consumer);
		});
		BotaniaBlocks.addAxeStripping();
		// Botania grass block till/path support (26.1: register on vanilla's mutable maps,
		// replacing the old IForgeBlock#getToolModifiedState hook)
		for (Block grass : new Block[] { BotaniaBlocks.dryGrass, BotaniaBlocks.goldenGrass, BotaniaBlocks.vividGrass,
				BotaniaBlocks.scorchedGrass, BotaniaBlocks.infusedGrass, BotaniaBlocks.mutatedGrass }) {
			HoeItemForgeAccessor.botania$getTillables().put(grass,
					Pair.of(HoeItem::onlyIfAirAbove, HoeItem.changeIntoState(Blocks.FARMLAND.defaultBlockState())));
			ShovelItemForgeAccessor.botania$getFlattenables().put(grass, Blocks.DIRT_PATH.defaultBlockState());
		}
		PaintableData.init();
		evt.enqueueWork(() -> CompostingData.init((itemLike, chance) -> ComposterBlock.COMPOSTABLES.putIfAbsent(itemLike.asItem(), (float) chance)));
		DefaultCorporeaMatchers.init();
		PlayerHelper.setFakePlayerClass(FakePlayer.class);

		PatchouliAPI.get().registerMultiblock(BuiltInRegistries.BLOCK.getKey(BotaniaBlocks.alfPortal), AlfheimPortalBlockEntity.MULTIBLOCK.get());
		PatchouliAPI.get().registerMultiblock(BuiltInRegistries.BLOCK.getKey(BotaniaBlocks.terraPlate), TerrestrialAgglomerationPlateBlockEntity.MULTIBLOCK.get());
		PatchouliAPI.get().registerMultiblock(BuiltInRegistries.BLOCK.getKey(BotaniaBlocks.enchanter), ManaEnchanterBlockEntity.MULTIBLOCK.get());
		PatchouliAPI.get().registerMultiblock(prefix("gaia_ritual"), GaiaGuardianEntity.ARENA_MULTIBLOCK.get());

		OrechidManager.registerListener();
		ConfigDataManagerImpl.registerListener();
		CraftyCrateBlockEntity.registerListener();
		CorporeaNodeDetectors.register(new ForgeCapCorporeaNodeDetector());
		if (ModList.get().isLoaded("inventorysorter")) {
			InventorySorterIntegration.init(modBus);
		}
	}

	private void coreInit() {
		// ensure API implementations are loaded
		BotaniaAPI.LOGGER.debug("API instances: {}",
				List.of(BotaniaAPI.instance(), XplatAbstractions.instance(),
						CorporeaHelper.instance(), ManaItemHandler.instance()));

		ForgeBotaniaConfig.setup(container, this.modBus);
		EquipmentHandler.init();
	}

	private void registryInit(IEventBus modBus) {
		// Core item/block/BE
		bind(modBus, Registries.SOUND_EVENT, BotaniaSounds::init);
		bind(modBus, Registries.BLOCK, consumer -> {
			BotaniaBlocks.registerBlocks(RegistrationBackfill.blockRegistrar(consumer));
			BotaniaBlockFlammability.register();
		});
		bindForItems(modBus, consumer -> BotaniaBlocks.registerItemBlocks(RegistrationBackfill.itemRegistrar(consumer)));
		bind(modBus, Registries.BLOCK_ENTITY_TYPE, BotaniaBlockEntities::registerTiles);
		bindForItems(modBus, consumer -> BotaniaItems.registerItems(RegistrationBackfill.itemRegistrar(consumer)));
		bind(modBus, Registries.BLOCK, consumer -> BotaniaFlowerBlocks.registerBlocks(RegistrationBackfill.blockRegistrar(consumer)));
		bindForItems(modBus, consumer -> BotaniaFlowerBlocks.registerItemBlocks(RegistrationBackfill.itemRegistrar(consumer)));
		bind(modBus, Registries.BLOCK_ENTITY_TYPE, BotaniaFlowerBlocks::registerTEs);

		// GUI and Recipe
		bind(modBus, Registries.MENU, BotaniaItems::registerMenuTypes);
		bind(modBus, Registries.RECIPE_SERIALIZER, BotaniaItems::registerRecipeSerializers);
		bind(modBus, Registries.RECIPE_TYPE, BotaniaRecipeTypes::submitRecipeTypes);
		bind(modBus, Registries.RECIPE_SERIALIZER, BotaniaRecipeTypes::submitRecipeSerializers);

		// Entities
		bind(modBus, Registries.ENTITY_TYPE, BotaniaEntities::registerEntities);
		modBus.addListener((EntityAttributeCreationEvent e) -> BotaniaEntities.registerAttributes((type, builder) -> e.put(type, builder.build())));
		modBus.addListener((EntityAttributeModificationEvent e) -> {
			e.add(EntityType.PLAYER, PixieHandler.PIXIE_SPAWN_CHANCE);
		});
		// MC 26.1 NeoForge: mod construction happens after the vanilla bootstrap froze the built-in
		// registries, so these static registrations must run inside the RegisterEvent window (which
		// NeoForge opens per registry, before the final freeze). ATTRIBUTE is the first event fired.
		modBus.addListener((RegisterEvent evt) -> {
			if (evt.getRegistryKey().equals(Registries.ATTRIBUTE)) {
				PixieHandler.init();
			} else if (evt.getRegistryKey().equals(Registries.MOB_EFFECT)) {
				BotaniaMobEffects.init();
			} else if (evt.getRegistryKey().equals(Registries.TRIGGER_TYPE)) {
				BotaniaCriteriaTriggers.init();
			}
		});

		// Potions: BotaniaMobEffects is initialized in the RegisterEvent hook above.

		bind(modBus, BotaniaRegistries.BREWS, BotaniaBrews::submitRegistrations);

		// Worldgen
		bind(modBus, Registries.FEATURE, BotaniaFeatures::registerFeatures);
		bind(modBus, Registries.CHUNK_GENERATOR, SkyblockChunkGenerator::submitRegistration);

		// Rest (BotaniaCriteriaTriggers is initialized in the RegisterEvent hook above)
		bind(modBus, Registries.PARTICLE_TYPE, BotaniaParticles::registerParticles);

		bind(modBus, Registries.LOOT_CONDITION_TYPE, BotaniaLootModifiers::submitLootConditions);
		bind(modBus, Registries.LOOT_FUNCTION_TYPE, BotaniaLootModifiers::submitLootFunctions);
		// Vanilla's stat constructor does the registration too, so we use this
		// event only for timing, not for registering
		modBus.addListener((RegisterEvent evt) -> {
			if (evt.getRegistryKey().equals(Registries.CUSTOM_STAT)) {
				BotaniaStats.init();
			}
		});
		bind(modBus, Registries.CREATIVE_MODE_TAB, consumer -> {
			consumer.accept(CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.botania").withStyle(style -> style.withColor(ChatFormatting.WHITE)))
					.icon(() -> new ItemStack(BotaniaItems.lexicon))
					.withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
					.backgroundTexture(Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_botania.png"))
					.withSearchBar()
					.build(),
					BotaniaRegistries.BOTANIA_TAB_KEY.identifier());
		});
		modBus.addListener((BuildCreativeModeTabContentsEvent e) -> {
			if (e.getTabKey() == BotaniaRegistries.BOTANIA_TAB_KEY) {
				for (Item item : this.itemsToAddToCreativeTab) {
					if (item instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else if (item instanceof BlockItem bi && bi.getBlock() instanceof CustomCreativeTabContents cc) {
						cc.addToCreativeTab(item, e);
					} else {
						e.accept(item);
					}
				}
			}
		});
	}

	private static <T> void bind(IEventBus modBus, ResourceKey<Registry<T>> registry, Consumer<BiConsumer<T, Identifier>> source) {
		modBus.addListener((RegisterEvent event) -> {
			if (registry.equals(event.getRegistryKey())) {
				source.accept((t, rl) -> event.register(registry, rl, () -> t));
			}
		});
	}

	private final Set<Item> itemsToAddToCreativeTab = new LinkedHashSet<>();

	private void bindForItems(IEventBus modBus, Consumer<BiConsumer<Item, Identifier>> source) {
		modBus.addListener((RegisterEvent event) -> {
			if (event.getRegistryKey().equals(Registries.ITEM)) {
				source.accept((t, rl) -> {
					itemsToAddToCreativeTab.add(t);
					event.register(Registries.ITEM, rl, () -> t);
				});
			}
		});
	}

	private void registerEvents() {
		IEventBus bus = NeoForge.EVENT_BUS;

		int blazeTime = 2400 * (XplatAbstractions.INSTANCE.gogLoaded() ? 5 : 10);
		bus.addListener((FurnaceFuelBurnTimeEvent e) -> {
			if (e.getItemStack().is(BotaniaBlocks.blazeBlock.asItem())) {
				e.setBurnTime(blazeTime);
			}
		});

		if (XplatAbstractions.INSTANCE.gogLoaded()) {
			bus.addListener((PlayerInteractEvent.RightClickBlock e) -> {
				InteractionResult result = SkyblockWorldEvents.onPlayerInteract(e.getEntity(), e.getLevel(), e.getHand(), e.getHitVec());
				if (result == InteractionResult.SUCCESS) {
					e.setCanceled(true);
					e.setCancellationResult(InteractionResult.SUCCESS);
				}
			});
		}
		bus.addListener((PlayerInteractEvent.LeftClickBlock e) -> ((ShiftingCrustRodItem) BotaniaItems.exchangeRod).onLeftClick(
				e.getEntity(), e.getLevel(), e.getHand(), e.getPos(), e.getFace()));
		bus.addListener((PlayerInteractEvent.LeftClickEmpty e) -> TerraBladeItem.leftClick(e.getItemStack()));
		bus.addListener((AttackEntityEvent e) -> TerraBladeItem.attackEntity(
				e.getEntity(), e.getEntity().level(), InteractionHand.MAIN_HAND, e.getTarget(), null));
		bus.addListener((RegisterCommandsEvent e) -> this.registerCommands(
				e.getDispatcher(), e.getCommandSelection() == Commands.CommandSelection.DEDICATED));
		bus.addListener((CanPlayerSleepEvent e) -> {
			Player.BedSleepingProblem problem = SleepingHandler.trySleep(e.getEntity(), e.getPos());
			if (problem != null) {
				e.setProblem(problem);
			}
		});
		bus.addListener((PlayerEvent.StartTracking e) -> DaffomillBlockEntity.onItemTrack(e.getEntity(), (ServerPlayer) e.getEntity()));
		bus.addListener((LootTableLoadEvent e) -> LootHandler.lootLoad(e.getName(), b -> e.getTable().addPool(b.build())));
		bus.addListener((ManaNetworkEvent e) -> ManaNetworkHandler.instance.onNetworkEvent(e.getReceiver(), e.getType(), e.getAction()));
		bus.addListener((EntityJoinLevelEvent e) -> {
			if (!e.getLevel().isClientSide()) {
				TigerseyeBlockEntity.pacifyAfterLoad(e.getEntity(), (ServerLevel) e.getLevel());
			}
		});

		bus.addListener((ServerAboutToStartEvent e) -> this.serverAboutToStart(e.getServer()));
		bus.addListener((ServerStoppingEvent e) -> this.serverStopping(e.getServer()));
		bus.addListener((PlayerEvent.PlayerLoggedOutEvent e) -> FlugelTiaraItem.playerLoggedOut((ServerPlayer) e.getEntity()));
		bus.addListener((PlayerEvent.Clone e) -> ResoluteIvyItem.onPlayerRespawn(e.getOriginal(), e.getEntity(), !e.isWasDeath()));
		bus.addListener((LevelTickEvent.Post e) -> {
			if (e.getLevel() instanceof ServerLevel level) {
				CommonTickHandler.onTick(level);
				GrassSeedsItem.onTickEnd(level);
				TerraTruncatorItem.onTickEnd(level);
			}
		});
		bus.addListener((PlayerInteractEvent.RightClickBlock e) -> {
			RedStringInterceptorBlock.onInteract(e.getEntity(), e.getLevel(), e.getHand(), e.getHitVec());
			RingOfLokiItem.onPlayerInteract(e.getEntity(), e.getLevel(), e.getHand(), e.getHitVec());
		});
		bus.addListener((PlayerInteractEvent.RightClickItem e) -> {
			InteractionResult result = EnderAirItem.onPlayerInteract(e.getEntity(), e.getLevel(), e.getHand());
			if (result.consumesAction()) {
				e.setCanceled(true);
				e.setCancellationResult(result);
			}
		});

		// Below here are events implemented via Mixins on the Fabric side, ordered by Mixin name
		// FabricMixinAnvilMenu
		bus.addListener((AnvilUpdateEvent e) -> {
			if (SpellbindingClothItem.shouldDenyAnvil(e.getLeft(), e.getRight())) {
				e.setCanceled(true);
			}
		});
		// FabricMixinAxeItem
		bus.addListener((BlockEvent.BlockToolModificationEvent e) -> {
			if (e.getItemAbility() == ItemAbilities.AXE_STRIP) {
				BlockState input = e.getState();
				Block output = ForgeXplatImpl.CUSTOM_STRIPPABLES.get(input.getBlock());
				if (output != null) {
					e.setFinalState(output.withPropertiesOf(input));
				}
			}
		});
		// FabricMixinEnderMan
		bus.addListener((EntityTeleportEvent.EnderEntity e) -> {
			if (e.getEntityLiving() instanceof EnderMan em) {
				var newPos = VinculotusBlockEntity.onEndermanTeleport(em, e.getTargetX(), e.getTargetY(), e.getTargetZ());
				if (newPos != null) {
					e.setTargetX(newPos.x());
					e.setTargetY(newPos.y());
					e.setTargetZ(newPos.z());
				}
			}
		});
		// FabricMixinExplosion
		bus.addListener((ExplosionEvent.Detonate e) -> {
			if (BenevolentGoddessCharmItem.shouldProtectExplosion(e.getLevel(), e.getExplosion().center())) {
				e.getAffectedBlocks().clear();
			}
		});
		// FabricMixinItemEntity
		bus.addListener((ItemEntityPickupEvent.Pre e) -> {
			// 26.1: Pre is not cancellable; deny the pickup via TriState instead
			if (FlowerPouchItem.onPickupItem(e.getItemEntity(), e.getPlayer())) {
				e.setCanPickup(TriState.FALSE);
			}
		});
		// FabricMixinLivingEntity
		{
			bus.addListener((LivingDropsEvent e) -> {
				var living = e.getEntity();
				LooniumBlockEntity.dropLooniumItems(living, stack -> {
					e.getDrops().clear();
					if (!stack.isEmpty()) {
						var ent = new ItemEntity(living.level(), living.getX(), living.getY(), living.getZ(), stack);
						ent.setDefaultPickUpDelay();
						e.getDrops().add(ent);
					}
				});
			});
			bus.addListener((LivingDeathEvent e) -> {
				if (e.getSource().getEntity() instanceof LivingEntity killer) {
					SoulCrossMobEffect.onEntityKill(e.getEntity(), killer);
				}
			});
			bus.addListener((LivingEvent.LivingJumpEvent e) -> SojournersSashItem.onPlayerJump(e.getEntity()));
		}
		// FabricMixinPlayer
		{
			bus.addListener((LivingIncomingDamageEvent e) -> {
				if (e.getEntity() instanceof Player player
						&& RingOfOdinItem.onPlayerAttacked(player, e.getSource())) {
					e.setCanceled(true);
				}
			});
			bus.addListener((ItemTossEvent e) -> RingOfMagnetizationItem.onTossItem(e.getPlayer()));
			bus.addListener((LivingDamageEvent.Pre e) -> {
				if (e.getEntity() instanceof Player player) {
					Container worn = EquipmentHandler.getAllWorn(player);
					for (int i = 0; i < worn.getContainerSize(); i++) {
						ItemStack stack = worn.getItem(i);
						if (stack.getItem() instanceof CloakOfVirtueItem cloak) {
							e.setNewDamage(cloak.onPlayerDamage(player, e.getSource(), e.getNewDamage()));
						}
					}

					PixieHandler.onDamageTaken(player, e.getSource());
				}
				if (e.getSource().getDirectEntity() instanceof Player player) {
					CharmOfTheDivaItem.onEntityDamaged(player, e.getEntity());
				}
			});
			bus.addListener((EntityTickEvent.Post e) -> {
				if (e.getEntity() instanceof Player player) {
					FlugelTiaraItem.updatePlayerFlyStatus(player);
					SojournersSashItem.tickBelt(player);
					if (!player.level().isClientSide()) {
						EnderOverseerBlockEntity.checkLookingAtEnderOverseer(player);
					}
				}
			});
			bus.addListener((LivingFallEvent e) -> {
				if (e.getEntity() instanceof Player player) {
					e.setDistance(SojournersSashItem.onPlayerFall(player, (float) e.getDistance()));
				}
			});
			bus.addListener(EventPriority.LOW, (CriticalHitEvent e) -> {
				// 26.1: Event.Result is gone; isCriticalHit() false covers both DENY and DEFAULT-without-vanilla-crit
				if (e.getEntity().level().isClientSide()
						|| !e.isCriticalHit()
						|| !TerrasteelHelmItem.hasTerraArmorSet(e.getEntity())
						|| !(e.getTarget() instanceof LivingEntity target)) {
					return;
				}
				e.setDamageMultiplier(e.getDamageMultiplier() * TerrasteelHelmItem.getCritDamageMult(e.getEntity()));
				((PlayerAccess) e.getEntity()).botania$setCritTarget(target);
			});

		}
		// FabricMixinResultSlot
		bus.addListener((PlayerEvent.ItemCraftedEvent e) -> AssemblyHaloItem.onItemCrafted(e.getEntity(), e.getInventory()));
	}

	// Attaching caps requires dispatching off the item, which is a huge pain because it generates long if-else
	// chains on items, and also doesn't match how Fabric is set up.
	// Instead, let's declare ahead of time what items get which caps, similar to how we do it for Fabric.
	// Needs to be lazy since items aren't initialized yet
	private static final Supplier<Map<Item, Function<ItemStack, AvatarWieldable>>> AVATAR_WIELDABLES = Suppliers.memoize(() -> Map.of(
			BotaniaItems.dirtRod, s -> new LandsRodItem.AvatarBehavior(),
			BotaniaItems.diviningRod, s -> new PlentifulMantleRodItem.AvatarBehavior(),
			BotaniaItems.fireRod, s -> new HellsRodItem.AvatarBehavior(),
			BotaniaItems.missileRod, s -> new UnstableReservoirRodItem.AvatarBehavior(),
			BotaniaItems.rainbowRod, s -> new BifrostRodItem.AvatarBehavior(),
			BotaniaItems.tornadoRod, s -> new SkiesRodItem.AvatarBehavior()
	));

	private static final Supplier<Map<Item, Function<ItemStack, BlockProvider>>> BLOCK_PROVIDER = Suppliers.memoize(() -> Map.of(
			BotaniaItems.dirtRod, LandsRodItem.BlockProviderImpl::new,
			BotaniaItems.skyDirtRod, LandsRodItem.BlockProviderImpl::new,
			BotaniaItems.blackHoleTalisman, BlackHoleTalismanItem.BlockProviderImpl::new,
			BotaniaItems.cobbleRod, s -> new DepthsRodItem.BlockProviderImpl(),
			BotaniaItems.enderHand, EnderHandItem.BlockProviderImpl::new,
			BotaniaItems.terraformRod, s -> new TerraFirmaRodItem.BlockProviderImpl()
	));

	private static final Supplier<Map<Item, Function<ItemStack, CoordBoundItem>>> COORD_BOUND_ITEM = Suppliers.memoize(() -> Map.of(
			BotaniaItems.flugelEye, EyeOfTheFlugelItem.CoordBoundItemImpl::new,
			BotaniaItems.manaMirror, ManaMirrorItem.CoordBoundItemImpl::new,
			BotaniaItems.twigWand, WandOfTheForestItem.CoordBoundItemImpl::new,
			BotaniaItems.dreamwoodWand, WandOfTheForestItem.CoordBoundItemImpl::new
	));

	private static final Supplier<Map<Item, Function<ItemStack, ManaItem>>> MANA_ITEM = Suppliers.memoize(() -> Map.of(
			BotaniaItems.manaMirror, ManaMirrorItem.ManaItemImpl::new,
			BotaniaItems.manaRing, BandOfManaItem.ManaItemImpl::new,
			BotaniaItems.manaRingGreater, GreaterBandOfManaItem.GreaterManaItemImpl::new,
			BotaniaItems.manaTablet, ManaTabletItem.ManaItemImpl::new,
			BotaniaItems.terraPick, TerraShattererItem.ManaItemImpl::new
	));

	private static final Supplier<Map<Item, Function<ItemStack, Relic>>> RELIC = Suppliers.memoize(() -> Map.of(
			BotaniaItems.dice, DiceOfFateItem::makeRelic,
			BotaniaItems.flugelEye, EyeOfTheFlugelItem::makeRelic,
			BotaniaItems.infiniteFruit, FruitOfGrisaiaItem::makeRelic,
			BotaniaItems.kingKey, KeyOfTheKingsLawItem::makeRelic,
			BotaniaItems.lokiRing, RingOfLokiItem::makeRelic,
			BotaniaItems.odinRing, RingOfOdinItem::makeRelic,
			BotaniaItems.thorRing, RingOfThorItem::makeRelic
	));

	private static <T> ICapabilityProvider<ItemStack, Void, T> itemMapProvider(Supplier<Map<Item, Function<ItemStack, T>>> mapSupplier) {
		return (stack, ctx) -> {
			var factory = mapSupplier.get().get(stack.getItem());
			return factory == null ? null : factory.apply(stack);
		};
	}

	private static <T> void registerSelfCap(RegisterCapabilitiesEvent evt, BlockCapability<T, ?> cap,
			BlockEntityType<?> type, Function<BlockEntity, T> fn) {
		evt.registerBlockEntity(cap, type, (be, ctx) -> fn.apply(be));
	}

	private void registerCapabilities(RegisterCapabilitiesEvent evt) {
		// Item caps backed by per-item maps
		evt.registerItem(BotaniaForgeCapabilities.AVATAR_WIELDABLE, itemMapProvider(AVATAR_WIELDABLES),
				AVATAR_WIELDABLES.get().keySet().toArray(Item[]::new));
		evt.registerItem(BotaniaForgeCapabilities.BLOCK_PROVIDER, itemMapProvider(BLOCK_PROVIDER),
				BLOCK_PROVIDER.get().keySet().toArray(Item[]::new));
		evt.registerItem(BotaniaForgeCapabilities.COORD_BOUND_ITEM, itemMapProvider(COORD_BOUND_ITEM),
				COORD_BOUND_ITEM.get().keySet().toArray(Item[]::new));
		evt.registerItem(BotaniaForgeCapabilities.MANA_ITEM, itemMapProvider(MANA_ITEM),
				MANA_ITEM.get().keySet().toArray(Item[]::new));
		evt.registerItem(BotaniaForgeCapabilities.RELIC, itemMapProvider(RELIC),
				RELIC.get().keySet().toArray(Item[]::new));

		// Curios integration
		if (EquipmentHandler.instance instanceof CurioIntegration ci) {
			Item[] baubleItems = BuiltInRegistries.ITEM.stream()
					.filter(item -> item instanceof BaubleItem)
					.toArray(Item[]::new);
			ci.initCapability(evt, baubleItems);
		}

		// Fluid items (26.1: the fluid capability now hands out ResourceHandler<FluidResource>)
		evt.registerItem(Capabilities.Fluid.ITEM,
				(stack, access) -> new CapabilityUtil.WaterBowlFluidHandler(access),
				BotaniaItems.waterBowl);
		evt.registerItem(Capabilities.Fluid.ITEM,
				(stack, access) -> new CapabilityUtil.ExtrapolatedBucketFluidHandler(stack),
				BotaniaItems.openBucket);

		// Block lookasides for blocks that implement capabilities without a BE
		evt.registerBlock(BotaniaForgeCapabilities.HORN_HARVEST,
				(w, p, s, be, ctx) -> (world, pos, stack, hornType, living) -> hornType == HornHarvestable.EnumHornType.CANOPY,
				Blocks.VINE, Blocks.CAVE_VINES, Blocks.CAVE_VINES_PLANT, Blocks.TWISTING_VINES,
				Blocks.TWISTING_VINES_PLANT, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT);
		evt.registerBlock(BotaniaForgeCapabilities.HORN_HARVEST,
				(w, p, s, be, ctx) -> DefaultHornHarvestable.INSTANCE,
				ColorHelper.supportedColors().map(BotaniaBlocks::getMushroom).toArray(Block[]::new));
		evt.registerBlock(BotaniaForgeCapabilities.HORN_HARVEST,
				(w, p, s, be, ctx) -> DefaultHornHarvestable.INSTANCE,
				ColorHelper.supportedColors().map(BotaniaBlocks::getShinyFlower).toArray(Block[]::new));
		evt.registerBlock(BotaniaForgeCapabilities.MANA_GHOST,
				(w, p, s, be, ctx) -> (ManaCollisionGhost) s.getBlock(),
				BotaniaBlocks.manaDetector,
				BotaniaBlocks.abstrusePlatform, BotaniaBlocks.infrangiblePlatform, BotaniaBlocks.spectralPlatform,
				BotaniaBlocks.prism, BotaniaBlocks.tinyPlanet);
		evt.registerBlock(BotaniaForgeCapabilities.MANA_RECEIVER,
				(w, p, s, be, ctx) -> new ManaVoidBlock.ManaReceiverImpl(w, p, s),
				BotaniaBlocks.manaVoid);
		evt.registerBlock(BotaniaForgeCapabilities.MANA_TRIGGER,
				(w, p, s, be, ctx) -> new DrumBlock.ManaTriggerImpl(w, p, s),
				BotaniaBlocks.canopyDrum, BotaniaBlocks.wildDrum, BotaniaBlocks.gatheringDrum);
		evt.registerBlock(BotaniaForgeCapabilities.MANA_TRIGGER,
				(w, p, s, be, ctx) -> new ManastormChargeBlock.ManaTriggerImpl(w, p, s),
				BotaniaBlocks.manaBomb);
		evt.registerBlock(BotaniaForgeCapabilities.MANA_TRIGGER,
				(w, p, s, be, ctx) -> new ManaDetectorBlock.ManaTriggerImpl(w, p, s),
				BotaniaBlocks.manaDetector);
		evt.registerBlock(BotaniaForgeCapabilities.WANDABLE,
				(w, p, s, be, ctx) -> (player, stack, side) -> ((ForceRelayBlock) s.getBlock()).onUsedByWand(player, stack, w, p),
				BotaniaBlocks.pistonRelay);

		// Block entities that implement the capability interfaces themselves
		for (var type : BlockEntityConstants.SELF_WANDADBLE_BES) {
			registerSelfCap(evt, BotaniaForgeCapabilities.WANDABLE, type, be -> (Wandable) be);
		}
		for (var type : BlockEntityConstants.SELF_PHANTOM_INKABLE_BES) {
			registerSelfCap(evt, BotaniaForgeCapabilities.PHANTOM_INKABLE, type, be -> (PhantomInkableBlock) be);
		}
		for (var type : BlockEntityConstants.SELF_MANA_TRIGGER_BES) {
			registerSelfCap(evt, BotaniaForgeCapabilities.MANA_TRIGGER, type, be -> (ManaTrigger) be);
		}
		for (var type : BlockEntityConstants.SELF_MANA_RECEIVER_BES) {
			registerSelfCap(evt, BotaniaForgeCapabilities.MANA_RECEIVER, type, be -> (ManaReceiver) be);
		}
		for (var type : BlockEntityConstants.SELF_SPARK_ATTACHABLE_BES) {
			registerSelfCap(evt, BotaniaForgeCapabilities.SPARK_ATTACHABLE, type, be -> (SparkAttachable) be);
		}

		// Furnace exoflame support
		evt.registerBlockEntity(BotaniaForgeCapabilities.EXOFLAME_HEATABLE, BlockEntityType.FURNACE,
				(be, ctx) -> new ExoflameFurnaceHandler.FurnaceExoflameHeatable((AbstractFurnaceBlockEntity) be));
		evt.registerBlockEntity(BotaniaForgeCapabilities.EXOFLAME_HEATABLE, BlockEntityType.BLAST_FURNACE,
				(be, ctx) -> new ExoflameFurnaceHandler.FurnaceExoflameHeatable((AbstractFurnaceBlockEntity) be));
		evt.registerBlockEntity(BotaniaForgeCapabilities.EXOFLAME_HEATABLE, BlockEntityType.SMOKER,
				(be, ctx) -> new ExoflameFurnaceHandler.FurnaceExoflameHeatable((AbstractFurnaceBlockEntity) be));

		// Items view for BEs with an exposed simple inventory
		Stream.of(
				BotaniaBlockEntities.PRISM, BotaniaBlockEntities.SPREADER,
				BotaniaBlockEntities.HOURGLASS, BotaniaBlockEntities.INCENSE_PLATE,
				BotaniaBlockEntities.OPEN_CRATE, BotaniaBlockEntities.SPARK_CHANGER, BotaniaBlockEntities.TINY_POTATO
		).forEach(blockEntityType -> evt.registerBlockEntity(
				Capabilities.Item.BLOCK, blockEntityType,
				(be, ctx) -> new WorldlyContainerWrapper((WorldlyContainer) be, ctx)));

		// Red string containers proxy the bound BE's inventory
		evt.registerBlockEntity(Capabilities.Item.BLOCK, BotaniaBlockEntities.RED_STRING_CONTAINER,
				(be, side) -> new RedStringContainerCapProvider(be).getCapability(be, side));
		evt.registerBlockEntity(Capabilities.Item.BLOCK, BotaniaBlockEntities.RED_STRING_DISPENSER,
				(be, side) -> new RedStringContainerCapProvider(be).getCapability(be, side));

		// we only provide a view of the energy level, no interaction allowed
		evt.registerBlockEntity(Capabilities.Energy.BLOCK, BotaniaBlockEntities.FLUXFIELD,
				(gen, context) -> new EnergyHandler() {
					@Override
					public long getAmountAsLong() {
						return gen.getEnergy();
					}

					@Override
					public long getCapacityAsLong() {
						return PowerGeneratorBlockEntity.MAX_ENERGY;
					}

					@Override
					public int insert(int amount, TransactionContext ctx) {
						return 0;
					}

					@Override
					public int extract(int amount, TransactionContext ctx) {
						return 0;
					}
				});

		// Hourglass trigger
		evt.registerBlockEntity(BotaniaForgeCapabilities.HOURGLASS_TRIGGER, BotaniaBlockEntities.ANIMATED_TORCH,
				(be, ctx) -> hourglass -> be.toggle());
	}

	private void serverAboutToStart(MinecraftServer server) {
		if (BotaniaAPI.instance().getClass() != BotaniaAPIImpl.class) {
			String clname = BotaniaAPI.instance().getClass().getName();
			throw new IllegalAccessError("The Botania API has been overriden. "
					+ "This will cause crashes and compatibility issues, and that's why it's marked as"
					+ " \"Do not Override\". Whoever had the brilliant idea of overriding it needs to go"
					+ " back to elementary school and learn to read. (Actual classname: " + clname + ")");
		}

		if (server.isDedicatedServer()) {
			ContributorList.firstStart();
		}
	}

	private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
		if (XplatAbstractions.INSTANCE.gogLoaded()) {
			SkyblockCommand.register(dispatcher);
		}
	}

	private void serverStopping(MinecraftServer server) {
		ManaNetworkHandler.instance.clear();
		CorporeaIndexBlockEntity.clearIndexCache();
	}

}
