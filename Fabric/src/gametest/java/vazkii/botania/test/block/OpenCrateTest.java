/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.block;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import vazkii.botania.test.TestingUtil;

public class OpenCrateTest {
	@GameTest(structure = "botania:block/opencrate_basic", maxTicks = 100)
	public void testBasic(GameTestHelper helper) {
		helper.pullLever(new BlockPos(0, 4, 0));
		BlockPos belowCrate = new BlockPos(2, 2, 1);
		helper.succeedWhen(() -> helper.assertItemEntityCountIs(Items.STICK, belowCrate, 0, 8));
	}

	@GameTest(structure = "botania:block/opencrate_blocked", maxTicks = 100)
	public void testBlocked(GameTestHelper helper) {
		helper.startSequence().thenExecute(() -> helper.pullLever(new BlockPos(0, 4, 0)))
				.thenExecuteFor(70, () -> helper.assertEntityNotPresent(EntityType.ITEM))
				.thenSucceed();
	}

	@GameTest(structure = "botania:block/opencrate_snug", maxTicks = 100)
	public void testSnug(GameTestHelper helper) {
		helper.pullLever(new BlockPos(0, 4, 0));
		BlockPos belowCrate = new BlockPos(2, 2, 1);
		helper.succeedWhen(() -> {
			var be = TestingUtil.getBlockEntity(helper, belowCrate);
			if (!(be instanceof HopperBlockEntity hopper
					&& hopper.countItem(Items.STICK) == 8)) {
				throw TestingUtil.assertException("Sticks not in lower hopper");
			}
		});
	}
}
