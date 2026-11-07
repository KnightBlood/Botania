/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestAssertPosException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.block.ForceRelayBlock;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.lens.LensItem;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TestingUtil {
	// Copied from FabricGameTest. Needs to be replaced if we ever run tests on both loaders
	public static final String EMPTY_STRUCTURE = "fabric-gametest-api-v1:empty";

	// 26.1: GameTestHelper's getBlockEntity / makeMockPlayer / assertBlock / assertBlockState /
	// fail and the assertion exceptions changed their signatures (Component-based messages,
	// generic getBlockEntity, GameType for makeMockPlayer, int tick). The static shims below
	// keep every ported test call site on the original String / Supplier<String> shape, so
	// only this GameTestHelper-facing file has to know about the new API.

	@Contract("_,_,_ -> fail")
	public static void throwPositionedAssertion(GameTestHelper helper, BlockPos relativePos, Supplier<String> msg) {
		//A couple of GameTestHelper's assertion errors throw this exception, but it's inconvenient to throw yourself
		throw new GameTestAssertPosException(Component.literal(msg.get()), helper.absolutePos(relativePos),
				relativePos, (int) helper.getTick());
	}

	@Contract("false,_ -> fail")
	public static void assertThat(boolean value, Supplier<String> message) {
		//The same as Preconditions.checkArgument but throws a GameTestAssertException
		if (!value) {
			// 26.1: GameTestAssertException(Component, int). The old String ctor carried no
			// tick; 0 is used here (only cosmetic in the failure message of a polling assert).
			throw new GameTestAssertException(Component.literal(message.get()), 0);
		}
	}

	@Contract("_,_,false,_ -> fail")
	public static void assertAt(GameTestHelper helper, BlockPos relativePos, boolean value, Supplier<String> message) {
		//The same as Preconditions.checkArgument but throws a GameTestAssertPosException
		if (!value) {
			throwPositionedAssertion(helper, relativePos, message);
		}
	}

	public static void assertEquals(@Nullable Object a, @Nullable Object b) {
		assertEquals(a, b, () -> "Expected " + a + " to equal " + b);
	}

	public static void assertEquals(@Nullable Object a, @Nullable Object b, Supplier<String> message) {
		assertThat(Objects.equals(a, b), message);
	}

	public static void assertEqualsAt(GameTestHelper helper, BlockPos relativePos, @Nullable Object a, @Nullable Object b) {
		assertEqualsAt(helper, relativePos, a, b, () -> "Expected " + a + " to equal " + b);
	}

	public static void assertEqualsAt(GameTestHelper helper, BlockPos relativePos, @Nullable Object a, @Nullable Object b, Supplier<String> message) {
		assertAt(helper, relativePos, Objects.equals(a, b), message);
	}

	// --- MC 26.1 GameTestHelper compatibility shims ---------------------------------

	/** Legacy no-arg getBlockEntity now requires a BlockEntity class argument. */
	public static BlockEntity getBlockEntity(GameTestHelper helper, BlockPos pos) {
		return helper.getBlockEntity(pos, BlockEntity.class);
	}

	/** Legacy no-arg makeMockPlayer now requires a GameType. */
	public static Player makeMockPlayer(GameTestHelper helper) {
		return helper.makeMockPlayer(GameType.SURVIVAL);
	}

	/** assertBlock(BlockPos, Predicate&lt;Block&gt;, String) -> Function&lt;Block, Component&gt; */
	public static void assertBlock(GameTestHelper helper, BlockPos pos, Predicate<Block> predicate, String message) {
		helper.assertBlock(pos, predicate, block -> Component.literal(message));
	}

	/** assertBlock(BlockPos, Predicate&lt;Block&gt;, Supplier&lt;String&gt;) -> Function&lt;Block, Component&gt; */
	public static void assertBlock(GameTestHelper helper, BlockPos pos, Predicate<Block> predicate, Supplier<String> message) {
		helper.assertBlock(pos, predicate, block -> Component.literal(message.get()));
	}

	/** assertBlockState(..., Supplier/ String) -> Function&lt;BlockState, Component&gt; */
	public static void assertBlockState(GameTestHelper helper, BlockPos pos, Predicate<BlockState> predicate, String message) {
		helper.assertBlockState(pos, predicate, state -> Component.literal(message));
	}

	public static void assertBlockState(GameTestHelper helper, BlockPos pos, Predicate<BlockState> predicate, Supplier<String> message) {
		helper.assertBlockState(pos, predicate, state -> Component.literal(message.get()));
	}

	/** fail(String, BlockPos/Entity) -> fail(Component, BlockPos/Entity) */
	public static void fail(GameTestHelper helper, String message, BlockPos pos) {
		helper.fail(Component.literal(message), pos);
	}

	public static void fail(GameTestHelper helper, String message, Entity entity) {
		helper.fail(Component.literal(message), entity);
	}

	/** The single-arg fail(String) is unchanged on 26.1; kept for completeness. */
	public static void fail(GameTestHelper helper, String message) {
		helper.fail(message);
	}

	/** Build the Component-based assertion exception (legacy String ctor is gone). */
	public static GameTestAssertException assertException(String message) {
		return new GameTestAssertException(Component.literal(message), 0);
	}

	/** Build the Component-based positioned assertion exception. */
	public static GameTestAssertPosException assertPosException(String message, BlockPos absolutePos, BlockPos relativePos, long tick) {
		return new GameTestAssertPosException(Component.literal(message), absolutePos, relativePos, (int) tick);
	}

	// --- original helpers, updated for 26.1 ----------------------------------------

	@SuppressWarnings("unchecked")
	public static <T extends BlockEntity> T assertBlockEntity(GameTestHelper helper, BlockPos relativePos, BlockEntityType<T> type) {
		BlockEntity be = helper.getBlockEntity(relativePos, BlockEntity.class);

		assertAt(helper, relativePos, be != null, () -> "Expected BlockEntity of type " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type) + " but found no BlockEntity");
		assertAt(helper, relativePos, be.getType() == type, () -> "Expected BlockEntity of type " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type) + " but found " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()));

		return (T) be;
	}

	public static <T extends BlockEntity> T assertBlockEntity(GameTestHelper helper, BlockPos relativePos, Class<T> classs) {
		BlockEntity be = helper.getBlockEntity(relativePos, BlockEntity.class);

		assertAt(helper, relativePos, be != null, () -> "Expected BlockEntity of class " + classs.getSimpleName() + " but found no BlockEntity");
		assertAt(helper, relativePos, classs.isAssignableFrom(be.getClass()), () -> "Expected BlockEntity to be an instance of " + classs.getSimpleName() + " but found " + be.getClass().getSimpleName());

		return classs.cast(be);
	}

	public static BlockEntity assertAnyBlockEntity(GameTestHelper helper, BlockPos relativePos) {
		BlockEntity be = helper.getBlockEntity(relativePos, BlockEntity.class);
		assertAt(helper, relativePos, be != null, () -> "Expected any BlockEntity but found nothing");
		return be;
	}

	public static void useItemOn(GameTestHelper helper, Player player, InteractionHand hand, BlockPos pos) {
		//Gametest gotcha: You don't have a ClientPlayer or a ServerPlayer, you literally just have a Player.
		//So there's no XxxxPlayerGameMode, which is where Item#useOn(UseOnContext) style interactions typically happen.
		//This is sorta a discount version of ServerPlayerGameMode#useItemOn, no cheat checks or criteria triggers or anything.
		BlockPos absolutePos = helper.absolutePos(pos);
		BlockHitResult result = new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true);
		ItemStack stack = player.getItemInHand(hand);
		UseOnContext useOnContext = new UseOnContext(player, hand, result);
		stack.useOn(useOnContext);
	}

	public static void bindWithWandOfTheForest(GameTestHelper helper, BlockPos first, BlockPos second) {
		//Conjure a player with Wand of the Forest
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);
		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BotaniaItems.twigWand));
		player.setShiftKeyDown(true);

		//Move the player to each destination just to make sure they're in-range
		player.setPos(Vec3.atCenterOf(first));
		useItemOn(helper, player, InteractionHand.MAIN_HAND, first);
		player.setPos(Vec3.atCenterOf(second));
		useItemOn(helper, player, InteractionHand.MAIN_HAND, second);
	}

	public static void bindForceRelayTarget(GameTestHelper helper, BlockPos relayPos, BlockPos targetPos) {
		var data = ForceRelayBlock.WorldData.get(helper.getLevel());
		data.mapping.put(helper.absolutePos(relayPos), helper.absolutePos(targetPos));
	}

	@Nullable
	public static BlockPos getBoundForceRelayTarget(GameTestHelper helper, BlockPos relayPos) {
		var data = ForceRelayBlock.WorldData.get(helper.getLevel());
		return data.mapping.get(helper.absolutePos(relayPos));
	}

	public static void setUpSpreaderAndCompositeLens(GameTestHelper helper, Item firstLensType, Item secondLensType, BlockPos spreaderPos, BlockPos spreaderTargetPos) {
		final var firstLensStack = new ItemStack(firstLensType);
		final var secondLensStack = new ItemStack(secondLensType);
		final var compositeLens = ((LensItem) firstLensStack.getItem()).setCompositeLens(firstLensStack, secondLensStack);
		final var spreaderEntity = assertBlockEntity(helper, spreaderPos, BotaniaBlockEntities.SPREADER);
		spreaderEntity.getItemHandler().setItem(0, compositeLens);

		bindWithWandOfTheForest(helper, spreaderPos, spreaderTargetPos);
	}
}
