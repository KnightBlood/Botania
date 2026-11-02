package vazkii.botania.api;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

import vazkii.botania.api.block.*;
import vazkii.botania.api.item.AvatarWieldable;
import vazkii.botania.api.item.BlockProvider;
import vazkii.botania.api.item.CoordBoundItem;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaCollisionGhost;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaTrigger;
import vazkii.botania.api.mana.spark.SparkAttachable;

public final class BotaniaFabricCapabilities {
	public static final ItemApiLookup<AvatarWieldable, Unit> AVATAR_WIELDABLE = ItemApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "avatar_wieldable"), AvatarWieldable.class, Unit.class);
	public static final ItemApiLookup<BlockProvider, Unit> BLOCK_PROVIDER = ItemApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "block_provider"), BlockProvider.class, Unit.class);
	public static final ItemApiLookup<CoordBoundItem, Unit> COORD_BOUND_ITEM = ItemApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "coord_bound_item"),
			CoordBoundItem.class, Unit.class);
	public static final ItemApiLookup<ManaItem, Unit> MANA_ITEM = ItemApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "mana_item"),
			ManaItem.class, Unit.class);
	public static final ItemApiLookup<Relic, Unit> RELIC = ItemApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "relic"),
			Relic.class, Unit.class);

	public static final BlockApiLookup<ExoflameHeatable, Unit> EXOFLAME_HEATABLE = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "exoflame_heatable"), ExoflameHeatable.class, Unit.class);
	public static final BlockApiLookup<HornHarvestable, Unit> HORN_HARVEST = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "horn_harvestable"), HornHarvestable.class, Unit.class);
	public static final BlockApiLookup<HourglassTrigger, Unit> HOURGLASS_TRIGGER = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "hourglass_trigger"), HourglassTrigger.class, Unit.class);
	public static final BlockApiLookup<ManaCollisionGhost, Unit> MANA_GHOST = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "mana_ghost"), ManaCollisionGhost.class, Unit.class);
	public static final BlockApiLookup<ManaReceiver, /* @Nullable */ Direction> MANA_RECEIVER = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "mana_receiver"), ManaReceiver.class, Direction.class);
	public static final BlockApiLookup<SparkAttachable, Direction> SPARK_ATTACHABLE = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "spark_attachable"), SparkAttachable.class, Direction.class);
	public static final BlockApiLookup<ManaTrigger, Unit> MANA_TRIGGER = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "mana_trigger"), ManaTrigger.class, Unit.class);
	public static final BlockApiLookup<Wandable, Unit> WANDABLE = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "wandable"), Wandable.class, Unit.class);
	public static final BlockApiLookup<PhantomInkableBlock, Unit> PHANTOM_INKABLE = BlockApiLookup.get(Identifier.fromNamespaceAndPath(BotaniaAPI.MODID, "phantom_inkable"), PhantomInkableBlock.class, Unit.class);

	private BotaniaFabricCapabilities() {}
}
