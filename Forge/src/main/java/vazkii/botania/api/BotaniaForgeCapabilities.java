package vazkii.botania.api;

import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;

import vazkii.botania.api.block.*;
import vazkii.botania.api.item.*;
import vazkii.botania.api.mana.ManaCollisionGhost;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaTrigger;
import vazkii.botania.api.mana.spark.SparkAttachable;

public final class BotaniaForgeCapabilities {
	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("botania", path);
	}

	public static final ItemCapability<AvatarWieldable, Void> AVATAR_WIELDABLE = ItemCapability.createVoid(id("avatar_wieldable"), AvatarWieldable.class);
	public static final ItemCapability<BlockProvider, Void> BLOCK_PROVIDER = ItemCapability.createVoid(id("block_provider"), BlockProvider.class);
	public static final ItemCapability<CoordBoundItem, Void> COORD_BOUND_ITEM = ItemCapability.createVoid(id("coord_bound_item"), CoordBoundItem.class);
	public static final ItemCapability<ManaItem, Void> MANA_ITEM = ItemCapability.createVoid(id("mana_item"), ManaItem.class);
	public static final ItemCapability<Relic, Void> RELIC = ItemCapability.createVoid(id("relic"), Relic.class);

	public static final BlockCapability<ExoflameHeatable, Void> EXOFLAME_HEATABLE = BlockCapability.createVoid(id("exoflame_heatable"), ExoflameHeatable.class);
	public static final BlockCapability<HornHarvestable, Void> HORN_HARVEST = BlockCapability.createVoid(id("horn_harvest"), HornHarvestable.class);
	public static final BlockCapability<HourglassTrigger, Void> HOURGLASS_TRIGGER = BlockCapability.createVoid(id("hourglass_trigger"), HourglassTrigger.class);
	public static final BlockCapability<ManaCollisionGhost, Void> MANA_GHOST = BlockCapability.createVoid(id("mana_ghost"), ManaCollisionGhost.class);
	public static final BlockCapability<ManaReceiver, Direction> MANA_RECEIVER = BlockCapability.createSided(id("mana_receiver"), ManaReceiver.class);
	public static final BlockCapability<SparkAttachable, Direction> SPARK_ATTACHABLE = BlockCapability.createSided(id("spark_attachable"), SparkAttachable.class);
	public static final BlockCapability<ManaTrigger, Void> MANA_TRIGGER = BlockCapability.createVoid(id("mana_trigger"), ManaTrigger.class);
	public static final BlockCapability<Wandable, Void> WANDABLE = BlockCapability.createVoid(id("wandable"), Wandable.class);
	public static final BlockCapability<PhantomInkableBlock, Void> PHANTOM_INKABLE = BlockCapability.createVoid(id("phantom_inkable"), PhantomInkableBlock.class);

	private BotaniaForgeCapabilities() {}
}
