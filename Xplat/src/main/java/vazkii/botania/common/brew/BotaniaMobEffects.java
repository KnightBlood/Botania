/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.brew;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import vazkii.botania.common.brew.effect.*;
import vazkii.botania.common.lib.LibPotionNames;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class BotaniaMobEffects {

	public static final Holder<MobEffect> soulCross = register(LibPotionNames.SOUL_CROSS, new SoulCrossMobEffect());
	public static final Holder<MobEffect> featherfeet = register(LibPotionNames.FEATHER_FEET, new FeatherfeetMobEffect());
	public static final Holder<MobEffect> emptiness = register(LibPotionNames.EMPTINESS, new EmptinessMobEffect());
	public static final Holder<MobEffect> bloodthrst = register(LibPotionNames.BLOODTHIRST, new BloodthirstMobEffect());
	public static final Holder<MobEffect> allure = register(LibPotionNames.ALLURE, new AllureMobEffect());
	public static final Holder<MobEffect> clear = register(LibPotionNames.CLEAR, new AbsolutionMobEffect());

	/**
	 * Forces this class to be loaded, which registers Botania's mob effects into the built-in
	 * MOB_EFFECT registry. Must be called before that registry is frozen.
	 */
	public static void init() {
	}

	private static Holder<MobEffect> register(String name, MobEffect effect) {
		return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, prefix(name), effect);
	}
}
