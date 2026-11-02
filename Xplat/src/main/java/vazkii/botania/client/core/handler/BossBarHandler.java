/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.entity.GaiaGuardianEntity;

import java.util.*;

public final class BossBarHandler {

	private BossBarHandler() {}

	// Only access on the client thread!
	public static final Set<GaiaGuardianEntity> bosses = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Identifier BAR_TEXTURE = Identifier.parse(ResourcesLib.GUI_BOSS_BAR);

	public static OptionalInt onBarRender(GuiGraphicsExtractor gui, int x, int y, BossEvent bossEvent, boolean drawName) {
		for (GaiaGuardianEntity currentBoss : bosses) {
			if (currentBoss.getBossInfoUuid().equals(bossEvent.getId())) {
				Minecraft mc = Minecraft.getInstance();
				// todo boss_bar.png has textures for different colors, respect bossEvent's getColor()?
				int frameU = 0, frameV = 0;
				int frameWidth = 185, frameHeight = 15;
				int healthU = 0, healthV = frameV + frameHeight;
				int healthWidth = 181, healthHeight = 7;
				int healthX = x + (frameWidth - healthWidth) / 2;
				int healthY = y + (frameHeight - healthHeight) / 2;

				int playerCountHeight = drawPlayerCount(currentBoss.getPlayerCount(), gui, x, y);
				RenderHelper.drawTexturedModalRect(gui, BAR_TEXTURE, x, y, frameU, frameV,
						frameWidth, frameHeight);
				drawHealthBar(gui, currentBoss, healthX, healthY, healthU, healthV,
						(int) (healthWidth * bossEvent.getProgress()), healthHeight, false);

				if (drawName) {
					Component name = bossEvent.getName();
					int centerX = mc.getWindow().getGuiScaledWidth() / 2;
					gui.centeredText(mc.font, name, centerX, y - 10, 0xA2018C);
				}

				return OptionalInt.of(frameHeight + playerCountHeight + (drawName ? mc.font.lineHeight : 0));
			}
		}

		return OptionalInt.empty();
	}

	private static int drawPlayerCount(int playerCount, GuiGraphicsExtractor gui, int x, int y) {
		int px = x + 160;
		int py = y + 12;

		Minecraft mc = Minecraft.getInstance();
		ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
		gui.item(stack, px, py);

		gui.text(mc.font, Integer.toString(playerCount), px + 15, py + 4, 0xFFFFFF);

		return 5;
	}

	private static void drawHealthBar(GuiGraphicsExtractor gui, GaiaGuardianEntity currentBoss, int x, int y, int u, int v, int w, int h, boolean bg) {
		// TODO 26.1: the dopplegangerBar shader (grain intensity / HP fracture effects) needs a
		// RenderPipeline-based rewrite; for now render the health bar as a plain textured quad.
		gui.blit(RenderPipelines.GUI_TEXTURED, BAR_TEXTURE, x, y, u, v, w, h, 256, 256, -1);
	}

}
