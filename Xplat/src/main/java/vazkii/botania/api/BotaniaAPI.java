/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import vazkii.botania.api.brew.Brew;
import vazkii.botania.api.configdata.ConfigDataManager;
import vazkii.botania.api.corporea.CorporeaNodeDetector;
import vazkii.botania.api.internal.DummyManaNetwork;
import vazkii.botania.api.internal.ManaNetwork;
import vazkii.botania.common.helper.IngredientHelper;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public interface BotaniaAPI {
	String MODID = "botania";
	String GOG_MODID = "gardenofglass";
	Logger LOGGER = LoggerFactory.getLogger(MODID);

	BotaniaAPI INSTANCE = ServiceUtil.findService(BotaniaAPI.class, () -> new BotaniaAPI() {});

	static BotaniaAPI instance() {
		return INSTANCE;
	}

	/**
	 * @return A unique version number for this version of the API. When anything is added, this number will be
	 *         incremented
	 */
	default int apiVersion() {
		return 0;
	}

	/**
	 * Get the registry for brews.
	 * This should only be called after the registry is registered.
	 * In Forge, that is after NewRegistryEvent. In Fabric, that is after Botania's common initializer
	 * is loaded.
	 * Note that this registry is neither saved nor synced, and thus its integer ID's should not be relied upon.
	 */
	@Nullable
	default Registry<Brew> getBrewRegistry() {
		return null;
	}

	default Map<Identifier, Function<DyeColor, Block>> getPaintableBlocks() {
		return Collections.emptyMap();
	}

	default void registerPaintableBlock(Block block, Function<DyeColor, Block> transformer) {
		registerPaintableBlock(BuiltInRegistries.BLOCK.getKey(block), transformer);
	}

	/**
	 * Make Botania aware of how to transform between different colors of a block, for use in the paint lens.
	 * This method can be safely called during parallel mod initialization
	 * 
	 * @param blockId     The block ID
	 * @param transformer Function from color to a new block
	 */
	default void registerPaintableBlock(Identifier blockId, Function<DyeColor, Block> transformer) {

	}

	TagKey<Item> DUMMY_REPAIR_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, "dummy_repair_items"));

	TagKey<Block> DUMMY_INCORRECT_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MODID, "dummy_incorrect_blocks"));

	// API-side fallback for when no BotaniaAPI implementation has been injected yet.
	// It deliberately keeps the vanilla leather equipment asset so this stub can never
	// resolve to a missing Botania asset; the real materials use armorAsset(...) instead.
	ArmorMaterial DUMMY_ARMOR_MATERIAL = new ArmorMaterial(
		0,
		Map.of(),
		0,
		SoundEvents.ARMOR_EQUIP_LEATHER,
		0.0F,
		0.0F,
		DUMMY_REPAIR_ITEMS,
		ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.withDefaultNamespace("leather"))
	);

	ToolMaterial DUMMY_ITEM_TIER = new ToolMaterial(DUMMY_INCORRECT_BLOCKS, 0, 0.0F, 0.0F, 0, DUMMY_REPAIR_ITEMS);

	default ArmorMaterial getManasteelArmorMaterial() {
		return DUMMY_ARMOR_MATERIAL;
	}

	default ArmorMaterial getElementiumArmorMaterial() {
		return DUMMY_ARMOR_MATERIAL;
	}

	default ArmorMaterial getManaweaveArmorMaterial() {
		return DUMMY_ARMOR_MATERIAL;
	}

	default ArmorMaterial getTerrasteelArmorMaterial() {
		return DUMMY_ARMOR_MATERIAL;
	}

	default ToolMaterial getManasteelItemTier() {
		return DUMMY_ITEM_TIER;
	}

	default ToolMaterial getElementiumItemTier() {
		return DUMMY_ITEM_TIER;
	}

	default ToolMaterial getTerrasteelItemTier() {
		return DUMMY_ITEM_TIER;
	}

	default ManaNetwork getManaNetworkInstance() {
		return DummyManaNetwork.instance;
	}

	default Container getAccessoriesInventory(Player player) {
		return new SimpleContainer(0);
	}

	/**
	 * Break all the blocks the given player has selected with the loki ring.
	 * The item passed must implement {@link vazkii.botania.api.item.SequentialBreaker}.
	 */
	default void breakOnAllCursors(Player player, ItemStack stack, BlockPos pos, Direction side) {}

	default boolean hasSolegnoliaAround(Entity e) {
		return false;
	}

	default void sparkleFX(Level world, double x, double y, double z, float r, float g, float b, float size, int m) {}

	default void registerCorporeaNodeDetector(CorporeaNodeDetector detector) {

	}

	default ConfigDataManager getConfigData() {
		return null;
	}

	default void setConfigData(ConfigDataManager configDataManager) {

	}
}
