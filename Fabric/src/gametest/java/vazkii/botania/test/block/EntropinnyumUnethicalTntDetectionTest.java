package vazkii.botania.test.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.ForceRelayBlock;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.flower.generating.EntropinnyumBlockEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.test.TestingUtil;

public class EntropinnyumUnethicalTntDetectionTest {
	private static final String TEMPLATE_SLIME_DUPER_FORCE_LENS = "botania:block/entropinnyum_unethical_slime_duper_force_lens";
	private static final String TEMPLATE_SLIME_DUPER_FORCE_RELAY = "botania:block/entropinnyum_unethical_slime_duper_force_relay";
	private static final String TEMPLATE_ENTITYLESS_DUPER = "botania:block/entropinnyum_unethical_entityless_duper";
	private static final String TEMPLATE_LEGIT_TNT = "botania:block/entropinnyum_ethical_tnt_block";

	private static final BlockPos POSITION_ENTROPINNYUM = new BlockPos(9, 1, 9);

	private static final BlockPos POSITION_FORCE_LENS_DETECTOR_RAIL = new BlockPos(7, 16, 10);
	private static final BlockPos POSITION_FORCE_LENS_PUSHED_SLIME_BLOCK = new BlockPos(6, 16, 10);
	private static final BlockPos POSITION_FORCE_LENS_MANA_SPREADER = new BlockPos(5, 16, 10);
	private static final BlockPos POSITION_FORCE_LENS_BUTTON = new BlockPos(5, 16, 8);

	private static final BlockPos POSITION_FORCE_RELAY_DETECTOR_RAIL = new BlockPos(4, 4, 10);
	private static final BlockPos POSITION_FORCE_RELAY_PUSHED_SLIME_BLOCK = new BlockPos(3, 4, 10);
	private static final BlockPos POSITION_FORCE_RELAY = new BlockPos(4, 3, 6);
	private static final BlockPos POSITION_FORCE_RELAY_BUTTON = new BlockPos(2, 4, 5);

	private static final BlockPos POSITION_ENTITYLESS_DUPER_BUTTON = new BlockPos(3, 13, 10);

	private static final BlockPos POSITION_ETHICAL_TNT_BLOCK_BUTTON = new BlockPos(9, 4, 9);

	private static Runnable checkEntropinnyumForUnethicalManaAmount(GameTestHelper helper) {
		return () -> {
			final var be = TestingUtil.getBlockEntity(helper, POSITION_ENTROPINNYUM);
			if (!(be instanceof EntropinnyumBlockEntity entropinnyum)) {
				throw TestingUtil.assertException("Missing Entropinnyum at: " + POSITION_ENTROPINNYUM);
			}
			if (entropinnyum.getMana() != 3) {
				throw TestingUtil.assertException("Wrong amount of mana: expected " + 3 + " but was " + entropinnyum.getMana());
			}
		};
	}

	@GameTest(structure = TEMPLATE_SLIME_DUPER_FORCE_LENS, maxTicks = 150)
	public void testForceLensDuper(GameTestHelper helper) {
		helper.startSequence()
				.thenExecute(() -> {
					var player = TestingUtil.makeMockPlayer(helper);
					var spreader = TestingUtil.assertBlockEntity(helper, POSITION_FORCE_LENS_MANA_SPREADER, BotaniaBlockEntities.SPREADER);
					TestingUtil.assertThat(spreader.bindTo(player, new ItemStack(BotaniaItems.twigWand),
							helper.absolutePos(POSITION_FORCE_LENS_PUSHED_SLIME_BLOCK), Direction.UP),
							() -> "Failed to bind spreader");

					if (helper.getBlockState(POSITION_FORCE_LENS_DETECTOR_RAIL).getBlock() != Blocks.DETECTOR_RAIL) {
						throw TestingUtil.assertException("Missing detector rail at: " +
								POSITION_FORCE_LENS_DETECTOR_RAIL);
					}
					helper.spawn(EntityType.MINECART, POSITION_FORCE_LENS_DETECTOR_RAIL);
					helper.pressButton(POSITION_FORCE_LENS_BUTTON);
				})
				.thenExecuteAfter(130, checkEntropinnyumForUnethicalManaAmount(helper))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE_SLIME_DUPER_FORCE_RELAY, maxTicks = 100)
	public void testForceRelayDuper(GameTestHelper helper) {
		helper.startSequence()
				.thenExecute(() -> {
					if (helper.getBlockState(POSITION_FORCE_RELAY).getBlock() != BotaniaBlocks.pistonRelay) {
						throw TestingUtil.assertException("Missing force relay at: " + POSITION_FORCE_RELAY);
					}
					var data = ForceRelayBlock.WorldData.get(helper.getLevel());
					data.mapping.put(helper.absolutePos(POSITION_FORCE_RELAY), helper.absolutePos(
							POSITION_FORCE_RELAY_PUSHED_SLIME_BLOCK));

					if (helper.getBlockState(POSITION_FORCE_RELAY_DETECTOR_RAIL).getBlock() != Blocks.DETECTOR_RAIL) {
						throw TestingUtil.assertException("Missing detector rail at: " +
								POSITION_FORCE_RELAY_DETECTOR_RAIL);
					}
					helper.spawn(EntityType.MINECART, POSITION_FORCE_RELAY_DETECTOR_RAIL);
				})
				.thenExecuteAfter(1, () -> helper.pressButton(POSITION_FORCE_RELAY_BUTTON))
				.thenExecuteAfter(90, checkEntropinnyumForUnethicalManaAmount(helper))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE_ENTITYLESS_DUPER, maxTicks = 100)
	public void testEntitylessDuper(GameTestHelper helper) {
		helper.startSequence()
				.thenExecute(() -> helper.pressButton(POSITION_ENTITYLESS_DUPER_BUTTON))
				.thenExecuteAfter(95, checkEntropinnyumForUnethicalManaAmount(helper))
				.thenSucceed();
	}

	@GameTest(structure = TEMPLATE_LEGIT_TNT, maxTicks = 100)
	public void testLegitimateTnt(GameTestHelper helper) {
		helper.startSequence()
				.thenExecute(() -> helper.pressButton(POSITION_ETHICAL_TNT_BLOCK_BUTTON))
				.thenExecuteAfter(95, () -> {
					final var be = TestingUtil.getBlockEntity(helper, POSITION_ENTROPINNYUM);
					if (!(be instanceof EntropinnyumBlockEntity entropinnyum)) {
						throw TestingUtil.assertException("Missing Entropinnyum at: " + POSITION_ENTROPINNYUM);
					}
					if (entropinnyum.getMana() != entropinnyum.getMaxMana()) {
						throw TestingUtil.assertException("Wrong amount of mana: expected " + entropinnyum.getMaxMana() + " but was " + entropinnyum.getMana());
					}
				})
				.thenSucceed();
	}
}
