/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import vazkii.botania.common.lib.BotaniaTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaRegistries;
import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.configdata.ConfigDataManager;
import vazkii.botania.api.corporea.CorporeaNodeDetector;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.client.fx.SparkleParticleData;
import vazkii.botania.common.block.flower.functional.SolegnoliaBlockEntity;
import vazkii.botania.common.config.ConfigDataManagerImpl;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.handler.EquipmentHandler;
import vazkii.botania.common.handler.ManaNetworkHandler;
import vazkii.botania.common.integration.corporea.CorporeaNodeDetectors;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.relic.RingOfLokiItem;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class BotaniaAPIImpl implements BotaniaAPI {

	private ConfigDataManager configDataManager = new ConfigDataManagerImpl();

	private static final ArmorMaterial MANASTEEL = new ArmorMaterial(
		16,
		Map.of(
			ArmorType.BOOTS, 2,
			ArmorType.LEGGINGS, 5,
			ArmorType.CHESTPLATE, 6,
			ArmorType.HELMET, 2
		),
		18,
		Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse("botania:equip_manasteel"))),
		0.0F,
		0.0F,
		ItemTags.WOODEN_TOOL_MATERIALS,
		ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.withDefaultNamespace("leather"))
	);

	private static final ArmorMaterial MANAWEAVE = new ArmorMaterial(
		5,
		Map.of(
			ArmorType.BOOTS, 1,
			ArmorType.LEGGINGS, 2,
			ArmorType.CHESTPLATE, 3,
			ArmorType.HELMET, 1
		),
		18,
		Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse("botania:equip_manaweave"))),
		0.0F,
		0.0F,
		ItemTags.WOODEN_TOOL_MATERIALS,
		ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.withDefaultNamespace("leather"))
	);

	private static final ArmorMaterial ELEMENTIUM = new ArmorMaterial(
		18,
		Map.of(
			ArmorType.BOOTS, 2,
			ArmorType.LEGGINGS, 5,
			ArmorType.CHESTPLATE, 6,
			ArmorType.HELMET, 2
		),
		18,
		Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse("botania:equip_elementium"))),
		0.0F,
		0.0F,
		ItemTags.WOODEN_TOOL_MATERIALS,
		ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.withDefaultNamespace("leather"))
	);

	private static final ArmorMaterial TERRASTEEL = new ArmorMaterial(
		34,
		Map.of(
			ArmorType.BOOTS, 3,
			ArmorType.LEGGINGS, 6,
			ArmorType.CHESTPLATE, 8,
			ArmorType.HELMET, 3
		),
		26,
		Holder.direct(SoundEvent.createVariableRangeEvent(Identifier.parse("botania:equip_terrasteel"))),
		3.0F,
		0.0F,
		ItemTags.WOODEN_TOOL_MATERIALS,
		ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.withDefaultNamespace("leather"))
	);

	@Override
	public int apiVersion() {
		return 2;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public Registry<Brew> getBrewRegistry() {
		return (Registry<Brew>) BuiltInRegistries.REGISTRY.getValue(BotaniaRegistries.BREWS.identifier());
	}

	@Override
	public ArmorMaterial getManasteelArmorMaterial() {
		return MANASTEEL;
	}

	@Override
	public ArmorMaterial getElementiumArmorMaterial() {
		return ELEMENTIUM;
	}

	@Override
	public ArmorMaterial getManaweaveArmorMaterial() {
		return MANAWEAVE;
	}

	@Override
	public ArmorMaterial getTerrasteelArmorMaterial() {
		return TERRASTEEL;
	}

	@Override
	public ToolMaterial getManasteelItemTier() {
				return new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 300, 6.2F, 2, 20, BotaniaTags.Items.INGOTS_MANASTEEL);
	}

	@Override
	public ToolMaterial getElementiumItemTier() {
		return new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 720, 6.2F, 2, 20, BotaniaTags.Items.INGOTS_ELEMENTIUM);
	}

	@Override
	public ToolMaterial getTerrasteelItemTier() {
		return new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2300, 9, 4, 26, BotaniaTags.Items.INGOTS_TERRASTEEL);
	}

	@Override
	public ManaNetwork getManaNetworkInstance() {
		return ManaNetworkHandler.instance;
	}

	@Override
	public Container getAccessoriesInventory(Player player) {
		return EquipmentHandler.getAllWorn(player);
	}

	@Override
	public void breakOnAllCursors(Player player, ItemStack stack, BlockPos pos, Direction side) {
		RingOfLokiItem.breakOnAllCursors(player, stack, pos, side);
	}

	@Override
	public boolean hasSolegnoliaAround(Entity e) {
		return SolegnoliaBlockEntity.hasSolegnoliaAround(e);
	}

	@Override
	public void sparkleFX(Level world, double x, double y, double z, float r, float g, float b, float size, int m) {
		SparkleParticleData data = SparkleParticleData.sparkle(size, r, g, b, m);
		world.addParticle(data, x, y, z, 0, 0, 0);
	}

	private final Map<Identifier, Function<DyeColor, Block>> paintableBlocks = new ConcurrentHashMap<>();

	@Override
	public Map<Identifier, Function<DyeColor, Block>> getPaintableBlocks() {
		return Collections.unmodifiableMap(paintableBlocks);
	}

	@Override
	public void registerPaintableBlock(Identifier block, Function<DyeColor, Block> transformer) {
		paintableBlocks.put(block, transformer);
	}

	@Override
	public void registerCorporeaNodeDetector(CorporeaNodeDetector detector) {
		CorporeaNodeDetectors.register(detector);
	}

	@Override
	public ConfigDataManager getConfigData() {
		return configDataManager;
	}

	@Override
	public void setConfigData(ConfigDataManager configDataManager) {
		this.configDataManager = configDataManager;
	}
}
