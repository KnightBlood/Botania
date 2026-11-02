package vazkii.botania.mixin;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.Properties.class)
public interface ItemPropertiesAccessor {
	// 26.1: Properties.id is required before Item construction; Botania backfills it during registration
	@Accessor("id")
	ResourceKey<Item> botania$getId();

	@Accessor("id")
	void botania$setId(ResourceKey<Item> id);

	@Invoker("effectiveDescriptionId")
	String botania$effectiveDescriptionId();

	@Invoker("effectiveModel")
	Identifier botania$effectiveModel();

	@Invoker("finalizeInitializer")
	DataComponentInitializers.Initializer<Item> botania$finalizeInitializer(Component name, Identifier model);
}
