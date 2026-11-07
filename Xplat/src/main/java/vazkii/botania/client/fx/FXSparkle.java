/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import vazkii.botania.xplat.ClientXplatAbstractions;

public class FXSparkle extends SingleQuadParticle {
	private final boolean corrupt;
	public final boolean fake;
	public final int particle = 16;
	private final boolean slowdown = true;
	private final SpriteSet sprite;

	@SuppressWarnings("this-escape") // same pattern as 1.20.1; vanilla particle setters called from ctor
	public FXSparkle(ClientLevel world, double x, double y, double z, float size,
			float red, float green, float blue, int m,
			boolean fake, boolean noClip, boolean corrupt, SpriteSet sprite) {
		super(world, x, y, z, 0.0D, 0.0D, 0.0D, sprite.first());
		rCol = red;
		gCol = green;
		bCol = blue;
		alpha = 0.75F;
		gravity = 0;
		xd = yd = zd = 0;
		quadSize = (this.random.nextFloat() * 0.5F + 0.5F) * 0.2F * size;
		lifetime = 3 * m;
		setSize(0.01F, 0.01F);
		xo = x;
		yo = y;
		zo = z;
		this.fake = fake;
		this.corrupt = corrupt;
		this.hasPhysics = !fake && !noClip;
		this.sprite = sprite;
		setSpriteFromAge(sprite);
	}

	@Override
	public float getQuadSize(float partialTicks) {
		return quadSize * (lifetime - age + 1) / (float) lifetime;
	}

	@Override
	public void tick() {
		setSpriteFromAge(sprite);
		xo = x;
		yo = y;
		zo = z;

		if (age++ >= lifetime) {
			remove();
		}

		yd -= 0.04D * gravity;

		if (hasPhysics && !fake) {
			wiggleAround(x, (getBoundingBox().minY + getBoundingBox().maxY) / 2.0D, z);
		}

		this.move(xd, yd, zd);

		if (slowdown) {
			xd *= 0.908000001907348633D;
			yd *= 0.908000001907348633D;
			zd *= 0.908000001907348633D;

			if (onGround) {
				xd *= 0.69999998807907104D;
				zd *= 0.69999998807907104D;
			}
		}

		if (fake && age > 1) {
			remove();
		}
	}

	@NotNull
	@Override
	public ParticleRenderType getGroup() {
		return ParticleRenderType.SINGLE_QUADS;
	}

	@Override
	protected SingleQuadParticle.Layer getLayer() {
		return SingleQuadParticle.Layer.TRANSLUCENT;
	}

	public void setGravity(float value) {
		gravity = value;
	}

	// [VanillaCopy] Entity.moveTowardClosestSpace with tweaks
	private void wiggleAround(double x, double y, double z) {
		BlockPos blockpos = BlockPos.containing(x, y, z);
		Vec3 Vector3d = new Vec3(x - (double) blockpos.getX(), y - (double) blockpos.getY(), z - (double) blockpos.getZ());
		BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
		Direction direction = Direction.UP;
		double d0 = Double.MAX_VALUE;

		for (Direction direction1 : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, Direction.UP }) {
			blockpos$mutable.set(blockpos).move(direction1);
			if (!this.level.getBlockState(blockpos$mutable).isCollisionShapeFullBlock(this.level, blockpos$mutable)) {
				double d1 = Vector3d.get(direction1.getAxis());
				double d2 = direction1.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D - d1 : d1;
				if (d2 < d0) {
					d0 = d2;
					direction = direction1;
				}
			}
		}

		// Botania - made multiplier and add both smaller
		float f = this.random.nextFloat() * 0.05F + 0.025F;
		float f1 = (float) direction.getAxisDirection().getStep();
		// Botania - Randomness in other axes as well
		float secondary = (random.nextFloat() - random.nextFloat()) * 0.1F;
		float secondary2 = (random.nextFloat() - random.nextFloat()) * 0.1F;
		if (direction.getAxis() == Direction.Axis.X) {
			xd = (double) (f1 * f);
			yd = secondary;
			zd = secondary2;
		} else if (direction.getAxis() == Direction.Axis.Y) {
			xd = secondary;
			yd = (double) (f1 * f);
			zd = secondary2;
		} else if (direction.getAxis() == Direction.Axis.Z) {
			xd = secondary;
			yd = secondary2;
			zd = (double) (f1 * f);
		}
	}

}
