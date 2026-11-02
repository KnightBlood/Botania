package vazkii.botania.forge.client;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.NeoForge;

import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.item.TinyPotatoRenderEvent;
import vazkii.botania.forge.integration.rubidium.RubidiumHelper;
import vazkii.botania.forge.network.ForgePacketHandler;
import vazkii.botania.network.BotaniaPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;

public class ForgeClientXplatImpl implements ClientXplatAbstractions {
	@Override
	public void fireRenderTinyPotato(BlockEntity potato, Component name, float tickDelta, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		NeoForge.EVENT_BUS.post(new TinyPotatoRenderEvent(potato, name, tickDelta, ms, buffers, light, overlay));
	}

	@Override
	public void sendToServer(BotaniaPacket packet) {
		var connection = Minecraft.getInstance().getConnection();
		if (connection != null) {
			connection.send(ForgePacketHandler.wrap(packet));
		}
	}

	@Nullable
	@Override
	public WandHUD findWandHud(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity be) {
		return BotaniaForgeClientCapabilities.WAND_HUD.getCapability(level, pos, state, be, null);
	}

	@Nullable
	@Override
	public WandHUD findWandHud(Entity entity) {
		return BotaniaForgeClientCapabilities.ENTITY_WAND_HUD.getCapability(entity, null);
	}

	@Override
	public BlockStateModel wrapPlatformModel(BlockStateModel original) {
		return new ForgePlatformModel(original);
	}

	@Override
	public void setFilterSave(AbstractTexture texture, boolean filter, boolean mipmap) {
		// 26.1: filtering is sampler state on GpuTexture; per-texture blur toggle no longer exists.
	}

	@Override
	public void restoreLastFilter(AbstractTexture texture) {
	}

	@Override
	public void tessellateBlock(Level level, BlockState state, BlockPos pos, PoseStack ps, MultiBufferSource buffers, int overlay) {
		var mc = Minecraft.getInstance();
		BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
		// Adapt the server-side level to the client-only render view vanilla expects here.
		BlockAndTintGetter view = new BlockAndTintGetter() {
			@Override
			public int getHeight() {
				return level.getHeight();
			}

			@Override
			public int getMinY() {
				return level.getMinY();
			}

			@Override
			public CardinalLighting cardinalLighting() {
				return level instanceof ClientLevel cl ? cl.cardinalLighting() : CardinalLighting.DEFAULT;
			}

			@Override
			public int getBlockTint(BlockPos p, ColorResolver resolver) {
				return level instanceof ClientLevel cl ? cl.getBlockTint(p, resolver) : 0;
			}

			@Override
			public LevelLightEngine getLightEngine() {
				return level.getLightEngine();
			}

			@Override
			public BlockEntity getBlockEntity(BlockPos p) {
				return level.getBlockEntity(p);
			}

			@Override
			public BlockState getBlockState(BlockPos p) {
				return level.getBlockState(p);
			}

			@Override
			public FluidState getFluidState(BlockPos p) {
				return level.getFluidState(p);
			}
		};
		// Same construction vanilla's SectionCompiler uses; BE rendering wants AO-aware quads.
		var renderer = new ModelBlockRenderer(true, true, mc.getBlockColors());
		renderer.tesselateBlock((x, y, z, quad, instance) -> {
			var info = quad.materialInfo();
			RenderType type = info == null ? RenderTypes.cutoutMovingBlock()
					: switch (info.layer()) {
						case SOLID -> RenderTypes.solidMovingBlock();
						case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
						default -> RenderTypes.cutoutMovingBlock();
					};
			buffers.getBuffer(type).putBakedQuad(ps.last(), quad, instance);
		}, 0F, 0F, 0F, view, pos, state, model, state.getSeed(pos));
	}

	@Override
	public void markSpriteActive(TextureAtlasSprite sprite) {
		if (rubidiumLoaded.get()) {
			RubidiumHelper.markSpriteActive(sprite);
		}
	}

	@Override
	public void submitGuiElementRenderState(net.minecraft.client.gui.GuiGraphicsExtractor gui, net.minecraft.client.renderer.state.gui.GuiElementRenderState state) {
		// NeoForge extension method; it also applies the extractor's current scissor stack.
		gui.submitGuiElementRenderState(state);
	}
	
	@Override
	public net.minecraft.client.gui.navigation.ScreenRectangle peekGuiScissor(net.minecraft.client.gui.GuiGraphicsExtractor gui) {
		// NeoForge extension method
		return gui.peekScissorStack();
	}

	private final Supplier<Boolean> rubidiumLoaded = Suppliers.memoize(() -> XplatAbstractions.INSTANCE.isModLoaded("rubidium"));
}
