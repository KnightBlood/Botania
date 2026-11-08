/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.fx;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;

import org.jetbrains.annotations.NotNull;

public class FXWisp extends SingleQuadParticle {
	private final boolean depthTest;
	private final float moteParticleScale;
	private final int moteHalfLife;

	@SuppressWarnings("this-escape") // same pattern as 1.20.1; vanilla particle setters called from ctor
	public FXWisp(ClientLevel world, double d, double d1, double d2, double xSpeed, double ySpeed, double zSpeed,
			float size, float red, float green, float blue, boolean depthTest, float maxAgeMul, boolean noClip, float g) {
		// TextureAtlasSprite.EMPTY no longer exists in 26.1; the sprite is set by the factory right after construction
		super(world, d, d1, d2, 0.0D, 0.0D, 0.0D, null);
		// super applies wiggle to motion so set it here instead
		xd = xSpeed;
		yd = ySpeed;
		zd = zSpeed;
		rCol = red;
		gCol = green;
		bCol = blue;
		alpha = 0.375F;
		gravity = g;
		quadSize = (this.random.nextFloat() * 0.5F + 0.5F) * 2.0F * size;
		moteParticleScale = quadSize;
		lifetime = (int) (28D / (Math.random() * 0.3D + 0.7D) * maxAgeMul);
		this.depthTest = depthTest;

		moteHalfLife = lifetime / 2;
		setSize(0.01F, 0.01F);

		xo = x;
		yo = y;
		zo = z;
		this.hasPhysics = !noClip;
	}

	@Override
	public float getQuadSize(float p_217561_1_) {
		float agescale = (float) age / (float) moteHalfLife;
		if (agescale > 1F) {
			agescale = 2 - agescale;
		}

		quadSize = moteParticleScale * agescale * 0.5F;
		return quadSize;
	}

	@Override
	protected int getLightCoords(float partialTicks) {
		return 0xF000F0;
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

	// [VanillaCopy] of super, without drag when onGround is true
	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;

		if (this.age++ >= this.lifetime) {
			this.remove();
		}

		this.yd -= this.gravity;
		this.move(this.xd, this.yd, this.zd);
		if (gravity == 0.0f) {
			this.xd *= 0.98;
			this.yd *= 0.98;
			this.zd *= 0.98;
		}
	}

	public void setGravity(float value) {
		gravity = value;
	}

}
