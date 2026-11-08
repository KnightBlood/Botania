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
 * used to be installed once per batch are now installed by each case's sequence and
 * undone by the same sequence before it succeeds. All nine cases run on Fabric's default
 * {@code "minecraft:default"} environment.
 * <p>
 * 26.1 limitation: a GameTest case only <em>starts</em> an asynchronous
 * GameTestSequence and returns immediately, so a mock installed at case start would be
 * undone before the sequence's assertions execute. The mock is therefore installed by the
 * sequence's first step and restored by its last step (before {@code thenSucceed()}).
 * Because {@code BotaniaConfig.common()} is a global singleton and the gametest runner
 * executes the nine cases concurrently in separate structure areas, the active windows
 * are additionally serialized through a static handoff flag ({@link #anotherCaseActive}):
 * every case waits (polling via {@code thenWaitUntil}) until no sibling case is active,
 * then installs its mock, runs, and clears the flag. This restores the determinism that
 * the 1.20.1 before/after-batch hooks provided for whole batches. All nine cases run on
 * Fabric's default {@code "minecraft:default"} environment.
 */
public class BoreLensTest {
	private static final String ARENA = "botania:item/lens/lens_mine";

	// Serialize the config-mock windows of the nine concurrently running cases. All
	// GameTest sequence steps run on the single server thread, so this flag needs no
	// synchronization; see the class Javadoc.
	private static boolean anotherCaseActive = false;

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

	private void testMine(GameTestHelper helper, Block toMine, int harvestLevel, boolean expectSuccess) {
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
					// 26.1: wait for any sibling bore case, then install the config mock inside
					// the sequence (a mock installed at case start would be restored before the
					// asynchronous sequence ran). The mock stays in force for the whole flight.
					.thenWaitUntil(() -> helper.assertTrue(!anotherCaseActive, "Waiting for another bore lens case to release the config mock"))
					.thenExecute(() -> {
						anotherCaseActive = true;
						mockMiningLevel(harvestLevel);
					})
					.thenExecuteAfter(1, () -> helper.pressButton(buttonPos))
					.thenWaitUntil(() -> helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, true))
					.thenExecute(() -> {
						restoreConfig();
						anotherCaseActive = false;
					})
					.thenSucceed();
		} else {
			// todo can we use failIf or something else that doesn't hardcode a tick count?
			helper.startSequence()
					.thenWaitUntil(() -> helper.assertTrue(!anotherCaseActive, "Waiting for another bore lens case to release the config mock"))
					.thenExecute(() -> {
						anotherCaseActive = true;
						mockMiningLevel(harvestLevel);
					})
					.thenExecuteAfter(1, () -> helper.pressButton(buttonPos))
					.thenExecuteFor(60, () -> helper.assertBlockProperty(lampPos, RedstoneLampBlock.LIT, false))
					.thenExecute(() -> {
						restoreConfig();
						anotherCaseActive = false;
					})
					.thenSucceed();
		}
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testSnow(GameTestHelper helper) {
		testMine(helper, Blocks.SNOW_BLOCK, 0, true);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testStone(GameTestHelper helper) {
		testMine(helper, Blocks.STONE, 0, true);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testLog(GameTestHelper helper) {
		testMine(helper, Blocks.OAK_LOG, 0, true);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testIronBlockTooLow(GameTestHelper helper) {
		testMine(helper, Blocks.IRON_BLOCK, 0, false);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testIronBlock(GameTestHelper helper) {
		testMine(helper, Blocks.IRON_BLOCK, 1, true);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testGoldOreTooLow(GameTestHelper helper) {
		testMine(helper, Blocks.GOLD_ORE, 1, false);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testGoldOre(GameTestHelper helper) {
		testMine(helper, Blocks.GOLD_ORE, 2, true);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testObsidianTooLow(GameTestHelper helper) {
		testMine(helper, Blocks.OBSIDIAN, 2, false);
	}

	@GameTest(structure = ARENA, maxTicks = 600)
	public void testObsidian(GameTestHelper helper) {
		testMine(helper, Blocks.OBSIDIAN, 3, true);
	}
}
