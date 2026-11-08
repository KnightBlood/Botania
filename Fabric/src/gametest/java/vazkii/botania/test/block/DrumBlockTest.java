package vazkii.botania.test.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.BotaniaFlowerBlocks;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.mixin.MushroomCowAccessor;
import vazkii.botania.test.TestingUtil;

import java.util.List;

public class DrumBlockTest {
	private static final String TEMPLATE = "botania:block/drum_gathering";

	private static final BlockPos POSITION_BUTTON = new BlockPos(10, 10, 9);
	private static final BlockPos POSITION_SPREADER = new BlockPos(10, 10, 10);
	private static final BlockPos POSITION_DRUM = new BlockPos(10, 11, 10);
	private static final BlockPos POSITION_BERGAMUTE = new BlockPos(11, 2, 11);
	private static final BlockPos POSITION_MOB = new BlockPos(10, 2, 10);
	private static final Vec3 VECTOR_MOB = POSITION_MOB.getCenter();

	private static <T extends Mob> T setup(GameTestHelper helper, EntityType<T> entityType, @Nullable Item item) {
		var player = TestingUtil.makeMockPlayer(helper);
		var spreader = TestingUtil.assertBlockEntity(helper, POSITION_SPREADER, BotaniaBlockEntities.SPREADER);
		TestingUtil.assertThat(spreader.bindTo(player, new ItemStack(BotaniaItems.twigWand),
				helper.absolutePos(POSITION_DRUM), Direction.UP),
				() -> "Failed to bind spreader");
		if (item != null) {
			helper.spawnItem(item, (float) VECTOR_MOB.x(), (float) VECTOR_MOB.y(), (float) VECTOR_MOB.z());
		}
		return helper.spawn(entityType, VECTOR_MOB);
	}

