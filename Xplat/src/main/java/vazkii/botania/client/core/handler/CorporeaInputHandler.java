/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import vazkii.botania.client.core.proxy.ClientProxy;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.mixin.client.AbstractContainerScreenAccessor;
import vazkii.botania.network.serverbound.IndexKeybindRequestPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CorporeaInputHandler {

	public static final List<Supplier<ItemStack>> hoveredStackGetters = new ArrayList<>();
	public static Predicate<Screen> supportedGuiFilter = gui -> gui instanceof AbstractContainerScreen;

	private static boolean isShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT);
	}

	private static boolean isControlDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)
				|| InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_CONTROL);
	}

	public static boolean buttonPressed(int keyCode, int scanCode) {
		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null || !supportedGuiFilter.test(mc.screen)
				|| !ClientProxy.CORPOREA_REQUEST.matches(new KeyEvent(keyCode, scanCode, 0))
				|| CorporeaIndexBlockEntity.getNearbyValidIndexes(mc.player).isEmpty()) {
			return false;
		}

		ItemStack stack = getStackUnderMouse();
		if (stack != null && !stack.isEmpty()) {
			int count = 1;
			int max = stack.getMaxStackSize();

			if (isShiftDown()) {
				count = max;
				if (isControlDown()) {
					count /= 4;
				}
			} else if (isControlDown()) {
				count = max / 2;
			}

			if (count > 0) {
				ItemStack requested = stack.copyWithCount(count);
				ClientXplatAbstractions.INSTANCE.sendToServer(new IndexKeybindRequestPacket(requested));
				return true;
			}
		}
		return false;
	}

	private static ItemStack getStackUnderMouse() {
		Screen screen = Minecraft.getInstance().screen;
		if (screen instanceof AbstractContainerScreen) {
			Slot slotUnderMouse = ((AbstractContainerScreenAccessor) screen).getHoveredSlot();
			if (slotUnderMouse != null && slotUnderMouse.hasItem()) {
				return slotUnderMouse.getItem().copy();
			}
			// TODO 26.1: recipe book integration (RecipeUpdateListener no longer exposes the component)
		}

		for (var getter : CorporeaInputHandler.hoveredStackGetters) {
			ItemStack stack = getter.get();
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}
}
