/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.helper.VecHelper;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.Random;

public class SkyblockSkyRenderer {

	private static final Identifier textureSkybox = Identifier.parse(ResourcesLib.MISC_SKYBOX);
	private static final Identifier textureRainbow = Identifier.parse(ResourcesLib.MISC_RAINBOW);
	private static final Identifier[] planetTextures = new Identifier[] {
			Identifier.parse(ResourcesLib.MISC_PLANET + "0.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "1.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "2.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "3.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "4.png"),
			Identifier.parse(ResourcesLib.MISC_PLANET + "5.png")
	};
	private static final RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
	private static final int STAR_COUNT = 1500;

	/**
	 * Equivalent of the removed 1.20.1 {@code Level.getTimeOfDay}, based on the overworld clock.
	 */
	private static float timeOfDay(ClientLevel world) {
		long dayTime = world.getOverworldClockTime();
		double d0 = (double) dayTime / 24000.0D - 0.25D;
		d0 -= Math.floor(d0);
		double d1 = 0.5D - Math.cos(d0 * Math.PI) / 2.0D;
		return (float) (d0 * 2.0D + d1) / 3.0F;
	}

	private static GpuBuffer starBuffer;
	private static int starIndexCount;

	private static void buildStars() {
		RandomSource random = RandomSource.createThreadLocalInstance(10842L);
		float starDistance = 100.0F;

		try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * STAR_COUNT * 4)) {
			BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);

			for (int i = 0; i < STAR_COUNT; i++) {
				float x = random.nextFloat() * 2.0F - 1.0F;
				float y = random.nextFloat() * 2.0F - 1.0F;
				float z = random.nextFloat() * 2.0F - 1.0F;
				float starSize = 0.15F + random.nextFloat() * 0.1F;
				float lengthSq = Mth.lengthSquared(x, y, z);
				if (!(lengthSq <= 0.010000001F) && !(lengthSq >= 1.0F)) {
					Vector3f starCenter = new Vector3f(x, y, z).normalize(starDistance);
					float zRot = (float) (random.nextDouble() * (float) Math.PI * 2.0);
					Matrix3f rotation = new Matrix3f().rotateTowards(new Vector3f(starCenter).negate(), new Vector3f(0.0F, 1.0F, 0.0F)).rotateZ(-zRot);
					bufferBuilder.addVertex(new Vector3f(starSize, -starSize, 0.0F).mul(rotation).add(starCenter));
					bufferBuilder.addVertex(new Vector3f(starSize, starSize, 0.0F).mul(rotation).add(starCenter));
					bufferBuilder.addVertex(new Vector3f(-starSize, starSize, 0.0F).mul(rotation).add(starCenter));
					bufferBuilder.addVertex(new Vector3f(-starSize, -starSize, 0.0F).mul(rotation).add(starCenter));
				}
			}

			try (MeshData mesh = bufferBuilder.buildOrThrow()) {
				starIndexCount = mesh.drawState().indexCount();
				starBuffer = RenderSystem.getDevice().createBuffer(() -> "Botania stars vertex buffer", 40, mesh.vertexBuffer());
			}
		}
	}

	/**
	 * Renders a dynamic POSITION_TEX quad mesh in one draw call, tinted via dynamic transform uniforms.
	 */
	private static void drawMesh(Identifier texture, PoseStack ms, float r, float g, float b, float a, BufferBuilder builder, String passName) {
		try (MeshData mesh = builder.buildOrThrow()) {
			GpuBuffer vertexBuffer = DefaultVertexFormat.POSITION_TEX.uploadImmediateVertexBuffer(mesh.vertexBuffer());
			int indexCount = mesh.drawState().indexCount();
			GpuBuffer indexBuffer = quadIndices.getBuffer(indexCount);
			AbstractTexture tex = Minecraft.getInstance().getTextureManager().getTexture(texture);
			GpuTextureView color = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
			GpuTextureView depth = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();

			Matrix4fStack mvs = RenderSystem.getModelViewStack();
			mvs.pushMatrix();
			mvs.mul(ms.last().pose());
			GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
					.writeTransform(mvs, new Vector4f(r, g, b, a), new Vector3f(), new Matrix4f());
			mvs.popMatrix();

			try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
					.createRenderPass(() -> passName, color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
				renderPass.setPipeline(RenderPipelines.CELESTIAL);
				RenderSystem.bindDefaultUniforms(renderPass);
				renderPass.setUniform("DynamicTransforms", dynamicTransforms);
				renderPass.bindTexture("Sampler0", tex.getTextureView(), tex.getSampler());
				renderPass.setVertexBuffer(0, vertexBuffer);
				renderPass.setIndexBuffer(indexBuffer, quadIndices.type());
				renderPass.drawIndexed(0, 0, indexCount, 1);
			}
		}
	}

	public static void renderExtra(PoseStack ms, ClientLevel world, float partialTicks, float insideVoid) {
		// Botania - Begin extra stuff
		Tesselator tessellator = Tesselator.getInstance();
		float rain = 1.0F - world.getRainLevel(partialTicks);
		float celAng = timeOfDay(world);
		float effCelAng = celAng;
		if (celAng > 0.5) {
			effCelAng = 0.5F - (celAng - 0.5F);
		}

		// === Planets
		float scale = 20F;
		float lowA = Math.max(0F, effCelAng - 0.3F) * rain;
		float a = Math.max(0.1F, lowA);
		float alpha = a * 4 * (1F - insideVoid);

		ms.pushPose();
		ms.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(90), 0.5F, 0.5F, 0F));
		for (int p = 0; p < planetTextures.length; p++) {
			Matrix4f mat = ms.last().pose();
			BufferBuilder builder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			builder.addVertex(mat, -scale, 100, -scale).setUv(0.0F, 0.0F);
			builder.addVertex(mat, scale, 100, -scale).setUv(1.0F, 0.0F);
			builder.addVertex(mat, scale, 100, scale).setUv(1.0F, 1.0F);
			builder.addVertex(mat, -scale, 100, scale).setUv(0.0F, 1.0F);
			drawMesh(planetTextures[p], ms, 1F, 1F, 1F, alpha, builder, "Botania sky planet" + p);

			switch (p) {
				case 0 -> {
					ms.mulPose(VecHelper.rotateX(70));
					scale = 12F;
				}
				case 1 -> {
					ms.mulPose(VecHelper.rotateZ(120));
					scale = 15F;
				}
				case 2 -> {
					ms.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(80), 1, 0, 1));
					scale = 25F;
				}
				case 3 -> {
					ms.mulPose(VecHelper.rotateZ(100));
					scale = 10F;
				}
				case 4 -> {
					ms.mulPose(new Quaternionf().rotateAxis(VecHelper.toRadians(-60), 1, 0, 0.5F));
					scale = 40F;
				}
			}
		}
		ms.popPose();

		// === Rays
		scale = 20F;
		a = lowA;
		ms.pushPose();
		ms.translate(0, -1, 0);
		ms.mulPose(VecHelper.rotateX(220));
		float r = 1F;
		float g = 1F;
		float b = 1F;
		int angles = 90;
		float y = 2F;
		float y0 = 0F;
		float uPer = 1F / 360F;
		float anglePer = 360F / angles;
		double fuzzPer = Math.PI * 10 / angles;
		float rotSpeed = 1F;
		float rotSpeedMod = 0.4F;

		for (int p = 0; p < 3; p++) {
			float baseAngle = rotSpeed * rotSpeedMod * (ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks);
			ms.mulPose(VecHelper.rotateY((ClientTickHandler.ticksInGame + ClientTickHandler.partialTicks) * 0.25F * rotSpeed * rotSpeedMod));

			Matrix4f mat = ms.last().pose();
			BufferBuilder builder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
			for (int i = 0; i < angles; i++) {
				int j = i;
				if (i % 2 == 0) {
					j--;
				}

				float ang = j * anglePer + baseAngle;
				float xp = (float) Math.cos(ang * Math.PI / 180F) * scale;
				float zp = (float) Math.sin(ang * Math.PI / 180F) * scale;
				float yo = (float) Math.sin(fuzzPer * j) * 1;

				float ut = ang * uPer;
				if (i % 2 == 0) {
					builder.addVertex(mat, xp, yo + y0 + y, zp).setUv(ut, 1F);
					builder.addVertex(mat, xp, yo + y0, zp).setUv(ut, 0);
				} else {
					builder.addVertex(mat, xp, yo + y0, zp).setUv(ut, 0);
					builder.addVertex(mat, xp, yo + y0 + y, zp).setUv(ut, 1F);
				}

			}
			drawMesh(textureSkybox, ms, r, g, b, a, builder, "Botania sky rays" + p);

			switch (p) {
				case 0 -> {
					ms.mulPose(VecHelper.rotateX(20));
					r = 1F;
					g = 0.4F;
					b = 0.4F;
					fuzzPer = Math.PI * 14 / angles;
					rotSpeed = 0.2F;
				}
				case 1 -> {
					ms.mulPose(VecHelper.rotateX(50));
					r = 0.4F;
					g = 1F;
					b = 0.7F;
					fuzzPer = Math.PI * 6 / angles;
					rotSpeed = 2F;
				}
			}
		}
		ms.popPose();

		// === Rainbow
		ms.pushPose();
		scale = 10F;
		float effCelAng1 = celAng;
		if (effCelAng1 > 0.25F) {
			effCelAng1 = 1F - effCelAng1;
		}
		effCelAng1 = 0.25F - Math.min(0.25F, effCelAng1);

		long time = world.getOverworldClockTime() + 1000;
		int day = (int) (time / 24000L);
		Random rand = new Random(day * 0xFF);
		float angle1 = rand.nextFloat() * 360F;
		float angle2 = rand.nextFloat() * 360F;
		float rainbowAlpha = effCelAng1 * (1F - insideVoid);
		ms.mulPose(VecHelper.rotateY(angle1));
		ms.mulPose(VecHelper.rotateZ(angle2));

		Matrix4f mat = ms.last().pose();
		BufferBuilder builder = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
		for (int i = 0; i < angles; i++) {
			int j = i;
			if (i % 2 == 0) {
				j--;
			}

			float ang = j * anglePer;
			float xp = (float) Math.cos(ang * Math.PI / 180F) * scale;
			float zp = (float) Math.sin(ang * Math.PI / 180F) * scale;
			float yo = 0;

			float ut = ang * uPer;
			if (i % 2 == 0) {
				builder.addVertex(mat, xp, yo + y0 + y, zp).setUv(ut, 1F);
				builder.addVertex(mat, xp, yo + y0, zp).setUv(ut, 0);
			} else {
				builder.addVertex(mat, xp, yo + y0, zp).setUv(ut, 0);
				builder.addVertex(mat, xp, yo + y0 + y, zp).setUv(ut, 1F);
			}

		}
		drawMesh(textureRainbow, ms, 1F, 1F, 1F, rainbowAlpha, builder, "Botania sky rainbow");
		ms.popPose();
	}

	public static void renderStars(PoseStack ms, float partialTicks) {
		if (starBuffer == null) {
			buildStars();
		}
		Minecraft mc = Minecraft.getInstance();
		float rain = 1.0F - mc.level.getRainLevel(partialTicks);
		float celAng = timeOfDay(mc.level);
		float effCelAng = celAng;
		if (celAng > 0.5) {
			effCelAng = 0.5F - (celAng - 0.5F);
		}
		float alpha = rain * Math.max(0.1F, effCelAng * 2);

		float t = (ClientTickHandler.ticksInGame + partialTicks + 2000) * 0.005F;
		GpuTextureView color = mc.getMainRenderTarget().getColorTextureView();
		GpuTextureView depth = mc.getMainRenderTarget().getDepthTextureView();
		GpuBuffer indexBuffer = quadIndices.getBuffer(starIndexCount);

		Matrix4fStack mvs = RenderSystem.getModelViewStack();
		mvs.pushMatrix();
		mvs.mul(ms.last().pose());

		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
				.createRenderPass(() -> "Botania stars", color, OptionalInt.empty(), depth, OptionalDouble.empty())) {
			renderPass.setPipeline(RenderPipelines.STARS);
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.setVertexBuffer(0, starBuffer);
			renderPass.setIndexBuffer(indexBuffer, quadIndices.type());

			ms.pushPose();
			ms.mulPose(VecHelper.rotateY(t * 3));
			drawStars(renderPass, ms, 1F, 1F, 1F, alpha);
			ms.popPose();

			ms.pushPose();
			ms.mulPose(VecHelper.rotateY(t));
			drawStars(renderPass, ms, 0.5F, 1F, 1F, alpha);
			ms.popPose();

			ms.pushPose();
			ms.mulPose(VecHelper.rotateY(t * 2));
			drawStars(renderPass, ms, 1F, 0.75F, 0.75F, alpha);
			ms.popPose();

			ms.pushPose();
			ms.mulPose(VecHelper.rotateZ(t * 3));
			drawStars(renderPass, ms, 1F, 1F, 1F, 0.25F * alpha);
			ms.popPose();

			ms.pushPose();
			ms.mulPose(VecHelper.rotateZ(t));
			drawStars(renderPass, ms, 0.5F, 1F, 1F, 0.25F * alpha);
			ms.popPose();

			ms.pushPose();
			ms.mulPose(VecHelper.rotateZ(t * 2));
			drawStars(renderPass, ms, 1F, 0.75F, 0.75F, 0.25F * alpha);
			ms.popPose();
		}

		mvs.popMatrix();
	}

	private static void drawStars(RenderPass renderPass, PoseStack ms, float r, float g, float b, float a) {
		GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
				.writeTransform(ms.last().pose(), new Vector4f(r, g, b, a), new Vector3f(), new Matrix4f());
		renderPass.setUniform("DynamicTransforms", dynamicTransforms);
		renderPass.drawIndexed(0, 0, starIndexCount, 1);
	}

}
