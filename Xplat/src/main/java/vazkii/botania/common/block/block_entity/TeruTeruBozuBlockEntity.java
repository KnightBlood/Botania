/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.WeatherData;

public class TeruTeruBozuBlockEntity extends BotaniaBlockEntity {
	private boolean wasRaining = false;

	public TeruTeruBozuBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.TERU_TERU_BOZU, pos, state);
	}

	public static void serverTick(Level level, BlockPos worldPosition, BlockState state, TeruTeruBozuBlockEntity self) {
		boolean isRaining = level.isRaining();
		if (isRaining && level.getRandom().nextInt(9600) == 0) {
			setRaining(level, false);
			resetRainTime(level);
		}

		if (self.wasRaining != isRaining) {
			level.updateNeighbourForOutputSignal(worldPosition, state.getBlock());
		}
		self.wasRaining = isRaining;
	}

	public static void setRaining(Level w, boolean rain) {
		getWeatherData(w).setRaining(rain);
	}

	public static void resetRainTime(Level w) {
		WeatherData weather = getWeatherData(w);
		int time = w.getRandom().nextInt(weather.isRaining() ? 12000 : 168000) + 12000;
		weather.setRainTime(time);
	}

	private static WeatherData getWeatherData(Level w) {
		return ((ServerLevel) w).getWeatherData();
	}
}
