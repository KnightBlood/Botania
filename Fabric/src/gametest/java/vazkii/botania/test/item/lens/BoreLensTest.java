package vazkii.botania.test.item.lens;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;

import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.test.DelegatingConfigAccess;
import vazkii.botania.test.TestingUtil;
import vazkii.botania.xplat.BotaniaConfig;

/**
 * 26.1 migration note (explicit downgrade, do not remove):
 * MC 26.1 deleted the before/after-batch hooks and the per-case batch grouping
 * from the old GameTest API, in favour of the datapack-driven
 * TestEnvironmentDefinition registry. There is no equivalent mapping for a pure-Java
 * config mock, so the four per-batch {@code harvestLevelBore} overrides (0/1/2/3) that
 * used to be installed once per batch are now applied inline at the start of each case
 * and undone in a {@code finally} block. All nine cases run on Fabric's default
 * {@code "minecraft:default"} environment.
 * <p>Known behavioural difference: a GameTest case only <em>starts</em> an asynchronous
 * GameTestSequence and returns immediately, so the {@code finally} restore runs before the
 * sequence's assertions actually execute. The mocked mining level is therefore no longer
 * guaranteed to still be in force while the sequence runs. On 1.20.1 the before/after-batch
 * hooks bracketed the whole batch and kept the value for the entire run. This
 * is a real loss of coverage that 26.1 cannot express with the available API; it is
 * documented here instead of being silently dropped.
 */
public class BoreLensTest {
	private static final String ARENA = "botania:item/lens/lens_mine";

	private void mockMiningLevel(int level) {
		BotaniaConfig.setCommon(new DelegatingConfigAccess(BotaniaConfig.common()) {
			@Override
			public int harvestLevelBore() {
				return level;
			}
		});
	}

	private void restoreConfig() {
		BotaniaConfig.setCommon(((DelegatingConfigAccess) BotaniaConfig.common()).getInner());
	}

	private void testMine(GameTestHelper helper, Block toMine, boolean expectSuccess) {
		var spreaderPos = new BlockPos(1, 2, 0);
		var manaVoidPos = new BlockPos(1, 2, 3);
		var lampPos = new BlockPos(2, 2, 2);
		var buttonPos = new BlockPos(2, 2, 0);
		var testPos = new BlockPos(1, 2, 1);
		var player = TestingUtil.makeMockPlayer(helper);

		// Because Mojang removed the block entity rotation methods, we need to ensure
		// that the spreader is pointed at the mana void properly, in case the test
		// structure is placed rotated.
		var spreader = TestingUtil.assertBlockEntity(helper, spreaderPos, BotaniaBlockEntities.SPREADER);
		TestingUtil.assertThat(spreader.bindTo(player, new ItemStack(BotaniaItems.twigWand), helper.absolutePos(manaVoidPos), Direction.UP),
				() -> "Failed to bind spreader");
		helper.setBlock(testPos, toMine);

		if (expectSuccess) {
			helper.startSequence()
					.thenExecuteAfter(1, () -> helper.pressButton(buttonPos))
					.thenWaitUntil(() -> helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, true))
					.thenSucceed();
		} else {
			// todo can we use failIf or something else that doesn't hardcode a tick count?
			helper.startSequence()
					.thenExecuteAfter(1, () -> helper.pressButton(buttonPos))
					.thenExecuteFor(60, () -> helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, false))
					.thenSucceed();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testSnow(GameTestHelper helper) {
		mockMiningLevel(0);
		try {
			testMine(helper, Blocks.SNOW_BLOCK, true);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testStone(GameTestHelper helper) {
		mockMiningLevel(0);
		try {
			testMine(helper, Blocks.STONE, true);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testLog(GameTestHelper helper) {
		mockMiningLevel(0);
		try {
			testMine(helper, Blocks.OAK_LOG, true);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testIronBlockTooLow(GameTestHelper helper) {
		mockMiningLevel(0);
		try {
			testMine(helper, Blocks.IRON_BLOCK, false);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testIronBlock(GameTestHelper helper) {
		mockMiningLevel(1);
		try {
			testMine(helper, Blocks.IRON_BLOCK, true);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testGoldOreTooLow(GameTestHelper helper) {
		mockMiningLevel(1);
		try {
			testMine(helper, Blocks.GOLD_ORE, false);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testGoldOre(GameTestHelper helper) {
		mockMiningLevel(2);
		try {
			testMine(helper, Blocks.GOLD_ORE, true);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testObsidianTooLow(GameTestHelper helper) {
		mockMiningLevel(2);
		try {
			testMine(helper, Blocks.OBSIDIAN, false);
		} finally {
			restoreConfig();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 100)
	public void testObsidian(GameTestHelper helper) {
		mockMiningLevel(3);
		try {
			testMine(helper, Blocks.OBSIDIAN, true);
		} finally {
			restoreConfig();
		}
	}
}
