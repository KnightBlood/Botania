/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.render.block_entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.DragonHeadModel;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.block.SkullBlock;

import vazkii.botania.client.model.GaiaHeadModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Supplies the {@link GaiaHeadModel} and populates the per-viewer skull models it mirrors.
 */
public class GaiaHeadBlockEntityRenderer {
	public static Map<SkullBlock.Type, SkullModelBase> models = Collections.emptyMap();

	public static SkullModelBase createGaiaHeadModel(EntityModelSet modelSet) {
		initModels(modelSet);
		return new GaiaHeadModel(modelSet.bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	/**
	 * Bakes a head model for every vanilla skull type, so that the Gaia head can mirror the entity
	 * that is looking at it.
	 */
	public static synchronized void initModels(EntityModelSet modelSet) {
		if (!models.isEmpty()) {
			return;
		}

		Map<SkullBlock.Type, SkullModelBase> baked = new HashMap<>();
		baked.put(SkullBlock.Types.SKELETON, new SkullModel(modelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
		baked.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(modelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
		baked.put(SkullBlock.Types.PLAYER, new SkullModel(modelSet.bakeLayer(ModelLayers.PLAYER_HEAD)));
		baked.put(SkullBlock.Types.ZOMBIE, new SkullModel(modelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
		baked.put(SkullBlock.Types.CREEPER, new SkullModel(modelSet.bakeLayer(ModelLayers.CREEPER_HEAD)));
		baked.put(SkullBlock.Types.DRAGON, new DragonHeadModel(modelSet.bakeLayer(ModelLayers.DRAGON_SKULL)));
		baked.put(SkullBlock.Types.PIGLIN, new PiglinHeadModel(modelSet.bakeLayer(ModelLayers.PIGLIN_HEAD)));
		models = baked;
	}

	public static SkullBlock.Type getViewType() {
		Entity view = Minecraft.getInstance().getCameraEntity();
		SkullBlock.Type type = SkullBlock.Types.PLAYER;

		if (view instanceof Skeleton) {
			type = SkullBlock.Types.SKELETON;
		} else if (view instanceof WitherSkeleton) {
			type = SkullBlock.Types.WITHER_SKELETON;
		} else if (view instanceof WitherBoss) {
			type = SkullBlock.Types.WITHER_SKELETON;
		} else if (view instanceof Zombie) {
			type = SkullBlock.Types.ZOMBIE;
		} else if (view instanceof Creeper) {
			type = SkullBlock.Types.CREEPER;
		} else if (view instanceof EnderDragon) {
			type = SkullBlock.Types.DRAGON;
		}
		return type;
	}
}
