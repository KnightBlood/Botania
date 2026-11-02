/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.helper;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;

import vazkii.botania.network.TriConsumer;
import vazkii.botania.xplat.BotaniaConfig;

import java.util.function.Consumer;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public class CoreShaders {
	private static Object starfieldShaderInstance;
	private static Object doppleganger;
	private static Object manaPool;
	private static Object terraPlate;
	private static Object enchanter;
	private static Object pylon;
	private static Object halo;
	private static Object filmGrainParticle;
	private static Object dopplegangerBar;

	// This is abstracted this way instead of just directly constructing the ShaderInstance
	// Because Fabric is cute and hides the ResourceProvider from modders (why?)
	public static void init(TriConsumer<Identifier, Object, Consumer<Object>> registrations) {
		registrations.accept(
				prefix("starfield"),
				null,
				inst -> starfieldShaderInstance = inst
		);
		registrations.accept(
				prefix("doppleganger"),
				null,
				inst -> doppleganger = inst
		);
		registrations.accept(
				prefix("mana_pool"),
				null,
				inst -> manaPool = inst
		);
		registrations.accept(
				prefix("terra_plate_rune"),
				null,
				inst -> terraPlate = inst
		);
		registrations.accept(
				prefix("enchanter_rune"),
				null,
				inst -> enchanter = inst
		);
		registrations.accept(
				prefix("pylon"),
				null,
				inst -> pylon = inst
		);
		registrations.accept(
				prefix("halo"),
				null,
				inst -> halo = inst
		);
		registrations.accept(
				prefix("film_grain_particle"),
				null,
				inst -> filmGrainParticle = inst
		);
		registrations.accept(
				prefix("doppleganger_bar"),
				null,
				inst -> dopplegangerBar = inst
		);
	}

	public static Object starfield() {
		return starfieldShaderInstance;
	}

	public static Object doppleganger() {
		if (BotaniaConfig.client().useShaders()) {
			return doppleganger;
		} else {
			return null;
		}
	}

	public static Object manaPool() {
		if (BotaniaConfig.client().useShaders()) {
			return manaPool;
		} else {
			return null;
		}
	}

	public static Object terraPlate() {
		if (BotaniaConfig.client().useShaders()) {
			return terraPlate;
		} else {
			return null;
		}
	}

	public static Object enchanter() {
		if (BotaniaConfig.client().useShaders()) {
			return enchanter;
		} else {
			return null;
		}
	}

	public static Object pylon() {
		if (BotaniaConfig.client().useShaders()) {
			return pylon;
		} else {
			return null;
		}
	}

	public static Object halo() {
		if (BotaniaConfig.client().useShaders()) {
			return halo;
		} else {
			return null;
		}
	}

	public static Object filmGrainParticle() {
		if (BotaniaConfig.client().useShaders()) {
			return filmGrainParticle;
		} else {
			return null;
		}
	}

	public static Object dopplegangerBar() {
		if (BotaniaConfig.client().useShaders()) {
			return dopplegangerBar;
		} else {
			return null;
		}
	}
}
