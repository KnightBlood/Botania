package vazkii.botania.mixin;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
	@Accessor("properties")
	BlockBehaviour.Properties botania$getProperties();

	// 26.1: both fields are cached at construction from Properties.id; backfilled during registration
	@Mutable
	@Accessor("drops")
	void botania_setDrops(Optional<ResourceKey<LootTable>> drops);

	@Mutable
	@Accessor("descriptionId")
	void botania_setDescriptionId(String descriptionId);
}
