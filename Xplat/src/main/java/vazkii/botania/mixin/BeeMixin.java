/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import vazkii.botania.common.lib.BotaniaTags;

@Mixin(Bee.class)
public abstract class BeeMixin {

	/**
	 * Allows bees to treat special flowers as proper flowers for
	 * pollination despite being excluded from the vanilla tag.
	 * <p>
	 * 26.1: flower detection collapsed into the static {@code Bee#attractsBees}
	 * (tag {@code BEE_ATTRACTIVE} + waterlogged check), called by both the
	 * pollinate goal's {@code findNearbyFlower} and the validate-flower goal; this
	 * replaces the 1.20.1 {@code isFlowerValid}/{@code VALID_POLLINATION_BLOCKS}
	 * mixins (the old fields/methods no longer exist). Callers already gate on
	 * {@code Level#isLoaded}, so no loaded check is needed here.
	 */
	@Inject(method = "attractsBees", cancellable = true, at = @At("HEAD"))
	private static void botania$isSpecialFlower(BlockState state, CallbackInfoReturnable<Boolean> cir) {
		if (state.is(BotaniaTags.Blocks.SPECIAL_FLOWERS)) {
			cir.setReturnValue(true);
		}
	}
}
