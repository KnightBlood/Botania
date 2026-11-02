/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.network.clientbound;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.client.gui.ItemsRemainingRenderHandler;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public record UpdateItemsRemainingPacket(ItemStack stack, int count, @Nullable Component tooltip) implements BotaniaPacket {

	public static final Identifier ID = prefix("rem");

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeWithCodec(NbtOps.INSTANCE, ItemStack.CODEC, stack);
		buf.writeVarInt(count);
		buf.writeBoolean(tooltip != null);
		if (tooltip != null) {
			buf.writeWithCodec(NbtOps.INSTANCE, ComponentSerialization.CODEC, tooltip);
		}
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	public static UpdateItemsRemainingPacket decode(FriendlyByteBuf buf) {
		return new UpdateItemsRemainingPacket(
				buf.readWithCodecTrusted(NbtOps.INSTANCE, ItemStack.CODEC),
				buf.readVarInt(),
				buf.readBoolean() ? buf.readWithCodecTrusted(NbtOps.INSTANCE, ComponentSerialization.CODEC) : null
		);
	}

	public static class Handler {
		public static void handle(UpdateItemsRemainingPacket packet) {
			ItemStack stack = packet.stack();
			int count = packet.count();
			Component tooltip = packet.tooltip();
			Minecraft.getInstance().execute(() -> ItemsRemainingRenderHandler.set(stack, count, tooltip));
		}
	}
}
