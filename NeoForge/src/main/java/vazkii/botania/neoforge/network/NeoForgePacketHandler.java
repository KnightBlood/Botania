package vazkii.botania.neoforge.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import vazkii.botania.network.BotaniaPacket;
import vazkii.botania.network.TriConsumer;
import vazkii.botania.network.clientbound.AvatarSkiesRodPacket;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.network.clientbound.GogWorldPacket;
import vazkii.botania.network.clientbound.ItemAgePacket;
import vazkii.botania.network.clientbound.SpawnGaiaGuardianPacket;
import vazkii.botania.network.clientbound.UpdateItemsRemainingPacket;
import vazkii.botania.network.serverbound.DodgePacket;
import vazkii.botania.network.serverbound.IndexKeybindRequestPacket;
import vazkii.botania.network.serverbound.IndexStringRequestPacket;
import vazkii.botania.network.serverbound.JumpPacket;
import vazkii.botania.network.serverbound.LeftClickPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * NeoForge 26.1 registration for Xplat's {@link BotaniaPacket}s.
 * Each packet is wrapped in a {@link BotaniaPayload} which adapts the
 * encode/decode-on-FriendlyByteBuf style to the CustomPacketPayload system.
 */
public class NeoForgePacketHandler {
	public record BotaniaPayload<T extends BotaniaPacket>(T packet, Type<BotaniaPayload<T>> type)
			implements
				CustomPacketPayload {

		private void write(RegistryFriendlyByteBuf buf) {
			packet.encode(buf);
		}
	}

	private record PayloadEntry<T extends BotaniaPacket>(
			Type<BotaniaPayload<T>> type,
			Function<RegistryFriendlyByteBuf, T> decoder) {
	}

	private static final Map<Class<? extends BotaniaPacket>, PayloadEntry<? extends BotaniaPacket>> PAYLOADS = new HashMap<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static CustomPacketPayload wrap(BotaniaPacket packet) {
		PayloadEntry entry = PAYLOADS.get(packet.getClass());
		if (entry == null) {
			throw new IllegalArgumentException("Unregistered packet type: " + packet.getClass());
		}
		return new BotaniaPayload(packet, entry.type());
	}

	public static void register(RegisterPayloadHandlersEvent evt) {
		PayloadRegistrar registrar = evt.registrar("1");

		// Serverbound
		registerServerbound(registrar, "dodge", DodgePacket.class, DodgePacket::decode, DodgePacket::handle);
		registerServerbound(registrar, "index_keybind_request", IndexKeybindRequestPacket.class,
				IndexKeybindRequestPacket::decode, IndexKeybindRequestPacket::handle);
		registerServerbound(registrar, "index_string_request", IndexStringRequestPacket.class,
				IndexStringRequestPacket::decode, IndexStringRequestPacket::handle);
		registerServerbound(registrar, "jump", JumpPacket.class, JumpPacket::decode, JumpPacket::handle);
		registerServerbound(registrar, "left_click", LeftClickPacket.class, LeftClickPacket::decode, LeftClickPacket::handle);

		// Clientbound
		registerClientbound(registrar, "avatar_skies_rod", AvatarSkiesRodPacket.class,
				AvatarSkiesRodPacket::decode, AvatarSkiesRodPacket.Handler::handle);
		registerClientbound(registrar, "effect", BotaniaEffectPacket.class,
				BotaniaEffectPacket::decode, BotaniaEffectPacket.Handler::handle);
		registerClientbound(registrar, "gog_world", GogWorldPacket.class,
				GogWorldPacket::decode, GogWorldPacket.Handler::handle);
		registerClientbound(registrar, "item_age", ItemAgePacket.class,
				ItemAgePacket::decode, ItemAgePacket.Handler::handle);
		registerClientbound(registrar, "spawn_gaia_guardian", SpawnGaiaGuardianPacket.class,
				SpawnGaiaGuardianPacket::decode, SpawnGaiaGuardianPacket.Handler::handle);
		registerClientbound(registrar, "update_items_remaining", UpdateItemsRemainingPacket.class,
				UpdateItemsRemainingPacket::decode, UpdateItemsRemainingPacket.Handler::handle);
	}

	private static <T extends BotaniaPacket> void registerServerbound(PayloadRegistrar registrar, String name,
			Class<T> clazz, Function<RegistryFriendlyByteBuf, T> decoder,
			TriConsumer<T, MinecraftServer, ServerPlayer> handler) {
		var type = new Type<BotaniaPayload<T>>(prefix(name));
		PAYLOADS.put(clazz, new PayloadEntry<>(type, decoder));
		registrar.playToServer(type, codec(type, decoder), (payload, ctx) -> {
			if (ctx.player() instanceof ServerPlayer sender) {
				// 26.1: ServerPlayer.server is private now
				handler.accept(payload.packet(), ServerLifecycleHooks.getCurrentServer(), sender);
			}
		});
	}

	private static <T extends BotaniaPacket> void registerClientbound(PayloadRegistrar registrar, String name,
			Class<T> clazz, Function<RegistryFriendlyByteBuf, T> decoder, Consumer<T> handler) {
		var type = new Type<BotaniaPayload<T>>(prefix(name));
		PAYLOADS.put(clazz, new PayloadEntry<>(type, decoder));
		registrar.playToClient(type, codec(type, decoder), (payload, ctx) -> handler.accept(payload.packet()));
	}

	private static <T extends BotaniaPacket> StreamCodec<RegistryFriendlyByteBuf, BotaniaPayload<T>> codec(
			Type<BotaniaPayload<T>> type, Function<RegistryFriendlyByteBuf, T> decoder) {
		return StreamCodec.ofMember(BotaniaPayload<T>::write, buf -> new BotaniaPayload<>(decoder.apply(buf), type));
	}
}
