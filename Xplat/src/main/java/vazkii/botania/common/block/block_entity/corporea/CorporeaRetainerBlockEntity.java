/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity.corporea;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.corporea.*;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.internal.VanillaPacketDispatcher;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CorporeaRetainerBlockEntity extends BotaniaBlockEntity implements Wandable {
	private static final String TAG_REQUEST_X = "requestX";
	private static final String TAG_REQUEST_Y = "requestY";
	private static final String TAG_REQUEST_Z = "requestZ";
	private static final String TAG_REQUEST_TYPE = "requestType";
	private static final String TAG_REQUEST_COUNT = "requestCount";
	private static final String TAG_RETAIN_MISSING = "retainMissing";

	private static final Map<Identifier, Function<CompoundTag, ? extends CorporeaRequestMatcher>> corporeaMatcherDeserializers = new ConcurrentHashMap<>();
	private static final Map<Class<? extends CorporeaRequestMatcher>, Identifier> corporeaMatcherSerializers = new ConcurrentHashMap<>();

	private BlockPos requestPos = ManaBurst.NO_SOURCE;

	@Nullable
	private CorporeaRequestMatcher request;
	private int requestCount;
	private boolean retainMissing = false;

	public CorporeaRetainerBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CORPOREA_RETAINER, pos, state);
	}

	public void remember(BlockPos pos, CorporeaRequestMatcher request, int count, int missing) {
		this.requestPos = pos;
		this.request = request;
		this.requestCount = retainMissing ? missing : count;

		setChanged();
	}

	public void forget() {
		request = null;
		requestCount = 0;
	}

	public int getComparatorValue() {
		return CorporeaHelper.instance().signalStrengthForRequestSize(requestCount);
	}

	public boolean hasPendingRequest() {
		return request != null;
	}

	public void fulfilRequest() {
		if (!hasPendingRequest()) {
			return;
		}

		CorporeaSpark spark = CorporeaHelper.instance().getSparkForBlock(level, requestPos);
		if (spark != null) {
			BlockEntity te = spark.getSparkNode().getWorld().getBlockEntity(spark.getSparkNode().getPos());
			if (te instanceof CorporeaRequestor requestor) {
				requestor.doCorporeaRequest(request, requestCount, spark, null);

				forget();
				setChanged();
			}
		}
	}

	@Override
	public void writePacketNBT(CompoundTag cmp) {
		super.writePacketNBT(cmp);

		cmp.putInt(TAG_REQUEST_X, requestPos.getX());
		cmp.putInt(TAG_REQUEST_Y, requestPos.getY());
		cmp.putInt(TAG_REQUEST_Z, requestPos.getZ());

		Identifier reqType = request != null ? corporeaMatcherSerializers.get(request.getClass()) : null;

		if (reqType != null) {
			cmp.putString(TAG_REQUEST_TYPE, reqType.toString());
			request.writeToNBT(cmp);
			cmp.putInt(TAG_REQUEST_COUNT, requestCount);
		}
		cmp.putBoolean(TAG_RETAIN_MISSING, retainMissing);
	}

	@Override
	public void readPacketNBT(CompoundTag cmp) {
		super.readPacketNBT(cmp);

		int x = cmp.getIntOr(TAG_REQUEST_X, 0);
		int y = cmp.getIntOr(TAG_REQUEST_Y, 0);
		int z = cmp.getIntOr(TAG_REQUEST_Z, 0);
		requestPos = new BlockPos(x, y, z);

		Identifier reqType = Identifier.tryParse(cmp.getStringOr(TAG_REQUEST_TYPE, ""));
		if (reqType != null && corporeaMatcherDeserializers.containsKey(reqType)) {
			request = corporeaMatcherDeserializers.get(reqType).apply(cmp);
		} else {
			request = null;
		}
		requestCount = cmp.getIntOr(TAG_REQUEST_COUNT, 0);
		retainMissing = cmp.getBooleanOr(TAG_RETAIN_MISSING, false);
	}

	public static <T extends CorporeaRequestMatcher> void addCorporeaRequestMatcher(Identifier id, Class<T> clazz, Function<CompoundTag, T> deserializer) {
		corporeaMatcherSerializers.put(clazz, id);
		corporeaMatcherDeserializers.put(id, deserializer);
	}

	public static class WandHud implements WandHUD {
		private final CorporeaRetainerBlockEntity retainer;

		public WandHud(CorporeaRetainerBlockEntity retainer) {
			this.retainer = retainer;
		}

		@Override
		public void renderHUD(GuiGraphicsExtractor gui, Minecraft mc) {
			String mode = I18n.get("botaniamisc.retainer." + (retainer.retainMissing ? "retain_missing" : "retain_all"));
			int strWidth = mc.font.width(mode);
			int x = (mc.getWindow().getGuiScaledWidth() - strWidth) / 2;
			int y = mc.getWindow().getGuiScaledHeight() / 2 + 8;

			RenderHelper.renderHUDBox(gui, x - 2, y, x + strWidth + 2, y + 12);
			gui.text(mc.font, mode, x, y + 2, ChatFormatting.WHITE.getColor());
		}
	}

	@Override
	public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
		if (!level.isClientSide()) {
			retainMissing = !retainMissing;
			setChanged();
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		}
		return true;
	}
}
