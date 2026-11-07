package vazkii.botania.test.item;

import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public class GrassSeedDispensingTest {
	@GameTest(structure = "botania:item/grass_seed_dispensing", maxTicks = 100)
	public void TestGrassSeedDispensing(GameTestHelper helper) {
		BlockPos dispenserPos = new BlockPos(4, 2, 4);
		helper.assertBlockPresent(Blocks.DISPENSER, dispenserPos);

		helper.setBlock(dispenserPos.east(), Blocks.REDSTONE_BLOCK);

		helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.GRASS_BLOCK, dispenserPos.west()));
	}

}