	private static <T extends Mob> void testMilkingAdultAnimal(GameTestHelper helper, EntityType<T> entityType, Item inputItem, Item outputItem) {
		setup(helper, entityType, inputItem);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenWaitUntil(() -> helper.assertItemEntityPresent(outputItem, POSITION_MOB, 1.0))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testMilkingGoat(GameTestHelper helper) {
		testMilkingAdultAnimal(helper, EntityType.GOAT, Items.BUCKET, Items.MILK_BUCKET);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testMilkingCow(GameTestHelper helper) {
		testMilkingAdultAnimal(helper, EntityType.COW, Items.BUCKET, Items.MILK_BUCKET);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testMilkingMooshroom(GameTestHelper helper) {
		testMilkingAdultAnimal(helper, EntityType.MOOSHROOM, Items.BUCKET, Items.MILK_BUCKET);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testMilkingMooshroomSoup(GameTestHelper helper) {
		testMilkingAdultAnimal(helper, EntityType.MOOSHROOM, Items.BOWL, Items.MUSHROOM_STEW);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testMilkingBrownMooshroomSuspiciousStew(GameTestHelper helper) {
		var cow = setup(helper, EntityType.MOOSHROOM, Items.BOWL);
		var cowAccessor = (MushroomCowAccessor) cow;
		// 26.1: MushroomCow.MushroomType -> Variant, setVariant is private (invoker),
		// and the two effect fields were replaced by a SuspiciousStewEffects component.
		cowAccessor.botania_setVariant(MushroomCow.Variant.BROWN);
		cowAccessor.setStewEffects(new SuspiciousStewEffects(List.of(
				new SuspiciousStewEffects.Entry(MobEffects.BLINDNESS, 15))));
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenWaitUntil(() -> helper.assertItemEntityPresent(Items.SUSPICIOUS_STEW, POSITION_MOB, 1.0))
				.thenExecute(() -> {
					final var item = helper.getEntities(EntityType.ITEM, POSITION_MOB, 1.0).stream().findFirst();
					helper.assertTrue(item.isPresent() && item.get().getItem().is(Items.SUSPICIOUS_STEW), "Item not found or not suspicious stew");
					final var stewEffects = item.get().getItem().get(DataComponents.SUSPICIOUS_STEW_EFFECTS);
					helper.assertTrue(stewEffects != null, "Missing suspicious stew effects component");
					helper.assertTrue(stewEffects.effects().size() == 1, "Exactly one effect expected");
					final var effectEntry = stewEffects.effects().get(0);
					final var effectDuration = effectEntry.duration();
					// 26.1: MobEffects fields are Holder<MobEffect>; compare holders, not values
					// (value() == holder would always be false and silently fail the case).
					helper.assertTrue(effectEntry.effect() == MobEffects.BLINDNESS && effectDuration == 15,
							"Unexpected effect type or duration: got " + effectEntry.effect()
									+ " duration " + effectDuration
									+ " (expected " + MobEffects.BLINDNESS + " duration 15)");
				})
				.thenSucceed();
	}

	private static <T extends Mob> void testMilkingBabyAnimal(GameTestHelper helper, EntityType<T> entityType, Item item) {
		var baby = setup(helper, entityType, item);
		baby.setBaby(true);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				// ensure the empty container item is still there:
				.thenExecuteAfter(20, () -> helper.assertItemEntityPresent(item, POSITION_MOB, 1.0))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testMilkingBabyGoat(GameTestHelper helper) {
		testMilkingBabyAnimal(helper, EntityType.GOAT, Items.BUCKET);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testMilkingBabyMooshroom(GameTestHelper helper) {
		testMilkingBabyAnimal(helper, EntityType.MOOSHROOM, Items.BOWL);
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testMilkingDeadAnimal(GameTestHelper helper) {
		var cow = setup(helper, EntityType.COW, Items.BUCKET);
		helper.startSequence()
				// mobs play 20 ticks of dying animation when killed, so the cow is still there when the drum goes off
				.thenExecute(() -> cow.kill(helper.getLevel()))
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenExecuteAfter(20, () -> helper.assertItemEntityPresent(Items.BUCKET, POSITION_MOB, 1.0))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testMilkingNearBergamute(GameTestHelper helper) {
		setup(helper, EntityType.COW, Items.BUCKET);
		// Bergamute should protect from drum interactions
		helper.setBlock(POSITION_BERGAMUTE, BotaniaFlowerBlocks.bergamuteFloating);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenExecuteAfter(20, () -> helper.assertItemEntityPresent(Items.BUCKET, POSITION_MOB, 1.0))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testShearingSheep(GameTestHelper helper) {
		var sheep = setup(helper, EntityType.SHEEP, null);
		sheep.setColor(DyeColor.LIME);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenWaitUntil(() -> helper.assertItemEntityPresent(Items.LIME_WOOL, POSITION_MOB, 1.0))
				.thenExecute(() -> helper.assertTrue(sheep.isAlive() && sheep.isSheared(), "Sheep should be sheared"))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testShearingBabySheep(GameTestHelper helper) {
		var sheep = setup(helper, EntityType.SHEEP, null);
		sheep.setBaby(true);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenExecuteAfter(20, () -> helper.assertTrue(sheep.isAlive() && !sheep.isSheared(), "Baby sheep should not be sheared"))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testShearingSnowGolem(GameTestHelper helper) {
		var golem = setup(helper, EntityType.SNOW_GOLEM, null);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenWaitUntil(() -> helper.assertItemEntityPresent(Items.CARVED_PUMPKIN, POSITION_MOB, 1.0))
				.thenExecute(() -> helper.assertTrue(golem.isAlive() && !golem.hasPumpkin(), "Snow golem should not wear a pumpkin"))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 20)
	public void testShearingPumpkinlessSnowGolem(GameTestHelper helper) {
		var golem = setup(helper, EntityType.SNOW_GOLEM, null);
		golem.setPumpkin(false);
		helper.startSequence()
				.thenExecute(() -> helper.pressButton(POSITION_BUTTON))
				.thenExecuteAfter(20, () -> helper.assertTrue(golem.isAlive() && !golem.hasPumpkin(), "Snow golem should not wear a pumpkin"))
				.thenExecute(() -> helper.assertItemEntityNotPresent(Items.CARVED_PUMPKIN, POSITION_MOB, 1.0))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE, maxTicks = 25)
	public void testShearingMooshroom(GameTestHelper helper) {
		setup(helper, EntityType.MOOSHROOM, null);
		helper.startSequence()
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_BUTTON))
				.thenExecuteAfter(20, () -> helper.assertEntityNotPresent(EntityType.COW))
				.thenSucceed();
	}
}
