package vazkii.botania.mixin;

import java.util.Optional;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockPropertiesAccessor {
	@Accessor("hasCollision")
	void botania_setHasCollision(boolean value);

	@Accessor("isRandomlyTicking")
	void botania_setIsRandomlyTicking(boolean value);

	@Accessor("replaceable")
	void botania_setReplaceable(boolean value);

	// 26.1: Properties.id is required before Block construction; Botania backfills it during registration
	@Accessor("id")
	ResourceKey<Block> botania$getId();

	@Accessor("id")
	void botania$setId(ResourceKey<Block> id);

	@Invoker("effectiveDrops")
	Optional<ResourceKey<LootTable>> botania$effectiveDrops();

	@Invoker("effectiveDescriptionId")
	String botania$effectiveDescriptionId();
}
