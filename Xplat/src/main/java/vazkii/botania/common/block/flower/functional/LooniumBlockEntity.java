/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.flower.functional;

import net.minecraft.world.entity.EntitySpawnReason;
import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.*;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.api.configdata.ConfigDataManager;
import vazkii.botania.api.configdata.LooniumMobAttributeModifier;
import vazkii.botania.api.configdata.LooniumMobEffectToApply;
import vazkii.botania.api.configdata.LooniumMobSpawnData;
import vazkii.botania.api.configdata.LooniumStructureConfiguration;
import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.internal_caps.LooniumComponent;
import vazkii.botania.common.lib.BotaniaTags;
import vazkii.botania.common.loot.BotaniaLootTables;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class LooniumBlockEntity extends FunctionalFlowerBlockEntity {
	private static final int RANGE = 5;
	private static final int CHECK_RANGE = 9;
	private static final String TAG_LOOT_TABLE = "lootTable";
	private static final String TAG_DETECTED_STRUCTURE = "detectedStructure";
	private static final String TAG_CONFIG_OVERRIDE = "configOverride";
	private static final String TAG_ATTUNE_DISPLAY_OVERRIDE = "attuneDisplayOverride";
	private static final Supplier<LooniumStructureConfiguration> FALLBACK_CONFIG =
			Suppliers.memoize(() -> LooniumStructureConfiguration.builder()
					.manaCost(LooniumStructureConfiguration.DEFAULT_COST)
					.maxNearbyMobs(LooniumStructureConfiguration.DEFAULT_MAX_NEARBY_MOBS)
					.boundingBoxType(StructureSpawnOverride.BoundingBoxType.PIECE)
					.spawnedMobs(LooniumMobSpawnData.entityWeight(EntityType.ZOMBIE, 1).build())
					.attributeModifiers()
					.effectsToApply(
							LooniumMobEffectToApply.effect(MobEffects.REGENERATION).build(),
							LooniumMobEffectToApply.effect(MobEffects.FIRE_RESISTANCE).build(),
							LooniumMobEffectToApply.effect(MobEffects.RESISTANCE).build(),
							LooniumMobEffectToApply.effect(MobEffects.HEALTH_BOOST).build()
					)
					.build());

	// this should never collide with the /team command, since space is not allowed in scoreboard team names
	public static final String LOONIUM_TEAM_NAME = "Loonium Monsters";

	/**
	 * 26.1: vanilla {@code Entity.getTeam()} now returns {@code PlayerTeam}, so the
	 * 1.20.1 fake anonymous Team subclass (returned from an EntityMixin) crashes with
	 * a ClassCastException the moment any vanilla code touches the mob's team. The
	 * team is now a real scoreboard team, created with the same options the fake
	 * team had (friendly fire on, invisibles visible, always-visible name/death tags,
	 * normal collisions). Client side receives team membership via the vanilla
	 * scoreboard sync, so identification and in-fighting behavior are unchanged.
	 */
	public static void joinLooniumTeam(ServerLevel world, Mob mob) {
		Scoreboard scoreboard = world.getScoreboard();
		PlayerTeam team = scoreboard.getPlayerTeam(LOONIUM_TEAM_NAME);
		if (team == null) {
			team = scoreboard.addPlayerTeam(LOONIUM_TEAM_NAME);
			team.setAllowFriendlyFire(true);
			team.setSeeFriendlyInvisibles(true);
			team.setNameTagVisibility(Team.Visibility.ALWAYS);
			team.setDeathMessageVisibility(Team.Visibility.ALWAYS);
			team.setCollisionRule(Team.CollisionRule.ALWAYS);
		}
		scoreboard.addPlayerToTeam(mob.getScoreboardName(), team);
	}

	@Nullable
	private Identifier lootTableOverride;
	@Nullable
	private Object2BooleanMap<Identifier> detectedStructures;
	@Nullable
	private Identifier configOverride;
	@Nullable
	private String attuneDisplayOverride;

	public LooniumBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaFlowerBlocks.LOONIUM, pos, state);
	}

	@Override
	public void tickFlower() {
		super.tickFlower();

		if (!(getLevel() instanceof ServerLevel world)) {
			return;
		}

		if (detectedStructures == null) {
			// Detection intentionally uses the flower position, not the effective position,
			// since the latter could change while this detection is only executed once.
			detectStructure(world);
		}

		if (redstoneSignal != 0 || ticksExisted % 100 != 0 || world.getDifficulty() == Difficulty.PEACEFUL
		// so mobs won't spawn in unloaded or border chunks
				|| !world.isPositionEntityTicking(getEffectivePos())) {
			return;
		}

		ConfigDataManager configData = BotaniaAPI.instance().getConfigData();
		Map<Identifier, LooniumStructureConfiguration> structureConfigs = determineStructureConfigs(configData, detectedStructures);
		List<Pair<Identifier, LootTable>> lootTables = determineLootTables(world, structureConfigs.keySet());

		if (lootTables.isEmpty()) {
			return;
		}

		Pair<Identifier, LootTable> randomPick = lootTables.get(world.getRandom().nextInt(lootTables.size()));
		LooniumStructureConfiguration pickedConfig = structureConfigs.getOrDefault(randomPick.key(),
				structureConfigs.get(LooniumStructureConfiguration.DEFAULT_CONFIG_ID));
		LootTable pickedLootTable = randomPick.value();

		if (getMana() < pickedConfig.manaCost) {
			return;
		}

		int numberOfMobsAround = countNearbyMobs(world, pickedConfig);
		if (numberOfMobsAround >= pickedConfig.maxNearbyMobs) {
			return;
		}

		LooniumMobSpawnData pickedMobType = pickedConfig.spawnedMobs.getRandom(world.getRandom()).orElse(null);
		if (pickedMobType == null) {
			return;
		}

		spawnMob(world, pickedMobType, pickedConfig, pickedLootTable);
	}

	private void spawnMob(ServerLevel world, LooniumMobSpawnData pickedMobType,
			LooniumStructureConfiguration pickedConfig, LootTable pickedLootTable) {

		ItemStack lootStack = pickRandomLootItem(world, pickedLootTable);
		if (lootStack.isEmpty()) {
			return;
		}

		RandomSource random = world.getRandom();
		double x = getEffectivePos().getX() + 0.5 - RANGE + 2 * RANGE * random.nextDouble();
		double y = getEffectivePos().getY();
		double z = getEffectivePos().getZ() + 0.5 - RANGE + 2 * RANGE * random.nextDouble();

		while (!world.noCollision(pickedMobType.type.getSpawnAABB(x, y, z))) {
			y += 1.0;
			if (y >= world.getMaxY() + 1) {
				return;
			}
		}

		Entity entity = pickedMobType.type.create(world, EntitySpawnReason.TRIGGERED);
		if (!(entity instanceof Mob mob)) {
			return;
		}

		if (pickedMobType.nbt != null) {
			// 26.1: readAdditionalSaveData is protected; Entity.load(ValueInput) is public now
			mob.load(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), pickedMobType.nbt));
		}
		if (pickedMobType.spawnAsBaby != null) {
			mob.setBaby(pickedMobType.spawnAsBaby);
		}

		mob.absSnapTo(x, y, z, random.nextFloat() * 360F, 0);
		mob.setDeltaMovement(Vec3.ZERO);

		applyAttributesAndEffects(pickedMobType, pickedConfig, mob);

		LooniumComponent looniumComponent = XplatAbstractions.INSTANCE.looniumComponent(mob);
		if (looniumComponent != null) {
			looniumComponent.setSlowDespawn(true);
			looniumComponent.setOverrideDrop(true);
			looniumComponent.setDrop(lootStack);
		}

		joinLooniumTeam(world, mob);

		mob.finalizeSpawn(world, world.getCurrentDifficultyAt(mob.blockPosition()), EntitySpawnReason.SPAWNER, null);
		if (Boolean.FALSE.equals(pickedMobType.spawnAsBaby) && mob.isBaby()) {
			// Note: might have already affected initial equipment/attribute selection, or even caused a special
			// mob configuration (such as chicken jockey) to spawn, which may look weird when reverting to adult.
			mob.setBaby(false);
		}

		if (pickedMobType.equipmentTable != null) {
			LootTable equipmentTable = world.getServer().reloadableRegistries()
					.getLootTable(BotaniaLootTables.key(pickedMobType.equipmentTable));
			if (equipmentTable != LootTable.EMPTY) {
				LootParams lootParams = new LootParams.Builder(world)
						.withParameter(LootContextParams.THIS_ENTITY, mob)
						.withParameter(LootContextParams.ORIGIN, mob.position())
						// TODO 1.21: replace with LootContextParamSets.EQUIPMENT
						.create(LootContextParamSets.SELECTOR);
				var equippedSlots = new HashSet<EquipmentSlot>();
				equipmentTable.getRandomItems(lootParams, equipmentStack -> {
					EquipmentSlot slot = equipmentStack.is(BotaniaTags.Items.LOONIUM_OFFHAND_EQUIPMENT)
							? EquipmentSlot.OFFHAND
							: mob.getEquipmentSlotForItem(equipmentStack);
					if (equippedSlots.contains(slot)) {
						slot = equippedSlots.contains(EquipmentSlot.MAINHAND)
								&& !equipmentStack.has(net.minecraft.core.component.DataComponents.TOOL)
										? EquipmentSlot.OFFHAND
										: EquipmentSlot.MAINHAND;
					}
					if (!equippedSlots.add(slot)) {
						return;
					}
					mob.setItemSlot(slot, slot.isArmor() ? equipmentStack.copyWithCount(1) : equipmentStack);
				});
			}
		}

		// in case the mob spawned with a vehicle or passenger(s), ensure those don't drop unexpected loot
		mob.getRootVehicle().getPassengersAndSelf().forEach(e -> {
			if (e instanceof Mob otherMob) {
				// prevent armor/weapon drops on player kill, also no nautilus shells from drowned:
				Arrays.stream(EquipmentSlot.values()).forEach(slot -> otherMob.setDropChance(slot, 0));

				if (otherMob instanceof PatrollingMonster patroller && patroller.isPatrolLeader()) {
					//  Loonium may be presenting challenges, but not that type of challenge
					patroller.setPatrolLeader(false);
					patroller.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
				}

				if (e == mob) {
					return;
				}

				Optional<LooniumMobSpawnData> mobType = pickedConfig.spawnedMobs.unwrap().stream()
						.map(net.minecraft.util.random.Weighted::value)
						.filter(mobSpawnData -> mobSpawnData.type.tryCast(otherMob) != null).findFirst();

				ItemStack bonusLoot;
				if (mobType.isPresent()) {
					applyAttributesAndEffects(mobType.get(), pickedConfig, otherMob);
					bonusLoot = pickRandomLootItem(world, pickedLootTable);
				} else {
					bonusLoot = ItemStack.EMPTY;
				}

				LooniumComponent otherLooniumComponent = XplatAbstractions.INSTANCE.looniumComponent(otherMob);
				if (otherLooniumComponent != null) {
					otherLooniumComponent.setSlowDespawn(true);
					otherLooniumComponent.setOverrideDrop(true);
					otherLooniumComponent.setDrop(bonusLoot);
				}
			}
		});

		if (!world.tryAddFreshEntityWithPassengers(mob)) {
			return;
		}

		mob.spawnAnim();
		world.levelEvent(LevelEvent.PARTICLES_MOBBLOCK_SPAWN, getBlockPos(), 0);
		world.gameEvent(mob, GameEvent.ENTITY_PLACE, mob.position());

		addMana(-pickedConfig.manaCost);
		sync();
	}

	private static void applyAttributesAndEffects(LooniumMobSpawnData mobSpawnData,
			LooniumStructureConfiguration pickedConfig, Mob mob) {
		List<LooniumMobAttributeModifier> attributeModifiers = mobSpawnData.attributeModifiers != null
				? mobSpawnData.attributeModifiers
				: pickedConfig.attributeModifiers;
		for (LooniumMobAttributeModifier attributeModifier : attributeModifiers) {
			AttributeInstance attribute = mob.getAttribute(attributeModifier.attribute);
			if (attribute != null) {
				attribute.addPermanentModifier(attributeModifier.createAttributeModifier());
				if (attribute.getAttribute() == Attributes.MAX_HEALTH.value()) {
					mob.setHealth(mob.getMaxHealth());
				}
			}
		}

		List<LooniumMobEffectToApply> effectsToApply = mobSpawnData.effectsToApply != null
				? mobSpawnData.effectsToApply
				: pickedConfig.effectsToApply;
		for (LooniumMobEffectToApply effectToApply : effectsToApply) {
			mob.addEffect(effectToApply.createMobEffectInstance());
		}
	}

	private int countNearbyMobs(ServerLevel world, LooniumStructureConfiguration pickedConfig) {
		var setOfMobTypes = pickedConfig.spawnedMobs.unwrap().stream().map(msd -> msd.value().type).collect(Collectors.toSet());
		return world.getEntitiesOfClass(Mob.class, new AABB(getEffectivePos()).inflate(CHECK_RANGE),
				m -> setOfMobTypes.contains(m.getType())).size();
	}

	private static ItemStack pickRandomLootItem(ServerLevel world, LootTable pickedLootTable) {
		LootParams params = new LootParams.Builder(world).create(LootContextParamSets.EMPTY);
		List<ItemStack> stacks = pickedLootTable.getRandomItems(params, world.getRandom().nextLong());
		stacks.removeIf(s -> s.isEmpty() || s.is(BotaniaTags.Items.LOONIUM_BLACKLIST));
		if (stacks.isEmpty()) {
			return ItemStack.EMPTY;
		} else {
			Collections.shuffle(stacks);
			return stacks.get(0);
		}
	}

	@NotNull
	private List<Pair<Identifier, LootTable>> determineLootTables(ServerLevel world,
			Set<Identifier> structureIds) {
		var lootTables = new ArrayList<Pair<Identifier, LootTable>>();
		var lootData = world.getServer().reloadableRegistries();
		Supplier<LootTable> defaultLootTableSupplier = Suppliers.memoize(() -> lootData.getLootTable(
				BotaniaLootTables.key(BotaniaLootTables.LOONIUM_DEFAULT_LOOT)));
		if (lootTableOverride != null) {
			LootTable lootTable = lootData.getLootTable(BotaniaLootTables.key(lootTableOverride));
			if (lootTable != LootTable.EMPTY) {
				lootTables.add(Pair.of(LooniumStructureConfiguration.DEFAULT_CONFIG_ID, lootTable));
			}
		} else {
			for (Identifier structureId : structureIds) {
				if (structureId.equals(LooniumStructureConfiguration.DEFAULT_CONFIG_ID)) {
					continue;
				}
				Identifier lootTableId = prefix("loonium/%s/%s".formatted(structureId.getNamespace(), structureId.getPath()));
				LootTable lootTable = lootData.getLootTable(BotaniaLootTables.key(lootTableId));
				if (lootTable != LootTable.EMPTY) {
					lootTables.add(Pair.of(structureId, lootTable));
				} else {
					LootTable defaultLootTable = defaultLootTableSupplier.get();
					if (defaultLootTable != LootTable.EMPTY) {
						lootTables.add(Pair.of(structureId, defaultLootTable));
					}
				}
			}
		}
		if (lootTables.isEmpty()) {
			LootTable defaultLootTable = defaultLootTableSupplier.get();
			if (defaultLootTable != LootTable.EMPTY) {
				lootTables.add(Pair.of(LooniumStructureConfiguration.DEFAULT_CONFIG_ID, defaultLootTable));
			}
		}
		return lootTables;
	}

	/**
	 * Build a map of structure IDs to resolved Loonium configurations, i.e. no need to traverse any parents.
	 * 
	 * @param configData Configuration data to read from.
	 * @param structures Detected structures to work with.
	 * @return The map, which is guaranteed to not be empty.
	 */
	@NotNull
	private Map<Identifier, LooniumStructureConfiguration> determineStructureConfigs(
			@NotNull ConfigDataManager configData, @NotNull Object2BooleanMap<Identifier> structures) {
		if (configOverride != null) {
			LooniumStructureConfiguration overrideConfig =
					configData.getEffectiveLooniumStructureConfiguration(configOverride);
			return Map.of(LooniumStructureConfiguration.DEFAULT_CONFIG_ID,
					overrideConfig != null ? overrideConfig : getDefaultConfig(configData));
		}

		LooniumStructureConfiguration defaultConfig = getDefaultConfig(configData);
		var structureConfigs = new HashMap<Identifier, LooniumStructureConfiguration>();
		for (Object2BooleanMap.Entry<Identifier> structureEntry : structures.object2BooleanEntrySet()) {
			LooniumStructureConfiguration structureSpecificConfig =
					configData.getEffectiveLooniumStructureConfiguration(structureEntry.getKey());
			LooniumStructureConfiguration structureConfig = structureSpecificConfig != null ? structureSpecificConfig : defaultConfig;
			if (structureConfig != null && (structureEntry.getBooleanValue()
					|| structureConfig.boundingBoxType == StructureSpawnOverride.BoundingBoxType.STRUCTURE)) {
				structureConfigs.put(structureEntry.getKey(), structureConfig);
			}
		}

		structureConfigs.put(LooniumStructureConfiguration.DEFAULT_CONFIG_ID, defaultConfig);
		return structureConfigs;
	}

	private static LooniumStructureConfiguration getDefaultConfig(ConfigDataManager configData) {
		LooniumStructureConfiguration defaultConfig = configData.getEffectiveLooniumStructureConfiguration(
				LooniumStructureConfiguration.DEFAULT_CONFIG_ID);
		return defaultConfig != null ? defaultConfig : FALLBACK_CONFIG.get();
	}

	private void detectStructure(ServerLevel world) {
		// structure ID and whether the position is inside a structure piece (false = only overall bounding box)
		var structureMap = new Object2BooleanRBTreeMap<Identifier>();
		StructureManager structureManager = world.structureManager();
		BlockPos pos = getBlockPos();
		Map<Structure, LongSet> structures = structureManager.getAllStructuresAt(pos);
		for (Map.Entry<Structure, LongSet> entry : structures.entrySet()) {
			Structure structure = entry.getKey();
			StructureStart start = structureManager.getStructureAt(pos, structure);
			if (start.isValid()) {
				Identifier structureId =
						world.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(structure);
				boolean insidePiece = structureManager.structureHasPieceAt(pos, start);
				if (insidePiece || !structureMap.getBoolean(structureId)) {
					structureMap.put(structureId, insidePiece);
				}
			}
		}

		detectedStructures = new Object2BooleanArrayMap<>(structureMap);

		setChanged();
		sync();
	}

	@Override
	public int getColor() {
		return 0xC29D62;
	}

	@Override
	public int getMaxMana() {
		return LooniumStructureConfiguration.DEFAULT_COST;
	}

	@Override
	public boolean acceptsRedstone() {
		return true;
	}

	@Override
	public RadiusDescriptor getRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
	}

	@Override
	public RadiusDescriptor getSecondaryRadius() {
		return RadiusDescriptor.Rectangle.square(getEffectivePos(), CHECK_RANGE);
	}

	@Override
	public void readFromPacketNBT(CompoundTag cmp) {
		super.readFromPacketNBT(cmp);
		if (cmp.contains(TAG_LOOT_TABLE)) {
			lootTableOverride = Identifier.parse(cmp.getStringOr(TAG_LOOT_TABLE, ""));
		}
		if (cmp.contains(TAG_CONFIG_OVERRIDE)) {
			configOverride = Identifier.parse(cmp.getStringOr(TAG_CONFIG_OVERRIDE, ""));
		}
		if (cmp.contains(TAG_ATTUNE_DISPLAY_OVERRIDE)) {
			attuneDisplayOverride = cmp.getStringOr(TAG_ATTUNE_DISPLAY_OVERRIDE, "");
		}
		if (cmp.contains(TAG_DETECTED_STRUCTURE)) {
			String rawString = cmp.getStringOr(TAG_DETECTED_STRUCTURE, "");
			if (rawString.isEmpty()) {
				detectedStructures = Object2BooleanMaps.emptyMap();
			} else {
				List<ObjectBooleanPair<Identifier>> structureList = Arrays.stream(rawString.split(",")).map(part -> {
					if (part.contains("|")) {
						String[] components = part.split("\\|", 2);
						return ObjectBooleanPair.of(Identifier.parse(components[0]), Boolean.parseBoolean(components[1]));
					} else {
						return ObjectBooleanPair.of(Identifier.parse(part), false);
					}
				}).toList();
				// list should never contain more than a few entries, so array is fine and retains entry order
				var map = new Object2BooleanArrayMap<Identifier>(structureList.size());
				structureList.forEach(entry -> map.put(entry.key(), entry.valueBoolean()));
				detectedStructures = map;
			}
		}
	}

	@Override
	public void writeToPacketNBT(CompoundTag cmp) {
		super.writeToPacketNBT(cmp);
		if (lootTableOverride != null) {
			cmp.putString(TAG_LOOT_TABLE, lootTableOverride.toString());
		}
		if (configOverride != null) {
			cmp.putString(TAG_CONFIG_OVERRIDE, configOverride.toString());
		}
		if (attuneDisplayOverride != null) {
			cmp.putString(TAG_ATTUNE_DISPLAY_OVERRIDE, attuneDisplayOverride);
		}
		if (detectedStructures != null) {
			var stringBuilder = new StringBuilder();
			boolean first = true;
			for (Object2BooleanMap.Entry<Identifier> entry : detectedStructures.object2BooleanEntrySet()) {
				if (first) {
					first = false;
				} else {
					stringBuilder.append(',');
				}
				stringBuilder.append(entry.getKey()).append('|').append(entry.getBooleanValue());
			}
			cmp.putString(TAG_DETECTED_STRUCTURE, stringBuilder.toString());
		}
	}

	public static void dropLooniumItems(LivingEntity living, Consumer<ItemStack> consumer) {
		LooniumComponent comp = XplatAbstractions.INSTANCE.looniumComponent(living);
		if (comp != null && comp.isOverrideDrop()) {
			consumer.accept(comp.getDrop());
		}
	}

	public static class WandHud extends BindableFlowerWandHud<LooniumBlockEntity> {
		public WandHud(LooniumBlockEntity flower) {
			super(flower);
		}

		@Override
		public void renderHUD(GuiGraphicsExtractor gui, Minecraft mc) {
			String lootType;
			String structureName = "";
			if (flower.attuneDisplayOverride != null) {
				lootType = flower.attuneDisplayOverride;
			} else if (flower.lootTableOverride != null) {
				lootType = "attuned";
			} else if (flower.detectedStructures == null || flower.detectedStructures.isEmpty()) {
				lootType = "not_attuned";
			} else {
				if (flower.detectedStructures.size() == 1) {
					lootType = "attuned_one";
					structureName = flower.detectedStructures
							.keySet().stream().findFirst()
							.map(rl -> I18n.get("structure." + rl.getNamespace() + "." + rl.getPath().replace("/", ".")))
							.orElseGet(() -> "");
				} else {
					lootType = "attuned_many";
				}
			}

			String lootTypeMessage = I18n.get("botaniamisc.loonium." + lootType, structureName);
			int lootTypeWidth = mc.font.width(lootTypeMessage);
			int lootTypeTextStart = (mc.getWindow().getGuiScaledWidth() - lootTypeWidth) / 2;
			int halfMinWidth = (lootTypeWidth + 4) / 2;
			int centerY = mc.getWindow().getGuiScaledHeight() / 2;

			super.renderHUD(gui, mc, halfMinWidth, halfMinWidth, 40);
			gui.text(mc.font, lootTypeMessage, lootTypeTextStart, centerY + 30, flower.getColor());
		}
	}
}
