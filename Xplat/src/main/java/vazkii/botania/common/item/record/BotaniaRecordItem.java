/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.item.record;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxPlayable;
import net.minecraft.world.item.JukeboxSong;

public class BotaniaRecordItem extends Item {
	// 26.1: RecordItem was removed; records now carry a JukeboxPlayable component
	// pointing at a JukeboxSong embedded directly (no registry entry needed).
	public BotaniaRecordItem(int comparator, SoundEvent sound, Properties builder, int lengthInSeconds, String descKey) {
		super(builder.component(DataComponents.JUKEBOX_PLAYABLE, new JukeboxPlayable(Holder.direct(new JukeboxSong(
				Holder.direct(sound), Component.translatable(descKey), lengthInSeconds, comparator)))));
	}
}
