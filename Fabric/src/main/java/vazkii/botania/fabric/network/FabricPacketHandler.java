/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.fabric.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import vazkii.botania.network.BotaniaPacket;
import vazkii.botania.network.TriConsumer;
import vazkii.botania.network.clientbound.*;
import vazkii.botania.network.serverbound.*;

import java.util.function.Consumer;
import java.util.function.Function;

public final class FabricPacketHandler {
	/**
	 * 26.1: Fabric networking became payload-based ({@link CustomPacketPayload} + {@link StreamCodec}),
	 * so Botania's raw {@link BotaniaPacket}s are wrapped in this record for transport.
	 */
	public record Payload<T extends BotaniaPacket>(CustomPacketPayload.Type<Payload<T>> type, T packet)
			implements
				CustomPacketPayload {
		// The record-generated type() accessor returns Type<Payload<T>>, which satisfies
		// CustomPacketPayload#type() (Type<? extends CustomPacketPayload>); do not override it.
	}

	private static final CustomPacketPayload.Type<Payload<DodgePacket>> DODGE = type(DodgePacket.ID);
	private static final CustomPacketPayload.Type<Payload<IndexKeybindRequestPacket>> INDEX_KEYBIND = type(IndexKeybindRequestPacket.ID);
	private static final CustomPacketPayload.Type<Payload<IndexStringRequestPacket>> INDEX_STRING = type(IndexStringRequestPacket.ID);
	private static final CustomPacketPayload.Type<Payload<JumpPacket>> JUMP = type(JumpPacket.ID);
	private static final CustomPacketPayload.Type<Payload<LeftClickPacket>> LEFT_CLICK = type(LeftClickPacket.ID);
	private static final CustomPacketPayload.Type<Payload<AvatarSkiesRodPacket>> AVATAR_SKIES = type(AvatarSkiesRodPacket.ID);
	private static final CustomPacketPayload.Type<Payload<BotaniaEffectPacket>> EFFECT = type(BotaniaEffectPacket.ID);
	private static final CustomPacketPayload.Type<Payload<GogWorldPacket>> GOG_WORLD = type(GogWorldPacket.ID);
	private static final CustomPacketPayload.Type<Payload<ItemAgePacket>> ITEM_AGE = type(ItemAgePacket.ID);
	private static final CustomPacketPayload.Type<Payload<SpawnGaiaGuardianPacket>> GAIA_GUARDIAN = type(SpawnGaiaGuardianPacket.ID);
	private static final CustomPacketPayload.Type<Payload<UpdateItemsRemainingPacket>> ITEMS_REMAINING = type(UpdateItemsRemainingPacket.ID);

	private static <T extends BotaniaPacket> CustomPacketPayload.Type<Payload<T>> type(Identifier id) {
		return new CustomPacketPayload.Type<>(id);
	}

	private static <T extends BotaniaPacket> StreamCodec<RegistryFriendlyByteBuf, Payload<T>> codec(
			CustomPacketPayload.Type<Payload<T>> type, Function<FriendlyByteBuf, T> decoder) {
		return StreamCodec.ofMember(
				(payload, buf) -> payload.packet().encode(buf),
				buf -> new Payload<>(type, decoder.apply(buf)));
	}

	/** Wraps a raw Botania packet for the payload-based Fabric send API. */
	public static CustomPacketPayload wrap(BotaniaPacket packet) {
		return new Payload<>(new CustomPacketPayload.Type<>(packet.getFabricId()), packet);
	}

	public static void init() {
		// 26.1: payload codecs must be registered on both logical sides before any send/receive.
		PayloadTypeRegistry.serverboundPlay().register(DODGE, codec(DODGE, DodgePacket::decode));
		PayloadTypeRegistry.serverboundPlay().register(INDEX_KEYBIND, codec(INDEX_KEYBIND, IndexKeybindRequestPacket::decode));
		PayloadTypeRegistry.serverboundPlay().register(INDEX_STRING, codec(INDEX_STRING, IndexStringRequestPacket::decode));
		PayloadTypeRegistry.serverboundPlay().register(JUMP, codec(JUMP, JumpPacket::decode));
		PayloadTypeRegistry.serverboundPlay().register(LEFT_CLICK, codec(LEFT_CLICK, LeftClickPacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(AVATAR_SKIES, codec(AVATAR_SKIES, AvatarSkiesRodPacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(EFFECT, codec(EFFECT, BotaniaEffectPacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(GOG_WORLD, codec(GOG_WORLD, GogWorldPacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(ITEM_AGE, codec(ITEM_AGE, ItemAgePacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(GAIA_GUARDIAN, codec(GAIA_GUARDIAN, SpawnGaiaGuardianPacket::decode));
		PayloadTypeRegistry.clientboundPlay().register(ITEMS_REMAINING, codec(ITEMS_REMAINING, UpdateItemsRemainingPacket::decode));

		ServerPlayNetworking.registerGlobalReceiver(DODGE, makeServerBoundHandler(DodgePacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(INDEX_KEYBIND, makeServerBoundHandler(IndexKeybindRequestPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(INDEX_STRING, makeServerBoundHandler(IndexStringRequestPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(JUMP, makeServerBoundHandler(JumpPacket::handle));
		ServerPlayNetworking.registerGlobalReceiver(LEFT_CLICK, makeServerBoundHandler(LeftClickPacket::handle));
	}

	private static <T extends BotaniaPacket> ServerPlayNetworking.PlayPayloadHandler<Payload<T>> makeServerBoundHandler(
			TriConsumer<T, MinecraftServer, ServerPlayer> handle) {
		return (payload, ctx) -> handle.accept(payload.packet(), ctx.server(), ctx.player());
	}

	public static void initClient() {
		ClientPlayNetworking.registerGlobalReceiver(AVATAR_SKIES, makeClientBoundHandler(AvatarSkiesRodPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(EFFECT, makeClientBoundHandler(BotaniaEffectPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(GOG_WORLD, makeClientBoundHandler(GogWorldPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ITEM_AGE, makeClientBoundHandler(ItemAgePacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(GAIA_GUARDIAN, makeClientBoundHandler(SpawnGaiaGuardianPacket.Handler::handle));
		ClientPlayNetworking.registerGlobalReceiver(ITEMS_REMAINING, makeClientBoundHandler(UpdateItemsRemainingPacket.Handler::handle));
	}

	private static <T extends BotaniaPacket> ClientPlayNetworking.PlayPayloadHandler<Payload<T>> makeClientBoundHandler(
			Consumer<T> handler) {
		return (payload, ctx) -> handler.accept(payload.packet());
	}

	private FabricPacketHandler() {}

}
