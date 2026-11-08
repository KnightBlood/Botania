package vazkii.botania.xplat;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.ServiceUtil;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public interface ClientXplatAbstractions {
	Identifier FLOATING_FLOWER_MODEL_LOADER_ID = prefix("floating_flower");
	Identifier MANA_GUN_MODEL_LOADER_ID = prefix("mana_gun");

	// Event firing
	void fireRenderTinyPotato(BlockEntity potato, Component name, float tickDelta, PoseStack ms, MultiBufferSource buffers, int light, int overlay);

	// Networking
	void sendToServer(BotaniaPacket packet);

	// Capability access
	@Nullable
	WandHUD findWandHud(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be);

	@Nullable
	WandHUD findWandHud(Entity entity);

	// Rendering stuff
	BlockStateModel wrapPlatformModel(BlockStateModel original);
	void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap);
	void restoreLastFilter(AbstractTexture texture);
	void tessellateBlock(Level level, BlockState state, BlockPos pos, PoseStack ps, MultiBufferSource buffers, int overlay);
	/**
	 * Submits a custom {@link GuiElementRenderState} through the given GUI extractor.
	 * NeoForge has an extension method for this; Fabric goes through the underlying
	 * {@code GuiRenderState} (see {@code GuiGraphicsExtractorAccessor}).
	 */
	void submitGuiElementRenderState(GuiGraphicsExtractor gui, GuiElementRenderState state);
	/**
	 * Returns the current scissor rectangle (top of the extractor's scissor stack), or null if
	 * no scissor is active. NeoForge exposes this as an extension method; Fabric needs an
	 * accessor mixin ({@code GuiGraphicsExtractorScissorAccessor}, declared in the target's
	 * package to reach the package-private {@code ScissorStack} nested class).
	 */
	@Nullable
	ScreenRectangle peekGuiScissor(GuiGraphicsExtractor gui);
	/** Marks an animated sprite as "active" for Sodium, if present. */
	void markSpriteActive(TextureAtlasSprite sprite);

	ClientXplatAbstractions INSTANCE = ServiceUtil.findService(ClientXplatAbstractions.class, null);

	static ClientXplatAbstractions instance() {
		return INSTANCE;
	}
}
