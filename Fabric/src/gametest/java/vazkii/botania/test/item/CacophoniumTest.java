/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test.item;

import net.minecraft.core.BlockPos;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.CacophoniumBlockEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.CacophoniumItem;
import vazkii.botania.mixin.MobAccessor;
import vazkii.botania.test.TestingUtil;

// https://github.com/williewillus/botania-fabric-issues/issues/92
public class CacophoniumTest {
	@GameTest(structure = "botania:block/cacophonium", maxTicks = 100)
	public void testCacophonium(GameTestHelper helper) {
		BlockPos cowPos = new BlockPos(1, 2, 1);
		BlockPos noteBlockPos = new BlockPos(1, 2, 3);
		helper.assertBlockPresent(Blocks.NOTE_BLOCK, noteBlockPos);

		Player player = TestingUtil.makeMockPlayer(helper);
		Cow cow = helper.spawnWithNoFreeWill(EntityType.COW, cowPos);

		//Give a cacophonium to the player.
		player.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(BotaniaItems.cacophonium));

		//Use the cacophonium on the cow.
		// 26.1 removed SoundEvents.COW_AMBIENT: cow sounds are now data-driven
		// CowSoundVariants, so ask the actual mob for its ambient sound (the same
		// MobAccessor invoker CacophoniumItem uses), instead of hardcoding a constant.
		var expectedSound = ((MobAccessor) cow).botania_getAmbientSound();
		// 26.1: interactOn now takes the interaction point explicitly.
		player.interactOn(cow, InteractionHand.MAIN_HAND, cow.position());
		TestingUtil.assertEquals(CacophoniumItem.getSound(player.getMainHandItem()), expectedSound);

		//Use the cacophonium on the note block.
		TestingUtil.useItemOn(helper, player, InteractionHand.MAIN_HAND, noteBlockPos);
		helper.assertBlockPresent(BotaniaBlocks.cacophonium, noteBlockPos);

		CacophoniumBlockEntity cacophoniumBlock = TestingUtil.assertBlockEntity(helper, noteBlockPos, BotaniaBlockEntities.CACOPHONIUM);
		TestingUtil.assertEqualsAt(helper, noteBlockPos, CacophoniumItem.getSound(cacophoniumBlock.stack), expectedSound);

		//Moo.
		helper.setBlock(noteBlockPos.south(), Blocks.REDSTONE_BLOCK);
		helper.assertBlockProperty(noteBlockPos, BlockStateProperties.POWERED, true);

		helper.succeed();
	}
}
