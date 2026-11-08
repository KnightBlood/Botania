/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;

import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.List;
import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

/**
 * Botania's custom shaders, ported to Minecraft 26.1's {@link RenderPipeline} system.
 *
 * <p>
 * 26.1 removed {@code ShaderInstance} / {@code shaders/core/*.json}: shader sources are now
 * discovered automatically by {@code ShaderManager} from {@code assets/<ns>/shaders/**}{@code .vsh/.fsh},
 * while the pipeline (shader ids, samplers, UBOs, defines, vertex format, blend state) is a plain Java
 * object. There is no public vanilla registration method -- {@code RenderPipelines.register} is private --
 * but an unregistered pipeline still compiles lazily on first use ({@code GlDevice#getOrCompilePipeline}).
 * NeoForge exposes {@code RegisterRenderPipelinesEvent} for eager precompilation/validation; see
 * {@link #registerAll(Consumer)}. Fabric has no equivalent event (the old
 * {@code CoreShaderRegistrationCallback} is gone), so it relies on lazy compilation.
 *
 * <p>
 * All six pipelines reuse a vanilla {@link RenderPipeline.Snippet} so their vertex format stays
 * exactly the one the existing Botania render code writes. Only the fragment shader is replaced.
 * The GLSL sources were rewritten for GLSL 330 + UBOs: {@code GameTime} now lives in the
 * {@code Globals} UBO, {@code ColorModulator} in {@code DynamicTransforms}, fog in {@code Fog}.
 *
 * <p>
 * <b>Explicitly NOT ported (visual degradation, no silent fallback):</b>
 * {@code doppleganger}, {@code doppleganger_bar} and {@code film_grain_particle}. These are real
 * 1.20.1 features, not dead resources: on the {@code 1.20.x} branch they were driven by
 * {@code GaiaGuardianRenderer}, {@code PixieRenderer}, {@code GaiaHeadBlockEntityRenderer},
 * {@code BossBarHandler} and {@code FXSparkle#CORRUPT_RENDER}.
 * <ul>
 * <li>{@code doppleganger} / {@code doppleganger_bar} need per-draw custom float uniforms
 * ({@code BotaniaDisfiguration}, {@code BotaniaGrainIntensity}, {@code BotaniaHpFract}) that
 * 1.20.1 uploaded through {@code ShaderInstance#getUniform}. 26.1's {@code RenderType} draw
 * path only binds the default UBOs, so these need a custom UBO plus new upload hooks in the
 * renderers. The Gaia renderers currently use vanilla entity layers and {@code BossBarHandler}
 * uses {@code RenderPipelines.GUI_TEXTURED}.</li>
 * <li>{@code film_grain_particle} needs no custom uniform at all, but 26.1 replaced
 * {@code ParticleRenderType} with the {@code SingleQuadParticle} layer system.
 * {@code FXSparkle#CORRUPT_RENDER} was dropped in the port and every sparkle now renders via
 * vanilla {@code ParticleRenderType.SINGLE_QUADS}, so there is no longer a draw path that could
 * select this shader. Restoring it requires reintroducing a custom particle group first.</li>
 * </ul>
 * Their 1.20.1 GLSL/JSON sources were removed rather than left as dead files that are never
 * referenced. This is tracked as an explicit port gap, not a silent fallback.
 *
 * <p>
 * <b>Behavior difference:</b> on 1.20.1 {@code useShaders()} was evaluated per draw through a
 * supplier. A 26.1 {@code RenderType} stores its pipeline at construction time, so the config is
 * evaluated once when {@code RenderHelper} is class-initialized (i.e. at client startup).
 */
public final class CoreShaders {
	private static final RenderPipeline MANA_POOL_WATER = entity(prefix("pipeline/mana_pool_water"), prefix("core/mana_pool"));
	private static final RenderPipeline TERRA_PLATE = entity(prefix("pipeline/terra_plate_rune"), prefix("core/terra_plate_rune"));
	private static final RenderPipeline ENCHANTER = entity(prefix("pipeline/enchanter_rune"), prefix("core/enchanter_rune"));

	private static final RenderPipeline HALO = RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
			.withLocation(prefix("pipeline/halo"))
			.withFragmentShader(prefix("core/halo"))
			.withUniform("Globals", UniformType.UNIFORM_BUFFER)
			.build();

	private static final RenderPipeline PYLON = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
			.withLocation(prefix("pipeline/pylon"))
			.withFragmentShader(prefix("core/pylon"))
			.withSampler("Sampler1")
			.withUniform("Globals", UniformType.UNIFORM_BUFFER)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.withCull(false)
			.build();

	private static final RenderPipeline STARFIELD = RenderPipeline.builder(RenderPipelines.END_PORTAL_SNIPPET)
			.withLocation(prefix("pipeline/starfield"))
			.withFragmentShader(prefix("core/starfield"))
			.withShaderDefine("PORTAL_LAYERS", 15)
			.withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	private static final List<RenderPipeline> ALL = List.of(
			MANA_POOL_WATER, TERRA_PLATE, ENCHANTER, HALO, PYLON, STARFIELD
	);

	private CoreShaders() {}

	private static RenderPipeline entity(Identifier location, Identifier fragment) {
		return RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
				.withLocation(location)
				.withFragmentShader(fragment)
				// core/entity.vsh texelFetches Sampler1 (overlay) unless NO_OVERLAY is defined.
				.withSampler("Sampler1")
				// GameTime is not part of ENTITY_SNIPPET's UBOs, but RenderSystem.bindDefaultUniforms()
				// binds the Globals UBO on every RenderType draw.
				.withUniform("Globals", UniformType.UNIFORM_BUFFER)
				.build();
	}

	/** Forces class initialization. Fabric has no shader registration event, so this is all it needs. */
	public static void bootstrap() {}

	/** Registers every pipeline for eager precompilation/validation (NeoForge RegisterRenderPipelinesEvent). */
	public static void registerAll(Consumer<RenderPipeline> registrar) {
		ALL.forEach(registrar);
		// Explicitly observable: without this line in the log the shaders were silently
		// degraded on 26.1 (the old Fabric callback had simply been removed).
		BotaniaAPI.LOGGER.info("Registered {} Botania custom render pipelines for 26.1 shader precompilation", ALL.size());
	}

	private static boolean useShaders() {
		return BotaniaConfig.client().useShaders();
	}

	public static RenderPipeline manaPool() {
		return useShaders() ? MANA_POOL_WATER : RenderPipelines.ENTITY_SOLID;
	}

	public static RenderPipeline terraPlate() {
		return useShaders() ? TERRA_PLATE : RenderPipelines.ENTITY_SOLID;
	}

	public static RenderPipeline enchanter() {
		return useShaders() ? ENCHANTER : RenderPipelines.ENTITY_SOLID;
	}

	public static RenderPipeline halo() {
		return useShaders() ? HALO : RenderPipelines.ITEM_CUTOUT;
	}

	public static RenderPipeline pylon() {
		return useShaders() ? PYLON : RenderPipelines.ENTITY_TRANSLUCENT;
	}

	public static RenderPipeline starfield() {
		return useShaders() ? STARFIELD : RenderPipelines.END_SKY;
	}
}
