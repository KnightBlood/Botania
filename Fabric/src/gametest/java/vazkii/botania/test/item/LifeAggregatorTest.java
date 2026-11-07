package vazkii.botania.test.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.LifeAggregatorItem;
import vazkii.botania.test.TestingUtil;

public class LifeAggregatorTest {
	@GameTest(structure = TestingUtil.EMPTY_STRUCTURE, maxTicks = 100)
	public void testSpawnerMover(GameTestHelper helper) {
		var spawnerPos = BlockPos.ZERO;
		var player = TestingUtil.makeMockPlayer(helper);
		var stack = new ItemStack(BotaniaItems.spawnerMover);

		helper.setBlock(spawnerPos, Blocks.SPAWNER);
		SpawnerBlockEntity be = (SpawnerBlockEntity) TestingUtil.getBlockEntity(helper, spawnerPos);
		be.setEntityId(EntityType.PIG, RandomSource.create());

		player.setItemInHand(InteractionHand.MAIN_HAND, stack);

		InteractionResult result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.ZERO, Direction.DOWN, helper.absolutePos(spawnerPos), false)));
		TestingUtil.assertThat(result.consumesAction(), () -> "Should have succeeded");
		TestingUtil.assertThat(LifeAggregatorItem.hasData(stack),
				() -> "Spawner mover should recognize saved data. Full NBT: " + (stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA) != null ? stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag() : "none"));
		TestingUtil.assertBlockState(helper, spawnerPos, BlockState::isAir, () -> "Spawner should be gone");

		result = stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
				new BlockHitResult(Vec3.ZERO, Direction.UP, helper.absolutePos(spawnerPos.below()), false)));
		TestingUtil.assertThat(result.consumesAction(), () -> "Should have succeeded");
		helper.assertBlockPresent(Blocks.SPAWNER, spawnerPos);
		TestingUtil.assertThat(stack.isEmpty(), () -> "Spawner mover should be broken after placing");

		helper.setBlock(spawnerPos, Blocks.AIR); // Don't spawn mobs that interfere with other continuing tests
		helper.succeed();
	}
}
