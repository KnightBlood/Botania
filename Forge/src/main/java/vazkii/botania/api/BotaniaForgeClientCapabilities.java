package vazkii.botania.api;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.EntityCapability;

import vazkii.botania.api.block.WandHUD;

public final class BotaniaForgeClientCapabilities {
	public static final BlockCapability<WandHUD, Void> WAND_HUD = BlockCapability.createVoid(
			Identifier.fromNamespaceAndPath("botania", "wand_hud"), WandHUD.class);
	public static final EntityCapability<WandHUD, Void> ENTITY_WAND_HUD = EntityCapability.createVoid(
			Identifier.fromNamespaceAndPath("botania", "wand_hud_entity"), WandHUD.class);

	private BotaniaForgeClientCapabilities() {}
}
