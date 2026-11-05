/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import vazkii.botania.client.ClothBotaniaConfigScreen;

/**
 * Wires the shared Cloth Config screen into Mod Menu's config button.
 * The screen implementation lives in Xplat
 * ({@link vazkii.botania.client.ClothBotaniaConfig}) so both loaders
 * present the identical settings UI.
 */
public class BotaniaModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ClothBotaniaConfigScreen::create;
	}
}
