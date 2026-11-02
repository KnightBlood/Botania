/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.SpectralRailBlock;

@Mixin(AbstractMinecart.class)
public class AbstractMinecartFabricMixin {
	@Inject(at = @At("HEAD"), method = "moveAlongTrack")
	private void handleOnRail(ServerLevel level, CallbackInfo ci) {
		AbstractMinecart self = (AbstractMinecart) (Object) this;
		// 26.1: moveAlongTrack no longer receives the rail pos/state; fetch them like
		// NewMinecartBehavior.moveAlongTrack does.
		BlockPos pos = self.getCurrentBlockPosOrRailBelow();
		BlockState state = level.getBlockState(pos);
		if (state.is(BotaniaBlocks.ghostRail)) {
			((SpectralRailBlock) BotaniaBlocks.ghostRail).onMinecartPass(state, self.level(), pos, self);
		}
	}

}
