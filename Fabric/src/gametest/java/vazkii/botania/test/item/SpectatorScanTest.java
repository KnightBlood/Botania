package vazkii.botania.test.item;

import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.equipment.bauble.SpectatorItem;
import vazkii.botania.mixin.AbstractHorseAccessor;
import vazkii.botania.test.TestingUtil;

import java.util.Arrays;
import java.util.function.BiConsumer;

// 26.1 migration note (explicit downgrade, do not remove):
// MC 26.1 removed the old per-case batch grouping and the before/after-batch hooks
// in favour of the TestEnvironmentDefinition registry. The batch grouping on the cases
// below has been dropped; all cases now run on Fabric's default "minecraft:default"
// environment. No per-batch setup existed in this class, so the only behavioural
// difference is the loss of batch isolation/ordering.
public class SpectatorScanTest {
	private static final BlockPos POSITION_CHEST_NORMAL = new BlockPos(17, 16, 16);
	private static final BlockPos POSITION_CHEST_LOOT = new BlockPos(15, 16, 16);
	private static final BlockPos POSITION_CART_NORMAL = new BlockPos(11, 2, 6);
	private static final BlockPos POSITION_CART_LOOT = new BlockPos(9, 2, 6);
	private static final BlockPos POSITION_ITEM = new BlockPos(7, 3, 3);
	private static final BlockPos POSITION_VILLAGER = new BlockPos(4, 2, 3);
	private static final BlockPos POSITION_DONKEY = new BlockPos(19, 2, 6);
	private static final BlockPos POSITION_ALLAY = new BlockPos(19, 2, 16);

	@GameTest(structure = "botania:item/spectator_scan", maxTicks = 100)
	public void testSpectatorScanMainHand(GameTestHelper helper) {
		performTest(helper, (h, player) -> {
			player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
		});
	}

	@GameTest(structure = "botania:item/spectator_scan", maxTicks = 100)
	public void testSpectatorScanOffHand(GameTestHelper helper) {
		performTest(helper, (h, player) -> {
			player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_INGOT));
		});
	}

	@GameTest(structure = "botania:item/spectator_scan", maxTicks = 100)
	public void testSpectatorScanBothHands(GameTestHelper helper) {
		performTest(helper, (h, player) -> {
			player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_INGOT));
			player.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.IRON_INGOT));
		});
	}

	private static void performTest(GameTestHelper helper, BiConsumer<GameTestHelper, Player> additionalSetup) {
		helper.killAllEntities();

		// set up test entities
		var itemEntity = helper.spawnItem(Items.IRON_INGOT, POSITION_ITEM);
		var villager = helper.spawn(EntityType.VILLAGER, POSITION_VILLAGER);
		// 26.1: VillagerData now holds Holder<VillagerType>/Holder<VillagerProfession>,
		// and PLAINS/TOOLSMITH are ResourceKeys. Resolve them through the level's registries.
		var registries = helper.getLevel().registryAccess();
		villager.setVillagerData(villager.getVillagerData()
				.withType(registries, VillagerType.PLAINS)
				.withProfession(registries, VillagerProfession.TOOLSMITH)
				.withLevel(2));

		// set inventory content
		var regularChestCart = helper.spawn(EntityType.CHEST_MINECART, POSITION_CART_NORMAL);
		regularChestCart.setItem(2, new ItemStack(Items.COAL));
		regularChestCart.setItem(5, new ItemStack(Items.IRON_INGOT));

		var regularChest = TestingUtil.assertBlockEntity(helper, POSITION_CHEST_NORMAL, BlockEntityType.CHEST);
		regularChest.setItem(3, new ItemStack(Items.FLINT));
		regularChest.setItem(7, new ItemStack(Items.IRON_INGOT));

		var donkey = helper.spawnWithNoFreeWill(EntityType.DONKEY, POSITION_DONKEY);
		donkey.setTamed(true);
		donkey.setChest(true);
		((AbstractHorseAccessor) donkey).botania_createInventory();
		var donkeyInventory = ((AbstractHorseAccessor) donkey).getInventory();
		donkeyInventory.setItem(3, new ItemStack(Items.COAL));
		donkeyInventory.setItem(4, new ItemStack(Items.IRON_INGOT));

		var allay = helper.spawnWithNoFreeWill(EntityType.ALLAY, POSITION_ALLAY);
		allay.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT));

		// set loot tables
		var lootChestCart = helper.spawn(EntityType.CHEST_MINECART, POSITION_CART_LOOT);
		// 26.1: loot tables are ResourceKey<LootTable>, not Identifier.
		lootChestCart.setLootTable(BuiltInLootTables.ABANDONED_MINESHAFT, 1L);

		var lootChest = TestingUtil.assertBlockEntity(helper, POSITION_CHEST_LOOT, BlockEntityType.CHEST);
		lootChest.setLootTable(BuiltInLootTables.SIMPLE_DUNGEON);
		lootChest.setLootTableSeed(1L);

		// set up player
		var player = TestingUtil.makeMockPlayer(helper);
		// 26.1: Entity#moveTo(Vec3) is gone; setPos(Vec3) is the equivalent teleport.
		player.setPos(helper.absoluteVec(new Vec3(24, 24, 24)));
		additionalSetup.accept(helper, player);
		var spectatorStack = new ItemStack(BotaniaItems.itemFinder);

		// execute test
		((SpectatorItem) BotaniaItems.itemFinder).scanForItems(spectatorStack, player);

		// ensure loot content has not been rolled
		TestingUtil.assertThat(lootChestCart.getLootTable() != null, () -> "Chest loot was rolled");
		TestingUtil.assertThat(lootChest.getLootTable() != null,
				() -> "Chest loot was rolled");

		// check that exactly the relevant positions have been found
		long[] blocks = ItemNBTHelper.getLongArray(spectatorStack, SpectatorItem.TAG_BLOCK_POSITIONS);
		TestingUtil.assertEquals(blocks.length, 1, () -> "Expected 1 block hit, was " + blocks.length);
		BlockPos chestPos = BlockPos.of(blocks[0]);
		TestingUtil.assertEquals(helper.absolutePos(POSITION_CHEST_NORMAL), chestPos,
				() -> "Chest position " + helper.absolutePos(POSITION_CHEST_NORMAL) + " not in result, but found " + chestPos);

		int[] entities = ItemNBTHelper.getIntArray(spectatorStack, SpectatorItem.TAG_ENTITY_POSITIONS);
		TestingUtil.assertEquals(entities.length, 5, () -> "Expected 5 entity hits, but got " + entities.length);
		TestingUtil.assertThat(Arrays.stream(entities).anyMatch(id -> villager.getId() == id), () -> "Villager not in result");
		TestingUtil.assertThat(Arrays.stream(entities).anyMatch(id -> itemEntity.getId() == id), () -> "Item entity not in result");
		TestingUtil.assertThat(Arrays.stream(entities).anyMatch(id -> regularChestCart.getId() == id), () -> "Minecart not in result");
		TestingUtil.assertThat(Arrays.stream(entities).anyMatch(id -> donkey.getId() == id), () -> "Donkey not in result");
		TestingUtil.assertThat(Arrays.stream(entities).anyMatch(id -> allay.getId() == id), () -> "Allay not in result");

		helper.killAllEntities();
		helper.succeed();
	}
}
