/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.ArrayList;
import java.util.List;

// MC 26.1: the F3 screen now renders a registry of DebugScreenEntry objects
// instead of DebugScreenOverlay returning text lists.
public class BotaniaDebugEntry implements DebugScreenEntry {
	@Override
	public void display(DebugScreenDisplayer displayer, Level level, LevelChunk clientChunk, LevelChunk serverChunk) {
		List<String> lines = new ArrayList<>();
		DebugHandler.onDrawDebugText(lines);
		for (String line : lines) {
			displayer.addLine(line);
		}
	}
}
