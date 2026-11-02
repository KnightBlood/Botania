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
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import vazkii.botania.common.entity.GaiaGuardianEntity;
import vazkii.botania.network.BotaniaPacket;

import java.util.UUID;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public record SpawnGaiaGuardianPacket(ClientboundAddEntityPacket inner, int playerCount, boolean hardMode,
		BlockPos source, UUID bossInfoId) implements BotaniaPacket {

	public static final Identifier ID = prefix("spg");

	@Override
	public void encode(FriendlyByteBuf buf) {
		ClientboundAddEntityPacket inner = inner();
		buf.writeVarInt(inner.getId());
		buf.writeUUID(inner.getUUID());
		buf.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(inner.getType()));
		buf.writeDouble(inner.getX());
		buf.writeDouble(inner.getY());
		buf.writeDouble(inner.getZ());
		Vec3.LP_STREAM_CODEC.encode(buf, inner.getMovement());
		buf.writeByte(Mth.packDegrees(inner.getXRot()));
		buf.writeByte(Mth.packDegrees(inner.getYRot()));
		buf.writeByte(Mth.packDegrees(inner.getYHeadRot()));
		buf.writeVarInt(inner.getData());
		buf.writeVarInt(playerCount());
		buf.writeBoolean(hardMode());
		buf.writeBlockPos(source());
		buf.writeUUID(bossInfoId());
	}

	@Override
	public Identifier getFabricId() {
		return ID;
	}

	public static SpawnGaiaGuardianPacket decode(FriendlyByteBuf buf) {
		var inner = new ClientboundAddEntityPacket(
				buf.readVarInt(),
				buf.readUUID(),
				buf.readDouble(),
				buf.readDouble(),
				buf.readDouble(),
				Mth.unpackDegrees(buf.readByte()),
				Mth.unpackDegrees(buf.readByte()),
				BuiltInRegistries.ENTITY_TYPE.byId(buf.readVarInt()),
				buf.readVarInt(),
				Vec3.LP_STREAM_CODEC.decode(buf),
				Mth.unpackDegrees(buf.readByte())
		);
		return new SpawnGaiaGuardianPacket(
				inner,
				buf.readVarInt(),
				buf.readBoolean(),
				buf.readBlockPos(),
				buf.readUUID()
		);
	}

	public static class Handler {
		public static void handle(SpawnGaiaGuardianPacket packet) {
			var inner = packet.inner();
			int playerCount = packet.playerCount();
			boolean hardMode = packet.hardMode();
			BlockPos source = packet.source();
			UUID bossInfoUuid = packet.bossInfoId();

			Minecraft.getInstance().execute(() -> {
				var player = Minecraft.getInstance().player;
				if (player != null) {
					player.connection.handleAddEntity(inner);
					Entity e = player.level().getEntity(inner.getId());
					if (e instanceof GaiaGuardianEntity dopple) {
						dopple.readSpawnData(playerCount, hardMode, source, bossInfoUuid);
					}
				}
			});
		}
	}
}
