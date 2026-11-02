/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.gui.bag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.item.FlowerPouchItem;

public class FlowerPouchGui extends AbstractContainerScreen<FlowerPouchContainer> {

	private static final Identifier texture = Identifier.parse(ResourcesLib.GUI_FLOWER_BAG);

	public FlowerPouchGui(FlowerPouchContainer container, Inventory playerInv, Component title) {
		// 176x166 default, plus 36 pixels of extra rows
		super(container, playerInv, title, 176, 202);
	}

	@Override
	protected void extractMenuBackground(GuiGraphicsExtractor gui) {
		int k = (width - imageWidth) / 2;
		int l = (height - imageHeight) / 2;
		gui.blit(RenderPipelines.GUI_TEXTURED, texture, k, l, 0, 0, imageWidth, imageHeight, 256, 256);
	}

	@Override
	protected void extractSlots(GuiGraphicsExtractor gui, int mouseX, int mouseY) {
		super.extractSlots(gui, mouseX, mouseY);

		for (Slot slot : menu.slots) {
			if (slot.container == menu.flowerBagInv && !slot.hasItem()) {
				ItemStack missingFlower = new ItemStack(FlowerPouchItem.getFlowerForSlot(slot.index));
				RenderHelper.renderGuiItemAlpha(gui, missingFlower, slot.x, slot.y, 0x5F, null);
			}
		}

		gui.nextStratum();
		Minecraft mc = Minecraft.getInstance();
		for (Slot slot : menu.slots) {
			if (slot.container == menu.flowerBagInv && slot.getItem().getCount() == 1) {
				// Always draw the count even at 1
				gui.text(mc.font, "1", slot.x + 11, slot.y + 9, 0xFFFFFF);
			}
		}
	}

}
