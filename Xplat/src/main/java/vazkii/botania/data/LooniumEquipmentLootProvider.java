package vazkii.botania.data;

import net.minecraft.advancements.criterion.*;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.trim.*;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import vazkii.botania.common.loot.BotaniaLootTables;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public class LooniumEquipmentLootProvider implements DataProvider {
	public static final int COLOR_ENDERMAN_BODY = 0x1d1d21; // (black)
	public static final int COLOR_TIDE_LEATHER = 0x169c9c; // (cyan)
	public static final int COLOR_EVOKER_COAT = 0x323639; // (black + gray)
	public static final int COLOR_VINDICATOR_BOOTS = 0x323639; // (black + gray(
	public static final int COLOR_VINDICATOR_JACKET = 0x474f52; // (gray)
	public static final int COLOR_VINDICATOR_LEGWEAR = 0x168c8c; // (black + 7 cyan)
	public static final int COLOR_ILLUSIONER_COAT = 0x3b7bc2; // (blue + light blue)

	private final PackOutput.PathProvider pathProvider;
	private final CompletableFuture<HolderLookup.Provider> registryLookupFuture;
	private HolderLookup.Provider registryLookup;

	public LooniumEquipmentLootProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
		// NOTE: equipment loot tables become a vanilla feature in future versions
		this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, Registries.LOOT_TABLE.identifier().getPath());
		this.registryLookupFuture = registryLookupFuture;
	}

	@NotNull
	@Override
	public CompletableFuture<?> run(@NotNull CachedOutput cache) {
		return registryLookupFuture.thenCompose(registryLookup -> this.run(cache, registryLookup));
	}

	private CompletableFuture<?> run(@NotNull CachedOutput cache, HolderLookup.Provider registryLookup) {
		this.registryLookup = registryLookup;
		HolderLookup.RegistryLookup<TrimPattern> patternRegistry = registryLookup.lookupOrThrow(Registries.TRIM_PATTERN);
		HolderLookup.RegistryLookup<TrimMaterial> materialRegistry = registryLookup.lookupOrThrow(Registries.TRIM_MATERIAL);
		BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory =
				(pattern, material) -> getTrim(patternRegistry, materialRegistry, pattern, material);
		BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory =
				(trim, armorItems) -> createArmorSet(List.of(addTrimToComponent(trim)), true, armorItems);
		TriFunction<ArmorTrim, Integer, Item[], LootTable.Builder> randomizedDyedSetFactory =
				(trim, color, armorItems) -> createArmorSet(List.of(addTrimToComponent(trim),
						addDyedColorToComponent(color)), true, armorItems);
		TriFunction<ArmorTrim, Integer, Item[], LootTable.Builder> fixedDyedSetFactory =
				(trim, color, armorItems) -> createArmorSet(List.of(addTrimToComponent(trim),
						addDyedColorToComponent(color)), false, armorItems);

		Map<ArmorMaterial, Item[]> armorItems = Map.of(
				ArmorMaterials.LEATHER, new Item[] {
						Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS, Items.LEATHER_BOOTS
				},
				ArmorMaterials.CHAINMAIL, new Item[] {
						Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS
				},
				ArmorMaterials.IRON, new Item[] {
						Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS
				},
				ArmorMaterials.GOLD, new Item[] {
						Items.GOLDEN_HELMET, Items.GOLDEN_CHESTPLATE, Items.GOLDEN_LEGGINGS, Items.GOLDEN_BOOTS
				},
				ArmorMaterials.DIAMOND, new Item[] {
						Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS
				},
				ArmorMaterials.NETHERITE, new Item[] {
						Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS
				}
		);

		Map<Identifier, LootTable.Builder> tables = new HashMap<>();

		defineWeaponEquipmentTables(tables);
		defineAncientCityEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineBastionRemnantEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineDesertPyramidEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineEndCityEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineJungleTempleEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineFortressEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineOceanMonumentEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory, randomizedDyedSetFactory);
		definePillagerOutpostEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineRuinedPortalEquipmentTables(tables);
		defineShipwreckEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineStrongholdEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineTrailRuinsEquipmentTables(tables, armorItems, trimFactory, randomizedSetFactory);
		defineWoodlandMansionEquipmentTables(tables, trimFactory, fixedDyedSetFactory);

		// TODO: we should be using LootTableSubProvider implementations instead of three individual loot providers
		var output = new ArrayList<CompletableFuture<?>>(tables.size());
		for (Map.Entry<Identifier, LootTable.Builder> e : tables.entrySet()) {
			Path path = pathProvider.json(e.getKey());
			LootTable.Builder builder = e.getValue();
			// TODO 1.21: use LootContextParamSets.EQUIPMENT instead
			LootTable lootTable = builder.setParamSet(LootContextParamSets.SELECTOR).build();
			output.add(DataProvider.saveStable(cache, registryLookup, LootTable.DIRECT_CODEC, lootTable, path));
		}
		return CompletableFuture.allOf(output.toArray(CompletableFuture<?>[]::new));
	}

	private void defineWeaponEquipmentTables(Map<Identifier, LootTable.Builder> tables) {
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_AXE,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(LootItem.lootTableItem(Items.IRON_AXE))
				// no need to add diamond axe, it's the same base damage, but actually less enchantable
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_AXE_GOLD,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(LootItem.lootTableItem(Items.GOLDEN_AXE))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_BOW,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(LootItem.lootTableItem(Items.BOW))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_CROSSBOW,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup))
						.add(LootItem.lootTableItem(Items.CROSSBOW))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_SWORD,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(4))
						.add(LootItem.lootTableItem(Items.DIAMOND_SWORD))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_SWORD_GOLD,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(LootItem.lootTableItem(Items.GOLDEN_SWORD))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_TRIDENT,
				LootTable.lootTable().withPool(LootPool.lootPool()
						// no useful enchantments for mob usage
						.add(LootItem.lootTableItem(Items.TRIDENT))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_BY_PROFESSION,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_AXE))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
										.when(LootItemRandomChanceCondition.randomChance(0.3f)))
								.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity().nbt(
												new NbtPredicate(getProfessionNbt(VillagerProfession.BUTCHER))))))
						.add(LootItem.lootTableItem(Items.IRON_HOE)
								.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity().nbt(
												new NbtPredicate(getProfessionNbt(VillagerProfession.FARMER))))))
						.add(LootItem.lootTableItem(Items.FISHING_ROD)
								.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity().nbt(
												new NbtPredicate(getProfessionNbt(VillagerProfession.FISHERMAN))))))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE)
								.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity().nbt(
												new NbtPredicate(getProfessionNbt(VillagerProfession.TOOLSMITH))))))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
										.when(LootItemRandomChanceCondition.randomChance(0.3f)))
								.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
										EntityPredicate.Builder.entity().nbt(
												new NbtPredicate(getProfessionNbt(VillagerProfession.WEAPONSMITH))))))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_FOR_PIGLIN,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD_GOLD)))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_CROSSBOW)))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_WEAPON_FOR_WITHER_SKELETON,
				LootTable.lootTable().withPool(LootPool.lootPool().setRolls(UniformGenerator.between(-1, 1))
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup))
						.add(LootItem.lootTableItem(Items.STONE_SWORD))
						.add(LootItem.lootTableItem(Items.BOW))
				)
		);
	}

	private CompoundTag getProfessionNbt(ResourceKey<VillagerProfession> profession) {
		var villagerDataTag = new CompoundTag();
		villagerDataTag.putString("profession", profession.identifier().toString());
		var tag = new CompoundTag();
		tag.put("VillagerData", villagerDataTag);
		return tag;
	}

	private void defineAncientCityEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimWardQuartz = trimFactory.apply(TrimPatterns.WARD, TrimMaterials.QUARTZ);
		ArmorTrim trimSilenceCopper = trimFactory.apply(TrimPatterns.SILENCE, TrimMaterials.COPPER);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WARD_IRON,
				randomizedSetFactory.apply(trimWardQuartz, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WARD_DIAMOND,
				randomizedSetFactory.apply(trimWardQuartz, armorItems.get(ArmorMaterials.DIAMOND)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SILENCE_GOLD,
				randomizedSetFactory.apply(trimSilenceCopper, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SILENCE_DIAMOND,
				randomizedSetFactory.apply(trimSilenceCopper, armorItems.get(ArmorMaterials.DIAMOND)));

		PotionContents darknessContents = getPotionContents(MobEffects.DARKNESS, 200);
		tables.put(BotaniaLootTables.LOONIUM_ARMOR_ANCIENT_CITY,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WARD_IRON)).setWeight(11))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WARD_DIAMOND)).setWeight(5))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SILENCE_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SILENCE_DIAMOND)).setWeight(1))
				).withPool(LootPool.lootPool()
						// Note: Slowness from Strays stacks with tipped arrow effects, so just checking for bow here
						.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment()
										.mainhand(ItemPredicate.Builder.item().of(BuiltInRegistries.ITEM, Items.BOW)).build())))
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.TIPPED_ARROW).apply(SetComponentsFunction.setComponent(DataComponents.POTION_CONTENTS, darknessContents)))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_ANCIENT_CITY,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_ANCIENT_CITY))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_ANCIENT_CITY,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_ANCIENT_CITY))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_ANCIENT_CITY,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_ANCIENT_CITY))))
		);
	}

	private void defineBastionRemnantEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimSnoutGold = trimFactory.apply(TrimPatterns.SNOUT, TrimMaterials.GOLD);
		ArmorTrim trimSnoutNetherite = trimFactory.apply(TrimPatterns.SNOUT, TrimMaterials.NETHERITE);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SNOUT_GOLD,
				randomizedSetFactory.apply(trimSnoutNetherite, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SNOUT_NETHERITE,
				randomizedSetFactory.apply(trimSnoutGold, armorItems.get(ArmorMaterials.NETHERITE)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_BASTION_REMNANT,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SNOUT_GOLD)).setWeight(4))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SNOUT_NETHERITE)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_PIGLIN_BASTION_REMNANT,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_FOR_PIGLIN))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_BASTION_REMNANT))))
		);
	}

	private void defineDesertPyramidEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimDuneRedstone = trimFactory.apply(TrimPatterns.DUNE, TrimMaterials.REDSTONE);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_DUNE_IRON,
				randomizedSetFactory.apply(trimDuneRedstone, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_DUNE_GOLD,
				randomizedSetFactory.apply(trimDuneRedstone, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_DUNE_DIAMOND,
				randomizedSetFactory.apply(trimDuneRedstone, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_DESERT_PYRAMID,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_DUNE_IRON)).setWeight(5))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_DUNE_GOLD)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_DUNE_DIAMOND)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_DESERT_PYRAMID,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_DESERT_PYRAMID))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_DESERT_PYRAMID,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_DESERT_PYRAMID))))
		);
	}

	private void defineEndCityEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimSpireAmethyst = trimFactory.apply(TrimPatterns.SPIRE, TrimMaterials.AMETHYST);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_IRON,
				randomizedSetFactory.apply(trimSpireAmethyst, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_GOLD,
				randomizedSetFactory.apply(trimSpireAmethyst, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_DIAMOND,
				randomizedSetFactory.apply(trimSpireAmethyst, armorItems.get(ArmorMaterials.DIAMOND)));

		PotionContents levitationContents = getPotionContents(MobEffects.LEVITATION, 200);
		tables.put(BotaniaLootTables.LOONIUM_ARMOR_END_CITY,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
								.when(LootItemRandomChanceCondition.randomChance(0.3f)))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_IRON)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_GOLD)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SPIRE_DIAMOND)).setWeight(2))
				).withPool(LootPool.lootPool()
						.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity()
										.entityType(EntityTypePredicate.of(this.registryLookup.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.SKELETON))))
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.TIPPED_ARROW)
								.apply(SetComponentsFunction.setComponent(DataComponents.POTION_CONTENTS, levitationContents)))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_END_CITY,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_END_CITY))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_END_CITY,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_END_CITY))))
		);
	}

	private static PotionContents getPotionContents(Holder<MobEffect> mobEffect, int duration) {
		// [VanillaCopy] based on PotionContents::setCustomEffects
		return new PotionContents(Optional.empty(), Optional.of(mobEffect.value().getColor()),
				List.of(new MobEffectInstance(mobEffect, duration)), Optional.empty());
	}

	private void defineFortressEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimRibIron = trimFactory.apply(TrimPatterns.RIB, TrimMaterials.IRON);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_RIB_IRON,
				randomizedSetFactory.apply(trimRibIron, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_RIB_GOLD,
				randomizedSetFactory.apply(trimRibIron, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_RIB_DIAMOND,
				randomizedSetFactory.apply(trimRibIron, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_FORTRESS,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_RIB_IRON)).setWeight(7))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_RIB_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_RIB_DIAMOND)).setWeight(2))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_FORTRESS,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_FOR_WITHER_SKELETON))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_FORTRESS))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_FORTRESS,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD_GOLD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_FORTRESS))))
		);
	}

	private void defineJungleTempleEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimWildEmerald = trimFactory.apply(TrimPatterns.WILD, TrimMaterials.EMERALD);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WILD_CHAIN,
				randomizedSetFactory.apply(trimWildEmerald, armorItems.get(ArmorMaterials.CHAINMAIL)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WILD_GOLD,
				randomizedSetFactory.apply(trimWildEmerald, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WILD_DIAMOND,
				randomizedSetFactory.apply(trimWildEmerald, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_JUNGLE_TEMPLE,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WILD_CHAIN)).setWeight(4))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WILD_GOLD)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WILD_DIAMOND)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_JUNGLE_TEMPLE,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_JUNGLE_TEMPLE))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_JUNGLE_TEMPLE,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_JUNGLE_TEMPLE))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_JUNGLE_TEMPLE,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_JUNGLE_TEMPLE))))
		);
	}

	private void defineOceanMonumentEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory,
			TriFunction<ArmorTrim, Integer, Item[], LootTable.Builder> randomizedDyedSetFactory) {

		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_TIDE_LEATHER, randomizedDyedSetFactory.apply(
				trimFactory.apply(TrimPatterns.TIDE, TrimMaterials.COPPER), COLOR_TIDE_LEATHER, armorItems.get(ArmorMaterials.LEATHER)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_TIDE_GOLD, randomizedSetFactory.apply(
				trimFactory.apply(TrimPatterns.TIDE, TrimMaterials.DIAMOND), armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_TIDE_DIAMOND, randomizedSetFactory.apply(
				trimFactory.apply(TrimPatterns.TIDE, TrimMaterials.GOLD), armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_MONUMENT,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_TIDE_LEATHER)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_TIDE_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_TIDE_DIAMOND)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_MONUMENT,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_MONUMENT))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_MONUMENT,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_MONUMENT))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_MONUMENT,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_MONUMENT))))
		);
	}

	private void definePillagerOutpostEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimSentryEmerald = trimFactory.apply(TrimPatterns.SENTRY, TrimMaterials.EMERALD);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_CHAIN,
				randomizedSetFactory.apply(trimSentryEmerald, armorItems.get(ArmorMaterials.CHAINMAIL)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_IRON,
				randomizedSetFactory.apply(trimSentryEmerald, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_DIAMOND,
				randomizedSetFactory.apply(trimSentryEmerald, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_OUTPOST,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_CHAIN)).setWeight(5))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_IRON)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SENTRY_DIAMOND)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_OUTPOST,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_OUTPOST))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_OUTPOST,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_OUTPOST))))
		);
	}

	private void defineRuinedPortalEquipmentTables(Map<Identifier, LootTable.Builder> tables) {

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_PORTAL,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(
								LootItem.lootTableItem(Items.GOLDEN_HELMET)).setRolls(UniformGenerator.between(0, 1)))
						.withPool(LootPool.lootPool().add(
								LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE)).setRolls(UniformGenerator.between(0, 1)))
						.withPool(LootPool.lootPool().add(
								LootItem.lootTableItem(Items.GOLDEN_LEGGINGS)).setRolls(UniformGenerator.between(0, 1)))
						.withPool(LootPool.lootPool().add(
								LootItem.lootTableItem(Items.GOLDEN_BOOTS)).setRolls(UniformGenerator.between(0, 1)))
		);

		tables.put(BotaniaLootTables.LOONIUM_DROWNED_PORTAL,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_PORTAL))))
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
		);
		tables.put(BotaniaLootTables.LOONIUM_PIGLIN_PORTAL,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_PORTAL))))
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_FOR_PIGLIN))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_PORTAL,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_PORTAL))))
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_PORTAL,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_PORTAL))))
						.withPool(LootPool.lootPool().add(
								NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD_GOLD))))
		);
	}

	private void defineShipwreckEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimCoastEmerald = trimFactory.apply(TrimPatterns.COAST, TrimMaterials.EMERALD);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COAST_CHAIN,
				randomizedSetFactory.apply(trimCoastEmerald, armorItems.get(ArmorMaterials.CHAINMAIL)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COAST_IRON,
				randomizedSetFactory.apply(trimCoastEmerald, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COAST_DIAMOND,
				randomizedSetFactory.apply(trimCoastEmerald, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_SHIPWRECK,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COAST_CHAIN)).setWeight(4))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COAST_IRON)).setWeight(4))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COAST_DIAMOND)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_SHIPWRECK,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_SHIPWRECK))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_SHIPWRECK,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_SHIPWRECK))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_SHIPWRECK,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_SHIPWRECK))))
		);
	}

	private void defineStrongholdEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimEyeRedstone = trimFactory.apply(TrimPatterns.EYE, TrimMaterials.REDSTONE);
		ArmorTrim trimEyeLapis = trimFactory.apply(TrimPatterns.EYE, TrimMaterials.LAPIS);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_EYE_IRON,
				randomizedSetFactory.apply(trimEyeLapis, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_EYE_GOLD,
				randomizedSetFactory.apply(trimEyeRedstone, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_EYE_DIAMOND,
				randomizedSetFactory.apply(trimEyeLapis, armorItems.get(ArmorMaterials.DIAMOND)));

		// Enderman cosplay
		var endermanTrim = trimFactory.apply(TrimPatterns.EYE, TrimMaterials.AMETHYST);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_ENDERMAN, LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_HELMET)
						.apply(addTrimToComponent(endermanTrim))
						.apply(addDyedColorToComponent(COLOR_ENDERMAN_BODY))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE)
						.apply(addDyedColorToComponent(COLOR_ENDERMAN_BODY))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_LEGGINGS)
						.apply(addDyedColorToComponent(COLOR_ENDERMAN_BODY))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_BOOTS)
						.apply(addDyedColorToComponent(COLOR_ENDERMAN_BODY))))
		);

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_STRONGHOLD,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_EYE_IRON)).setWeight(5))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_EYE_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_EYE_DIAMOND)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_ENDERMAN)).setWeight(1))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_STRONGHOLD,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_STRONGHOLD))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_STRONGHOLD,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_STRONGHOLD))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_STRONGHOLD,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_STRONGHOLD))))
		);
	}

	private void defineTrailRuinsEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			Map<ArmorMaterial, Item[]> armorItems,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			BiFunction<ArmorTrim, Item[], LootTable.Builder> randomizedSetFactory) {

		ArmorTrim trimHostEmerald = trimFactory.apply(TrimPatterns.HOST, TrimMaterials.EMERALD);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_HOST_CHAIN,
				randomizedSetFactory.apply(trimHostEmerald, armorItems.get(ArmorMaterials.CHAINMAIL)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_HOST_IRON,
				randomizedSetFactory.apply(trimHostEmerald, armorItems.get(ArmorMaterials.IRON)));

		ArmorTrim trimRaiserAmethyst = trimFactory.apply(TrimPatterns.RAISER, TrimMaterials.AMETHYST);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_RAISER_IRON,
				randomizedSetFactory.apply(trimRaiserAmethyst, armorItems.get(ArmorMaterials.IRON)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_RAISER_GOLD,
				randomizedSetFactory.apply(trimRaiserAmethyst, armorItems.get(ArmorMaterials.GOLD)));

		ArmorTrim trimShaperLapis = trimFactory.apply(TrimPatterns.SHAPER, TrimMaterials.LAPIS);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SHAPER_GOLD,
				randomizedSetFactory.apply(trimShaperLapis, armorItems.get(ArmorMaterials.GOLD)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_SHAPER_DIAMOND,
				randomizedSetFactory.apply(trimShaperLapis, armorItems.get(ArmorMaterials.DIAMOND)));

		ArmorTrim trimWayfinderRedstone = trimFactory.apply(TrimPatterns.WAYFINDER, TrimMaterials.REDSTONE);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WAYFINDER_CHAIN,
				randomizedSetFactory.apply(trimWayfinderRedstone, armorItems.get(ArmorMaterials.CHAINMAIL)));
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_WAYFINDER_DIAMOND,
				randomizedSetFactory.apply(trimWayfinderRedstone, armorItems.get(ArmorMaterials.DIAMOND)));

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_TRAIL_RUINS,
				LootTable.lootTable().withPool(LootPool.lootPool()
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_HOST_CHAIN)).setWeight(7))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WAYFINDER_CHAIN)).setWeight(7))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_RAISER_IRON)).setWeight(8))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_HOST_IRON)).setWeight(8))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_RAISER_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SHAPER_GOLD)).setWeight(3))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_SHAPER_DIAMOND)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_WAYFINDER_DIAMOND)).setWeight(2))
				)
		);
		tables.put(BotaniaLootTables.LOONIUM_DROWNED_TRAIL_RUINS,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_TRIDENT))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_TRAIL_RUINS))))
		);
		tables.put(BotaniaLootTables.LOONIUM_SKELETON_TRAIL_RUINS,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_BOW))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_TRAIL_RUINS))))
		);
		tables.put(BotaniaLootTables.LOONIUM_ZOMBIE_TRAIL_RUINS,
				LootTable.lootTable()
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_WEAPON_SWORD))))
						.withPool(LootPool.lootPool().add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMOR_TRAIL_RUINS))))
		);
	}

	private void defineWoodlandMansionEquipmentTables(Map<Identifier, LootTable.Builder> tables,
			BiFunction<ResourceKey<TrimPattern>, ResourceKey<TrimMaterial>, ArmorTrim> trimFactory,
			TriFunction<ArmorTrim, Integer, Item[], LootTable.Builder> fixedDyedSetFactory) {

		// Evoker cosplay, with higher likelihood of holding a totem
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_EVOKER, fixedDyedSetFactory.apply(
				trimFactory.apply(TrimPatterns.VEX, TrimMaterials.GOLD), COLOR_EVOKER_COAT,
				new Item[] { Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS })
				.withPool(LootPool.lootPool()
						.when(LootItemRandomChanceCondition.randomChance(0.2f))
						.add(LootItem.lootTableItem(Items.TOTEM_OF_UNDYING))
				)
		);

		// Vindicator cosplay, usually including axe (even for ranged mobs)
		var vindicatorTrim = trimFactory.apply(TrimPatterns.VEX, TrimMaterials.NETHERITE);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_VINDICATOR, LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE)
						.apply(addTrimToComponent(vindicatorTrim))
						.apply(addDyedColorToComponent(COLOR_VINDICATOR_JACKET))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_LEGGINGS)
						.apply(addDyedColorToComponent(COLOR_VINDICATOR_LEGWEAR))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.LEATHER_BOOTS)
						.apply(addDyedColorToComponent(COLOR_VINDICATOR_BOOTS))))
				.withPool(LootPool.lootPool()
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.IRON_AXE)
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
										.when(LootItemRandomChanceCondition.randomChance(0.3f)))))
		);

		// Illusioner cosplay, including bow and blindness arrows, even for mobs that don't know how to use bows
		PotionContents blindnessContents = getPotionContents(MobEffects.BLINDNESS, 100);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_ILLUSIONER, fixedDyedSetFactory.apply(
				trimFactory.apply(TrimPatterns.VEX, TrimMaterials.LAPIS), COLOR_ILLUSIONER_COAT,
				new Item[] { Items.LEATHER_HELMET, Items.LEATHER_CHESTPLATE, Items.LEATHER_LEGGINGS })
				.withPool(LootPool.lootPool()
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.BOW)
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
										.when(LootItemRandomChanceCondition.randomChance(0.3f)))))
				.withPool(LootPool.lootPool()
						.when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
								EntityPredicate.Builder.entity()
										.entityType(EntityTypePredicate.of(this.registryLookup.lookupOrThrow(Registries.ENTITY_TYPE), EntityType.SKELETON))))
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.TIPPED_ARROW)
								.apply(SetComponentsFunction.setComponent(DataComponents.POTION_CONTENTS, blindnessContents))))
		);

		// Vex cosplay, including sword (even for ranged mobs)
		var vexTrim = trimFactory.apply(TrimPatterns.VEX, TrimMaterials.AMETHYST);
		tables.put(BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_VEX, LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.DIAMOND_HELMET)
						.apply(addTrimToComponent(vexTrim))))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)))
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)))
				.withPool(LootPool.lootPool()
						.when(LootItemRandomChanceCondition.randomChance(0.9f))
						.add(LootItem.lootTableItem(Items.IRON_SWORD)
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registryLookup)
										.when(LootItemRandomChanceCondition.randomChance(0.3f)))))
		);

		tables.put(BotaniaLootTables.LOONIUM_ARMOR_MANSION,
				LootTable.lootTable().withPool(LootPool.lootPool()
						// it's cosplays all the way down
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_EVOKER)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_VINDICATOR)).setWeight(2))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_ILLUSIONER)).setWeight(1))
						.add(NestedLootTable.lootTableReference(ResourceKey.create(Registries.LOOT_TABLE, BotaniaLootTables.LOONIUM_ARMORSET_COSTUME_VEX)).setWeight(45)
								.when(AnyOfCondition.anyOf(
										// focus Vex cosplay on baby mobs, reduce chance for everyone else
										LootItemRandomChanceCondition.randomChance(0.005f),
										LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS,
												EntityPredicate.Builder.entity()
														.flags(EntityFlagsPredicate.Builder.flags().setIsBaby(true)))
								)))
				).withPool(LootPool.lootPool()
						.when(LootItemRandomChanceCondition.randomChance(0.05f))
						.add(LootItem.lootTableItem(Items.TOTEM_OF_UNDYING))
				)
		);
	}

	private static ArmorTrim getTrim(HolderLookup.RegistryLookup<TrimPattern> patternRegistry,
			HolderLookup.RegistryLookup<TrimMaterial> materialRegistry,
			ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material) {
		Holder.Reference<TrimPattern> tidePattern = patternRegistry.get(pattern).orElseThrow();
		Holder.Reference<TrimMaterial> goldMaterial = materialRegistry.get(material).orElseThrow();
		return new ArmorTrim(goldMaterial, tidePattern);
	}

	private static LootItemFunction.Builder addTrimToComponent(ArmorTrim trim) {
		// [VanillaCopy] from ArmorTrim::setTrim, because no access to item components here
		return SetComponentsFunction.setComponent(DataComponents.TRIM, trim);
	}

	private static LootItemFunction.Builder addDyedColorToComponent(int color) {
		// [VanillaCopy] implementation based on DyeableLeatherItem::setColor
		return SetComponentsFunction.setComponent(DataComponents.DYED_COLOR, new DyedItemColor(color));
	}

	private LootTable.Builder createArmorSet(List<LootItemFunction.Builder> modifiers, boolean randomized, Item... armorItems) {
		LootTable.Builder lootTable = LootTable.lootTable();
		for (Item armorItem : armorItems) {
			var entry = LootItem.lootTableItem(armorItem);
			for (LootItemFunction.Builder modifier : modifiers) {
				entry.apply(modifier);
			}
			lootTable.withPool(LootPool.lootPool()
					.setRolls(randomized ? UniformGenerator.between(0, 1) : ConstantValue.exactly(1))
					.add(entry));
		}
		return lootTable;
	}

	@NotNull
	@Override
	public String getName() {
		return "Equipment tables for Loonium-spawned mobs";
	}
}
