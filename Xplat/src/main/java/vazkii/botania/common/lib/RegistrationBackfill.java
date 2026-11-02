/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.lib;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import vazkii.botania.mixin.BlockBehaviourAccessor;
import vazkii.botania.mixin.BlockPropertiesAccessor;
import vazkii.botania.mixin.ItemAccessor;
import vazkii.botania.mixin.ItemPropertiesAccessor;

/**
 * 26.1 requires {@code Properties.setId} to be called before a Block/Item is constructed, but
 * Botania constructs its registry objects as static fields whose registration ids are only known
 * later. The BlockBehaviour/Item mixins substitute placeholder values during construction and
 * queue the objects here; the registrar wrappers below then backfill the real ids (and the values
 * derived from them) right before actual registration, so behaviour matches vanilla.
 */
public final class RegistrationBackfill {
	private RegistrationBackfill() {
	}

	// Placeholder key handed to the (skipped) DataComponentInitializers.add call during
	// pending item construction; the real initializer is added during backfill.
	public static final ResourceKey<Item> PENDING_ITEM_KEY = ResourceKey.create(Registries.ITEM,
			Identifier.withDefaultNamespace("botania_pending"));

	private static final Map<Item, PendingItemInit> PENDING_ITEMS = new HashMap<>();

	// 26.1: backfilling mutates Properties.id, and several Botania registration sites hand the very
	// same Properties instance to a whole group of blocks or items (e.g. BotaniaBlocks
	// .registerItemBlocks builds one Item.Properties and passes it to every block item). After the
	// first member of such a group has been backfilled its Properties no longer looks "id-less", so
	// every later member used to be mistaken for a vanilla-style construction that needs no work.
	// That silently dropped their DataComponentInitializers entry, and 26.1 binds anything without
	// one to DataComponentMap.EMPTY, i.e. an item with no name and no item model. Remembering which
	// instances have already been consumed keeps the deferred path active for the whole group.
	private static final Set<Item.Properties> CONSUMED_ITEM_PROPS = Collections.newSetFromMap(new IdentityHashMap<>());
	private static final Set<BlockBehaviour.Properties> CONSUMED_BLOCK_PROPS = Collections.newSetFromMap(new IdentityHashMap<>());

	/**
	 * Whether a block/item built from these properties still needs its id-derived values backfilled:
	 * either the id was never known (the usual Botania case) or the properties have already served a
	 * previous member of the same group and therefore currently hold that member's id.
	 */
	public static boolean isDeferredItem(Item.Properties props) {
		return ((ItemPropertiesAccessor) (Object) props).botania$getId() == null
				|| CONSUMED_ITEM_PROPS.contains(props);
	}

	public static boolean isDeferredBlock(BlockBehaviour.Properties props) {
		return ((BlockPropertiesAccessor) (Object) props).botania$getId() == null
				|| CONSUMED_BLOCK_PROPS.contains(props);
	}

	private record PendingItemInit(Item.Properties props, Component name) {
	}

	// Called from ItemMixin when construction happens without an id set on the properties.
	public static void queuePendingItem(Item item, Item.Properties props, Component name) {
		PENDING_ITEMS.put(item, new PendingItemInit(props, name));
	}

	public static BiConsumer<Block, Identifier> blockRegistrar(BiConsumer<Block, Identifier> inner) {
		return (block, id) -> {
			backfillBlock(block, id);
			inner.accept(block, id);
		};
	}

	public static BiConsumer<Item, Identifier> itemRegistrar(BiConsumer<Item, Identifier> inner) {
		return (item, id) -> {
			backfillItem(item, id);
			inner.accept(item, id);
		};
	}

	private static void backfillBlock(Block block, Identifier id) {
		BlockBehaviour.Properties props = ((BlockBehaviourAccessor) block).botania$getProperties();
		if (!isDeferredBlock(props)) {
			// id was already set before construction (vanilla-style), nothing to do
			return;
		}
		BlockPropertiesAccessor propsAccessor = (BlockPropertiesAccessor) (Object) props;
		propsAccessor.botania$setId(ResourceKey.create(Registries.BLOCK, id));
		((BlockBehaviourAccessor) block).botania_setDrops(propsAccessor.botania$effectiveDrops());
		((BlockBehaviourAccessor) block).botania_setDescriptionId(propsAccessor.botania$effectiveDescriptionId());
		CONSUMED_BLOCK_PROPS.add(props);
	}

	private static void backfillItem(Item item, Identifier id) {
		PendingItemInit pending = PENDING_ITEMS.remove(item);
		if (pending == null) {
			// id was already set before construction (vanilla-style), nothing to do
			return;
		}
		ItemPropertiesAccessor propsAccessor = (ItemPropertiesAccessor) (Object) pending.props();
		propsAccessor.botania$setId(ResourceKey.create(Registries.ITEM, id));
		String descriptionId = propsAccessor.botania$effectiveDescriptionId();
		((ItemAccessor) item).botania_setDescriptionId(descriptionId);
		DataComponentInitializers.Initializer<Item> init = propsAccessor
				.botania$finalizeInitializer(Component.translatable(descriptionId), propsAccessor.botania$effectiveModel());
		BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.add(ResourceKey.create(Registries.ITEM, id), init);
		CONSUMED_ITEM_PROPS.add(pending.props());
	}
}
